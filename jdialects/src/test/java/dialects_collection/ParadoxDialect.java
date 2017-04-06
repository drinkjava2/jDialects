package dialects_collection;

import java.sql.Types;

/**
 * An SQL dialect for HXTT Paradox.
 */
public class ParadoxDialect  extends HxttDialect {

    public ParadoxDialect() {
        super();
        //complete map
        registerColumnType( Types.BIT, "boolean" );
        registerColumnType( Types.CHAR, "varchar($l)" );
        registerColumnType( Types.VARCHAR, "varchar($l)" );
        //registerColumnType(Types.VARCHAR, 254, "varchar($l)");
        registerColumnType(Types.LONGVARCHAR, "longvarchar");
        registerColumnType(Types.NUMERIC, "numeric($p,$s)");
        registerColumnType(Types.BOOLEAN, "boolean");
        registerColumnType( Types.BIGINT, "integer");//numeric(19,0)" );  // Hxtt Paradox DON'T SUPPORT BIGINT
        registerColumnType(Types.SMALLINT, "smallint");
        registerColumnType( Types.TINYINT, "tinyint");//numeric(3,0)" );   // Hxtt Paradox DON'T SUPPORT TINYINT
        registerColumnType(Types.INTEGER, "integer");
        registerColumnType( Types.FLOAT, "float" );           // Hxtt Paradox DON'T SUPPORT FLOAT ,it will be a double type
        registerColumnType(Types.DOUBLE, "double");
        registerColumnType( Types.BINARY, "binary" );    // BLOB COLUMN WILL CHANGE TO  VARBINARY TYPE COLUMN
        registerColumnType(Types.VARBINARY, "varbinary");
        registerColumnType(Types.LONGVARBINARY, "longvarbinary");

        registerColumnType(Types.DATE, "date");
        registerColumnType(Types.TIME, "time");
        registerColumnType(Types.TIMESTAMP, "timestamp");
        registerColumnType( Types.BLOB, "blob" ); // BLOB COLUMN WILL CHANGE TO  LONGVARBINARY TYPE COLUMN
        registerColumnType( Types.CLOB, "clob" ); // CLOB COLUMN WILL CHANGE TO  LONGVARCHAR TYPE COLUMN
        registerColumnType(Types.OTHER, "currency");
        registerColumnType(Types.OTHER, "graphics");
        registerColumnType(Types.OTHER, "ole");
        registerColumnType( Types.JAVA_OBJECT, "java_object" );   // BLOB COLUMN WILL CHANGE TO  LONGVARBINARY TYPE COLUMN


    }

}
