/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.coveragetest.jdialects;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.StrUtils;

/**
 * This is unit test for DDL
 * 
 * @author Yong Z.
 * @since 1.0.2
 *
 */
public class DDLTest {

	@Test
	public void testCreateTable() {
		for (Dialect d : Dialect.values()) {
			String createtable = d.createTable("testTable");
			System.out.println(createtable);
			Assert.assertTrue(StrUtils.containsIgnoreCase(createtable, "create"));
			Assert.assertTrue(StrUtils.containsIgnoreCase(createtable, "testTable"));
		}
	}

	@Test
	public void testColumn() {
		for (Dialect d : Dialect.values()) {
			String columnDDL = d.column("testColumn", d.INTEGER()).toString();
			System.out.println(columnDDL);
		}
	}

	@Test
	public void testAddColumn() {
		for (Dialect d : Dialect.values()) {
			String columnDDL = null;
			try {
				columnDDL = d.addColumn("testTable", "testColumn", d.INTEGER()).toString();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			System.out.println(columnDDL);
		}
	}
}
