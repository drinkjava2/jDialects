/*
 * AllDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.coveragetest.alldialects;

import static com.github.drinkjava2.alldialects.Dialect.DB2400Dialect;
import static com.github.drinkjava2.alldialects.Dialect.DB2Dialect;
import static com.github.drinkjava2.alldialects.Dialect.DerbyDialect;
import static com.github.drinkjava2.alldialects.Dialect.DerbyTenFiveDialect;
import static com.github.drinkjava2.alldialects.Dialect.DerbyTenSevenDialect;
import static com.github.drinkjava2.alldialects.Dialect.DerbyTenSixDialect;
import static com.github.drinkjava2.alldialects.Dialect.H2Dialect;
import static com.github.drinkjava2.alldialects.Dialect.HSQLDialect;
import static com.github.drinkjava2.alldialects.Dialect.InformixDialect;
import static com.github.drinkjava2.alldialects.Dialect.IngresDialect;
import static com.github.drinkjava2.alldialects.Dialect.MySQL5Dialect;
import static com.github.drinkjava2.alldialects.Dialect.MySQLDialect;
import static com.github.drinkjava2.alldialects.Dialect.Oracle10gDialect;
import static com.github.drinkjava2.alldialects.Dialect.Oracle8iDialect;
import static com.github.drinkjava2.alldialects.Dialect.Oracle9iDialect;
import static com.github.drinkjava2.alldialects.Dialect.PostgreSQL81Dialect;
import static com.github.drinkjava2.alldialects.Dialect.PostgreSQL82Dialect;
import static com.github.drinkjava2.alldialects.Dialect.PostgreSQL9Dialect;
import static com.github.drinkjava2.alldialects.Dialect.PostgresPlusDialect;
import static com.github.drinkjava2.alldialects.Dialect.SQLServerDialect;
import static com.github.drinkjava2.alldialects.Dialect.SQLiteDialect;
import static com.github.drinkjava2.alldialects.Dialect.SybaseASE15Dialect;
import static com.github.drinkjava2.alldialects.Dialect.SybaseAnywhereDialect;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.alldialects.Dialect;
import com.github.drinkjava2.alldialects.DialectException;

/**
 * This is unit test for AllDialects
 * 
 * @author Yong Z.
 *
 */
public class DialectTest {

	// ========test pagination=========
	@Test
	public void selectTest() {
		String s = Dialect.MySQL5Dialect.paginate(2, 10, "select * from user where userid=1 order by id");
		System.out.println(s);
	}

	@Test(expected = DialectException.class)
	public void selectWrongTest() {
		System.out.println(Dialect.MySQL5Dialect.paginate(2, 10, "query from user"));
	}

	@Test(expected = DialectException.class)
	public void notSupportTest() {
		System.out.println(Dialect.Cache71Dialect.paginate(2, 10, "select * from user"));
	}

	@Test(expected = DialectException.class)
	public void selectMSSQLNotSupportPagination() {
		String s;
		s = Dialect.SQLServerDialect.paginate(2, 10, "select * from users a where id>'1' order by  id, a.username");
		System.out.println(s);
	}

	@Test(expected = DialectException.class)
	public void selectMSSQLNotSupportPagination2() {
		String s;
		s = Dialect.SQLServerDialect.paginate(1, 10, "select * from users a where id>'1' order by  id, a.username");
		System.out.println(s);
	}
	
	@Test 
	public void selectMSSQLTEMP() {
		String s;
		s = Dialect.SQLServer2005Dialect.paginate(3, 3, "select distinct a.id, a.userName, a.userName as u2 from usertemp a where id>'0' order by id, a.username");
		System.out.println(s); 
	}

	@Test
	public void selectMSSQLPagination() {
		String s;
		s = Dialect.SQLServer2005Dialect.paginate(1, 10, "select * from users a where id>'1' order by  id, a.username");
		System.out.println(s);
		s = Dialect.SQLServer2005Dialect.paginate(2, 10, "select * from users a where id>'1' order by  id, a.username");
		System.out.println(s);
		s = Dialect.SQLServer2008Dialect.paginate(1, 10, "select * from users a where id>'1' order by id, a.username");
		System.out.println(s);
		s = Dialect.SQLServer2008Dialect.paginate(2, 10, "select * from users a where id>'1' order by id, a.username");
		System.out.println(s);
		s = Dialect.SQLServer2012Dialect.paginate(1, 10, "select * from users a where id>'1' order by id, a.username");
		System.out.println(s);
		s = Dialect.SQLServer2012Dialect.paginate(2, 10, "select * from users a where id>'1' order by id, a.username");
		System.out.println(s);
	}

	@Test
	public void testGetTemplatesAndMappings() {
		System.out.println(Dialect.PostgresPlusDialect.getPaginSQLTemplate());
		System.out.println(Dialect.MySQL55Dialect.getPaginSQLTemplate());
		System.out.println(Dialect.MySQL55Dialect.getTypeMappings().size());
	}

	// =======test guess dialects=======
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
