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
 * The platform-independent table model
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class Table {

	/** The table tableName in database */
	private String tableName;

	/** check constraint for table */
	private String check;

	/** comment for table */
	private String comment;

	/** Columns in this table, key is lower case of column tableName */
	private Map<String, Column> columns = new LinkedHashMap<>();

	/** sequences */
	private Map<String, Sequence> sequences = new LinkedHashMap<>();

	/** tableGenerators */
	private Map<String, TableGenerator> tableGenerators = new LinkedHashMap<>();

	public Table() {
		super();
	}

	public Table(String tableName) {
		this.tableName = tableName;
	}

	public void addTableGenerator(TableGenerator tableGenerator) {
		//@formatter:off
		DialectException.assureNotNull(tableGenerator);
		DialectException.assureNotEmpty(tableGenerator.getName(), "TableGenerator name can not be empty");  
		if (  tableGenerators.get(tableGenerator.getName().toLowerCase()) != null ) {
			DialectException.throwEX("Dulplicated TableGenerator name \"" + tableGenerator.getName() + "\" found in table \""
					+ this.getTableName() + "\"");
		} 
		tableGenerators.put(tableGenerator.getName().toLowerCase(), tableGenerator);
	}

	public void addDefaultTableGenerator() {
		addTableGenerator(new TableGenerator("jdialets_gen", "jdialets_gen", "pk_col", "value_col", "next_val", 1, 1));
	}

	public void addTableGenerator(String name, String tableName, String pkColumnName, String valueColumnName,
			String pkColumnValue, Integer initialValue, Integer allocationSize) {
		addTableGenerator(new TableGenerator(name, tableName, pkColumnName, valueColumnName, pkColumnValue,
				initialValue, allocationSize));
	}

	public void addSequence(String name, String sequenceName, Integer initialValue, Integer allocationSize) {
		this.addSequence(new Sequence(name, sequenceName, initialValue, allocationSize));
	}

	public void addSequence(Sequence sequence) {
		DialectException.assureNotNull(sequence);
		DialectException.assureNotEmpty(sequence.getSequenceName(), "Sequence name can not be empty");
		sequences.put(sequence.getSequenceName().toLowerCase(), sequence);
	}

	public Table check(String check) {
		this.check = check;
		return this;
	}

	public Table comment(String comment) {
		this.comment = comment;
		return this;
	}
  
	public Table append(Column column) {
		DialectException.assureNotNull(column);
		DialectException.assureNotEmpty(column.getColumnName(), "Column tableName can not be empty");
		if (!(columns.get(column.getColumnName().toLowerCase()) == null)) {
			DialectException.throwEX("Dulplicated column name \"" + column.getColumnName() + "\" found in table \""
					+ this.getTableName() + "\"");
		}
		columns.put(column.getColumnName().toLowerCase(), column);
		return this;
	}

	public Column column(String columnName) {
		Column column = new Column(columnName);
		append(column);
		return column;
	}

	public Column getColumn(String columnName) {
		DialectException.assureNotEmpty(columnName);
		return columns.get(columnName.toUpperCase());
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

	public Map<String, Sequence> getSequences() {
		return sequences;
	}

	public void setSequences(Map<String, Sequence> sequences) {
		this.sequences = sequences;
	}

	public Map<String, TableGenerator> getTableGenerators() {
		return tableGenerators;
	}

	public void setTableGenerators(Map<String, TableGenerator> tableGenerators) {
		this.tableGenerators = tableGenerators;
	}
 

}
