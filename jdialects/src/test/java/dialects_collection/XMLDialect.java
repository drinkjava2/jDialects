package dialects_collection;

import java.sql.Types;

/**
 * An SQL dialect for HXTT XML.
 */
public class XMLDialect extends HxttDialect {

    public XMLDialect() {
        super();
        //complete map
        registerColumnType( Types.BIT, "boolean" );
        registerColumnType( Types.CHAR, "varchar($l)" );
        registerColumnType( Types.VARCHAR, "varchar($l)" );
        //registerColumnType(Types.VARCHAR, 255, "varchar($l)");
        registerColumnType(Types.LONGVARCHAR, "longvarchar");
        registerColumnType(Types.NUMERIC, "numeric($p,$s)");
        registerColumnType(Types.BOOLEAN, "boolean");
        registerColumnType( Types.BIGINT, "bigint" );
        registerColumnType( Types.SMALLINT, "numeric(5,0)" ); // HXTT XML DON'T SUPPORT SMALLINT
        registerColumnType( Types.TINYINT, "numeric(3,0)" );   // HXTT XML DON'T SUPPORT TINYINT
        registerColumnType(Types.INTEGER, "integer");
        registerColumnType(Types.REAL, "real");
        registerColumnType( Types.FLOAT, "float" );           // HXTT XML DON'T SUPPORT FLOAT ,it will be a double type
        registerColumnType(Types.DOUBLE, "double");
        registerColumnType( Types.BINARY, "binary" );
        registerColumnType(Types.VARBINARY, "varbinary");
        registerColumnType( Types.LONGVARBINARY, "longvarbinary");

        registerColumnType( Types.DATE, "date" );
        registerColumnType( Types.TIME, "time" );
        registerColumnType(Types.TIMESTAMP, "timestamp");
        registerColumnType( Types.BLOB, "blob" ); // BLOB COLUMN WILL CHANGE TO  JAVA_OBJECT TYPE COLUMN
        registerColumnType( Types.CLOB, "clob" ); // CLOB COLUMN WILL CHANGE TO  LONGVARCHAR TYPE COLUMN
        //registerColumnType(Types.OTHER, "currency");
        //registerColumnType( Types.OTHER, "graphics" );
        //registerColumnType(Types.OTHER, "ole");
        //registerColumnType( Types.JAVA_OBJECT, "java_object" );
        registerColumnType( Types.JAVA_OBJECT, "longvarchar" );//2011-06-02 changed for copy data to other database

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
