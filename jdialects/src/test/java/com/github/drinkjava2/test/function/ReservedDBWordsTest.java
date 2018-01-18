/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.test.function;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.ReservedDBWords;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;

/**
 * StrUtils Unit Test
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
@Table(name = "order") // "order" is a reserved word for all database
public class ReservedDBWordsTest {

	@Test
	public void doTest() {
		Assert.assertTrue(ReservedDBWords.isReservedWord(Dialect.H2Dialect, "CURRENT_TIMESTAMP"));
		Assert.assertFalse(ReservedDBWords.isReservedWord(Dialect.H2Dialect, "CURRENT_TIMESTAMP___"));
		Assert.assertTrue(ReservedDBWords.isReservedWord(Dialect.H2Dialect, "AUTHORIZATION"));

	}

	@Test(expected = DialectException.class)
	public void doTestExceptionThrow() {
		Dialect.MySQL55Dialect.toCreateDDL(ReservedDBWordsTest.class);
	}
}
