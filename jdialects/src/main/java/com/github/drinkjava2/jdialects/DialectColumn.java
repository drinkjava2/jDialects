/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import java.util.ArrayList;
import java.util.List;

/**
 * To build create/add/modify column
 * 
 * <pre>
For example:

SQL=d.createTable("xxx")+"("+
        d.column("colname", d.Type).required().unique().autoInc().PKey("pkname").default().description().addConstraint().pkey() //
  +","+ d.column("colname", d.Type).required().autoInc().FKey("table","col1,col2").default("").description()   
   +")" +d.engine("");
  
SQL=d.addColumn("colname", d.Type).notNull().autoINC().isPkey("").default().description().toString();
 * 
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class DialectColumn {
	public static final String OPERATION_NO = "NO";
	public static final String OPERATION_ADD = "ADD";

	private String operation = OPERATION_NO;
	private Dialect dialect;// In which dialect
	private String tableName;// In which table
	private String columnName;
	private String columnType;
	private Boolean required;
	private Boolean unique;
	private Boolean autoInc;
	private String pkeyName;
	private Boolean pkey;
	private Object defaultValue; 

	public DialectColumn(String columnName) {
		this.columnName = columnName;
	}

	public DialectColumn required() {
		this.required = true;
		return this;
	}

	public DialectColumn unique() {
		this.unique = true;
		return this;
	}

	public DialectColumn unique(Boolean unique) {
		this.unique = unique;
		return this;
	}

	public DialectColumn autoInc() {
		this.autoInc = true;
		return this;
	}

	public DialectColumn defaultValue(Object value) {
		this.defaultValue = value;
		return this;
	}

	public DialectColumn pkey() {
		this.pkey = true;
		return this;
	}

	public DialectColumn pkey(Boolean pkey) {
		this.pkey = pkey;
		return this;
	}

	public DialectColumn pkey(String pkeyName) { 
		this.pkey = true;
		this.pkeyName=pkeyName;
		return this;
	}

	/**
	 * Return a DDL String, can be one of below format:
	 * 
	 * <pre>
	   name varchar(30)...
	   alter table xxx add column somename varchar(30) ...
	 * </pre>
	 */
	@Override
	public String toString() {
		switch (operation) {
		case OPERATION_NO:
			return columnName + " " + columnType;
		case OPERATION_ADD: {// NOSONAR
			if (!DDLFeatures.isValidString(dialect.ddlFeatures.addColumnString))
				return (String) DialectException.throwEX(dialect + " does not support add column.");
			else
				return new StringBuilder("alter table ").append(dialect.ddlFeatures.addColumnString).append(" ")
						.append(columnName).append(" ").append(columnType)
						.append(dialect.ddlFeatures.addColumnSuffixString).toString();
		}
		default:
			return (String) DialectException.throwEX("Unknow operation type for DialectColumn");
		}
	}

	public Dialect getDialect() {
		return dialect;
	}

	public DialectColumn setDialect(Dialect dialect) {
		this.dialect = dialect;
		return this;
	}

	public String getTableName() {
		return tableName;
	}

	public DialectColumn setTableName(String tableName) {
		this.tableName = tableName;
		return this;
	}

	public String getOperation() {
		return operation;
	}

	public DialectColumn setOperation(String operation) {
		this.operation = operation;
		return this;
	}

	//@formatter:off shut off eclipse's formatter
	public DialectColumn LONG() {this.columnType=dialect.LONG() ; return this;} 
	public DialectColumn BOOLEAN() {this.columnType=dialect.BOOLEAN() ; return this;} 
	public DialectColumn DOUBLE() {this.columnType=dialect.DOUBLE() ; return this;} 
	public DialectColumn FLOAT() {this.columnType=dialect.FLOAT() ; return this;} 
	public DialectColumn INTEGER() {this.columnType=dialect.INTEGER() ; return this;} 
	public DialectColumn SHORT() {this.columnType=dialect.SHORT() ; return this;} 
	public DialectColumn BIGDECIMAL(int precision, int scale) {this.columnType=dialect.BIGDECIMAL(precision,  scale) ; return this;} 
	public DialectColumn STRING(int length) {this.columnType=dialect.STRING(length) ; return this;} 
	public DialectColumn DATE() {this.columnType=dialect.DATE() ; return this;} 
	public DialectColumn TIME() {this.columnType=dialect.TIME() ; return this;} 
	public DialectColumn TIMESTAMP() {this.columnType=dialect.TIMESTAMP() ; return this;} 
	public DialectColumn BIGINT() {this.columnType=dialect.BIGINT() ; return this;} 
	public DialectColumn BINARY(int... lengths) {this.columnType=dialect.BINARY(lengths) ; return this;} 
	public DialectColumn BIT() {this.columnType=dialect.BIT() ; return this;} 
	public DialectColumn BLOB(int... lengths) {this.columnType=dialect.BLOB(lengths) ; return this;} 
	public DialectColumn CHAR(int... lengths) {this.columnType=dialect.CHAR(lengths) ; return this;} 
	public DialectColumn CLOB(int... lengths) {this.columnType=dialect.CLOB( lengths) ; return this;} 
	public DialectColumn DECIMAL(int... lengths) {this.columnType=dialect.DECIMAL( lengths) ; return this;} 
	public DialectColumn JAVA_OBJECT() {this.columnType=dialect.JAVA_OBJECT() ; return this;} 
	public DialectColumn LONGNVARCHAR(int length) {this.columnType=dialect.LONGNVARCHAR( length) ; return this;} 
	public DialectColumn LONGVARBINARY(int... lengths) {this.columnType=dialect.LONGVARBINARY( lengths) ; return this;} 
	public DialectColumn LONGVARCHAR(int... lengths) {this.columnType=dialect.LONGVARCHAR( lengths) ; return this;} 
	public DialectColumn NCHAR(int length) {this.columnType=dialect.NCHAR(length) ; return this;} 
	public DialectColumn NCLOB() {this.columnType=dialect.NCLOB() ; return this;} 
	public DialectColumn NUMERIC(int... lengths) {this.columnType=dialect.NUMERIC( lengths) ; return this;} 
	public DialectColumn NVARCHAR(int length) {this.columnType=dialect.NVARCHAR( length) ; return this;} 
	public DialectColumn OTHER(int... lengths) {this.columnType=dialect.OTHER( lengths) ; return this;} 
	public DialectColumn REAL() {this.columnType=dialect.REAL() ; return this;} 
	public DialectColumn SMALLINT() {this.columnType=dialect.SMALLINT() ; return this;} 
	public DialectColumn TINYINT() {this.columnType=dialect.TINYINT() ; return this;} 
	public DialectColumn VARBINARY(int... lengths) {this.columnType=dialect.VARBINARY( lengths) ; return this;} 
	public DialectColumn VARCHAR(int length) {this.columnType=dialect.VARCHAR(length) ; return this;} 

}
