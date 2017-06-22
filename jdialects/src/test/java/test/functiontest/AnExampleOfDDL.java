/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.functiontest;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.Table;

/**
 * A DDL Drop and create example
 * 
 * @author Yong Z.
 * @since 1.0.2
 */
public class AnExampleOfDDL {

	public static void main(String[] args) {
		Table t1 = new Table("customers");
		t1.column("id").LONG().identity();
		t1.column("name").STRING(20).unique().pkey();
		t1.column("email").STRING(50).unique().pkey().index("IDX_EMAIL");
		t1.column("address").VARCHAR(50).index().defaultValue("Beijing").comment("address comment");
		t1.column("phoneNumber").VARCHAR(50).index("IDX_phone");
		t1.column("age").INTEGER().notNull().check(">0");

		Table t2 = new Table("orders").comment("order comment");
		t2.engineTail(" DEFAULT CHARSET=utf8");
		t2.column("id").INTEGER().autoID().pkey();
		t2.column("customerID").STRING(20).fkey("customer", "id");
		t2.column("customerName").STRING(20).unique().pkey().tail(" default 'Sam'");
		t2.column("customerEmail").STRING(50).unique().index("IDX_EMAIL");
		t2.fkey("customerName", "customerEmail").ref("customers", "name", "email");

		Table t3 = new Table("sampletable");
		t3.addTableGenerator("table_gen1", "tb1", "pkcol2", "valcol", "pkval", 1, 10);
		t3.column("id1").INTEGER().tableGenerator("table_gen1");
		t3.addSequence("seq1", "seq_1", 1, 1);
		t3.column("id2").INTEGER().sequence("seq1");

		String[] dropAndCreateDDL = Dialect.Oracle12cDialect.toDropAndCreateDDL(t1, t2, t3);
		for (String ddl : dropAndCreateDDL)
			System.out.println(ddl);
		// System.out.println(DDLFormatter.format(ddl));
	}

}