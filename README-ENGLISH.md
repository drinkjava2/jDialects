## jDialects
License: [LGPL 2.1](http://www.gnu.org/licenses/lgpl-2.1.html)  
  
jDialects is a small java project support ~70 database dialects included SQLLite and Access, usually jDialects is used for build pagination SQL and DDL SQL for cross-databases developing. jDialects runs on Java6+.  
  
jDialects is a core part of jSqlBox project, but it can be used seperately for any Java project which used JDBC. Main parts of jDialects are extracted from Hibernate5.2.9 project by source code generator tool, see project [jDiagen](https://github.com/drinkjava2/jDiagen). And also it build a Excel file "DatabaseDialects.xls", it's useful if you want compare difference of databases.  
  
### How to Use jDialects in project?   
Download and put file "jdialects-1.0.3.jar" in class folder, or add below in pom.xml: 
```
    <dependency>  
        <groupId>com.github.drinkjava2</groupId>  
        <artifactId>jdialects</artifactId>  
        <version>1.0.3</version>  
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
     in Oracle12cDialect, result is: "select * from users  where id=? offset 20 rows fetch next 10 rows only"
     in Sybase11Dialect, get a DialectExcepiton with message: "Sybase11Dialect" does not support physical pagination
     ...
```     
      
2) Build cross-database DDL SQL: 
```   
    public static void main(String[] args) {
        Table t1 = new Table("customers");
        t1.column("id").LONG().identity();
        t1.column("name").STRING(20).unique().pkey();
        t1.column("email").STRING(50).unique().pkey().index("IDX_EMAIL");
        t1.column("address").VARCHAR(50).index().defaultValue("Beijing").comment("address comment");
        t1.column("phoneNumber").VARCHAR(50).index("IDX_phone");
        t1.column("age").INTEGER().notNull().check(">0");

        Table t2 = new Table("orders").comment("order comment");
        t2.engineTail(" DEFAULT CHARSET=utf8");
        t2.column("id").INTEGER().autoID().pkey();
        t2.column("customerID").STRING(20).fkey("customers", "id");
        t2.column("customerName").STRING(20).unique().pkey().tail(" default 'Sam'");
        t2.column("customerEmail").STRING(50).unique().index("IDX_EMAIL");
        t2.fkey("customerName", "customerEmail").ref("customers", "name", "email");

        Table t3 = new Table("sampletable");
        t3.addTableGenerator("table_gen1", "tb1", "pkcol2", "valcol", "pkval", 1, 10);
        t3.column("id1").INTEGER().tableGenerator("table_gen1");
        t3.addSequence("seq1", "seq_1", 1, 1);
        t3.column("id2").INTEGER().sequence("seq1");

        String[] dropAndCreateDDL = Dialect.Oracle12cDialect.toDropAndCreateDDL(t1, t2, t3);
        for (String ddl : dropAndCreateDDL)
            System.out.println(ddl); 
    }
```    
*)LONG、SRING...method: column type definition:  
Common: BOOLEAN,DOUBLE,FLOAT,INTEGER,LONG(=BIGINT),SHORT(=SMALLINT),BIGDECIMAL(=NUMERIC),STRING(=VARCHAR),DATE,TIME,TIMESTAMP,BIGINT,VARCHAR  
Un-common：BINARY,BIT,BLOB,CHAR,CLOB,DECIMAL,LONGNVARCHAR,LONGVARBINARY,LONGVARCHAR,NCHAR,NCLOB,NUMERIC,NVARCHAR,REAL,SMALLINT,TINYINT,VARBINARY  
*)pkey(): Primary key definition.  
*)identity(): Identity column, note some databases do not support identity.  
*)unique:  Unique constraint definition.  
*)index(): Index definition.  
*)notNull(): Not null column definition.  
*)check(): Check constraint definition  
*)defaultValue(): default value definition  
*)column's fkey() method: foreign key definition, format: column.fkey(ftable，fcol1, fcol2...).  
*)table's fkey() method: another way to define a foreign key, format: table.fkey(col1,col2).ref(ftable，fcol1,fcol2..).  
*)comment(): comment definition.  
*)autoID(): Similar like JPA's @Auto type, in program using dialect.getNextAutoID(connection) to get a Long type ID。  
*)tableGenerator(): Simliar like JPA's @TableGenerator type.  
*)dialect's toDropDDL(), toCreateDDL() and toDropAndCreateDDL() methods: build DDL String Array。DDLFormatter.format() can be used format DDL.  
*)toCreateDDL() method will run reserved words checking, if found reserved words like "user"、"order" will throw an Exception.  
*)table.engineTail() method: put extra String behind DDL tail only when database(like MySql) support engine keyword。column.tail() put extra String on column definition.  

Result of above example is:
```
alter table orders  drop constraint  fk_orders_customerName_customerEmail
alter table orders  drop constraint  fk_orders_customerID
drop table tb1 cascade constraints
drop sequence seq_1
drop sequence jdialects_autoid
drop table customers cascade constraints
drop table orders cascade constraints
drop table sampletable cascade constraints
create table customers (id number(19,0) generated as identity,name varchar2(20 char),email varchar2(50 char),address varchar2(50 char) default Beijing,phoneNumber varchar2(50 char),age number(10,0) not null check (>0), primary key (name,email))
alter table customers add constraint unique_customers_name unique (name)
alter table customers add constraint unique_customers_email unique (email)
comment on column customers.address is 'address comment'
create index IDX_customers_address on customers (address)
create index IDX_phone on customers (phoneNumber)
create table orders (id number(10,0),customerID varchar2(20 char),customerName varchar2(20 char) default 'Sam',customerEmail varchar2(50 char), primary key (id,customerName))
alter table orders add constraint unique_orders_customername unique (customerName)
alter table orders add constraint unique_orders_customeremail unique (customerEmail)
comment on table orders is 'order comment'
create table sampletable (id1 number(10,0),id2 number(10,0))
create sequence jdialects_autoid start with 1 increment by 1
create sequence seq_1 start with 1 increment by 1
create table tb1 (pkcol2 varchar2(100 char),valcol number(19,0) )
alter table orders  add constraint fk_orders_customerID foreign key (customerID) references customers
alter table orders  add constraint fk_orders_customerName_customerEmail foreign key (customerName,customerEmail) references customers```   
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
 