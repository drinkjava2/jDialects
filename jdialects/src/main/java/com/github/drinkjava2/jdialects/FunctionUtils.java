/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Guess Dialect Utils
 * 
 * @author Yong Zhu
 * @since 1.0.1
 */
@SuppressWarnings("all")
public class FunctionUtils {

	/**
	 * The render method translate funTemplate and args into a real SQL function
	 * piece
	 * 
	 * @param funTemplateID
	 *            SQL template ID
	 * @param args
	 *            function parameters
	 * @return sql piece
	 */
	protected static String render(String funTemplateID, Object... args) {
		return "";//TODO here
	}
}
