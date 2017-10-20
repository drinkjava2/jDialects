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

import com.github.drinkjava2.jdialects.ModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;

import test.TestBase;

/**
 * Unit test for SortedUUIDGenerator
 */
public class Db2ModelsTest extends TestBase {

	@Test
	public void testDb2Model() {
		TableModel t = new TableModel("testTable");
		t.column("id").LONG().pkey();
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
	
		String[] ddls = guessedDialect.toDropDDL(t);
		quietExecuteDDLs(ddls);
		ddls = guessedDialect.toCreateDDL(t);
		executeDDLs(ddls);

		Connection con = null;
		TableModel[] tableModels=null;
		try {
			con = dbPro.prepareConnection();
			tableModels=ModelUtils.db2Model(con, guessedDialect);
			System.out.println(tableModels[0]);
			System.out.println(tableModels[0]);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				dbPro.close(con);
			} catch (SQLException e) { 
				e.printStackTrace();
			}
		}

	}
}
