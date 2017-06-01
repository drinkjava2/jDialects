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
 * The platform-independent database model
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class Database {

	/** Tables in this Database, key is lower case of table name */
	private Map<String, Table> tables = new LinkedHashMap<>();

	public Database addTable(Table table) {
		DialectException.assureNotNull(table);
		DialectException.assureNotEmpty(table.getTableName(), "Table name can not be empty");
		tables.put(table.getTableName().toLowerCase(), table);
		return this;
	}

	// getter & setter=========
	public Map<String, Table> getTables() {
		return tables;
	}

	public void setTables(Map<String, Table> tables) {
		this.tables = tables;
	}

}
