/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.functiontest.jdialects;

import org.junit.Test;

import com.github.drinkjava2.jdialects.ColumnDef;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.annotation.Column;
import com.github.drinkjava2.jdialects.annotation.Entity;
import com.github.drinkjava2.jdialects.annotation.FKey;
import com.github.drinkjava2.jdialects.annotation.FKey1;
import com.github.drinkjava2.jdialects.annotation.GeneratedValue;
import com.github.drinkjava2.jdialects.annotation.GenerationType;
import com.github.drinkjava2.jdialects.annotation.Id;
import com.github.drinkjava2.jdialects.annotation.Index;
import com.github.drinkjava2.jdialects.annotation.SequenceGenerator;
import com.github.drinkjava2.jdialects.annotation.SequenceGenerator1;
import com.github.drinkjava2.jdialects.annotation.SingleFKey;
import com.github.drinkjava2.jdialects.annotation.SingleIndex;
import com.github.drinkjava2.jdialects.annotation.SingleUnique;
import com.github.drinkjava2.jdialects.annotation.Table;
import com.github.drinkjava2.jdialects.annotation.TableGenerator;
import com.github.drinkjava2.jdialects.annotation.TableGenerator2;
import com.github.drinkjava2.jdialects.annotation.Transient;
import com.github.drinkjava2.jdialects.annotation.UniqueConstraint;
import com.github.drinkjava2.jdialects.utils.ConvertUtils;

import test.BaseDDLTest;

/**
 * Annotation Test
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class AnnotationTest extends BaseDDLTest {

	public static class POJO1 {
		public String field1;
		public String field2;

		public String getField1() {
			return field1;
		}

		public void setField1(String field1) {
			this.field1 = field1;
		}

		public String getField2() {
			return field2;
		}

		public void setField2(String field2) {
			this.field2 = field2;
		}
	}

	@Entity
	@Table(name = "testpo", //
			uniqueConstraints = { @UniqueConstraint(columnNames = { "field1" }),
					@UniqueConstraint(name = "unique_cons2", columnNames = { "field1", "field2" }) }, //
			indexes = { @Index(columnList = "field1,field2", unique = true),
					@Index(name = "index_cons2", columnList = "field1,field2", unique = false) }//
	)
	@SequenceGenerator(name = "seqID1", sequenceName = "seqName1", initialValue = 1, allocationSize = 10)
	@SequenceGenerator1(name = "seqID2", sequenceName = "seqName2", initialValue = 2, allocationSize = 20)
	@TableGenerator(name = "tableID1", table = "table1", pkColumnName = "pkCol1", valueColumnName = "vcol1", pkColumnValue = "pkcolval1", initialValue = 2, allocationSize = 20)
	@TableGenerator2(name = "tableID2", table = "table2", pkColumnName = "pkCol1", valueColumnName = "vcol1", pkColumnValue = "pkcolval1", initialValue = 2, allocationSize = 20)
	@FKey(name = "fk1", columns = { "field1", "field2" }, refs = { "POJO1", "field1", "field2" })
	@FKey1(columns = { "field2", "field3" }, refs = { "POJO1", "field1", "field2" })
	public static class POJO2 {
		@Id
		@Column(columnDefinition = ColumnDef.VARCHAR, length = 20)
		public String field1;

		@Column(name = "field2", nullable = false, columnDefinition = ColumnDef.BIGINT)
		public String field2;

		@GeneratedValue(strategy = GenerationType.TABLE, generator = "CUST_GEN")
		@Column(name = "field3", nullable = false, columnDefinition = ColumnDef.BIGINT)
		@SingleFKey(name = "singleFkey1", refs = { "POJO1", "field1" })
		@SingleIndex
		@SingleUnique
		public Integer field3;

		@Transient
		public Integer field4;

		public String getField1() {
			return field1;
		}

		public void setField1(String field1) {
			this.field1 = field1;
		}

		public String getField2() {
			return field2;
		}

		public void setField2(String field2) {
			this.field2 = field2;
		}

		public Integer getField3() {
			return field3;
		}

		public void setField3(Integer field3) {
			this.field3 = field3;
		}

		public Integer getField4() {
			return field4;
		}

		public void setField4(Integer field4) {
			this.field4 = field4;
		}

	}

	@Test
	public void ddlOutTest() {
		String[] dropAndCreateDDL = Dialect.H2Dialect.toCreateDDL(ConvertUtils.pojo2Model(POJO1.class, POJO2.class));
		for (String ddl : dropAndCreateDDL)
			System.out.println(ddl);

		testOnCurrentRealDatabase(ConvertUtils.pojo2Model(POJO1.class, POJO2.class));
	}
}
