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
public enum Dialect {
	Cache71, CUBRID, DataDirectOracle9, DB2, DB2390, DB2400, Derby, DerbyTenFive, DerbyTenSeven, DerbyTenSix, Firebird, FrontBase, H2, HANAColumnStore, HANARowStore, HSQL, Informix, Informix10, Ingres, Ingres10, Ingres9, Interbase, JDataStore, MariaDB, MariaDB53, Mckoi, MimerSQL, MySQL, MySQL5, MySQL55, MySQL57, MySQL57InnoDB, MySQL5InnoDB, MySQLInnoDB, MySQLMyISAM, Oracle, Oracle10g, Oracle12c, Oracle8i, Oracle9, Oracle9i, Pointbase, PostgresPlus, PostgreSQL, PostgreSQL81, PostgreSQL82, PostgreSQL9, PostgreSQL91, PostgreSQL92, PostgreSQL93, PostgreSQL94, PostgreSQL95, Progress, RDMSOS2200, SAPDB, SQLServer, SQLServer2005, SQLServer2008, SQLServer2012, Sybase, Sybase11, SybaseAnywhere, SybaseASE15, SybaseASE157, Teradata, Teradata14, TimesTen;

	private String getPaginSqlTemplate() {
		switch (this) {//NOSONAR
		case H2:
		case HANAColumnStore:
		case HANARowStore:
		case PostgresPlus:
		case PostgreSQL81:
		case PostgreSQL82:
		case PostgreSQL91:
		case PostgreSQL92:
		case PostgreSQL93:
		case PostgreSQL94:
		case PostgreSQL95:
		case PostgreSQL9:
		case PostgreSQL:
			return "$SQL limit $MAX offset $OFF";
		case CUBRID:
		case MariaDB53:
		case MariaDB:
		case MySQL55:
		case MySQL57:
		case MySQL57InnoDB:
		case MySQL5:
		case MySQL5InnoDB:
		case MySQL:
		case MySQLInnoDB:
		case MySQLMyISAM:
			return "$SQL limit $OFF, $MAX";
		case Ingres10:
		case Ingres9:
			return "$SQL offset $OFF fetch first $MAX rows only";
		case Derby:
		case DerbyTenFive:
		case DerbyTenSeven:
		case DerbyTenSix:
		case Oracle12c:
		case SQLServer2012:
			return "$SQL offset $OFF rows fetch next $MAX rows only";
		case Interbase:
			return "$SQL rows ? to ?";

		case DB2400:
		case DB2:
			return "select * from ( select inner2_.*, rownumber() over(order by order of inner2_) as rownumber_ from ( $SQL fetch first $END rows only ) as inner2_ ) as inner1_ where rownumber_ > $OFF order by rownumber_";
		case Oracle8i:
		case Oracle:
			return "select * from ( select row_.*, rownum rownum_ from ( $SQL ) row_ ) where rownum_ <= $END and rownum_ $OFF ?";
		case DataDirectOracle9:
		case Oracle10g:
		case Oracle9:
		case Oracle9i:
			return "select * from ( select row_.*, rownum rownum_ from ( $SQL ) row_ where rownum <= $END) where rownum_ $OFF ?";
		case Firebird:
			return "select first $MAX skip $OFF XXX";
		case HSQL:
			return "select limit $OFF $MAX XXX";
		case Informix10:
			return "select SKIP $OFF FIRST $MAX XXX";
		case SQLServer2005:
		case SQLServer2008:
			return "WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hidia_row_nr__ FROM ( select TOP($MAX) XXX ) inner_query ) SELECT c1, c2 FROM query WHERE __hidia_row_nr__ >= ? AND __hidia_row_nr__ < ?";
		case Cache71:
		case DB2390:
		case FrontBase:
		case Informix:
		case Ingres:
		case JDataStore:
		case Mckoi:
		case MimerSQL:
		case Pointbase:
		case Progress:
		case RDMSOS2200:
		case SAPDB:
		case SQLServer:
		case Sybase11:
		case SybaseAnywhere:
		case SybaseASE157:
		case SybaseASE15:
		case Sybase:
		case Teradata14:
		case Teradata:
		case TimesTen:
		default:
			return "";
		}
	}

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
