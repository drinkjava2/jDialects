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
 * DataSourceManager determine how to get or release connection from DataSource,
 * it can be different transaction strategies like JDBC/SpringManaged/JTA..
 * 
 * @author Yong Zhu
 */
public interface DataSourceManager {

	/**
	 * Get a connection from DataSource or from ThreadLocal variant in side of a
	 * transaction, determined by DataSourceManager strategy
	 * 
	 * @param dataSource
	 * @return Connection instance
	 * @throws SQLException
	 */
	public Connection getConnection(DataSource ds) throws SQLException;

	/**
	 * Close a connection directly or release it to ThreadLocal variant for
	 * transaction purpose, determined by DataSourceManager strategy
	 * 
	 * @param con
	 * @param dataSource 
	 * @throws SQLException 
	 */
	public void releaseConnection(Connection conn, DataSource ds) throws SQLException ;

}
