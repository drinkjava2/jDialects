/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.coveragetest.jdialects;

import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;

/**
 * This is unit test for jDialects.Dialect
 * 
 * @author Yong Z.
 *
 */
public class FunctionsTest {

	@Test
	public void test1() {
		System.out.println(Dialect.MySQL55Dialect.fn_ABS());// abs($Params)
		System.out.println(Dialect.MySQL55Dialect.fn_ABS("123"));
		System.out.println();

		System.out.println(Dialect.MySQL55Dialect.fn_COALESCE("a", "b", "c", "d"));// $Params
		System.out.println(Dialect.SybaseASE157Dialect.fn_COALESCE("a", "b", "c", "d")); // $Compact_Params
		System.out.println(Dialect.SybaseASE157Dialect.fn_COALESCE());
		System.out.println();

		System.out.println(Dialect.InformixDialect.fn_nvl("a", "b"));// $NVL_Params
		System.out.println(Dialect.InformixDialect.fn_nvl("a", "b", "c", "d"));
		System.out.println();

		System.out.println(Dialect.Cache71Dialect.fn_concat("a", "b"));// $Lined_Params
		System.out.println(Dialect.Cache71Dialect.fn_concat("a", "b", "c", "d"));
		System.out.println(Dialect.Cache71Dialect.fn_concat("a"));
		System.out.println(Dialect.Cache71Dialect.fn_concat());
		System.out.println();

		System.out.println(Dialect.SQLServerDialect.fn_concat("a", "b"));// $Add_Params
		System.out.println(Dialect.SQLServerDialect.fn_concat("a", "b", "c", "d"));
		System.out.println(Dialect.SQLServerDialect.fn_concat("a"));
		System.out.println(Dialect.SQLServerDialect.fn_concat());
		System.out.println();

		System.out.println(Dialect.Cache71Dialect.fn_position("a", "b"));// $IN_Params
		System.out.println(Dialect.Cache71Dialect.fn_position("a", "b", "c", "d"));
		System.out.println(Dialect.Cache71Dialect.fn_position("a"));
		System.out.println(Dialect.Cache71Dialect.fn_position());
		System.out.println();

		System.out.println(Dialect.H2Dialect.fn_rand());// 0 parameter
		System.out.println(Dialect.H2Dialect.fn_CAST("a", "b"));//2 parameters
		System.out.println(Dialect.H2Dialect.fn_TRIM("a","b","c","d")); //4 parameters
		System.out.println(Dialect.SQLServer2012Dialect.fn_TRIM("a","b"));
		System.out.println();

	}
}
