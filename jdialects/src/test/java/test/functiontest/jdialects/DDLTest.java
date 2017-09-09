/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.functiontest.jdialects;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;

import test.BaseDDLTest;

/**
 * This is unit test for DDL
 * 
 * @author Yong Z.
 * @since 1.0.2
 */
public class DDLTest extends BaseDDLTest { 

	@Test
	public void testANormalModel() {// A normal setting
		TableModel t = new TableModel("testTable");
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
		TableModel t = new TableModel("aa");
		t.addColumn(new ColumnModel("aaaa"));
		TableModel table = new TableModel("tb").addColumn(new ColumnModel("field1").INTEGER())
				.addColumn(new ColumnModel("field2").LONG());
		printAllDialectsDDLs(table);
		testOnCurrentRealDatabase(table);
	}

	@Test
	public void testCompondPkey() {// Compound PKEY
		TableModel t = new TableModel("testTable");
		t.column("i4").INTEGER().pkey().notNull().defaultValue("1");
		t.column("l5").LONG().pkey();
		t.column("s6").SHORT();
		printAllDialectsDDLs(t);
		testOnCurrentRealDatabase(t);
	}

	private static TableModel testNotNullModel() {// Not Null
		TableModel t = new TableModel("testTable");
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

	private static TableModel allowNullModel() {// Allow Null
		TableModel t = new TableModel("testTable");
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

	private static TableModel uniqueModel() {// unique constraint
		TableModel t = new TableModel("testTable");
		t.column("s1").STRING(20).singleUnique();
		t.column("s2").STRING(20).singleUnique().notNull();

		t.column("s3").STRING(20).singleUnique("uname1");
		t.column("s4").STRING(20).singleUnique("uname2").notNull();

		t.column("s5").STRING(20).singleUnique("A");
		t.column("s6").STRING(20).singleUnique("A");
		t.column("s7").STRING(20).singleUnique("B").notNull();
		t.column("s8").STRING(20).singleUnique("B").notNull();

		t.column("s9").STRING(20).singleUnique("C");
		t.column("s10").STRING(20).singleUnique("D");
		t.column("s11").STRING(20).singleUnique("E").notNull();
		t.column("s12").STRING(20).singleUnique("F").notNull();
		t.unique().columns("S9", "S10");
		t.unique("uk1").columns("s11", "s12");
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

	private static TableModel checkModel() {// column check
		TableModel t = new TableModel("testTable");
		t.column("s1").STRING(20).notNull().check("s1>5");
		t.column("s2").STRING(20).check("s2>5");
		t.column("s3").STRING(20).notNull().check("s3>5");
		t.column("s4").STRING(20).check("s4>5");
		return t;
	}

	@Test
	public void testCheck() {
		printAllDialectsDDLs(checkModel());
		testOnCurrentRealDatabase(checkModel());
	}

	private static TableModel tableCheckModel() {// table check
		TableModel t = new TableModel("testTable");
		t.check("s2>10");
		t.column("s1").STRING(20).notNull();
		t.column("s2").STRING(20);
		return t;
	}

	@Test
	public void testTableCheck() {
		printAllDialectsDDLs(tableCheckModel());
		testOnCurrentRealDatabase(tableCheckModel());
	}

	private static TableModel IdentityModel() {// Identity
		TableModel t = new TableModel("testTable");
		t.check("s2>10"); 
		t.column("s1").INTEGER().notNull().identity().pkey();
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

	private static TableModel CommentModel() {// Comment
		TableModel t = new TableModel("testTable").comment("table_comment");
		t.column("s1").INTEGER().notNull().identity().pkey();
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

	private static TableModel SequenceModel() {// SequenceGen
		TableModel t = new TableModel("testTable");
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

	private static TableModel tableGeneratorModel() {// tableGenerator
		TableModel t = new TableModel("testTable");
		t.addTableGenerator("tbgen1", "tb1", "pkcol", "valcol", "pkval", 1, 10);
		t.addTableGenerator("tbgen2", "tb1", "pkcol2", "valcol", "pkval", 1, 10);
		t.column("i1").INTEGER().pkey().tableGenerator("tbgen1");
		t.column("i2").INTEGER().pkey().tableGenerator("tbgen2");
		return t;
	}

	private static TableModel tableGeneratorModel2() {// tableGenerator
		TableModel t = new TableModel("testTableGeneratorModel2");
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

	private static TableModel autoGeneratorModel() {// autoGenerator
		TableModel t = new TableModel("testTable1");
		t.column("i1").INTEGER().pkey().autoID();
		t.column("i2").INTEGER().autoID();
		return t;
	}

	private static TableModel autoGeneratorModel2() {// autoGenerator
		TableModel t = new TableModel("testTable2");
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
		TableModel t1 = new TableModel("master1");
		t1.column("id").INTEGER().pkey();

		TableModel t2 = new TableModel("master2");
		t2.column("name").VARCHAR(20).pkey();
		t2.column("address").VARCHAR(20).pkey();

		TableModel t3 = new TableModel("child");
		t3.column("id").INTEGER().pkey();
		t3.column("masterid1").INTEGER();
		t3.column("myname").VARCHAR(20);
		t3.column("myaddress").VARCHAR(20);
		t3.fkey().columns("masterid1").refs("master1", "id");
		t3.fkey("FKNAME1").columns("myname", "myaddress").refs("master2", "name", "address");

		TableModel t4 = new TableModel("child2");
		t4.column("id").INTEGER().pkey();
		t4.column("masterid2").INTEGER();
		t4.column("myname2").VARCHAR(20);
		t4.column("myaddress2").VARCHAR(20);
		t4.fkey().columns("masterid2").refs("master1", "id");
		t4.fkey().columns("myname2", "myaddress2").refs("master2", "name", "address");
		printAllDialectsDDLs(t1, t2, t3);
		printOneDialectsDDLs(Dialect.MySQL5InnoDBDialect, t1, t2, t3, t4);
		testOnCurrentRealDatabase(t1, t2, t3, t4);
	}

	@Test
	public void testIndex() {// index
		TableModel t = new TableModel("indexTable");
		t.column("s1").STRING(20).index().singleUnique();
		t.column("s2").STRING(20).index();
		t.column("s3").STRING(20).index();
		t.column("s4").STRING(20).singleIndex("a");
		t.column("s5").STRING(20).singleIndex("b");
		t.column("s6").STRING(20).singleIndex("c");
		t.index().columns("s1", "s2");
		t.index("idx1").columns("s5", "s1");
		printAllDialectsDDLs(t);
		printOneDialectsDDLs(Dialect.MySQL5InnoDBDialect, t);
		testOnCurrentRealDatabase(t);
	}

	@Test
	public void testEngineTailAndColumnTail() {// engineTail and column Tail
		TableModel t = new TableModel("tailsTestTable");
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
		TableModel t = new TableModel("testNextIdTable");
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
	public void singleXxxMethodTest() {// Test singleXxx methods
		TableModel t1 = new TableModel("customers");
		t1.column("name").STRING(20).singleUnique();
		t1.column("email").VARCHAR(50).singleIndex("IDX1").defaultValue("'Beijing'").comment("address comment");

		TableModel t2 = new TableModel("orders");
		t2.column("item").STRING(20).singleUnique("A");
		t2.column("name").STRING(20).singleFKey("customers", "name");

		String[] dropAndCreateDDL = Dialect.H2Dialect.toDropAndCreateDDL(t1, t2);
		for (String ddl : dropAndCreateDDL)
			System.out.println(ddl);
		testOnCurrentRealDatabase(t1, t2);
	}

	@Test
	public void sampleTest() {// An example used to put on README.md
		TableModel t1 = new TableModel("customers");
		t1.column("name").STRING(20).pkey();
		t1.column("email").STRING(20).pkey();
		t1.column("address").VARCHAR(50).defaultValue("'Beijing'").comment("address comment");
		t1.column("phoneNumber").VARCHAR(50).singleIndex("IDX2");
		t1.column("age").INTEGER().notNull().check("'>0'");
		t1.index("idx3").columns("address", "phoneNumber").unique();

		TableModel t2 = new TableModel("orders").comment("order comment");
		t2.column("id").LONG().autoID().pkey();
		t2.column("name").STRING(20);
		t2.column("email").STRING(20);
		t2.column("name2").STRING(20).pkey().tail(" default 'Sam'");
		t2.column("email2").STRING(20);
		t2.fkey().columns("name2", "email2").refs("customers", "name", "email");
		t2.fkey("fk1").columns("name", "email").refs("customers", "name", "email");
		t2.unique("uk1").columns("name2", "email2");

		TableModel t3 = new TableModel("sampletable");
		t3.column("id").LONG().identity().pkey();
		t3.addTableGenerator("table_gen1", "tb1", "pkcol2", "valcol", "pkval", 1, 10);
		t3.column("id1").INTEGER().tableGenerator("table_gen1");
		t3.addSequence("seq1", "seq_1", 1, 1);
		t3.column("id2").INTEGER().sequence("seq1");
		t3.engineTail(" DEFAULT CHARSET=utf8");
 

		String[] dropAndCreateDDL = Dialect.H2Dialect.toDropAndCreateDDL(t1, t2, t3);
		for (String ddl : dropAndCreateDDL)
			System.out.println(ddl);

		testOnCurrentRealDatabase(t1, t2, t3);
	}
}