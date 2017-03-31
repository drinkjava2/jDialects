/*
 * HiDialect, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.hidialect;

/**
 * This is a configuration class, equal to XML in Spring
 *
 */
public enum HiDialect {
	Cache71, CUBRID, DataDirectOracle9, DB2, DB2390, DB2400, Derby, DerbyTenFive, DerbyTenSeven, DerbyTenSix, Firebird, FrontBase, H2, HANAColumnStore, HANARowStore, HSQL, Informix, Informix10, Ingres, Ingres10, Ingres9, Interbase, JDataStore, MariaDB, MariaDB53, Mckoi, MimerSQL, MySQL, MySQL5, MySQL55, MySQL57, MySQL57InnoDB, MySQL5InnoDB, MySQLInnoDB, MySQLMyISAM, Oracle, Oracle10g, Oracle12c, Oracle8i, Oracle9, Oracle9i, Pointbase, PostgresPlus, PostgreSQL, PostgreSQL81, PostgreSQL82, PostgreSQL9, PostgreSQL91, PostgreSQL92, PostgreSQL93, PostgreSQL94, PostgreSQL95, Progress, RDMSOS2200, SAPDB, SQLServer, SQLServer2005, SQLServer2008, SQLServer2012, Sybase, Sybase11, SybaseAnywhere, SybaseASE15, SybaseASE157, Teradata, Teradata14, TimesTen;

	public String pagin(String sql) {
		switch (this) {
		case Cache71:
			sql += this;
			break;
		case CUBRID:
			sql += this;
			break;
		case DataDirectOracle9:
			sql += this;
			break;
		default:
			return sql;
		}
		return sql;
	}

}
