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

import org.junit.Test;

import com.github.drinkjava2.jdialects.id.AutoIdGenerator;
import com.github.drinkjava2.jdialects.id.SortedUUIDGenerator;
import com.github.drinkjava2.jdialects.model.TableModel;

import test.BaseDDLTest;

/**
 * Unit test for SortedUUIDGenerator
 */
public class IdgeneratorTest extends BaseDDLTest {

	@Test
	public void testAutoIdGenerator() {
		TableModel table = new TableModel("testAutoIdGenerator");
		table.column("id").STRING(30).pkey().autoID();
		reBuildDB(table);

		AutoIdGenerator gen = AutoIdGenerator.INSTANCE;
		for (int i = 0; i < 5; i++) {
			System.out.println(gen.getNextID(db, guessedDialect));
		}
	}

	@Test
	public void testSortedUUIDGenerator() {
		TableModel table = new TableModel("testSortedUUIDGenerator");
		table.column("id").STRING(30).pkey().autoID();
		reBuildDB(table);

		SortedUUIDGenerator gen = new SortedUUIDGenerator(12, 5);
		for (int i = 0; i < 5555; i++) {
			System.out.println(gen.getNextID(db, guessedDialect));
		}
	}

	@Test
	public void testTableIdGenerator() {
//		TableModel table = new TableModel("testTableIdGenerator");
//		table.tableGenerator("table1", "tb1", "pkCol", "valueColname", "pkColVal", 1, 10);
//		table.addColumn("id").STRING(30).pkey().tableGenerator("table1");
//		reBuildDB(table);
// 
//		IdGenerator gen = table.getGenerator("table1");
//		System.out.println(gen);
//		for (int i = 0; i < 8; i++) {
//			System.out.println(gen.getNextID(db, guessedDialect));
//		}
//		for (int i = 0; i < 8; i++) {
//			System.out.println(gen.getNextID(db, guessedDialect));
//		}
//		
//		for (int i = 0; i < 8; i++) {
//			System.out.println(   );
//		}
	}

}
