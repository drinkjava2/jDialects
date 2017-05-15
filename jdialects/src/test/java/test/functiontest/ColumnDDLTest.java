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

import com.github.drinkjava2.jdialects.Table;

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
		t.addColumn("b1").BOOLEAN();
		t.addColumn("d2").DOUBLE();
		t.addColumn("f3").FLOAT();
		t.addColumn("i4").INTEGER().pkey().unique().autoInc().notNull().defaultValue(1);
		t.addColumn("l5").LONG();
		t.addColumn("s6").SHORT();
		t.addColumn("b7").BIGDECIMAL(10, 2);
		t.addColumn("s8").STRING(20);
		t.addColumn("d9").DATE();
		t.addColumn("t10").TIME();
		t.addColumn("t11").TIMESTAMP();
		t.addColumn("v12").VARCHAR(300);
		return t;
	}

	@Test
	public void testCreateAndDropTable() {
		String ddl = tableModel().toCreateTableDDL(dialect);
		dao.execute(ddl);
		Assert.assertEquals(0, (int) dao.queryForInteger("select count(*) from ", testTable));
		dao.execute("insert into ", testTable, "(i4,d2,f3,s8) values(?,?,?,?)", para_(1, 2.1, 3.3, "str"));
		Assert.assertEquals(1, (int) dao.queryForInteger("select count(*) from ", testTable));
	}
}
