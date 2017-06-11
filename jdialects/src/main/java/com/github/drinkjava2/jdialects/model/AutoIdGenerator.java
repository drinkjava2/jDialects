/**
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

/**
 * The global unique ID generator named as "jdialects_autoid" (similar like in
 * Hibernate the table "hibernate_sequence"), one database only allowed 1
 * AutoIdGenerator table, all table columns which need a "auto" type ID will
 * share use this table
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class AutoIdGenerator {
	public static final String JDIALECTS_AUTOID = "jdialects_autoid";;
	public static final String NEXT_VAL = "next_val";
}
