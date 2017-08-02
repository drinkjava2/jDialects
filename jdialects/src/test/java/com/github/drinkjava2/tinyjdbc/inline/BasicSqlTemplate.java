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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.drinkjava2.tinyjdbc.utils.StrHelper;

/**
 * BasicSqlTemplate is a simple implementation of SqlTemplateSupport. It
 * translate a SQL template and parameter Map into a
 * {@link #org.apache.commons.dbutils.inline.SqlAndParams} instance. Use
 * InlineSupport.getDefaultSqlTemplate() method can set to use an other template
 * engine if it implemented SqlTemplateSupport interface.
 * 
 * @author Yong Zhu
 */
public class BasicSqlTemplate implements SqlTemplateEngine {
	private String startDelimiter;
	private String endDelimiter;

	private static final String DIRECT_REPLACE_START_DELIMITER = "${";
	private static final String DIRECT_REPLACE_END_DELIMITER = "}";

	/**
	 * Build a BasicSqlTemplate instance, default use #{} as delimiter
	 */
	public BasicSqlTemplate() {
		this.startDelimiter = "#{";
		this.endDelimiter = "}";
	}

	/**
	 * Build a BasicSqlTemplate instance by given startDelimiter and
	 * endDelimiter, startDelimiter should be 1 or 2 characters and endDelimiter
	 * should be 1 character
	 * 
	 * @param startDelimiter
	 *            The start delimiter
	 * @param endDelimiter
	 *            The end delimiter
	 */
	public BasicSqlTemplate(String startDelimiter, String endDelimiter) {
		if (StrHelper.isEmpty(startDelimiter) || StrHelper.isEmpty(endDelimiter) || startDelimiter.length() > 2
				|| endDelimiter.length() != 1)
			throw new RuntimeException(
					"BasicSqlTemplate only support startDelimiter has 1 or 2 characters and endDelimiter has 1 character");
		this.startDelimiter = startDelimiter;
		this.endDelimiter = endDelimiter;
	}

	@Override
	public SqlAndParams render(String sqlTemplate, Map<String, Object> paramMap, Set<String> directReplaceNamesSet) {
		return doRender(sqlTemplate, paramMap, startDelimiter, endDelimiter, directReplaceNamesSet);
	}

	/**
	 * @param template
	 *            A SQL Template String
	 * @param paramMap
	 *            A Map stored SQL parameters
	 * @param startDelimiter
	 *            Start Delimiter of SQL Template
	 * @param endDelimiter
	 *            End Delimiter of SQL Template
	 * @return A SqlAndParams instance
	 */
	public static SqlAndParams doRender(String template, Map<String, Object> paramMap, String startDelimiter,
			String endDelimiter, Set<String> directReplaceNamesSet) {
		if (template == null)
			throw new NullPointerException("Template can not be null");
		StringBuilder sql = new StringBuilder();
		StringBuilder keyNameSB = new StringBuilder();
		List<Object> paramList = new ArrayList<Object>();
		char[] chars = ("   " + template + "   ").toCharArray();

		int lg = startDelimiter.length();
		char start1 = startDelimiter.charAt(0);
		char start2 = '\u0000';
		if (lg == 2)
			start2 = startDelimiter.charAt(1);
		char e = endDelimiter.charAt(0);

		int DRlg = DIRECT_REPLACE_START_DELIMITER.length();
		char DRst1 = DIRECT_REPLACE_START_DELIMITER.charAt(0);
		char DRst2 = '\u0000';
		if (DRlg == 2)
			DRst2 = DIRECT_REPLACE_START_DELIMITER.charAt(1);
		char eDirect = DIRECT_REPLACE_END_DELIMITER.charAt(0);

		// - - # { - - - } - - - $ { - - - } - -
		// 0 0 1 1 2 2 2 3 0 0 0 1 1 2 2 2 3 0 0
		// - - - - - - - - - - - D D D D D - - -
		int status = 0; // 0:normal 1:start-delimiter 2:inside 3: end-delimiter
		boolean directRep = false; // direct replace tag
		for (int i = 3; i < chars.length - 2; i++) {
			char c = chars[i];
			char c1 = chars[i + 1];
			char c_1 = chars[i - 1];
			char c_2 = chars[i - 2];
			if (status == 0 && ((lg == 1 && c == start1) || (lg == 2 && c == start1 && c1 == start2))) {
				status = 1;
				keyNameSB.setLength(0);
				directRep = false;
			} else if (status == 0 && ((DRlg == 1 && c == DRst1) || (DRlg == 2 && c == DRst1 && c1 == DRst2))) {
				status = 1;
				keyNameSB.setLength(0);
				directRep = true;
			} else if (status == 1 && ((lg == 1 && c_1 == start1) || (lg == 2 && (c_2 == start1 && c_1 == start2)))) {
				status = 2;
			} else if (status == 1 && ((DRlg == 1 && c_1 == DRst1) || (DRlg == 2 && (c_2 == DRst1 && c_1 == DRst2)))) {
				status = 2;
			} else if (status == 2 && (((c == e) && !directRep) || ((c == eDirect) && directRep))) {
				status = 3;
				if (keyNameSB.length() == 0)
					throwEX("Empty parameter name \"" + startDelimiter + endDelimiter + "\" found in template: "
							+ template);
				String key = keyNameSB.toString();
				if (key.indexOf(".") >= 0) {// JavaBean
					String beanName = StrHelper.substringBefore(key, ".");
					String propertyName = StrHelper.substringAfter(key, ".");
					if (StrHelper.isEmpty(beanName) || StrHelper.isEmpty(propertyName))
						throwEX("illegal parameter name \"" + key + "\" found in template: " + template);
					boolean directReplaceType = isDirectReplaceTypeParameter(template, paramMap, directReplaceNamesSet,
							directRep, beanName);

					boolean hasValue = paramMap.containsKey(beanName);
					if (!hasValue)
						throwEX("Not found bean \"" + beanName + "\" when render template: " + template);

					Object bean = paramMap.get(beanName);
					PropertyDescriptor pd = null;
					try {
						pd = new PropertyDescriptor(propertyName, bean.getClass());
					} catch (IntrospectionException e1) {
						throwEX("IntrospectionException happen when get bean property \"" + key + "\" in template: "
								+ template, e1);
					}
					Method method = pd.getReadMethod();
					Object beanProperty = null;
					try {
						beanProperty = method.invoke(bean);
					} catch (Exception e1) {
						throwEX("Exception happen when read bean property \"" + key + "\" in template: " + template,
								e1);
					}
					if (directReplaceType) {
						sql.append(beanProperty);
					} else {
						sql.append("?");
						paramList.add(beanProperty);
					}
				} else {
					boolean directReplaceType = isDirectReplaceTypeParameter(template, paramMap, directReplaceNamesSet,
							directRep, key);
					boolean hasValue = paramMap.containsKey(key);
					if (!hasValue)
						throwEX("No parameter found for \"" + key + "\" in template: " + template);
					if (directReplaceType) {
						sql.append(paramMap.get(key));
					} else {
						sql.append("?");
						paramList.add(paramMap.get(key));
					}
				}
				keyNameSB.setLength(0);
			} else if (status == 3 && c_1 == e) {
				status = 0;
			}
			if (status == 0)
				sql.append(c);
			else if (status == 2)
				keyNameSB.append(c);
		}
		if (status != 0)
			throwEX("Missing end delimiter \"" + endDelimiter + "\" in template: " + template);
		SqlAndParams sp = new SqlAndParams();
		sql.setLength(sql.length() - 1);
		sp.setSql(sql.toString());
		sp.setParams(paramList.toArray(new Object[paramList.size()]));
		return sp;
	}

	private static boolean isDirectReplaceTypeParameter(String template, Map<String, Object> paramMap,
			Set<String> directReplaceNamesSet, boolean directRep, String beanName) {
		boolean directReplaceType = isDirectReplaceType(beanName, paramMap, directReplaceNamesSet);
		if (directReplaceType && !directRep)
			throwEX("\"" + beanName + "\" is a SQL parameter, but put as a direct-replace type parameter, in template: "
					+ template);
		if (!directReplaceType && directRep)
			throwEX("\"" + beanName + "\" is a direct-replace type parameter, but put as a SQL parameter, in template: "
					+ template);
		return directReplaceType;
	}

	private static boolean isDirectReplaceType(String keyName, Map<String, Object> paramMap,
			Set<String> directReplaceNamesSet) {
		if (directReplaceNamesSet == null)
			return false;
		if (directReplaceNamesSet.contains(keyName)) {
			if (!paramMap.containsKey(keyName))
				throwEX("\" " + keyName + " is indicated as a direct replace parameter but can not in parameter Map");
			return true;
		}
		return false;
	}

	private static void throwEX(String message, Exception... cause) {
		if (cause != null && cause.length > 0)
			throw new RuntimeException(message, cause[0]);
		else
			throw new RuntimeException(message);
	}

}
