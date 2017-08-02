/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.drinkjava2.tinyjdbc.inline;

import java.util.Map;
import java.util.Set;

/**
 * A SqlTemplateEngine render a SQL Template String and a Map<String, Object>
 * instance into a {@link SqlAndParams} Object
 * 
 * @author Yong Zhu
 */
public interface SqlTemplateEngine {

	/**
	 * Render a SQL Template String and a Map<String, Object> instance into a
	 * {@link SqlAndParams} instance
	 * 
	 * @param sqlTemplate
	 *            A SQL template String.
	 * @param paramMap
	 *            A Map instance, key is String type, value is Object type
	 * @param directReplaceNamesSet
	 *            Optional, A Set includes key names in Template should directly
	 *            replaced by a String value, can not be treated as SQL
	 *            parameters by using bind() methods, for example, in
	 *            BasicSqlTemplate, if use a bind() method to bind a value to a
	 *            ${xx} key, will get an Runtime Exception with message "should
	 *            use replace() instead of bind() method", this design is to
	 *            avoid typing mistake caused SQL injection for a ${} parameter
	 * @return SqlAndParams
	 */
	public SqlAndParams render(String sqlTemplate, Map<String, Object> paramMap, Set<String> directReplaceNamesSet);

}
