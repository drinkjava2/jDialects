## jDialects
License: [LGPL 2.1](http://www.gnu.org/licenses/lgpl-2.1.html)  
  
jDialects is a small java project collect all databases' dialect, most are extracted from Hibernate, usually jDialects is used for build pagination SQL and DDL SQL for cross-databases purpose. Currently jDialects support 75 database dialects include SQLLite and Access. jDialects run on Java7+.  
  
jDialects is built for jSqlBox project, but it can be used for any Java project which used native SQL.  
  
Main part of this project is extracted from Hibernate5.2.9 by a source code generator tool,see project [jDiagen](https://github.com/drinkjava2/jDiagen). And also it build a Excel file "DatabaseDialects.xls", it's useful if you want know difference of databases.  
  
### How to Use jDialects in project?   
Download and put "jdialects-1.0.1.jar" in project class folder, or add below lines in pom.xml: 
```
	<dependency>  
		<groupId>com.github.drinkjava2</groupId>  
		<artifactId>jdialects</artifactId>  
		<version>1.0.1</version>  
	</dependency> 
```
  
### In source code:
   1) Build pagination SQL    
```   
      Dialect dialect=Dialect.MySQL5Dialect;
      //Dialect dialect=guessDialect(connection);  //or guess dialect by given connection
      //Dialect dialect=guessDialect(dataSource);  //or guess dialect by given dataSource
      String result=dialect.paginate(3, 10, "select * from users where id=?");
      
     in MySQL5Dialect, result is: "select * from users  where id=? limit 20, 10"
     in Oracle8iDialect, result is: "select * from ( select row_.*, rownum rownum_ from ( select * from users  where id=? ) row_ ) where rownum_ <= 30 and rownum_ > 20"
     in Oracle12cDialect, result is: "select * from users  where id=? offset 20 rows fetch next 10 rows only"
     in Sybase11Dialect, get a DialectExcepiton with message: "Sybase11Dialect" does not support physical pagination
     ...
```	 
      
   2) Build cross-database DDL SQL: 
```   
	private static String ddlSQL(Dialect d) {
		return "create table " + d.check("BufferPool") + "("//
				+ d.BIGINT("f1") //
				+ ", " + d.BIT("f2", 5) //
				+ ", " + d.BLOB("f3") //
				+ ", " + d.BOOLEAN("f4") //
				+ ", " + d.INTEGER("f5") //
				+ ", " + d.VARCHAR("f6", 8000) //
				+ ", " + d.NUMERIC("ACCESS_LOCK", 8,2) // 
				+ ")" + d.engine(" DEFAULT CHARSET=utf8");
	}

	public static void main(String[] args) {
		System.out.println(ddlSQL(Dialect.MySQL57InnoDBDialect));
		System.out.println(ddlSQL(Dialect.SQLServer2012Dialect));
		System.out.println(ddlSQL(Dialect.Oracle10gDialect));
	} 
```	
  Result is:  
```
   create table BufferPool(f1 bigint, f2 bit, f3 longblob, f4 bit, f5 integer, f6 varchar(8000), ACCESS_LOCK decimal(8,2))engine=innoDB DEFAULT CHARSET=utf8
   create table BufferPool(f1 bigint, f2 bit, f3 varbinary(MAX), f4 bit, f5 int, f6 varchar(MAX), ACCESS_LOCK numeric(8,2))
   create table BufferPool(f1 number(19,0), f2 number(1,0), f3 blob, f4 number(1,0), f5 number(10,0), f6 long, ACCESS_LOCK number(8,2))
```   
   
   For above example, there is a log warning: "BufferPool" is reserved word of DB2, and "ACCESS_LOCK" is reserved word of Teradata, this reminder you change to some other names otherwise if run on DB2Dialect or TeradataDialect will get a DialectException.  
   If want bypass the reserved words checking (not recommended), can write DDL like below:   
```   
	   ddl= "create table BufferPool("//
				+ "f1 "+d.BIGINT() //
				+ ",f2 " + d.BIT(5) //
				+ ",f3 " + d.BLOB() //
				+ ",f4 " + d.BOOLEAN() //
				+ ",f5 " + d.INTEGER() //
				+ ",f6 " + d.VARCHAR(8000) //
				+ ",ACCESS_LOCK " + d.NUMERIC(8,2) // 
				+ ")" + d.engine();
```
    All jDialects supported Types:  
	BOOLEAN  
	DOUBLE  
	FLOAT  
	INTEGER  
	LONG(=BIGINT)  
	SHORT(=SMALLINT)  
	BIGDECIMAL(=NUMERIC)  
	STRING(=VARCHAR)  
	DATE  
	TIME  
	TIMESTAMP  
	BIGINT  
	BINARY  
	BIT  
	BLOB  
	CHAR  
	CLOB  
	DECIMAL  
	LONGNVARCHAR  
	LONGVARBINARY  
	LONGVARCHAR  
	NCHAR  
	NCLOB  
	NUMERIC  
	NVARCHAR  
	REAL  
	SMALLINT  
	TINYINT  
	VARBINARY  
	VARCHAR  
	
### Below are all dialects in jDialects:  
AccessDialect  
Cache71Dialect  
CobolDialect  
CUBRIDDialect  
DataDirectOracle9Dialect  
DB2390Dialect  
DB2400Dialect  
DB2Dialect  
DbfDialect  
DerbyDialect  
DerbyTenFiveDialect  
DerbyTenSevenDialect  
DerbyTenSixDialect  
ExcelDialect  
FirebirdDialect  
FrontBaseDialect  
H2Dialect  
HANAColumnStoreDialect  
HANARowStoreDialect  
HSQLDialect  
Informix10Dialect  
InformixDialect  
Ingres10Dialect  
Ingres9Dialect  
IngresDialect  
InterbaseDialect  
JDataStoreDialect  
MariaDB53Dialect  
MariaDBDialect  
MckoiDialect  
MimerSQLDialect  
MySQL55Dialect  
MySQL57Dialect  
MySQL57InnoDBDialect  
MySQL5Dialect  
MySQL5InnoDBDialect  
MySQLDialect  
MySQLInnoDBDialect  
MySQLMyISAMDialect  
Oracle10gDialect  
Oracle12cDialect  
Oracle8iDialect  
Oracle9Dialect  
Oracle9iDialect  
OracleDialect  
ParadoxDialect  
PointbaseDialect  
PostgresPlusDialect  
PostgreSQL81Dialect  
PostgreSQL82Dialect  
PostgreSQL91Dialect  
PostgreSQL92Dialect  
PostgreSQL93Dialect  
PostgreSQL94Dialect  
PostgreSQL95Dialect  
PostgreSQL9Dialect  
PostgreSQLDialect  
ProgressDialect  
RDMSOS2200Dialect  
SAPDBDialect  
SQLiteDialect  
SQLServer2005Dialect  
SQLServer2008Dialect  
SQLServer2012Dialect  
SQLServerDialect  
Sybase11Dialect  
SybaseAnywhereDialect  
SybaseASE157Dialect  
SybaseASE15Dialect  
SybaseDialect  
Teradata14Dialect  
TeradataDialect  
TextDialect  
TimesTenDialect  
XMLDialect  
