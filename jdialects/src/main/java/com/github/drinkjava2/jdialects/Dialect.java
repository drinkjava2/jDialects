/*
* jDialects, a tiny SQL dialect tool 
*
* License: GNU Lesser General Public License (LGPL), version 2.1 or later.
* See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
*/
package com.github.drinkjava2.jdialects;

import java.sql.Connection;
import java.util.EnumMap;
import java.util.Map;

import javax.sql.DataSource;

import com.github.drinkjava2.hibernate.StringHelper;
import com.github.drinkjava2.hibernate.pagination.RowSelection;
import com.github.drinkjava2.hibernate.pagination.SQLServer2005LimitHandler;
import com.github.drinkjava2.hibernate.pagination.SQLServer2012LimitHandler;

/**
 * jDialects is a small Java project collect all databases' dialect, most are
 * extracted from Hibernate, usually jDialects is used for build pagination SQL
 * and DDL SQL for cross-databases purpose. Currently jDialects support 75
 * database dialects include SQLite and Access. It requires JDK1.7 or above.
 * 
 * @author Yong Zhu
 * @since 1.0.0
 * 
 */
public enum Dialect {
	SQLiteDialect, AccessDialect, ExcelDialect, TextDialect, ParadoxDialect, CobolDialect, XMLDialect, DbfDialect, // NOSONAR
	// below are from Hibernate
	@Deprecated DerbyDialect, // NOSONAR Use other Derby version instead
	@Deprecated OracleDialect, // NOSONAR use Oracle8iDialect instead
	@Deprecated Oracle9Dialect, // NOSONAR Use Oracle9i instead
	Cache71Dialect, CUBRIDDialect, DerbyTenFiveDialect, DataDirectOracle9Dialect, DB2Dialect, DB2390Dialect, DB2400Dialect, DerbyTenSevenDialect, DerbyTenSixDialect, FirebirdDialect, FrontBaseDialect, H2Dialect, HANAColumnStoreDialect, HANARowStoreDialect, HSQLDialect, InformixDialect, Informix10Dialect, IngresDialect, Ingres10Dialect, Ingres9Dialect, InterbaseDialect, JDataStoreDialect, MariaDBDialect, MariaDB53Dialect, MckoiDialect, MimerSQLDialect, MySQLDialect, MySQL5Dialect, MySQL55Dialect, MySQL57Dialect, MySQL57InnoDBDialect, MySQL5InnoDBDialect, MySQLInnoDBDialect, MySQLMyISAMDialect, Oracle8iDialect, Oracle9iDialect, Oracle10gDialect, Oracle12cDialect, PointbaseDialect, PostgresPlusDialect, PostgreSQLDialect, PostgreSQL81Dialect, PostgreSQL82Dialect, PostgreSQL9Dialect, PostgreSQL91Dialect, PostgreSQL92Dialect, PostgreSQL93Dialect, PostgreSQL94Dialect, PostgreSQL95Dialect, ProgressDialect, RDMSOS2200Dialect, SAPDBDialect, SQLServerDialect, SQLServer2005Dialect, SQLServer2008Dialect, SQLServer2012Dialect, SybaseDialect, Sybase11Dialect, SybaseAnywhereDialect, SybaseASE15Dialect, SybaseASE157Dialect, TeradataDialect, Teradata14Dialect, TimesTenDialect;// NOSONAR

	private static final String SKIP_ROWS = "$SKIP_ROWS";
	private static final String PAGESIZE = "$PAGESIZE";
	private static final String TOTAL_ROWS = "$TOTAL_ROWS";
	private static final String SKIP_ROWS_PLUS1 = "$SKIP_ROWS_PLUS1";
	private static final String TOTAL_ROWS_PLUS1 = "$TOTAL_ROWS_PLUS1";
	private static final String DISTINCT_TAG = "($DISTINCT)";
	public static final String NOT_SUPPORT = "NOT_SUPPORT";
	DialectLogger logger = DialectLogger.getLog(Dialect.class);

	private String sqlTemplate = null;
	private String topLimitTemplate = null;
	protected final Map<Type, String> typeMappings = new EnumMap<>(Type.class);

	protected final DDLFeatures ddlFeatures = new DDLFeatures();// NOSONAR

	static {
		for (Dialect d : Dialect.values()) {
			d.sqlTemplate = InitPaginationTemplate.initializePaginSQLTemplate(d);
			d.topLimitTemplate = InitPaginationTemplate.initializeTopLimitSqlTemplate(d);
			InitTypeMapping.initializeTypeMappings(d);
			DDLFeatures.initDDLFeatures(d, d.ddlFeatures);
		}
	}

	/**
	 * Guess Dialect by given databaseName, major & minor version if have
	 * 
	 * @param databaseName
	 * @param majorVersionMinorVersion
	 * @return Dialect
	 */
	public static Dialect guessDialect(String databaseName, Object... majorVersionMinorVersion) {
		return GuessDialectUtils.guessDialect(databaseName, majorVersionMinorVersion);
	}

	/**
	 * Guess Dialect by given connection, note:this method does not close
	 * connection
	 * 
	 * @param con The JDBC Connection
	 * @return Dialect The Dialect intance, if can not guess out, return null
	 */
	public static Dialect guessDialect(Connection connection) {
		return GuessDialectUtils.guessDialect(connection);
	}

	/**
	 * Guess Dialect by given data source
	 * 
	 * @param datasource
	 * @return Dialect
	 */
	public static Dialect guessDialect(DataSource datasource) {
		return GuessDialectUtils.guessDialect(datasource);
	}

	/**
	 * Check if is current dialect or ANSI reserved word, if yes throw
	 * exception. if is other database's reserved word, log output a warning.
	 */
	private void checkIfReservedWord(Dialect dialect, String word) {
		if (ReservedDBWords.isReservedWord(word)) {
			String reservedForDB = ReservedDBWords.reservedForDB(word);
			if (ReservedDBWords.isReservedWord(dialect, word)) {
				DialectException.throwEX("\"" + word + "\" is a reserved word of \"" + reservedForDB
						+ "\", should not use it as table or column name");
			} else {
				logger.warn("\"" + word + "\" is a reserved word of other database \"" + reservedForDB
						+ "\", not recommended to be used as table or column name");
			}
		}
	}

	/**
	 * Check if a word is current dialect or ANSI-SQL's reserved word, if yes
	 * throw exception. if is other database's reserved word, log output a
	 * warning. Otherwise return word itself.
	 */
	public String check(String word) {
		checkIfReservedWord(this, word);
		return word;
	}

	/**
	 * Transfer jdialect.Type to a real dialect's ddl type, if colName is
	 * current dialect or ANSI-SQL's reserved word of any database, throw a
	 * DialectException
	 */
	private String translateToDDLType(String colName, Type type, Integer... lengths) {
		checkIfReservedWord(this, colName);
		return colName + " " + translateToDDLType(type, lengths);
	}

	/**
	 * Transfer jdialect.Type to a real dialect's ddl type, lengths is optional
	 * for some types
	 */
	protected String translateToDDLType(Type type, Integer... lengths) {// NOSONAR
		String value = this.typeMappings.get(type);
		if (StrUtils.isEmpty(value) || "N/A".equals(value) || "n/a".equals(value))
			DialectException.throwEX("Type \"" + type + "\" is not supported by dialect \"" + this + "\"");

		if (value.contains("|")) {
			// format example: varchar($l)<255|lvarchar($l)<32739|varchar($l)
			String[] mappings = StringHelper.split("|", value);

			for (String mapping : mappings) {
				if (mapping.contains("<")) {// varchar($l)<255
					String[] limitType = StringHelper.split("<", mapping);
					if (lengths.length > 0 && lengths[0] < Integer.parseInt(limitType[1]))// NOSONAR
						return putParamters(type, limitType[0], lengths);
				} else {// varchar($l)
					return putParamters(type, mapping, lengths);
				}
			}
		} else {
			if (value.contains("$")) {
				// always this order: $l, $p, $s
				return putParamters(type, value, lengths);
			} else
				return value;
		}
		return "";
	}

	/**
	 * inside function
	 */
	private String putParamters(Type type, String value, Integer... lengths) {
		if (lengths.length < StrUtils.countMatches(value, '$'))
			DialectException.throwEX("In Dialect \"" + this + "\", Type \"" + type + "\" should have "
					+ StrUtils.countMatches(value, '$') + " parameters");
		int i = 0;
		String newValue = value;
		if (newValue.contains("$l"))
			newValue = StrUtils.replace(newValue, "$l", String.valueOf(lengths[i++]));
		if (newValue.contains("$p"))
			newValue = StrUtils.replace(newValue, "$p", String.valueOf(lengths[i++]));
		if (newValue.contains("$s"))
			newValue = StrUtils.replace(newValue, "$s", String.valueOf(lengths[i]));
		return newValue;
	}

	//@formatter:off shut off eclipse's formatter
	public String LONG() {return translateToDDLType(Type.BIGINT);}//NOSONAR	
	public String BOOLEAN() {return translateToDDLType(Type.BOOLEAN);}//NOSONAR 
 	public String DOUBLE() {return translateToDDLType(Type.DOUBLE);}//NOSONAR
	public String FLOAT(Integer... lengths) {return translateToDDLType(Type.FLOAT, lengths);}//NOSONAR
	public String INTEGER() {return translateToDDLType(Type.INTEGER);}//NOSONAR
	public String SHORT() {return translateToDDLType(Type.SMALLINT);}//NOSONAR
	public String BIGDECIMAL(Integer precision, Integer scale) {return translateToDDLType(Type.NUMERIC, precision, scale);}//NOSONAR
	public String STRING(Integer length) {return translateToDDLType(Type.VARCHAR, length);}//NOSONAR 
	public String DATE() {return translateToDDLType(Type.DATE);}//NOSONAR	
	public String TIME() {return translateToDDLType(Type.TIME);}//NOSONAR
	public String TIMESTAMP() {return translateToDDLType(Type.TIMESTAMP);}//NOSONAR 
	
	public String BIGINT() {return translateToDDLType(Type.BIGINT);}//NOSONAR 
	public String BINARY(Integer... lengths) {return translateToDDLType(Type.BINARY, lengths);}//NOSONAR
	public String BIT() {return translateToDDLType(Type.BIT);}//NOSONAR
	public String BLOB(Integer... lengths) {return translateToDDLType(Type.BLOB, lengths);}//NOSONAR 
	public String CHAR(Integer... lengths) {return translateToDDLType(Type.CHAR, lengths);}//NOSONAR
	public String CLOB(Integer... lengths) {return translateToDDLType(Type.CLOB, lengths);}//NOSONAR 
	public String DECIMAL(Integer... lengths) {return translateToDDLType(Type.DECIMAL, lengths);}//NOSONAR   
	public String JAVA_OBJECT() {return translateToDDLType(Type.JAVA_OBJECT);}//NOSONAR
	public String LONGNVARCHAR(Integer length) {return translateToDDLType(Type.LONGNVARCHAR, length);}//NOSONAR
	public String LONGVARBINARY(Integer... lengths) {return translateToDDLType(Type.LONGVARBINARY, lengths);}//NOSONAR
	public String LONGVARCHAR(Integer... lengths) {return translateToDDLType(Type.LONGVARCHAR, lengths);}//NOSONAR
	public String NCHAR(Integer length) {return translateToDDLType(Type.NCHAR, length);}//NOSONAR
	public String NCLOB() {return translateToDDLType(Type.NCLOB);}//NOSONAR
	public String NUMERIC(Integer... lengths) {return translateToDDLType(Type.NUMERIC, lengths);}//NOSONAR
	public String NVARCHAR(Integer length) {return translateToDDLType(Type.NVARCHAR, length);}//NOSONAR
	public String OTHER(Integer... lengths) {return translateToDDLType(Type.OTHER, lengths);}//NOSONAR
	public String REAL() {return translateToDDLType(Type.REAL);}//NOSONAR
	public String SMALLINT() {return translateToDDLType(Type.SMALLINT);}//NOSONAR
	public String TINYINT() {return translateToDDLType(Type.TINYINT);}//NOSONAR
	public String VARBINARY(Integer... lengths) {return translateToDDLType(Type.VARBINARY, lengths);}//NOSONAR
	public String VARCHAR(Integer length) {return translateToDDLType(Type.VARCHAR, length);}//NOSONAR 
	
	public String LONG(String colName) {return translateToDDLType(colName,Type.BIGINT);}//NOSONAR	
	public String BOOLEAN(String colName) {return translateToDDLType(colName,Type.BOOLEAN);}//NOSONAR 
 	public String DOUBLE(String colName) {return translateToDDLType(colName,Type.DOUBLE);}//NOSONAR
	public String FLOAT(String colName,Integer... lengths) {return translateToDDLType(colName,Type.FLOAT,lengths);}//NOSONAR
	public String INTEGER(String colName) {return translateToDDLType(colName,Type.INTEGER);}//NOSONAR
	public String SHORT(String colName) {return translateToDDLType(colName,Type.SMALLINT);}//NOSONAR
	public String BIGDECIMAL(String colName,Integer precision, Integer scale) {return translateToDDLType(colName,Type.NUMERIC, precision, scale);}//NOSONAR
	public String STRING(String colName,Integer length) {return translateToDDLType(colName,Type.VARCHAR, length);}//NOSONAR 
	public String DATE(String colName) {return translateToDDLType(colName,Type.DATE);}//NOSONAR	
	public String TIME(String colName) {return translateToDDLType(colName,Type.TIME);}//NOSONAR
	public String TIMESTAMP(String colName) {return translateToDDLType(colName,Type.TIMESTAMP);}//NOSONAR 
	
	public String BIGINT(String colName) {return translateToDDLType(colName,Type.BIGINT);}//NOSONAR 
	public String BINARY(String colName,Integer... lengths) {return translateToDDLType(colName,Type.BINARY, lengths);}//NOSONAR
	public String BIT(String colName) {return translateToDDLType(colName,Type.BIT);}//NOSONAR
	public String BLOB(String colName,Integer... lengths) {return translateToDDLType(colName,Type.BLOB, lengths);}//NOSONAR 
	public String CHAR(String colName,Integer... lengths) {return translateToDDLType(colName,Type.CHAR, lengths);}//NOSONAR
	public String CLOB(String colName,Integer... lengths) {return translateToDDLType(colName,Type.CLOB, lengths);}//NOSONAR 
	public String DECIMAL(String colName,Integer... lengths) {return translateToDDLType(colName,Type.DECIMAL, lengths);}//NOSONAR   
	public String JAVA_OBJECT(String colName) {return translateToDDLType(colName,Type.JAVA_OBJECT);}//NOSONAR
	public String LONGNVARCHAR(String colName,Integer length) {return translateToDDLType(colName,Type.LONGNVARCHAR, length);}//NOSONAR
	public String LONGVARBINARY(String colName,Integer... lengths) {return translateToDDLType(colName,Type.LONGVARBINARY, lengths);}//NOSONAR
	public String LONGVARCHAR(String colName,Integer... lengths) {return translateToDDLType(colName,Type.LONGVARCHAR, lengths);}//NOSONAR
	public String NCHAR(String colName,Integer length) {return translateToDDLType(colName,Type.NCHAR, length);}//NOSONAR
	public String NCLOB(String colName) {return translateToDDLType(colName,Type.NCLOB);}//NOSONAR
	public String NUMERIC(String colName,Integer... lengths) {return translateToDDLType(colName,Type.NUMERIC, lengths);}//NOSONAR
	public String NVARCHAR(String colName,Integer length) {return translateToDDLType(colName,Type.NVARCHAR, length);}//NOSONAR
	public String OTHER(String colName,Integer... lengths) {return translateToDDLType(colName,Type.OTHER, lengths);}//NOSONAR
	public String REAL(String colName) {return translateToDDLType(colName,Type.REAL);}//NOSONAR
	public String SMALLINT(String colName) {return translateToDDLType(colName,Type.SMALLINT);}//NOSONAR
	public String TINYINT(String colName) {return translateToDDLType(colName,Type.TINYINT);}//NOSONAR
	public String VARBINARY(String colName,Integer... lengths) {return translateToDDLType(colName,Type.VARBINARY, lengths);}//NOSONAR
	public String VARCHAR(String colName,Integer length) {return translateToDDLType(colName,Type.VARCHAR, length);}//NOSONAR
 	//@formatter:on 

	/**
	 * return dialect's engine 
	 */
	public String engine(String... extraStrings) {
		String value = this.ddlFeatures.tableTypeString;
		if (StrUtils.isEmpty(value))
			return "";
		StringBuilder sb = new StringBuilder(value);
		for (String str : extraStrings)
			sb.append(str);
		return sb.toString();
	}

	/**
	 * An example tell users how to use a top limit SQL for a dialect
	 */
	private static String aTopLimitSqlExample(String template) {
		String result = StrUtils.replaceIgnoreCase(template, "$SQL", "select * from users order by userid");
		result = StrUtils.replaceIgnoreCase(result, "$BODY", "* from users order by userid");
		result = StrUtils.replaceIgnoreCase(result, " " + DISTINCT_TAG, "");
		result = StrUtils.replaceIgnoreCase(result, SKIP_ROWS, "0");
		result = StrUtils.replaceIgnoreCase(result, PAGESIZE, "10");
		result = StrUtils.replaceIgnoreCase(result, TOTAL_ROWS, "10");
		return result;
	}

	/**
	 * SQLServer is complex, don't want re-invent wheel, copy Hibernate's source
	 * code in this project to do the dirty job, that's why this project use
	 * LGPL license
	 */
	private static String processSQLServer(Dialect dialect, int pageNumber, int pageSize, String sql) {
		int skipRows = (pageNumber - 1) * pageSize;
		int totalRows = pageNumber * pageSize;

		RowSelection selection = new RowSelection(skipRows, totalRows);
		String result = null;
		switch (dialect) {
		case SQLServer2005Dialect:
		case SQLServer2008Dialect:
			result = new SQLServer2005LimitHandler().processSql(sql, selection);
			break;
		case SQLServer2012Dialect:
			result = new SQLServer2012LimitHandler().processSql(sql, selection);
			break;
		default:
		}
		result = StringHelper.replace(result, "__hibernate_row_nr__", "_ROW_NUM_");
		// Replace a special top tag
		result = StringHelper.replaceOnce(result, " $Top_Tag(?) ", " TOP(" + totalRows + ") ");
		result = StringHelper.replaceOnce(result, "_ROW_NUM_ >= ? AND _ROW_NUM_ < ?",
				"_ROW_NUM_ >= " + (skipRows + 1) + " AND _ROW_NUM_ < " + (totalRows + 1));
		result = StringHelper.replaceOnce(result, "offset ? rows fetch next ? rows only",
				"offset " + skipRows + " rows fetch next " + pageSize + " rows only");
		result = StringHelper.replaceOnce(result, "offset 0 rows fetch next ? rows only",
				"offset 0 rows fetch next " + pageSize + " rows only");

		if (StrUtils.isEmpty(result))
			return (String) DialectException.throwEX("Unexpected error, please report this bug");
		return result;
	}

	// ====================================================
	// ====================================================

	/**
	 * Create a pagination SQL by given pageNumber, pageSize and SQL<br/>
	 * 
	 * @param pageNumber The page number, start from 1
	 * @param pageSize The page item size
	 * @param sql The original SQL
	 * @return The paginated SQL
	 */

	public String paginate(int pageNumber, int pageSize, String sql) {// NOSONAR
		switch (this) {
		case SQLServer2005Dialect:
		case SQLServer2008Dialect:
		case SQLServer2012Dialect:
			return processSQLServer(this, pageNumber, pageSize, sql);
		default:
		}

		if (!StrUtils.startsWithIgnoreCase(sql, "select "))
			return (String) DialectException.throwEX("SQL should be started with \"select \".");
		String body = sql.substring(7).trim();
		if (StrUtils.isEmpty(body))
			return (String) DialectException.throwEX("SQL body can not be null");

		int skipRows = (pageNumber - 1) * pageSize;
		int skipRowsPlus1 = skipRows + 1;
		int totalRows = pageNumber * pageSize;
		int totalRowsPlus1 = totalRows + 1;
		String useTemplate = this.sqlTemplate;

		// use simple limit ? template if offset is 0
		if (skipRows == 0)
			useTemplate = this.topLimitTemplate;

		if (Dialect.NOT_SUPPORT.equals(useTemplate)) {
			if (!Dialect.NOT_SUPPORT.equals(this.topLimitTemplate))
				return (String) DialectException
						.throwEX("Dialect \"" + this + "\" only support top limit SQL, for example: \""
								+ aTopLimitSqlExample(this.topLimitTemplate) + "\"");
			return (String) DialectException.throwEX("Dialect \"" + this + "\" does not support physical pagination");
		}

		if (useTemplate.contains(DISTINCT_TAG)) {
			// if distinct template use non-distinct sql, delete distinct tag
			if (!StrUtils.startsWithIgnoreCase(body, "distinct "))
				useTemplate = StrUtils.replace(useTemplate, DISTINCT_TAG, "");
			else {
				// if distinct template use distinct sql, use it
				useTemplate = StrUtils.replace(useTemplate, DISTINCT_TAG, "distinct");
				body = body.substring(9);
			}
		}

		// if have $XXX tag, replaced by real values
		String result = StrUtils.replaceIgnoreCase(useTemplate, SKIP_ROWS, String.valueOf(skipRows));
		result = StrUtils.replaceIgnoreCase(result, PAGESIZE, String.valueOf(pageSize));
		result = StrUtils.replaceIgnoreCase(result, TOTAL_ROWS, String.valueOf(totalRows));
		result = StrUtils.replaceIgnoreCase(result, SKIP_ROWS_PLUS1, String.valueOf(skipRowsPlus1));
		result = StrUtils.replaceIgnoreCase(result, TOTAL_ROWS_PLUS1, String.valueOf(totalRowsPlus1));

		// now insert the customer's real full SQL here
		result = StrUtils.replace(result, "$SQL", sql);

		// or only insert the body without "select "
		result = StrUtils.replace(result, "$BODY", body);
		return result;
	}

	/**
	 * @return true if is MySql family
	 */
	public boolean isMySqlFamily() {
		return this.toString().startsWith("MySQL");
	}
	
	/**
	 * @return true if is Infomix family
	 */
	public boolean isInfomixFamily() {
		return this.toString().startsWith("Infomix");
	}

	/**
	 * @return true if is Oracle family
	 */
	public boolean isOracleFamily() {
		return this.toString().startsWith("Oracle");
	}

	/**
	 * @return true if is SQL Server family
	 */
	public boolean isSQLServerFamily() {
		return this.toString().startsWith("SQLServer");
	}

	/**
	 * @return true if is H2 family
	 */
	public boolean isH2Family() {
		return H2Dialect.equals(this);
	}

	/**
	 * @return true if is Postgres family
	 */
	public boolean isPostgresFamily() {
		return this.toString().startsWith("Postgres");
	}

	/**
	 * @return true if is Sybase family
	 */
	public boolean isSybaseFamily() {
		return this.toString().startsWith("Sybase");
	}

	/**
	 * @return true if is DB2 family
	 */
	public boolean isDB2Family() {
		return this.toString().startsWith("DB2");
	}

	/**
	 * @return true if is Derby family
	 */
	public boolean isDerbyFamily() {
		return this.toString().startsWith("Derby");
	}

	// ===============================================
	// Below are new DDL methods
	// ===============================================

	/**
	 * Build a "create table xxxx " DDL String based on this dialect
	 */
	public String createTable(String tableName) {
		return ddlFeatures.createTableString + " " + check(tableName) + " ";
	}

	/**
	 * Build a "drop table xxxx " DDL String based on this dialect
	 */
	public String dropTable(String tableName) {
		return ddlFeatures.dropTableString.replaceFirst("_TABLENAME", tableName);
	}
}
