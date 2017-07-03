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
import java.sql.Types;
import java.util.Arrays;
import java.util.Map;

/**
 * A tiny JDBC tool
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class TinyJdbcUtils {
	private static TinyLogger logger = TinyLogger.getLog(TinyJdbcUtils.class);
	private static Boolean allowShowSQL = false;

	public static Boolean getAllowShowSQL() {
		return allowShowSQL;
	}

	public static void setAllowShowSQL(Boolean allowShowSQL) {
		TinyJdbcUtils.allowShowSQL = allowShowSQL;
	}

	public static Integer hotQueryForInteger(Connection conn, String sql, Object... params) {
		TinyResult rst = hotQuery(conn, sql, params);
		if (rst != null && rst.getRowCount() == 1) {
			Map<?, ?> row = rst.getRows()[0];
			Object s = row.get(row.keySet().iterator().next());
			if (s == null)
				return 0;
			return Integer.parseInt("" + s);
		} else
			TinyJdbcException.throwEX(
					"hotQueryForInteger error: null or multiple lines found, " + sqlAndParamsPrintInfo(sql, params));
		return null;
	}

	public static Object hotQueryForObject(Connection conn, String sql, Object... params) {
		TinyResult rst = hotQuery(conn, sql, params);
		if (rst != null && rst.getRowCount() == 1) {
			Map<?, ?> row = rst.getRows()[0];
			return row.get(row.keySet().iterator().next());
		} else
			TinyJdbcException.throwEX("TinyJdbc queryForObject error: null or multiple lines found, "
					+ sqlAndParamsPrintInfo(sql, params));
		return null;
	}

	public static String hotQueryForString(Connection conn, String sql, Object... params) {
		return (String) hotQueryForObject(conn, sql, params);
	}

	public static boolean hotExecuteQuiet(Connection conn, String sql, Object... params) {
		try {
			return hotExecute(conn, sql, params);
		} catch (Exception e) {
			TinyJdbcException.eatException(e);
			return false;
		}
	}

	public static void hotExecuteManySqls(Connection conn, String... sqls) {
		for (String str : sqls)
			hotExecute(conn, str);
	}

	public static void hotExecuteQuietManySqls(Connection conn, String... sqls) {
		for (String str : sqls)
			hotExecuteQuiet(conn, str);
	}

	public static boolean hotExecute(Connection conn, String sql, Object... params) {
		if (getAllowShowSQL())
			logger.info(sqlAndParamsPrintInfo(sql, params));
		checkConnAndSqlNotNull(conn, sql);
		PreparedStatement pst = null;
		SQLException exception = null;
		try {
			pst = conn.prepareStatement(sql);
			fillStatement(pst, params);
			return pst.execute();
		} catch (SQLException e) {
			exception = e;
		} finally {
			closeRSandPST(null, pst, exception, sql, params);
		}
		return false;
	}

	public static int hotExecuteUpdate(Connection conn, String sql, Object... params) {
		if (getAllowShowSQL())
			logger.info(sqlAndParamsPrintInfo(sql, params));
		checkConnAndSqlNotNull(conn, sql);
		PreparedStatement pst = null;
		SQLException exception = null;
		try {
			pst = conn.prepareStatement(sql);
			fillStatement(pst, params);
			return pst.executeUpdate();
		} catch (SQLException e) {
			exception = e;
		} finally {
			closeRSandPST(null, pst, exception, sql, params);
		}
		return 0;
	}

	public static TinyResult hotQuery(Connection conn, String sql, Object... params) {
		if (getAllowShowSQL())
			logger.info(sqlAndParamsPrintInfo(sql, params));
		checkConnAndSqlNotNull(conn, sql);
		ResultSet rs = null;
		PreparedStatement pst = null;
		SQLException exception = null;
		try {
			pst = conn.prepareStatement(sql);
			fillStatement(pst, params);
			rs = pst.executeQuery();
			return TinyResultSetUtils.toResult(rs);
		} catch (SQLException e) {
			exception = e;
		} finally {
			closeRSandPST(rs, pst, exception, sql, params);
		}
		return null;
	}

	public static TinyResult hotInsertBatch(Connection conn, String sql, Object[][] params) {
		if (params == null)
			TinyJdbcException.throwEX("Null parameters. If parameters aren't need, pass an empty array.");
		if (getAllowShowSQL())
			logger.info(sqlAndParamsPrintInfo(sql, params[0]));
		checkConnAndSqlNotNull(conn, sql);

		PreparedStatement pst = null;
		SQLException exception = null;
		try {
			pst = conn.prepareStatement(sql);
			for (int i = 0; i < params.length; i++) {
				fillStatement(pst, params[i]);
				pst.addBatch();
			}
			pst.executeBatch();
			ResultSet rs = pst.getGeneratedKeys();
			return TinyResultSetUtils.toResult(rs);
		} catch (SQLException e) {
			exception = e;
		} finally {
			closeRSandPST(null, pst, exception, sql);
		}
		return null;
	}

	public static int[] hotExecuteBatch(Connection conn, String sql, Object[][] params) {
		if (params == null)
			TinyJdbcException.throwEX("Null parameters. If parameters aren't need, pass an empty array.");
		if (getAllowShowSQL())
			logger.info(sqlAndParamsPrintInfo(sql, params[0]));
		checkConnAndSqlNotNull(conn, sql);
		PreparedStatement pst = null;
		SQLException exception = null;
		int[] rows = null;
		try {
			pst = conn.prepareStatement(sql);
			for (int i = 0; i < params.length; i++) {
				fillStatement(pst, params[i]);
				pst.addBatch();
			}
			return pst.executeBatch();
		} catch (SQLException e) {
			exception = e;
		} finally {
			closeRSandPST(null, pst, exception, sql);
		}
		return rows;
	}

	private static void fillStatement(PreparedStatement stmt, Object... params) throws SQLException {
		if (params == null)
			return;
		for (int i = 0; i < params.length; i++) {
			if (params[i] != null) {
				stmt.setObject(i + 1, params[i]);
			} else {
				// VARCHAR works with many drivers regardless
				// of the actual column type. Oddly, NULL and
				// OTHER don't work with Oracle's drivers.
				int sqlType = Types.VARCHAR;
				stmt.setNull(i + 1, sqlType);
			}
		}
	}

	private static void closeRSandPST(ResultSet rs, PreparedStatement pst, SQLException exception, String sql,
			Object... params) {
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
		if (exception != null)
			TinyJdbcException.throwEX(exception, "SQLException found, " + sqlAndParamsPrintInfo(sql, params));
	}

	private static void checkConnAndSqlNotNull(Connection conn, String sql) {
		if (conn == null)
			TinyJdbcException.throwEX("Null connection");
		if (sql == null)
			TinyJdbcException.throwEX("Null SQL statement");
	}

	private static String sqlAndParamsPrintInfo(String sql, Object... params) {
		StringBuffer msg = new StringBuffer(" SQL: ").append(sql).append("\r\n Parameters: ");
		if (params == null || params.length == 0)
			msg.append("[]");
		else
			msg.append(Arrays.deepToString(params));
		return msg.toString();
	}
}
