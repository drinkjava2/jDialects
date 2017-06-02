/**
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	 * Transfer table to ddl by given dialect and do not format it
	 */
	public static String[] toCreateDDLwithoutFormat(Dialect dialect, Table... tables) {
		List<String> resultList = new ArrayList<>();
		for (Table table : tables) {
			resultList = transferTableToDDL(dialect, table, resultList);
		}
		return resultList.toArray(new String[resultList.size()]);
	}

	/**
	 * Transfer table to DDL by given dialect and without format it
	 */
	private static List<String> transferTableToDDL(Dialect dialect, Table t, List<String> resultList) {
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
		for (TableGenerator tg : t.getTableGenerators().values()) {
			dialect.checkReservedWords(tg.getTableName());
			

		}

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

			// Identity
			if (c.getIdentity() && !features.supportsIdentityColumns)
				DialectException.throwEX("Unsupported identity setting for dialect \"" + dialect + "\" on column \""
						+ c.getColumnName() + "\" at table \"" + tableName + "\"");

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
		buf.append("");

		resultList.add(buf.toString());

		// unique constraint
		for (Column column : columns.values()) {
			String uniqueDDL = getAddUniqueConstraint(dialect, tableName, column);
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

	private static String getAddUniqueConstraint(Dialect dialect, String tableName, Column column) {
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
