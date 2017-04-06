package com.hxtt.support.hibernate;

import java.sql.Types;

/**
 * An SQL dialect for HXTT Cobol.
 */
public class CobolDialect extends HxttDialect {

    public CobolDialect() {
        super();
        //complete map
        registerColumnType( Types.BIT, "boolean" );
        registerColumnType( Types.CHAR, "varchar($l)" );
        registerColumnType( Types.VARCHAR, "varchar($l)" );
        //registerColumnType(Types.VARCHAR, 254, "varchar($l)");
        registerColumnType( Types.LONGVARCHAR,  "longvarchar");
        registerColumnType( Types.NUMERIC, "numeric($p,$s)" );
        registerColumnType( Types.DECIMAL, "decimal($p,$s)" );
        registerColumnType( Types.BIGINT, "bigint" );
        registerColumnType( Types.SMALLINT, "smallint" );
        registerColumnType( Types.TINYINT, "tinyint" );
        registerColumnType( Types.BOOLEAN, "boolean" );
        registerColumnType( Types.INTEGER, "integer" );

        registerColumnType( Types.FLOAT, "float" );
        registerColumnType( Types.DOUBLE, "double" );
        registerColumnType( Types.BINARY,  "binary" );
        registerColumnType( Types.VARBINARY,  "varbinary" );
        registerColumnType( Types.LONGVARBINARY, "longvarbinary");

        registerColumnType( Types.DATE, "date" );
        registerColumnType( Types.TIME, "time" );
        registerColumnType( Types.TIMESTAMP, "timestamp" );
        registerColumnType( Types.BLOB, "blob" );
        registerColumnType( Types.CLOB, "clob" );
        registerColumnType( Types.JAVA_OBJECT, "java_object" );
    }

    /**
     * Does this dialect support the <tt>ALTER TABLE</tt> syntax?
     *
     * @return True if we support altering of tables; false otherwise.
     */
    public boolean hasAlterTable() {
            return false;//true;
    }
    

    /**
     * Does this dialect support identity column key generation?
     *
     * @return True if IDENTITY columns are supported; false otherwise.
     */
    public boolean supportsIdentityColumns() {
            return false;
    }

    /**
     * Whether this dialect have an Identity clause added to the data type or a
     * completely seperate identity data type
     *
     * @return boolean
     */
    public boolean hasDataTypeInIdentityColumn() {
            return false;
    }
}
