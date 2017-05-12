/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.functiontest;

import org.junit.After;

import com.github.drinkjava2.jbeanbox.BeanBox;

/**
 * This test base class in charge of close data sources.
 * 
 * @author Yong Z.
 * @since 1.0.2
 *
 */
public class TestBase {
	@After
	public void closeDataSource() {
		BeanBox.defaultContext.close();// close dataSource
	}

}
