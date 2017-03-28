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
	 * Drop and rebuild all tables
	 */
	public static void prepareDatasource_setDefaultSqlBoxConetxt_recreateTables() {
		BeanBox.defaultContext.close();
		SqlBoxContext.setDefaultSqlBoxContext(BeanBox.getBean(DefaultSqlBoxContextBox.class));
		System.out.println("SqlBoxContext Initialization done.");
	}

	/**
	 * Close BeanBox Context
	 */
	public static void closeDatasource_closeDefaultSqlBoxConetxt() {
		BeanBox.defaultContext.close();
		SqlBoxContext.getDefaultSqlBoxContext().close();
	}

	public static void main(String[] args) {
		prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		closeDatasource_closeDefaultSqlBoxConetxt();
	}
}
