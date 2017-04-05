/*
 * AllDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.codegenerator;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.from;
import static com.github.drinkjava2.jsqlbox.SqlHelper.q;
import static com.github.drinkjava2.jsqlbox.SqlHelper.select;
import static com.github.drinkjava2.jsqlbox.SqlHelper.valuesAndQuestions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.engine.jdbc.dialect.internal.DialectFactoryImpl;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.Entity;

import test.TestBase;

/**
 * This is not a unit test class, it's a code generator tool to create source
 * code for Dialect.java
 *
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class PaginationCodeGenerator extends TestBase {

	@Test
	public void transferPagination() {
		String createSQL = "create table tb_pagination ("//
				+ "dialect varchar(100),"//
				+ "pagination varchar(500),"//
				+ "paginationFirstOnly varchar(500),"//
				+ "sortorder int"//
				+ ")";
		Dao.executeQuiet("drop table tb_pagination");
		Dao.execute(createSQL);
		Dao.refreshMetaData();
		exportHibernateDialectPaginations();
		exportHibernateDialectPaginationFirstOnly();

		// put my sqls here
		someSpecialSQLfix();

		System.out.println("//====================================================");
		System.out.println("//====================================================");

		generatePaginationSourceCode();
		generatePaginationFirstOnlySourceCode();
		System.out.println("//====================================================");
		System.out.println("//====================================================");

	}

	private static Dialect buildDialectByName(Class<?> dialect) {
		BootstrapServiceRegistry bootReg = new BootstrapServiceRegistryBuilder()
				.applyClassLoader(HibernateDialectsList.class.getClassLoader()).build();
		StandardServiceRegistry registry = new StandardServiceRegistryBuilder(bootReg).build();
		DialectFactoryImpl dialectFactory = new DialectFactoryImpl();
		dialectFactory.injectServices((ServiceRegistryImplementor) registry);
		final Map<String, String> configValues = new HashMap<>();
		configValues.put(Environment.DIALECT, dialect.getName());
		return dialectFactory.buildDialect(configValues, null);
	}

	public static class TB_pagination implements Entity {
		private String dialect;
		private String pagination;
		private String paginationFirstOnly;
		private Integer sortorder;

		{
			this.box().configEntityIDs("dialect");
		}

		public String getDialect() {
			return dialect;
		}

		public void setDialect(String dialect) {
			this.dialect = dialect;
		}

		public String getPagination() {
			return pagination;
		}

		public void setPagination(String pagination) {
			this.pagination = pagination;
		}

		public String getpaginationFirstOnly() {
			return paginationFirstOnly;
		}

		public void setpaginationFirstOnly(String paginationFirstOnly) {
			this.paginationFirstOnly = paginationFirstOnly;
		}

		public Integer getSortorder() {
			return sortorder;
		}

		public void setSortorder(Integer sortorder) {
			this.sortorder = sortorder;
		}
	}

	private void exportHibernateDialectPaginations() {
		System.out.println("exportDialectPaginations========================");
		RowSelection r = new RowSelection();
		r.setFirstRow(37);// OFFSET
		r.setMaxRows(13);// MAX
		r.setFetchSize(100);// no use
		r.setTimeout(1000);// no use
		List<Class<? extends Dialect>> dialects = HibernateDialectsList.SUPPORTED_DIALECTS;
		for (Class<? extends Dialect> class1 : dialects) {
			Dialect dia = buildDialectByName(class1);
			LimitHandler l = dia.getLimitHandler();

			String dialect = class1.getSimpleName();
			String pagination = com.github.drinkjava2.alldialects.Dialect.NOT_SUPPORT;
			try {
				String baitSqlBody = "a.c1 as c1, b.c2 as c2 from ta a, tb b where a.c2 like 'a%' group by a.c1 order by a.c1, b.c2";
				pagination = l.processSql("select " + baitSqlBody, r);
				pagination = replaceOffsetAndLimit(dialect, pagination, baitSqlBody);

			} catch (Exception e) {
			}
			Dao.executeInsert("insert into tb_pagination (" //
					, "dialect ," + empty(dialect)//
					, "pagination )" + empty(pagination)//
					, valuesAndQuestions());
		}
		// Done
	}

	private void exportHibernateDialectPaginationFirstOnly() {
		System.out.println("exportDialectPaginations========================");
		RowSelection r = new RowSelection();
		r.setFirstRow(0);// OFFSET set to 0
		r.setMaxRows(13);// MAX
		r.setFetchSize(100);// no use
		r.setTimeout(1000);// no use
		List<Class<? extends Dialect>> dialects = HibernateDialectsList.SUPPORTED_DIALECTS;
		for (Class<? extends Dialect> class1 : dialects) {
			Dialect dia = buildDialectByName(class1);
			LimitHandler l = dia.getLimitHandler();

			String dialect = class1.getSimpleName();
			String paginationFirstOnly = com.github.drinkjava2.alldialects.Dialect.NOT_SUPPORT;
			try {
				String baitSqlBody = "a.c1 as c1, b.c2 as c2 from ta a, tb b where a.c2 like 'a%' group by a.c1 order by a.c1, b.c2";
				paginationFirstOnly = l.processSql("select " + baitSqlBody, r);
				paginationFirstOnly = replaceFirstLimitOnly(dialect, paginationFirstOnly, baitSqlBody);
			} catch (Exception e) {
			}
			Dao.execute("update tb_pagination  " //
					, " set paginationFirstOnly=?" + empty(paginationFirstOnly)//
					, " where dialect=? " + empty(dialect)//
			);
		}

	}

	private static String replaceDialectStr(String dialectName, String SQL, String strOld, String strNew,
			String... dialects) {
		if (StringUtils.isEmpty(dialectName) || dialects == null || dialects.length == 0)
			return StringUtils.replace(SQL, strOld, strNew);

		String newSQL = SQL;
		for (String dia : dialects) {
			if (StringUtils.containsIgnoreCase(dialectName, dia))
				newSQL = StringUtils.replace(SQL, strOld, strNew);
		}
		return newSQL;
	}

	/**
	 * $0BASE_OFFSET=0, $1BASE_ROW_START=$0BASE_OFFSET+1
	 * 
	 * <pre>
	 *  Here is a good article:
	 *  https://blog.jooq.org/2014/06/09/stop-trying-to-emulate-sql-offset-pagination-with-your-in-house-db-framework/
	 * </pre>
	 */
	private String replaceOffsetAndLimit(String dialectName, String sql, String baitSqlBody) {
		sql = replaceDialectStr(dialectName, sql, baitSqlBody, "$BODY");
		sql = replaceDialectStr(dialectName, sql, "select $BODY", "$SQL");
		sql = replaceDialectStr(dialectName, sql, "37", "$0BASE_OFFSET");
		sql = replaceDialectStr(dialectName, sql, "13", "$PAGESIZE");
		sql = replaceDialectStr(dialectName, sql, "50", "$1BASE_ROW_END");

		sql = replaceDialectStr(dialectName, sql, " rownum <= ?) where rownum_ > ?",
				" rownum <= $1BASE_ROW_END) where rownum_ > $0BASE_OFFSET", "Oracle");

		sql = replaceDialectStr(dialectName, sql, " rownum_ <= ? and rownum_ > ?",
				" rownum_ <= $1BASE_ROW_END and rownum_ > $0BASE_OFFSET", "Oracle");

		sql = replaceDialectStr(dialectName, sql, "offset ? rows fetch next ? rows",
				"offset $0BASE_OFFSET rows fetch next $PAGESIZE rows", "Oracle12c");

		sql = replaceDialectStr(dialectName, sql, " limit ? offset ?", " limit $PAGESIZE offset $0BASE_OFFSET");

		sql = replaceDialectStr(dialectName, sql, " rows ? to ?", " rows $1BASE_ROW_START to $1BASE_ROW_END",
				"Interbase");

		sql = replaceDialectStr(dialectName, sql, " first ? skip ?", " first $PAGESIZE skip $0BASE_OFFSET", "FireBird");

		sql = replaceDialectStr(dialectName, sql, " limit ?, ?", " limit $PAGESIZE, $0BASE_OFFSET", "PostgreSQL");

		sql = replaceDialectStr(dialectName, sql, " limit ?, ?", " limit $0BASE_OFFSET, $PAGESIZE", "MySql", "Maria",
				"CUBRID");

		sql = replaceDialectStr(dialectName, sql, "select limit ? ? ", "select limit $0BASE_OFFSET $PAGESIZE ", "HSQL");

		sql = replaceDialectStr(dialectName, sql, " offset ? rows fetch next ? rows",
				" offset $0BASE_OFFSET rows fetch next $PAGESIZE rows", "SQLServer2012");
		return sql;
	}

	private String replaceFirstLimitOnly(String dialectName, String sql, String baitSqlBody) {
		sql = replaceDialectStr(dialectName, sql, baitSqlBody, "$BODY");
		sql = replaceDialectStr(dialectName, sql, "select $BODY", "$SQL");
		sql = replaceDialectStr(dialectName, sql, "13", "$PAGESIZE");

		sql = replaceDialectStr(dialectName, sql, " limit ?", " limit $PAGESIZE");
		sql = replaceDialectStr(dialectName, sql, " TOP ?", " TOP $PAGESIZE");
		sql = replaceDialectStr(dialectName, sql, " rownum <= ?", " rownum <= $PAGESIZE");
		sql = replaceDialectStr(dialectName, sql, " first ?", " first $PAGESIZE");
		sql = replaceDialectStr(dialectName, sql, " top ?", " top $PAGESIZE");
		sql = replaceDialectStr(dialectName, sql, " rows ?", " rows $PAGESIZE");
		sql = replaceDialectStr(dialectName, sql, " TOP(?)", " TOP($PAGESIZE)");
		sql = replaceDialectStr(dialectName, sql, " offset 0 rows fetch next ? rows",
				" offset 0 rows fetch next $PAGESIZE rows");

		return sql;
	}

	/**
	 * To fix or append some special SQL
	 */
	private void someSpecialSQLfix() {
		// For SQL SERVER 2005 and 2008, use simple SQL template
		String pg = "SELECT * FROM (SELECT ROW_NUMBER() OVER($ORDER_BY_ONLY) AS ROW__NM, $NO_ORDER_BODY) TMP_TB WHERE ROW__NM BETWEEN $1BASE_ROW_START AND $1BASE_ROW_END";
		Dao.execute("update tb_pagination set pagination=" + q(pg) + " where dialect=" + q("SQLServer2005Dialect")
				+ " or dialect=" + q("SQLServer2008Dialect"));
	}

	private void generatePaginationSourceCode() {
		// Dao.getDefaultContext().setShowSql(true);
		// Now delete repeat pagination
		TB_pagination tp = new TB_pagination();
		List<TB_pagination> l = Dao.queryForEntityList(TB_pagination.class, select(), tp.all(), from(), tp.table(),
				" order by pagination, dialect ");

		// Delete repeat pagination test
		TB_pagination lastLine = null;
		int sortorder = 1;
		for (TB_pagination thisLine : l) {
			thisLine.setSortorder(sortorder++);
			thisLine.update();
			if (lastLine != null && lastLine.getPagination().equals(thisLine.getPagination())) {
				lastLine.setPagination("");
				lastLine.update();
			}
			lastLine = thisLine;
		}

		// Now generate Java source code to console
		StringBuilder sb = new StringBuilder();

		sb.append("// Initialize paginSQLTemplate\r\n");
		sb.append("private void initializePaginSqlTemplate() {// NOSONAR\r\n");
		sb.append("switch (this.toString()) {// NOSONAR\r\n");
		l = Dao.queryForEntityList(TB_pagination.class, select(), tp.all(), from(), tp.table(), " order by sortorder");
		for (TB_pagination t : l) {
			sb.append("case \"").append(t.getDialect()).append("\":");
			if (!StringUtils.isEmpty(t.getPagination())) {

				sb.append("paginSQLTemplate= \"" + t.getPagination() + "\";\r\n");
				sb.append("break;");
			}
			sb.append("\r\n");
		}
		sb.append("default:  \r\n");
		sb.append("	paginSQLTemplate = NOT_SUPPORT;\r\n");
		sb.append("}\r\n");
		sb.append("}\r\n");

		System.out.println();
		System.out.println(sb.toString());
		System.out.println();
	}

	private void generatePaginationFirstOnlySourceCode() {
		// Dao.getDefaultContext().setShowSql(true);
		// Now delete repeat pagination
		TB_pagination tp = new TB_pagination();
		List<TB_pagination> l = Dao.queryForEntityList(TB_pagination.class, select(), tp.all(), from(), tp.table(),
				" order by paginationFirstOnly, dialect ");

		// Delete repeat pagination test
		TB_pagination lastLine = null;
		int sortorder = 1;
		for (TB_pagination thisLine : l) {
			thisLine.setSortorder(sortorder++);
			thisLine.update();
			if (lastLine != null && lastLine.getpaginationFirstOnly().equals(thisLine.getpaginationFirstOnly())) {
				lastLine.setpaginationFirstOnly("");
				lastLine.update();
			}
			lastLine = thisLine;
		}

		// Now generate Java source code to console
		StringBuilder sb = new StringBuilder();

		sb.append("//initialize paginFirstOnlySqlTemplate\r\n");
		sb.append("private void initializePaginFirstOnlySqlTemplate() {// NOSONAR\r\n");
		sb.append("switch (this.toString()) {// NOSONAR\r\n");
		l = Dao.queryForEntityList(TB_pagination.class, select(), tp.all(), from(), tp.table(), " order by sortorder");
		for (TB_pagination t : l) {
			sb.append("case \"").append(t.getDialect()).append("\":");
			if (!StringUtils.isEmpty(t.getpaginationFirstOnly())) {
				sb.append("paginFirstOnlyTemplate= \"" + t.getpaginationFirstOnly() + "\";\r\n");
				sb.append("break;");
			}
			sb.append("\r\n");
		}
		sb.append("default:  \r\n");
		sb.append("	paginFirstOnlyTemplate = NOT_SUPPORT;\r\n");
		sb.append("}\r\n");
		sb.append("}\r\n");

		System.out.println();
		System.out.println(sb.toString());
		System.out.println();
	}
}