/**
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.drinkjava2.jdialects.model.AutoIdGenerator;
import com.github.drinkjava2.jdialects.model.Column;
import com.github.drinkjava2.jdialects.model.FKeyConstraint;
import com.github.drinkjava2.jdialects.model.InlineFKeyConstraint;
import com.github.drinkjava2.jdialects.model.Sequence;
import com.github.drinkjava2.jdialects.model.Table;
import com.github.drinkjava2.jdialects.model.TableGenerator;

/**
 * To transfer platform-independent model to create DDL String array
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class DDLCreateUtils {
	private static DialectLogger logger = DialectLogger.getLog(DDLCreateUtils.class);

	/**
	 * Transfer tables to DDL by given dialect and without format it, if want
	 * get a formatted DDL, use DDLFormatter.format(DDLs) method to format it
	 */
	public static String[] toCreateDDL(Dialect dialect, Table... tables) {
		// resultList store mixed DDL String + TableGenerator + Sequence
		List<Object> objectResultList = new ArrayList<Object>();

		for (Table table : tables)
			transferTableToObjectList(dialect, table, objectResultList);

		List<String> stringResultList = new ArrayList<String>();
		List<TableGenerator> tbGeneratorList = new ArrayList<TableGenerator>();
		List<Sequence> sequenceList = new ArrayList<Sequence>();
		List<AutoIdGenerator> autoIdGeneratorList = new ArrayList<AutoIdGenerator>();
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
					autoIdGeneratorList.add((AutoIdGenerator) strOrObj);
				else if (strOrObj instanceof InlineFKeyConstraint)
					inlinefKeyConstraintList.add((InlineFKeyConstraint) strOrObj);
				else if (strOrObj instanceof FKeyConstraint)
					fKeyConstraintList.add((FKeyConstraint) strOrObj);
			}
		}

		buildSequenceDDL(dialect, stringResultList, sequenceList);
		buildTableGeneratorDDL(dialect, stringResultList, tbGeneratorList);
		buildAutoIdGeneratorDDL(dialect, stringResultList, autoIdGeneratorList);
		buildFKeyConstraintDDL(dialect, stringResultList, inlinefKeyConstraintList);
		outputFKeyConstraintDDL(dialect, stringResultList, fKeyConstraintList);

		return stringResultList.toArray(new String[stringResultList.size()]);
	}

	/**
	 * Transfer table to a mixed DDL String or TableGenerator Object list
	 */
	/**
	 * @param dialect
	 * @param t
	 * @param objectResultList
	 */
	private static void transferTableToObjectList(Dialect dialect, Table t, List<Object> objectResultList) {
		DDLFeatures features = dialect.ddlFeatures;

		StringBuilder buf = new StringBuilder();
		boolean hasPkey = false;
		String pkeys = "";
		String tableName = t.getTableName();
		Map<String, Column> columns = t.getColumns();

		// Reserved words check
		dialect.checkNotEmptyReservedWords(tableName, "Table name can not be empty");
		for (Column col : columns.values()) {
			dialect.checkNotEmptyReservedWords(col.getColumnName(), "Column name can not be empty");
			dialect.checkReservedWords(col.getPkeyName());
			dialect.checkReservedWords(col.getUniqueConstraintName());
		}

		for (Column col : columns.values()) {
			// "Auto" type generator
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

		// check and cache prime keys
		for (Column col : columns.values()) {
			if (col.getPkey()) {
				hasPkey = true;
				if (StrUtils.isEmpty(pkeys))
					pkeys = col.getColumnName();
				else
					pkeys += "," + col.getColumnName();
			}
		}

		// create table
		buf.append(hasPkey ? dialect.ddlFeatures.createTableString : dialect.ddlFeatures.createMultisetTableString)
				.append(" ").append(tableName).append(" (");

		for (Column c : columns.values()) {
			if (c.getColumnType() == null)
				DialectException
						.throwEX("Type not set on column \"" + c.getColumnName() + "\" at table \"" + tableName + "\"");

			// column definition
			buf.append(c.getColumnName()).append(" ");

			// Identity
			if (c.getIdentity() && !features.supportsIdentityColumns)
				DialectException.throwEX("Unsupported identity setting for dialect \"" + dialect + "\" on column \""
						+ c.getColumnName() + "\" at table \"" + tableName + "\"");

			// Column type definition
			if (c.getIdentity()) {
				if (features.hasDataTypeInIdentityColumn)
					buf.append(dialect.translateToDDLType(c.getColumnType(), c.getLengths()));
				buf.append(' ');
				if (Type.BIGINT.equals(c.getColumnType()))
					buf.append(features.identityColumnStringBigINT);
				else
					buf.append(features.identityColumnString);
			} else {
				buf.append(dialect.translateToDDLType(c.getColumnType(), c.getLengths()));

				// Default
				String defaultValue = c.getDefaultValue();
				if (defaultValue != null) {
					buf.append(" default ").append(defaultValue);
				}

				// Not null
				if (c.getNotNull())
					buf.append(" not null");
				else
					buf.append(features.nullColumnString);
			}

			// Check
			if (!StrUtils.isEmpty(c.getCheck())) {
				if (features.supportsColumnCheck)
					buf.append(" check (").append(c.getCheck()).append(")");
				else
					logger.warn("Ignore unsupported check setting for dialect \"" + dialect + "\" on column \""
							+ c.getColumnName() + "\" at table \"" + tableName + "\" with value: " + c.getCheck());
			}

			// Comments
			if (c.getComment() != null) {
				if (StrUtils.isEmpty(features.columnComment) && !features.supportsCommentOn)
					logger.warn("Ignore unsupported comment setting for dialect \"" + dialect + "\" on column \""
							+ c.getColumnName() + "\" at table \"" + tableName + "\" with value: " + c.getComment());
				else
					buf.append(StrUtils.replace(features.columnComment, "_COMMENT", c.getComment()));
			}

			// tail String
			if (!StrUtils.isEmpty(c.getTail()))
				buf.append(c.getTail());

			buf.append(",");
		}
		// PKEY
		if (!StrUtils.isEmpty(pkeys)) {
			buf.append(" primary key (").append(pkeys).append("),");
		}

		// Table Check
		if (!StrUtils.isEmpty(t.getCheck())) {
			if (features.supportsTableCheck)
				buf.append(" check (").append(t.getCheck()).append("),");
			else
				logger.warn("Ignore unsupported table check setting for dialect \"" + dialect + "\" on table \""
						+ tableName + "\" with value: " + t.getCheck());
		}

		buf.setLength(buf.length() - 1);
		buf.append(")");

		// Engine for MariaDB & MySql only, for example "engine=innoDB"
		String tableTypeString = features.tableTypeString;
		if (!StrUtils.isEmpty(tableTypeString) && !DDLFeatures.NOT_SUPPORT.equals(tableTypeString)) {
			buf.append(tableTypeString);

			// EngineTail, for example:" DEFAULT CHARSET=utf8"
			if (!StrUtils.isEmpty(t.getEngineTail()))
				buf.append(t.getEngineTail());
		}

		objectResultList.add(buf.toString());

		// unique constraint
		for (Column column : columns.values())
			addUniqueConstraintDDL(objectResultList, dialect, tableName, column);

		// table comment on
		if (t.getComment() != null) {
			if (features.supportsCommentOn)
				objectResultList.add("comment on table " + t.getTableName() + " is '" + t.getComment() + "'");
			else
				logger.warn("Ignore unsupported table comment setting for dialect \"" + dialect + "\" on table \""
						+ tableName + "\" with value: " + t.getComment());
		}

		// column comment on
		for (Column c : columns.values()) {
			if (features.supportsCommentOn && c.getComment() != null && StrUtils.isEmpty(features.columnComment))
				objectResultList.add(
						"comment on column " + tableName + '.' + c.getColumnName() + " is '" + c.getComment() + "'");
		}

		// index
		buildIndexDLL(dialect, objectResultList, tableName, columns);

	}

	private static void buildSequenceDDL(Dialect dialect, List<String> stringList, List<Sequence> sequenceList) {
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
					if (features.supportsPooledSequences) {
						// create sequence _SEQ start with 11 increment by 33
						String pooledSequence = StrUtils.replace(features.createPooledSequenceStrings, "_SEQ",
								seq.getSequenceName());
						pooledSequence = StrUtils.replace(pooledSequence, "11", "" + seq.getInitialValue());
						pooledSequence = StrUtils.replace(pooledSequence, "33", "" + seq.getAllocationSize());
						stringList.add(pooledSequence);
					} else {
						if (seq.getInitialValue() >= 2 || seq.getAllocationSize() >= 2)
							DialectException.throwEX("Dialect \"" + dialect
									+ "\" does not support initialValue and allocationSize setting on sequence \""
									+ seq.getName() + "\", try set initialValue and allocationSize to 1 to fix");
						// "create sequence _SEQ"
						String simepleSeq = StrUtils.replace(features.createSequenceStrings, "_SEQ",
								seq.getSequenceName());
						stringList.add(simepleSeq);
					}
					sequenceNameExisted.add(sequenceName);
				}
			}
		}

	}

	private static void buildAutoIdGeneratorDDL(Dialect dialect, List<String> stringList,
			List<AutoIdGenerator> autoIdGenerator) {
		if (autoIdGenerator != null && autoIdGenerator.size() > 0) {
			stringList.add(dialect.ddlFeatures.createTableString + " " + AutoIdGenerator.JDIALECTS_AUTOID + " ("
					+ AutoIdGenerator.NEXT_VAL + " " + dialect.translateToDDLType(Type.BIGINT) + " )");
			stringList.add("insert into " + AutoIdGenerator.JDIALECTS_AUTOID + " values ( 1 )");
		}
	}

	private static void buildTableGeneratorDDL(Dialect dialect, List<String> stringList,
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

		for (TableGenerator tg : tbGeneratorList) {
			for (TableGenerator tg2 : tbGeneratorList) {
				if (tg != tg2 && (tg2.getAllocationSize() != 0)) {
					if (tg.getName().equalsIgnoreCase(tg2.getName())) {
						tg.setAllocationSize(0);// set to 0 to skip repeated
					} else {
						if (tg.getTableName().equalsIgnoreCase(tg2.getTableName())
								&& tg.getPkColumnName().equalsIgnoreCase(tg2.getPkColumnName())
								&& tg.getPkColumnValue().equalsIgnoreCase(tg2.getPkColumnValue())
								&& tg.getValueColumnName().equalsIgnoreCase(tg2.getValueColumnName()))
							DialectException.throwEX("Dulplicated tableGenerator setting \"" + tg.getName()
									+ "\" and \"" + tg2.getName() + "\" found.");
					}
				}
			}
		}

		Set<String> tableExisted = new HashSet<String>();
		Set<String> columnExisted = new HashSet<String>();
		for (TableGenerator tg : tbGeneratorList)
			if (tg.getAllocationSize() != 0) {
				String tableName = tg.getTableName().toLowerCase();
				String tableAndPKColumn = tg.getTableName().toLowerCase() + "..XXOO.." + tg.getPkColumnName();
				String tableAndValColumn = tg.getTableName().toLowerCase() + "..XXOO.." + tg.getValueColumnName();
				if (!tableExisted.contains(tableName)) {
					String s = dialect.ddlFeatures.createTableString + " " + tableName + " (";
					s += tg.getPkColumnName() + " " + dialect.translateToDDLType(Type.VARCHAR, 100) + ",";
					s += tg.getValueColumnName() + " " + dialect.translateToDDLType(Type.BIGINT) + " )";
					stringList.add(s);
					tableExisted.add(tableName);
					columnExisted.add(tableAndPKColumn);
					columnExisted.add(tableAndValColumn);
				} else {
					if (!columnExisted.contains(tableAndPKColumn)) {
						stringList.add("alter table " + tableName + " " + dialect.ddlFeatures.addColumnString + " "
								+ tg.getPkColumnName() + " " + dialect.translateToDDLType(Type.VARCHAR, 100) + " "
								+ dialect.ddlFeatures.addColumnSuffixString);
						columnExisted.add(tableAndPKColumn);
					}
					if (!columnExisted.contains(tableAndValColumn)) {
						stringList.add("alter table " + tableName + " " + dialect.ddlFeatures.addColumnString + " "
								+ tg.getValueColumnName() + " " + dialect.translateToDDLType(Type.VARCHAR, 100) + " "
								+ dialect.ddlFeatures.addColumnSuffixString);
						columnExisted.add(tableAndValColumn);
					}
				}
			}
	}

	private static void buildFKeyConstraintDDL(Dialect dialect, List<String> stringList,
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

		outputFKeyConstraintDDL(dialect, stringList, trueList);
	}

	private static void outputFKeyConstraintDDL(Dialect dialect, List<String> stringList,
			List<FKeyConstraint> trueList) {
		if (DDLFeatures.NOT_SUPPORT.equals(dialect.ddlFeatures.addForeignKeyConstraintString)) {
			logger.warn("Dialect \"" + dialect + "\" does not support foreign key setting, settings be ignored");
			return;
		}
		for (FKeyConstraint t : trueList) {
			/*
			 * ADD CONSTRAINT _FKEYNAME FOREIGN KEY _FKEYNAME (_FK1, _FK2)
			 * REFERENCES _REFTABLE (_REF1, _REF2)
			 */
			String s = dialect.ddlFeatures.addForeignKeyConstraintString;
			s = StrUtils.replace(s, "_FK1, _FK2", StrUtils.listToString(t.getColumnNames()));
			s = StrUtils.replace(s, "_REF1, _REF2", StrUtils.arrayToString(t.getRefColumnNames()));
			s = StrUtils.replace(s, "_REFTABLE", t.getRefTableName());
			s = StrUtils.replace(s, "_FKEYNAME", "fk_" + t.getTableName().toLowerCase() + "_"
					+ StrUtils.replace(StrUtils.listToString(t.getColumnNames()), ",", "_"));
			stringList.add("alter table " + t.getTableName() + " " + s);
		}
	}

	private static void addUniqueConstraintDDL(List<Object> objectList, Dialect dialect, String tableName,
			Column column) {
		if (!column.getUnique())
			return;
		String UniqueConstraintName = column.getUniqueConstraintName();
		if (StrUtils.isEmpty(UniqueConstraintName))
			UniqueConstraintName = "unique_" + tableName.toLowerCase() + "_" + column.getColumnName().toLowerCase();
		StringBuilder sb = new StringBuilder("alter table ").append(tableName);

		if (dialect.isInfomixFamily()) {
			sb.append(" add constraint unique (").append(column.getColumnName()).append(") constraint ")
					.append(UniqueConstraintName).toString();
			objectList.add(sb.toString());
			return;
		}

		if (dialect.isDerbyFamily() || dialect.isDB2Family()) {
			if (!column.getNotNull()) {
				objectList.add(new StringBuilder("create unique index ").append(UniqueConstraintName).append(" on ")
						.append(tableName).append("(").append(column.getColumnName()).append(")").toString());
				return;
			}
		}
		sb.append(" add constraint ").append(UniqueConstraintName).append(" unique (").append(column.getColumnName())
				.append(")").toString();
		objectList.add(sb.toString());
	}

	private static void buildIndexDLL(Dialect dialect, List<Object> objectResultList, String tableName,
			Map<String, Column> columns) {
		Map<String, String> indexes = new LinkedHashMap<String, String>();
		for (Column c : columns.values()) {
			if (c.getIndex() && !c.getUnique()) {
				String[] indexNames = c.getIndexNames();
				if (indexNames == null || indexNames.length == 0)
					indexNames = new String[] { "IDX_" + tableName + "_" + c.getColumnName() };
				for (String indexName : indexNames) {
					String indexedColumns = indexes.get(indexName);
					if (StrUtils.isEmpty(indexedColumns))
						indexes.put(indexName, c.getColumnName());
					else {
						indexedColumns += "," + c.getColumnName();
						indexes.put(indexName, indexedColumns);
					}
				}

			}
		}
		for (Entry<String, String> idx : indexes.entrySet()) {
			if (Dialect.Teradata14Dialect.equals(dialect))
				objectResultList.add("create index " + idx.getKey() + " (" + idx.getValue() + ") on " + tableName);
			else
				objectResultList.add("create index " + idx.getKey() + " on " + tableName + " (" + idx.getValue() + ")");
		}
	}

}
