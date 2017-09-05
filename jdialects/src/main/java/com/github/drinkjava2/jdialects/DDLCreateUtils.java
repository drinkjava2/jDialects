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
import java.util.Map;
import java.util.Set;

import com.github.drinkjava2.jdialects.model.AutoIdGen;
import com.github.drinkjava2.jdialects.model.FKeyConst;
import com.github.drinkjava2.jdialects.model.IndexConst;
import com.github.drinkjava2.jdialects.model.SequenceGen;
import com.github.drinkjava2.jdialects.model.TableGen;
import com.github.drinkjava2.jdialects.model.UniqueConst;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.utils.StrUtils;

/**
 * To transfer platform-independent model to create DDL String array
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class DDLCreateUtils {
	private static DialectLogger logger = DialectLogger.getLog(DDLCreateUtils.class);

	/**
	 * Transfer tables to DDL by given dialect and without format it, if want get a
	 * formatted DDL, use DDLFormatter.format(DDLs) method to format it
	 */
	public static String[] toCreateDDL(Dialect dialect, TableModel... tables) {
		// Store mixed DDL String, TableGen Object, SequenceGen Object ...
		List<Object> objectResultList = new ArrayList<Object>();

		for (TableModel table : tables)
			transferTableToObjectList(dialect, table, objectResultList);

		List<String> stringResultList = new ArrayList<String>();
		List<TableGen> tbGeneratorList = new ArrayList<TableGen>();
		List<SequenceGen> sequenceList = new ArrayList<SequenceGen>();
		List<AutoIdGen> autoIdGeneratorList = new ArrayList<AutoIdGen>();
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
					autoIdGeneratorList.add((AutoIdGen) strOrObj);
				else if (strOrObj instanceof FKeyConst)
					fKeyConstraintList.add((FKeyConst) strOrObj);
			}
		}

		buildSequenceDDL(dialect, stringResultList, sequenceList);
		buildTableGeneratorDDL(dialect, stringResultList, tbGeneratorList);
		buildAutoIdGeneratorDDL(dialect, stringResultList, autoIdGeneratorList);
		outputFKeyConstraintDDL(dialect, stringResultList, fKeyConstraintList);

		return stringResultList.toArray(new String[stringResultList.size()]);
	}

	/**
	 * Transfer table to a mixed DDL String or TableGen Object list
	 */
	/**
	 * @param dialect
	 * @param t
	 * @param objectResultList
	 */
	private static void transferTableToObjectList(Dialect dialect, TableModel t, List<Object> objectResultList) {
		DDLFeatures features = dialect.ddlFeatures;

		StringBuilder buf = new StringBuilder();
		boolean hasPkey = false;
		String pkeys = "";
		String tableName = t.getTableName();
		Map<String, ColumnModel> columns = t.getColumns();

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

		for (ColumnModel col : columns.values())
			dialect.checkNotEmptyReservedWords(col.getColumnName(), "Column name can not be empty");

		for (ColumnModel col : columns.values()) {
			// "Auto" type generator
			if (col.getAutoGenerator()) {// if support sequence
				if (features.supportBasicOrPooledSequence()) {
					objectResultList.add(
							new SequenceGen(AutoIdGen.JDIALECTS_AUTOID, AutoIdGen.JDIALECTS_AUTOID, 1, 1));
				} else {// AutoIdGen
					objectResultList.add(new AutoIdGen());
				}
			}

		}

		// sequence
		for (SequenceGen seq : t.getSequences().values())
			objectResultList.add(seq);

		// tableGenerator
		for (TableGen tableGenerator : t.getTableGenerators().values())
			objectResultList.add(tableGenerator);

		// Foreign key
		for (FKeyConst fkey : t.getFkeyConstraints())
			objectResultList.add(fkey);

		// check and cache prime keys
		for (ColumnModel col : columns.values()) {
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
				.append(" ").append(tableName).append(" ( ");

		for (ColumnModel c : columns.values()) {
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
				if (!c.getNullable())
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

		// table comment on
		if (t.getComment() != null) {
			if (features.supportsCommentOn)
				objectResultList.add("comment on table " + t.getTableName() + " is '" + t.getComment() + "'");
			else
				logger.warn("Ignore unsupported table comment setting for dialect \"" + dialect + "\" on table \""
						+ tableName + "\" with value: " + t.getComment());
		}

		// column comment on
		for (ColumnModel c : columns.values()) {
			if (features.supportsCommentOn && c.getComment() != null && StrUtils.isEmpty(features.columnComment))
				objectResultList.add(
						"comment on column " + tableName + '.' + c.getColumnName() + " is '" + c.getComment() + "'");
		}

		// index
		buildIndexDLL(dialect, objectResultList, t);

		// unique
		buildUniqueDLL(dialect, objectResultList, t);
	}

	private static void buildSequenceDDL(Dialect dialect, List<String> stringList, List<SequenceGen> sequenceList) {
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
			List<AutoIdGen> autoIdGenerator) {
		if (autoIdGenerator != null && !autoIdGenerator.isEmpty()) {
			stringList.add(dialect.ddlFeatures.createTableString + " " + AutoIdGen.JDIALECTS_AUTOID + " ("
					+ AutoIdGen.NEXT_VAL + " " + dialect.translateToDDLType(Type.BIGINT) + " )");
			stringList.add("insert into " + AutoIdGen.JDIALECTS_AUTOID + " values ( 1 )");
		}
	}

	private static void buildTableGeneratorDDL(Dialect dialect, List<String> stringList,
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

		for (TableGen tg : tbGeneratorList) {
			for (TableGen tg2 : tbGeneratorList) {
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
		for (TableGen tg : tbGeneratorList)
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

	private static void outputFKeyConstraintDDL(Dialect dialect, List<String> stringList, List<FKeyConst> trueList) {
		if (DDLFeatures.NOT_SUPPORT.equals(dialect.ddlFeatures.addForeignKeyConstraintString)) {
			logger.warn("Dialect \"" + dialect + "\" does not support foreign key setting, settings be ignored");
			return;
		}
		for (FKeyConst t : trueList) {
			/*
			 * ADD CONSTRAINT _FKEYNAME FOREIGN KEY _FKEYNAME (_FK1, _FK2) REFERENCES
			 * _REFTABLE (_REF1, _REF2)
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

	private static void buildIndexDLL(Dialect dialect, List<Object> objectResultList, TableModel t) {
		List<IndexConst> l = t.getIndexConsts();
		if (l == null || l.isEmpty())
			return;
		String template;
		if (Dialect.Teradata14Dialect.equals(dialect))
			template = "create $ifUnique index $indexName ($indexValues) on " + t.getTableName();
		else
			template = "create $ifUnique index $indexName on " + t.getTableName() + " ($indexValues)";
		for (IndexConst index : l) {
			String indexname = index.getName();
			if (StrUtils.isEmpty(indexname))
				indexname = "IX_" + t.getTableName() + "_" + StrUtils.arrayToString(index.getColumnList(), "_");
			String ifUnique = index.getUnique() ? "unique" : "";
			String result = StrUtils.replace(template, "$ifUnique", ifUnique);
			result = StrUtils.replace(result, "$indexName", indexname);
			result = StrUtils.replace(result, "$indexValues", StrUtils.arrayToString(index.getColumnList()));
			objectResultList.add(result);
		}
	}

	private static void buildUniqueDLL(Dialect dialect, List<Object> objectResultList, TableModel t) {
		List<UniqueConst> l = t.getUniqueConsts();
		if (l == null || l.isEmpty())
			return;
		String dialectName = "" + dialect;
		for (UniqueConst unique : l) {
			boolean nullable = false;
			String[] columns = unique.getColumnList();
			for (String colNames : columns) {
				ColumnModel vc = t.getColumn(colNames.toLowerCase());
				if (vc != null && vc.getNullable())
					nullable = true;
			}
			String uniqueName = unique.getName();
			if (StrUtils.isEmpty(uniqueName))
				uniqueName = "UK_" + t.getTableName() + "_" + StrUtils.arrayToString(unique.getColumnList(), "_");

			String template = "alter table $TABLE add constraint $UKNAME unique ($COLUMNS)";
			if ((StrUtils.startsWithIgnoreCase(dialectName, "DB2")// DB2 and DERBY
					|| StrUtils.startsWithIgnoreCase(dialectName, "DERBY")) && nullable)
				template = "create unique index $UKNAME on $TABLE ($COLUMNS)";
			else if (StrUtils.startsWithIgnoreCase(dialectName, "Informix"))
				template = "alter table $TABLE add constraint unique ($COLUMNS) constraint $UKNAME";
			String result = StrUtils.replace(template, "$TABLE", t.getTableName());
			result = StrUtils.replace(result, "$UKNAME", uniqueName);
			result = StrUtils.replace(result, "$COLUMNS", StrUtils.arrayToString(unique.getColumnList()));
			objectResultList.add(result);
		}
	}

}
