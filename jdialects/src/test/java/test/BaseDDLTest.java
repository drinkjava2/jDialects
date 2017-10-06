/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.TableModel;

import test.DataSourceConfig.DataSourceBox;

/**
 * This base test class in charge of configure and close data sources.
 * 
 * @author Yong Z.
 * @since 1.0.2
 *
 */
public class BaseDDLTest {
	protected DataSource ds = BeanBox.getBean(DataSourceBox.class);
	protected DbPro db = new DbPro(ds);
	protected Dialect guessedDialect = Dialect.guessDialect(ds);;

	@Before
	public void initDao() {
		System.out.println("Current guessedDialect=" + guessedDialect);
	}

	@After
	public void closeDataSource() {
		BeanBox.defaultContext.close();// close dataSource
	}

	protected static void printDDLs(String[] ddl) {
		for (String str : ddl) {
			System.out.println(str);
		}
	}

	protected void quiteExecuteNoParamSqls(String... sqls) {
		for (String sql : sqls) {
			try {
				db.nExecute(sql);
			} catch (Exception e) {
			}
		}
	}

	public void reBuildDB(TableModel... tables) {
		String[] ddls = guessedDialect.toDropDDL(tables);
		quiteExecuteNoParamSqls(ddls);

		ddls = guessedDialect.toCreateDDL(tables);
		executeNoParamSqls(ddls);
	}

	protected void executeNoParamSqls(String... sqls) {
		for (String sql : sqls)
			db.nExecute(sql);
	}

	protected void testOnCurrentRealDatabase(TableModel... tables) {
		System.out.println("======Test on real Database of dialect: " + guessedDialect + "=====");

		String[] ddls = guessedDialect.toDropDDL(tables);

		quiteExecuteNoParamSqls(ddls);

		ddls = guessedDialect.toCreateDDL(tables);
		executeNoParamSqls(ddls);

		ddls = guessedDialect.toDropAndCreateDDL(tables);
		executeNoParamSqls(ddls);

		ddls = guessedDialect.toDropDDL(tables);
		executeNoParamSqls(ddls);
	}

	protected static void printOneDialectsDDLs(Dialect dialect, TableModel... tables) {
		System.out.println("======" + dialect + "=====");
		try {
			String[] ddls = dialect.toDropAndCreateDDL(tables);
			printDDLs(ddls);
			// printDDLs(DDLFormatter.format(ddls));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception found: " + e.getMessage());
		}
	}

	protected static void printAllDialectsDDLs(TableModel... tables) {
		Dialect[] diaList = Dialect.values();
		for (Dialect dialect : diaList) {
			System.out.println("======" + dialect + "=====");
			try {
				String[] ddls = dialect.toDropAndCreateDDL(tables);
				printDDLs(ddls);
				// printDDLs(DDLFormatter.format(ddls));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Exception found: " + e.getMessage());
			}
		}
	}
}
