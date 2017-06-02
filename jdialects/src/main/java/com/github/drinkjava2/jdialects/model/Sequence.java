/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

/**
 * The platform-independent Sequence model, similar like JPA but use
 * allocationSize instead of allocationSize
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class Sequence {

	/**
	 * A unique generator name that can be referenced by one or more classes to
	 * be the generator for primary key values.
	 */
	private String name;

	/**
	 * The name of the sequence in database
	 */
	private String sequenceName;

	/**
	 * The value from which the sequence is to start generating.
	 */
	private Integer initialValue = 0;

	/**
	 * The amount to allocationSize by when allocating sequence numbers from the
	 * sequence, in Oracle this is identical to "INCREMENT BY", for JPA and ORM
	 * tools this usually value is 50
	 */
	private Integer allocationSize = 1;

	public Sequence() {
		// default constructor
	}

	public Sequence(String name, String sequenceName, Integer initialValue, Integer allocationSize) {
		this.name = name;
		this.sequenceName = sequenceName;
		this.initialValue = initialValue;
		this.allocationSize = allocationSize;
	}

	// getter & setter==============
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
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
