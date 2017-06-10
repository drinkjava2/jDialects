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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import javax.sql.DataSource;

import com.github.drinkjava2.jdialects.DialectException;

/**
 * A tiny pure JDBC tool to access database, only used for unit test
 * 
 *
 * @author Yong Zhu
 * @version 1.0.0
 */
public class TinyJdbc {

	DataSource dataSource;

	private static ThreadLocal<ArrayList<Object>> paraCache = new ThreadLocal<ArrayList<Object>>() {
		@Override
		protected ArrayList<Object> initialValue() {
			return new ArrayList<>();
		}
	};

	public TinyJdbc(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	private void releaseConnection(Connection connection) throws SQLException {
		connection.close();
	}

	/**
	 * Clear cache first, then return a empty string and cache parameters in
	 * thread local for SQL use
	 */
	public static String para_(Object... parameters) {
		paraCache.get().clear();
		return para(parameters);
	}

	/**
	 * Return a empty string and cache parameters in thread local for SQL use
	 */
	public static String para(Object... parameters) {
		for (Object o : parameters)
			paraCache.get().add(o);
		return "";
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

	public TinyResult executeQuery(String... sqls) {// NOSONAR
		TinySqlAndParameters pairs = prepareSQLandParameters(sqls);
		ResultSet rs = null;
		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = getConnection();
			con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			con.setAutoCommit(false);
			int i = 1;
			pst = con.prepareStatement(pairs.getSql());// NOSONAR
			for (Object obj : pairs.getParameters())
				pst.setObject(i++, obj);
			rs = pst.executeQuery();
			TinyResult r = ResultSupport.toResult(rs);
			con.commit();
			return r;
		} catch (SQLException e) {
			try {
				if (con != null)
					con.rollback();
				TinyJdbcException.throwEX(e, e.getMessage());
			} catch (SQLException e1) {
				TinyJdbcException.throwEX(e1, e1.getMessage());
			}
		} finally {
			closeResources(rs, con, pst);
		}
		return null;
	}

	private void closeResources(ResultSet rs, Connection con, PreparedStatement pst) {
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
		try {
			if (con != null && !con.isClosed()) {
				try {// NOSONAR
					releaseConnection(con);
				} catch (SQLException e) {
					TinyJdbcException.throwEX(e, e.getMessage());
				}
			}
		} catch (SQLException e) {
			TinyJdbcException.throwEX(e, e.getMessage());
		}
	}

	// =============================old ============================
	public Integer queryForInteger(String... sqls) {
		TinyResult rst = executeQuery(sqls);
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

	public int executeUpdate(String... sqls) {// NOSONAR
		TinySqlAndParameters pairs = prepareSQLandParameters(sqls);
		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = getConnection();
			con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			con.setAutoCommit(false);
			int i = 1;
			pst = con.prepareStatement(pairs.getSql());// NOSONAR
			for (Object obj : pairs.getParameters())
				pst.setObject(i++, obj);
			int count = pst.executeUpdate();
			con.commit();
			return count;
		} catch (SQLException e) {
			TinyJdbcException.throwEX(e, e.getMessage());
			try {
				if (con != null)
					con.rollback();
			} catch (SQLException e1) {
				TinyJdbcException.throwEX(e1, e1.getMessage());
			}
		} finally {
			closeResources(null, con, pst);
		}
		return 0;
	}

	public boolean executeQuiet(String... sqls) {
		try {
			return execute(sqls);
		} catch (Exception e) {
			DialectException.eatException(e);
			return false;
		}
	}

	public void executeManySqls(String... sqls) {// NOSONAR
		for (String str : sqls)
			execute(str);
	}

	public void executeQuietManySqls(String... sqls) {// NOSONAR
		for (String str : sqls)
			executeQuiet(str);
	}

	public boolean execute(String... sqls) {// NOSONAR
		TinySqlAndParameters pairs = prepareSQLandParameters(sqls);

		System.out.println("TinyJdbc execute sql=" + pairs.getSql());

		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = getConnection();
			con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			con.setAutoCommit(false);
			pst = con.prepareStatement(pairs.getSql());// NOSONAR
			int i = 1;
			for (Object obj : pairs.getParameters())
				pst.setObject(i++, obj);
			boolean bl = pst.execute();
			con.commit();
			return bl;
		} catch (SQLException e) {
			TinyJdbcException.throwEX(e, e.getMessage());
			try {
				if (con != null)
					con.rollback();
			} catch (SQLException e1) {
				TinyJdbcException.throwEX(e1, e1.getMessage());
			}
		} finally {
			closeResources(null, con, pst);
		}
		return false;
	}

	public int getTableCount() {
		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = getConnection();
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
			try {
				if (con != null)
					con.rollback();
			} catch (SQLException e1) {
				TinyJdbcException.throwEX(e1, e1.getMessage());
			}
		} finally {
			closeResources(null, con, pst);
		}
		return 0;
	}
}
