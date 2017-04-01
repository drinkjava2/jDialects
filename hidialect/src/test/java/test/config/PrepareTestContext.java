/*
 * HiDialect, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.config;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import test.config.JBeanBoxConfig.DefaultSqlBoxContextBox;

/**
 * This is a configuration class, equal to XML in Spring
 *
 */
public class PrepareTestContext {

	/**
	 * prepare DataSource, set DefaultSqlBoxConetxt, recreate tables
	 */
	public static void prepareDatasource_setDefaultSqlBoxConetxt_recreateTables() {
		BeanBox.defaultContext.close();
		SqlBoxContext.setDefaultSqlBoxContext(BeanBox.getBean(DefaultSqlBoxContextBox.class));
		System.out.println("SqlBoxContext Initialization done.");
	}

	/**
	 * Close BeanBox Context, DataSource will be closed automatically when
	 * BenBox Context be closed if "PreDestory" method be set
	 */
	public static void closeDatasource_closeDefaultSqlBoxConetxt() {
		BeanBox.defaultContext.close();
		SqlBoxContext.getDefaultSqlBoxContext().close();
	}

}
