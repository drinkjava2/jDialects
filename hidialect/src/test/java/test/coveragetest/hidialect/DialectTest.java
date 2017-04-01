/*
 * HiDialect, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.coveragetest.hidialect;

import static com.github.drinkjava2.hidialect.Dialect.*;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.hidialect.Dialect;
import com.github.drinkjava2.hidialect.DialectException;

/**
 * This is unit test for HiDialect
 * 
 * @author Yong Z.
 *
 */
public class DialectTest {

	// ========test pagination=========
	@Test
	public void selectTest() {
		System.out.println(Dialect.MySQL5.pagin(1, 10, "select * from user where userid=1 order by id"));
		System.out.println(Dialect.pagin(Dialect.MySQL57, 1, 10, "select * from user where userid=1 order by id"));
	}

	@Test(expected = DialectException.class)
	public void selectWrongTest() {
		System.out.println(Dialect.MySQL5.pagin(1, 10, "query from user"));
	}

	@Test(expected = DialectException.class)
	public void notSupportTest() {
		System.out.println(Dialect.Cache71.pagin(1, 10, "select * from user"));
	}

	@Test(expected = DialectException.class)
	public void nullTest() {
		System.out.println(Dialect.pagin(null, 1, 10, "select * from user where userid=1 order by id"));
	}

	// =======test guess dialects=======
	@Test
	public void testGuessDialects() {
		System.out.println("=====testGuessDialects========== ");
		Assert.assertEquals(SQLite, Dialect.guessDialect("SQLite"));
		Assert.assertEquals(HSQL, Dialect.guessDialect("HSQL Database Engine"));
		Assert.assertEquals(H2, Dialect.guessDialect("H2"));
		Assert.assertEquals(MySQL, Dialect.guessDialect("MySQL"));
		Assert.assertEquals(MySQL5, Dialect.guessDialect("MySQL", 5, 0));
		Assert.assertEquals(PostgreSQL81, Dialect.guessDialect("PostgreSQL"));
		Assert.assertEquals(PostgreSQL82, Dialect.guessDialect("PostgreSQL", 8, 2));
		Assert.assertEquals(PostgreSQL9, Dialect.guessDialect("PostgreSQL", 9, 0));
		Assert.assertEquals(PostgresPlus, Dialect.guessDialect("EnterpriseDB", 9, 2));
		Assert.assertEquals(Derby, Dialect.guessDialect("Apache Derby", 10, 4));
		Assert.assertEquals(DerbyTenFive, Dialect.guessDialect("Apache Derby", 10, 5));
		Assert.assertEquals(DerbyTenSix, Dialect.guessDialect("Apache Derby", 10, 6));
		Assert.assertEquals(DerbyTenSeven, Dialect.guessDialect("Apache Derby", 11, 5));
		Assert.assertEquals(Ingres, Dialect.guessDialect("Ingres"));
		Assert.assertEquals(Ingres, Dialect.guessDialect("ingres"));
		Assert.assertEquals(Ingres, Dialect.guessDialect("INGRES"));
		Assert.assertEquals(SQLServer, Dialect.guessDialect("Microsoft SQL Server Database"));
		Assert.assertEquals(SQLServer, Dialect.guessDialect("Microsoft SQL Server"));
		Assert.assertEquals(SybaseASE15, Dialect.guessDialect("Sybase SQL Server"));
		Assert.assertEquals(SybaseASE15, Dialect.guessDialect("Adaptive Server Enterprise"));
		Assert.assertEquals(SybaseAnywhere, Dialect.guessDialect("Adaptive Server Anywhere"));
		Assert.assertEquals(Informix, Dialect.guessDialect("Informix Dynamic Server"));
		Assert.assertEquals(DB2, Dialect.guessDialect("DB2/NT"));
		Assert.assertEquals(DB2, Dialect.guessDialect("DB2/LINUX"));
		Assert.assertEquals(DB2, Dialect.guessDialect("DB2/6000"));
		Assert.assertEquals(DB2, Dialect.guessDialect("DB2/HPUX"));
		Assert.assertEquals(DB2, Dialect.guessDialect("DB2/SUN"));
		Assert.assertEquals(DB2, Dialect.guessDialect("DB2/LINUX390"));
		Assert.assertEquals(DB2, Dialect.guessDialect("DB2/AIX64"));
		Assert.assertEquals(DB2400, Dialect.guessDialect("DB2 UDB for AS/400"));
		Assert.assertEquals(Oracle8i, Dialect.guessDialect("Oracle", 8));
		Assert.assertEquals(Oracle9i, Dialect.guessDialect("Oracle", 9));
		Assert.assertEquals(Oracle10g, Dialect.guessDialect("Oracle", 10));
		Assert.assertEquals(Oracle10g, Dialect.guessDialect("Oracle", 11));
	}

}
