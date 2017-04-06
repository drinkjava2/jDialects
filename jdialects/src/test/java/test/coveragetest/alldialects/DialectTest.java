/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.coveragetest.alldialects;

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

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;

/**
 * This is unit test for jDialects
 * 
 * @author Yong Z.
 *
 */
public class DialectTest {

	private static final String sql1 = "select distinct a.id, a.userName, a.userName as u2 from usertemp a where id>'0' order by id, a.username";

	@Test
	public void testPagination() {
		Dialect[] dialects = Dialect.values();
		for (Dialect dialect : dialects) {
			System.out.println("=========" + dialect + "==========");
			String result = "";
			try {
				result = dialect.paginate(1, 10, sql1);
				System.out.println(result);
			} catch (Exception e) {
				System.out.println("Error:"+e.getMessage());
			}
			Assert.assertFalse(result.contains("$") || result.contains("?"));
		}
	}

	// =======test guess dialects=======
	@SuppressWarnings("deprecation")
	@Test
	public void testGuessDialects() {
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

}
