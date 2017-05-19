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

	public String[] toCreateTableDDL(Dialect dialect, boolean formatDDL) {
		if (formatDDL) {
			String[] ddls = toCreateTableDDL(dialect);
			for (int i = 0; i < ddls.length; i++) {
				ddls[i] = DDLFormatter.format(ddls[i]);
			}
			return ddls;
		}
		return toCreateTableDDL(dialect);
	}

	public String[] toCreateTableDDL(Dialect dialect) {
		DDLFeatures ddl = dialect.ddlFeatures;

		StringBuilder createDDL = new StringBuilder();
		boolean hasPkey = false;
		String pkeys = "";

		// Reserved words check
		dialect.check(tableName);
		for (Column col : columns.values()) {
			dialect.check(col.getColumnName());
			dialect.check(col.getPkeyName());
			dialect.check(col.getUniqueConstraintName());
		}

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
		createDDL
				.append(hasPkey ? dialect.ddlFeatures.createTableString : dialect.ddlFeatures.createMultisetTableString)
				.append(" ").append(tableName).append(" (");

		for (Column c : columns.values()) {
			// column definition
			createDDL.append(c.getColumnName()).append(" ")
					.append(dialect.translateToDDLType(c.getColumnType(), c.getLengths()));

			if (c.getNotNull() || c.getPkey()) // Not null
				createDDL.append(" NOT NULL");//
			else
				createDDL.append(ddl.nullColumnString);// Null String

			createDDL.append(",");
		}
		// PKEY
		if (!StrUtils.isEmpty(pkeys)) {
			createDDL.append(" primary key (").append(pkeys).append("),");
		}
		createDDL.setLength(createDDL.length() - 1);
		createDDL.append(")");
		// type or engine for MariaDB & MySql
		createDDL.append(dialect.engine());
		createDDL.append(";");

		List<String> resultList = new ArrayList<>();
		resultList.add(createDDL.toString());

		// add unique constraint
		for (Column column : columns.values()) {
			String uniqueDDL = DDLUtils.getAddUniqueConstraint(dialect, tableName, column);
			if (!StrUtils.isEmpty(uniqueDDL))
				resultList.add(uniqueDDL);
		}

		return resultList.toArray(new String[resultList.size()]);
	}

}
