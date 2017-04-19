(English version see "README-ENGLISH.MD")  
## jDialects
开源协议: [LGPL 2.1](http://www.gnu.org/licenses/lgpl-2.1.html)  

jDialect是一个收集了市面几乎所有数据库方言的Java小项目，通常可用来创建分页SQL和建表DDL语句，可根据不同的数据库方言生成不同的SQL。目前jDialects支持75种数据库方言，包括Hibernate中没有的SQLLite和Access等。jDialects需要JDK1.7或以上版本支持。

jDialects起始是为了jSqlBox项目而开发的，但它本身是一个独立的小工具(发布包只有约60k大小且无其它第三方依赖)，只要使用到了原生SQL，就可以利用这个工具来创建支持多种数据库的分页SQL和DDL。如果你使用了纯JDBC、JdbcTemplate、DbUtils...等，只要出现原生SQL，并有跨数据库需求的场合(例如单元测试需要同时在H2内存数据库和实际数据库MySql上运行)，都可以利用这个工具来实现跨数据库开发。 

本版本jDialects的主文件"Dialects.java"主体部分是由代码生成工具从Hibernate5.2.9版本中自动抽取而生成，代码生成工具可见jDiaGen项目(https://github.com/drinkjava2/jDiagen)。
  
### 如何引入项目?  
下载 "jdialects-1.0.0.jar"并放入项目库目录，或在项目的pom.xml文件中加入：
```
	<dependency>  
		<groupId>com.github.drinkjava2</groupId>  
		<artifactId>jdialects</artifactId>  
		<version>1.0.0</version>  
	</dependency>
```	
### 在程序中使用   
1) 创建跨数据库的分页：  
```
       Dialect d=guessDialect(dataSource);  //根据数据源判断方言类型,  
       //Dialect d=guessDialect(connection);  //或根据连接来判断方言类型  
       //Dialect d=Dialect.MySQL5Dialect;     //或手工指定数据库方言类型 
       String result=d.paginate(3, 10, "select * from users where id=?");  //创建分页SQL  
     
   如方言为MySQL5Dialect,    结果为: "select * from users where id=? limit 20, 10"  
   如方言为Oracle8iDialect,  结果为: "select * from ( select row_.*, rownum rownum_ from ( select * from users where id=? ) row_ ) where rownum_ <= 30 and rownum_ > 20"  
   如方言为Oracle12cDialect, 结果为: "select * from users where id=? offset 20 rows fetch next 10 rows only"  
   如方言为Sybase11Dialect, 抛出DialectExcepiton异常并提示: "Sybase11Dialect" does not support physical pagination  
   ...
```   
    
     
2) 创建跨数据库的DDL:  
```
		Dialect d=guessDialect(dataSource);  
		ddlSql = "create table test("//  
		"f1 " + d.BIGINT() //  
		",f3 " + d.BIT() //  
		",f4 " + d.BLOB() //  
		",f5 " + d.BOOLEAN() //  
		",f6 " + d.CHAR() //  
		")" + d.ENGINE();  
   如方言为MySql5Dialect, ddlSql值为： "create table test(f1 bigint,f3 bit,f4 longblob,f5 bit,f6 char(1))engine=innoDB"  
   如方言为SQLServer2012Dialect, ddlSql值为： "create table test(f1 int8,f3 bool,f4 oid,f5 boolean,f6 char(1))"   
   BIGINT()及BIT()等为java.sql.Types中定义类型的同名方法。
```   
3) 关于SQL函数  
jDialects暂不支持跨数据库的SQL函数，主要是因为SQL函数在不同的数据库里往往有两种情况，一种是命名和参数完全相同，一种是变化非常大，很多是专有函数，无论前者还是后者，都不适合用通用的SQL函数来代表。但是jDialects代码生成工具将75种方言的函数对比写在“DatabaseDialects.xls”这个文件中(位于项目的根目录)，如果需要作数据库移植时可以随时作为速查手册。

以上即为jDialects全部文档，如有不清楚处可以查看"Dialect.java"源码或单元测试示例。最后强调一下，jDialects只是个SQL文本变换工具，根据不同的方言对SQL进行不同的转换，它本身不是一个持久化工具。

附录：以下为目前jDialects支持的75种数据库方言：
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
