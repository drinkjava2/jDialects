/*
 * HiDialect, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.hidialect;

/**
 * <pre>
 * This tool transfer SQL with pagination added according different database dialects.
 * 
 * Usage: 
 * new Paging().setDialect(hidialect.MySql5).setPageNumber(2).setPageSize(10).pagin("select * from user order by id");
 *   or
 * new Paging(hidialect.MySql5).setPage(2,10).pagin("select * from user order by id");
 *   or
 * new Paging(hidialect.Oracle10).pagin(2,10,"select * from user order by id");
 * 
 * Return: a new SQL with with pagination text inserted, for example:
 * MySQL5:   
 *      select * from user order by id limit 10 , 10 
 * 
 * Oracle10:  
 *      select * from ( select row_.*, rownum rownum_ from ( select * from user order by id ) row_ where rownum <=  20) where rownum_ > 10
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class Paging {
	private Dialect dialect;
	private int pageNumber = 0;
	private int pageSize = 0;

	public Paging() {
	}

	public Paging(Dialect dialect) {
		this.dialect = dialect;
	}

	public Dialect getDialect() {
		return dialect;
	}

	public Paging setDialect(Dialect dialect) {
		this.dialect = dialect;
		return this;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public Paging setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
		return this;
	}

	public int getPageSize() {
		return pageSize;
	}

	public Paging setPageSize(int pageSize) {
		this.pageSize = pageSize;
		return this;
	}

	public String pagin(String sql) {
		return dialect.pagin(sql);
	}

	public Paging setPage(int pageNumber, int pageSize) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		return this;
	}

}
