/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jdialects.translate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.StrUtils;

/**
 * TranslateUtil parse a Sql, translate all universal functions like fn_sin() to
 * native SQL functions like sin()
 * 
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class TranslateUtil {
	private static boolean debugMode = true;
	static Map<String, Integer> functionMap = new HashMap<String, Integer>();

	static {
		functionMap.put("CONCAT", 2);
		functionMap.put("YEAR", 2);
		functionMap.put("SECOND", 2);
	}

	/**
	 * Item type can be: <br/>
	 * S:String, F:function, U:Unknow(need correct), ",":","
	 * 
	 * @author Yong Zhu
	 * @since 1.7.0
	 */
	public static class SqlItem {
		public char type;
		public int priority;
		public Object value;

		SqlItem[] subItems;

		void setTypeAndValue(char type, Object value) {
			this.type = type;
			this.value = value;
		}

		String getDebugInfo(int include) {
			String result = "\r";
			for (int i = 0; i < include; i++) {
				result += "     ";
			}
			result += type + " ";
			if (value != null)
				result += value;
			if (subItems != null) {
				for (SqlItem Item : subItems) {
					result += Item.getDebugInfo(include + 1);
				}
			}
			return result;
		}

	}

	static class SearchResult {
		SqlItem item;
		int leftStart;
		int leftEnd;

		SearchResult(SqlItem item, int leftStart, int leftEnd) {
			this.item = item;
			this.leftStart = leftStart;
			this.leftEnd = leftEnd;
		}
	}

	static class ParamPosition {
		int position = 0;
	}

	/**
	 * Parse a expression String, return an object result
	 * 
	 * @param bean
	 *            Expression allow direct use only 1 bean's fields
	 * @param keyWords
	 *            The preset key words key-value map
	 * @param expression
	 *            The expression
	 * @param params
	 *            The expression parameter array
	 * @return an object result
	 */
	public String doParse(Dialect d, String expression) {
		if (StrUtils.isEmpty(expression))
			return null;
		char[] chars = (" " + expression + " ").toCharArray();
		SqlItem[] items = seperateCharsToItems(chars, 1, chars.length - 2);
		for (SqlItem item : items) {
			correctType(item);
		}
		if (debugMode)
			for (SqlItem item : items)
				System.out.print(item.getDebugInfo(0));// NOSONAR
		return join(true, null, items);
	}

	/** Separate chars to Items list */
	SqlItem[] seperateCharsToItems(char[] chars, int start, int end) {
		List<SqlItem> items = new ArrayList<SqlItem>();
		SearchResult result = findFirstResult(chars, start, end);
		while (result != null) {
			items.add(result.item);
			result = findFirstResult(chars, result.leftStart, result.leftEnd);
		}
		return items.toArray(new SqlItem[items.size()]);
	}

	/** if is U type, use this method to correct type */
	void correctType(SqlItem item) {
		if (item.type == 'U') {// correct Unknown type to other type
			String valueStr = (String) item.value;
			String valueUpcase = valueStr.toUpperCase();
			// check is function
			if (valueUpcase != null && StrUtils.startsWithIgnoreCase(valueUpcase, Dialect.UNIVERSAL_FUNCTION_PREFIX)
					&& functionMap.containsKey(valueUpcase.substring(Dialect.UNIVERSAL_FUNCTION_PREFIX.length()))) {
				item.type = 'F';
				item.value = valueStr.substring(Dialect.UNIVERSAL_FUNCTION_PREFIX.length());
				item.priority = 2;
			}
			if (item.type == 'U')// still not found
				if (",".equals(valueStr))
					// is Long able?
					item.setTypeAndValue(',', valueStr);
				else
					item.setTypeAndValue('S', valueStr);
		}
		if (item.subItems != null)
			for (SqlItem t : item.subItems)
				correctType(t);
	}

	/**
	 * Find first item and store left start and left end position in
	 * SearchResult
	 */
	SearchResult findFirstResult(char[] chars, int start, int end) {
		if (start > end)
			return null;
		boolean letters = false;
		StringBuilder sb = new StringBuilder();
		for (int i = start; i <= end; i++) {
			if (!letters) {// no letters found

				if (chars[i] == ' ') {
					SqlItem item = new SqlItem();
					item.type = 'S';
					item.value = " ";
					return new SearchResult(item, i + 1, end);
				}

				if (chars[i] == '?') {
					SqlItem item = new SqlItem();
					item.type = 'S';
					item.value = "?";
					return new SearchResult(item, i + 1, end);
				}

				if (chars[i] == '\'') {
					for (int j = i + 1; j <= end; j++) {
						if (chars[j] == '\'' && chars[j - 1] != '\\') {
							SqlItem item = new SqlItem();
							item.type = 'S';
							item.value = sb.insert(0, '\'').append('\'').toString();
							return new SearchResult(item, j + 1, end);
						} else
							sb.append(chars[j]);
					}
					throw new DialectException("Miss right ' charactor in expression.");
				} else if (chars[i] == '(') {
					int count = 1;
					boolean inString = false;
					for (int j = i + 1; j <= end; j++) {
						if (!inString) {
							if (chars[j] == '(')
								count++;
							else if (chars[j] == ')') {
								count--;
								if (count == 0) {
									SqlItem[] subItems = seperateCharsToItems(chars, i + 1, j - 1);
									SqlItem item = new SqlItem();
									item.type = '(';
									item.subItems = subItems;
									return new SearchResult(item, j + 1, end);
								}
							} else if (chars[j] == '\'') {
								inString = true;
							}
						} else {
							if (chars[j] == '\'' && chars[j - 1] != '\\') {
								inString = false;
							}
						}
					}
					throw new DialectException("Miss right ) charactor in SQL.");
				} else if (chars[i] > ' ') {
					letters = true;
					sb.append(chars[i]);
				}
			} else {// letters found
				if (chars[i] == '?' || chars[i] == '\'' || chars[i] == '(' || chars[i] < ' '
						|| isLetterNumber(chars[i]) != isLetterNumber(chars[i - 1])) {
					SqlItem item = new SqlItem();
					item.type = 'U';
					item.value = sb.toString();
					return new SearchResult(item, i, end);
				} else {
					sb.append(chars[i]);
				}
			}
		}
		if (sb.length() > 0) {
			SqlItem item = new SqlItem();
			item.type = 'U';
			item.value = sb.toString();
			return new SearchResult(item, end + 1, end);
		} else
			return null;
	}

	/**
	 * Join items list into one String, if function is null, join as String,
	 * otherwise treat as function parameters
	 */
	String join(boolean isTopLevel, SqlItem function, SqlItem[] items) {
		int pos = 0;
		for (SqlItem item : items) {
			if (item.subItems != null) {
				String value;
				if (pos > 0 && items[pos - 1] != null && items[pos - 1].type == 'F')
					// join as parameters
					value = join(false, items[pos - 1], item.subItems);
				else
					value = join(false, null, item.subItems); // join as string
				item.type = 'S';
				item.value = value;
				item.subItems = null;
			}
			pos++;
		} // now there is no subItems

		if (function != null) {
			List<String> l = new ArrayList<String>();
			for (SqlItem item : items) {
				if (item.type != '0')
					l.add((String) item.value);
			}
			return doFunction(function, l.toArray(new String[l.size()]));
		}

		StringBuilder sb = new StringBuilder();
		if (!isTopLevel)
			sb.append("(");
		for (SqlItem item : items)
			if (item.type != '0') {
				sb.append(item.value);
			}
		if (!isTopLevel)
			sb.append(")");
		return sb.toString();
	}

	private static String doFunction(SqlItem function, String... params) {
		function.type = '0';
		StringBuilder sb = new StringBuilder();
		sb.append("#");
		sb.append(function.value);
		sb.append("(");
		boolean first = true;
		for (int i = 0; i < params.length; i++) {
			 
			sb.append( params[i]); 
		}
		sb.append(")");
		return sb.toString();
	}

	public static void deleteItem(SqlItem item) {
		if (item != null)
			item.type = '0';
	}

	public static void deleteItem(SqlItem lastItem, SqlItem nextItem) {
		if (lastItem != null)
			lastItem.type = '0';
		if (nextItem != null)
			nextItem.type = '0';
	}

	// ==================String Utils below======================

	public static boolean isLetterNumber(char c) {
		return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || c == '_' || c == '.'
				|| c == '@' || c == '#' || c == '$' || c == '+' || c == '-';
	}

}