/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.functiontest;

import static test.utils.tinyjdbc.TinyJdbc.para;
import static test.utils.tinyjdbc.TinyJdbc.para0;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.StrUtils;

import test.functiontest.DataSourceConfig.DataSourceBox;
import test.utils.tinyjdbc.TinyJdbc;

/**
 * Test build and drop database tables
 * 
 * @author Yong Zhu
 * @version 1.0.2
 */
public class CreateAndDropTablesTest extends TestBase {

	private static String createTable1(DataSource ds) {
		Dialect d = Dialect.guessDialect(ds);
		return "create table " + d.check("table1") + "("//
				+ d.BOOLEAN("boolean_") //
				+ ", " + d.DOUBLE("double_") //
				+ ", " + d.FLOAT("float_") //
				+ ", " + d.INTEGER("integer_") //
				+ ", " + d.LONG("long_") //
				+ ", " + d.SHORT("short_") //
				+ ", " + d.BIGDECIMAL("bigdecimal_", 10, 2) //
				+ ", " + d.STRING("string_", 10) //
				+ ", " + d.DATE("date_") //
				+ ", " + d.TIME("time_") //
				+ ", " + d.TIMESTAMP("timestamp_") //
				+ ")" + d.engine();
	}

	@Test
	public void testCreateAndDropTable1() {
		DataSource ds = BeanBox.getBean(DataSourceBox.class);
		Dialect d = Dialect.guessDialect(ds);

		TinyJdbc dao = new TinyJdbc(ds);
		dao.executeQuiet(d.dropTable("table1"));

		dao.execute(createTable1(ds));
		Assert.assertEquals(0, (int) dao.queryForInteger("select count(*) from table1"));
		dao.execute("insert into table1 (integer_,double_,float_,string_) values(?,?,?,?)", para0(1), para(1.1),
				para(1.2), para("str"));
		Assert.assertEquals(1, (int) dao.queryForInteger("select count(*) from table1"));

		dao.execute(d.dropTable("table1"));
		String error = null;
		try {
			dao.queryForInteger("select count(*) from table1");
		} catch (Exception e) {
			error = e.getMessage();
		}
		Assert.assertNotNull(StrUtils.containsIgnoreCase(error, "table1"));
	}

}
