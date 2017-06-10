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

package test.utils.tinyjdbc;

/**
 * SqlAndParameters class used by SqlBox for store sql and parameters in
 * threadlocal
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0
 */
public class TinySqlAndParameters {
	private String sql;

	/**
	 * Sql parameters
	 */
	private Object[] parameters;

	public TinySqlAndParameters() {
		// default Constructor
	}

	public TinySqlAndParameters(String sql, Object[] parameters) {
		this.sql = sql;
		this.parameters = parameters;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Object[] getParameters() {
		for (Object object : parameters)
			System.out.print(object + ",");
		System.out.println();
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}

}
