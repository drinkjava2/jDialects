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

import com.github.drinkjava2.tinyjdbc.inline.InlineSupport;
import com.github.drinkjava2.tinyjdbc.inline.SqlAndParams;

/**
 * A tiny JDBC utility tool, all methods do not close connection, you need close
 * it by yourself
 * 
 * 
 * <pre>
 * Usuage: 
        Connection conn = ds.getConnection();// Or get from somewhere else like a ThreadLocal variant
		try {
			TinyJdbcUtils.execute(conn, sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    releaseConnection(conn,ds) // Must close connection or release to somewhere which in charge of close it
		}
 * 
 * </pre>
 *
 * @author Yong Zhu
 *
 * @since 1.0.0
 */
public abstract class TinyJdbcUtils extends InlineSupport {
	private static TinyLogger logger = TinyLogger.getLog(TinyJdbcUtils.class);
	private static Boolean allowShowSQL = false;

	public static Boolean getAllowShowSQL() {
		return allowShowSQL;
	}

	public static void setAllowShowSQL(Boolean allowShowSQL) {
		TinyJdbcUtils.allowShowSQL = allowShowSQL;
	}

	public static Integer queryForInteger(Connection conn, String sql, Object... params) throws SQLException {
		return queryForObject(conn, Integer.class, sql, params);
	}

	public static Integer inlineQueryForInteger(Connection conn, String... sqls) throws SQLException {
		return inlineQueryForObject(conn, Integer.class, sqls);
	}

	public static Long queryForLong(Connection conn, String sql, Object... params) throws SQLException {
		return queryForObject(conn, Long.class, sql, params);
	}

	public static Long inlineQueryForLong(Connection conn, String... sqls) throws SQLException {
		return inlineQueryForObject(conn, Long.class, sqls);
	}

	public static String queryForString(Connection conn, String sql, Object... params) throws SQLException {
		return queryForObject(conn, String.class, sql, params);
	}

	public static String inlineQueryForString(Connection conn, String... sqls) throws SQLException {
		return inlineQueryForObject(conn, String.class, sqls);
	}

	@SuppressWarnings("unchecked")
	public static <T> T queryForObject(Connection conn, Class<T> requiredType, String sql, Object... params)
			throws SQLException {
		TinyResultSet rst = query(conn, requiredType, sql, params);
		if (rst != null && rst.getRowCount() == 1) {
			Map<?, ?> row = rst.getRows()[0];
			return (T) row.get(row.keySet().iterator().next());
		} else
			throw new SQLException("Require an Object value but a null or more than 1 record returned");
	}

	public static <T> T inlineQueryForObject(Connection conn, Class<T> requiredType, String... sqls)
			throws SQLException {
		SqlAndParams sp = buildSqlAndParams(sqls);
		return queryForObject(conn, requiredType, sp.getSql(), sp.getParams());
	}

	public static void executeNonParamSqls(Connection conn, String... sqls) throws SQLException {
		for (String str : sqls)
			execute(conn, str);
	}

	public static boolean execute(Connection conn, String sql, Object... params) throws SQLException {
		if (getAllowShowSQL())
			logger.info(buildSqlAndParamsDebugInfo(sql, params));
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

	public static boolean inlineExecute(Connection conn, String... sqls) throws SQLException {
		SqlAndParams sp = buildSqlAndParams(sqls);
		return execute(conn, sp.getSql(), sp.getParams());
	}

	public static int executeUpdate(Connection conn, String sql, Object... params) throws SQLException {
		if (getAllowShowSQL())
			logger.info(buildSqlAndParamsDebugInfo(sql, params));
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

	public static int inlineExecuteUpdate(Connection conn, String... sqls) throws SQLException {
		SqlAndParams sp = buildSqlAndParams(sqls);
		return executeUpdate(conn, sp.getSql(), sp.getParams());
	}

	public static <T> TinyResultSet query(Connection conn, Class<T> requiredType, String sql, Object... params)
			throws SQLException {
		if (getAllowShowSQL())
			logger.info(buildSqlAndParamsDebugInfo(sql, params));
		checkConnAndSqlNotNull(conn, sql);
		ResultSet rs = null;
		PreparedStatement pst = null;
		SQLException exception = null;
		try {
			pst = conn.prepareStatement(sql);
			fillStatement(pst, params);
			rs = pst.executeQuery();
			return TinyResultSet.toResult(rs, requiredType);
		} catch (SQLException e) {
			exception = e;
		} finally {
			closeRSandPST(rs, pst, exception, sql, params);
		}
		return null;
	}

	public static <T> TinyResultSet inlineQuery(Connection conn, Class<T> requiredType, String... sqls)
			throws SQLException {
		SqlAndParams sp = buildSqlAndParams(sqls);
		return query(conn, requiredType, sp.getSql(), sp.getParams());
	}

	public static TinyResultSet query(Connection conn, String sql, Object... params) throws SQLException {
		if (getAllowShowSQL())
			logger.info(buildSqlAndParamsDebugInfo(sql, params));
		checkConnAndSqlNotNull(conn, sql);
		ResultSet rs = null;
		PreparedStatement pst = null;
		SQLException exception = null;
		try {
			pst = conn.prepareStatement(sql);
			fillStatement(pst, params);
			rs = pst.executeQuery();
			return TinyResultSet.toResult(rs);
		} catch (SQLException e) {
			exception = e;
		} finally {
			closeRSandPST(rs, pst, exception, sql, params);
		}
		return null;
	}

	public static TinyResultSet inlineQuery(Connection conn, String... sqls) throws SQLException {
		SqlAndParams sp = buildSqlAndParams(sqls);
		return query(conn, sp.getSql(), sp.getParams());
	}

	public static TinyResultSet insertBatch(Connection conn, String sql, Object[][] params) throws SQLException {
		if (params == null)
			throw new SQLException("Null parameters. If parameters aren't need, pass an empty array.");
		if (getAllowShowSQL())
			logger.info(buildSqlAndParamsDebugInfo(sql, params[0]));
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
			return TinyResultSet.toResult(rs);
		} catch (SQLException e) {
			exception = e;
		} finally {
			closeRSandPST(null, pst, exception, sql);
		}
		return null;
	}

	public static int[] executeBatch(Connection conn, String sql, Object[][] params) throws SQLException {
		if (params == null)
			throw new SQLException("Null parameters. If parameters aren't need, pass an empty array.");
		if (getAllowShowSQL())
			logger.info(buildSqlAndParamsDebugInfo(sql, params[0]));
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
			Object... params) throws SQLException {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				exception = linkException(exception, e);
			} finally {
				rs = null;
			}
		}
		if (pst != null) {
			try {
				pst.close();
			} catch (SQLException e) {
				exception = linkException(exception, e);
			} finally {
				pst = null;
			}
		}
		if (exception != null)
			throw exception;
	}

	private static void checkConnAndSqlNotNull(Connection conn, String sql) throws SQLException {
		if (conn == null)
			throw new SQLException("Null connection");
		if (sql == null)
			throw new SQLException("Null SQL string");
	}

	private static String buildSqlAndParamsDebugInfo(String sql, Object... params) {
		StringBuffer msg = new StringBuffer(" SQL: ").append(sql).append("\r\n Parameters: ");
		if (params == null || params.length == 0)
			msg.append("[]");
		else
			msg.append(Arrays.deepToString(params));
		return msg.toString();
	}

	public static SQLException linkException(SQLException lastException, SQLException e) {
		if (lastException != null)
			e.setNextException(lastException);
		return e;
	}

}
