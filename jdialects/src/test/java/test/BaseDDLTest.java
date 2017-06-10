/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test;

import javax.sql.DataSource;

import org.junit.After;
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
	protected Dialect guessedDialect = null;
	protected TinyJdbc dao = null;

	@Before
	public void initDao() {
		ds = BeanBox.getBean(DataSourceBox.class);
		guessedDialect = Dialect.guessDialect(ds);
		dao = new TinyJdbc(ds);
	}

	@After
	public void closeDataSource() {
		BeanBox.defaultContext.close();// close dataSource
	}

}
