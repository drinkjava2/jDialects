/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.coveragetest;

import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.translate.TranslateUtil;

/**
 * 
 * @author Yong Z.
 * @since 1.0.6
 *
 */
public class FunctionTranslateTest {

	@Test
	public void doTest() {
		TranslateUtil trans = new TranslateUtil();
		//String result=(String)
				trans.doParse(Dialect.SQLiteDialect,"select a,b, c as d, date() as 3, cos(3),e, #concat(#year(1998),'sdfasd', true, sin('b',3,?)    ,c), #second((199.8)+20) from user as u");
		//System.out.println(result);
	}

}
