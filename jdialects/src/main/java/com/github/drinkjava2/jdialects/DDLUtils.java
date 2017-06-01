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
import com.github.drinkjava2.jdialects.model.Table;

/**
 * public static methods platform-independent model to DDL String
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class DDLUtils {
	public static DialectLogger logger = DialectLogger.getLog(DDLUtils.class);

	/**
	 * Transfer table to ddl by given dialect and format it if formatOutputDDL
	 * set to true
	 */
	public static String[] toCreateDDL(Dialect dialect, Table t, boolean formatOutputDDL) {
		if (formatOutputDDL) {
			String[] ddls = toCreateDDL(dialect, t);
			for (int i = 0; i < ddls.length; i++) {
				ddls[i] = DDLFormatter.format(ddls[i]);
			}
			return ddls;
		} else
			return toCreateDDL(dialect, t);
	}

	/**
	 * Transfer table to ddl by given dialect and do not format it
	 */
	public static String[] toCreateDDL(Dialect dialect, Table t) {
		DDLFeatures features = dialect.ddlFeatures;

		StringBuilder buf = new StringBuilder();
		boolean hasPkey = false;
		String pkeys = "";
		String tableName = t.getTableName();
		Map<String, Column> columns = t.getColumns();

		// Reserved words check
		dialect.check(tableName);
		for (Column col : columns.values()) {
			dialect.check(col.getColumnName());
			dialect.check(col.getPkeyName());
			dialect.check(col.getUniqueConstraintName());
			dialect.check(col.getSequenceName());
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

		List<String> resultList = new ArrayList<>();

		// create sequences if sequenceName not empty
		for (Column col : columns.values())
			if (!StrUtils.isEmpty(col.getSequenceName())) {

				if (col.getIdentity())
					DialectException.throwEX("Can not set sequence and identity at same time on column \""
							+ col.getColumnName() + "\" in table \"" + tableName + "\"");

				if (!(features.supportsPooledSequences || features.supportsSequences)) {
					if (col.getIdentityOrSequence()) {
						if (!features.supportsIdentityColumns)
							DialectException.throwEX("Dialect \"" + dialect
									+ "\" does not support sequence or identity setting, on column \""
									+ col.getColumnName() + "\" in table \"" + tableName + "\"");
					} else
						DialectException
								.throwEX("Dialect \"" + dialect + "\" does not support sequence setting, on column \""
										+ col.getColumnName() + "\" in table \"" + tableName + "\"");
				}

				if (!(col.getIdentityOrSequence() && features.supportsIdentityColumns)) {
					if (features.supportsPooledSequences) {
						// create sequence _SEQ start with 11 increment by 33
						String pooledSequence = StrUtils.replace(features.createPooledSequenceStrings, "_SEQ",
								col.getSequenceName());
						pooledSequence = StrUtils.replace(pooledSequence, "11", "" + col.getSequenceStart());
						pooledSequence = StrUtils.replace(pooledSequence, "33", "" + col.getSequenceIncrement());
						resultList.add(pooledSequence);
					} else {
						if (col.getSequenceStart() >= 2 || col.getSequenceIncrement() >= 2)
							DialectException.throwEX("Unsupported sequence start and increment setting of dialect \""
									+ dialect + "\" on column \"" + col.getColumnName() + "\" in table \"" + tableName
									+ "\", this dialect only support basic sequence setting.");
						// "create sequence _SEQ"
						String pooledSequence = StrUtils.replace(features.createSequenceStrings, "_SEQ",
								col.getSequenceName());
						resultList.add(pooledSequence);
					}

				}
			}

		// create table
		buf.append(hasPkey ? dialect.ddlFeatures.createTableString : dialect.ddlFeatures.createMultisetTableString)
				.append(" ").append(tableName).append(" (");

		for (

		Column c : columns.values()) {
			// column definition
			buf.append(c.getColumnName()).append(" ");

			// Identity
			if (c.getIdentity() && !features.supportsIdentityColumns)
				DialectException.throwEX("Unsupported identity setting for dialect \"" + dialect + "\" on column \""
						+ c.getColumnName() + "\" in table \"" + tableName + "\"");

			if (c.getIdentity() || (c.getIdentityOrSequence() && features.supportsIdentityColumns)) {
				if (features.hasDataTypeInIdentityColumn)
					buf.append(dialect.translateToDDLType(c.getColumnType(), c.getLengths()));
				buf.append(' ');
				if (Type.BIGINT.equals(c.getColumnType()))
					buf.append(features.identityColumnStringBigINT);
				else
					buf.append(features.identityColumnString);

			} else if (c.getIdentity() && c.getIdentityOrSequence()) {

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
							+ c.getColumnName() + "\" in table \"" + tableName + "\" with value: " + c.getCheck());
			}

			// Comments
			if (c.getComment() != null) {
				if (StrUtils.isEmpty(features.columnComment) && !features.supportsCommentOn)
					logger.warn("Ignore unsupported comment setting for dialect \"" + dialect + "\" on column \""
							+ c.getColumnName() + "\" in table \"" + tableName + "\" with value: " + c.getComment());
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
		String[] resultArray = new String[resultList.size()];
		for (int i = 0; i < resultArray.length; i++)
			resultArray[i] = resultList.get(i) + ";";
		return resultArray;
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
