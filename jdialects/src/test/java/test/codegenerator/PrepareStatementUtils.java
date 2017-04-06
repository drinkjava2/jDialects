/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.codegenerator;

import java.sql.SQLException;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.engine.spi.RowSelection;

import com.github.drinkjava2.jdialects.DialectException;

/**
 * This is not a unit test class, it's a code generator tool to create source
 * code in Dialect.java
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class PrepareStatementUtils {

	public static String prepareQueryStatement(final RowSelection selection, Dialect dialect, LimitHandler limitHandler) throws SQLException {
		FakePrepareStatement st = new FakePrepareStatement();
		try { 
			int col = 1;
			col += limitHandler.bindLimitParametersAtStartOfQuery(selection, st, col);
			col += limitHandler.bindLimitParametersAtEndOfQuery(selection, st, col);
			limitHandler.setMaxRows(selection, st);
		} catch (Exception e) {
			return (String) DialectException.throwEX(e, "");
		}
		return st.getLimits();
	}

}