/**
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.drinkjava2.jdialects.id.AutoIdGenerator;
import com.github.drinkjava2.jdialects.id.IdGenerator;
import com.github.drinkjava2.jdialects.id.SequenceIdGenerator;
import com.github.drinkjava2.jdialects.id.TableIdGenerator;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.IndexModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.model.UniqueModel;
import com.github.drinkjava2.jdialects.utils.StrUtils;

/**
 * To transfer platform-independent model to drop DDL String array
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class DDLDropUtils {

	/**
	 * Transfer tables to drop DDL and without format it
	 */
	public static String[] toDropDDL(Dialect dialect, TableModel... tables) {
		// resultList store mixed drop DDL + drop Ojbects
		List<Object> objectResultList = new ArrayList<Object>();

		for (TableModel table : tables)
			transferTableToObjectList(dialect, table, objectResultList);

		List<String> stringResultList = new ArrayList<String>();
		List<TableIdGenerator> tbGeneratorList = new ArrayList<TableIdGenerator>();
		List<SequenceIdGenerator> sequenceList = new ArrayList<SequenceIdGenerator>();
		List<AutoIdGenerator> globalIdGeneratorList = new ArrayList<AutoIdGenerator>();
		List<FKeyModel> fKeyConstraintList = new ArrayList<FKeyModel>();

		for (Object strOrObj : objectResultList) {
			if (!StrUtils.isEmpty(strOrObj)) {
				if (strOrObj instanceof String)
					stringResultList.add((String) strOrObj);
				else if (strOrObj instanceof TableIdGenerator)
					tbGeneratorList.add((TableIdGenerator) strOrObj);
				else if (strOrObj instanceof SequenceIdGenerator)
					sequenceList.add((SequenceIdGenerator) strOrObj);
				else if (strOrObj instanceof AutoIdGenerator)
					globalIdGeneratorList.add((AutoIdGenerator) strOrObj);
				else if (strOrObj instanceof FKeyModel)
					fKeyConstraintList.add((FKeyModel) strOrObj);
			}
		}

		buildDropSequenceDDL(dialect, stringResultList, sequenceList);
		buildDropTableGeneratorDDL(dialect, stringResultList, tbGeneratorList);
		buildDropGolbalIDGeneratorDDL(dialect, stringResultList, globalIdGeneratorList);
		outputDropFKeyConstraintDDL(dialect, stringResultList, fKeyConstraintList);

		return stringResultList.toArray(new String[stringResultList.size()]);
	}

	/**
	 * Transfer table to a mixed DDL String or TableGen Object list
	 */
	private static void transferTableToObjectList(Dialect dialect, TableModel t, List<Object> objectResultList) {
		DDLFeatures features = dialect.ddlFeatures;

		StringBuilder buf = new StringBuilder();
		String tableName = t.getTableName();
		List<ColumnModel> columns = t.getColumns();

		// Reserved words check
		dialect.checkNotEmptyReservedWords(tableName, "Table name can not be empty");

		List<IndexModel> l = t.getIndexConsts();// check index names
		if (l != null && !l.isEmpty())
			for (IndexModel index : l)
				dialect.checkReservedWords(index.getName());

		List<UniqueModel> l2 = t.getUniqueConsts();// check unique names
		if (l2 != null && !l2.isEmpty())
			for (UniqueModel unique : l2)
				dialect.checkReservedWords(unique.getName());

		List<FKeyModel> fkeyChks = t.getFkeyConstraints();// check Fkey names
		if (fkeyChks != null && !fkeyChks.isEmpty())
			for (FKeyModel fkey : fkeyChks)
				dialect.checkReservedWords(fkey.getFkeyName());

		for (ColumnModel col : columns)
			dialect.checkNotEmptyReservedWords(col.getColumnName(), "Column name can not be empty");

		for (ColumnModel col : columns) {
			if (col.getTransientable())
				continue;
			// autoGenerator, only support sequence or table for "Auto" type
			if (col.getAutoGenerator()) {// if support sequence
				if (features.supportBasicOrPooledSequence()) {
					objectResultList.add(new SequenceIdGenerator(AutoIdGenerator.JDIALECTS_AUTOID, AutoIdGenerator.JDIALECTS_AUTOID, 1, 1));
				} else {// AutoIdGen
					objectResultList.add(new AutoIdGenerator());
				}
			}
		}

		//idGenerator
		for (IdGenerator idGen : t.getIdGenerators())
			objectResultList.add(idGen);

		// Foreign key
		for (FKeyModel fkey : t.getFkeyConstraints())
			objectResultList.add(fkey);

		// drop table
		buf.append(dialect.dropTableDDL(tableName));
		objectResultList.add(buf.toString());
	}

	private static void buildDropSequenceDDL(Dialect dialect, List<String> stringResultList,
			List<SequenceIdGenerator> sequenceList) {
		DDLFeatures features = dialect.ddlFeatures;
		for (SequenceIdGenerator seq : sequenceList) {
			DialectException.assureNotEmpty(seq.getName(), "SequenceGen name can not be empty");
			DialectException.assureNotEmpty(seq.getSequenceName(),
					"sequenceName can not be empty of \"" + seq.getName() + "\"");
		}

		for (SequenceIdGenerator seq : sequenceList) {
			for (SequenceIdGenerator seq2 : sequenceList) {
				if (seq != seq2 && (seq2.getAllocationSize() != 0)) {
					if (seq.getName().equalsIgnoreCase(seq2.getName())) {
						seq.setAllocationSize(0);// set to 0 to skip repeated
					} else {
						if (seq.getSequenceName().equalsIgnoreCase(seq2.getSequenceName()))
							DialectException.throwEX("Dulplicated SequenceGen setting \"" + seq.getName() + "\" and \""
									+ seq2.getName() + "\" found.");
					}
				}
			}
		}

		Set<String> sequenceNameExisted = new HashSet<String>();
		for (SequenceIdGenerator seq : sequenceList) {
			if (seq.getAllocationSize() != 0) {
				String sequenceName = seq.getSequenceName().toLowerCase();
				if (!sequenceNameExisted.contains(sequenceName)) {
					if (!features.supportBasicOrPooledSequence()) {
						DialectException.throwEX("Dialect \"" + dialect
								+ "\" does not support sequence setting on sequence \"" + seq.getName() + "\"");
					}
					if (!DDLFeatures.NOT_SUPPORT.equals(features.dropSequenceStrings)
							&& !StrUtils.isEmpty(features.dropSequenceStrings)) {
						stringResultList.add(0,
								StrUtils.replace(features.dropSequenceStrings, "_SEQNAME", seq.getSequenceName()));
					} else
						DialectException.throwEX("Dialect \"" + dialect
								+ "\" does not support drop sequence ddl, on sequence \"" + seq.getName() + "\"");
					sequenceNameExisted.add(sequenceName);
				}
			}
		}

	}

	private static void buildDropTableGeneratorDDL(Dialect dialect, List<String> stringResultList,
			List<TableIdGenerator> tbGeneratorList) {
		for (TableIdGenerator tg : tbGeneratorList) {
			//@formatter:off
			DialectException.assureNotEmpty(tg.getName(), "TableGen name can not be empty"); 
			DialectException.assureNotEmpty(tg.getTable(), "TableGen tableName can not be empty of \""+tg.getName()+"\"");
			DialectException.assureNotEmpty(tg.getPkColumnName(), "TableGen pkColumnName can not be empty of \""+tg.getName()+"\"");
			DialectException.assureNotEmpty(tg.getPkColumnValue(), "TableGen pkColumnValue can not be empty of \""+tg.getName()+"\"");
			DialectException.assureNotEmpty(tg.getValueColumnName(), "TableGen valueColumnName can not be empty of \""+tg.getName()+"\""); 
			//@formatter:on
		}

		Set<String> tableExisted = new HashSet<String>();
		for (TableIdGenerator tg : tbGeneratorList) {
			String tableName = tg.getTable().toLowerCase();
			if (!tableExisted.contains(tableName)) {
				stringResultList.add(0, dialect.dropTableDDL(tableName));
				tableExisted.add(tableName);
			}
		}
	}

	private static void buildDropGolbalIDGeneratorDDL(Dialect dialect, List<String> stringResultList,
			List<AutoIdGenerator> globalIdGeneratorList) {
		if (globalIdGeneratorList != null && !globalIdGeneratorList.isEmpty())
			stringResultList.add(0, dialect.dropTableDDL(AutoIdGenerator.JDIALECTS_AUTOID));
	}

	private static void outputDropFKeyConstraintDDL(Dialect dialect, List<String> stringResultList,
			List<FKeyModel> trueList) {
		if (DDLFeatures.NOT_SUPPORT.equals(dialect.ddlFeatures.addForeignKeyConstraintString))
			return;
		for (FKeyModel t : trueList) {
			String dropStr = dialect.ddlFeatures.dropForeignKeyString;
			String constName = t.getFkeyName();
			if (StrUtils.isEmpty(constName))
				constName = "fk_" + t.getTableName().toLowerCase() + "_"
						+ StrUtils.replace(StrUtils.listToString(t.getColumnNames()), ",", "_");
			if (DDLFeatures.NOT_SUPPORT.equals(dropStr))
				DialectException.throwEX("Dialect \"" + dialect
						+ "\" does not support drop foreign key, for setting: \"" + "fk_" + constName + "\"");
			stringResultList.add(0, "alter table " + t.getTableName() + " " + dropStr + " " + constName);
		}
	}
}
