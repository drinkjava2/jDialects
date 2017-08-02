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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * InlineSupport class store parameters in ThreadLocal to support in-line style
 * 
 * @author Yong Zhu
 */
public abstract class InlineSupport {

	private static SqlTemplateEngine globalSqlTemplateEngine = new BasicSqlTemplate();

	/**
	 * A ThreadLocal variant for temporally store parameters in current Thread
	 */
	private static ThreadLocal<ArrayList<Object>> paramCache = new ThreadLocal<ArrayList<Object>>() {
		@Override
		protected ArrayList<Object> initialValue() {
			return new ArrayList<Object>();
		}
	};

	/**
	 * A ThreadLocal variant for temporally store parameter Map in current
	 * Thread
	 */
	private static ThreadLocal<Map<String, Object>> paramMapCache = new ThreadLocal<Map<String, Object>>() {
		@Override
		protected Map<String, Object> initialValue() {
			return new HashMap<String, Object>();
		}
	};

	/**
	 * A ThreadLocal variant for temporally store parameter key names which is a
	 * direct-replace type parameter in current Thread
	 */
	private static ThreadLocal<Set<String>> directReplaceKeysCache = new ThreadLocal<Set<String>>() {
		@Override
		protected Set<String> initialValue() {
			return new HashSet<String>();
		}
	};

	/**
	 * Clear all ThreadLocal cache, return an empty string
	 */
	public static String clearAll() {
		paramCache.get().clear();
		paramMapCache.get().clear();
		directReplaceKeysCache.get().clear();
		return "";
	};

	/**
	 * Get GlobalSqlTemplateEngine
	 */
	public static SqlTemplateEngine getGlobalSqlTemplateEngine() {
		return globalSqlTemplateEngine;
	}

	/**
	 * Set GlobalSqlTemplateEngine instance
	 * 
	 * @param defaultSqlTemplate
	 */
	public static synchronized void setGlobalSqlTemplateEngine(SqlTemplateEngine sqlTemplateEngine) {
		InlineSupport.globalSqlTemplateEngine = sqlTemplateEngine;
	}

	/**
	 * Clear all ThreadLocal cache first, then cache parameters in ThreadLocal
	 * and return an empty String
	 */
	public static String param0(Object... parameters) {
		clearAll();
		return param(parameters);
	}

	/**
	 * Return an empty String and cache parameters in ThreadLocal
	 */
	public static String param(Object... parameters) {
		for (Object o : parameters)
			paramCache.get().add(o);
		return "";
	}

	/**
	 * Clear cache first, then return a "?" String and cache parameters in
	 * ThreadLocal
	 */
	public static String question0(Object... parameters) {
		clearAll();
		return question(parameters);
	}

	/**
	 * Return a "?" String and cache parameters in ThreadLocal
	 */
	public static String question(Object... parameters) {
		for (Object o : parameters)
			paramCache.get().add(o);
		return "?";
	}

	/**
	 * Build a SqlAndParams instance by given in-line style SQL and parameters
	 * stored in ThreadLocal
	 * 
	 * @param inlineSQL
	 * @return SqlAndParams instance
	 */
	public static SqlAndParams buildSqlAndParams(String... inlineSQL) {
		try {
			String sql = null;
			if (inlineSQL != null) {
				StringBuilder sb = new StringBuilder("");
				for (String str : inlineSQL)
					sb.append(str);
				sql = sb.toString();
			}
			Map<String, Object> paramMap = paramMapCache.get();
			ArrayList<Object> params = paramCache.get();
			if (paramMap.size() != 0) {
				if (params.size() != 0)
					throw new RuntimeException("In-line style SQL does not allow mixed use param() and bind() method.");
				return globalSqlTemplateEngine.render(sql, paramMap, directReplaceKeysCache.get());
			} else {
				SqlAndParams sp = new SqlAndParams();
				sp.setSql(sql);
				sp.setParams(params.toArray(new Object[params.size()]));
				return sp;
			}
		} finally {
			clearAll();
		}
	}

	/**
	 * Clear all ThreadLocal cache, return an empty string, equal to clear()
	 * method
	 */
	public static String bind0() {
		clearAll();
		return "";
	}

	/**
	 * Clear all ThreadLocal cache first, add a name-value pair into ThreadLocal
	 * parameter Map, return an empty string
	 */
	public static String bind0(String name, Object value) {
		clearAll();
		return bind(name, value);
	}

	/**
	 * Add a name-value pair into ThreadLocal parameter Map, return an empty
	 * string
	 */
	public static String bind(String name, Object value) {
		paramMapCache.get().put(name, value);
		return "";
	}

	/**
	 * Clear all ThreadLocal cache first, then add a name-value into ThreadLocal
	 * parameter Map, return an empty string, Note: use replace() method the
	 * value will directly replace name in template instead of as a SQL
	 * parameter
	 */
	public static String replace0(String name, Object value) {
		clearAll();
		return replace(name, value);
	}

	/**
	 * Add a name-value into ThreadLocal parameter Map, return an empty string,
	 * Note: use replace() method the value will directly replace name in
	 * template instead of as a SQL parameter
	 */
	public static String replace(String name, Object value) {
		paramMapCache.get().put(name, value);
		directReplaceKeysCache.get().add(name);
		return "";
	}

}
