<p align="center">
  <a href="https://github.com/drinkjava2/jDialects">
   <img alt="jdialects-logo" src="jdialects-logo.png">
  </a>
</p>

<p align="center">
  A Database Dialect Tool
</p>

# Introduction

jDialects supports DDL creation, paging, function transformation, type conversion and major JPA annotation parsing functions for up to 80 database dialects.  
It is usually used in combination with JDBC tools and as part of other Dao tools such as jSqlBox.  
jDialects requires JDK1.6+  

# Advantages 
- **No intrusion**: jDialects works based on transforming SQL text without any impact on your existing persistence layer tools.
- ** Less dependency**: Only rely on a single jar file, about 280k in size.
- **Create DDL from Annotation**: Support build DDL from major JPA annotations
- **Create DDL from Java methods**: Provide Java method configuration to create DDL, the same Java methods can also used at runtime.
- **Primary Key Generator**: Provides ten primary key generators and supports custom primary key generators. A distributed primary key generator is also provided.
- **Paging**: Provides a cross-database pagination method.
- **Function transformation**: Parse different databases' functions corresponding dialects.  
- ** type conversion**: Provides mutual transformation with Java types for different database data types.
- **Reserved Word Check**: Provides database reserved word checking.

# Documentation

[Chinese](https://gitee.com/drinkjava2/jdialects/wikis/pages)  |  [English](https://gitee.com/drinkjava2/jdialects/wiki)

[JavaDoc](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jdialects%22)

# Demo

[For Pure JDBC](https://github.com/drinkjava2/jDialects/tree/master/demo/demo-pure-jdbc)

[For jSqlBox](https://github.com/drinkjava2/jDialects/tree/master/demo/demo-jsqlbox)

[For MyBatis](https://github.com/drinkjava2/jDialects/tree/master/demo/demo-mybatis)

# Download

[Download here](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jdialects%22)  
Or put below in pom.xml:  
```xml
<dependency>
    <groupId>com.github.drinkjava2</groupId>
    <artifactId>jdialects</artifactId>
    <version>5.0.6.jre8</version>  <!-- Or newest version -->
</dependency>
```

# Other Author's Projects

- [jSqlBox](https://gitee.com/drinkjava2/jSqlBox)
- [jTransactions](https://gitee.com/drinkjava2/jTransactions)
- [jBeanBox](https://gitee.com/drinkjava2/jBeanBox)

# Futures

Welcome post issue to help improve jDialects.

# License

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

# About Me
[Github](https://github.com/drinkjava2)  
[Gitee](https://gitee.com/drinkjava2)  
