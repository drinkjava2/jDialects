/**
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.drinkjava2.hibernate.DDLFormatter;
import com.github.drinkjava2.jdialects.model.Column;
import com.github.drinkjava2.jdialects.model.FKeyConstraint;
import com.github.drinkjava2.jdialects.model.AutoIdGenerator;
import com.github.drinkjava2.jdialects.model.InlineFKeyConstraint;
import com.github.drinkjava2.jdialects.model.Sequence;
import com.github.drinkjava2.jdialects.model.Table;
import com.github.drinkjava2.jdialects.model.TableGenerator;

/**
 * DDL utilities used to transfer platform-independent model to drop or create
 * DDL String array
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class DDLDropUtils {
	private static DialectLogger logger = DialectLogger.getLog(DDLDropUtils.class);

	/**
	 * Transfer tables to drop and create DDL
	 */
	public static String[] toDropAndCreateDDL(Dialect dialect, Table... tables) {
		String[] drop = toDropAndCreateDDL(dialect, tables);
		String[] create = toDropDDL(dialect, tables);
		return StrUtils.joinStringArray(drop, create);
	}

	/**
	 * Transfer tables to drop DDL
	 */
	public static String[] toDropDDL(Dialect dialect, Table... tables) {
		String[] ddls = toDropDDLwithoutFormat(dialect, tables);
		for (int i = 0; i < ddls.length; i++) {
			ddls[i] = DDLFormatter.format(ddls[i]) + ";";
		}
		return ddls;
	}

	/**
	 * Transfer tables to drop DDL and without format it
	 */
	public static String[] toDropDDLwithoutFormat(Dialect dialect, Table... tables) {
		return new String[0];
	}

}
