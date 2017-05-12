/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

/**
 * DDL Utils
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class DDLUtils {
	private DDLUtils() {
		// hide constructor
	}

	protected static String createTable(Dialect dialect, String tableName) {
		return dialect.ddlFeatures.createTableString + " " + tableName + " ";
	}

	protected static String dropTable(Dialect dialect, String tableName) {
		return dialect.ddlFeatures.dropTableString.replaceFirst("_TABLENAME", tableName);
	}

	protected static DialectColumn column(Dialect dialect, String columnName, String columnType) {
		return new DialectColumn(columnName, columnType).setDialect(dialect);
	}

	protected static DialectColumn addColumn(Dialect dialect, String tableName, String columnName, String columnType) {
		return new DialectColumn(columnName, columnType).setDialect(dialect).setTableName(tableName)
				.setOperation("ADD");
	}

	protected static String dropColumn(Dialect dialect, String tableName, String columnName) {
		return new StringBuilder("alter table ").append(dialect.check(tableName)).append(" drop ").append(columnName)
				.toString();
	}

	protected static DialectConstraint constraint(Dialect dialect, String constraintName, String constraintType) {
		return new DialectConstraint(constraintName).setConstraintType(constraintType).setDialect(dialect);
	}

	protected static DialectConstraint addConstraint(Dialect dialect, String tableName, String constraintName,
			String constraintType) {
		return new DialectConstraint(constraintName).setConstraintType(constraintType).setDialect(dialect)
				.setTable(tableName);
	}

	/**
	 * Create a "alter table drop constraint xxx" DDL
	 */
	protected static String dropConstraint(Dialect dialect, String tableName, String constraintName) {
		String dropForeignKeyString=dialect.ddlFeatures.dropForeignKeyString;
		if(DDLFeatures.NOT_SUPPORT.equals(dropForeignKeyString))
			return (String) DialectException.throwEX("Dialect \""+dialect+"\" does not support drop constraint.");
		return new StringBuilder("alter table ").append(tableName) .append(dropForeignKeyString).append(constraintName)
				.toString(); 
	}
}
