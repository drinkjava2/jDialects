/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

/**
 * DDL Utils
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class DDLUtils {
	public static String getAddUniqueConstraint(Dialect dialect, String tableName, Column column) {
		if (!column.getUnique())
			return null;
		String UniqueConstraintName = column.getUniqueConstraintName();
		if (StrUtils.isEmpty(UniqueConstraintName))
			UniqueConstraintName = "UK_" + RandomStrUtils.getRandomString(20);
		StringBuilder sb = new StringBuilder("alter table ").append(tableName);

		if (dialect.isInfomixFamily()) {
			return sb.append(" add constraint unique (").append(column.getColumnName()).append(") constraint ")
					.append(UniqueConstraintName).append(";").toString();
		}

		if (dialect.isDerbyFamily() || dialect.isDB2Family()) {
			if (!column.getNotNull()) {
				return new StringBuilder("create unique index ").append(UniqueConstraintName).append(" on ")
						.append(tableName).append("(").append(column.getColumnName()).append(");").toString();
			}
		}
		return sb.append(" add constraint ").append(UniqueConstraintName).append(" unique (")
				.append(column.getColumnName()).append(");").toString();
	}
}
