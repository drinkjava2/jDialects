/**
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

/**
 * The platform-independent Index model
 * 
 * <pre>
 * Usage:
 * Table t=new Table('Customer');
 * ...  
 *  t.index().forColumn("column1");
 *  or
 *  t.index("indexName").forColumn("column1","column2");
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.5
 */
public class IndexConst {
	/** (Optional) The names of the index */
	private String name;

	/** The names of the columns to be included in the index */
	private String[] columnList;

	/** The names of the table which index belong to */
	private String tableName;

	/** Whether the index is unique. */
	private Boolean unique = false;

	public IndexConst() {

	}

	public IndexConst(String name) {
		this.name = name;
	}

	public IndexConst columns(String... columns) {
		this.columnList = columns;
		return this;
	}
	
	public IndexConst unique( ) {
		this.unique = true;
		return this;
	}

	// getter & setter =========
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getColumnList() {
		return columnList;
	}

	public void setColumnList(String[] columnList) {
		this.columnList = columnList;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Boolean getUnique() {
		return unique;
	}

	public void setUnique(Boolean unique) {
		this.unique = unique;
	}

}
