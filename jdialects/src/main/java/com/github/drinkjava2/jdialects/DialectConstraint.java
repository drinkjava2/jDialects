/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

/**
 * To build create/add/modify constraint
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class DialectConstraint {
	public static final String OPERATION_NO = "";
	public static final String OPERATION_ADD = "";

	public static final String PKEY = "PKEY";
	public static final String FKEY = "FKEY";
	public static final String INDEX = "INDEX";
	public static final String UNIQUE = "UNIQUE";
	public static final String CHECK = "CHECK";

	private String operation = OPERATION_NO;
	private Dialect dialect;// In which dialect
	private String table;// In which table
	private String column;// In which column
	private String constraintName;
	private String constraintType;

	public DialectConstraint() {
		// Default constructor
	}

	public DialectConstraint(String constraintName) {
		this.constraintName = constraintName;
	}

	public DialectConstraint name(String constraintName) {
		this.constraintName = constraintName;
		return this;
	}

	public DialectConstraint type(String constraintType) {
		if (PKEY.equalsIgnoreCase(constraintType) || FKEY.equalsIgnoreCase(constraintType)// NOSONAR
				|| INDEX.equalsIgnoreCase(constraintType) || UNIQUE.equalsIgnoreCase(constraintType)
				|| CHECK.equalsIgnoreCase(constraintType))
			this.constraintType = constraintType;
		else
			DialectException.throwEX("ConstraintType can only be one of:\"" + PKEY + "\", \"" + FKEY + "\",\"" + INDEX
					+ "\",\"" + UNIQUE + "\",\"" + CHECK + "\",");
		return this;
	}

	// getter & setters
	public Dialect getDialect() {
		return dialect;
	}

	public DialectConstraint setDialect(Dialect dialect) {
		this.dialect = dialect;
		return this;
	}

	public String getConstraintType() {
		return constraintType;
	}

	public String getOperation() {
		return operation;
	}

	public DialectConstraint setOperation(String operation) {
		this.operation = operation;
		return this;
	}

	public String getTable() {
		return table;
	}

	public DialectConstraint setTable(String table) {
		this.table = table;
		return this;
	}

	public String getColumn() {
		return column;
	}

	public DialectConstraint setColumn(String column) {
		this.column = column;
		return this;
	}

	public DialectConstraint setConstraintType(String constraintType) {
		this.constraintType = constraintType;
		return this;
	}

	public String getConstraintName() {
		return constraintName;
	}

	public DialectConstraint setConstraintName(String constraintName) {
		this.constraintName = constraintName;
		return this;
	}
}
