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
	public static final String OPERATION_NO = "";
	public static final String OPERATION_ADD = "";
	public static final String OPERATION_MODIFY = "";

	private String operation = OPERATION_NO;
	private String tableName;// In which table
	private String columnName;
	private String columnType;
	private Boolean required;
	private Boolean unique;
	private Boolean autoInc;
	private Boolean pkey;
	private Object defaultValue;
	private List<DialectConstraint> constraints = new ArrayList<>();

	public DialectColumn(String columnName, String columnType) {
		this.columnName = columnName;
		this.columnType = columnType;
	}

	public DialectColumn required() {
		this.required = true;
		return this;
	}

	public DialectColumn unique() {
		this.unique = true;
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
		return pkey("");
	}

	public DialectColumn pkey(String pkeyName) {
		DialectConstraint cons = new DialectConstraint();
		cons.setConstraintType(DialectConstraint.PKEY);
		cons.setConstraintName(pkeyName);
		constraints.add(cons);
		this.pkey = true;
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
		return "aa";
	}

	// getter & setters
	public String getTableName() {
		return tableName;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Boolean getUnique() {
		return unique;
	}

	public void setUnique(Boolean unique) {
		this.unique = unique;
	}

	public Boolean getAutoInc() {
		return autoInc;
	}

	public void setAutoInc(Boolean autoInc) {
		this.autoInc = autoInc;
	}

	public Boolean getPkey() {
		return pkey;
	}

	public void setPkey(Boolean pkey) {
		this.pkey = pkey;
	}

	public List<DialectConstraint> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<DialectConstraint> constraints) {
		this.constraints = constraints;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

}
