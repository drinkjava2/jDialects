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
import com.github.drinkjava2.jdialects.FunctionUtils;
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
	public static TranslateUtil instance = new TranslateUtil();
	private static boolean debugMode = true;
	static Map<String, Integer> functionMap = new HashMap<String, Integer>();

	static {
		functionMap.put("ABS", 2);
		functionMap.put("ACOS", 2);
		functionMap.put("ASCII", 2);
		functionMap.put("ASIN", 2);
		functionMap.put("ATAN", 2);
		functionMap.put("ATAN2", 2);
		functionMap.put("AVG", 2);
		functionMap.put("BIN", 2);
		functionMap.put("BIT_LENGTH", 2);
		functionMap.put("CAST", 2);
		functionMap.put("CEIL", 2);
		functionMap.put("CEILING", 2);
		functionMap.put("CHAR", 2);
		functionMap.put("CHAR_LENGTH", 2);
		functionMap.put("CHARACTER_LENGTH", 2);
		functionMap.put("CHR", 2);
		functionMap.put("COALESCE", 2);
		functionMap.put("CONCAT", 2);
		functionMap.put("COS", 2);
		functionMap.put("COT", 2);
		functionMap.put("COUNT", 2);
		functionMap.put("CRC32", 2);
		functionMap.put("CURDATE", 2);
		functionMap.put("CURRENT_DATE", 2);
		functionMap.put("CURRENT_SCHEMA", 2);
		functionMap.put("CURRENT_TIME", 2);
		functionMap.put("CURRENT_TIMESTAMP", 2);
		functionMap.put("CURRENT_USER", 2);
		functionMap.put("CURTIME", 2);
		functionMap.put("DATE", 2);
		functionMap.put("DATE_TRUNC", 2);
		functionMap.put("DATEDIFF", 2);
		functionMap.put("DAY", 2);
		functionMap.put("DAYNAME", 2);
		functionMap.put("DAYOFMONTH", 2);
		functionMap.put("DAYOFWEEK", 2);
		functionMap.put("DAYOFYEAR", 2);
		functionMap.put("DEGREES", 2);
		functionMap.put("ENCRYPT", 2);
		functionMap.put("EXP", 2);
		functionMap.put("EXTRACT", 2);
		functionMap.put("FLOOR", 2);
		functionMap.put("FROM_DAYS", 2);
		functionMap.put("HEX", 2);
		functionMap.put("HOUR", 2);
		functionMap.put("INITCAP", 2);
		functionMap.put("INSTR", 2);
		functionMap.put("ISNULL", 2);
		functionMap.put("LAST_DAY", 2);
		functionMap.put("LCASE", 2);
		functionMap.put("LEFT", 2);
		functionMap.put("LEN", 2);
		functionMap.put("LENGTH", 2);
		functionMap.put("LN", 2);
		functionMap.put("LOCALTIME", 2);
		functionMap.put("LOCALTIMESTAMP", 2);
		functionMap.put("LOCATE", 2);
		functionMap.put("LOG", 2);
		functionMap.put("LOG10", 2);
		functionMap.put("LOG2", 2);
		functionMap.put("LOWER", 2);
		functionMap.put("LPAD", 2);
		functionMap.put("LTRIM", 2);
		functionMap.put("MAX", 2);
		functionMap.put("MD5", 2);
		functionMap.put("MICROSECOND", 2);
		functionMap.put("MIN", 2);
		functionMap.put("MINUTE", 2);
		functionMap.put("MOD", 2);
		functionMap.put("MONTH", 2);
		functionMap.put("MONTHNAME", 2);
		functionMap.put("NOW", 2);
		functionMap.put("NULLIF", 2);
		functionMap.put("NVL", 2);
		functionMap.put("OCT", 2);
		functionMap.put("OCTET_LENGTH", 2);
		functionMap.put("PI", 2);
		functionMap.put("POSITION", 2);
		functionMap.put("POWER", 2);
		functionMap.put("QUARTER", 2);
		functionMap.put("RADIANS", 2);
		functionMap.put("RAND", 2);
		functionMap.put("RANDOM", 2);
		functionMap.put("REPLACE", 2);
		functionMap.put("REVERSE", 2);
		functionMap.put("RIGHT", 2);
		functionMap.put("ROUND", 2);
		functionMap.put("RPAD", 2);
		functionMap.put("RTRIM", 2);
		functionMap.put("SECOND", 2);
		functionMap.put("SESSION_USER", 2);
		functionMap.put("SIGN", 2);
		functionMap.put("SIN", 2);
		functionMap.put("SOUNDEX", 2);
		functionMap.put("SPACE", 2);
		functionMap.put("SQRT", 2);
		functionMap.put("STDDEV", 2);
		functionMap.put("STR", 2);
		functionMap.put("SUBSTR", 2);
		functionMap.put("SUBSTRING", 2);
		functionMap.put("SUM", 2);
		functionMap.put("SYSDATE", 2);
		functionMap.put("TAN", 2);
		functionMap.put("TIME", 2);
		functionMap.put("TIMEDIFF", 2);
		functionMap.put("TIMESTAMP", 2);
		functionMap.put("TO_CHAR", 2);
		functionMap.put("TO_DATE", 2);
		functionMap.put("TO_DAYS", 2);
		functionMap.put("TO_TIMESTAMP", 2);
		functionMap.put("TRANSLATE", 2);
		functionMap.put("TRIM", 2);
		functionMap.put("TRUNC", 2);
		functionMap.put("UCASE", 2);
		functionMap.put("UNHEX", 2);
		functionMap.put("UPPER", 2);
		functionMap.put("USER", 2);
		functionMap.put("VARIANCE", 2);
		functionMap.put("WEEK", 2);
		functionMap.put("WEEKDAY", 2);
		functionMap.put("WEEKOFYEAR", 2);
		functionMap.put("YEAR", 2);
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
	 * @param bean Expression allow direct use only 1 bean's fields
	 * @param keyWords The preset key words key-value map
	 * @param expression The expression
	 * @param params The expression parameter array
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
		return join(d, true, null, items);
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
	 * Find first item and store left start and left end position in SearchResult
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
					throw new DialectException("Miss right ' charactor in SQL.");
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
				if (chars[i] == '?' || chars[i] == '\'' || chars[i] == '(' || chars[i] <=' '
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
	String join(Dialect d, boolean isTopLevel, SqlItem function, SqlItem[] items) {
		int pos = 0;
		for (SqlItem item : items) {
			if (item.subItems != null) {
				String value;
				if (pos > 0 && items[pos - 1] != null && items[pos - 1].type == 'F')
					// join as parameters
					value = join(d, false, items[pos - 1], item.subItems);
				else
					value = join(d, false, null, item.subItems); // join as string
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
			return renderFunction(d, function, l.toArray(new String[l.size()]));
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

	private static String renderFunction(Dialect d, SqlItem function, String... params) {
		System.out.println();
		System.out.print("Function name="+function.value+"  params=");
		for (String param : params) {
			System.out.print("/"+param);
		}
		System.out.println();
		
		
		function.type = '0';
		List<String> l = new ArrayList<String>();
		String current = "";
		for (String param : params) {
			if (",".equals(param)) {
				l.add(current);
				current = "";
			} else
				current += param;// NOSONAR
		}
		String lastValue = current.trim();
		if (lastValue.length() > 0)
			l.add(current);
		System.out.println("l.size="+l.size());
		String[] lArr=l.toArray(new String[l.size()]);
		System.out.println("lArr length="+lArr.length);
		String result= FunctionUtils.render(d, (String) function.value, lArr );
		System.out.println("result="+result);
		return result;
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