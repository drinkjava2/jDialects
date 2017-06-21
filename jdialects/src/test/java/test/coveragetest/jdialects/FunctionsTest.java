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
		 System.out.println(Dialect.MySQL55Dialect.fn_abs());
		 System.out.println(Dialect.MySQL55Dialect.fn_abs("123"));
	}
}
