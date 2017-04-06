package dialects_collection;

import java.sql.Types;

/**
 * An SQL dialect for HXTT Access.
 */
public class AccessDialect  extends HxttDialect {

    public AccessDialect() {
        super();
        //complete map
        registerColumnType( Types.BIT, "boolean" );
        registerColumnType( Types.CHAR, "varchar($l)" );
        registerColumnType( Types.VARCHAR, "varchar($l)" );
        //registerColumnType(Types.VARCHAR, 255, "varchar($l)");
        registerColumnType(Types.LONGVARCHAR, "longvarchar");
        registerColumnType(Types.NUMERIC, "numeric($p,$s)");
        registerColumnType(Types.BOOLEAN, "boolean");
        registerColumnType( Types.BIGINT, "integer");//numeric(19,0)" );  // MS Access DON'T SUPPORT BIGINT
        registerColumnType(Types.TINYINT, "tinyint"); //HXTT Access' tinyint is from 0 to 255
        registerColumnType(Types.SMALLINT, "smallint");
        registerColumnType(Types.INTEGER, "integer");
        registerColumnType(Types.REAL, "real");
        registerColumnType( Types.FLOAT, "float" ); // HXTT Access DON'T SUPPORT FLOAT ,it will be a double type
        registerColumnType(Types.DOUBLE, "double");
        registerColumnType( Types.BINARY, 255, "binary" );
        registerColumnType(Types.VARBINARY, 255, "varbinary");
        registerColumnType( Types.LONGVARBINARY, "longvarbinary");

        registerColumnType( Types.DATE, "date" );
        registerColumnType( Types.TIME, "timestamp");//time" );   //  HXTT Access DON'T SUPPORT TIME
        registerColumnType(Types.TIMESTAMP, "timestamp");
        registerColumnType( Types.BLOB, "blob" ); // BLOB COLUMN WILL CHANGE TO  JAVA_OBJECT TYPE COLUMN
//        registerColumnType( Types.CLOB, "clob" ); // CLOB COLUMN WILL CHANGE TO  LONGVARCHAR TYPE COLUMN
        registerColumnType( Types.CLOB, "longvarchar" ); // CLOB COLUMN WILL CHANGE TO  LONGVARCHAR TYPE COLUMN
        registerColumnType(Types.OTHER, "currency");
        //registerColumnType( Types.OTHER, "graphics" );
//        registerColumnType(Types.OTHER, "ole");
        registerColumnType(Types.BLOB, "ole");
        registerColumnType( Types.JAVA_OBJECT, "java_object" );

    }


    public boolean supportsCascadeDelete() {
        return true;//false;
    }
}

