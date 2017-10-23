/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

/**
 * Interface which has a paginate method
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public interface PaginateSupport {
	
	/**
	 * Create a pagination SQL by given pageNumber, pageSize and SQL<br/>
	 * 
	 * @param pageNumber The page number, start from 1
	 * @param pageSize The page item size
	 * @param sql The original SQL
	 * @return The paginated SQL
	 */
	public String paginate(int pageNumber, int pageSize, String sql);
}
