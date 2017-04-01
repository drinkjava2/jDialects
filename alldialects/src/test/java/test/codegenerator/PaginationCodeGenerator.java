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
				+ "sortorder int"//
				+ ")";
		Dao.executeQuiet("drop table tb_pagination");
		Dao.execute(createSQL);
		Dao.refreshMetaData();
		exportDialectPaginations();
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

		public Integer getSortorder() {
			return sortorder;
		}

		public void setSortorder(Integer sortorder) {
			this.sortorder = sortorder;
		}
	}

	private void exportDialectPaginations() {
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
			String pagination = "NOT SUPPORT";
			try {
				String baitSQL = "a.c1 as c1, b.c2 as c2 from ta a, tb b where a.c2 like 'a%' group by a.c1 order by a.c1, b.c2";
				pagination = l.processSql("select " + baitSQL, r);
				pagination = StringUtils.replace(pagination, baitSQL, "$BODY");
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
				pagination = StringUtils.replace(pagination, " c1, c2 ", " $MSSQL_ORDERBY ");
			} catch (Exception e) {
			}
			Dao.executeInsert("insert into tb_pagination (" //
					, "dialect ," + empty(dialect)//
					, "pagination )" + empty(pagination)//
					, valuesAndQuestions());
		}
		// Done

		// Dao.getDefaultContext().setShowSql(true);
		// Now delete repeat pagination
		TB_pagination tp = new TB_pagination();
		List<TB_pagination> l = Dao.queryForEntityList(TB_pagination.class, select(), tp.all(), from(), tp.table(),
				" order by pagination,dialect");
		TB_pagination lastLine = null;
		int sortorder = 1;
		// Dao.getDefaultContext().setShowSql(true);
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
		sb.append("	paginSQLTemplate = \"\";\r\n");
		sb.append("}\r\n");
		sb.append("}\r\n");

		System.out.println("====================================================");
		System.out.println("====================================================");
		System.out.println("====================================================");
		System.out.println();
		System.out.println(sb.toString());
		System.out.println();
		System.out.println("====================================================");
		System.out.println("====================================================");
		System.out.println("====================================================");
	}

}