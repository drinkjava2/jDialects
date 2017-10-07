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
 * This TimeStampGenerator return a long type value based on computer's current
 * time
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class TimeStampIdGenerator implements IdGenerator {
	public static final TimeStampIdGenerator INSTANCE = new TimeStampIdGenerator();
	private Integer count = 1;

	@Override
	public GenerationType getGenerationType() {
		return GenerationType.TIMESTAMP;
	}

	@Override
	public String getIdGenName() {
		return "SIMPLE";
	}

	@Override
	public Object getNextID(NormalJdbcTool jdbc, Dialect dialect) {
		if (count > 999999)
			count = 1;
		return System.currentTimeMillis() * 1000000 + ++count;
	}

	@Override
	public IdGenerator newCopy() {
		return INSTANCE;
	};
}