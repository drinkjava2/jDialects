/*
 * HiDialect, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.codegenerator;

import org.hibernate.dialect.DB2400Dialect;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.DerbyDialect;
import org.hibernate.dialect.DerbyTenFiveDialect;
import org.hibernate.dialect.DerbyTenSevenDialect;
import org.hibernate.dialect.DerbyTenSixDialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.InformixDialect;
import org.hibernate.dialect.IngresDialect;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.Oracle9iDialect;
import org.hibernate.dialect.PostgreSQL81Dialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.dialect.PostgresPlusDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.SybaseASE15Dialect;
import org.hibernate.dialect.SybaseAnywhereDialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
import org.junit.Assert;
import org.junit.Test;

import test.TestBase;

/**
 * This is not a unit test class, it's a code generator tool to create source
 * code in Dialect.java
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class GuessDialectCodeGenerator extends TestBase {

	@Test
	public void testGuessDialects() {
		System.out.println("testDialectHelper=======================");
		DialectResolver resolver = StandardDialectResolver.INSTANCE;
		Assert.assertEquals(HSQLDialect.class,
				CodeGeneratorHelper.guessDialect("HSQL Database Engine", resolver).getClass());
		Assert.assertEquals(H2Dialect.class, CodeGeneratorHelper.guessDialect("H2", resolver).getClass());
		Assert.assertEquals(MySQLDialect.class, CodeGeneratorHelper.guessDialect("MySQL", resolver).getClass());
		Assert.assertEquals(MySQL5Dialect.class, CodeGeneratorHelper.guessDialect("MySQL", 5, 0, resolver).getClass());
		Assert.assertEquals(PostgreSQL81Dialect.class,
				CodeGeneratorHelper.guessDialect("PostgreSQL", resolver).getClass());
		Assert.assertEquals(PostgreSQL82Dialect.class,
				CodeGeneratorHelper.guessDialect("PostgreSQL", 8, 2, resolver).getClass());
		Assert.assertEquals(PostgreSQL9Dialect.class,
				CodeGeneratorHelper.guessDialect("PostgreSQL", 9, 0, resolver).getClass());
		Assert.assertEquals(PostgresPlusDialect.class,
				CodeGeneratorHelper.guessDialect("EnterpriseDB", 9, 2, resolver).getClass());
		Assert.assertEquals(DerbyDialect.class,
				CodeGeneratorHelper.guessDialect("Apache Derby", 10, 4, resolver).getClass());
		Assert.assertEquals(DerbyTenFiveDialect.class,
				CodeGeneratorHelper.guessDialect("Apache Derby", 10, 5, resolver).getClass());
		Assert.assertEquals(DerbyTenSixDialect.class,
				CodeGeneratorHelper.guessDialect("Apache Derby", 10, 6, resolver).getClass());
		Assert.assertEquals(DerbyTenSevenDialect.class,
				CodeGeneratorHelper.guessDialect("Apache Derby", 11, 5, resolver).getClass());
		Assert.assertEquals(IngresDialect.class, CodeGeneratorHelper.guessDialect("Ingres", resolver).getClass());
		Assert.assertEquals(IngresDialect.class, CodeGeneratorHelper.guessDialect("ingres", resolver).getClass());
		Assert.assertEquals(IngresDialect.class, CodeGeneratorHelper.guessDialect("INGRES", resolver).getClass());
		Assert.assertEquals(SQLServerDialect.class,
				CodeGeneratorHelper.guessDialect("Microsoft SQL Server Database", resolver).getClass());
		Assert.assertEquals(SQLServerDialect.class,
				CodeGeneratorHelper.guessDialect("Microsoft SQL Server", resolver).getClass());
		Assert.assertEquals(SybaseASE15Dialect.class,
				CodeGeneratorHelper.guessDialect("Sybase SQL Server", resolver).getClass());
		Assert.assertEquals(SybaseASE15Dialect.class,
				CodeGeneratorHelper.guessDialect("Adaptive Server Enterprise", resolver).getClass());
		Assert.assertEquals(SybaseAnywhereDialect.class,
				CodeGeneratorHelper.guessDialect("Adaptive Server Anywhere", resolver).getClass());
		Assert.assertEquals(InformixDialect.class,
				CodeGeneratorHelper.guessDialect("Informix Dynamic Server", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, CodeGeneratorHelper.guessDialect("DB2/NT", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, CodeGeneratorHelper.guessDialect("DB2/LINUX", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, CodeGeneratorHelper.guessDialect("DB2/6000", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, CodeGeneratorHelper.guessDialect("DB2/HPUX", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, CodeGeneratorHelper.guessDialect("DB2/SUN", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, CodeGeneratorHelper.guessDialect("DB2/LINUX390", resolver).getClass());
		Assert.assertEquals(DB2Dialect.class, CodeGeneratorHelper.guessDialect("DB2/AIX64", resolver).getClass());
		Assert.assertEquals(DB2400Dialect.class,
				CodeGeneratorHelper.guessDialect("DB2 UDB for AS/400", resolver).getClass());
		Assert.assertEquals(Oracle8iDialect.class, CodeGeneratorHelper.guessDialect("Oracle", 8, resolver).getClass());
		Assert.assertEquals(Oracle9iDialect.class, CodeGeneratorHelper.guessDialect("Oracle", 9, resolver).getClass());
		Assert.assertEquals(Oracle10gDialect.class,
				CodeGeneratorHelper.guessDialect("Oracle", 10, resolver).getClass());
		Assert.assertEquals(Oracle10gDialect.class,
				CodeGeneratorHelper.guessDialect("Oracle", 11, resolver).getClass());
	}

}