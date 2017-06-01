/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.functiontest;

import static test.utils.tinyjdbc.TinyJdbc.para_;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.model.Table;

import test.BaseDDLTest;

/**
 * Test build and drop database tables
 * 
 * @author Yong Zhu
 * @version 1.0.2
 */
public class ColumnDDLTest extends BaseDDLTest {
	private static Table tableModel() {
		Table t = new Table(testTable);
		t.column("b1").BOOLEAN();
		t.column("d2").DOUBLE();
		t.column("f3").FLOAT();
		t.column("i4").INTEGER().pkey().unique().notNull().defaultValue("1");
		t.column("l5").LONG();
		t.column("s6").SHORT();
		t.column("b7").BIGDECIMAL(10, 2);
		t.column("s8").STRING(20);
		t.column("d9").DATE();
		t.column("t10").TIME();
		t.column("t11").TIMESTAMP();
		t.column("v12").VARCHAR(300);
		return t;
	}

	@Test
	public void testCreateAndDropTable() {
		String[] ddl = tableModel().toCreateDDL(dialect);
		dao.executeManySqls(ddl);
		Assert.assertEquals(0, (int) dao.queryForInteger("select count(*) from ", testTable));
		dao.execute("insert into ", testTable, "(i4,d2,f3,s8) values(?,?,?,?)", para_(1, 2.1, 3.3, "str"));
		Assert.assertEquals(1, (int) dao.queryForInteger("select count(*) from ", testTable));
	}
}
