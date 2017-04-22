/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.coveragetest.jdialects;

import static com.github.drinkjava2.jdialects.Dialect.DB2400Dialect;
import static com.github.drinkjava2.jdialects.Dialect.DB2Dialect;
import static com.github.drinkjava2.jdialects.Dialect.DerbyDialect;
import static com.github.drinkjava2.jdialects.Dialect.DerbyTenFiveDialect;
import static com.github.drinkjava2.jdialects.Dialect.DerbyTenSevenDialect;
import static com.github.drinkjava2.jdialects.Dialect.DerbyTenSixDialect;
import static com.github.drinkjava2.jdialects.Dialect.H2Dialect;
import static com.github.drinkjava2.jdialects.Dialect.HSQLDialect;
import static com.github.drinkjava2.jdialects.Dialect.InformixDialect;
import static com.github.drinkjava2.jdialects.Dialect.IngresDialect;
import static com.github.drinkjava2.jdialects.Dialect.MySQL5Dialect;
import static com.github.drinkjava2.jdialects.Dialect.MySQLDialect;
import static com.github.drinkjava2.jdialects.Dialect.Oracle10gDialect;
import static com.github.drinkjava2.jdialects.Dialect.Oracle8iDialect;
import static com.github.drinkjava2.jdialects.Dialect.Oracle9iDialect;
import static com.github.drinkjava2.jdialects.Dialect.PostgreSQL81Dialect;
import static com.github.drinkjava2.jdialects.Dialect.PostgreSQL82Dialect;
import static com.github.drinkjava2.jdialects.Dialect.PostgreSQL9Dialect;
import static com.github.drinkjava2.jdialects.Dialect.PostgresPlusDialect;
import static com.github.drinkjava2.jdialects.Dialect.SQLServerDialect;
import static com.github.drinkjava2.jdialects.Dialect.SQLiteDialect;
import static com.github.drinkjava2.jdialects.Dialect.SybaseASE15Dialect;
import static com.github.drinkjava2.jdialects.Dialect.SybaseAnywhereDialect;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.StrUtils;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This is unit test for jDialects.Dialect
 * 
 * @author Yong Z.
 *
 */
public class DialectTest {

	private static final String sql1 = "select distinct a.id, a.userName, a.userName as u2 from usertemp a where id>1 order by id, a.username";
	private static final String sql2 = "select * from users";
	private static final String sql3 = "select a.id, a.userName, a.userName as u2, b.c1 from usertemp a where id>? group by b.b1 order by id, a.username";
	private static final String sql4 = "select distinct top(?) * from users";

	@Test
	public void testPagination() {
		Dialect[] dialects = Dialect.values();
		for (Dialect dialect : dialects) {
			System.out.println("=========" + dialect + "==========");
			String result = "";
			try {
				result = dialect.paginate(1, 10, sql1);
				System.out.println(result);
			} catch (DialectException e) {
				System.out.println("Error:" + e.getMessage());
			}
			Assert.assertFalse(result.contains("$"));
			try {
				result = dialect.paginate(3, 10, sql1);
				System.out.println(result);
			} catch (DialectException e) {
				System.out.println("Error:" + e.getMessage());
			}
			Assert.assertFalse(result.contains("$"));
			try {
				result = dialect.paginate(1, 10, sql2);
				System.out.println(result);
			} catch (DialectException e) {
				System.out.println("Error:" + e.getMessage());
			}
			Assert.assertFalse(result.contains("$"));
			try {
				result = dialect.paginate(3, 10, sql2);
				System.out.println(result);
			} catch (DialectException e) {
				System.out.println("Error:" + e.getMessage());
			}
			Assert.assertFalse(result.contains("$"));
		}
	}

	@Test
	public void testPagination2() {
		Dialect[] dialects = Dialect.values();
		for (Dialect dialect : dialects) {
			System.out.println("=========" + dialect + "==========");
			String result = "";
			try {
				result = dialect.paginate(1, 10, sql3);
				System.out.println(result);
			} catch (DialectException e) {
				System.out.println("Error:" + e.getMessage());
			}
			Assert.assertFalse(result.contains("$"));
			Assert.assertTrue(StrUtils.isEmpty(result) || 1 == StrUtils.countMatches(result, '?'));
			try {
				result = dialect.paginate(3, 10, sql4);
				System.out.println(result);
			} catch (DialectException e) {
				System.out.println("Error:" + e.getMessage());
			}
			Assert.assertFalse(result.contains("$"));
			Assert.assertTrue(StrUtils.isEmpty(result) || 1 == StrUtils.countMatches(result, '?'));
			try {
				result = dialect.paginate(1, 10, sql3);
				System.out.println(result);
			} catch (DialectException e) {
				System.out.println("Error:" + e.getMessage());
			}
			Assert.assertFalse(result.contains("$"));
			Assert.assertTrue(StrUtils.isEmpty(result) || 1 == StrUtils.countMatches(result, '?'));
			try {
				result = dialect.paginate(3, 10, sql4);
				System.out.println(result);
			} catch (DialectException e) {
				System.out.println("Error:" + e.getMessage());
			}
			Assert.assertFalse(result.contains("$"));
			Assert.assertTrue(StrUtils.isEmpty(result) || 1 == StrUtils.countMatches(result, '?'));
		}
	}

	// =======test guess dialects=======

	private HikariDataSource buildH2Datasource() {
		HikariDataSource ds = new HikariDataSource();
		ds.addDataSourceProperty("cachePrepStmts", true);
		ds.addDataSourceProperty("prepStmtCacheSize", 250);
		ds.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
		ds.addDataSourceProperty("useServerPrepStmts", true);
		ds.setMaximumPoolSize(3);
		ds.setConnectionTimeout(5000);
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setDriverClassName("org.h2.Driver");
		ds.setUsername("sa");
		ds.setPassword("");
		return ds;
	}

	@Test
	public void testGuessDialectsByDatasource() {
		HikariDataSource ds = buildH2Datasource();
		String dialectName = Dialect.guessDialect(ds).toString();
		Assert.assertEquals("H2Dialect", dialectName);
		ds.close();
	}

	@Test
	public void testGuessDialectsByConnection() {
		HikariDataSource ds = buildH2Datasource();
		String dialectName = null;
		Connection con = null;
		try {
			con = ds.getConnection();
			dialectName = Dialect.guessDialect(con).toString();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		Assert.assertEquals("H2Dialect", dialectName);
		ds.close();
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testGuessDialectsByDatabaseName() {
		Assert.assertEquals(SQLiteDialect, Dialect.guessDialect("SQLite"));
		Assert.assertEquals(HSQLDialect, Dialect.guessDialect("HSQL Database Engine"));
		Assert.assertEquals(H2Dialect, Dialect.guessDialect("H2"));
		Assert.assertEquals(MySQLDialect, Dialect.guessDialect("MySQL"));
		Assert.assertEquals(MySQL5Dialect, Dialect.guessDialect("MySQL", 5, 0));
		Assert.assertEquals(PostgreSQL81Dialect, Dialect.guessDialect("PostgreSQL"));
		Assert.assertEquals(PostgreSQL82Dialect, Dialect.guessDialect("PostgreSQL", 8, 2));
		Assert.assertEquals(PostgreSQL9Dialect, Dialect.guessDialect("PostgreSQL", 9, 0));
		Assert.assertEquals(PostgresPlusDialect, Dialect.guessDialect("EnterpriseDB", 9, 2));
		Assert.assertEquals(DerbyDialect, Dialect.guessDialect("Apache Derby", 10, 4));
		Assert.assertEquals(DerbyTenFiveDialect, Dialect.guessDialect("Apache Derby", 10, 5));
		Assert.assertEquals(DerbyTenSixDialect, Dialect.guessDialect("Apache Derby", 10, 6));
		Assert.assertEquals(DerbyTenSevenDialect, Dialect.guessDialect("Apache Derby", 11, 5));
		Assert.assertEquals(IngresDialect, Dialect.guessDialect("Ingres"));
		Assert.assertEquals(IngresDialect, Dialect.guessDialect("ingres"));
		Assert.assertEquals(IngresDialect, Dialect.guessDialect("INGRES"));
		Assert.assertEquals(SQLServerDialect, Dialect.guessDialect("Microsoft SQL Server Database"));
		Assert.assertEquals(SQLServerDialect, Dialect.guessDialect("Microsoft SQL Server"));
		Assert.assertEquals(SybaseASE15Dialect, Dialect.guessDialect("Sybase SQL Server"));
		Assert.assertEquals(SybaseASE15Dialect, Dialect.guessDialect("Adaptive Server Enterprise"));
		Assert.assertEquals(SybaseAnywhereDialect, Dialect.guessDialect("Adaptive Server Anywhere"));
		Assert.assertEquals(InformixDialect, Dialect.guessDialect("Informix Dynamic Server"));
		Assert.assertEquals(DB2Dialect, Dialect.guessDialect("DB2/NT"));
		Assert.assertEquals(DB2Dialect, Dialect.guessDialect("DB2/LINUX"));
		Assert.assertEquals(DB2Dialect, Dialect.guessDialect("DB2/6000"));
		Assert.assertEquals(DB2Dialect, Dialect.guessDialect("DB2/HPUX"));
		Assert.assertEquals(DB2Dialect, Dialect.guessDialect("DB2/SUN"));
		Assert.assertEquals(DB2Dialect, Dialect.guessDialect("DB2/LINUX390"));
		Assert.assertEquals(DB2Dialect, Dialect.guessDialect("DB2/AIX64"));
		Assert.assertEquals(DB2400Dialect, Dialect.guessDialect("DB2 UDB for AS/400"));
		Assert.assertEquals(Oracle8iDialect, Dialect.guessDialect("Oracle", 8));
		Assert.assertEquals(Oracle9iDialect, Dialect.guessDialect("Oracle", 9));
		Assert.assertEquals(Oracle10gDialect, Dialect.guessDialect("Oracle", 10));
		Assert.assertEquals(Oracle10gDialect, Dialect.guessDialect("Oracle", 11));
	}

	// =======test DDL Type Mapping method=======
	@Test
	public void testDDLTypeMapping() {
		Dialect d = Dialect.PostgreSQL81Dialect;
		String ddlSql = "create table ddl_test("//
				+ "f1 " + d.BIGINT() //
				+ ",f3 " + d.BIT() //
				+ ",f4 " + d.BLOB() //
				+ ",f5 " + d.BOOLEAN() //
				+ ",f6 " + d.CHAR() //
				+ ")" + d.ENGINE(" DEFAULT CHARSET=utf8");
		System.out.println(ddlSql);
		d = Dialect.MySQL5InnoDBDialect;
		String ddl = "create table ddl_test("//
				+ "f1 " + d.BIGINT() //
				+ ",f2 " + d.BINARY(5) //
				+ ",f3 " + d.BIT() //
				+ ",f4 " + d.BLOB() //
				+ ",f5 " + d.BOOLEAN() //
				+ ",f6 " + d.CHAR() //
				+ ",f7 " + d.CLOB() //
				+ ",f8 " + d.DATE() //
				// + ",f9 " + d.DECIMAL(3,5) //
				+ ",f10 " + d.DOUBLE() //
				+ ",f11 " + d.FLOAT() //
				+ ",f12 " + d.INTEGER() //
				// + ",f13 " + d.JAVA_OBJECT() //
				+ ",f14 " + d.LONGNVARCHAR(10) //
				+ ",f15 " + d.LONGVARBINARY() //
				+ ",f16 " + d.LONGVARCHAR() //
				+ ",f17 " + d.NCHAR(5) //
				+ ",f18 " + d.NCLOB() //
				+ ",f19 " + d.NUMERIC(6, 4) //
				+ ",f20 " + d.NVARCHAR(6) //
				// + ",f21 " + d.OTHER() //
				+ ",f22 " + d.REAL() //
				+ ",f23 " + d.SMALLINT() //
				+ ",f24 " + d.TIME() //
				+ ",f25 " + d.TIMESTAMP() //
				+ ",f26 " + d.TINYINT() //
				+ ",f27 " + d.VARBINARY() //
				+ ",f28 " + d.VARCHAR() //
				+ ")" + d.ENGINE(" DEFAULT CHARSET=utf8");
		System.out.println(ddl);
		d = Dialect.Oracle10gDialect;
		ddl = "create table ddl_test("//
				+ "f1 " + d.BIGINT() //
				+ ",f2 " + d.BINARY(5) //
				+ ",f3 " + d.BIT() //
				+ ",f4 " + d.BLOB() //
				+ ",f5 " + d.BOOLEAN() //
				+ ",f6 " + d.CHAR() //
				+ ",f7 " + d.CLOB() //
				+ ",f8 " + d.DATE() //
				+ ",f9 " + d.DECIMAL(3, 5) //
				+ ",f10 " + d.DOUBLE() //
				+ ",f11 " + d.FLOAT() //
				+ ",f12 " + d.INTEGER() //
				// + ",f13 " + d.JAVA_OBJECT() //
				+ ",f14 " + d.LONGNVARCHAR(10) //
				+ ",f15 " + d.LONGVARBINARY() //
				+ ",f16 " + d.LONGVARCHAR() //
				+ ",f17 " + d.NCHAR(5) //
				+ ",f18 " + d.NCLOB() //
				+ ",f19 " + d.NUMERIC(6, 4) //
				+ ",f20 " + d.NVARCHAR(6) //
				// + ",f21 " + d.OTHER() //
				+ ",f22 " + d.REAL() //
				+ ",f23 " + d.SMALLINT() //
				+ ",f24 " + d.TIME() //
				+ ",f25 " + d.TIMESTAMP() //
				+ ",f26 " + d.TINYINT() //
				+ ",f27 " + d.VARBINARY() //
				+ ",f28 " + d.VARCHAR() //
				+ ")" + d.ENGINE();
		System.out.println(ddl);
	}

	@Test
	public void dialectFamilyTest() {
		Assert.assertTrue(Dialect.DB2400Dialect.isDB2Family());
		Assert.assertFalse(Dialect.Oracle10gDialect.isDB2Family());

		Assert.assertTrue(Dialect.DerbyTenFiveDialect.isDerbyFamily());
		Assert.assertFalse(Dialect.Oracle10gDialect.isDerbyFamily());

		Assert.assertTrue(Dialect.H2Dialect.isH2Family());
		Assert.assertFalse(Dialect.Oracle10gDialect.isH2Family());

		Assert.assertTrue(Dialect.MySQL5InnoDBDialect.isMySqlFamily());
		Assert.assertFalse(Dialect.Oracle10gDialect.isMySqlFamily());

		Assert.assertTrue(Dialect.Oracle8iDialect.isOracleFamily());
		Assert.assertFalse(Dialect.SQLiteDialect.isOracleFamily());

		Assert.assertTrue(Dialect.PostgresPlusDialect.isPostgresFamily());
		Assert.assertFalse(Dialect.Oracle10gDialect.isPostgresFamily());

		Assert.assertTrue(Dialect.SQLServer2005Dialect.isSQLServerFamily());
		Assert.assertFalse(Dialect.Oracle10gDialect.isSQLServerFamily());

		Assert.assertTrue(Dialect.SybaseAnywhereDialect.isSybaseFamily());
		Assert.assertFalse(Dialect.Oracle10gDialect.isSybaseFamily());

	}
}
