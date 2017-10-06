/**
* Copyright (C) 2016 Yong Zhu.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.github.drinkjava2.jdialects.id;

import com.github.drinkjava2.jdbpro.NormalJdbcTool;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.annotation.GenerationType;

/**
 * Define an Identity type generator, supported by MySQL, SQL Server, DB2,
 * Derby, Sybase, PostgreSQL
 * 
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class IdentityIdGenerator implements IdGenerator {
	public static final IdentityIdGenerator INSTANCE = new IdentityIdGenerator();

	@Override
	public GenerationType getGenerationType() {
		return GenerationType.IDENTITY;
	}

	@Override
	public String getIdGenName() {
		return "IDENTITY";
	}

	@Override
	public Object getNextID(NormalJdbcTool jdbc, Dialect dialect) {
		// id is created by database, not me
		return null;
	}

	@Override
	public IdGenerator newCopy() {
		return INSTANCE;
	};
}
