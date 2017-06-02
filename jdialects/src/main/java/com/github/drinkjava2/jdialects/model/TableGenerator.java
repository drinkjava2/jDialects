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
	 * A unique generator name that can be referenced by one or more classes to
	 * be the generator for id values.
	 */
	private String name;

	/**
	 * Name of the primary key column in the table.
	 */
	private String pkColumnName = "";

	/**
	 * Name of the column that stores the last value generated.
	 */
	private String valueColumnName = "";

	/**
	 * The primary key value in the generator table that distinguishes this set
	 * of generated values from others that may be stored in the table.
	 */
	private String pkColumnValue = "";

	/**
	 * The initial value to be used when allocating id numbers from the
	 * generator.
	 */
	private Integer initialValue = 0;

	/**
	 * The amount to increment by when allocating id numbers from the generator.
	 */
	private Integer allocationSize = 50;

	public TableGenerator() {
		super();
		// default constructor
	}

	public TableGenerator(String name, String tableName, String pkColumnName, String valueColumnName,
			String pkColumnValue, Integer initialValue, Integer allocationSize) {
		this.name = name;
		this.setTableName(tableName);
		this.pkColumnName = pkColumnName;
		this.valueColumnName = valueColumnName;
		this.pkColumnValue = pkColumnValue;
		this.initialValue = initialValue;
		this.allocationSize = allocationSize;
	}

	// getter && setter=====================
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
