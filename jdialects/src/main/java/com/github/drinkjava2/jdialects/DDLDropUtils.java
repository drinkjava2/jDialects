/**
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.drinkjava2.jdialects.model.AutoIdGenerator;
import com.github.drinkjava2.jdialects.model.Column;
import com.github.drinkjava2.jdialects.model.FKeyConstraint;
import com.github.drinkjava2.jdialects.model.InlineFKeyConstraint;
import com.github.drinkjava2.jdialects.model.Sequence;
import com.github.drinkjava2.jdialects.model.Table;
import com.github.drinkjava2.jdialects.model.TableGenerator;

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
	public static String[] toDropDDL(Dialect dialect, Table... tables) {
		// resultList store mixed drop DDL + drop Ojbects
		List<Object> objectResultList = new ArrayList<Object>();

		for (Table table : tables)
			transferTableToObjectList(dialect, table, objectResultList);

		List<String> stringResultList = new ArrayList<String>();
		List<TableGenerator> tbGeneratorList = new ArrayList<TableGenerator>();
		List<Sequence> sequenceList = new ArrayList<Sequence>();
		List<AutoIdGenerator> globalIdGeneratorList = new ArrayList<AutoIdGenerator>();
		List<InlineFKeyConstraint> inlinefKeyConstraintList = new ArrayList<InlineFKeyConstraint>();
		List<FKeyConstraint> fKeyConstraintList = new ArrayList<FKeyConstraint>();

		for (Object strOrObj : objectResultList) {
			if (!StrUtils.isEmpty(strOrObj)) {
				if (strOrObj instanceof String)
					stringResultList.add((String) strOrObj);
				else if (strOrObj instanceof TableGenerator)
					tbGeneratorList.add((TableGenerator) strOrObj);
				else if (strOrObj instanceof Sequence)
					sequenceList.add((Sequence) strOrObj);
				else if (strOrObj instanceof AutoIdGenerator)
					globalIdGeneratorList.add((AutoIdGenerator) strOrObj);
				else if (strOrObj instanceof InlineFKeyConstraint)
					inlinefKeyConstraintList.add((InlineFKeyConstraint) strOrObj);
				else if (strOrObj instanceof FKeyConstraint)
					fKeyConstraintList.add((FKeyConstraint) strOrObj);
			}
		}

		buildDropSequenceDDL(dialect, stringResultList, sequenceList);
		buildDropTableGeneratorDDL(dialect, stringResultList, tbGeneratorList);
		buildDropGolbalIDGeneratorDDL(dialect, stringResultList, globalIdGeneratorList);
		buildDropFKeyConstraintDDL(dialect, stringResultList, inlinefKeyConstraintList);
		outputDropFKeyConstraintDDL(dialect, stringResultList, fKeyConstraintList);

		return stringResultList.toArray(new String[stringResultList.size()]);
	}

	/**
	 * Transfer table to a mixed DDL String or TableGenerator Object list
	 */
	private static void transferTableToObjectList(Dialect dialect, Table t, List<Object> objectResultList) {
		DDLFeatures features = dialect.ddlFeatures;

		StringBuilder buf = new StringBuilder();
		String tableName = t.getTableName();
		Map<String, Column> columns = t.getColumns();

		// Reserved words check
		dialect.checkNotEmptyReservedWords(tableName, "Table name can not be empty");
		for (Column col : columns.values()) {
			dialect.checkNotEmptyReservedWords(col.getColumnName(), "Column name can not be empty");
			dialect.checkReservedWords(col.getPkeyName());
			dialect.checkReservedWords(col.getUniqueConstraintNames());
			dialect.checkReservedWords(col.getIndexNames()); 
		}

		for (Column col : columns.values()) {
			// autoGenerator, only support sequence or table for "Auto" type
			if (col.getAutoGenerator()) {// if support sequence
				if (features.supportBasicOrPooledSequence()) {
					objectResultList.add(
							new Sequence(AutoIdGenerator.JDIALECTS_AUTOID, AutoIdGenerator.JDIALECTS_AUTOID, 1, 1));
				} else {// AutoIdGenerator
					objectResultList.add(new AutoIdGenerator());
				}
			}

			// foreign keys
			if (!StrUtils.isEmpty(col.getFkeyReferenceTable()))
				objectResultList.add(new InlineFKeyConstraint(tableName, col.getColumnName(),
						col.getFkeyReferenceTable(), col.getFkeyReferenceColumns()));
		}

		// sequence
		for (Sequence seq : t.getSequences().values())
			objectResultList.add(seq);

		// tableGenerator
		for (TableGenerator tableGenerator : t.getTableGenerators().values())
			objectResultList.add(tableGenerator);

		// Foreign key
		for (FKeyConstraint fkey : t.getFkeyConstraints())
			objectResultList.add(fkey);

		// drop table
		buf.append(dialect.dropTableDDL(tableName));
		objectResultList.add(buf.toString());
	}

	private static void buildDropSequenceDDL(Dialect dialect, List<String> stringResultList,
			List<Sequence> sequenceList) {
		DDLFeatures features = dialect.ddlFeatures;
		for (Sequence seq : sequenceList) {
			DialectException.assureNotEmpty(seq.getName(), "Sequence name can not be empty");
			DialectException.assureNotEmpty(seq.getSequenceName(),
					"sequenceName can not be empty of \"" + seq.getName() + "\"");
		}

		for (Sequence seq : sequenceList) {
			for (Sequence seq2 : sequenceList) {
				if (seq != seq2 && (seq2.getAllocationSize() != 0)) {
					if (seq.getName().equalsIgnoreCase(seq2.getName())) {
						seq.setAllocationSize(0);// set to 0 to skip repeated
					} else {
						if (seq.getSequenceName().equalsIgnoreCase(seq2.getSequenceName()))
							DialectException.throwEX("Dulplicated Sequence setting \"" + seq.getName() + "\" and \""
									+ seq2.getName() + "\" found.");
					}
				}
			}
		}

		Set<String> sequenceNameExisted = new HashSet<String>();
		for (Sequence seq : sequenceList) {
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
			List<TableGenerator> tbGeneratorList) {
		for (TableGenerator tg : tbGeneratorList) {
			//@formatter:off
			DialectException.assureNotEmpty(tg.getName(), "TableGenerator name can not be empty"); 
			DialectException.assureNotEmpty(tg.getTableName(), "TableGenerator tableName can not be empty of \""+tg.getName()+"\"");
			DialectException.assureNotEmpty(tg.getPkColumnName(), "TableGenerator pkColumnName can not be empty of \""+tg.getName()+"\"");
			DialectException.assureNotEmpty(tg.getPkColumnValue(), "TableGenerator pkColumnValue can not be empty of \""+tg.getName()+"\"");
			DialectException.assureNotEmpty(tg.getValueColumnName(), "TableGenerator valueColumnName can not be empty of \""+tg.getName()+"\""); 
			//@formatter:on
		}

		Set<String> tableExisted = new HashSet<String>();
		for (TableGenerator tg : tbGeneratorList) {
			String tableName = tg.getTableName().toLowerCase();
			if (!tableExisted.contains(tableName)) {
				stringResultList.add(0, dialect.dropTableDDL(tableName));
				tableExisted.add(tableName);
			}
		}
	}

	private static void buildDropGolbalIDGeneratorDDL(Dialect dialect, List<String> stringResultList,
			List<AutoIdGenerator> globalIdGeneratorList) {
		if (globalIdGeneratorList != null && globalIdGeneratorList.size() > 0)
			stringResultList.add(0, dialect.dropTableDDL(AutoIdGenerator.JDIALECTS_AUTOID));
	}

	private static void buildDropFKeyConstraintDDL(Dialect dialect, List<String> stringResultList,
			List<InlineFKeyConstraint> fKeyConstraintList) {
		for (InlineFKeyConstraint kfc : fKeyConstraintList) {
			dialect.checkNotEmptyReservedWords(kfc.getFkeyReferenceTable(), "FkeyReferenceTable can not be empty");
			for (String refColName : kfc.getRefColumnNames())
				dialect.checkNotEmptyReservedWords(refColName, "FkeyReferenceColumn name can not be empty");
		}
		/*
		 * join table col1 refTable ref1 ref2 + table col2 refTable ref1 ref2
		 * into one
		 */
		List<FKeyConstraint> trueList = new ArrayList<FKeyConstraint>();
		for (int i = 0; i < fKeyConstraintList.size(); i++) {
			InlineFKeyConstraint fk = fKeyConstraintList.get(i);
			FKeyConstraint temp = new FKeyConstraint(fk);
			temp.getColumnNames().add(fk.getColumnName());
			if (i == 0) {
				trueList.add(temp);
			} else {
				FKeyConstraint found = null;
				for (FKeyConstraint old : trueList) {
					if (fk.getTableName().equals(old.getTableName())
							&& fk.getFkeyReferenceTable().equals(old.getRefTableName())
							&& StrUtils.arraysEqual(fk.getRefColumnNames(), old.getRefColumnNames())) {
						found = old;
					}
				}
				if (found == null)
					trueList.add(temp);
				else
					found.getColumnNames().add(fk.getColumnName());
			}
		}

		outputDropFKeyConstraintDDL(dialect, stringResultList, trueList);
	}

	private static void outputDropFKeyConstraintDDL(Dialect dialect, List<String> stringResultList,
			List<FKeyConstraint> trueList) {
		if (DDLFeatures.NOT_SUPPORT.equals(dialect.ddlFeatures.addForeignKeyConstraintString))
			return;
		for (FKeyConstraint t : trueList) {
			String dropStr = dialect.ddlFeatures.dropForeignKeyString;
			String fkname = "fk_" + t.getTableName().toLowerCase() + "_"
					+ StrUtils.replace(StrUtils.listToString(t.getColumnNames()), ",", "_");

			if (DDLFeatures.NOT_SUPPORT.equals(dropStr))
				DialectException.throwEX("Dialect \"" + dialect
						+ "\" does not support drop foreign key, for setting: \"" + "fk_" + fkname + "\"");
			stringResultList.add(0, "alter table " + t.getTableName() + " " + dropStr + " " + fkname);
		}
	}
}
