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
package com.github.drinkjava2.jdialects.tinyjdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import javax.sql.DataSource;

import com.github.drinkjava2.jdialects.DialectLogger;

/**
 * A tiny JDBC tool to access database
 * 
 *
 * @author Yong Zhu
 * @version 1.0.0
 */
public class TinyJdbc {
	private static DialectLogger logger = DialectLogger.getLog(TinyJdbc.class);
	public static boolean show_sql = false;

	private static void loggerOutputSql(String sql) {
		if (show_sql)
			logger.info(sql);
	}

	private static ThreadLocal<ArrayList<Object>> paraCache = new ThreadLocal<ArrayList<Object>>() {
		@Override
		protected ArrayList<Object> initialValue() {
			return new ArrayList<>();
		}
	};

	/**
	 * Clear cache first, then return a empty string and cache parameters in
	 * thread local for SQL use
	 */
	public static String P0(Object... parameters) {
		paraCache.get().clear();
		return P(parameters);
	}

	/**
	 * Return a empty string and cache parameters in thread local for SQL use
	 */
	public static String P(Object... parameters) {
		for (Object o : parameters)
			paraCache.get().add(o);
		return "";
	}

	private static void closeResources(ResultSet rs, PreparedStatement pst) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				TinyJdbcException.throwEX(e, e.getMessage());
			}
		}
		try {
			if (pst != null)
				pst.close();
		} catch (SQLException e) {
			TinyJdbcException.throwEX(e, e.getMessage());
		}
	}

	public static Connection getConnection(DataSource datasource) {
		try {
			return datasource.getConnection();
		} catch (SQLException e) {
			TinyJdbcException.throwEX(e, "Can not get connection from datasource");
			return null;
		}
	}

	public static void closeConnection(Connection con) {
		try {
			if (con != null && !con.isClosed()) {
				try {
					con.close();
				} catch (SQLException e) {
					TinyJdbcException.throwEX(e, e.getMessage());
				}
			}
		} catch (SQLException e) {
			TinyJdbcException.throwEX(e, e.getMessage());
		}
	}

	private static TinySqlAndParameters prepareSQLandParameters(String... sqls) {
		try {
			StringBuilder sb = new StringBuilder("");
			for (String str : sqls) {
				sb.append(str);
			}
			TinySqlAndParameters sp = new TinySqlAndParameters();
			String sql = sb.toString();
			sp.setSql(sql);

			ArrayList<Object> list = paraCache.get();
			sp.setParameters(list.toArray(new Object[list.size()]));
			return sp;
		} finally {
			paraCache.get().clear();
		}
	}

	public static TinyResult executeQuery(Connection con, String... sqls) {// NOSONAR
		TinySqlAndParameters pairs = prepareSQLandParameters(sqls);
		loggerOutputSql(pairs.getSql());
		ResultSet rs = null;
		PreparedStatement pst = null;
		try {
			int i = 1;
			pst = con.prepareStatement(pairs.getSql());// NOSONAR
			for (Object obj : pairs.getParameters())
				pst.setObject(i++, obj);
			rs = pst.executeQuery();
			TinyResult r = ResultSupport.toResult(rs);
			return r;
		} catch (SQLException e) {
			TinyJdbcException.throwEX(e, e.getMessage());
		} finally {
			closeResources(rs, pst);
		}
		return null;
	}

	// =============================old ============================
	public static Integer queryForInteger(Connection con, String... sqls) {
		TinyResult rst = executeQuery(con, sqls);
		if (rst != null && rst.getRowCount() == 1) {
			Map<?, ?> row = rst.getRows()[0];
			Object s = row.get(row.keySet().iterator().next());
			if (s == null)
				return 0;
			return Integer.parseInt("" + s);
		} else
			TinyJdbcException.throwEX("TinyJdbc queryForObject error: null or multiple lines found for sql:" + sqls);
		return null;
	}

	public static Object queryForObject(Connection con, String... sqls) {
		TinyResult rst = executeQuery(con, sqls);
		if (rst != null && rst.getRowCount() == 1) {
			Map<?, ?> row = rst.getRows()[0];
			return row.get(row.keySet().iterator().next());
		} else
			TinyJdbcException.throwEX("TinyJdbc queryForObject error: null or multiple lines found for sql:" + sqls);
		return null;
	}

	public static String queryForString(Connection con, String... sqls) {
		return (String) queryForObject(con, sqls);
	}

	public static int executeUpdate(Connection con, String... sqls) {// NOSONAR
		TinySqlAndParameters pairs = prepareSQLandParameters(sqls);
		loggerOutputSql(pairs.getSql());
		PreparedStatement pst = null;
		try {
			int i = 1;
			pst = con.prepareStatement(pairs.getSql());// NOSONAR
			for (Object obj : pairs.getParameters())
				pst.setObject(i++, obj);
			int count = pst.executeUpdate();
			return count;
		} catch (SQLException e) {
			TinyJdbcException.throwEX(e, e.getMessage());
		} finally {
			closeResources(null, pst);
		}
		return 0;
	}

	public static boolean executeQuiet(Connection con, String... sqls) {
		try {
			return execute(con, sqls);
		} catch (Exception e) {
			TinyJdbcException.eatException(e);
			return false;
		}
	}

	public static void executeManySqls(Connection con, String... sqls) {// NOSONAR
		for (String str : sqls)
			execute(con, str);
	}

	public static void executeQuietManySqls(Connection con, String... sqls) {// NOSONAR
		for (String str : sqls)
			executeQuiet(con, str);
	}

	public static boolean execute(Connection con, String... sqls) {// NOSONAR
		TinySqlAndParameters pairs = prepareSQLandParameters(sqls);
		loggerOutputSql(pairs.getSql());
		PreparedStatement pst = null;
		try {
			pst = con.prepareStatement(pairs.getSql());// NOSONAR
			int i = 1;
			for (Object obj : pairs.getParameters())
				pst.setObject(i++, obj);
			boolean bl = pst.execute();
			return bl;
		} catch (SQLException e) {
			TinyJdbcException.throwEX(e, e.getMessage());
		} finally {
			closeResources(null, pst);
		}
		return false;
	}

	public static int getTableCount(Connection con) {
		PreparedStatement pst = null;
		try {
			DatabaseMetaData metaData = con.getMetaData();
			int tableCount = 0;
			ResultSet rs = metaData.getTables(con.getCatalog(), "test", null, new String[] { "TABLE" });
			while (rs.next()) {
				tableCount++;
				System.out.println(rs.getString("TABLE_NAME"));
			}
			return tableCount;
		} catch (SQLException e) {
			TinyJdbcException.throwEX(e, e.getMessage());
		} finally {
			closeResources(null, pst);
		}
		return 0;
	}

}
