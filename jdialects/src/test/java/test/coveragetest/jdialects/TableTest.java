/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.coveragetest.jdialects;

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
	private static void printDDLs(String[] ddl) {
		for (String str : ddl) {
			System.out.println(str);
		}
	}

	private static Table aNormalModel() {// A normal setting
		Table t = new Table("testTable");
		t.addColumn("b1").BOOLEAN();
		t.addColumn("d2").DOUBLE();
		t.addColumn("f3").FLOAT(5);
		t.addColumn("i4").INTEGER().pkey();
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
	public void testANormalModel() {
		Dialect[] diaList = Dialect.values();
		for (Dialect dialect : diaList) {
			System.out.println("======" + dialect + "=====");
			String[] ddl = aNormalModel().toCreateTableDDL(dialect, true);
			printDDLs(ddl);
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
		String[] ddl = testNoPkeyModel().toCreateTableDDL(Dialect.Teradata14Dialect);
		printDDLs(ddl);
	}

	private static Table testCompoundPkeyModel() {// Compound PKEY
		Table t = new Table("testTable");
		t.addColumn("i4").INTEGER().pkey().unique().autoInc().notNull().defaultValue(1);
		t.addColumn("l5").LONG().pkey();
		t.addColumn("s6").SHORT();
		return t;
	}

	@Test
	public void testCompondPkey() {
		Dialect[] diaList = Dialect.values();
		for (Dialect dialect : diaList) {
			System.out.println("======" + dialect + "=====");
			String[] ddl = testCompoundPkeyModel().toCreateTableDDL(dialect);
			printDDLs(ddl);
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
			String[] ddl = testNotNullModel().toCreateTableDDL(dialect);
			printDDLs(ddl);
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
			String[] ddl = allowNullModel().toCreateTableDDL(dialect, true);
			printDDLs(ddl);
		}
	}

	private static Table uniqueModel() {// unique
		Table t = new Table("testTable");
		t.addColumn("s1").STRING(20).unique().notNull();
		t.addColumn("s2").STRING(20).unique();
		t.addColumn("s3").STRING(20).unique("uname1").notNull();
		t.addColumn("s4").STRING(20).unique("uname2");
		return t;
	}

	@Test
	public void testUnique() {
		Dialect[] diaList = Dialect.values();
		for (Dialect dialect : diaList) {
			System.out.println("======" + dialect + "=====");
			String[] ddl = uniqueModel().toCreateTableDDL(dialect, true);
			printDDLs(ddl);
		}
	}

	private static Table checkModel() {// check
		Table t = new Table("testTable");
		t.addColumn("s1").STRING(20).unique().notNull().check("s1>5");
		t.addColumn("s2").STRING(20).unique().check("s2>5");
		t.addColumn("s3").STRING(20).unique("uname1").notNull().check("s3>5");
		t.addColumn("s4").STRING(20).unique("uname2").check("s4>5");
		return t;
	}

	@Test
	public void testCheck() {
		Dialect[] diaList = Dialect.values();
		for (Dialect dialect : diaList) {
			System.out.println("======" + dialect + "=====");
			String[] ddl = checkModel().toCreateTableDDL(dialect, true);
			printDDLs(ddl);
		}
	}

	private static Table tableCheckModel() {// table check
		Table t = new Table("testTable");
		t.check("s2>10");
		t.addColumn("s1").STRING(20).unique().notNull().check("s1>5");
		t.addColumn("s2").STRING(20);
		return t;
	}

	@Test
	public void testTableCheck() {
		Dialect[] diaList = Dialect.values();
		for (Dialect dialect : diaList) {
			System.out.println("======" + dialect + "=====");
			String[] ddl = tableCheckModel().toCreateTableDDL(dialect, true);
			printDDLs(ddl);
		}
	}

}