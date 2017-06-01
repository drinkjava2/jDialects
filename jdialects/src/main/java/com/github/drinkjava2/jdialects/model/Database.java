/**
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

import java.util.LinkedHashMap;
import java.util.Map;

import com.github.drinkjava2.jdialects.DialectException;

/**
 * The platform-independent database model
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class Database {

	/** Tables in this Database, key is lower case of table name */
	private Map<String, Table> tables = new LinkedHashMap<>();

	private Map<String, Sequence> sequences = new LinkedHashMap<>();

	public Database addTable(Table table) {
		DialectException.assureNotNull(table);
		DialectException.assureNotEmpty(table.getTableName(), "Table name can not be empty");
		tables.put(table.getTableName().toLowerCase(), table);
		return this;
	}

	public Database addSequence(Sequence sequence) {
		DialectException.assureNotNull(sequence);
		DialectException.assureNotEmpty(sequence.getSequenceName(), "Sequence name can not be empty");
		sequences.put(sequence.getSequenceName().toLowerCase(), sequence);
		return this;
	}

	// getter & setter=========
	public Map<String, Table> getTables() {
		return tables;
	}

	public void setTables(Map<String, Table> tables) {
		this.tables = tables;
	}

	public Map<String, Sequence> getSequences() {
		return sequences;
	}

	public void setSequences(Map<String, Sequence> sequences) {
		this.sequences = sequences;
	}

}
