/*
* jDialects, a tiny SQL dialect tool 
*
* License: GNU Lesser General Public License (LGPL), version 2.1 or later.
* See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
*/
package com.github.drinkjava2.jdialects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Some simple JDBC method
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public abstract class DialectJdbcUtils {

	public static Long hotQueryForLong(Connection conn, String sql) throws SQLException {
		PreparedStatement pst = null;
		SQLException exception = null;
		try {
			pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			rs.next();
			return rs.getLong(0);
		} catch (SQLException e) {
			exception = e;
			return null;
		} finally {
			closeRSandPST(null, pst, exception, sql);
		}
	}

	public static void hotExecuteSql(Connection conn, String sql) throws SQLException {
		PreparedStatement pst = null;
		SQLException exception = null;
		try {
			pst = conn.prepareStatement(sql);
			pst.execute();
		} catch (SQLException e) {
			exception = e;
		} finally {
			closeRSandPST(null, pst, exception, sql);
		}
	}

	public static void closeRSandPST(ResultSet rs, PreparedStatement pst, SQLException exception, String sql,
			Object... params) throws SQLException {
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
			throw exception;
	}
}
