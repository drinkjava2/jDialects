/*
 * AllDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.codegenerator;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.from;
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
 * This is not a unit test class, it's a code generator tool to create source code for Dialect.java
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

		generatePaginationSourceCode();
		generatePaginationFirstOnlySourceCode();
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
				pagination = replaceSqlTags(pagination, baitSqlBody);

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
				paginationFirstOnly = replaceSqlTags(paginationFirstOnly, baitSqlBody);
			} catch (Exception e) {
			}
			Dao.execute("update tb_pagination  " //
					, " set paginationFirstOnly=?" + empty(paginationFirstOnly)//
					, " where dialect=? " + empty(dialect)//
			);
		}

	}

	private String replaceSqlTags(String pagination, String baitSqlBody) {
		pagination = StringUtils.replace(pagination, baitSqlBody, "$BODY");
		pagination = StringUtils.replace(pagination, "select $BODY", "$SQL");

		pagination = StringUtils.replace(pagination, "37", "$OFFSET");
		pagination = StringUtils.replace(pagination, "13", "$MAX");
		pagination = StringUtils.replace(pagination, "50", "$END");
		pagination = StringUtils.replace(pagination, "__hibernate_row_nr__", "_rownum_");

		pagination = StringUtils.replace(pagination, "limit ?, ?", "limit $OFFSET, $MAX");
		pagination = StringUtils.replace(pagination, " limit ? offset ?", " limit $MAX offset $OFFSET");
		pagination = StringUtils.replace(pagination, " rows ? to ?", " rows $OFFSET to $END");
		pagination = StringUtils.replace(pagination, " rownum_ <= ? and rownum_ > ?",
				" rownum_ <= $END and rownum_ > $OFFSET");

		pagination = StringUtils.replace(pagination, " rownum <= ?) where rownum_ > ?",
				" rownum <= $END) where rownum_ > $OFFSET");

		pagination = StringUtils.replace(pagination, " limit ? ?", " limit $OFFSET $MAX");
		pagination = StringUtils.replace(pagination, "offset ? rows fetch next ? rows",
				"offset $OFFSET rows fetch next $MAX rows");
		pagination = StringUtils.replace(pagination, " first ? skip ?", " first $OFFSET skip $MAX");
		pagination = StringUtils.replace(pagination, " TOP(?) ", " TOP($MAX) ");
		pagination = StringUtils.replace(pagination, " _rownum_ >= ? AND _rownum_ < ?",
				" _rownum_ >= $OFFSET AND _rownum_ < $END");
		pagination = StringUtils.replace(pagination, " c1, c2 ", " $ORDER_BY ");
		return pagination;
	}

	private void someSpecialSQLfix() {
		TB_pagination tb=Dao.load(TB_pagination.class, com.github.drinkjava2.alldialects.Dialect.SQLServer2005Dialect.toString());
		tb.setPagination("SELECT * FROM (SELECT ROW_NUMBER() OVER($ORDER_BY) AS ROW__NM, $BODY_NO_ORDER) TMP_TB WHERE ROW__NM BETWEEN $OFFSET_1 AND $MAX ");
		//NOT FINISH
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

		System.out.println("====================================================");
		System.out.println("====================================================");
		System.out.println("====================================================");

		// Now generate Java source code to console
		StringBuilder sb = new StringBuilder();

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
		System.out.println("====================================================");
		System.out.println("====================================================");
		System.out.println("====================================================");
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

		System.out.println("====================================================");
		System.out.println("====================================================");
		System.out.println("====================================================");

		// Now generate Java source code to console
		StringBuilder sb = new StringBuilder();

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
		System.out.println("====================================================");
		System.out.println("====================================================");
		System.out.println("====================================================");
	}
}