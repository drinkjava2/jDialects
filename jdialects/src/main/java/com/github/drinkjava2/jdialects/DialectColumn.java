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

	// getter & setters

	public String getTableName() {
		return tableName;
	}

	public Dialect getDialect() {
		return dialect;
	}

	public DialectColumn setDialect(Dialect dialect) {
		this.dialect = dialect;
		return this;
	}

	public String getOperation() {
		return operation;
	}

	public DialectColumn setOperation(String operation) {
		this.operation = operation;
		return this;
	}

	public DialectColumn setTableName(String tableName) {
		this.tableName = tableName;
		return this;
	}

	public String getColumnName() {
		return columnName;
	}

	public DialectColumn setColumnName(String columnName) {
		this.columnName = columnName;
		return this;
	}

	public String getColumnType() {
		return columnType;
	}

	public DialectColumn setColumnType(String columnType) {
		this.columnType = columnType;
		return this;
	}

	public Boolean getRequired() {
		return required;
	}

	public DialectColumn setRequired(Boolean required) {
		this.required = required;
		return this;
	}

	public Boolean getUnique() {
		return unique;
	}

	public DialectColumn setUnique(Boolean unique) {
		this.unique = unique;
		return this;
	}

	public Boolean getAutoInc() {
		return autoInc;
	}

	public DialectColumn setAutoInc(Boolean autoInc) {
		this.autoInc = autoInc;
		return this;
	}

	public Boolean getPkey() {
		return pkey;
	}

	public DialectColumn setPkey(Boolean pkey) {
		this.pkey = pkey;
		return this;
	}

	public List<DialectConstraint> getConstraints() {
		return constraints;
	}

	public DialectColumn setConstraints(List<DialectConstraint> constraints) {
		this.constraints = constraints;
		return this;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public DialectColumn setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

}
