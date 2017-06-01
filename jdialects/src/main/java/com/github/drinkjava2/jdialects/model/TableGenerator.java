/**
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

/**
 * The platform-independent table model
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class TableGenerator extends Table {

	/**
	 * (Required) A unique generator name that can be referenced by one or more
	 * classes to be the generator for id values.
	 */
	private String name;

	/**
	 * (Optional) Name of table that stores the generated id values.
	 * <p>
	 * Defaults to a name chosen by persistence provider.
	 */
	private String table = "";

	private String pkColumnName = "";

	/**
	 * (Optional) Name of the column that stores the last value generated.
	 * <p>
	 * Defaults to a provider-chosen name.
	 */
	private String valueColumnName = "";

	/**
	 * (Optional) The primary key value in the generator table that
	 * distinguishes this set of generated values from others that may be stored
	 * in the table.
	 * <p>
	 * Defaults to a provider-chosen value to store in the primary key column of
	 * the generator table
	 */
	private String pkColumnValue = "";

	/**
	 * (Optional) The initial value to be used when allocating id numbers from
	 * the generator.
	 */
	private Integer initialValue = 0;

	/**
	 * (Optional) The amount to increment by when allocating id numbers from the
	 * generator.
	 */
	private Integer allocationSize = 50;

	// getter && setter=====================

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getPkColumnName() {
		return pkColumnName;
	}

	public void setPkColumnName(String pkColumnName) {
		this.pkColumnName = pkColumnName;
	}

	public String getValueColumnName() {
		return valueColumnName;
	}

	public void setValueColumnName(String valueColumnName) {
		this.valueColumnName = valueColumnName;
	}

	public String getPkColumnValue() {
		return pkColumnValue;
	}

	public void setPkColumnValue(String pkColumnValue) {
		this.pkColumnValue = pkColumnValue;
	}

	public Integer getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(Integer initialValue) {
		this.initialValue = initialValue;
	}

	public Integer getAllocationSize() {
		return allocationSize;
	}

	public void setAllocationSize(Integer allocationSize) {
		this.allocationSize = allocationSize;
	}
}
