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
	private String table;// belong to which table
	private String column;// belong to which column
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

	public String getConstraintType() {
		return constraintType;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public void setConstraintType(String constraintType) {
		this.constraintType = constraintType;
	}

	public String getConstraintName() {
		return constraintName;
	}

	public void setConstraintName(String constraintName) {
		this.constraintName = constraintName;
	}
}
