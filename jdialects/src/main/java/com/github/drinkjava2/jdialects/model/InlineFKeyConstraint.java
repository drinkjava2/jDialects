/**
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

/**
 * An "in-line" style Foreign key constraint, some time it's more convenient
 * than use t.fkey() method
 * 
 * <pre>
 * Usage:
 * For simple Foreign Key:
 *     column("column1").fkey("refTable","refColumn1");
 * 
 * For Compound Foreign Key:
 *     column("column1").fkey("refTable","refColumn1", "refColumn2"...);
 *     column("column2").fkey("refTable","refColumn1", "refColumn2"...);
 *     
 * Note: the order is important
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class InlineFKeyConstraint {
	private String tableName;
	private String columnName;
	private String refTableName;
	private String[] refColumnNames;

	public String getFkeyReferenceTable() {
		return refTableName;
	}

	public InlineFKeyConstraint() {// default constructor
	}

	public InlineFKeyConstraint(String tableName, String columnName, String refTableName, String... refColumnNames) {
		this.tableName = tableName;
		this.columnName = columnName;
		this.refTableName = refTableName;
		this.refColumnNames = refColumnNames;
	}
	// getter & setter=================

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getRefTableName() {
		return refTableName;
	}

	public void setRefTableName(String refTableName) {
		this.refTableName = refTableName;
	}

	public String[] getRefColumnNames() {
		return refColumnNames;
	}

	public void setRefColumnNames(String[] refColumnNames) {
		this.refColumnNames = refColumnNames;
	}

}
