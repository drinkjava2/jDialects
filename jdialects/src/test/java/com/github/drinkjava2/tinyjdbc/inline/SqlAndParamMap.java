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

package com.github.drinkjava2.tinyjdbc.inline;

import java.util.HashMap;
import java.util.Map;

/**
 * SqlAndParameters is a POJO class to store SQL and parameter Map
 * 
 * @author Yong Zhu
 */
public class SqlAndParamMap {
	private String sql;
	private Map<String, Object> paramMap = new HashMap<String, Object>();

	public String getSql() {
		return sql;
	}

	public Map<String, Object> getParamMap() {
		return paramMap;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public void setParamMap(Map<String, Object> paramMap) {
		this.paramMap = paramMap;
	}

}
