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
package com.github.drinkjava2.test.function;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.PKey;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.GeneratedValue;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.test.TestBase;

/**
 * Unit test for SortedUUIDGenerator
 */
public class IdgeneratorTest2 extends TestBase {

	public static class pkeyEntity {
		@Id
		private String id1;

		@PKey
		private String id2;

		public String getId2() {
			return id2;
		}

		public void setId2(String id2) {
			this.id2 = id2;
		}

		public String getId1() {
			return id1;
		}

		public void setId1(String id1) {
			this.id1 = id1;
		}
	}

	@Test
	public void testPKey() {// nextID
		TableModel t = TableModelUtils.entity2Model(pkeyEntity.class);
		Assert.assertTrue(t.column("id1").getPkey());
		Assert.assertTrue(t.column("id2").getPkey());
	}

	public static class uuid25Entity {
		@GeneratedValue(strategy = GenerationType.UUID25)
		private String id1;
		@UUID25
		private String id2;

		private String id3;

		public static void config(TableModel t) {
			t.getColumn("id3").uuid25();
		}

		public String getId1() {
			return id1;
		}

		public void setId1(String id1) {
			this.id1 = id1;
		}

		public String getId2() {
			return id2;
		}

		public void setId2(String id2) {
			this.id2 = id2;
		}

		public String getId3() {
			return id3;
		}

		public void setId3(String id3) {
			this.id3 = id3;
		}

	}

	@Test
	public void testUUID25() { 
		reBuildDB(TableModelUtils.entity2Models(uuid25Entity.class));
		testOnCurrentRealDatabase(TableModelUtils.entity2Models(uuid25Entity.class));
	}

}
