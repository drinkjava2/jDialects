/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdialects.Dialect;

import test.functiontest.DataSourceConfig.DataSourceBox;
import test.utils.tinyjdbc.TinyJdbc;

/**
 * This test base class in charge of close data sources.
 * 
 * @author Yong Z.
 * @since 1.0.2
 *
 */
public class BaseDDLTest {
	protected DataSource ds = null;
	protected Dialect dialect = null;
	protected TinyJdbc dao = null;
	protected static final String testTable = "DDLTestTable";

	@Before
	public void initDao() {
		ds = BeanBox.getBean(DataSourceBox.class);
		dialect = Dialect.guessDialect(ds);
		dao = new TinyJdbc(ds);
		dao.executeQuiet(dialect.dropTable(testTable));
		assertTableNotExist(testTable);
	}

	@After
	public void closeDataSource() {
		dao.execute(dialect.dropTable(testTable));
		assertTableNotExist(testTable);
		BeanBox.defaultContext.close();// close dataSource
	}

	protected void assertTableNotExist(String table) {
		boolean hasException = false;
		try {
			dao.queryForInteger("select count(*) from " + table);
		} catch (Exception e) {
			hasException = true;
		}
		Assert.assertTrue(hasException);
	}

}
