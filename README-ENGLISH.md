## jDialects
License: [LGPL 2.1](http://www.gnu.org/licenses/lgpl-2.1.html)  
  
jDialects is a small java project support ~70 database dialects included SQLite and Access, usually jDialects is used for build pagination SQL and DDL SQL for cross-databases developing. jDialects runs on Java6 or above.  
  
jDialects is a core part of jSqlBox project, but it can be used seperately for any Java project which used JDBC. Main parts of jDialects are extracted from Hibernate5.2.9 project by source code generator tool, see project [jDiagen](https://github.com/drinkjava2/jDiagen). And also it build a Excel file "DatabaseDialects.xls", it's useful if you want compare difference of databases.  
  
### How to Use jDialects in project?   
Download and put file "jdialects-1.0.4.jar" in class folder, or add below in pom.xml: 
```
    <dependency>  
        <groupId>com.github.drinkjava2</groupId>  
        <artifactId>jdialects</artifactId>  
        <version>1.0.4</version>  
    </dependency> 
```
  
### Functions instruction:
1) Build pagination SQL    
```   
      Dialect dialect=Dialect.MySQL5Dialect;
      //Dialect dialect=guessDialect(connection);  //or guess dialect by given connection
      //Dialect dialect=guessDialect(dataSource);  //or guess dialect by given dataSource
      String result=dialect.paginate(3, 10, "select * from users where id=?");
      
     in MySQL5Dialect, result is: "select * from users  where id=? limit 20, 10"
     in Oracle8iDialect, result is: "select * from ( select row_.*, rownum rownum_ from ( select * from users  where id=? ) row_ ) where rownum_ <= 30 and rownum_ > 20"
     in Oracle12cDialect, result is: "select * from users where id=? offset 20 rows fetch next 10 rows only"
     in Sybase11Dialect, get a DialectExcepiton with message: "Sybase11Dialect" does not support physical pagination
     ...
```     
      
2) Build cross-database DDL SQL: 
```   
	public static void main(String[] args) {
		Table t1 = new Table("customers");
		t1.column("name").STRING(20).unique().pkey();
		t1.column("email").STRING(20).pkey();
		t1.column("address").VARCHAR(50).index("IDX1").defaultValue("'Beijing'").comment("address comment");
		t1.column("phoneNumber").VARCHAR(50).index("IDX1","IDX2");
		t1.column("age").INTEGER().notNull().check("'>0'");

		Table t2 = new Table("orders").comment("order comment");
		t2.column("id").LONG().autoID().pkey();
		t2.column("name").STRING(20).fkey("customers", "name", "email");
		t2.column("email").STRING(20).fkey("customers", "name", "email");
		t2.column("name2").STRING(20).unique("A").pkey().tail(" default 'Sam'");
		t2.column("email2").STRING(20).unique("A", "B");
		t2.fkey("name2", "email2").ref("customers", "name", "email");

		Table t3 = new Table("sampletable");
		t3.column("id").LONG().identity().pkey();
		t3.addTableGenerator("table_gen1", "tb1", "pkcol2", "valcol", "pkval", 1, 10);
		t3.column("id1").INTEGER().tableGenerator("table_gen1");
		t3.addSequence("seq1", "seq_1", 1, 1);
		t3.column("id2").INTEGER().sequence("seq1");
		t3.engineTail(" DEFAULT CHARSET=utf8");

		String[] dropAndCreateDDL = Dialect.H2Dialect.toDropAndCreateDDL(t1, t2, t3);
		for (String ddl : dropAndCreateDDL)
			System.out.println(ddl);
	}
```    
*)LONG()、STRING()...methods: column type definition:  
Common: BOOLEAN,DOUBLE,FLOAT,INTEGER,LONG(=BIGINT),SHORT(=SMALLINT),BIGDECIMAL(=NUMERIC),STRING(=VARCHAR),DATE,TIME,TIMESTAMP,BIGINT,VARCHAR  
Un-common：BINARY,BIT,BLOB,CHAR,CLOB,DECIMAL,LONGNVARCHAR,LONGVARBINARY,LONGVARCHAR,NCHAR,NCLOB,NUMERIC,NVARCHAR,REAL,SMALLINT,TINYINT,VARBINARY  
*)pkey(): Primary key definition.  
*)identity(): Identity column, note some databases do not support identity.  
*)unique:  Unique constraint definition.  
*)index(): Index definition.  
*)notNull(): Not null column definition.  
*)check(): Check constraint definition.  
*)defaultValue(): default value definition.  
*)fkey() method: foreign key definition, format: column.fkey(ftable，fcol1, fcol2...).  
*)table's fkey() method: another way to define a foreign key, format: table.fkey(col1,col2).ref(ftable，fcol1,fcol2..).  
*)comment(): comment definition.  
*)autoID(): Similar like JPA's @Auto type, in program using dialect.getNextAutoID(connection) to get a Long type ID.  
*)tableGenerator(): Simliar like JPA's @TableGenerator type.  
*)dialect's toDropDDL(), toCreateDDL() and toDropAndCreateDDL() methods: build DDL String Array。DDLFormatter.format() can be used format DDL.  
*)toCreateDDL() method will run reserved words checking, if found reserved words like "user"、"order" will throw an Exception.  
*)table.engineTail() method: put extra String behind DDL tail only when database(like MySql) support engine。column.tail() add extra String on column definition.  

Result of above example is:
```
alter table orders  drop constraint  fk_orders_name2_email2
alter table orders  drop constraint  fk_orders_name_email
drop table tb1 if exists
drop sequence if exists seq_1
drop sequence if exists jdialects_autoid
drop table customers if exists
drop table orders if exists
drop table sampletable if exists
create table customers (name varchar(20),email varchar(20),address varchar(50) default 'Beijing',phoneNumber varchar(50),age integer not null check ('>0'), primary key (name,email))
alter table customers add constraint UK_customers_name unique (name)
create index IDX1 on customers (address,phoneNumber)
create index IDX2 on customers (phoneNumber)
create table orders (id bigint,name varchar(20),email varchar(20),name2 varchar(20) default 'Sam',email2 varchar(20), primary key (id,name2))
alter table orders add constraint A unique (name2,email2)
alter table orders add constraint B unique (email2)
create table sampletable (id bigint generated by default as identity,id1 integer,id2 integer, primary key (id))
create sequence jdialects_autoid start with 1 increment by 1
create sequence seq_1 start with 1 increment by 1
create table tb1 (pkcol2 varchar(100),valcol bigint )
alter table orders  add constraint fk_orders_name_email foreign key (name,email) references customers
alter table orders  add constraint fk_orders_name2_email2 foreign key (name2,email2) references customers
```   
3) SQL function support:
```
    public static void main(String[] args) {
        String[] params = new String[] { "'a'", "'b'", "'c'", "'d'" };
        System.out.println(Dialect.MySQL55Dialect.fn_concat(params));
        System.out.println(Dialect.SQLServerDialect.fn_concat(params));
        System.out.println(Dialect.Oracle12cDialect.fn_concat(params));
    }
```     
The result is：
```
concat('a', 'b', 'c', 'd')
('a'+'b'+'c'+'d')
'a'||'b'||'c'||'d'
```
Jdialect extracted all function definations from Hibernate, all SQL functions are started with "fn_".
  
### Appendex - All supported database dialects:  
Cache71Dialect, CobolDialect, CUBRIDDialect, DataDirectOracle9Dialect, DB2390Dialect, DB2400Dialect, DB2Dialect, DbfDialect, DerbyDialect, DerbyTenFiveDialect, DerbyTenSevenDialect, DerbyTenSixDialect, ExcelDialect, FirebirdDialect, FrontBaseDialect, H2Dialect, HANAColumnStoreDialect, HANARowStoreDialect, HSQLDialect, Informix10Dialect, InformixDialect, Ingres10Dialect, Ingres9Dialect, IngresDialect, InterbaseDialect, JDataStoreDialect, MariaDB53Dialect, MariaDBDialect, MckoiDialect, MimerSQLDialect, MySQL55Dialect, MySQL57Dialect, MySQL57InnoDBDialect, MySQL5Dialect, MySQL5InnoDBDialect, MySQLDialect, MySQLInnoDBDialect, MySQLMyISAMDialect, Oracle10gDialect, Oracle12cDialect, Oracle8iDialect, Oracle9Dialect, Oracle9iDialect, OracleDialect, ParadoxDialect, PointbaseDialect, PostgresPlusDialect, PostgreSQL81Dialect, PostgreSQL82Dialect, PostgreSQL91Dialect, PostgreSQL92Dialect, PostgreSQL93Dialect, PostgreSQL94Dialect, PostgreSQL95Dialect, PostgreSQL9Dialect, PostgreSQLDialect, ProgressDialect, RDMSOS2200Dialect, SAPDBDialect, SQLiteDialect, SQLServer2005Dialect, SQLServer2008Dialect, SQLServer2012Dialect, SQLServerDialect, Sybase11Dialect, SybaseAnywhereDialect, SybaseASE157Dialect, SybaseASE15Dialect, SybaseDialect, Teradata14Dialect, TeradataDialect, TextDialect, TimesTenDialect, XMLDialect 
 