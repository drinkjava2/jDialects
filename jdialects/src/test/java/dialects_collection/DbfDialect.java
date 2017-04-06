package dialects_collection;

import java.sql.Types;

/**
 * An SQL dialect for HXTT DBF.
 */
public class DbfDialect extends HxttDialect {

	public DbfDialect() {
		super();
        //complete map
        registerColumnType( Types.BIT, "boolean" );
        registerColumnType( Types.CHAR, "varchar($l)" );
        registerColumnType( Types.VARCHAR, "varchar($l)" );
        //registerColumnType( Types.VARCHAR,255, "varchar($l)" );
        registerColumnType( Types.LONGVARCHAR,  "longvarchar");
        registerColumnType( Types.NUMERIC, "numeric($p,$s)" );
        registerColumnType( Types.BOOLEAN, "boolean" );
       	registerColumnType( Types.BIGINT, "integer");//numeric(19,0)" );  // HXTT DBF DON'T SUPPORT BIGINT
        registerColumnType( Types.SMALLINT, "smallint");//"numeric(5,0)" ); // HXTT DBF DON'T SUPPORT SMALLINT
        registerColumnType( Types.TINYINT, "tinyint");//"numeric(3,0)" );   // HXTT DBF DON'T SUPPORT TINYINT
	registerColumnType( Types.INTEGER, "integer" );
	registerColumnType( Types.FLOAT, "float" );           // HXTT DBF DON'T SUPPORT FLOAT ,it will be a double type
	registerColumnType( Types.DOUBLE, "double" );
        registerColumnType( Types.BINARY, 255, "binary" );
 	registerColumnType( Types.VARBINARY, 255, "varbinary" );
        registerColumnType( Types.LONGVARBINARY, "longvarbinary");

	registerColumnType( Types.DATE, "date" );
        // registerColumnType( Types.TIME, "time" );   //  HXTT DBF DON'T SUPPORT TIME
	registerColumnType( Types.TIMESTAMP, "timestamp" );
        registerColumnType( Types.BLOB, "blob" ); // BLOB COLUMN WILL CHANGE TO  JAVA_OBJECT TYPE COLUMN
        registerColumnType( Types.CLOB, "clob" ); // CLOB COLUMN WILL CHANGE TO  LONGVARCHAR TYPE COLUMN
        registerColumnType( Types.OTHER, "currency" );
//        registerColumnType( Types.OTHER, "graphics" );
        registerColumnType( Types.OTHER, "blob" );
        registerColumnType( Types.JAVA_OBJECT, "java_object" );
    }





}
