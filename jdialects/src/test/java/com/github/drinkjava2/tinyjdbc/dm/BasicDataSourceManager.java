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
package com.github.drinkjava2.tinyjdbc.dm;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * BasicDataSourceManager is the traditional DataSource dealing strategy, get
 * connection directly from a DataSource, and close connection directly
 * 
 * @author Yong Zhu
 */
public class BasicDataSourceManager implements DataSourceManager {
	private static class InnerBasicDataSourceManager {
		private static final DataSourceManager INSTANCE = new BasicDataSourceManager();
	}

	/**
	 * @return A singleton instance of TinyAutoCommitDataSourceManager
	 */
	public static final DataSourceManager instance() {
		return InnerBasicDataSourceManager.INSTANCE;
	}

	/**
	 * Get a Connection directly from DataSource
	 * 
	 * @throws SQLException
	 */
	public Connection getConnection(DataSource dataSource) throws SQLException {
		return dataSource.getConnection();
	}

	/**
	 * If Connection not null, close it directly
	 * 
	 * @throws SQLException
	 */
	public void releaseConnection(Connection conn, DataSource dataSource) throws SQLException {
		if (conn != null) {
			conn.close();
		}
	}

}
