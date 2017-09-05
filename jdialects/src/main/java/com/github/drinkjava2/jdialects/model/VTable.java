/**
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdialects.DialectException;

/**
 * A Virtual Table definition represents a platform dependent Database Table,
 * from 1.0.5 this class name changed from "Table" to "VTable" to avoid naming
 * conflict to JPA's "@Table" annotation
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class VTable {

	/** The table tableName in database */
	private String tableName;

	/** check constraint for table */
	private String check;

	/** comment for table */
	private String comment;

	/**
	 * Optional, If support engine like MySQL or MariaDB, add engineTail at the end
	 * of "create table..." DDL, usually used to set encode String like " DEFAULT
	 * CHARSET=utf8" for MySQL
	 */
	private String engineTail;

	/** Columns in this table, key is lower case of column tableName */
	private Map<String, VColumn> columns = new LinkedHashMap<String, VColumn>();

	/** sequences */
	private Map<String, Sequence> sequences = new LinkedHashMap<String, Sequence>();

	/** tableGenerators */
	private Map<String, TableGenerator> tableGenerators = new LinkedHashMap<String, TableGenerator>();

	private List<FKeyConst> fkeyConstraints = new ArrayList<FKeyConst>();
	private List<IndexConst> indexConsts = new ArrayList<IndexConst>();
	private List<UniqueConst> uniqueConsts = new ArrayList<UniqueConst>();

	public VTable() {
		super();
	}

	public VTable(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Add a "create table..." DDL to generate ID, similar like JPA's TableGenerator
	 */
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
 
	/**
	 * Add a "create table..." DDL to generate ID, similar like JPA's TableGenerator 
	 * @param name The name of TableGenerator Java object itself
	 * @param tableName The name of the table will created in database to generate ID
	 * @param pkColumnName The name of prime key column
	 * @param valueColumnName The name of value column
	 * @param pkColumnValue The value in prime key column
	 * @param initialValue The initial value
	 * @param allocationSize The allocationSize
	 */
	public void addTableGenerator(String name, String tableName, String pkColumnName, String valueColumnName,
			String pkColumnValue, Integer initialValue, Integer allocationSize) {
		addTableGenerator(new TableGenerator(name, tableName, pkColumnName, valueColumnName, pkColumnValue,
				initialValue, allocationSize));
	}

	/**
	 * Add a sequence definition DDL, note: some dialects do not support sequence
	 * @param name The name of sequence Java object itself
	 * @param sequenceName the name of the sequence will created in database
	 * @param initialValue The initial value
	 * @param allocationSize The allocationSize
	 */
	public void addSequence(String name, String sequenceName, Integer initialValue, Integer allocationSize) {
		this.addSequence(new Sequence(name, sequenceName, initialValue, allocationSize));
	}

	/**
	 * Add a sequence definition DDL 
	 */
	public void addSequence(Sequence sequence) {
		DialectException.assureNotNull(sequence);
		DialectException.assureNotEmpty(sequence.getSequenceName(), "Sequence name can not be empty");
		sequences.put(sequence.getSequenceName().toLowerCase(), sequence);
	}

	/**
	 *  Add the table check String DDL piece if support
	 */
	public VTable check(String check) {
		this.check = check;
		return this;
	}

	/**
	 *  Add the table comment String DDL piece if support
	 */
	public VTable comment(String comment) {
		this.comment = comment;
		return this;
	}
  
	/**
	 * Add a column definition piece in DDL
	 */
	public VTable addColumn(VColumn column) {
		DialectException.assureNotNull(column);
		DialectException.assureNotEmpty(column.getColumnName(), "Column's columnName can not be empty");
		if ((columns.get(column.getColumnName().toLowerCase()) != null)) {
			DialectException.throwEX("Dulplicated column name \"" + column.getColumnName() + "\" found in table \""
					+ this.getTableName() + "\"");
		}
		columns.put(column.getColumnName().toLowerCase(), column);
		return this;
	}

 
	/**
	 * Start add a column definition piece in DDL, detail usage see demo
	 *  
	 * @param columnName
	 * @return the Column object
	 */
	public VColumn column(String columnName) {
		VColumn column = new VColumn(columnName);
		column.setVtable(this);
		addColumn(column);
		return column;
	}

	/**
	 * Return Column object by columnName
	 */
	public VColumn getColumn(String columnName) {
		DialectException.assureNotEmpty(columnName);
		return columns.get(columnName.toLowerCase());
	}
	
	/**
	 *  Start add a foreign key definition in DDL, detail usage see demo
	 */
	public FKeyConst fkey() {
		FKeyConst fkey=new FKeyConst(); 
		fkey.setTableName(this.tableName);
		this.fkeyConstraints.add(fkey);
		return fkey;
	}
	
	/**
	 *  Start add a foreign key definition in DDL, detail usage see demo
	 */
	public FKeyConst fkey(String fkeyName) {
		FKeyConst fkey=fkey();
		fkey.setFkeyName(fkeyName);
		return fkey;
	}
	
	/**
	 *  Start add a Index in DDL, detail usage see demo
	 */
	public IndexConst index( ) {
		IndexConst index=new IndexConst( ); 
		index.setTableName(this.tableName);
		this.indexConsts.add(index);
		return index;
	}
	
	/**
	 *  Start add a Index in DDL, detail usage see demo
	 */
	public IndexConst index(String indexName) {
		IndexConst index=index();
		index.setName(indexName);
		return index;
	}
	
	/**
	 *  Start add a unique constraint in DDL, detail usage see demo
	 */
	public UniqueConst unique( ) {
		UniqueConst unique=new UniqueConst( ); 
		unique.setTableName(this.tableName);
		this.uniqueConsts.add(unique);
		return unique;
	}
	
	/**
	 *  Start add a unique constraint in DDL, detail usage see demo
	 */
	public UniqueConst unique(String uniqueName) {
		UniqueConst unique=unique();
		unique.setName(uniqueName);
		return unique;
	}
 
	/**
	 * If support engine like MySQL or MariaDB, add engineTail at the end of
	 * "create table..." DDL, usually used to set encode String like " DEFAULT CHARSET=utf8" for MySQL
	 */
	public VTable engineTail(String engineTail) {
		this.engineTail=engineTail;
		return this;
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

	public Map<String, VColumn> getColumns() {
		return columns;
	}

	public void setColumns(Map<String, VColumn> columns) {
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

	public List<FKeyConst> getFkeyConstraints() {
		return fkeyConstraints;
	}

	public void setFkeyConstraints(List<FKeyConst> fkeyConstraints) {
		this.fkeyConstraints = fkeyConstraints;
	}

	public String getEngineTail() {
		return engineTail;
	}

	public void setEngineTail(String engineTail) {
		this.engineTail = engineTail;
	}

	public List<IndexConst> getIndexConsts() {
		return indexConsts;
	}

	public void setIndexConsts(List<IndexConst> indexConsts) {
		this.indexConsts = indexConsts;
	}

	public List<UniqueConst> getUniqueConsts() {
		return uniqueConsts;
	}

	public void setUniqueConsts(List<UniqueConst> uniqueConsts) {
		this.uniqueConsts = uniqueConsts;
	} 
	
}
