/**
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package test.functiontest.jdialects;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.utils.DialectUtils;

import test.BaseDDLTest;

/**
 * Unit test for SortedUUIDGenerator
 */
public class Db2ModelsTest extends BaseDDLTest {

	@Test
	public void testDb2Model() {
		TableModel t = new TableModel("testTable");
		t.addColumn("id").LONG().pkey();
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
	
		String[] ddls = guessedDialect.toDropDDL(t);
		quiteExecuteNoParamSqls(ddls);
		ddls = guessedDialect.toCreateDDL(t);
		executeNoParamSqls(ddls);

		Connection con = null;
		TableModel[] tableModels=null;
		try {
			con = db.prepareConnection();
			tableModels=DialectUtils.db2Models(con, guessedDialect);
			System.out.println(tableModels[0]);
			System.out.println(tableModels[0]);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				db.close(con);
			} catch (SQLException e) { 
				e.printStackTrace();
			}
		}

	}
}
