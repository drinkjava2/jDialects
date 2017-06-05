/**
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

/**
 * The platform-independent Foreign Key Constraint model
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
public class FKeyConstraint {
	private String tableName;
	private String columnName;
	private String fkeyReferenceTable;
	private String[] fkeyReferenceColumns;

	public String getFkeyReferenceTable() {
		return fkeyReferenceTable;
	}

	public FKeyConstraint() {// default constructor
	}

	public FKeyConstraint(String tableName, String columnName, String fkeyReferenceTable,
			String... fkeyReferenceColumns) {
		this.tableName = tableName;
		this.columnName = columnName;
		this.fkeyReferenceTable = fkeyReferenceTable;
		this.fkeyReferenceColumns = fkeyReferenceColumns;
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

	public String[] getFkeyReferenceColumns() {
		return fkeyReferenceColumns;
	}

	public void setFkeyReferenceColumns(String[] fkeyReferenceColumns) {
		this.fkeyReferenceColumns = fkeyReferenceColumns;
	}

	public void setFkeyReferenceTable(String fkeyReferenceTable) {
		this.fkeyReferenceTable = fkeyReferenceTable;
	}

}
