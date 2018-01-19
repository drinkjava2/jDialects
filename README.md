<p align="center">
  <a href="https://github.com/drinkjava2/jDialects">
   <img alt="jdialects-logo" src="jdialects-logo.png">
  </a>
</p>

<p align="center">
  一个通用的数据库方言工具
</p>

<p align="center">
  <a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.drinkjava2%22%20AND%20a%3A%22jdialects%22">
    <img alt="maven" src="https://img.shields.io/maven-central/v/com.github.drinkjava2/jdialects.svg?style=flat-square">
  </a>

  <a href="https://www.gnu.org/licenses/lgpl-3.0">
    <img alt="code style" src="https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=flat-square">
  </a>
</p>

# 简介 | Intro

jDialects 支持多达75种数据库方言的DDL创建、分页、函数变换、主键生成。

# 优点 | Advantages

- **无侵入**：jDialects只对SQL文本进行变换，不会对您现有的持久层工具产生任何影响。
- **依赖少**：仅依赖jdialects-x.x.x.jar(约260k)。
- **从Annotation创建DDL**：提供对主要JPA注解的支持，且无需添加JPA库依赖。
- **从Java方法创建DDL**：提供Java链式方法配置以创建DDL，Java方法的配置可以在运行期修改。
- **主键生成器**：支持十种主键生成器，并可方便地自定义。
- **分页**：提供分页方法。
- **函数解析**：对不同的数据库解析成对应方言的函数，尽量做到写一次SQL到处运行。
- **JPA解析**：提供对主要JPA注解的解析。

# 文档 | Documentation

[用户手册](../../wiki)
[JavaDoc](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jdialects%22)(选javadoc.jar下载后解压)

# 应用示例 | Demo

[在纯JDBC中使用](../../tree/master/jdialects-demo/demo-pure-jdbc)

[在jSqlBox中使用](../../tree/master/jdialects-demo/demo-jsqlbox)

[在MyBatis中使用](../../tree/master/jdialects-demo/demo-mybatis)

# 下载地址 | Download

[点此去下载](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jdialects%22)

```xml
<dependency>
    <groupId>com.github.drinkjava2</groupId>
    <artifactId>jdialects</artifactId>
    <version>官方最新版本为准</version>
</dependency>
```

# 其他开源项目 | Other Project

- [基于DbUtils和jDialects的持久层工具 jSqlBox](https://gitee.com/drinkjava2/jSqlBox)
- [基于Java初始化块配置的IOC/AOP工具 jBeanBox](https://gitee.com/drinkjava2/jBeanBox)
- [一个独立的声明式事务工具 jTransactions](https://gitee.com/drinkjava2/jTransactions)

# 期望 | Futures

欢迎发issue提出更好的意见，帮助完善 jDialects

# 版权 | License

[LGPL 3.0](https://www.gnu.org/licenses/lgpl-3.0)

# 捐赠 | Donate

提交意见就是对这个项目最好的捐赠。

# 关注我 | About Me
[Github](https://github.com/drinkjava2)  
[码云](https://gitee.com/drinkjava2)  