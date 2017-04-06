package com.hxtt.support.hibernate;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.dialect.Dialect;
//@author Gavin King, David Channon steve.ebersole@jboss.com
//http://viewvc.jboss.org/cgi-bin/viewvc.cgi/hibernate/core/trunk/core/src/main/java/org/hibernate/dialect/
//http://viewvc.jboss.org/cgi-bin/viewvc.cgi/hibernate/core/trunk/core/src/main/java/org/hibernate/dialect/Dialect.java?view=markup
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;

/**
 * An common SQL  dialect for HXTT JDBC drivers.
 * Written according to Jboss' dialect samples.
 */
public abstract class HxttDialect extends Dialect {
    
    static final String DEFAULT_BATCH_SIZE = "15";
    static final String NO_BATCH = "0";
    
    
    public HxttDialect() {
        super();
        //Mathematical Functions
        registerFunction("abs", new StandardSQLFunction("abs") );
        registerFunction("ceiling", new StandardSQLFunction("ceiling", StandardBasicTypes.INTEGER) );
        registerFunction("ceil", new StandardSQLFunction("ceil", StandardBasicTypes.INTEGER) );
        registerFunction("sign", new StandardSQLFunction("sign", StandardBasicTypes.INTEGER) );
        registerFunction("degrees", new StandardSQLFunction("degrees", StandardBasicTypes.DOUBLE) );
        registerFunction("exp", new StandardSQLFunction("exp", StandardBasicTypes.DOUBLE) );
        registerFunction("floor", new StandardSQLFunction("floor", StandardBasicTypes.INTEGER) );
        registerFunction("int", new StandardSQLFunction("int", StandardBasicTypes.INTEGER) );
        registerFunction("log", new StandardSQLFunction("log", StandardBasicTypes.DOUBLE) );
        registerFunction("log10", new StandardSQLFunction("log10", StandardBasicTypes.DOUBLE) );
        registerFunction("log2", new StandardSQLFunction("log2", StandardBasicTypes.DOUBLE) );
        registerFunction("ln", new StandardSQLFunction("ln", StandardBasicTypes.DOUBLE) );
        registerFunction("mod", new StandardSQLFunction("mod", StandardBasicTypes.INTEGER) );
        registerFunction("pi", new NoArgSQLFunction("pi", StandardBasicTypes.DOUBLE) );
        registerFunction("pow", new StandardSQLFunction("pow", StandardBasicTypes.DOUBLE) );
        registerFunction("power", new StandardSQLFunction("power", StandardBasicTypes.DOUBLE) );
        registerFunction("padians", new StandardSQLFunction("padians", StandardBasicTypes.DOUBLE) );
        registerFunction("radians", new StandardSQLFunction("radians", StandardBasicTypes.DOUBLE) );
        registerFunction("rand", new NoArgSQLFunction("rand", StandardBasicTypes.DOUBLE) );
        registerFunction("round", new StandardSQLFunction("round", StandardBasicTypes.INTEGER) );
        registerFunction("sign", new StandardSQLFunction("sign", StandardBasicTypes.INTEGER) );
        registerFunction("sqrt", new StandardSQLFunction("sqrt", StandardBasicTypes.DOUBLE) );
        registerFunction("trunc", new StandardSQLFunction("trunc", StandardBasicTypes.DOUBLE) );
        registerFunction("truncate", new StandardSQLFunction("truncate", StandardBasicTypes.DOUBLE) );
        //Trigonometric Functions
        registerFunction("acos", new StandardSQLFunction("acos", StandardBasicTypes.DOUBLE) );
        registerFunction("asin", new StandardSQLFunction("asin", StandardBasicTypes.DOUBLE) );
        registerFunction("atan", new StandardSQLFunction("atan", StandardBasicTypes.DOUBLE) );
        registerFunction("atan2", new StandardSQLFunction("atan2", StandardBasicTypes.DOUBLE) );
        registerFunction("cos", new StandardSQLFunction("cos", StandardBasicTypes.DOUBLE) );
        registerFunction("cot", new StandardSQLFunction("cot", StandardBasicTypes.DOUBLE) );
        registerFunction("crc32", new StandardSQLFunction("crc32", StandardBasicTypes.LONG) );
        registerFunction("sin", new StandardSQLFunction("sin", StandardBasicTypes.DOUBLE) );
        registerFunction("tan", new StandardSQLFunction("tan", StandardBasicTypes.DOUBLE) );
        
//String Functions
        registerFunction("alltrim", new StandardSQLFunction("alltrim") );
        registerFunction("asc", new StandardSQLFunction("asc", StandardBasicTypes.INTEGER) );
        registerFunction("ascii", new StandardSQLFunction("ascii", StandardBasicTypes.INTEGER) );
        registerFunction("at", new StandardSQLFunction("at", StandardBasicTypes.INTEGER) );
        registerFunction("bin", new StandardSQLFunction("bin", StandardBasicTypes.STRING) );
        registerFunction("bit_length", new StandardSQLFunction("bit_length", StandardBasicTypes.LONG) );
        registerFunction("char_length", new StandardSQLFunction("char_length", StandardBasicTypes.LONG) );
        registerFunction("character_length", new StandardSQLFunction("character_length", StandardBasicTypes.LONG) );
        registerFunction("char", new StandardSQLFunction("char", StandardBasicTypes.STRING) );
        registerFunction("chr", new StandardSQLFunction("char", StandardBasicTypes.STRING) );
        registerFunction("chrtran", new StandardSQLFunction("chrtran", StandardBasicTypes.STRING) );
        registerFunction( "concat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "", "||", "") );
        registerFunction( "concat_ws", new VarArgsSQLFunction(StandardBasicTypes.STRING, "", "||", "") );
        registerFunction( "conv", new StandardSQLFunction("conv", StandardBasicTypes.STRING) );
        registerFunction( "difference", new StandardSQLFunction("difference", StandardBasicTypes.STRING) );
        registerFunction("hex", new StandardSQLFunction("hex", StandardBasicTypes.STRING) );
        registerFunction("initcap", new StandardSQLFunction("initcap") );
//	registerFunction("insert", new StandardSQLFunction("insert") );
        registerFunction("instr", new StandardSQLFunction("instr", StandardBasicTypes.INTEGER) );
        registerFunction("lcase", new StandardSQLFunction("lcase") );
        registerFunction("left", new StandardSQLFunction("left", StandardBasicTypes.INTEGER) );
        registerFunction("len", new StandardSQLFunction("len", StandardBasicTypes.LONG) );
        registerFunction("length", new StandardSQLFunction("length", StandardBasicTypes.LONG) );
        registerFunction("locate", new StandardSQLFunction("locate", StandardBasicTypes.LONG) );
        registerFunction("lower", new StandardSQLFunction("lower") );
        registerFunction("lpad", new StandardSQLFunction("lpad", StandardBasicTypes.STRING) );
        registerFunction("ltrim", new StandardSQLFunction("ltrim") );
        registerFunction("mid", new StandardSQLFunction("mid", StandardBasicTypes.STRING) );
        registerFunction("oct", new StandardSQLFunction("oct", StandardBasicTypes.STRING) );
        registerFunction("octet_length", new StandardSQLFunction("octet_length", StandardBasicTypes.LONG) );
        registerFunction("padc", new StandardSQLFunction("padc", StandardBasicTypes.STRING) );
        registerFunction("padl", new StandardSQLFunction("padl", StandardBasicTypes.STRING) );
        registerFunction("padr", new StandardSQLFunction("padr", StandardBasicTypes.STRING) );
        registerFunction("position", new StandardSQLFunction("position", StandardBasicTypes.INTEGER) );
        registerFunction("proper", new StandardSQLFunction("proper") )	;
        registerFunction("repeat", new StandardSQLFunction("repeat", StandardBasicTypes.STRING) );
        registerFunction("replicate", new StandardSQLFunction("replicate", StandardBasicTypes.STRING) );
        registerFunction("replace", new StandardSQLFunction("replace", StandardBasicTypes.STRING) );
        registerFunction("right", new StandardSQLFunction("right", StandardBasicTypes.INTEGER) );
        registerFunction("rpad", new StandardSQLFunction("rpad", StandardBasicTypes.STRING) );
        registerFunction("rtrim", new StandardSQLFunction("rtrim") );
        registerFunction("soundex", new StandardSQLFunction("soundex") );
        registerFunction("space", new StandardSQLFunction("space", StandardBasicTypes.STRING) );
        registerFunction( "strcat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "", "||", "") );
        registerFunction("strcmp", new StandardSQLFunction("strcmp", StandardBasicTypes.INTEGER) );
        registerFunction("strconv", new StandardSQLFunction("strconv", StandardBasicTypes.STRING) );
        registerFunction("strtran", new StandardSQLFunction("strtran", StandardBasicTypes.STRING) );
        registerFunction("stuff", new StandardSQLFunction("stuff", StandardBasicTypes.STRING) );
        registerFunction("substr", new StandardSQLFunction("stuff", StandardBasicTypes.STRING) );
        registerFunction("substring", new StandardSQLFunction("substring", StandardBasicTypes.STRING) );
        registerFunction("translate", new StandardSQLFunction("translate", StandardBasicTypes.STRING) );
        registerFunction("trim", new StandardSQLFunction("trim") );
        registerFunction("ucase", new StandardSQLFunction("ucase") );
        registerFunction("upper", new StandardSQLFunction("upper") );
        registerFunction("charmirr", new StandardSQLFunction("charmirr") );
        registerFunction("reverse", new StandardSQLFunction("reverse") );
        
        //Date/Time Functions
        registerFunction("addtime",new StandardSQLFunction("addtime",StandardBasicTypes.TIMESTAMP));
        registerFunction("cdow",new StandardSQLFunction("cdow",StandardBasicTypes.STRING));
        registerFunction("cmonth",new StandardSQLFunction("cmonth",StandardBasicTypes.STRING));
        registerFunction("curdate", new NoArgSQLFunction("curdate", StandardBasicTypes.DATE) );
        registerFunction("curtime", new NoArgSQLFunction("curtime", StandardBasicTypes.TIME) );
        registerFunction("date", new StandardSQLFunction("date", StandardBasicTypes.DATE) );
        registerFunction("datediff", new StandardSQLFunction("datediff", StandardBasicTypes.INTEGER) );
        registerFunction("datetime",new  NoArgSQLFunction("datetime",StandardBasicTypes.TIMESTAMP));
        registerFunction("date_add",new  StandardSQLFunction("date_add",StandardBasicTypes.DATE));
        registerFunction("date_sub",new  StandardSQLFunction("date_sub",StandardBasicTypes.DATE));
        registerFunction("adddate",new  StandardSQLFunction("adddate",StandardBasicTypes.DATE));
        registerFunction("subdate",new  StandardSQLFunction("subdate",StandardBasicTypes.DATE));
        registerFunction("day", new StandardSQLFunction("day", StandardBasicTypes.INTEGER) );
        registerFunction("dayofmonth", new StandardSQLFunction("dayofmonth", StandardBasicTypes.INTEGER) );
        registerFunction("dayname", new StandardSQLFunction("dayname", StandardBasicTypes.STRING) );
        registerFunction("dayofweek", new StandardSQLFunction("dayofweek", StandardBasicTypes.INTEGER) );
        registerFunction("dayofyear", new StandardSQLFunction("dayofyear", StandardBasicTypes.INTEGER) );
        registerFunction("extract",new  StandardSQLFunction("extract",StandardBasicTypes.INTEGER));
        registerFunction("dow",new StandardSQLFunction("dow",StandardBasicTypes.STRING));
        registerFunction("from_days", new StandardSQLFunction("from_days", StandardBasicTypes.DATE) );
        registerFunction("gomonth", new StandardSQLFunction("gomonth", StandardBasicTypes.DATE) );
        registerFunction("hour", new StandardSQLFunction("hour", StandardBasicTypes.INTEGER) );
        registerFunction("last_day", new StandardSQLFunction("last_day", StandardBasicTypes.DATE) );
        registerFunction("minute",new  StandardSQLFunction("minute",StandardBasicTypes.INTEGER));
        registerFunction("millisecond",new  StandardSQLFunction("millisecond",StandardBasicTypes.INTEGER));
        registerFunction("microsecond",new  StandardSQLFunction("microsecond",StandardBasicTypes.INTEGER));
        registerFunction("month",new  StandardSQLFunction("month",StandardBasicTypes.INTEGER));
        registerFunction("monthname",new StandardSQLFunction("monthname",StandardBasicTypes.STRING));
        registerFunction("now", new NoArgSQLFunction("now", StandardBasicTypes.TIMESTAMP) );
        registerFunction("quarter", new StandardSQLFunction("quarter", StandardBasicTypes.INTEGER) );
        registerFunction("second", new StandardSQLFunction("second", StandardBasicTypes.INTEGER) );
        registerFunction("sub_time", new NoArgSQLFunction("sub_time", StandardBasicTypes.TIMESTAMP) );
        registerFunction("sysdate", new NoArgSQLFunction("sysdate", StandardBasicTypes.TIMESTAMP) );
        registerFunction("time", new StandardSQLFunction("time", StandardBasicTypes.TIME) );
        registerFunction("timediff", new StandardSQLFunction("timediff", StandardBasicTypes.TIME) );
        registerFunction("timestamp", new StandardSQLFunction("timestamp", StandardBasicTypes.TIMESTAMP) );
        registerFunction("timestampadd", new StandardSQLFunction("timestampadd", StandardBasicTypes.TIMESTAMP) );
        registerFunction("timestampdiff", new StandardSQLFunction("timestampdiff", StandardBasicTypes.INTEGER) );
        registerFunction("to_days", new StandardSQLFunction("to_days", StandardBasicTypes.INTEGER) );
        registerFunction("week", new StandardSQLFunction("week", StandardBasicTypes.INTEGER) );
        registerFunction("weekofyear", new StandardSQLFunction("weekofyear", StandardBasicTypes.INTEGER) );
        registerFunction("year", new StandardSQLFunction("year", StandardBasicTypes.INTEGER) );
        //boolean functions
        registerFunction("empty", new StandardSQLFunction("empty", StandardBasicTypes.BOOLEAN) );
        registerFunction("isblank", new StandardSQLFunction("isblank", StandardBasicTypes.BOOLEAN) );
        registerFunction("isalpha", new StandardSQLFunction("isalpha", StandardBasicTypes.BOOLEAN) );
        registerFunction("isdigit", new StandardSQLFunction("isdigit", StandardBasicTypes.BOOLEAN) );
        registerFunction("isnull", new StandardSQLFunction("isnull", StandardBasicTypes.BOOLEAN) );
        //Conversion Functions
        registerFunction("cbool", new StandardSQLFunction("cbool", StandardBasicTypes.BOOLEAN) );
        registerFunction("cbyte", new StandardSQLFunction("cbyte", StandardBasicTypes.BYTE) );
        registerFunction("cdate", new StandardSQLFunction("cdate", StandardBasicTypes.DATE) );
        registerFunction("cdbl", new StandardSQLFunction("cdbl", StandardBasicTypes.DOUBLE) );
        registerFunction("cint", new StandardSQLFunction("cint", StandardBasicTypes.INTEGER) );
        registerFunction("clng", new StandardSQLFunction("clng", StandardBasicTypes.LONG) );
        registerFunction("csng", new StandardSQLFunction("csng", StandardBasicTypes.FLOAT) );
        registerFunction("cstr", new StandardSQLFunction("cstr", StandardBasicTypes.STRING) );
        registerFunction("ctod", new StandardSQLFunction("ctod", StandardBasicTypes.DATE) );
        registerFunction("ctot", new StandardSQLFunction("ctot", StandardBasicTypes.TIMESTAMP) );
        registerFunction("dtoc", new StandardSQLFunction("dtoc", StandardBasicTypes.STRING) );
        registerFunction("dtot", new StandardSQLFunction("dtot", StandardBasicTypes.TIMESTAMP) );
        registerFunction("ttoc", new StandardSQLFunction("ttoc", StandardBasicTypes.STRING) );
        registerFunction("ttod", new StandardSQLFunction("ttod", StandardBasicTypes.DATE) );
        //Security Functions
        registerFunction("compress", new StandardSQLFunction("compress", StandardBasicTypes.STRING) );
        registerFunction("uncompress", new StandardSQLFunction("uncompress", StandardBasicTypes.STRING) );
        registerFunction("encrypt", new StandardSQLFunction("encrypt", StandardBasicTypes.STRING) );
        registerFunction("decrypt", new StandardSQLFunction("decrypt", StandardBasicTypes.STRING) );
        registerFunction("encode", new StandardSQLFunction("encode", StandardBasicTypes.STRING) );
        registerFunction("decode", new StandardSQLFunction("decode", StandardBasicTypes.STRING) );
        registerFunction("md5", new StandardSQLFunction("md5", StandardBasicTypes.STRING) );
        registerFunction("crypt3", new StandardSQLFunction("crypt3", StandardBasicTypes.STRING) );
        //System Functions
        registerFunction( "database", new NoArgSQLFunction("database", StandardBasicTypes.STRING, false) );
        registerFunction( "user", new NoArgSQLFunction("user", StandardBasicTypes.STRING, false) );
        registerFunction( "deleted", new NoArgSQLFunction("deleted", StandardBasicTypes.BOOLEAN, false) );
        registerFunction( "reccount", new NoArgSQLFunction("reccount", StandardBasicTypes.LONG, false) );
        registerFunction( "recno", new NoArgSQLFunction("recno", StandardBasicTypes.LONG, false) );
        registerFunction( "rowlocked", new NoArgSQLFunction("rowlocked", StandardBasicTypes.BOOLEAN, false) );
        //Miscellaneous Functions
        registerFunction( "nvl", new StandardSQLFunction("nvl") );
        registerFunction( "ifnull", new StandardSQLFunction("ifnull") );
        
        
        getDefaultProperties().setProperty(Environment.MAX_FETCH_DEPTH, "2");
        getDefaultProperties().setProperty(Environment.STATEMENT_BATCH_SIZE, DEFAULT_BATCH_SIZE);
    }
    
    // SEQUENCE support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /**
     * Does this dialect support sequences?
     *
     * @return True if sequences supported; false otherwise.
     */
    public final boolean supportsSequences() {
        return true;//false;
    }
    
    /**
     * Does this dialect support "pooled" sequences.  Not aware of a better
     * name for this.  Essentially can we specify the initial and increment values?
     *
     * @return True if such "pooled" sequences are supported; false otherwise.
     * @see #getCreateSequenceStrings(String, int, int)
     * @see #getCreateSequenceString(String, int, int)
     */
    public boolean supportsPooledSequences() {
        return true;//false;
    }
    
    /**
     * Generate the appropriate select statement to to retreive the next value
     * of a sequence.
     * <p/>
     * This should be a "stand alone" select statement.
     *
     * @param sequenceName the name of the sequence
     * @return String The "nextval" select string.
     * @throws MappingException If sequences are not supported.
     */
    public final String getSequenceNextValString(String sequenceName) {
        //SELECT NEXTVAL('SEQUENCENAME')
        //"select next_value of " + sequenceName + " from system.onerow";
        return "select " + getSelectSequenceNextValString( sequenceName ) ;
    }
    
    /**
     * Generate the select expression fragment that will retreive the next
     * value of a sequence as part of another (typically DML) statement.
     * <p/>
     * This differs from {@link #getSequenceNextValString(String)} in that this
     * should return an expression usable within another statement.
     *
     * @param sequenceName the name of the sequence
     * @return The "nextval" fragment.
     * @throws MappingException If sequences are not supported.
     */
    public final String getSelectSequenceNextValString(String sequenceName) {
        return  "nextval('" + sequenceName+"')";
    }           
   
    /**
     * Typically dialects which support sequences can create a sequence
     * with a single command.  This is convenience form of
     * {@link #getCreateSequenceStrings} to help facilitate that.
     * <p/>
     * Dialects which support sequences and can create a sequence in a
     * single command need *only* override this method.  Dialects
     * which support sequences but require multiple commands to create
     * a sequence should instead override {@link #getCreateSequenceStrings}.
     *
     * @param sequenceName The name of the sequence
     * @return The sequence creation command
     * @throws MappingException If sequences are not supported.
     */
    public final String getCreateSequenceString(String sequenceName) {
        // create sequence if not exists userID start WITH 100 increment by 2 maxvalue 2000 cache 5 cycle;
        return "create sequence " + sequenceName;        
    }
    
    /**
     * Typically dialects which support sequences can drop a sequence
     * with a single command.  This is convenience form of
     * {@link #getDropSequenceStrings} to help facilitate that.
     * <p/>
     * Dialects which support sequences and can drop a sequence in a
     * single command need *only* override this method.  Dialects
     * which support sequences but require multiple commands to drop
     * a sequence should instead override {@link #getDropSequenceStrings}.
     *
     * @param sequenceName The name of the sequence
     * @return The sequence drop commands
     * @throws MappingException If sequences are not supported.
     */
    public final String getDropSequenceString(String sequenceName) {
        //drop sequence if exists userID;
        return "drop sequence " + sequenceName;
    }
    
    /**
     * Get the select command used retrieve the names of all sequences.
     *
     * @return The select command; or null if sequences are not supported.
     * @see org.hibernate.tool.hbm2ddl.SchemaUpdate
     */
    public final String getQuerySequencesString() {
        return null;
        //select sequence_name from domain.sequences";
        //"select sequence_schema || '.' || sequence_name from information_schema.ext_sequences";
    }
        
  
    // limit/offset support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Does this dialect support some form of limiting query results
     * via a SQL clause?
     *
     * @return True if this dialect supports some form of LIMIT.
     */
    public final boolean supportsLimit() {
        return true;//false;
    }


    /**
     * Apply s limit clause to the query.
     * <p/>
     * Typically dialects utilize {@link #supportsVariableLimit() variable}
     * limit caluses when they support limits.  Thus, when building the
     * select command we do not actually need to know the limit or the offest
     * since we will just be using placeholders.
     * <p/>
     * Here we do still pass along whether or not an offset was specified
     * so that dialects not supporting offsets can generate proper exceptions.
     * In general, dialects will override one or the other of this method and
     * {@link #getLimitString(String, int, int)}.
     *
     * @param query The query to which to apply the limit.
     * @param hasOffset Is the query requesting an offset?
     * @return the modified SQL
     */
    public String getLimitString(String sql, boolean hasOffset) {
        return new StringBuffer(sql.length() + 20)
            .append(sql)
            .append(hasOffset ? " limit ?, ?" : " limit ?")
            .toString();

 /*       sql = sql.trim();
        boolean isForUpdate = false;
        if ( sql.toLowerCase().endsWith(" for update") ) {
            sql = sql.substring( 0, sql.length()-11 );
            isForUpdate = true;
        }

        StringBuffer pagingSelect = new StringBuffer( sql.length()+100 );
        if (hasOffset) {
            pagingSelect.append("select * from ( select row_.*, RECNO() rownum_ from ( ");
        }
        else {
            pagingSelect.append("select * from ( ");
        }
        pagingSelect.append(sql);
        if (hasOffset) {
            pagingSelect.append(" ) row_ where RECNO() <= ?) where rownum_ > ?");
        }
        else {
            pagingSelect.append(" ) where RECNO() <= ?");
        }

        if (isForUpdate) pagingSelect.append(" for update");

        return pagingSelect.toString();*/
    }

    
    // IDENTITY support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Does this dialect support identity column key generation?
     *
     * @return True if IDENTITY columns are supported; false otherwise.
     */
    public boolean supportsIdentityColumns() {
            return true;//false;
    }

    /**
     * Whether this dialect have an Identity clause added to the data type or a
     * completely seperate identity data type
     *
     * @return boolean
     */
/*    public boolean hasDataTypeInIdentityColumn() {
            return true;//false;//true;
    }*/


    
    /**
     * Get the select command to use to retrieve the last generated IDENTITY
     * value for a particuar table
     *
     * @param table The table into which the insert was done
     * @param column The PK column.
     * @param type The {@link java.sql.Types} type code.
     * @return The appropriate select command
     * @throws MappingException If IDENTITY generation is not supported.
     */
    public final String getIdentitySelectString(String table, String column, int type){
        //return getIdentitySelectString();
        return new StringBuffer().append("select currval('")
			.append(table)
			.append("','")
			.append(column)
			.append("')")
			.toString();
/*		return new StringBuffer().append("select currval('")
			.append(table)
			.append('_')
			.append(column)
			.append("_seq')")
			.toString();
 		return type==Types.BIGINT ?
			"select dbinfo('serial8') from systables where tabid=1" :
			"select dbinfo('sqlca.sqlerrd1') from systables where tabid=1";

 */
            
    }

 /*   public String getLimitString(String querySelect, int offset, int limit) {
        int lastIndexOfOrderBy = querySelect.toLowerCase().lastIndexOf("order by ");
        if (lastIndexOfOrderBy < 0 || querySelect.endsWith(")") || offset == 0) {
            return super.getLimitString(querySelect, 0, limit);
        } else {
            String orderby = querySelect.substring(lastIndexOfOrderBy, querySelect.length());
            int indexOfFrom = querySelect.toLowerCase().indexOf("from");
            String selectFld = querySelect.substring(0, indexOfFrom);
            String selectFromTableAndWhere = querySelect.substring(indexOfFrom, lastIndexOfOrderBy);
            StringBuffer sql = new StringBuffer(querySelect.length() + 100);
            sql.append("select * from (")
                    .append(selectFld)
                    .append(",recno() as _page_row_num_hb ")
                    .append(selectFromTableAndWhere).append(" ) temp ")
                    .append(" where  _page_row_num_hb BETWEEN  ")
                    .append(offset + 1).append(" and ").append(limit);
            return sql.toString();
        }
    }    */
    
    /**
     * Get the select command to use to retrieve the last generated IDENTITY
     * value.
     *
     * @return The appropriate select command
     * @throws MappingException If IDENTITY generation is not supported.
     */
/*    public String getIdentitySelectString() {
        //return "select last_insert_id()";
        return "select @@identity";
        //"select identity_val_local() from sysibm.sysdummy1"
        //call identity()"
        //"SELECT LAST_IDENTITY() FROM %TSQL_sys.snf";
    }*/

    /**
     * The syntax used during DDL to define a column as being an IDENTITY of
     * a particular type.
     *
     * @param type The {@link java.sql.Types} type code.
     * @return The appropriate DDL fragment.
     * @throws MappingException If IDENTITY generation is not supported.
     */
/*    public String getIdentityColumnString(int type) {
            return getIdentityColumnString();
/*		return type==Types.BIGINT ?
			"bigserial not null" :
			"serial not null";
 		return type==Types.BIGINT ?
			"serial8 not null" :
			"serial not null";

 * /
            
    }*/

    /**
     * The syntax used during DDL to define a column as being an IDENTITY.
     *
     * @return The appropriate DDL fragment.
     * @throws MappingException If IDENTITY generation is not supported.
     */
    public final String getIdentityColumnString() {
         return "auto_increment not null"; //starts with 1, implicitly
         //return "identity not null"; //starts with 1, implicitly
         //"autoincrement";
         //identity";
         //return "generated by default as identity (start with 1)"; //not null is implicit
    }


    /**
     * The keyword used to insert a generated value into an identity column (or null).
     * Need if the dialect does not support inserts that specify no column values.
     *
     * @return The appropriate keyword.
     */
    public final String getIdentityInsertString() {
        //return null;
        return "null";
    }
    
    // lock acquisition support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Does this dialect support <tt>FOR UPDATE</tt> in conjunction with
     * outer joined rows?
     *
     * @return True if outer joined rows can be locked via <tt>FOR UPDATE</tt>.
     */
    public final boolean supportsOuterJoinForUpdate() {
            return false;//true;//???           
    }
    
    
    // current timestamp support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Does this dialect support a way to retrieve the database's current
     * timestamp value?
     *
     * @return True if the current timestamp can be retrieved; false otherwise.
     */
    public final boolean supportsCurrentTimestampSelection() {
            return true;//false;
    }

    /**
     * Should the value returned by {@link #getCurrentTimestampSelectString}
     * be treated as callable.  Typically this indicates that JDBC escape
     * sytnax is being used...
     *
     * @return True if the {@link #getCurrentTimestampSelectString} return
     * is callable; false otherwise.
     */
    public final boolean isCurrentTimestampSelectStringCallable() {
            return true;//???
    }

    /**
     * Retrieve the command used to retrieve the current timestammp from the
     * database.
     *
     * @return The command.
     */
    public final String getCurrentTimestampSelectString() {
        return "select now()";
        //"select systimestamp from dual"
        //"select now()";
        //"call current_timestamp()"
    }
    
    /**
     * The name of the database-specific SQL function for retrieving the
     * current timestamp.
     *
     * @return The function name.
     */
    public final String getCurrentTimestampSQLFunctionName() {
        // the standard SQL function name is current_timestamp...
        return "now";//current_timestamp";
    }    
    
    // union subclass support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Does this dialect support UNION ALL, which is generally a faster
     * variant of UNION?
     *
     * @return True if UNION ALL is supported; false otherwise.
     */
    public final boolean supportsUnionAll() {
        return true;//false;
    }

    // miscellaneous support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~    
    
    /**
     * The fragment used to insert a row without specifying any column values.
     * This is not possible on some databases.
     *
     * @return The appropriate empty values clause.
     */
    public final String getNoColumnsInsertString() {
            return "values ( )";//???Doesn't support now.'
            //return "default values";
    }
    
    /**
     * The SQL literal value to which this database maps boolean values.
     *
     * @param bool The boolean value
     * @return The appropriate SQL literal.
     */
    public final String toBooleanValueString(boolean bool) {
        return bool ? "true" : "false";
    }    

    // DDL support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Do we need to drop constraints before dropping tables in this dialect?
     *
     * @return True if constraints must be dropped prior to dropping
     * the table; false otherwise.
     */
    public final boolean dropConstraints() {
            return false;//true;
    }
    
    /**
     * Do we need to qualify index names with the schema name?
     *
     * @return boolean
     */
    public final boolean qualifyIndexName() {
            return false;//true;
    }

    /**
     * The syntax used to add a column to a table (optional).
     *
     * @return The "add column" fragment.
     */
    public final String getAddColumnString() {
        //ALTER table TBNAME ADD COLUMN FIELDNAME FIELDTYPE
        return "add column";
    }

    public final String getDropForeignKeyString() {
        throw new UnsupportedOperationException("No drop foreign key foreign supported by Hxtt Dialect");
        //return " drop constraint ";
    }

    /**
     * The syntax used to add a primary key constraint to a table.
     *
     * @param constraintName The name of the PK constraint.
     * @return The "add PK" fragment
     */
    public final String getAddPrimaryKeyConstraintString(String constraintName) {
        return " primary key ";
        //return " add constraint " + constraintName + " primary key ";
    }
    
    
    /**
     * The keyword used to specify a nullable column.
     *
     * @return String
     */
    public final String getNullColumnString() {
        return " null";
    }

    public final boolean supportsIfExistsBeforeTableName() {
        return true;//false;
    }
    
    /**
     * Does this dialect support column-level check constraints?
     *
     * @return True if column-level CHECK constraints are supported; false
     * otherwise.
     */
    public final boolean supportsColumnCheck() {
        return false;//true;//Support little
    }

    /**
     * Does this dialect support table-level check constraints?
     *
     * @return True if table-level CHECK constraints are supported; false
     * otherwise.
     */
    public final boolean supportsTableCheck() {
        return false;//true;
    }
    
    public boolean supportsCascadeDelete() {
        return false;//true; HXTT Access supports
    }    
    
    /**
     * Is this dialect known to support what ANSI-SQL terms "row value
     * constructor" syntax; sometimes called tuple syntax.
     * <p/>
     * Basically, does it support syntax like
     * "... where (FIRST_NAME, LAST_NAME) = ('Steve', 'Ebersole') ...".
     *
     * @return True if this SQL dialect is known to support "row value
     * constructor" syntax; false otherwise.
     * @since 3.2
     */
    public final boolean supportsRowValueConstructorSyntax() {
        // return false here, as most databases do not properly support this construct...
        return true;//false;
    }
   
    /**
     * If the dialect supports {@link #supportsRowValueConstructorSyntax() row values},
     * does it offer such support in IN lists as well?
     * <p/>
     * For example, "... where (FIRST_NAME, LAST_NAME) IN ( (?, ?), (?, ?) ) ..."
     *
     * @return True if this SQL dialect is known to support "row value
     * constructor" syntax in the IN list; false otherwise.
     * @since 3.2
     */
    public final boolean supportsRowValueConstructorSyntaxInInList() {
            return true;//false;
    }

    /**
     * Should LOBs (both BLOB and CLOB) be bound using stream operations (i.e.
     * {@link java.sql.PreparedStatement#setBinaryStream}).
     *
     * @return True if BLOBs and CLOBs should be bound using stream operations.
     * @since 3.2
     */
    public final boolean useInputStreamToInsertBlob() {
            return false;//true;//???
    }
    

    /**
     * Does this dialect support definition of cascade delete constraints
     * which can cause circular chains?
     *
     * @return True if circular cascade delete constraints are supported; false
     * otherwise.
     * @since 3.2
     */
    public final boolean supportsCircularCascadeDeleteConstraints() {
            return false;//true; //??? MS Access doesn't support too?
    }
    
    
}
