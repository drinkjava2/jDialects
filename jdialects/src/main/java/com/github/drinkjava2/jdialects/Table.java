/**
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import java.util.LinkedHashMap;
import java.util.Map;

import com.github.drinkjava2.hibernate.DDLFormatter;

/**
 * The platform-independent table model
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class Table {

	/** The table name. */
	private String tableName;

	/** Columns in this table, key is upper case of column name */
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

	public String toCreateTableDDL(Dialect d, boolean formatDDL) {
		if (formatDDL)
			return DDLFormatter.format(toCreateTableDDL(d));
		return toCreateTableDDL(d);
	}

	public String toCreateTableDDL(Dialect d) {
		StringBuilder sb = new StringBuilder();
		boolean hasPkey = false;
		String pkeys = "";
		for (Column col : columns.values()) {
			// check if have PKEY
			if (col.getPkey()) {
				hasPkey = true;
				if (StrUtils.isEmpty(pkeys))
					pkeys = col.getColumnName();
				else
					pkeys += "," + col.getColumnName();
			}
		}
		// create table
		sb.append(hasPkey ? d.ddlFeatures.createTableString : d.ddlFeatures.createMultisetTableString).append(" ")
				.append(d.check(tableName)).append(" (");

		for (Column c : columns.values()) {
			// column definition
			sb.append(c.getColumnName()).append(" ").append(d.translateToDDLType(c.getColumnType(), c.getLengths()));
			if (c.getNotNull() || c.getPkey())
				sb.append(" NOT NULL");// ??
			sb.append(",");

		}
		// PKEY
		if (!StrUtils.isEmpty(pkeys)) {
			sb.append(" primary key (").append(pkeys).append("),");
		}
		sb.setLength(sb.length() - 1);
		sb.append(")");
		// type or engine for MariaDB & MySql
		sb.append(d.engine());
		return sb.toString();
	}

}
