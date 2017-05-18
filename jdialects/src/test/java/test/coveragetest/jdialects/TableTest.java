/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.coveragetest.jdialects;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.Table;

/**
 * This is unit test for Table
 * 
 * @author Yong Z.
 * @since 1.0.2
 */
public class TableTest {
	private static Table testCreateSQLModel() {// First CONIFG
		Table t = new Table("testTable");
		t.addColumn("b1").BOOLEAN();
		t.addColumn("d2").DOUBLE();
		t.addColumn("f3").FLOAT(5);
		t.addColumn("i4").INTEGER().pkey().unique().autoInc().notNull().defaultValue(1);
		t.addColumn("l5").LONG().pkey();
		t.addColumn("s6").SHORT();
		t.addColumn("b7").BIGDECIMAL(10, 2);
		t.addColumn("s8").STRING(20);
		t.addColumn("d9").DATE();
		t.addColumn("t10").TIME();
		t.addColumn("t11").TIMESTAMP();
		t.addColumn("v12").VARCHAR(300);
		return t;
	}

	@Test
	public void testCreateSQL() {
		Dialect[] diaList = Dialect.values();
		for (Dialect dialect : diaList) {
			System.out.println("***" + dialect + "***");
			String ddl = testCreateSQLModel().toCreateTableDDL(dialect, true);
			System.out.println(ddl);
		}
	}

	private static Table testNoPkeyModel() {// NO Prime Key
		Table t = new Table("testTable");
		t.addColumn("i4").INTEGER();
		t.addColumn("l5").LONG();
		return t;
	}

	@Test
	public void testNoPkey() {
		String ddl = testNoPkeyModel().toCreateTableDDL(Dialect.Teradata14Dialect);
		System.out.println(ddl);
		Assert.assertTrue(StringUtils.contains(ddl, "create multiset table"));
	}

	private static Table testCompoundPkeyModel() {// Compound PKEY
		Table t = new Table("testTable");
		t.addColumn("b1").BOOLEAN();
		t.addColumn("d2").DOUBLE();
		t.addColumn("f3").FLOAT(5);
		t.addColumn("i4").INTEGER().pkey().unique().autoInc().notNull().defaultValue(1);
		t.addColumn("l5").LONG().pkey();
		t.addColumn("s6").SHORT();
		t.addColumn("b7").BIGDECIMAL(10, 2);
		t.addColumn("s8").STRING(20);
		t.addColumn("d9").DATE();
		t.addColumn("t10").TIME();
		t.addColumn("t11").TIMESTAMP();
		t.addColumn("v12").VARCHAR(300);
		return t;
	}

	@Test
	public void testCompondPkey() {
		Dialect[] diaList = Dialect.values();
		for (Dialect dialect : diaList) {
			System.out.println("======" + dialect + "=====");
			String ddl = testCompoundPkeyModel().toCreateTableDDL(dialect);
			System.out.println(ddl);
		}
	}

	private static Table testNotNullModel() {// Not Null
		Table t = new Table("testTable");
		t.addColumn("b1").BOOLEAN().notNull();
		t.addColumn("d2").DOUBLE().notNull();
		t.addColumn("f3").FLOAT(5).notNull();
		t.addColumn("i4").INTEGER().notNull();
		t.addColumn("l5").LONG().notNull();
		t.addColumn("s6").SHORT().notNull();
		t.addColumn("b7").BIGDECIMAL(10, 2).notNull();
		t.addColumn("s8").STRING(20).notNull();
		t.addColumn("d9").DATE().notNull();
		t.addColumn("t10").TIME().notNull();
		t.addColumn("t11").TIMESTAMP().notNull();
		t.addColumn("v12").VARCHAR(300).notNull();
		return t;
	}

	@Test
	public void testNotNull() {
		Dialect[] diaList = Dialect.values();
		for (Dialect dialect : diaList) {
			System.out.println("======" + dialect + "=====");
			String ddl = testNotNullModel().toCreateTableDDL(dialect);
			System.out.println(ddl);
		}
	}

	private static Table allowNullModel() {// Allow Null
		Table t = new Table("testTable");
		t.addColumn("b1").BOOLEAN();
		t.addColumn("d2").DOUBLE();
		t.addColumn("f3").FLOAT(5);
		t.addColumn("i4").INTEGER();
		t.addColumn("l5").LONG();
		t.addColumn("s6").SHORT();
		t.addColumn("b7").BIGDECIMAL(10, 2);
		t.addColumn("s8").STRING(20);
		t.addColumn("d9").DATE();
		t.addColumn("t10").TIME();
		t.addColumn("t11").TIMESTAMP();
		t.addColumn("v12").VARCHAR(300);
		return t;
	}

	@Test
	public void testAllowNull() {
		Dialect[] diaList = Dialect.values();
		for (Dialect dialect : diaList) {
			System.out.println("======" + dialect + "=====");
			String ddl = allowNullModel().toCreateTableDDL(dialect,true);
			System.out.println(ddl);
		}
	}

}