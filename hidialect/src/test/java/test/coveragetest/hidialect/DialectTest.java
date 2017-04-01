/*
 * HiDialect, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.coveragetest.hidialect;

import org.junit.Test;

import com.github.drinkjava2.hidialect.DialectException;
import com.github.drinkjava2.hidialect.Dialect;

/**
 * This is unit test for HiDialect
 * 
 * @author Yong Z.
 *
 */
public class DialectTest {

	@Test
	public void selectTest() {
		System.out.println(Dialect.MySQL5.pagin(1, 10, "select * from user where userid=1 order by id"));
		System.out.println(Dialect.pagin(Dialect.MySQL57, 1, 10, "select * from user where userid=1 order by id"));
	}

	@Test(expected = DialectException.class)
	public void selectWrongTest() {
		System.out.println(Dialect.MySQL5.pagin(1, 10, "query from user"));
	}

	@Test(expected = DialectException.class)
	public void notSupportTest() {
		System.out.println(Dialect.Cache71.pagin(1, 10, "select * from user"));
	}

	@Test(expected = DialectException.class)
	public void nullTest() {
		System.out.println(Dialect.pagin(null, 1, 10, "select * from user where userid=1 order by id"));
	}

}
