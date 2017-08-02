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
package com.github.drinkjava2.tinyjdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.github.drinkjava2.tinyjdbc.dm.BasicDataSourceManager;
import com.github.drinkjava2.tinyjdbc.dm.DataSourceManager;

/**
 * A tiny JDBC tool support Inline-Style query, no any 3rd party dependency,
 * usually used for unit test, for product developing, suggest use DbUtils-Pro
 * which also support inline-style query
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TinyJdbc extends TinyJdbcUtils {
	private DataSourceManager dm;
	private DataSource ds;
	protected static ThreadLocal<SQLException> threadLocalLastSQLException = new ThreadLocal<SQLException>();

	public TinyJdbc(DataSource ds) {
		this.ds = ds;
		this.dm = BasicDataSourceManager.instance();
	}

	public TinyJdbc(DataSource ds, DataSourceManager dm) {
		this.ds = ds;
		this.dm = dm;
	}

	// getter & Setter ========
	public DataSourceManager getDataSourceManager() {
		return dm;
	}

	public DataSource getDataSource() {
		return ds;
	}

	// getter & setter end===========

	public Integer queryForInteger(String sql, Object... params) {
		return this.queryForObject(Integer.class, sql, params);
	}

	public Integer inlineQueryForInteger(String... sqls) {
		return this.inlineQueryForObject(Integer.class, sqls);
	}

	public Long queryForLong(String sql, Object... params) {
		return this.queryForObject(Long.class, sql, params);
	}

	public Long inlineQueryForLong(String... sqls) {
		return this.inlineQueryForObject(Long.class, sqls);
	}

	public String queryForString(String sql, Object... params) {
		return this.queryForObject(String.class, sql, params);
	}

	public String inlineQueryForString(String... sqls) {
		return this.inlineQueryForObject(String.class, sqls);
	}

	public <T> T queryForObject(Class<T> requiredType, String sql, Object... params) {
		Connection conn = getConnection();
		try {
			return queryForObject(conn, requiredType, sql, params);
		} catch (SQLException e) {
			return this.linkException(requiredType, e);
		} finally {
			releaseConnection(conn);
		}
	}

	public <T> T inlineQueryForObject(Class<T> requiredType, String... sqls) {
		Connection conn = getConnection();
		try {
			return inlineQueryForObject(conn, requiredType, sqls);
		} catch (SQLException e) {
			return this.linkException(requiredType, e);
		} finally {
			releaseConnection(conn);
		}
	}

	public TinyResultSet query(String sql, Object... params) {
		Connection conn = getConnection();
		try {
			return query(conn, sql, params);
		} catch (SQLException e) {
			return this.linkException(TinyResultSet.class, e);
		} finally {
			releaseConnection(conn);
		}
	}

	public TinyResultSet inlineQuery(String... sqls) {
		Connection conn = getConnection();
		try {
			return inlineQuery(conn, sqls);
		} catch (SQLException e) {
			return this.linkException(TinyResultSet.class, e);
		} finally {
			releaseConnection(conn);
		}
	}

	public <T> TinyResultSet query(Class<T> requiredType, String sql, Object... params) {
		Connection conn = getConnection();
		try {
			return query(conn, requiredType, sql, params);
		} catch (SQLException e) {
			return this.linkException(TinyResultSet.class, e);
		} finally {
			releaseConnection(conn);
		}
	}

	public <T> TinyResultSet inlineQuery(Class<T> requiredType, String... sqls) {
		Connection conn = getConnection();
		try {
			return inlineQuery(conn, requiredType, sqls);
		} catch (SQLException e) {
			return this.linkException(TinyResultSet.class, e);
		} finally {
			releaseConnection(conn);
		}
	}

	public void executeNoParamSqls(String... sqls) {
		for (String str : sqls)
			execute(str);
	}

	public void executeNoParamSqlsQuiet(String... sqls) {
		for (String str : sqls)
			try {
				execute(str);
			} catch (Exception e) {// do nothing
			}
	}

	public boolean execute(String sql, Object... params) {
		Connection conn = getConnection();
		try {
			return execute(conn, sql, params);
		} catch (SQLException e) {
			this.linkException(e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public boolean inlineExecute(String... sqls) {
		Connection conn = getConnection();
		try {
			return inlineExecute(conn, sqls);
		} catch (SQLException e) {
			this.linkException(e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public int executeUpdate(String sql, Object... params) {
		Connection conn = getConnection();
		try {
			return executeUpdate(conn, sql, params);
		} catch (SQLException e) {
			this.linkException(e);
		} finally {
			releaseConnection(conn);
		}
		return 0;
	}

	public int inlineExecuteUpdate(String... sqls) {
		Connection conn = getConnection();
		try {
			return inlineExecuteUpdate(conn, sqls);
		} catch (SQLException e) {
			this.linkException(e);
		} finally {
			releaseConnection(conn);
		}
		return 0;
	}

	public boolean executeQuiet(String sql, Object... params) {
		try {
			return execute(sql, params);
		} catch (Exception e) {
			TinyRuntimeException.eatException(e);
			return false;
		}
	}

	public boolean inlineExecuteQuiet(String... sqls) {
		try {
			return inlineExecute(sqls);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Close connection or release to DataSource pool, catch SQLException and
	 * throw TinyRuntimeException
	 */
	public void releaseConnection(Connection conn) {
		SQLException lastException = threadLocalLastSQLException.get();
		threadLocalLastSQLException.remove();
		if (conn == null)
			throw new TinyRuntimeException("Can not release a null Connection.");
		SQLException releaseException = null;
		try {
			dm.releaseConnection(conn, ds);
		} catch (SQLException e) {
			releaseException = e;
		}
		if (releaseException != null) {
			if (lastException != null)
				throw new TinyRuntimeException("Fail to release Connection " + lastException.getMessage(),
						releaseException);
			throw new TinyRuntimeException("Fail to release Connection.", releaseException);
		} else if (lastException != null)
			throw new TinyRuntimeException(lastException);
	}

	/**
	 * Get a connection from DataSource or from DataSource pool, catch
	 * SQLException and throw TinyRuntimeException
	 */
	public Connection getConnection() {
		try {
			return dm.getConnection(ds);
		} catch (SQLException e) {
			throw new TinyRuntimeException("Fail to obtain a connection", e);
		}
	}

	public Object linkException(SQLException e) {
		SQLException lastException = threadLocalLastSQLException.get();
		if (lastException != null)
			e.setNextException(lastException);
		threadLocalLastSQLException.set(e);
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T linkException(Class<T> requiredType, SQLException e) {
		return (T) linkException(e);
	}

}
