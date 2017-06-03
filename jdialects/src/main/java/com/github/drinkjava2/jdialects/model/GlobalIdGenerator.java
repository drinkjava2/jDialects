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
public class GlobalIdGenerator {
	public static final String JDIALECTS_IDGEN_TABLE = "jdialects_id_gen";;
	public static final String JDIALECTS_IDGEN_COLUMN = "next_val";
}
