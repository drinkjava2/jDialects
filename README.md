(English instruction please see [README-ENGLISH.md](README-ENGLISH.md) )  
## jDialects
开源协议: [LGPL 2.1](http://www.gnu.org/licenses/lgpl-2.1.html) 

jDialects是一个从Hibernate中提取的支持70多种数据库(并加入了SQLite、Access等)方言的小项目，主要功能有:  
1.创建跨数据库的分页SQL，根据当前方言生成当前数据库的分页SQL。  
2.创建跨数据库的建表和删表DDL语句，根据当前数据库方言生成相应的DDL语句。  
3.支持主要的JPA注解的解析，将JPA标注的POJO转化为Java内存模型，从而可以作进一步处理，如生成DDL或提供给ORM程序使用。  
4.创建跨数据库的SQL函数，根据当前数据库方言生成对应的SQL函数片段。  

jDialects起初是为了jSqlBox项目而开发的，但它本身是一个独立的项目，没有任何第三方库依赖。只要用到了原生SQL，就可以利用它来实现跨数据库开发，适用于使用了纯JDBC、JdbcTemplate、DbUtils等以原生SQL为基础的持久层工具，并有跨数据库需求的场合(例如单元测试需要同时在H2内存数据库和实际数据库Oracle上运行)。对于一些ORM项目来说，也可以考虑引入jDialects来避免重复开发自已的数据库方言实现。jDialects项目的主体部分是由代码生成工具从Hibernate5.2.9版中抽取自动生成，这从一定程度上也保证了它的代码质量。代码生成工具详见[jDiagen](https://github.com/drinkjava2/jDiagen)项目。jDialects运行于Java6或以上版本。  
  
### 如何引入项目?  
下载"jdialects-1.0.5.jar"并放入项目库目录，或在项目的pom.xml文件中加入：
```
	<dependency>  
		<groupId>com.github.drinkjava2</groupId>  
		<artifactId>jdialects</artifactId>  
		<version>1.0.5</version>  (待Release)
	</dependency>
```	 
### 在程序中使用   
一. 生成跨数据库的分页SQL：  
```
       Dialect d=guessDialect(dataSource);  //根据数据源判断方言类型,  
       //Dialect d=guessDialect(connection);  //或根据连接来判断方言类型  
       //Dialect d=Dialect.MySQL5Dialect;     //或手工指定数据库方言类型 
       String result=d.paginate(3, 10, "select * from users where age>?");  //创建分页SQL 
     
   当方言为MySQL5Dialect, 结果为: "select * from users where age>? limit 20, 10"  
   当方言为Oracle8iDialect, 结果为: "select * from ( select row_.*, rownum rownum_ from ( select * from users where age>? ) row_ ) where rownum_ <= 30 and rownum_ > 20"  
   当方言为Oracle12cDialect, 结果为: "select * from users where age>? offset 20 rows fetch next 10 rows only"  
   当方言为Sybase11Dialect, 抛出DialectExcepiton异常并提示: "Sybase11Dialect" does not support physical pagination  
   ...
```   
     
二. 创建跨数据库的DDL:  
```
	@Test
	public void sampleTest() {// An example used to put on README.md
		TableModel t1 = new TableModel("customers");
		t1.column("name").STRING(20).pkey();
		t1.column("email").STRING(20).pkey();
		t1.column("address").VARCHAR(50).defaultValue("'Beijing'").comment("address comment");
		t1.column("phoneNumber").VARCHAR(50).singleIndex("IDX2");
		t1.column("age").INTEGER().notNull().check("'>0'");
		t1.index("idx3").columns("address", "phoneNumber").unique();

		TableModel t2 = new TableModel("orders").comment("order comment");
		t2.column("id").LONG().autoID().pkey();
		t2.column("name").STRING(20);
		t2.column("email").STRING(20);
		t2.column("name2").STRING(20).pkey().tail(" default 'Sam'");
		t2.column("email2").STRING(20);
		t2.fkey().columns("name2", "email2").refs("customers", "name", "email");
		t2.fkey("fk1").columns("name", "email").refs("customers", "name", "email");
		t2.unique("uk1").columns("name2", "email2");

		TableModel t3 = new TableModel("sampletable");
		t3.column("id").LONG().identity().pkey();
		t3.addTableGenerator("table_gen1", "tb1", "pkcol2", "valcol", "pkval", 1, 10);
		t3.column("id1").INTEGER().tableGenerator("table_gen1");
		t3.addSequence("seq1", "seq_1", 1, 1);
		t3.column("id2").INTEGER().sequence("seq1");
		t3.engineTail(" DEFAULT CHARSET=utf8");
 

		String[] dropAndCreateDDL = Dialect.H2Dialect.toDropAndCreateDDL(t1, t2, t3);
		for (String ddl : dropAndCreateDDL)
			System.out.println(ddl);

		testOnCurrentRealDatabase(t1, t2, t3);
	}
```	
以上代码演示用Java来生成跨数据库的Drop和Create DDL语句，介绍如下：  
*)LONG()、STRING()...等大写方法: 定义列的类型，运行时将翻译成对应当前数据库方言的实际SQL类型，一共有如下类型：  
常用: BOOLEAN,DOUBLE,FLOAT,INTEGER,LONG(=BIGINT),SHORT(=SMALLINT),BIGDECIMAL(=NUMERIC),STRING(=VARCHAR),DATE,TIME,TIMESTAMP,BIGINT,VARCHAR  
不常用：BINARY,BIT,BLOB,CHAR,CLOB,DECIMAL,LONGNVARCHAR,LONGVARBINARY,LONGVARCHAR,NCHAR,NCLOB,NUMERIC,NVARCHAR,REAL,SMALLINT,TINYINT,VARBINARY  
*)pkey()方法表示列为主键，如一个table对象有多个pkey()方法，将成为复合主键。  
*)identity()方法定义自增类型，sequence()方法定义序列类型，注意这两种类型不是所有数据库都支持，例如MySql不支持Sequence，如定义了Sequence并运行在MySQL方言上将会抛出导常。  
*)unique()方法定义唯一约束，约束名称可以省略
*)index()方法定义索引，索引名称可以省略  
*)notNull()方法指定列内容不能为空。  
*)check()方法添加一个check约束，注意有些数据库不支持check，如果数据库不支持则输出警告并忽略此设定。  
*)defaultValue()方法指定缺省值。  
*)fkey()方法定义外键，格式为 table.fkey(键名).columns(本表列1,本表列2...).refs(参考表，参考表列名1,参考表列名2...)
*)singleIndex()、singleUnique、singleFKey()三个方法为快捷方法，分别对当前单列生成索引、唯一约束、外键。
*)column对象和Table对象的comment方法分别用于对列和表添加注解，如果数据库不支持则输出警告并忽略此设定。  
*)autoID()方法定义一个自动自增主键类型，与JPA的Auto类型相似。jDialect利用创建一个Sequence或一个表格(如方言不支持sequence)来模拟实现，在程序中可用dialect.getNextAutoID(connection)方法来获取生成的唯一自增ID, 类型为Long类型。  
*)tableGenerator()方法对应于JPA的@TableGenerator定义，主要用于ORM工具使用，以方便重利用JPA注解定义的实体，一般用户不必关心。  
*)dialect.toDropDDL()方法、toCreateDDL()方法、toDropAndCreateDDL()方法分别对应于生成当前方言的删表、建表、先删后建表DDL语句，参数为单个table实列或table实例数组，返回值是一个DDL字符串数组。一个小技巧：运行删表DDL时通常要屏蔽异常，因为第一遍运行时表格还没建立，运行Drop语句可能会报错。静态方法DDLFormatter.format()可用于格式化DDL输出。  
*)toCreateDDL()方法运行时将进行保留字检查，如果定义了当前数据库方言的保留字作为列名或表名，如"user"、"order"等，将抛出异常，如果定义了非当前数据库的保留字，则仅输出警告提示。  
*)table.engineTail()方法仅当方言支持engine关键字时追加一个额外字串，通常用于MySQL字符集设定。column.tail()方法用于在列定义后强制追加一个额外字串。 

上例功能演示示例的输出结果如下： 
```   
alter table orders  drop constraint  fk1
alter table orders  drop constraint  fk_orders_name2_email2
drop table tb1 if exists
drop sequence if exists seq_1
drop sequence if exists jdialects_autoid
drop table customers if exists
drop table orders if exists
drop table sampletable if exists
create table customers ( name varchar(20),email varchar(20),address varchar(50) default 'Beijing',phoneNumber varchar(50),age integer not null check ('>0'), primary key (name,email))
create  index IDX2 on customers (phoneNumber)
create unique index idx3 on customers (address,phoneNumber)
create table orders ( id bigint,name varchar(20),email varchar(20),name2 varchar(20) default 'Sam',email2 varchar(20), primary key (id,name2))
alter table orders add constraint uk1 unique (name2,email2)
create table sampletable ( id bigint generated by default as identity,id1 integer,id2 integer, primary key (id))
create sequence jdialects_autoid start with 1 increment by 1
create sequence seq_1 start with 1 increment by 1
create table tb1 (pkcol2 varchar(100),valcol bigint )
alter table orders  add constraint fk_orders_name2_email2 foreign key (name2,email2) references customers (name,email)
alter table orders  add constraint fk1 foreign key (name,email) references customers (name,email)
```
    
```
jdialect抽取了Hibernate所有方言的函数定义，所有函数均以"fn_"开头,运行时将翻译成实际的SQL函数片段，所有数据库方言都支持的函数用大写字符来区分，如fn_ABS()即为所有数据库都有对应的取绝对值函数，如函数名为小写，如fn_ltrim(),则表示不是每个数据库都有对应的函数。如果调用了当前方言不支持的函数将在运行期抛出异常。 
具体各个方言支持的函数列表可见“DatabaseDialects.xls”文件，这个文件中还包含了各种方言的分页和类型定义对比，如果需要作数据库移植时也可以作为速查手册。 

三. 注解支持  
jDialect实现了对主要的JPA注解支持，可以用来将JPA注解标注的POJO转化为jDialects的Java内存模型，从而可以输出为DDL或由ORM程序做进一步处理。  
目前jDialects支持以下JPA注解:  
   @Entity, @Transient, @UniqueConstraint, @GenerationType, @Id, @Index, @SequenceGenerator, @GeneratedValue, @Table, @Column, @TableGenerator  
相比与JPA注解，jDialect添加了一个独有注解，用于更方便DDL生成：  
   @FKey        用于定义外键  
   SingleIndex  用于定义只针对单例有效的索引  
   SingleUnique 用于定义只针对单例有效的唯一约束  
   SingleFKey   用于定义只针对单例有效的外键约束  
例如下例为将一个JPA标注的POJO类转为DDL输出：
```
public class AnnotationTest extends BaseDDLTest {
	public static class POJO1 {
		public String field1;
		public String field2;
		//getter & setters
	}

	@Entity
	@Table(name = "testpo", //
			uniqueConstraints = { @UniqueConstraint(columnNames = { "field1" }),
					@UniqueConstraint(name = "unique_cons2", columnNames = { "field1", "field2" }) }, //
			indexes = { @Index(columnList = "field1,field2", unique = true),
					@Index(name = "index_cons2", columnList = "field1,field2", unique = false) }//
	)
	@SequenceGenerator(name = "seqID1", sequenceName = "seqName1", initialValue = 1, allocationSize = 10)
	@SequenceGenerator1(name = "seqID2", sequenceName = "seqName2", initialValue = 2, allocationSize = 20)
	@TableGenerator(name = "tableID1", table = "table1", pkColumnName = "pkCol1", valueColumnName = "vcol1", pkColumnValue = "pkcolval1", initialValue = 2, allocationSize = 20)
	@TableGenerator2(name = "tableID2", table = "table2", pkColumnName = "pkCol1", valueColumnName = "vcol1", pkColumnValue = "pkcolval1", initialValue = 2, allocationSize = 20)
	@FKey(name = "fk1", columns = { "field1", "field2" }, refs = { "POJO1", "field1", "field2" })
	@FKey1(columns = { "field2", "field3" }, refs = { "POJO1", "field1", "field2" })
	public static class POJO2 {
		@Id
		@Column(columnDefinition = ColumnDef.VARCHAR, length = 20)
		public String field1;

		@Column(name = "field2", nullable = false, columnDefinition = ColumnDef.BIGINT)
		public String field2;

		@GeneratedValue(strategy = GenerationType.TABLE, generator = "CUST_GEN")
		@Column(name = "field3", nullable = false, columnDefinition = ColumnDef.BIGINT)
		@SingleFKey(name = "singleFkey1", refs = { "POJO1", "field1" })
		@SingleIndex
		@SingleUnique
		public Integer field3;

		@Transient
		public Integer field4;

		//getter & setters
	}

	@Test
	public void ddlOutTest() {
		String[] dropAndCreateDDL = Dialect.H2Dialect.toCreateDDL(ConvertUtils.pojo2Model(POJO1.class, POJO2.class));
		for (String ddl : dropAndCreateDDL)
			System.out.println(ddl);
	}
}
``` 
   

四. 生成跨数据库的SQL函数片段:  
```
    public static void main(String[] args) {
        String[] params = new String[] { "'a'", "'b'", "'c'", "'d'" };
        System.out.println(Dialect.MySQL55Dialect.fn_concat(params));
        System.out.println(Dialect.SQLServerDialect.fn_concat(params));
        System.out.println(Dialect.Oracle12cDialect.fn_concat(params));
    }
```     
上例运行结果如下：
```
concat('a', 'b', 'c', 'd')
('a'+'b'+'c'+'d')
'a'||'b'||'c'||'d'

   
五. 总结  
以上即为jDialects全部文档，如有不清楚处可以查看项目及单元测试源码。最后强调一下，jDialects只是个文本变换工具，根据不同的方言对SQL进行不同的变换，它本身不是一个完整的持久化工具，必须配合其它持久化工具如JDBC/DbUtils等使用。 

附录1 版本变动记录  
1.0.1 第一版发布，支持Java7  
1.0.2 完善DDL功能  
1.0.3 更正一些Bug，压缩了一下尽寸; 并降级到Java6版本，以支持更多开发环境。  
1.0.4 修正DDL中Unique约束不支持多个列的Bug。  
1.0.5 添加JPA等Annotation支持，index、unique、fkey等方法作了一些较大改动，采用singleXxx()方法代替。  

附录2 目前jDialects支持的75种数据库方言    
Cache71Dialect, CobolDialect, CUBRIDDialect, DataDirectOracle9Dialect, DB2390Dialect, DB2400Dialect, DB2Dialect, DbfDialect, DerbyDialect, DerbyTenFiveDialect, DerbyTenSevenDialect, DerbyTenSixDialect, ExcelDialect, FirebirdDialect, FrontBaseDialect, H2Dialect, HANAColumnStoreDialect, HANARowStoreDialect, HSQLDialect, Informix10Dialect, InformixDialect, Ingres10Dialect, Ingres9Dialect, IngresDialect, InterbaseDialect, JDataStoreDialect, MariaDB53Dialect, MariaDBDialect, MckoiDialect, MimerSQLDialect, MySQL55Dialect, MySQL57Dialect, MySQL57InnoDBDialect, MySQL5Dialect, MySQL5InnoDBDialect, MySQLDialect, MySQLInnoDBDialect, MySQLMyISAMDialect, Oracle10gDialect, Oracle12cDialect, Oracle8iDialect, Oracle9Dialect, Oracle9iDialect, OracleDialect, ParadoxDialect, PointbaseDialect, PostgresPlusDialect, PostgreSQL81Dialect, PostgreSQL82Dialect, PostgreSQL91Dialect, PostgreSQL92Dialect, PostgreSQL93Dialect, PostgreSQL94Dialect, PostgreSQL95Dialect, PostgreSQL9Dialect, PostgreSQLDialect, ProgressDialect, RDMSOS2200Dialect, SAPDBDialect, SQLiteDialect, SQLServer2005Dialect, SQLServer2008Dialect, SQLServer2012Dialect, SQLServerDialect, Sybase11Dialect, SybaseAnywhereDialect, SybaseASE157Dialect, SybaseASE15Dialect, SybaseDialect, Teradata14Dialect, TeradataDialect, TextDialect, TimesTenDialect, XMLDialect 