<p align="center">
  <a href="https://github.com/drinkjava2/jDialects">
   <img alt="jdialects-logo" src="jdialects-logo.png">
  </a>
</p>

<p align="center">
  一个通用数据库方言工具
</p>

# 简介 | Intro

jDialects 支持多达75种数据库方言的DDL创建、分页、函数变换、主键生成。

# 优点 | Advantages

- **无侵入**：jDialects只对SQL文本进行变换，不会对您现有的持久层工具产生任何影响。
- **依赖少**：仅依赖单个文件约260k。
- **从Annotation创建DDL**：提供对主要JPA注解的支持，且无需添加JPA库依赖。
- **从Java方法创建DDL**：提供Java方法配置来创建DDL，同样的语法也可以在运行期修改配置。
- **主键生成器**：支持十种主键生成器，并可方便地自定义。
- **分页**：提供跨数据库的物理分页方法。
- **函数解析**：对不同的数据库解析成对应方言的函数，尽量做到一次SQL到处运行。
- **保留字检查**：提供数据库保留字检查功能。

# 文档 | Documentation

[中文](../../wiki)  |  [English](../../wiki)

[JavaDoc](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jdialects%22)

# 应用示例 | Demo

[在纯JDBC中使用](../../wiki/在纯JDBC项目中使用)

[在jSqlBox中使用](../../wiki/在jSqlBox项目中使用)

[在MyBatis中使用](../../wiki/在MyBatis中使用)

# 下载地址 | Download

[点此去下载](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jdialects%22)

```xml
<dependency>
    <groupId>com.github.drinkjava2</groupId>
    <artifactId>jdialects</artifactId>
    <version>官方最新版本为准</version>
</dependency>
```

# 相关开源项目 | Other Projects

- [基于DbUtils和jDialects的持久层工具 jSqlBox](https://gitee.com/drinkjava2/jSqlBox)
- [一个独立的声明式事务工具 jTransactions](https://gitee.com/drinkjava2/jTransactions)
- [基于Java初始化块配置的IOC/AOP工具 jBeanBox](https://gitee.com/drinkjava2/jBeanBox)

# 期望 | Futures

欢迎发issue提出更好的意见或提交PR，帮助完善 jDialects

# 版权 | License

[LGPL 2.1](https://www.gnu.org/licenses/lgpl-2.1)

# 关注我 | About Me
[Github](https://github.com/drinkjava2)  
[码云](https://gitee.com/drinkjava2)  
