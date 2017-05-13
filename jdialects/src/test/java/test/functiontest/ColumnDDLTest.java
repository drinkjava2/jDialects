/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.functiontest;

import static test.utils.tinyjdbc.TinyJdbc.para0;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectConstraint;

/**
 * Test build and drop database tables
 * 
 * @author Yong Zhu
 * @version 1.0.2
 */
public class ColumnDDLTest extends BaseDDLTest {
	private static String createTable2(Dialect d) {
		return d.createTable(testTable) + "(" //
				+ d.column("boolean_col").BOOLEAN() //
				+ ", " + d.column("double_col").DOUBLE() //
				+ ", " + d.column("float_col").FLOAT() //
				+ ", " + d.column("integer_col").INTEGER().pkey().unique().autoInc().required().defaultValue(1) //
				+ ", " + d.column("long_col").LONG() //
				+ ", " + d.column("short_col").SHORT() //
				+ ", " + d.column("bigdecimal_col").BIGDECIMAL(10, 2) //
				+ ", " + d.column("string_col").STRING(20) //
				+ ", " + d.column("date_col").DATE()//
				+ ", " + d.column("time_col").TIME()//
				+ ", " + d.column("timestamp_col").TIMESTAMP() //
				+ ", " + d.constraint("cons1", DialectConstraint.FKEY) //
				+ ")" + d.engine();
	}

	@Test
	public void testCreateAndDropTable() {
		dao.execute(createTable2(dialect));
		Assert.assertEquals(0, (int) dao.queryForInteger("select count(*) from ", testTable));
		dao.execute("insert into ", testTable, " (integer_col,double_col,float_col,string_col) values(?,?,?,?)",
				para0(1, 2.1, 3.3, "str"));
		Assert.assertEquals(1, (int) dao.queryForInteger("select count(*) from ", testTable));
	}
}
