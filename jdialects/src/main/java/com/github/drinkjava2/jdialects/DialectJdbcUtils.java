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

	public static Long hotExecuteQuery(Connection conn, String sql) throws SQLException {
		PreparedStatement pst = null;
		SQLException exception = null;
		ResultSet rs =null;
		try {
			pst = conn.prepareStatement(sql);
			rs = pst.executeQuery();
			rs.next();
			return rs.getLong(1);
		} catch (SQLException e) {
			exception = e;
			return null;
		} finally {
			closeRSandPST(rs, pst, exception);
		}
	}

	public static int hotExecuteUpdate(Connection conn, String sql) throws SQLException {
		PreparedStatement pst = null;
		SQLException exception = null;
		try {
			pst = conn.prepareStatement(sql);
			return pst.executeUpdate();
		} catch (SQLException e) {
			exception = e;
			return 0;
		} finally {
			closeRSandPST(null, pst, exception);
		}
	}

	public static void closeRSandPST(ResultSet rs, PreparedStatement pst, SQLException exception) throws SQLException {
		SQLException newSQLException=exception;
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				if (newSQLException != null)
					e.setNextException(newSQLException);
				newSQLException = e;
			}  
		}
		if (pst != null) {
			try {
				pst.close();
			} catch (SQLException e) {
				if (newSQLException != null)
					e.setNextException(newSQLException);
				newSQLException = e;
			}  
		}
		if (newSQLException != null)
			throw newSQLException;
	}
}
