/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

/**
 * The platform-independent Sequence model, similar like JPA but use increment
 * instead of allocationSize
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class Sequence {

	/**
	 * The name of the database sequence object from which to obtain primary key
	 * values.
	 */
	private String sequenceName;

	/**
	 * (Optional) The value from which the sequence object is to start
	 * generating.
	 */
	private Integer initialValue = 1;

	/**
	 * (Optional) The increment when allocating sequence numbers from the
	 * sequence, this is different with allocationSize.
	 */
	private Integer increment = 1;

	// getter & setter==============
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

	public Integer getIncrement() {
		return increment;
	}

	public void setIncrement(Integer increment) {
		this.increment = increment;
	}
}
