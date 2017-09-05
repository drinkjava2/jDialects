/**
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The platform-independent Foreign Key Constraint model
 * 
 * <pre>
 * Usage:
 * Table t=new Table('Customer');
 *    ...
 *  t.fkey("column1").ref("refTable", "refColumn1")
 *  
 *  or compound foreign key: *  
 *  t.fkey("column1","column2").ref("refTable", "refColumn1", "refColumn2");
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class FKeyConst {
	private String fkeyName;
	private String tableName;
	private List<String> columnNames = new ArrayList<String>();
	private String refTableName;
	private String[] refColumnNames;

	public FKeyConst() {
		// default constructor
	}

	public FKeyConst(String fkeyName) {
		this.fkeyName = fkeyName;
	}

	public FKeyConst columns(String... columnNames) {
		this.columnNames = Arrays.asList(columnNames);
		return this;
	}

	public FKeyConst ref(String refTableName, String... refColumnNames) {
		this.refTableName = refTableName;
		this.refColumnNames = refColumnNames;
		return this;
	}

	// getter & setter=====
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
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

	public String getFkeyName() {
		return fkeyName;
	}

	public void setFkeyName(String fkeyName) {
		this.fkeyName = fkeyName;
	}

}
