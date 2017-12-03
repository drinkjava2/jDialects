## jDialects
License: [LGPL 2.1](http://www.gnu.org/licenses/lgpl-2.1.html)  
  
jDialects is a small java project support ~70 database dialects included SQLite and Access, usually jDialects is used for build pagination SQL and DDL SQL for cross-databases developing. jDialects runs on Java6 or above.  
  
jDialects is a core part of jSqlBox project, but it can be used seperately for any Java project which used JDBC. Main parts of jDialects are extracted from Hibernate5.2.9 project by source code generator tool, see project [jDiagen](https://github.com/drinkjava2/jDiagen). And also it build a Excel file "DatabaseDialects.xls", it's useful if you want compare difference of databases.  
  
### How to Use jDialects in project?   
Download and put file "jdialects-1.0.6.jar" in class folder, or add below in pom.xml: 
```
	<dependency>  
		<groupId>com.github.drinkjava2</groupId>  
		<artifactId>jdialects</artifactId>  
		<version>1.0.6</version>
	</dependency>
```
That's all needed.  
  
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
	@Test
	public void sampleTest() {// An example used to put on README.md
		TableModel t1 = new TableModel("customers");
		t1.column("name").STRING(20).pkey();
		t1.column("email").STRING(20).pkey().entityField("email").updatable(true).insertable(false);
		t1.column("address").VARCHAR(50).defaultValue("'Beijing'").comment("address comment");
		t1.column("phoneNumber").VARCHAR(50).singleIndex("IDX2");
		t1.column("age").INTEGER().notNull().check("'>0'");
		t1.index("idx3").columns("address", "phoneNumber").unique();

		TableModel t2 = new TableModel("orders").comment("order comment");
		t2.column("id").LONG().autoId().pkey();
		t2.column("name").STRING(20);
		t2.column("email").STRING(20);
		t2.column("name2").STRING(20).pkey().tail(" default 'Sam'");
		t2.column("email2").STRING(20);
		t2.fkey().columns("name2", "email2").refs("customers", "name", "email");
		t2.fkey("fk1").columns("name", "email").refs("customers", "name", "email");
		t2.unique("uk1").columns("name2", "email2");

		TableModel t3 = new TableModel("sampletable");
		t3.column("id").LONG().identityId().pkey();
		t3.tableGenerator("table_gen1", "tb1", "pkcol2", "valcol", "pkval", 1, 10);
		t3.column("id1").INTEGER().idGenerator("table_gen1");
		t3.sequenceGenerator("seq1", "seq_1", 1, 1);
		t3.column("id2").INTEGER().idGenerator("seq1");
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
*)identityId(): Identity column, note some databases do not support identity.  
*)unique:  Unique constraint definition.  
*)index(): Index definition.  
*)notNull(): Not null column definition.  
*)check(): Check constraint definition.  
*)defaultValue(): default value definition.  
*)fkey() method: foreign key definition, format: column.fkey(ftable，fcol1, fcol2...).  
*)SingleXxx() methods: shortcut method to build foreign key, index, unique constraints but only works for 1 column   
*)comment(): comment definition.  
*)autoID(): Similar like JPA's @Auto type, in program using dialect.getNextAutoID(connection) to get a Long type ID.  
*)tableGenerator() and sequenceGenerator(): Simliar like JPA's @TableGenerator and @SequenceGenerator type.  
*)dialect's toDropDDL(), toCreateDDL() and toDropAndCreateDDL() methods: build DDL String Array。DDLFormatter.format() can be used format DDL.  
*)toCreateDDL() method will run reserved words checking, if found reserved words like "user"、"order" will throw an Exception.  
*)table.engineTail() method: put extra String behind DDL tail only when database(like MySql) support engine。column.tail() add extra String on column definition.  
*)FKeyConst and ColumnsModel has tail() method to add extra String piece at end of DDL definition.  
*)entityField() method: mark a column be mapped to a Java POJO's field, this is designed for ORM tool

Result of above example is:  
```
alter table orders  drop constraint  fk1
alter table orders  drop constraint  fk_orders_name2_email2
drop table tb1 if exists
drop sequence if exists seq_1
drop sequence if exists jdia_seq_autoid
drop table customers if exists
drop table orders if exists
drop table sampletable if exists
create table customers ( name varchar(20),email varchar(20),address varchar(50) default 'Beijing',phoneNumber varchar(50),age integer not null check ('>0'), primary key (name,email))
create  index IDX2 on customers (phoneNumber)
create unique index idx3 on customers (address,phoneNumber)
create table orders ( id bigint,name varchar(20),email varchar(20),name2 varchar(20) default 'Sam',email2 varchar(20), primary key (id,name2))
alter table orders add constraint uk1 unique (name2,email2)
create table sampletable ( id bigint generated by default as identity,id1 integer,id2 integer, primary key (id))
create sequence jdia_seq_autoid start with 1 increment by 1
create sequence seq_1 start with 1 increment by 1
create table tb1 (pkcol2 varchar(100),valcol bigint )
alter table orders  add constraint fk_orders_name2_email2 foreign key (name2,email2) references customers (name,email)
alter table orders  add constraint fk1 foreign key (name,email) references customers (name,email)
```   

3). Annotation support  
jDialect support below JPA Annotations:  
   @Entity, @Transient, @UniqueConstraint, @GenerationType, @Id, @Index, @SequenceGenerator, @GeneratedValue, @Table, @Column, @TableGenerator  
And jDialect added below Annotations:  
   @FKey        For foreign key defination 
   SingleIndex  For Index defination but only works for 1 column   
   SingleUnique For Unique defination but only works for 1 column   
   SingleFKey   For foreign key defination but only works for 1 column   
Below is a demo show how to transfer a annotated POJO into DDL:  
```
public class AnnotationTest extends TestBase { 

	@Entity
	@Table(name = "testpo", //
			uniqueConstraints = { @UniqueConstraint(columnNames = { "field1" }),
					@UniqueConstraint(name = "unique_cons2", columnNames = { "field1", "field2" }) }, //
			indexes = { @Index(columnList = "field1,field2", unique = true),
					@Index(name = "index_cons2", columnList = "field1,field2", unique = false) }//
	)
	@SequenceGenerator(name = "seqID1", sequenceName = "seqName1", initialValue = 1, allocationSize = 10)
	@TableGenerator(name = "tableID1", table = "table1", pkColumnName = "pkCol1", valueColumnName = "vcol1", pkColumnValue = "pkcolval1", initialValue = 2, allocationSize = 20)
	@FKey(name = "fkey1", ddl=true, columns = { "field1", "field2" }, refs = { "Entity1", "field1", "field2" })
	@FKey1(columns = { "field2", "field3" }, refs = { "Entity1", "field1", "field2" })
	public static class Entity2 {
		@SequenceGenerator(name = "seqID2", sequenceName = "seqName2", initialValue = 2, allocationSize = 20) 
		@TableGenerator(name = "tableID2", table = "table2", pkColumnName = "pkCol1", valueColumnName = "vcol1", pkColumnValue = "pkcolval1", initialValue = 2, allocationSize = 20)
		@Id
		@Column(columnDefinition = TypeUtils.VARCHAR, length = 20)
		public String field1;

		@Column(name = "field2", nullable = false, columnDefinition = TypeUtils.BIGINT)
		public String field2;

		@GeneratedValue(strategy = GenerationType.TABLE, generator = "CUST_GEN")
		@Column(name = "field3", nullable = false, columnDefinition = TypeUtils.BIGINT)
		@SingleFKey(name = "singleFkey1", ddl=true, refs = { "Entity1", "field1" })
		@SingleIndex
		@SingleUnique
		public Integer field3;

		@Transient
		public Integer field4;

		@UUID36
		public String field5;

		public static void config(TableModel tableModel) {
			tableModel.getColumn("field7").setColumnName("changedfield7");
			tableModel.column("newField9").STRING(10);
		} 
		//getter & Setter
	}

	@Test
	public void ddlOutTest() {
		String[] dropAndCreateDDL = Dialect.H2Dialect.toDropAndCreateDDL(ModelUtils.entity2Model(Entity1.class, Entity2.class));
		for (String ddl : dropAndCreateDDL)
			System.out.println(ddl); 
	}
}
``` 

4) SQL function support:
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
Jdialect extracted all function definations from Hibernate, all SQL functions are started with "fn_", more detail see "DatabaseDialects.xls"  
  
### Appendex - All supported database dialects:  
Cache71Dialect, CobolDialect, CUBRIDDialect, DataDirectOracle9Dialect, DB2390Dialect, DB2400Dialect, DB2Dialect, DbfDialect, DerbyDialect, DerbyTenFiveDialect, DerbyTenSevenDialect, DerbyTenSixDialect, ExcelDialect, FirebirdDialect, FrontBaseDialect, H2Dialect, HANAColumnStoreDialect, HANARowStoreDialect, HSQLDialect, Informix10Dialect, InformixDialect, Ingres10Dialect, Ingres9Dialect, IngresDialect, InterbaseDialect, JDataStoreDialect, MariaDB53Dialect, MariaDBDialect, MckoiDialect, MimerSQLDialect, MySQL55Dialect, MySQL57Dialect, MySQL57InnoDBDialect, MySQL5Dialect, MySQL5InnoDBDialect, MySQLDialect, MySQLInnoDBDialect, MySQLMyISAMDialect, Oracle10gDialect, Oracle12cDialect, Oracle8iDialect, Oracle9Dialect, Oracle9iDialect, OracleDialect, ParadoxDialect, PointbaseDialect, PostgresPlusDialect, PostgreSQL81Dialect, PostgreSQL82Dialect, PostgreSQL91Dialect, PostgreSQL92Dialect, PostgreSQL93Dialect, PostgreSQL94Dialect, PostgreSQL95Dialect, PostgreSQL9Dialect, PostgreSQLDialect, ProgressDialect, RDMSOS2200Dialect, SAPDBDialect, SQLiteDialect, SQLServer2005Dialect, SQLServer2008Dialect, SQLServer2012Dialect, SQLServerDialect, Sybase11Dialect, SybaseAnywhereDialect, SybaseASE157Dialect, SybaseASE15Dialect, SybaseDialect, Teradata14Dialect, TeradataDialect, TextDialect, TimesTenDialect, XMLDialect 
 