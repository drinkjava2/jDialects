/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.coveragetest.jdialects;

import org.junit.Test;

import com.github.drinkjava2.hibernate.utils.DDLFormatter;

/**
 * This is for DDLFormatter Test
 * 
 * @author Yong Z.
 *
 */
public class DDLFormatterTest {

	@Test
	public void testDDLFormatter() {
		System.out.println(DDLFormatter.format(
				"create column table customertable (id varchar(32) not null, customer_name varchar(30), primary key (id))"));
		System.out.println(DDLFormatter.format(
				"create table customertable (id varchar(32) not null, customer_name varchar(30), primary key (id)) engine=InnoDB"));
	}

}
