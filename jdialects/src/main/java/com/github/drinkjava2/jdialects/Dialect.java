/*
* jDialects, a tiny SQL dialect tool 
*
* License: GNU Lesser General Public License (LGPL), version 2.1 or later.
* See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
*/
package com.github.drinkjava2.jdialects;

import java.sql.Connection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.github.drinkjava2.hibernate.pagination.RowSelection;
import com.github.drinkjava2.hibernate.pagination.SQLServer2005LimitHandler;
import com.github.drinkjava2.hibernate.pagination.SQLServer2012LimitHandler;
import com.github.drinkjava2.hibernate.utils.StringHelper;
import com.github.drinkjava2.jdialects.model.AutoIdGenerator;
import com.github.drinkjava2.jdialects.model.Table;
import com.github.drinkjava2.jdialects.tinyjdbc.TinyJdbc;

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
	@Deprecated
	DerbyDialect, // Use other Derby version instead
	@Deprecated
	OracleDialect, // Use Oracle8iDialect instead
	@Deprecated
	Oracle9Dialect, // Use Oracle9i instead
	Cache71Dialect, CUBRIDDialect, DerbyTenFiveDialect, DataDirectOracle9Dialect, DB2Dialect, DB2390Dialect, DB2400Dialect, DerbyTenSevenDialect, DerbyTenSixDialect, FirebirdDialect, FrontBaseDialect, H2Dialect, HANAColumnStoreDialect, HANARowStoreDialect, HSQLDialect, InformixDialect, Informix10Dialect, IngresDialect, Ingres10Dialect, Ingres9Dialect, InterbaseDialect, JDataStoreDialect, MariaDBDialect, MariaDB53Dialect, MckoiDialect, MimerSQLDialect, MySQLDialect, MySQL5Dialect, MySQL55Dialect, MySQL57Dialect, MySQL57InnoDBDialect, MySQL5InnoDBDialect, MySQLInnoDBDialect, MySQLMyISAMDialect, Oracle8iDialect, Oracle9iDialect, Oracle10gDialect, Oracle12cDialect, PointbaseDialect, PostgresPlusDialect, PostgreSQLDialect, PostgreSQL81Dialect, PostgreSQL82Dialect, PostgreSQL9Dialect, PostgreSQL91Dialect, PostgreSQL92Dialect, PostgreSQL93Dialect, PostgreSQL94Dialect, PostgreSQL95Dialect, ProgressDialect, RDMSOS2200Dialect, SAPDBDialect, SQLServerDialect, SQLServer2005Dialect, SQLServer2008Dialect, SQLServer2012Dialect, SybaseDialect, Sybase11Dialect, SybaseAnywhereDialect, SybaseASE15Dialect, SybaseASE157Dialect, TeradataDialect, Teradata14Dialect, TimesTenDialect;// NOSONAR

	private static final String SKIP_ROWS = "$SKIP_ROWS";
	private static final String PAGESIZE = "$PAGESIZE";
	private static final String TOTAL_ROWS = "$TOTAL_ROWS";
	private static final String SKIP_ROWS_PLUS1 = "$SKIP_ROWS_PLUS1";
	private static final String TOTAL_ROWS_PLUS1 = "$TOTAL_ROWS_PLUS1";
	private static final String DISTINCT_TAG = "($DISTINCT)";
	public static final String NOT_SUPPORT = "NOT_SUPPORT";
	private static DialectLogger logger = DialectLogger.getLog(Dialect.class);

	private String sqlTemplate = null;
	private String topLimitTemplate = null;
	protected final Map<Type, String> typeMappings = new EnumMap<>(Type.class);
	protected final Map<String, String> functions = new HashMap<>();

	public final DDLFeatures ddlFeatures = new DDLFeatures();// NOSONAR

	static {
		for (Dialect d : Dialect.values()) {
			d.sqlTemplate = DialectPaginationTemplate.initializePaginSQLTemplate(d);
			d.topLimitTemplate = DialectPaginationTemplate.initializeTopLimitSqlTemplate(d);
			DialectTypeMappingTemplate.initializeTypeMappings(d);
			DDLFeatures.initDDLFeatures(d, d.ddlFeatures);
			// to avoid 65534 limitation of method
			DialectFunctionTemplate.initFunctionTemplates1(d);
			DialectFunctionTemplate.initFunctionTemplates2(d);
			DialectFunctionTemplate.initFunctionTemplates3(d);
			DialectFunctionTemplate.initFunctionTemplates4(d);
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
	 * @param con
	 *            The JDBC Connection
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
	private void checkIfReservedWord(String word) {
		if (ReservedDBWords.isReservedWord(word)) {
			String reservedForDB = ReservedDBWords.reservedForDB(word);
			if (ReservedDBWords.isReservedWord(this, word)) {
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
	public String checkReservedWords(String word) {
		checkIfReservedWord(word);
		return word;
	}

	/**
	 * Check if a word is current dialect or ANSI-SQL's reserved word, if yes
	 * throw exception. if is other database's reserved word, log output a
	 * warning. Otherwise return word itself.
	 */
	public String checkNotEmptyReservedWords(String word, String... errorMSG) {
		if (StrUtils.isEmpty(word)) {
			if (errorMSG.length == 0)
				DialectException.throwEX("Empty value error");
			else
				DialectException.throwEX(errorMSG[0]);
		}
		checkIfReservedWord(word);
		return word;
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

	//@formatter:off shut off eclipse's formatter
	//get DDL type String, for example, if is MySQLDialect, call TYPE_LONG() method will get "bigint" String
	/** Long type column definition, 100% dialects support, recommend use */
	public String TYPE_LONG() {return translateToDDLType(Type.BIGINT);}//NOSONAR	
	
	/** Boolean type column definition, 100% dialects support, recommend use */
	public String TYPE_BOOLEAN() {return translateToDDLType(Type.BOOLEAN);}//NOSONAR 
	
	/** Double type column definition, 100% dialects support, recommend use */
 	public String TYPE_DOUBLE() {return translateToDDLType(Type.DOUBLE);}//NOSONAR
 	
 	/** Float type column definition, 100% dialects support, recommend use */
	public String TYPE_FLOAT(Integer... lengths) {return translateToDDLType(Type.FLOAT, lengths);}//NOSONAR
	
	/** Integer type column definition, 100% dialects support, recommend use */
	public String TYPE_INTEGER() {return translateToDDLType(Type.INTEGER);}//NOSONAR
	
	/** Short type column definition, 100% dialects support, recommend use */
	public String TYPE_SHORT() {return translateToDDLType(Type.SMALLINT);}//NOSONAR
	
	/** BidDecimal type column definition, 100% dialects support, recommend use */
	public String TYPE_BIGDECIMAL(Integer precision, Integer scale) {return translateToDDLType(Type.NUMERIC, precision, scale);}//NOSONAR
	
	/** String type column definition, 100% dialects support, recommend use */
	public String TYPE_STRING(Integer length) {return translateToDDLType(Type.VARCHAR, length);}//NOSONAR 
	
	/** Date type column definition, 100% dialects support, recommend use */
	public String TYPE_DATE() {return translateToDDLType(Type.DATE);}//NOSONAR	
	
	/** Time type column definition, 100% dialects support, recommend use */
	public String TYPE_TIME() {return translateToDDLType(Type.TIME);}//NOSONAR
	
	/** TimeStamp type column definition, 100% dialects support, recommend use */
	public String TYPE_TIMESTAMP() {return translateToDDLType(Type.TIMESTAMP);}//NOSONAR 
	
	/** BigInt type column definition, 100% dialects support, recommend use */
	public String TYPE_BIGINT() {return translateToDDLType(Type.BIGINT);}//NOSONAR 
	
	public String TYPE_BINARY(Integer... lengths) {return translateToDDLType(Type.BINARY, lengths);}//NOSONAR
	public String TYPE_BIT() {return translateToDDLType(Type.BIT);}//NOSONAR
	public String TYPE_BLOB(Integer... lengths) {return translateToDDLType(Type.BLOB, lengths);}//NOSONAR 
	public String TYPE_CHAR(Integer... lengths) {return translateToDDLType(Type.CHAR, lengths);}//NOSONAR
	public String TYPE_CLOB(Integer... lengths) {return translateToDDLType(Type.CLOB, lengths);}//NOSONAR 
	public String TYPE_DECIMAL(Integer... lengths) {return translateToDDLType(Type.DECIMAL, lengths);}//NOSONAR   
	public String TYPE_JAVA_OBJECT() {return translateToDDLType(Type.JAVA_OBJECT);}//NOSONAR
	public String TYPE_LONGNVARCHAR(Integer length) {return translateToDDLType(Type.LONGNVARCHAR, length);}//NOSONAR
	public String TYPE_LONGVARBINARY(Integer... lengths) {return translateToDDLType(Type.LONGVARBINARY, lengths);}//NOSONAR
	public String TYPE_LONGVARCHAR(Integer... lengths) {return translateToDDLType(Type.LONGVARCHAR, lengths);}//NOSONAR
	public String TYPE_NCHAR(Integer length) {return translateToDDLType(Type.NCHAR, length);}//NOSONAR
	public String TYPE_NCLOB() {return translateToDDLType(Type.NCLOB);}//NOSONAR
	public String TYPE_NUMERIC(Integer... lengths) {return translateToDDLType(Type.NUMERIC, lengths);}//NOSONAR
	public String TYPE_NVARCHAR(Integer length) {return translateToDDLType(Type.NVARCHAR, length);}//NOSONAR
	public String TYPE_OTHER(Integer... lengths) {return translateToDDLType(Type.OTHER, lengths);}//NOSONAR
	public String TYPE_REAL() {return translateToDDLType(Type.REAL);}//NOSONAR
	public String TYPE_SMALLINT() {return translateToDDLType(Type.SMALLINT);}//NOSONAR
	public String TYPE_TINYINT() {return translateToDDLType(Type.TINYINT);}//NOSONAR
	public String TYPE_VARBINARY(Integer... lengths) {return translateToDDLType(Type.VARBINARY, lengths);}//NOSONAR
	
	/** Varchar type column definition, 100% dialects support, recommend use */
	public String TYPE_VARCHAR(Integer length) {return translateToDDLType(Type.VARCHAR, length);}//NOSONAR
	
   //@formatter:on 

	//@formatter:off shut off eclipse's formatter
	//functions
	/** ABS() function, 100% dialects support this function */
	public String fn_ABS(Object... args){return FunctionUtils.render(this, "abs", args);}
	/** AVG() function, 100% dialects support this function */
	public String fn_AVG(Object... args){return FunctionUtils.render(this, "avg", args);}
	/** BIT_LENGTH() function, 100% dialects support this function */
	public String fn_BIT_LENGTH(Object... args){return FunctionUtils.render(this, "bit_length", args);}
	/** CAST() function, 100% dialects support this function */
	public String fn_CAST(Object... args){return FunctionUtils.render(this, "cast", args);}
	/** COALESCE() function, 100% dialects support this function */
	public String fn_COALESCE(Object... args){return FunctionUtils.render(this, "coalesce", args);}
	/** COUNT() function, 100% dialects support this function */
	public String fn_COUNT(Object... args){return FunctionUtils.render(this, "count", args);}
	/** DAY() function, 100% dialects support this function */
	public String fn_DAY(Object... args){return FunctionUtils.render(this, "day", args);}
	/** EXTRACT() function, 100% dialects support this function */
	public String fn_EXTRACT(Object... args){return FunctionUtils.render(this, "extract", args);}
	/** HOUR() function, 100% dialects support this function */
	public String fn_HOUR(Object... args){return FunctionUtils.render(this, "hour", args);}
	/** LENGTH() function, 100% dialects support this function */
	public String fn_LENGTH(Object... args){return FunctionUtils.render(this, "length", args);}
	/** LOCATE() function, 100% dialects support this function */
	public String fn_LOCATE(Object... args){return FunctionUtils.render(this, "locate", args);}
	/** LOWER() function, 100% dialects support this function */
	public String fn_LOWER(Object... args){return FunctionUtils.render(this, "lower", args);}
	/** MAX() function, 100% dialects support this function */
	public String fn_MAX(Object... args){return FunctionUtils.render(this, "max", args);}
	/** MIN() function, 100% dialects support this function */
	public String fn_MIN(Object... args){return FunctionUtils.render(this, "min", args);}
	/** MINUTE() function, 100% dialects support this function */
	public String fn_MINUTE(Object... args){return FunctionUtils.render(this, "minute", args);}
	/** MOD() function, 100% dialects support this function */
	public String fn_MOD(Object... args){return FunctionUtils.render(this, "mod", args);}
	/** MONTH() function, 100% dialects support this function */
	public String fn_MONTH(Object... args){return FunctionUtils.render(this, "month", args);}
	/** NULLIF() function, 100% dialects support this function */
	public String fn_NULLIF(Object... args){return FunctionUtils.render(this, "nullif", args);}
	/** SECOND() function, 100% dialects support this function */
	public String fn_SECOND(Object... args){return FunctionUtils.render(this, "second", args);}
	/** SQRT() function, 100% dialects support this function */
	public String fn_SQRT(Object... args){return FunctionUtils.render(this, "sqrt", args);}
	/** STR() function, 100% dialects support this function */
	public String fn_STR(Object... args){return FunctionUtils.render(this, "str", args);}
	/** SUBSTRING() function, 100% dialects support this function */
	public String fn_SUBSTRING(Object... args){return FunctionUtils.render(this, "substring", args);}
	/** SUM() function, 100% dialects support this function */
	public String fn_SUM(Object... args){return FunctionUtils.render(this, "sum", args);}
	/** TRIM() function, 100% dialects support this function */
	public String fn_TRIM(Object... args){return FunctionUtils.render(this, "trim", args);}
	/** UPPER() function, 100% dialects support this function */
	public String fn_UPPER(Object... args){return FunctionUtils.render(this, "upper", args);}
	/** YEAR() function, 100% dialects support this function */
	public String fn_YEAR(Object... args){return FunctionUtils.render(this, "year", args);}
	/** CONCAT() function, 93% dialects support this function */
	public String fn_concat(Object... args){return FunctionUtils.render(this, "concat", args);}
	/** COS() function, 83% dialects support this function */
	public String fn_cos(Object... args){return FunctionUtils.render(this, "cos", args);}
	/** EXP() function, 83% dialects support this function */
	public String fn_exp(Object... args){return FunctionUtils.render(this, "exp", args);}
	/** SIN() function, 83% dialects support this function */
	public String fn_sin(Object... args){return FunctionUtils.render(this, "sin", args);}
	/** LOG() function, 81% dialects support this function */
	public String fn_log(Object... args){return FunctionUtils.render(this, "log", args);}
	/** ROUND() function, 81% dialects support this function */
	public String fn_round(Object... args){return FunctionUtils.render(this, "round", args);}
	/** ATAN() function, 80% dialects support this function */
	public String fn_atan(Object... args){return FunctionUtils.render(this, "atan", args);}
	/** SIGN() function, 80% dialects support this function */
	public String fn_sign(Object... args){return FunctionUtils.render(this, "sign", args);}
	/** ACOS() function, 79% dialects support this function */
	public String fn_acos(Object... args){return FunctionUtils.render(this, "acos", args);}
	/** ASIN() function, 79% dialects support this function */
	public String fn_asin(Object... args){return FunctionUtils.render(this, "asin", args);}
	/** CURRENT_DATE() function, 79% dialects support this function */
	public String fn_current_date(Object... args){return FunctionUtils.render(this, "current_date", args);}
	/** FLOOR() function, 79% dialects support this function */
	public String fn_floor(Object... args){return FunctionUtils.render(this, "floor", args);}
	/** TAN() function, 79% dialects support this function */
	public String fn_tan(Object... args){return FunctionUtils.render(this, "tan", args);}
	/** CURRENT_TIMESTAMP() function, 76% dialects support this function */
	public String fn_current_timestamp(Object... args){return FunctionUtils.render(this, "current_timestamp", args);}
	/** CURRENT_TIME() function, 73% dialects support this function */
	public String fn_current_time(Object... args){return FunctionUtils.render(this, "current_time", args);}
	/** COT() function, 69% dialects support this function */
	public String fn_cot(Object... args){return FunctionUtils.render(this, "cot", args);}
	/** ASCII() function, 68% dialects support this function */
	public String fn_ascii(Object... args){return FunctionUtils.render(this, "ascii", args);}
	/** RTRIM() function, 67% dialects support this function */
	public String fn_rtrim(Object... args){return FunctionUtils.render(this, "rtrim", args);}
	/** LN() function, 65% dialects support this function */
	public String fn_ln(Object... args){return FunctionUtils.render(this, "ln", args);}
	/** LTRIM() function, 65% dialects support this function */
	public String fn_ltrim(Object... args){return FunctionUtils.render(this, "ltrim", args);}
	/** DEGREES() function, 64% dialects support this function */
	public String fn_degrees(Object... args){return FunctionUtils.render(this, "degrees", args);}
	/** RADIANS() function, 63% dialects support this function */
	public String fn_radians(Object... args){return FunctionUtils.render(this, "radians", args);}
	/** RAND() function, 63% dialects support this function */
	public String fn_rand(Object... args){return FunctionUtils.render(this, "rand", args);}
	/** CEIL() function, 61% dialects support this function */
	public String fn_ceil(Object... args){return FunctionUtils.render(this, "ceil", args);}
	/** SOUNDEX() function, 56% dialects support this function */
	public String fn_soundex(Object... args){return FunctionUtils.render(this, "soundex", args);}
	/** USER() function, 56% dialects support this function */
	public String fn_user(Object... args){return FunctionUtils.render(this, "user", args);}
	/** LOG10() function, 52% dialects support this function */
	public String fn_log10(Object... args){return FunctionUtils.render(this, "log10", args);}
	/** SUBSTR() function, 51% dialects support this function */
	public String fn_substr(Object... args){return FunctionUtils.render(this, "substr", args);}
	/** CEILING() function, 49% dialects support this function */
	public String fn_ceiling(Object... args){return FunctionUtils.render(this, "ceiling", args);}
	/** STDDEV() function, 49% dialects support this function */
	public String fn_stddev(Object... args){return FunctionUtils.render(this, "stddev", args);}
	/** NOW() function, 45% dialects support this function */
	public String fn_now(Object... args){return FunctionUtils.render(this, "now", args);}
	/** CHAR_LENGTH() function, 44% dialects support this function */
	public String fn_char_length(Object... args){return FunctionUtils.render(this, "char_length", args);}
	/** CHR() function, 44% dialects support this function */
	public String fn_chr(Object... args){return FunctionUtils.render(this, "chr", args);}
	/** DAYOFYEAR() function, 44% dialects support this function */
	public String fn_dayofyear(Object... args){return FunctionUtils.render(this, "dayofyear", args);}
	/** OCTET_LENGTH() function, 43% dialects support this function */
	public String fn_octet_length(Object... args){return FunctionUtils.render(this, "octet_length", args);}
	/** PI() function, 43% dialects support this function */
	public String fn_pi(Object... args){return FunctionUtils.render(this, "pi", args);}
	/** WEEK() function, 43% dialects support this function */
	public String fn_week(Object... args){return FunctionUtils.render(this, "week", args);}
	/** DAYNAME() function, 41% dialects support this function */
	public String fn_dayname(Object... args){return FunctionUtils.render(this, "dayname", args);}
	/** DAYOFWEEK() function, 41% dialects support this function */
	public String fn_dayofweek(Object... args){return FunctionUtils.render(this, "dayofweek", args);}
	/** LCASE() function, 41% dialects support this function */
	public String fn_lcase(Object... args){return FunctionUtils.render(this, "lcase", args);}
	/** MONTHNAME() function, 41% dialects support this function */
	public String fn_monthname(Object... args){return FunctionUtils.render(this, "monthname", args);}
	/** QUARTER() function, 41% dialects support this function */
	public String fn_quarter(Object... args){return FunctionUtils.render(this, "quarter", args);}
	/** SPACE() function, 41% dialects support this function */
	public String fn_space(Object... args){return FunctionUtils.render(this, "space", args);}
	/** SYSDATE() function, 41% dialects support this function */
	public String fn_sysdate(Object... args){return FunctionUtils.render(this, "sysdate", args);}
	/** UCASE() function, 41% dialects support this function */
	public String fn_ucase(Object... args){return FunctionUtils.render(this, "ucase", args);}
	/** CHAR() function, 39% dialects support this function */
	public String fn_char(Object... args){return FunctionUtils.render(this, "char", args);}
	/** REVERSE() function, 39% dialects support this function */
	public String fn_reverse(Object... args){return FunctionUtils.render(this, "reverse", args);}
	/** HEX() function, 37% dialects support this function */
	public String fn_hex(Object... args){return FunctionUtils.render(this, "hex", args);}
	/** LAST_DAY() function, 37% dialects support this function */
	public String fn_last_day(Object... args){return FunctionUtils.render(this, "last_day", args);}
	/** MD5() function, 37% dialects support this function */
	public String fn_md5(Object... args){return FunctionUtils.render(this, "md5", args);}
	/** TIME() function, 37% dialects support this function */
	public String fn_time(Object... args){return FunctionUtils.render(this, "time", args);}
	/** TIMESTAMP() function, 37% dialects support this function */
	public String fn_timestamp(Object... args){return FunctionUtils.render(this, "timestamp", args);}
	/** DATE() function, 36% dialects support this function */
	public String fn_date(Object... args){return FunctionUtils.render(this, "date", args);}
	/** TRUNC() function, 36% dialects support this function */
	public String fn_trunc(Object... args){return FunctionUtils.render(this, "trunc", args);}
	/** VARIANCE() function, 36% dialects support this function */
	public String fn_variance(Object... args){return FunctionUtils.render(this, "variance", args);}
	/** INITCAP() function, 35% dialects support this function */
	public String fn_initcap(Object... args){return FunctionUtils.render(this, "initcap", args);}
	/** POWER() function, 35% dialects support this function */
	public String fn_power(Object... args){return FunctionUtils.render(this, "power", args);}
	/** DAYOFMONTH() function, 33% dialects support this function */
	public String fn_dayofmonth(Object... args){return FunctionUtils.render(this, "dayofmonth", args);}
	/** ATAN2() function, 31% dialects support this function */
	public String fn_atan2(Object... args){return FunctionUtils.render(this, "atan2", args);}
	/** CHARACTER_LENGTH() function, 31% dialects support this function */
	public String fn_character_length(Object... args){return FunctionUtils.render(this, "character_length", args);}
	/** CURDATE() function, 29% dialects support this function */
	public String fn_curdate(Object... args){return FunctionUtils.render(this, "curdate", args);}
	/** CURTIME() function, 29% dialects support this function */
	public String fn_curtime(Object... args){return FunctionUtils.render(this, "curtime", args);}
	/** DATEDIFF() function, 29% dialects support this function */
	public String fn_datediff(Object... args){return FunctionUtils.render(this, "datediff", args);}
	/** REPLACE() function, 29% dialects support this function */
	public String fn_replace(Object... args){return FunctionUtils.render(this, "replace", args);}
	/** TO_DATE() function, 29% dialects support this function */
	public String fn_to_date(Object... args){return FunctionUtils.render(this, "to_date", args);}
	/** LOCALTIME() function, 28% dialects support this function */
	public String fn_localtime(Object... args){return FunctionUtils.render(this, "localtime", args);}
	/** LOCALTIMESTAMP() function, 28% dialects support this function */
	public String fn_localtimestamp(Object... args){return FunctionUtils.render(this, "localtimestamp", args);}
	/** NVL() function, 28% dialects support this function */
	public String fn_nvl(Object... args){return FunctionUtils.render(this, "nvl", args);}
	/** TO_CHAR() function, 28% dialects support this function */
	public String fn_to_char(Object... args){return FunctionUtils.render(this, "to_char", args);}
	/** LPAD() function, 27% dialects support this function */
	public String fn_lpad(Object... args){return FunctionUtils.render(this, "lpad", args);}
	/** RPAD() function, 27% dialects support this function */
	public String fn_rpad(Object... args){return FunctionUtils.render(this, "rpad", args);}
	/** BIN() function, 24% dialects support this function */
	public String fn_bin(Object... args){return FunctionUtils.render(this, "bin", args);}
	/** ENCRYPT() function, 24% dialects support this function */
	public String fn_encrypt(Object... args){return FunctionUtils.render(this, "encrypt", args);}
	/** FROM_DAYS() function, 24% dialects support this function */
	public String fn_from_days(Object... args){return FunctionUtils.render(this, "from_days", args);}
	/** LOG2() function, 24% dialects support this function */
	public String fn_log2(Object... args){return FunctionUtils.render(this, "log2", args);}
	/** TIMEDIFF() function, 24% dialects support this function */
	public String fn_timediff(Object... args){return FunctionUtils.render(this, "timediff", args);}
	/** TO_DAYS() function, 24% dialects support this function */
	public String fn_to_days(Object... args){return FunctionUtils.render(this, "to_days", args);}
	/** WEEKOFYEAR() function, 24% dialects support this function */
	public String fn_weekofyear(Object... args){return FunctionUtils.render(this, "weekofyear", args);}
	/** CRC32() function, 23% dialects support this function */
	public String fn_crc32(Object... args){return FunctionUtils.render(this, "crc32", args);}
	/** INSTR() function, 23% dialects support this function */
	public String fn_instr(Object... args){return FunctionUtils.render(this, "instr", args);}
	/** ISNULL() function, 23% dialects support this function */
	public String fn_isnull(Object... args){return FunctionUtils.render(this, "isnull", args);}
	/** LEN() function, 23% dialects support this function */
	public String fn_len(Object... args){return FunctionUtils.render(this, "len", args);}
	/** OCT() function, 23% dialects support this function */
	public String fn_oct(Object... args){return FunctionUtils.render(this, "oct", args);}
	/** TRANSLATE() function, 23% dialects support this function */
	public String fn_translate(Object... args){return FunctionUtils.render(this, "translate", args);}
	/** MICROSECOND() function, 21% dialects support this function */
	public String fn_microsecond(Object... args){return FunctionUtils.render(this, "microsecond", args);}
	/** RIGHT() function, 21% dialects support this function */
	public String fn_right(Object... args){return FunctionUtils.render(this, "right", args);}
	/** CURRENT_USER() function, 20% dialects support this function */
	public String fn_current_user(Object... args){return FunctionUtils.render(this, "current_user", args);}
	/** RANDOM() function, 20% dialects support this function */
	public String fn_random(Object... args){return FunctionUtils.render(this, "random", args);}
	/** SESSION_USER() function, 20% dialects support this function */
	public String fn_session_user(Object... args){return FunctionUtils.render(this, "session_user", args);}
	/** LEFT() function, 19% dialects support this function */
	public String fn_left(Object... args){return FunctionUtils.render(this, "left", args);}
	/** DATE_TRUNC() function, 17% dialects support this function */
	public String fn_date_trunc(Object... args){return FunctionUtils.render(this, "date_trunc", args);}
	/** UNHEX() function, 17% dialects support this function */
	public String fn_unhex(Object... args){return FunctionUtils.render(this, "unhex", args);}
	/** WEEKDAY() function, 17% dialects support this function */
	public String fn_weekday(Object... args){return FunctionUtils.render(this, "weekday", args);}
	/** CURRENT_SCHEMA() function, 16% dialects support this function */
	public String fn_current_schema(Object... args){return FunctionUtils.render(this, "current_schema", args);}
	/** POSITION() function, 16% dialects support this function */
	public String fn_position(Object... args){return FunctionUtils.render(this, "position", args);}
	/** TO_TIMESTAMP() function, 16% dialects support this function */
	public String fn_to_timestamp(Object... args){return FunctionUtils.render(this, "to_timestamp", args);}
	/** ADD_MONTHS() function, 15% dialects support this function */
	public String fn_add_months(Object... args){return FunctionUtils.render(this, "add_months", args);}
	/** BIT_COUNT() function, 15% dialects support this function */
	public String fn_bit_count(Object... args){return FunctionUtils.render(this, "bit_count", args);}
	/** COSH() function, 15% dialects support this function */
	public String fn_cosh(Object... args){return FunctionUtils.render(this, "cosh", args);}
	/** FROM_UNIXTIME() function, 15% dialects support this function */
	public String fn_from_unixtime(Object... args){return FunctionUtils.render(this, "from_unixtime", args);}
	/** GETDATE() function, 15% dialects support this function */
	public String fn_getdate(Object... args){return FunctionUtils.render(this, "getdate", args);}
	/** QUOTE() function, 15% dialects support this function */
	public String fn_quote(Object... args){return FunctionUtils.render(this, "quote", args);}
	/** SEC_TO_TIME() function, 15% dialects support this function */
	public String fn_sec_to_time(Object... args){return FunctionUtils.render(this, "sec_to_time", args);}
	/** SINH() function, 15% dialects support this function */
	public String fn_sinh(Object... args){return FunctionUtils.render(this, "sinh", args);}
	/** TANH() function, 15% dialects support this function */
	public String fn_tanh(Object... args){return FunctionUtils.render(this, "tanh", args);}
	/** TIME_TO_SEC() function, 15% dialects support this function */
	public String fn_time_to_sec(Object... args){return FunctionUtils.render(this, "time_to_sec", args);}
	/** TO_NUMBER() function, 15% dialects support this function */
	public String fn_to_number(Object... args){return FunctionUtils.render(this, "to_number", args);}
	/** UNIX_TIMESTAMP() function, 15% dialects support this function */
	public String fn_unix_timestamp(Object... args){return FunctionUtils.render(this, "unix_timestamp", args);}
	/** UTC_DATE() function, 15% dialects support this function */
	public String fn_utc_date(Object... args){return FunctionUtils.render(this, "utc_date", args);}
	/** UTC_TIME() function, 15% dialects support this function */
	public String fn_utc_time(Object... args){return FunctionUtils.render(this, "utc_time", args);}
	/** AGE() function, 13% dialects support this function */
	public String fn_age(Object... args){return FunctionUtils.render(this, "age", args);}
	/** CBRT() function, 13% dialects support this function */
	public String fn_cbrt(Object... args){return FunctionUtils.render(this, "cbrt", args);}
	/** CURRENT_DATABASE() function, 13% dialects support this function */
	public String fn_current_database(Object... args){return FunctionUtils.render(this, "current_database", args);}
	/** DATABASE() function, 13% dialects support this function */
	public String fn_database(Object... args){return FunctionUtils.render(this, "database", args);}
	/** DATENAME() function, 13% dialects support this function */
	public String fn_datename(Object... args){return FunctionUtils.render(this, "datename", args);}
	/** DATE_FORMAT() function, 13% dialects support this function */
	public String fn_date_format(Object... args){return FunctionUtils.render(this, "date_format", args);}
	/** DIFFERENCE() function, 13% dialects support this function */
	public String fn_difference(Object... args){return FunctionUtils.render(this, "difference", args);}
	/** DOW() function, 13% dialects support this function */
	public String fn_dow(Object... args){return FunctionUtils.render(this, "dow", args);}
	/** IFNULL() function, 13% dialects support this function */
	public String fn_ifnull(Object... args){return FunctionUtils.render(this, "ifnull", args);}
	/** MICROSECONDS() function, 13% dialects support this function */
	public String fn_microseconds(Object... args){return FunctionUtils.render(this, "microseconds", args);}
	/** NEXT_DAY() function, 13% dialects support this function */
	public String fn_next_day(Object... args){return FunctionUtils.render(this, "next_day", args);}
	/** ORD() function, 13% dialects support this function */
	public String fn_ord(Object... args){return FunctionUtils.render(this, "ord", args);}
	/** QUOTE_IDENT() function, 13% dialects support this function */
	public String fn_quote_ident(Object... args){return FunctionUtils.render(this, "quote_ident", args);}
	/** QUOTE_LITERAL() function, 13% dialects support this function */
	public String fn_quote_literal(Object... args){return FunctionUtils.render(this, "quote_literal", args);}
	/** REPLICATE() function, 13% dialects support this function */
	public String fn_replicate(Object... args){return FunctionUtils.render(this, "replicate", args);}
	/** SHA() function, 13% dialects support this function */
	public String fn_sha(Object... args){return FunctionUtils.render(this, "sha", args);}
	/** SHA1() function, 13% dialects support this function */
	public String fn_sha1(Object... args){return FunctionUtils.render(this, "sha1", args);}
	/** TIMEOFDAY() function, 13% dialects support this function */
	public String fn_timeofday(Object... args){return FunctionUtils.render(this, "timeofday", args);}
	/** TO_ASCII() function, 13% dialects support this function */
	public String fn_to_ascii(Object... args){return FunctionUtils.render(this, "to_ascii", args);}
	/** TRUNCATE() function, 13% dialects support this function */
	public String fn_truncate(Object... args){return FunctionUtils.render(this, "truncate", args);}
	/** UTC_TIMESTAMP() function, 13% dialects support this function */
	public String fn_utc_timestamp(Object... args){return FunctionUtils.render(this, "utc_timestamp", args);}
	/** YEARWEEK() function, 13% dialects support this function */
	public String fn_yearweek(Object... args){return FunctionUtils.render(this, "yearweek", args);}
	/** DATETIME() function, 12% dialects support this function */
	public String fn_datetime(Object... args){return FunctionUtils.render(this, "datetime", args);}
	/** GETUTCDATE() function, 12% dialects support this function */
	public String fn_getutcdate(Object... args){return FunctionUtils.render(this, "getutcdate", args);}
	/** MONTHS_BETWEEN() function, 12% dialects support this function */
	public String fn_months_between(Object... args){return FunctionUtils.render(this, "months_between", args);}
	/** NVL2() function, 12% dialects support this function */
	public String fn_nvl2(Object... args){return FunctionUtils.render(this, "nvl2", args);}
	/** REPEAT() function, 12% dialects support this function */
	public String fn_repeat(Object... args){return FunctionUtils.render(this, "repeat", args);}
	/** ROWNUM() function, 12% dialects support this function */
	public String fn_rownum(Object... args){return FunctionUtils.render(this, "rownum", args);}
	/** SQUARE() function, 12% dialects support this function */
	public String fn_square(Object... args){return FunctionUtils.render(this, "square", args);}
	/** STUFF() function, 12% dialects support this function */
	public String fn_stuff(Object... args){return FunctionUtils.render(this, "stuff", args);}
	/** BIGINT() function, 11% dialects support this function */
	public String fn_bigint(Object... args){return FunctionUtils.render(this, "bigint", args);}
	/** COMPRESS() function, 11% dialects support this function */
	public String fn_compress(Object... args){return FunctionUtils.render(this, "compress", args);}
	/** DAYS() function, 11% dialects support this function */
	public String fn_days(Object... args){return FunctionUtils.render(this, "days", args);}
	/** DECRYPT() function, 11% dialects support this function */
	public String fn_decrypt(Object... args){return FunctionUtils.render(this, "decrypt", args);}
	/** INSTRB() function, 11% dialects support this function */
	public String fn_instrb(Object... args){return FunctionUtils.render(this, "instrb", args);}
	/** INTEGER() function, 11% dialects support this function */
	public String fn_integer(Object... args){return FunctionUtils.render(this, "integer", args);}
	/** REAL() function, 11% dialects support this function */
	public String fn_real(Object... args){return FunctionUtils.render(this, "real", args);}
	/** ROWID() function, 11% dialects support this function */
	public String fn_rowid(Object... args){return FunctionUtils.render(this, "rowid", args);}
	/** SMALLINT() function, 11% dialects support this function */
	public String fn_smallint(Object... args){return FunctionUtils.render(this, "smallint", args);}
	/** SYSTIMESTAMP() function, 11% dialects support this function */
	public String fn_systimestamp(Object... args){return FunctionUtils.render(this, "systimestamp", args);}
	/** TIMESTAMPADD() function, 11% dialects support this function */
	public String fn_timestampadd(Object... args){return FunctionUtils.render(this, "timestampadd", args);}
	/** TIMESTAMPDIFF() function, 11% dialects support this function */
	public String fn_timestampdiff(Object... args){return FunctionUtils.render(this, "timestampdiff", args);}
	/** VARCHAR() function, 11% dialects support this function */
	public String fn_varchar(Object... args){return FunctionUtils.render(this, "varchar", args);}
	/** ABSVAL() function, 9% dialects support this function */
	public String fn_absval(Object... args){return FunctionUtils.render(this, "absval", args);}
	/** ADDDATE() function, 9% dialects support this function */
	public String fn_adddate(Object... args){return FunctionUtils.render(this, "adddate", args);}
	/** ADDTIME() function, 9% dialects support this function */
	public String fn_addtime(Object... args){return FunctionUtils.render(this, "addtime", args);}
	/** ALLTRIM() function, 9% dialects support this function */
	public String fn_alltrim(Object... args){return FunctionUtils.render(this, "alltrim", args);}
	/** ASC() function, 9% dialects support this function */
	public String fn_asc(Object... args){return FunctionUtils.render(this, "asc", args);}
	/** AT() function, 9% dialects support this function */
	public String fn_at(Object... args){return FunctionUtils.render(this, "at", args);}
	/** BITAND() function, 9% dialects support this function */
	public String fn_bitand(Object... args){return FunctionUtils.render(this, "bitand", args);}
	/** CBOOL() function, 9% dialects support this function */
	public String fn_cbool(Object... args){return FunctionUtils.render(this, "cbool", args);}
	/** CBYTE() function, 9% dialects support this function */
	public String fn_cbyte(Object... args){return FunctionUtils.render(this, "cbyte", args);}
	/** CDATE() function, 9% dialects support this function */
	public String fn_cdate(Object... args){return FunctionUtils.render(this, "cdate", args);}
	/** CDBL() function, 9% dialects support this function */
	public String fn_cdbl(Object... args){return FunctionUtils.render(this, "cdbl", args);}
	/** CDOW() function, 9% dialects support this function */
	public String fn_cdow(Object... args){return FunctionUtils.render(this, "cdow", args);}
	/** CHARMIRR() function, 9% dialects support this function */
	public String fn_charmirr(Object... args){return FunctionUtils.render(this, "charmirr", args);}
	/** CHRTRAN() function, 9% dialects support this function */
	public String fn_chrtran(Object... args){return FunctionUtils.render(this, "chrtran", args);}
	/** CINT() function, 9% dialects support this function */
	public String fn_cint(Object... args){return FunctionUtils.render(this, "cint", args);}
	/** CLNG() function, 9% dialects support this function */
	public String fn_clng(Object... args){return FunctionUtils.render(this, "clng", args);}
	/** CMONTH() function, 9% dialects support this function */
	public String fn_cmonth(Object... args){return FunctionUtils.render(this, "cmonth", args);}
	/** CONCAT_WS() function, 9% dialects support this function */
	public String fn_concat_ws(Object... args){return FunctionUtils.render(this, "concat_ws", args);}
	/** CONV() function, 9% dialects support this function */
	public String fn_conv(Object... args){return FunctionUtils.render(this, "conv", args);}
	/** CRYPT3() function, 9% dialects support this function */
	public String fn_crypt3(Object... args){return FunctionUtils.render(this, "crypt3", args);}
	/** CSNG() function, 9% dialects support this function */
	public String fn_csng(Object... args){return FunctionUtils.render(this, "csng", args);}
	/** CSTR() function, 9% dialects support this function */
	public String fn_cstr(Object... args){return FunctionUtils.render(this, "cstr", args);}
	/** CTOD() function, 9% dialects support this function */
	public String fn_ctod(Object... args){return FunctionUtils.render(this, "ctod", args);}
	/** CTOT() function, 9% dialects support this function */
	public String fn_ctot(Object... args){return FunctionUtils.render(this, "ctot", args);}
	/** DATE_ADD() function, 9% dialects support this function */
	public String fn_date_add(Object... args){return FunctionUtils.render(this, "date_add", args);}
	/** DATE_SUB() function, 9% dialects support this function */
	public String fn_date_sub(Object... args){return FunctionUtils.render(this, "date_sub", args);}
	/** DAYOFWEEK_ISO() function, 9% dialects support this function */
	public String fn_dayofweek_iso(Object... args){return FunctionUtils.render(this, "dayofweek_iso", args);}
	/** DECODE() function, 9% dialects support this function */
	public String fn_decode(Object... args){return FunctionUtils.render(this, "decode", args);}
	/** DELETED() function, 9% dialects support this function */
	public String fn_deleted(Object... args){return FunctionUtils.render(this, "deleted", args);}
	/** DIGITS() function, 9% dialects support this function */
	public String fn_digits(Object... args){return FunctionUtils.render(this, "digits", args);}
	/** DOUBLE() function, 9% dialects support this function */
	public String fn_double(Object... args){return FunctionUtils.render(this, "double", args);}
	/** DTOC() function, 9% dialects support this function */
	public String fn_dtoc(Object... args){return FunctionUtils.render(this, "dtoc", args);}
	/** DTOT() function, 9% dialects support this function */
	public String fn_dtot(Object... args){return FunctionUtils.render(this, "dtot", args);}
	/** EMPTY() function, 9% dialects support this function */
	public String fn_empty(Object... args){return FunctionUtils.render(this, "empty", args);}
	/** ENCODE() function, 9% dialects support this function */
	public String fn_encode(Object... args){return FunctionUtils.render(this, "encode", args);}
	/** FLOAT() function, 9% dialects support this function */
	public String fn_float(Object... args){return FunctionUtils.render(this, "float", args);}
	/** GOMONTH() function, 9% dialects support this function */
	public String fn_gomonth(Object... args){return FunctionUtils.render(this, "gomonth", args);}
	/** INT() function, 9% dialects support this function */
	public String fn_int(Object... args){return FunctionUtils.render(this, "int", args);}
	/** ISALPHA() function, 9% dialects support this function */
	public String fn_isalpha(Object... args){return FunctionUtils.render(this, "isalpha", args);}
	/** ISBLANK() function, 9% dialects support this function */
	public String fn_isblank(Object... args){return FunctionUtils.render(this, "isblank", args);}
	/** ISDIGIT() function, 9% dialects support this function */
	public String fn_isdigit(Object... args){return FunctionUtils.render(this, "isdigit", args);}
	/** JULIAN_DAY() function, 9% dialects support this function */
	public String fn_julian_day(Object... args){return FunctionUtils.render(this, "julian_day", args);}
	/** MID() function, 9% dialects support this function */
	public String fn_mid(Object... args){return FunctionUtils.render(this, "mid", args);}
	/** MIDNIGHT_SECONDS() function, 9% dialects support this function */
	public String fn_midnight_seconds(Object... args){return FunctionUtils.render(this, "midnight_seconds", args);}
	/** MILLISECOND() function, 9% dialects support this function */
	public String fn_millisecond(Object... args){return FunctionUtils.render(this, "millisecond", args);}
	/** PADC() function, 9% dialects support this function */
	public String fn_padc(Object... args){return FunctionUtils.render(this, "padc", args);}
	/** PADIANS() function, 9% dialects support this function */
	public String fn_padians(Object... args){return FunctionUtils.render(this, "padians", args);}
	/** PADL() function, 9% dialects support this function */
	public String fn_padl(Object... args){return FunctionUtils.render(this, "padl", args);}
	/** PADR() function, 9% dialects support this function */
	public String fn_padr(Object... args){return FunctionUtils.render(this, "padr", args);}
	/** POSSTR() function, 9% dialects support this function */
	public String fn_posstr(Object... args){return FunctionUtils.render(this, "posstr", args);}
	/** POW() function, 9% dialects support this function */
	public String fn_pow(Object... args){return FunctionUtils.render(this, "pow", args);}
	/** PROPER() function, 9% dialects support this function */
	public String fn_proper(Object... args){return FunctionUtils.render(this, "proper", args);}
	/** RECCOUNT() function, 9% dialects support this function */
	public String fn_reccount(Object... args){return FunctionUtils.render(this, "reccount", args);}
	/** RECNO() function, 9% dialects support this function */
	public String fn_recno(Object... args){return FunctionUtils.render(this, "recno", args);}
	/** ROWLOCKED() function, 9% dialects support this function */
	public String fn_rowlocked(Object... args){return FunctionUtils.render(this, "rowlocked", args);}
	/** STRCAT() function, 9% dialects support this function */
	public String fn_strcat(Object... args){return FunctionUtils.render(this, "strcat", args);}
	/** STRCMP() function, 9% dialects support this function */
	public String fn_strcmp(Object... args){return FunctionUtils.render(this, "strcmp", args);}
	/** STRCONV() function, 9% dialects support this function */
	public String fn_strconv(Object... args){return FunctionUtils.render(this, "strconv", args);}
	/** STRTRAN() function, 9% dialects support this function */
	public String fn_strtran(Object... args){return FunctionUtils.render(this, "strtran", args);}
	/** SUBDATE() function, 9% dialects support this function */
	public String fn_subdate(Object... args){return FunctionUtils.render(this, "subdate", args);}
	/** SUBSTRB() function, 9% dialects support this function */
	public String fn_substrb(Object... args){return FunctionUtils.render(this, "substrb", args);}
	/** SUB_TIME() function, 9% dialects support this function */
	public String fn_sub_time(Object... args){return FunctionUtils.render(this, "sub_time", args);}
	/** TIMESTAMP_ISO() function, 9% dialects support this function */
	public String fn_timestamp_iso(Object... args){return FunctionUtils.render(this, "timestamp_iso", args);}
	/** TTOC() function, 9% dialects support this function */
	public String fn_ttoc(Object... args){return FunctionUtils.render(this, "ttoc", args);}
	/** TTOD() function, 9% dialects support this function */
	public String fn_ttod(Object... args){return FunctionUtils.render(this, "ttod", args);}
	/** UID() function, 9% dialects support this function */
	public String fn_uid(Object... args){return FunctionUtils.render(this, "uid", args);}
	/** UNCOMPRESS() function, 9% dialects support this function */
	public String fn_uncompress(Object... args){return FunctionUtils.render(this, "uncompress", args);}
	/** WEEK_ISO() function, 9% dialects support this function */
	public String fn_week_iso(Object... args){return FunctionUtils.render(this, "week_iso", args);} 
	 //@formatter:on 

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
	 * @param pageNumber
	 *            The page number, start from 1
	 * @param pageSize
	 *            The page item size
	 * @param trimedSql
	 *            The original SQL
	 * @return The paginated SQL
	 */
	public String paginate(int pageNumber, int pageSize, String sql) {// NOSONAR
		DialectException.assureNotNull(sql, "sql string can not be null");
		String trimedSql = sql.trim();
		DialectException.assureNotEmpty(trimedSql, "sql string can not be empty");
		switch (this) {
		case SQLServer2005Dialect:
		case SQLServer2008Dialect:
		case SQLServer2012Dialect:
			return processSQLServer(this, pageNumber, pageSize, trimedSql);
		default:
		}

		if (!StrUtils.startsWithIgnoreCase(trimedSql, "select "))
			return (String) DialectException.throwEX("SQL should start with \"select \".");
		String body = trimedSql.substring(7).trim();
		DialectException.assureNotEmpty(body, "SQL body can not be empty");

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
		result = StrUtils.replace(result, "$SQL", trimedSql);

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
	 * Transfer tables to formatted create DDL
	 */
	public String[] toCreateDDL(Table... tables) {
		return DDLCreateUtils.toCreateDDL(this, tables);
	}

	/**
	 * Transfer tables to formatted drop DDL
	 */
	public String[] toDropDDL(Table... tables) {
		return DDLDropUtils.toDropDDL(this, tables);
	}

	/**
	 * Transfer tables to drop and create DDL String array
	 */
	public String[] toDropAndCreateDDL(Table... tables) {
		String[] drop = DDLDropUtils.toDropDDL(this, tables);
		String[] create = DDLCreateUtils.toCreateDDL(this, tables);
		return StrUtils.joinStringArray(drop, create);
	}

	/**
	 * Build a "drop table xxxx " like DDL String according this dialect
	 */
	public String dropTableDDL(String tableName) {
		return ddlFeatures.dropTableString.replaceFirst("_TABLENAME", tableName);
	}

	/**
	 * Build a "drop sequence xxxx " like DDL String according this dialect
	 */
	public String dropSequenceDDL(String sequenceName) {
		if (DDLFeatures.isValidDDLTemplate(ddlFeatures.dropSequenceStrings))
			return StrUtils.replace(ddlFeatures.dropSequenceStrings, "_SEQNAME", sequenceName);
		else
			return (String) DialectException.throwEX("Dialect \"" + this
					+ "\" does not support drop sequence ddl, on sequence \"" + sequenceName + "\"");
	}

	/**
	 * Build a "alter table tableName drop foreign key fkeyName " like DDL
	 * String according this dialect
	 */
	public String dropFKeyDDL(String tableName, String fkeyName) {
		if (DDLFeatures.isValidDDLTemplate(ddlFeatures.dropForeignKeyString))
			return "alter table " + tableName + " " + ddlFeatures.dropForeignKeyString + " " + fkeyName;
		else
			return (String) DialectException.throwEX(
					"Dialect \"" + this + "\" does not support drop foreign key, on foreign key \"" + fkeyName + "\"");
	}

	/**
	 * Get a Long type Auto-Generated ID from sequence or "jdialects_autoid"
	 * table. <br/>
	 * Note: need run inside of a transaction, because if fetch generated ID
	 * from "jdialects_autoid" table, need run 2 SQLs: <br/>
	 * update jdialects_autoid set next_val=next_val+1 <br/>
	 * select next_val from jdialects_autoid <br/>
	 */
	public Long getNextAutoID(Connection connection) {
		if (ddlFeatures.supportBasicOrPooledSequence()) {
			String sql = StrUtils.replace(ddlFeatures.sequenceNextValString, "_SEQNAME",
					AutoIdGenerator.JDIALECTS_AUTOID);
			return (Long) TinyJdbc.queryForObject(connection, sql);
		} else {
			String sql = "update " + AutoIdGenerator.JDIALECTS_AUTOID + " set " + AutoIdGenerator.NEXT_VAL + "=("
					+ AutoIdGenerator.NEXT_VAL + "+1)";
			int updatedCount = TinyJdbc.executeUpdate(connection, sql);
			if (updatedCount != 1)
				DialectException.throwEX("Exception found when update " + AutoIdGenerator.JDIALECTS_AUTOID + " table");
			Long result = (Long) TinyJdbc.queryForObject(connection,
					"select " + AutoIdGenerator.NEXT_VAL + " from " + AutoIdGenerator.JDIALECTS_AUTOID);
			DialectException.assureNotNull(result, "Exception found when fetch Auto-Generated ID");
			return result;
		}
	}
}
