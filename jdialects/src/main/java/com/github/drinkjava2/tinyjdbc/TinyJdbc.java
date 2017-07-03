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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import javax.sql.DataSource;

/**
 * A tiny JDBC tool
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class TinyJdbc {
	private static TinyLogger logger = TinyLogger.getLog(TinyJdbc.class);
	private Boolean allowShowSQL = false;
	private TinyDataSourceManager tinyDataSourceManager;
	private DataSource dataSource;

	private static ThreadLocal<ArrayList<Object>> paramCache = new ThreadLocal<ArrayList<Object>>() {
		@Override
		protected ArrayList<Object> initialValue() {
			return new ArrayList<>();
		}
	};

	public TinyJdbc() {
	}

	public TinyJdbc(DataSource ds) {
		this.dataSource = ds;
		this.tinyDataSourceManager = TinyDataSourceManager.jdbcDataSourceManager();
	}

	public TinyJdbc(DataSource ds, TinyDataSourceManager dm) {
		this.dataSource = ds;
		this.tinyDataSourceManager = dm;
	}

	// getter & Setter ========
	/** A ThreadLocal parameters cache */

	public Boolean getAllowShowSQL() {
		return allowShowSQL;
	}

	public TinyDataSourceManager getTinyDataSourceManager() {
		return tinyDataSourceManager;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public Connection getConnection() {
		return this.getTinyDataSourceManager().getConnection(this.getDataSource());
	}

	public void releaseConnection(Connection conn) {
		this.getTinyDataSourceManager().releaseConnection(conn, this.getDataSource());
	}

	public void setAllowShowSQL(Boolean allowShowSQL) {
		this.allowShowSQL = allowShowSQL;
	}

	public void setTinyDataSourceManager(TinyDataSourceManager tinyDataSourceManager) {
		this.tinyDataSourceManager = tinyDataSourceManager;
	}

	public void setDataSource(DataSource ds) {
		this.dataSource = ds;
	}

	// getter & setter end===========

	/**
	 * Clear cache first, then return a empty string and cache parameters in
	 * thread local for SQL use
	 */
	public static String param0(Object... parameters) {
		paramCache.get().clear();
		return param(parameters);
	}

	/**
	 * Return a empty string and cache parameters in thread local for SQL use
	 */
	public static String param(Object... parameters) {
		for (Object o : parameters)
			paramCache.get().add(o);
		return "";
	}

	public Integer queryForInteger(String... sqls) {
		TinyResult rst = executeQuery(sqls);
		if (rst != null && rst.getRowCount() == 1) {
			Map<?, ?> row = rst.getRows()[0];
			Object s = row.get(row.keySet().iterator().next());
			if (s == null)
				return 0;
			return Integer.parseInt("" + s);
		} else
			TinyJdbcException.throwEX("TinyJdbc queryForInteger error: null or multiple lines found for sql:" + sqls);
		return null;
	}

	public Object queryForObject(String... sqls) {
		TinyResult rst = executeQuery(sqls);
		if (rst != null && rst.getRowCount() == 1) {
			Map<?, ?> row = rst.getRows()[0];
			return row.get(row.keySet().iterator().next());
		} else
			TinyJdbcException.throwEX("TinyJdbc queryForObject error: null or multiple lines found for sql:" + sqls);
		return null;
	}

	public String queryForString(String... sqls) {
		return (String) queryForObject(sqls);
	}

	public boolean executeQuiet(String... sqls) {
		try {
			return execute(sqls);
		} catch (Exception e) {
			TinyJdbcException.eatException(e);
			return false;
		}
	}

	public void executeManySqls(String... sqls) {
		for (String str : sqls)
			execute(str);
	}

	public void executeQuietManySqls(String... sqls) {
		for (String str : sqls)
			executeQuiet(str);
	}

	public boolean execute(String... sqls) {
		TinySqlAndParams sqlAndParams = prepareSQLandParams(sqls);
		if (getAllowShowSQL())
			logger.info(sqlAndParams.getLogOutputString());
		PreparedStatement pst = null;
		Connection con = tinyDataSourceManager.getConnection(dataSource);
		SQLException exception = null;
		try {
			pst = con.prepareStatement(sqlAndParams.getSql());
			int i = 1;
			for (Object obj : sqlAndParams.getParameters())
				pst.setObject(i++, obj);
			boolean bl = pst.execute();
			return bl;
		} catch (SQLException e) {
			exception = e;
		} finally {
			closeJdbcResources(null, pst, con, exception, sqlAndParams);
		}
		return false;
	}

	public int executeUpdate(String... sqls) {
		TinySqlAndParams sqlAndParams = prepareSQLandParams(sqls);
		if (getAllowShowSQL())
			logger.info(sqlAndParams.getLogOutputString());
		PreparedStatement pst = null;
		Connection con = tinyDataSourceManager.getConnection(dataSource);
		SQLException exception = null;
		try {
			int i = 1;
			pst = con.prepareStatement(sqlAndParams.getSql());
			for (Object obj : sqlAndParams.getParameters())
				pst.setObject(i++, obj);
			int count = pst.executeUpdate();
			return count;
		} catch (SQLException e) {
			exception = e;
		} finally {
			closeJdbcResources(null, pst, con, exception, sqlAndParams);
		}
		return 0;
	}

	public TinyResult executeQuery(String... sqls) {
		TinySqlAndParams sqlAndParams = prepareSQLandParams(sqls);
		if (getAllowShowSQL())
			logger.info(sqlAndParams.getLogOutputString());
		ResultSet rs = null;
		PreparedStatement pst = null;
		Connection con = tinyDataSourceManager.getConnection(dataSource);
		SQLException exception = null;
		try {
			int i = 1;
			pst = con.prepareStatement(sqlAndParams.getSql());
			for (Object obj : sqlAndParams.getParameters())
				pst.setObject(i++, obj);
			rs = pst.executeQuery();
			TinyResult r = TinyResultSetUtils.toResult(rs);
			return r;
		} catch (SQLException e) {
			exception = e;
		} finally {
			closeJdbcResources(rs, pst, con, exception, sqlAndParams);
		}
		return null;
	}

	/**
	 * Close ResultSet, PreparedStatement, Connection resources
	 */
	private void closeJdbcResources(ResultSet rs, PreparedStatement pst, Connection con, SQLException exception,
			TinySqlAndParams sp) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				if (exception != null)
					e.setNextException(exception);
				exception = e;
			} finally {
				rs = null;
			}
		}
		if (pst != null) {
			try {
				pst.close();
			} catch (SQLException e) {
				if (exception != null)
					e.setNextException(exception);
				exception = e;
			} finally {
				pst = null;
			}
		}
		try {
			tinyDataSourceManager.releaseConnection(con, dataSource);
		} catch (Exception e) {
			TinyJdbcException.throwEX(e, "Fail to release connection, " + sp.getLogOutputString());
		}
		if (exception != null)
			TinyJdbcException.throwEX(exception, "SQLException found, " + sp.getLogOutputString());
	}

	private static TinySqlAndParams prepareSQLandParams(String... sqls) {
		try {
			StringBuilder sb = new StringBuilder("");
			for (String str : sqls) {
				sb.append(str);
			}
			TinySqlAndParams sp = new TinySqlAndParams();
			String sql = sb.toString();
			sp.setSql(sql);

			ArrayList<Object> list = paramCache.get();
			sp.setParameters(list.toArray(new Object[list.size()]));
			return sp;
		} finally {
			paramCache.get().clear();
		}
	}

}
