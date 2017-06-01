/**
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

import java.util.LinkedHashMap;
import java.util.Map;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.DDLUtils;

/**
 * The platform-independent table model
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class Table extends Database {

	/** The table name. */
	private String tableName;

	/** check constraint for table */
	private String check;

	/** comment for table */
	private String comment;

	/** Columns in this table, key is lower case of column name */
	private Map<String, Column> columns = new LinkedHashMap<>();

	public Table() {
	}

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
		return DDLUtils.toCreateDDL(dialect, this, formatOutputDDL);
	}

	public String[] toCreateDDL(Dialect dialect) {
		return DDLUtils.toCreateDDL(dialect, this);
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
