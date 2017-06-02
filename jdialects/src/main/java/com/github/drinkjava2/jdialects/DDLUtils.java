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

import com.github.drinkjava2.hibernate.DDLFormatter;
import com.github.drinkjava2.jdialects.model.Column;
import com.github.drinkjava2.jdialects.model.Sequence;
import com.github.drinkjava2.jdialects.model.Table;
import com.github.drinkjava2.jdialects.model.TableGenerator;

/**
 * public static methods platform-independent model to DDL String
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class DDLUtils {
	public static DialectLogger logger = DialectLogger.getLog(DDLUtils.class);

	public static String[] toCreateDDLWithoutFormat(Dialect dialect, Sequence seq) {
		DDLFeatures features = dialect.ddlFeatures;
		List<String> resultList = new ArrayList<>();

		if (features.supportsPooledSequences) {
			// create sequence _SEQ start with 11 increment by 33
			String pooledSequence = StrUtils.replace(features.createPooledSequenceStrings, "_SEQ",
					seq.getSequenceName());
			// 11 and 33 is fixed template value
			pooledSequence = StrUtils.replace(pooledSequence, "11", "" + seq.getInitialValue());
			pooledSequence = StrUtils.replace(pooledSequence, "33", "" + seq.getAllocationSize());
			resultList.add(pooledSequence);
		} else {
			if (seq.getInitialValue() >= 2 || seq.getAllocationSize() >= 2)
				DialectException.throwEX("Unsupported sequence start and increment setting of dialect \"" + dialect
						+ "\" on seq \"" + seq.getSequenceName() + "\"");
			// "create sequence _SEQ"
			String pooledSequence = StrUtils.replace(features.createSequenceStrings, "_SEQ", seq.getSequenceName());
			resultList.add(pooledSequence);
		}
		String[] resultArray = new String[resultList.size()];
		for (int i = 0; i < resultArray.length; i++)
			resultArray[i] = resultList.get(i) + ";";
		return resultArray;
	}

	/**
	 * Transfer table to formatted DDL according given dialect
	 */
	public static String[] toCreateDDL(Dialect dialect, Table... tables) {
		String[] ddls = toCreateDDLwithoutFormat(dialect, tables);
		for (int i = 0; i < ddls.length; i++) {
			ddls[i] = DDLFormatter.format(ddls[i]) + ";";
		}
		return ddls;
	}

	/**
	 * Transfer table to DDL by given dialect and without format it
	 */
	public static String[] toCreateDDLwithoutFormat(Dialect dialect, Table... tables) {
		List<Object> resultList = new ArrayList<>();
		for (Table table : tables) {
			resultList = transferTableToList(dialect, table, resultList);
			// now we get a mixed DDL and TableGenerator list
		}
		List<String> stringList = new ArrayList<>();

		List<TableGenerator> tbGeneratorList = new ArrayList<>();
		for (Object ddl : resultList) {
			/*
			 * if is String, put into String list, if is is TableGenerator, put
			 * into tbGeneratorList, because the TableGenertors can be joined if
			 * they are share same table, so do this join job at the end
			 */
			if (!StrUtils.isEmpty(ddl)) {
				if (ddl instanceof String)
					stringList.add((String) ddl);
				else if (ddl instanceof TableGenerator)
					tbGeneratorList.add((TableGenerator) ddl);
			}
		}

		buildTableGeneratorDDL(dialect, stringList, tbGeneratorList);
		return stringList.toArray(new String[stringList.size()]);
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
				if (tg != tg2) {
					if (tg.getName().equalsIgnoreCase(tg2.getName()))
						DialectException.throwEX("Dulplicated tableGenerator name \"" + tg.getName() + "\" found.");
					if (tg.getTableName().equalsIgnoreCase(tg2.getTableName())
							&& tg.getPkColumnName().equalsIgnoreCase(tg2.getPkColumnName())
							&& tg.getPkColumnValue().equalsIgnoreCase(tg2.getPkColumnValue())
							&& tg.getValueColumnName().equalsIgnoreCase(tg2.getValueColumnName())) {
						DialectException.throwEX("Dulplicated tableGenerator setting \"" + tg.getName() + "\" and \""
								+ tg2.getName() + "\" found.");
					}
				}
			}
		}

		for (TableGenerator tg : tbGeneratorList) {
			for (TableGenerator tg2 : tbGeneratorList) {
				if (tg != tg2) {
					if (tg.getName().equalsIgnoreCase(tg2.getName()))
						DialectException.throwEX("Dulplicated tableGenerator name \"" + tg.getName() + "\" found.");
					if (tg.getTableName().equalsIgnoreCase(tg2.getTableName())
							&& tg.getPkColumnName().equalsIgnoreCase(tg2.getPkColumnName())
							&& tg.getPkColumnValue().equalsIgnoreCase(tg2.getPkColumnValue())
							&& tg.getValueColumnName().equalsIgnoreCase(tg2.getValueColumnName())) {
						DialectException.throwEX("Dulplicated tableGenerator setting \"" + tg.getName() + "\" and \""
								+ tg2.getName() + "\" found.");
					}
				}
			}
		}
		Set<String> tableExisted = new HashSet<>();
		Set<String> columnExisted = new HashSet<>();
		for (TableGenerator tg : tbGeneratorList) {
			String tableName = tg.getTableName().toLowerCase();
			String tableAndPKColumn = tg.getTableName().toLowerCase() + "..blabla.." + tg.getPkColumnName();
			String tableAndValColumn = tg.getTableName().toLowerCase() + "..blabla.." + tg.getValueColumnName();
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
							+ tg.getPkColumnName() + dialect.ddlFeatures.addColumnSuffixString);
					columnExisted.add(tableAndPKColumn);
				}
				if (!columnExisted.contains(tableAndValColumn)) {
					stringList.add("alter table " + tableName + " " + dialect.ddlFeatures.addColumnString + " "
							+ tg.getValueColumnName() + dialect.ddlFeatures.addColumnSuffixString);
					columnExisted.add(tableAndValColumn);
				}
			}
		}
	}
	
	private static void buildSequence(){}//TODO

	/**
	 * Transfer table to a mixed DDL String or TableGenerator Object list
	 */
	private static List<Object> transferTableToList(Dialect dialect, Table t, List<Object> resultList) {
		DDLFeatures features = dialect.ddlFeatures;

		StringBuilder buf = new StringBuilder();
		boolean hasPkey = false;
		String pkeys = "";
		String tableName = t.getTableName();
		Map<String, Column> columns = t.getColumns();

		// Reserved words check
		dialect.checkReservedWords(tableName);
		for (Column col : columns.values()) {
			dialect.checkReservedWords(col.getColumnName());
			dialect.checkReservedWords(col.getPkeyName());
			dialect.checkReservedWords(col.getUniqueConstraintName());
		}

		for (Column col : columns.values()) {
			if (col != null && col.getAutoGenerator()) {
				if (features.supportsIdentityColumns) { 
				} else if (features.supportsSequences || features.supportsPooledSequences) {
					if (!t.getSequences().containsKey(tableName.toLowerCase() + "_autosequence")) {
						Sequence autoSeq = new Sequence(tableName.toLowerCase() + "_autosequence",
								tableName.toLowerCase() + "_autosequence", 1, 1);
						t.addSequence(autoSeq);
					}
				} else {
					if (!t.getTableGenerators().containsKey(tableName.toLowerCase() + "_autotablegen")) {
						TableGenerator tableGen = new TableGenerator(tableName.toLowerCase() + "_autotablegen",
								tableName.toLowerCase() + "_autotablegen", "pkcol", "valCol", "pkval", 1, 1);
						t.addTableGenerator(tableGen);
					}
				}
				System.out.println("identity2=" + col.getIdentity());
			}
		}

		// sequence
		for (Sequence seq : t.getSequences().values()) {
			dialect.checkReservedWords(seq.getSequenceName());
			if (!(features.supportsPooledSequences || features.supportsSequences)) {
				DialectException.throwEX("Dialect \"" + dialect + "\" does not support sequence setting on sequence \""
						+ seq.getName() + "\" at table \"" + tableName + "\"");
			}
			if (features.supportsPooledSequences) {
				// create sequence _SEQ start with 11 increment by 33
				String pooledSequence = StrUtils.replace(features.createPooledSequenceStrings, "_SEQ",
						seq.getSequenceName());
				pooledSequence = StrUtils.replace(pooledSequence, "11", "" + seq.getInitialValue());
				pooledSequence = StrUtils.replace(pooledSequence, "33", "" + seq.getAllocationSize());
				resultList.add(pooledSequence);
			} else {
				if (seq.getInitialValue() >= 2 || seq.getAllocationSize() >= 2)
					DialectException.throwEX("Dialect \"" + dialect
							+ "\" does not support initialValue and allocationSize setting on sequence \""
							+ seq.getName() + "\" at table \"" + tableName
							+ "\", try set initialValue and allocationSize to 1 to fix");
				// "create sequence _SEQ"
				String simepleSeq = StrUtils.replace(features.createSequenceStrings, "_SEQ", seq.getSequenceName());
				resultList.add(simepleSeq);
			}
		}

		// tableGenerator
		for (TableGenerator tg : t.getTableGenerators().values())
			resultList.add(tg);

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
			// column definition
			buf.append(c.getColumnName()).append(" ");

			// Identity or autoGenerator+supportIdentity
			if (c.getIdentity() && !features.supportsIdentityColumns)
				DialectException.throwEX("Unsupported identity setting for dialect \"" + dialect + "\" on column \""
						+ c.getColumnName() + "\" at table \"" + tableName + "\"");

			if (c.getIdentity() || (c.getAutoGenerator() && features.supportsIdentityColumns)) {
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

		// type or engine for MariaDB & MySql
		buf.append(dialect.engine());

		resultList.add(buf.toString());

		// unique constraint
		for (Column column : columns.values()) {
			String uniqueDDL = buildUniqueConstraint(dialect, tableName, column);
			if (!StrUtils.isEmpty(uniqueDDL))
				resultList.add(uniqueDDL);
		}

		// table comment on
		if (t.getComment() != null) {
			if (features.supportsCommentOn)
				resultList.add("comment on table " + t.getTableName() + " is '" + t.getComment() + "'");
			else
				logger.warn("Ignore unsupported table comment setting for dialect \"" + dialect + "\" on table \""
						+ tableName + "\" with value: " + t.getComment());
		}

		// column comment on
		for (Column c : columns.values()) {
			if (features.supportsCommentOn && c.getComment() != null && StrUtils.isEmpty(features.columnComment))
				resultList.add(
						"comment on column " + tableName + '.' + c.getColumnName() + " is '" + c.getComment() + "'");
		}
		return resultList;
	}

	private static String buildUniqueConstraint(Dialect dialect, String tableName, Column column) {
		if (!column.getUnique())
			return null;
		String UniqueConstraintName = column.getUniqueConstraintName();
		if (StrUtils.isEmpty(UniqueConstraintName))
			UniqueConstraintName = "UK_" + RandomStrUtils.getRandomString(20);
		StringBuilder sb = new StringBuilder("alter table ").append(tableName);

		if (dialect.isInfomixFamily()) {
			return sb.append(" add constraint unique (").append(column.getColumnName()).append(") constraint ")
					.append(UniqueConstraintName).toString();
		}

		if (dialect.isDerbyFamily() || dialect.isDB2Family()) {
			if (!column.getNotNull()) {
				return new StringBuilder("create unique index ").append(UniqueConstraintName).append(" on ")
						.append(tableName).append("(").append(column.getColumnName()).append(")").toString();
			}
		}
		return sb.append(" add constraint ").append(UniqueConstraintName).append(" unique (")
				.append(column.getColumnName()).append(")").toString();
	}

}
