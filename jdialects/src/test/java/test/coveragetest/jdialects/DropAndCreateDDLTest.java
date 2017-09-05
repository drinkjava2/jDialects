/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.coveragetest.jdialects;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.hibernatesrc.utils.DDLFormatter;
import com.github.drinkjava2.jdialects.model.VColumn;
import com.github.drinkjava2.jdialects.model.VTable;

import test.BaseDDLTest;

/**
 * This is unit test for Table
 * 
 * @author Yong Z.
 * @since 1.0.2
 */
public class DropAndCreateDDLTest extends BaseDDLTest {
	private static void printDDLs(String[] ddl) {
		for (String str : ddl) {
			System.out.println(str);
		}
	}

	private void quiteExecuteNoParamSqls(String... sqls) {
		for (String sql : sqls) {
			try {
				tiny.nExecute(sql);
			} catch (Exception e) {
			}
		}
	}

	private void executeNoParamSqls(String... sqls) {
		for (String sql : sqls)
			tiny.nExecute(sql);
	}

	private void testOnCurrentRealDatabase(VTable... tables) {
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

	private static void printOneDialectsDDLs(Dialect dialect, VTable... tables) {
		System.out.println("======" + dialect + "=====");
		try {
			String[] ddls = dialect.toDropAndCreateDDL(tables);
			printDDLs(DDLFormatter.format(ddls));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception found: " + e.getMessage());
		}
	}

	private static void printAllDialectsDDLs(VTable... tables) {
		Dialect[] diaList = Dialect.values();
		for (Dialect dialect : diaList) {
			System.out.println("======" + dialect + "=====");
			try {
				String[] ddls = dialect.toDropAndCreateDDL(tables);
				printDDLs(DDLFormatter.format(ddls));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Exception found: " + e.getMessage());
			}
		}
	}

	@Test
	public void testANormalModel() {// A normal setting
		VTable t = new VTable("testTable");
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
		testOnCurrentRealDatabase(t);
	}

	@Test
	public void testNoPkey() {// Test no Prime Key
		// append() is a linked method
		VTable t = new VTable("aa");
		t.addColumn(new VColumn("aaaa"));
		VTable table = new VTable("tb").addColumn(new VColumn("field1").INTEGER())
				.addColumn(new VColumn("field2").LONG());
		printAllDialectsDDLs(table);
		testOnCurrentRealDatabase(table);
	}

	@Test
	public void testCompondPkey() {// Compound PKEY
		VTable t = new VTable("testTable");
		t.column("i4").INTEGER().pkey().unique().notNull().defaultValue("1");
		t.column("l5").LONG().pkey();
		t.column("s6").SHORT();
		printAllDialectsDDLs(t);
		testOnCurrentRealDatabase(t);
	}

	private static VTable testNotNullModel() {// Not Null
		VTable t = new VTable("testTable");
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
		testOnCurrentRealDatabase(testNotNullModel());
	}

	private static VTable allowNullModel() {// Allow Null
		VTable t = new VTable("testTable");
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
		testOnCurrentRealDatabase(allowNullModel());
	}

	private static VTable uniqueModel() {// unique constraint
		VTable t = new VTable("testTable");
		t.column("s1").STRING(20).unique();
		t.column("s2").STRING(20).unique().notNull();

		t.column("s3").STRING(20).unique("uname1");
		t.column("s4").STRING(20).unique("uname2").notNull();

		t.column("s5").STRING(20).unique("A");
		t.column("s6").STRING(20).unique("A");
		t.column("s7").STRING(20).unique("B").notNull();
		t.column("s8").STRING(20).unique("B").notNull();

		t.column("s9").STRING(20).unique("C");
		t.column("s10").STRING(20).unique("D");
		t.column("s11").STRING(20).unique("E").notNull();
		t.column("s12").STRING(20).unique("F").notNull();
		t.unique().columns("S9","S10");
		t.unique("uk1").columns("s11","s12");
		t.unique().columns("s5");
		return t;
	}

	@Test
	public void testUnique() {
		printAllDialectsDDLs(uniqueModel());
		testOnCurrentRealDatabase(allowNullModel());
		printOneDialectsDDLs(Dialect.DB2Dialect, uniqueModel());
		printOneDialectsDDLs(Dialect.InformixDialect, uniqueModel());
	}

	private static VTable checkModel() {// column check
		VTable t = new VTable("testTable");
		t.column("s1").STRING(20).unique().notNull().check("s1>5");
		t.column("s2").STRING(20).unique().check("s2>5");
		t.column("s3").STRING(20).unique("uname1").notNull().check("s3>5");
		t.column("s4").STRING(20).unique("uname2").check("s4>5");
		return t;
	}

	@Test
	public void testCheck() {
		printAllDialectsDDLs(checkModel());
		testOnCurrentRealDatabase(checkModel());
	}

	private static VTable tableCheckModel() {// table check
		VTable t = new VTable("testTable");
		t.check("s2>10");
		t.column("s1").STRING(20).unique().notNull();
		t.column("s2").STRING(20);
		return t;
	}

	@Test
	public void testTableCheck() {
		printAllDialectsDDLs(tableCheckModel());
		testOnCurrentRealDatabase(tableCheckModel());
	}

	private static VTable IdentityModel() {// Identity
		VTable t = new VTable("testTable");
		t.check("s2>10");
		t.column("s1").INTEGER().unique().notNull().identity().pkey();
		t.column("s2").LONG().check("s2>10");
		t.column("s3").BIGINT();
		return t;
	}

	@Test
	public void testIdentity() {
		printAllDialectsDDLs(IdentityModel());
		testOnCurrentRealDatabase(IdentityModel());
	}

	@Test
	public void testIdentity2() {
		printOneDialectsDDLs(Dialect.SybaseASE15Dialect, IdentityModel());
		printOneDialectsDDLs(Dialect.MySQL55Dialect, IdentityModel());
		printOneDialectsDDLs(Dialect.InformixDialect, IdentityModel());
	}

	private static VTable CommentModel() {// Comment
		VTable t = new VTable("testTable").comment("table_comment");
		t.column("s1").INTEGER().unique().notNull().identity().pkey();
		t.column("s2").LONG().comment("column_comment1");
		t.column("s3").BIGINT().comment("column_comment2");
		return t;
	}

	@Test
	public void testComment() {
		printAllDialectsDDLs(CommentModel());
		testOnCurrentRealDatabase(CommentModel());
	}

	@Test
	public void testComment2() {
		printOneDialectsDDLs(Dialect.Ingres10Dialect, CommentModel());
		printOneDialectsDDLs(Dialect.DB2Dialect, CommentModel());
		printOneDialectsDDLs(Dialect.MariaDBDialect, CommentModel());
		printOneDialectsDDLs(Dialect.SQLServer2012Dialect, CommentModel());
		printOneDialectsDDLs(Dialect.MySQL55Dialect, CommentModel());
	}

	private static VTable SequenceModel() {// Sequence
		VTable t = new VTable("testTable");
		t.addSequence("seq1", "seq_1", 1, 1);
		t.addSequence("seq2", "seq_2", 1, 2);
		t.column("i1").INTEGER().pkey().sequence("seq1");
		t.column("i2").INTEGER().pkey().sequence("seq2");
		return t;
	}

	@Test
	public void testSequence() {
		printAllDialectsDDLs(SequenceModel());
		if (guessedDialect.getDdlFeatures().supportBasicOrPooledSequence())
			testOnCurrentRealDatabase(SequenceModel());
	}

	private static VTable tableGeneratorModel() {// tableGenerator
		VTable t = new VTable("testTable");
		t.addTableGenerator("tbgen1", "tb1", "pkcol", "valcol", "pkval", 1, 10);
		t.addTableGenerator("tbgen2", "tb1", "pkcol2", "valcol", "pkval", 1, 10);
		t.column("i1").INTEGER().pkey().tableGenerator("tbgen1");
		t.column("i2").INTEGER().pkey().tableGenerator("tbgen2");
		return t;
	}

	private static VTable tableGeneratorModel2() {// tableGenerator
		VTable t = new VTable("testTableGeneratorModel2");
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
		testOnCurrentRealDatabase(tableGeneratorModel(), tableGeneratorModel2());
	}

	private static VTable autoGeneratorModel() {// autoGenerator
		VTable t = new VTable("testTable1");
		t.column("i1").INTEGER().pkey().autoID();
		t.column("i2").INTEGER().autoID();
		return t;
	}

	private static VTable autoGeneratorModel2() {// autoGenerator
		VTable t = new VTable("testTable2");
		t.addTableGenerator("tbgen7", "tb1", "pkcol4", "valcol", "pkval5", 1, 10);
		t.column("i1").INTEGER().pkey().autoID();
		t.column("i2").INTEGER().autoID();
		t.column("i3").INTEGER().autoID();
		return t;
	}

	@Test
	public void testAutoGeneratorModel() {
		printAllDialectsDDLs(autoGeneratorModel(), autoGeneratorModel2(), tableGeneratorModel2());
		printOneDialectsDDLs(Dialect.MySQL5Dialect, autoGeneratorModel(), autoGeneratorModel2(),
				tableGeneratorModel2());
		testOnCurrentRealDatabase(autoGeneratorModel(), autoGeneratorModel2(), tableGeneratorModel2());
	}

	@Test
	public void testFKEY() {// FKEY
		VTable t1 = new VTable("master1");
		t1.column("id").INTEGER().pkey();

		VTable t2 = new VTable("master2");
		t2.column("name").VARCHAR(20).pkey();
		t2.column("address").VARCHAR(20).pkey();

		VTable t3 = new VTable("child");
		t3.column("id").INTEGER().pkey();
		t3.column("masterid1").INTEGER();
		t3.column("myname").VARCHAR(20);
		t3.column("myaddress").VARCHAR(20);
		t3.fkey().columns("masterid1").ref("master1", "id");
		t3.fkey().columns("myname", "myaddress").ref("master2", "name", "address");

		VTable t4 = new VTable("child2");
		t4.column("id").INTEGER().pkey();
		t4.column("masterid2").INTEGER();
		t4.column("myname2").VARCHAR(20);
		t4.column("myaddress2").VARCHAR(20);
		t4.fkey().columns("masterid2").ref("master1", "id");
		t4.fkey().columns("myname2", "myaddress2").ref("master2", "name", "address");
		printAllDialectsDDLs(t1, t2, t3);
		printOneDialectsDDLs(Dialect.MySQL5InnoDBDialect, t1, t2, t3, t4);
		testOnCurrentRealDatabase(t1, t2, t3, t4);
	}

	@Test
	public void testIndex() {// index
		VTable t = new VTable("indexTable");
		t.column("s1").STRING(20).index().unique();
		t.column("s2").STRING(20).index();
		t.column("s3").STRING(20).index();
		t.column("s4").STRING(20).index("a");
		t.column("s5").STRING(20).index("b");
		t.column("s6").STRING(20).index("c");
		t.index().columns("s1","s2");
		t.index("idx1").columns("s5","s1");
		printAllDialectsDDLs(t);
		printOneDialectsDDLs(Dialect.MySQL5InnoDBDialect, t);
		testOnCurrentRealDatabase(t);
	}

	@Test
	public void testEngineTailAndColumnTail() {// engineTail and column Tail
		VTable t = new VTable("tailsTestTable");
		t.engineTail(" DEFAULT CHARSET=utf8");
		t.column("id").STRING(20).pkey();
		t.column("name").STRING(20).tail(" default 'hahaha'");
		printOneDialectsDDLs(Dialect.Oracle10gDialect, t);
		printOneDialectsDDLs(Dialect.H2Dialect, t);
		printOneDialectsDDLs(Dialect.MySQL5InnoDBDialect, t);
		printOneDialectsDDLs(Dialect.MariaDB53Dialect, t);
		testOnCurrentRealDatabase(t);
	}

	@Test
	public void testNextID() {// nextID
		VTable t = new VTable("testNextIdTable");
		t.column("id1").LONG().autoID().pkey();
		t.column("id2").LONG().autoID();
		tiny.setAllowShowSQL(true);
		String[] ddls = guessedDialect.toDropDDL(t);
		quiteExecuteNoParamSqls(ddls);

		ddls = guessedDialect.toCreateDDL(t);
		executeNoParamSqls(ddls);
		Connection conn = null;
		try {
			conn = tiny.prepareConnection();
			for (int i = 0; i < 10; i++) {
				Long id1 = guessedDialect.getNextAutoID(conn);
				Long id2 = guessedDialect.getNextAutoID(conn);
				tiny.execute(conn, "insert into testNextIdTable values(?,?)", id1, id2);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				tiny.close(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void sampleTest() {// An example used to put on README.md
		VTable t1 = new VTable("customers");
		t1.column("name").STRING(20).unique().pkey();
		t1.column("email").STRING(20).pkey();
		t1.column("address").VARCHAR(50).index("IDX1").defaultValue("'Beijing'").comment("address comment");
		t1.column("phoneNumber").VARCHAR(50).index("IDX2");
		t1.column("age").INTEGER().notNull().check("'>0'");
		t1.index("idx3").columns("address","phoneNumber").unique();

		VTable t2 = new VTable("orders").comment("order comment");
		t2.column("id").LONG().autoID().pkey();
		t2.column("name").STRING(20);
		t2.column("email").STRING(20);
		t2.column("name2").STRING(20).unique("A").pkey().tail(" default 'Sam'");
		t2.column("email2").STRING(20).unique("B");
		t2.fkey().columns("name2", "email2").ref("customers", "name", "email");
		t2.fkey("fk1").columns("name", "email").ref("customers", "name", "email");
		t2.unique("uk1").columns("name2","email2");

		VTable t3 = new VTable("sampletable");
		t3.column("id").LONG().identity().pkey();
		t3.addTableGenerator("table_gen1", "tb1", "pkcol2", "valcol", "pkval", 1, 10);
		t3.column("id1").INTEGER().tableGenerator("table_gen1");
		t3.addSequence("seq1", "seq_1", 1, 1);
		t3.column("id2").INTEGER().sequence("seq1");
		t3.engineTail(" DEFAULT CHARSET=utf8");

		VTable t4 = new VTable("sampletable2");
		t3.column("id_a").LONG().ref("sampletable", "id");

		String[] dropAndCreateDDL = Dialect.H2Dialect.toDropAndCreateDDL(t1, t2, t3, t4);
		for (String ddl : dropAndCreateDDL)
			System.out.println(ddl);

		testOnCurrentRealDatabase(t1, t2, t3, t4);
	}
}