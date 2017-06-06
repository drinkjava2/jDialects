/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.coveragetest.jdialects;

import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.Column;
import com.github.drinkjava2.jdialects.model.Table;

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

	private static void printOneDialectsDDLs(Dialect dialect, Table... tables) {
		System.out.println("======" + dialect + "=====");
		try {
			String[] ddl = dialect.toCreateDDL(tables);
			printDDLs(ddl);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception found: " + e.getMessage());
		}
	}

	private static void printAllDialectsDDLs(Table... tables) {
		Dialect[] diaList = Dialect.values();
		for (Dialect dialect : diaList) {
			System.out.println("======" + dialect + "=====");
			try {
				String[] ddl = dialect.toCreateDDL(tables);
				printDDLs(ddl);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Exception found: " + e.getMessage());
			}
		}
	}

	@Test
	public void testANormalModel() {// A normal setting
		Table t = new Table("testTable");
		t.column("b1").BOOLEAN();
		t.column("d2").DOUBLE();
		t.column("f3").FLOAT(5);
		t.column("i4").INTEGER().pkey();
		t.column("l5").LONG();
		t.column("s6").SHORT();
		t.column("b7").BIGDECIMAL(10, 2);
		t.column("s8").STRING(20);
		t.column("d9").DATE();
		t.column("t10").TIME();
		t.column("t11").TIMESTAMP();
		t.column("v12").VARCHAR(300);
		printAllDialectsDDLs(t);
	}

	@Test
	public void testNoPkey() {// Test no Prime Key
		// append() is a linked method
		Table table = new Table("tb").append(new Column("field1").INTEGER()).append(new Column("field2").LONG());
		printAllDialectsDDLs(table);
	}

	@Test
	public void testCompondPkey() {// Compound PKEY
		Table t = new Table("testTable");
		t.column("i4").INTEGER().pkey().unique().notNull().defaultValue("1");
		t.column("l5").LONG().pkey();
		t.column("s6").SHORT();
		printAllDialectsDDLs(t);
	}

	private static Table testNotNullModel() {// Not Null
		Table t = new Table("testTable");
		t.column("b1").BOOLEAN().notNull();
		t.column("d2").DOUBLE().notNull();
		t.column("f3").FLOAT(5).notNull();
		t.column("i4").INTEGER().notNull();
		t.column("l5").LONG().notNull();
		t.column("s6").SHORT().notNull();
		t.column("b7").BIGDECIMAL(10, 2).notNull();
		t.column("s8").STRING(20).notNull();
		t.column("d9").DATE().notNull();
		t.column("t10").TIME().notNull();
		t.column("t11").TIMESTAMP().notNull();
		t.column("v12").VARCHAR(300).notNull();
		return t;
	}

	@Test
	public void testNotNull() {
		printAllDialectsDDLs(testNotNullModel());
	}

	private static Table allowNullModel() {// Allow Null
		Table t = new Table("testTable");
		t.column("b1").BOOLEAN();
		t.column("d2").DOUBLE();
		t.column("f3").FLOAT(5);
		t.column("i4").INTEGER();
		t.column("l5").LONG();
		t.column("s6").SHORT();
		t.column("b7").BIGDECIMAL(10, 2);
		t.column("s8").STRING(20);
		t.column("d9").DATE();
		t.column("t10").TIME();
		t.column("t11").TIMESTAMP();
		t.column("v12").VARCHAR(300);
		return t;
	}

	@Test
	public void testAllowNull() {
		printAllDialectsDDLs(allowNullModel());
	}

	private static Table uniqueModel() {// unique
		Table t = new Table("testTable");
		t.column("s1").STRING(20).unique().notNull();
		t.column("s2").STRING(20).unique();
		t.column("s3").STRING(20).unique("uname1").notNull();
		t.column("s4").STRING(20).unique("uname2");
		return t;
	}

	@Test
	public void testUnique() {
		printAllDialectsDDLs(uniqueModel());
	}

	private static Table checkModel() {// column check
		Table t = new Table("testTable");
		t.column("s1").STRING(20).unique().notNull().check("s1>5");
		t.column("s2").STRING(20).unique().check("s2>5");
		t.column("s3").STRING(20).unique("uname1").notNull().check("s3>5");
		t.column("s4").STRING(20).unique("uname2").check("s4>5");
		return t;
	}

	@Test
	public void testCheck() {
		printAllDialectsDDLs(checkModel());
	}

	private static Table tableCheckModel() {// table check
		Table t = new Table("testTable");
		t.check("s2>10");
		t.column("s1").STRING(20).unique().notNull();
		t.column("s2").STRING(20);
		return t;
	}

	@Test
	public void testTableCheck() {
		printAllDialectsDDLs(tableCheckModel());
	}

	private static Table IdentityModel() {// Identity
		Table t = new Table("testTable");
		t.check("s2>10");
		t.column("s1").INTEGER().unique().notNull().identity().pkey();
		t.column("s2").LONG().check("s2>10");
		t.column("s3").BIGINT();
		return t;
	}

	@Test
	public void testIdentity() {
		printAllDialectsDDLs(IdentityModel());
	}

	@Test
	public void testIdentity2() {
		printOneDialectsDDLs(Dialect.SybaseASE15Dialect, IdentityModel());
		printOneDialectsDDLs(Dialect.MySQL55Dialect, IdentityModel());
		printOneDialectsDDLs(Dialect.InformixDialect, IdentityModel());
	}

	private static Table CommentModel() {// Comment
		Table t = new Table("testTable").comment("table_comment");
		t.column("s1").INTEGER().unique().notNull().identity().pkey();
		t.column("s2").LONG().comment("column_comment1");
		t.column("s3").BIGINT().comment("column_comment2");
		return t;
	}

	@Test
	public void testComment() {
		printAllDialectsDDLs(CommentModel());
	}

	@Test
	public void testComment2() {
		printOneDialectsDDLs(Dialect.Ingres10Dialect, CommentModel());
		printOneDialectsDDLs(Dialect.DB2Dialect, CommentModel());
		printOneDialectsDDLs(Dialect.MariaDBDialect, CommentModel());
		printOneDialectsDDLs(Dialect.SQLServer2012Dialect, CommentModel());
		printOneDialectsDDLs(Dialect.MySQL55Dialect, CommentModel());
	}

	private static Table SequenceModel() {// Sequence
		Table t = new Table("testTable");
		t.addSequence("seq1", "seq_1", 1, 1);
		t.addSequence("seq2", "seq_2", 1, 1);
		t.column("i1").INTEGER().pkey().sequence("seq1");
		t.column("i2").INTEGER().pkey().sequence("seq2");
		return t;
	}

	@Test
	public void testSequence() {
		printAllDialectsDDLs(SequenceModel());
	}

	private static Table tableGeneratorModel() {// tableGenerator
		Table t = new Table("testTable");
		t.addTableGenerator("tbgen1", "tb1", "pkcol", "valcol", "pkval", 1, 10);
		t.addTableGenerator("tbgen2", "tb1", "pkcol2", "valcol", "pkval", 1, 10);
		t.column("i1").INTEGER().pkey().tableGenerator("tbgen1");
		t.column("i2").INTEGER().pkey().tableGenerator("tbgen2");
		return t;
	}

	private static Table tableGeneratorModel2() {// tableGenerator
		Table t = new Table("testTable2");
		t.addTableGenerator("tbgen3", "tb1", "pkcol3", "valcol", "pkval", 1, 10);
		t.addTableGenerator("tbgen4", "tb1", "pkcol3", "valcol", "pkval2", 1, 10);
		t.addTableGenerator("tbgen5", "tb1", "pkcol4", "valcol", "pkval3", 1, 10);
		t.addTableGenerator("tbgen6", "tb1", "pkcol4", "valcol", "pkval4", 1, 10);
		t.column("i1").INTEGER().pkey().tableGenerator("tbgen1");
		t.column("i2").INTEGER().pkey().tableGenerator("tbgen2");
		return t;
	}

	@Test
	public void testTableGeneratorModel() {
		printAllDialectsDDLs(tableGeneratorModel(), tableGeneratorModel2());
	}

	private static Table autoGeneratorModel() {// autoGenerator
		Table t = new Table("testTable1");
		t.column("i1").INTEGER().pkey().autoGenerator();
		t.column("i2").INTEGER().autoGenerator();
		return t;
	}

	private static Table autoGeneratorModel2() {// autoGenerator
		Table t = new Table("testTable2");
		t.addTableGenerator("tbgen7", "tb1", "pkcol4", "valcol", "pkval5", 1, 10);
		t.column("i1").INTEGER().pkey().autoGenerator();
		t.column("i2").INTEGER().autoGenerator();
		t.column("i3").INTEGER().autoGenerator();
		return t;
	}

	@Test
	public void testAutoGeneratorModel() {
		printAllDialectsDDLs(autoGeneratorModel(), autoGeneratorModel2(), tableGeneratorModel2());
	}

	@Test
	public void testFKEY() {// FKEY
		Table t1 = new Table("master1");
		t1.column("id").INTEGER().pkey();

		Table t2 = new Table("master2");
		t2.column("name").VARCHAR(20).pkey();
		t2.column("address").VARCHAR(20).pkey();

		Table t3 = new Table("child");
		t3.column("id").INTEGER().pkey();
		t3.column("masterid1").INTEGER().fkey("master1", "id");
		t3.column("myname").VARCHAR(20).fkey("master2", "name", "address");
		t3.column("myaddress").VARCHAR(20).fkey("master2", "name", "address");

		Table t4 = new Table("child2");
		t4.column("id").INTEGER().pkey();
		t4.column("masterid1").INTEGER();
		t4.column("myname").VARCHAR(20);
		t4.column("myaddress").VARCHAR(20);
		t4.fkey("f1").ref("master1", "id");
		t4.fkey("f2","f3").ref("master2", "name", "address");
		printAllDialectsDDLs(t1, t2, t3);
		printOneDialectsDDLs(Dialect.MySQL5InnoDBDialect, t1, t2, t3, t4);
	}

}