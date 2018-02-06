package com.github.drinkjava2.demo;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

public class Demo {
	public static ThreadLocal<Object[]> paginInfo = new ThreadLocal<Object[]>();

	@Table(name = "users")
	public static class User extends ActiveRecord {
		@UUID25
		@Id
		private String id;

		private String firstName;

		private String lastName;

		private Integer age;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}
	}

	public static interface UserMapper {
		@Select("select concat(firstName, ' ', lastName) as USERNAME, age as AGE from users where age>#{age}")
		List<Map<String, Object>> getOlderThan(int age);
	}

	// JDialectsPlugin is a MyBatis plug, referenced this article:
	// https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Interceptor.md
	@Intercepts({
			@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
					RowBounds.class, ResultHandler.class }),
			@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
					RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class }), })
	public class JDialectsPlugin implements Interceptor {

		@Override
		public Object intercept(Invocation invocation) throws Throwable {
			Object[] args = invocation.getArgs();
			MappedStatement ms = (MappedStatement) args[0];
			Object parameter = args[1];
			RowBounds rowBounds = (RowBounds) args[2];
			@SuppressWarnings("rawtypes")
			ResultHandler resultHandler = (ResultHandler) args[3];
			Executor executor = (Executor) invocation.getTarget();
			CacheKey cacheKey;
			BoundSql boundSql;
			if (args.length == 4) {
				boundSql = ms.getBoundSql(parameter);
				cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
			} else {
				cacheKey = (CacheKey) args[4];
				boundSql = (BoundSql) args[5];
			}
			if (paginInfo.get() != null) {// if paginInfo exist in threadlocal
				Configuration configuration = ms.getConfiguration();
				String pageSql = ((Dialect) paginInfo.get()[0]).paginAndTrans((int) paginInfo.get()[1],
						(int) paginInfo.get()[2], boundSql.getSql());
				BoundSql pageBoundSql = new BoundSql(configuration, pageSql, boundSql.getParameterMappings(),
						parameter);
				return executor.query(ms, parameter, RowBounds.DEFAULT, resultHandler, cacheKey, pageBoundSql);
			} else
				return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
		}

		@Override
		public Object plugin(Object target) {
			return Plugin.wrap(target, this);
		}

		@Override
		public void setProperties(Properties properties) {
		}

	}

	@Test
	public void doTest() {
		HikariDataSource dataSource = new HikariDataSource();// DataSource

		// H2 is a memory database
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		dataSource.setUsername("sa");
		dataSource.setPassword("");

		// MySQL
		// dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		// dataSource.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/test?rewriteBatchedStatements=true&useSSL=false");
		// dataSource.setUsername("root");
		// dataSource.setPassword("root888");

		// MS-SqlServer
		// dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		// dataSource.setJdbcUrl("jdbc:sqlserver://localhost:1433;databaseName=test");
		// dataSource.setUsername("sa");
		// dataSource.setPassword("root888");

		// ORACLE
		// dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
		// dataSource.setJdbcUrl("jdbc:oracle:thin:@127.0.0.1:1521:XE");
		// dataSource.setUsername("root");
		// dataSource.setPassword("root888");

		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);
		SqlBoxContext.setGlobalAllowShowSql(true);
		String[] ddlArray = ctx.toDropAndCreateDDL(User.class);
		for (String ddl : ddlArray)
			ctx.quiteExecute(ddl);
		for (int i = 1; i <= 100; i++)
			new User().put("firstName", "Foo" + i, "lastName", "Bar" + i, "age", i).insert();

		TransactionFactory transactionFactory = new JdbcTransactionFactory();
		Environment environment = new Environment("demo", transactionFactory, dataSource);
		Configuration configuration = new Configuration(environment);
		configuration.addMapper(UserMapper.class);
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
		configuration.addInterceptor(new JDialectsPlugin());

		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			Connection conn = session.getConnection();
			Assert.assertEquals(100, ctx.nQueryForLongValue(conn, "select count(*) from users"));

			List<Map<String, Object>> users;
			try {
				paginInfo.set(new Object[] { ctx.getDialect(), 3, 10 });
				users = session.getMapper(UserMapper.class).getOlderThan(50);
			} finally {
				paginInfo.remove();
			}
			Assert.assertEquals(10, users.size());
			for (Map<String, Object> map : users)
				System.out.println("UserName=" + map.get("USERNAME") + ", age=" + map.get("AGE"));
		} finally {
			session.close();
		}
		dataSource.close();
	}
}
