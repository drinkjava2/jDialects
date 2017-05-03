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

	public static String createTable(Dialect d, String tableName) {
		return d.ddlFeatures.createTableString + " " + tableName + " ";
	}

	protected static DialectColumn column(Dialect dialect, String columnName, String columnType) {
		return new DialectColumn(columnName, columnType).setDialect(dialect);
	}

	protected static DialectColumn addColumn(Dialect dialect, String tableName, String columnName, String columnType) {
		return new DialectColumn(columnName, columnType).setDialect(dialect).setTableName(tableName)
				.setOperation("ADD");
	}

	protected static void dropColumn(Dialect dialect, String tableName, String columnName) {
		return;
	}

	/**
	 * Create a constraint DDL fragment inside of a
	 * "create table someTable (xxxx)" DDL
	 */
	protected static DialectConstraint constraint(Dialect d, String constraintName, String constraintType) {
		return new DialectConstraint(constraintName);
	}

	/**
	 * Create a single "alter table add constraint xxx" DDL
	 */
	protected static DialectConstraint addConstraint(Dialect d, String tableName, String constraintName,
			String constraintType) {
		return new DialectConstraint();
	}

	/**
	 * Create a "alter table drop constraint xxx" DDL
	 */
	protected static void dropConstraint(Dialect d, String tableName, String constraintName) {
		return;
	}
}
