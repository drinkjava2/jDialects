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

import com.github.drinkjava2.jdialects.model.AutoIdGen;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyConst;
import com.github.drinkjava2.jdialects.model.IndexConst;
import com.github.drinkjava2.jdialects.model.SequenceGen;
import com.github.drinkjava2.jdialects.model.TableGen;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.model.UniqueConst;
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
		List<TableGen> tbGeneratorList = new ArrayList<TableGen>();
		List<SequenceGen> sequenceList = new ArrayList<SequenceGen>();
		List<AutoIdGen> globalIdGeneratorList = new ArrayList<AutoIdGen>();
		List<FKeyConst> fKeyConstraintList = new ArrayList<FKeyConst>();

		for (Object strOrObj : objectResultList) {
			if (!StrUtils.isEmpty(strOrObj)) {
				if (strOrObj instanceof String)
					stringResultList.add((String) strOrObj);
				else if (strOrObj instanceof TableGen)
					tbGeneratorList.add((TableGen) strOrObj);
				else if (strOrObj instanceof SequenceGen)
					sequenceList.add((SequenceGen) strOrObj);
				else if (strOrObj instanceof AutoIdGen)
					globalIdGeneratorList.add((AutoIdGen) strOrObj);
				else if (strOrObj instanceof FKeyConst)
					fKeyConstraintList.add((FKeyConst) strOrObj);
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

		List<IndexConst> l = t.getIndexConsts();// check index names
		if (l != null && !l.isEmpty())
			for (IndexConst index : l)
				dialect.checkReservedWords(index.getName());

		List<UniqueConst> l2 = t.getUniqueConsts();// check unique names
		if (l2 != null && !l2.isEmpty())
			for (UniqueConst unique : l2)
				dialect.checkReservedWords(unique.getName());

		List<FKeyConst> fkeyChks = t.getFkeyConstraints();// check Fkey names
		if (fkeyChks != null && !fkeyChks.isEmpty())
			for (FKeyConst fkey : fkeyChks)
				dialect.checkReservedWords(fkey.getFkeyName());

		for (ColumnModel col : columns)
			dialect.checkNotEmptyReservedWords(col.getColumnName(), "Column name can not be empty");

		for (ColumnModel col : columns) {
			// autoGenerator, only support sequence or table for "Auto" type
			if (col.getAutoGenerator()) {// if support sequence
				if (features.supportBasicOrPooledSequence()) {
					objectResultList.add(new SequenceGen(AutoIdGen.JDIALECTS_AUTOID, AutoIdGen.JDIALECTS_AUTOID, 1, 1));
				} else {// AutoIdGen
					objectResultList.add(new AutoIdGen());
				}
			}
		}

		// sequence
		for (SequenceGen seq : t.getSequences())
			objectResultList.add(seq);

		// tableGenerator
		for (TableGen tableGenerator : t.getTableGenerators())
			objectResultList.add(tableGenerator);

		// Foreign key
		for (FKeyConst fkey : t.getFkeyConstraints())
			objectResultList.add(fkey);

		// drop table
		buf.append(dialect.dropTableDDL(tableName));
		objectResultList.add(buf.toString());
	}

	private static void buildDropSequenceDDL(Dialect dialect, List<String> stringResultList,
			List<SequenceGen> sequenceList) {
		DDLFeatures features = dialect.ddlFeatures;
		for (SequenceGen seq : sequenceList) {
			DialectException.assureNotEmpty(seq.getName(), "SequenceGen name can not be empty");
			DialectException.assureNotEmpty(seq.getSequenceName(),
					"sequenceName can not be empty of \"" + seq.getName() + "\"");
		}

		for (SequenceGen seq : sequenceList) {
			for (SequenceGen seq2 : sequenceList) {
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
		for (SequenceGen seq : sequenceList) {
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
			List<TableGen> tbGeneratorList) {
		for (TableGen tg : tbGeneratorList) {
			//@formatter:off
			DialectException.assureNotEmpty(tg.getName(), "TableGen name can not be empty"); 
			DialectException.assureNotEmpty(tg.getTableName(), "TableGen tableName can not be empty of \""+tg.getName()+"\"");
			DialectException.assureNotEmpty(tg.getPkColumnName(), "TableGen pkColumnName can not be empty of \""+tg.getName()+"\"");
			DialectException.assureNotEmpty(tg.getPkColumnValue(), "TableGen pkColumnValue can not be empty of \""+tg.getName()+"\"");
			DialectException.assureNotEmpty(tg.getValueColumnName(), "TableGen valueColumnName can not be empty of \""+tg.getName()+"\""); 
			//@formatter:on
		}

		Set<String> tableExisted = new HashSet<String>();
		for (TableGen tg : tbGeneratorList) {
			String tableName = tg.getTableName().toLowerCase();
			if (!tableExisted.contains(tableName)) {
				stringResultList.add(0, dialect.dropTableDDL(tableName));
				tableExisted.add(tableName);
			}
		}
	}

	private static void buildDropGolbalIDGeneratorDDL(Dialect dialect, List<String> stringResultList,
			List<AutoIdGen> globalIdGeneratorList) {
		if (globalIdGeneratorList != null && !globalIdGeneratorList.isEmpty())
			stringResultList.add(0, dialect.dropTableDDL(AutoIdGen.JDIALECTS_AUTOID));
	}

	private static void outputDropFKeyConstraintDDL(Dialect dialect, List<String> stringResultList,
			List<FKeyConst> trueList) {
		if (DDLFeatures.NOT_SUPPORT.equals(dialect.ddlFeatures.addForeignKeyConstraintString))
			return;
		for (FKeyConst t : trueList) {
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
