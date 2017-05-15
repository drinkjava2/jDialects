/**
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The platform-independent table model
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class Table {

	/** The table name. */
	private String tableName;

	/** The columns in this table, key is upper case of column name */
	private Map<String, Column> columns = new LinkedHashMap<>();

	public Table(String tableName) {
		this.tableName = tableName;
	}

	public Column addColumn(String columnName) {
		DialectException.assureNotEmpty(columnName);
		Column column = new Column(columnName);
		columns.put(columnName.toUpperCase(), column);
		return column;
	}

	public Column getColumn(String columnName) {
		DialectException.assureNotEmpty(columnName);
		return columns.get(columnName.toUpperCase());
	}

	public String toCreateTableSQLs(Dialect d) {
		StringBuilder sb = new StringBuilder();
		sb.append(d.ddlFeatures.createTableString).append(" ").append(tableName).append(" (");
		String pkeys = "";
		for (Column c : columns.values()) {
			// pkey
			if (c.getPkey()) {
				c.setNotNull(true);// pkey column should not null
				if (StrUtils.isEmpty(pkeys))
					pkeys = c.getColumnName();
				else
					pkeys += "," + c.getColumnName();
			}

			// column defination
			sb.append(c.getColumnName()).append(" ").append(d.translateToDDLType(c.getColumnType(), c.getLengths()));
			if(c.getNotNull())sb.append("");
			sb.append(",");

		}
		// pkey
		if (!StrUtils.isEmpty(pkeys)) {
			sb.append(" primary key (").append(pkeys).append("),");
		}
		sb.setLength(sb.length() - 1);
		sb.append(")"); 
		// engine, for MySql
		sb.append(d.engine());
		return sb.toString();
	}

}
