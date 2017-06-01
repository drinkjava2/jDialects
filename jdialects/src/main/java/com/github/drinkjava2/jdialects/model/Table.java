/**
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.hibernate.DDLFormatter;

/**
 * The platform-independent table model
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class Table {
	public static DialectLogger logger = DialectLogger.getLog(Table.class);

	/** The table name. */
	private String tableName;

	/** check constraint for table */
	private String check;

	/** comment for table */
	private String comment;

	/** Columns in this table, key is lower case of column name */
	private Map<String, Column> columns = new LinkedHashMap<>();

	public Table(String tableName) {
		this.tableName = tableName;
	}

	public Table check(String check) {
		this.check = check;
		return this;
	}

	public Table comment(String comment) {
		this.comment = comment;
		return this;
	}

	public Table addColumn(Column column) {
		DialectException.assureNotNull(column);
		DialectException.assureNotEmpty(column.getColumnName(), "Column name can not be empty");
		columns.put(column.getColumnName().toLowerCase(), column);
		return this;
	}

	public Column column(String columnName) {
		Column column = new Column(columnName);
		addColumn(column);
		return column;
	}

	public Column getColumn(String columnName) {
		DialectException.assureNotEmpty(columnName);
		return columns.get(columnName.toUpperCase());
	}

	public String[] toCreateDDL(Dialect dialect, boolean formatOutputDDL) {
		if (formatOutputDDL) {
			String[] ddls = toCreateDDL(dialect);
			for (int i = 0; i < ddls.length; i++) {
				ddls[i] = DDLFormatter.format(ddls[i]);
			}
			return ddls;
		}
		return toCreateDDL(dialect);
	}

	public String[] toCreateDDL(Dialect dialect) {
		DDLFeatures features = dialect.ddlFeatures;

		StringBuilder buf = new StringBuilder();
		boolean hasPkey = false;
		String pkeys = "";

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
		if (!StrUtils.isEmpty(check)) {
			if (features.supportsTableCheck)
				buf.append(" check (").append(check).append("),");
			else
				logger.warn("Ignore unsupported table check setting for dialect \"" + dialect + "\" on table \""
						+ tableName + "\" with value: " + check);
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
		if (this.getComment() != null) {
			if (features.supportsCommentOn)
				resultList.add("comment on table " + this.getTableName() + " is '" + this.getComment() + "'");
			else
				logger.warn("Ignore unsupported table comment setting for dialect \"" + dialect + "\" on table \""
						+ tableName + "\" with value: " + this.getComment());
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

	// getter & setter=========================
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getCheck() {
		return check;
	}

	public void setCheck(String check) {
		this.check = check;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Map<String, Column> getColumns() {
		return columns;
	}

	public void setColumns(Map<String, Column> columns) {
		this.columns = columns;
	}
}
