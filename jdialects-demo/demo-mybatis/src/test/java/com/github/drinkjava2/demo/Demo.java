package com.github.drinkjava2.demo;

import java.lang.reflect.Field;
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
import com.github.drinkjava2.jdialects.springsrc.utils.ReflectionUtils;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

public class Demo {
	protected static ThreadLocal<Integer> pageNo = new ThreadLocal<Integer>();
	protected static ThreadLocal<Integer> pageSize = new ThreadLocal<Integer>();

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
		@Select("SELECT count(*) FROM users")
		Integer countUsers();

		@Select("select concat(firstName, ' ', lastName) as USERNAME, age as AGE from users where age>#{age}")
		List<Map<String, Object>> getOlderThan(int age);
	}

	public static Object getFieldValue(Object obj, String fieldname) {
		try {
			Field field = ReflectionUtils.findField(obj.getClass(), fieldname);
			field.setAccessible(true);
			Object o = field.get(obj);
			return o;
		} catch (Exception e) {
			return null;
		}
	}

	public static void setFieldValue(Object obj, String fieldname, Object value) {
		try {
			Field field = ReflectionUtils.findField(obj.getClass(), fieldname);
			field.setAccessible(true);
			field.set(obj, value);
		} catch (Exception e) {
		}
	}

	// copied from
	// https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Interceptor.md
	@Intercepts({
			@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
					RowBounds.class, ResultHandler.class }),
			@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
					RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class }), })
	public class JDialectsPlugin implements Interceptor {
		Dialect dialect;

		JDialectsPlugin(Dialect dialect) {
			this.dialect = dialect;
		}

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
			// 由于逻辑关系，只会进入一次
			if (args.length == 4) {
				boundSql = ms.getBoundSql(parameter);
				cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
			} else { // 6 个参数时
				cacheKey = (CacheKey) args[4];
				boundSql = (BoundSql) args[5];
			}
			if (pageNo.get() != null) {
				Configuration configuration = ms.getConfiguration();
				String pageSql = dialect.paginAndTrans(pageNo.get(), pageSize.get(), boundSql.getSql()); 

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
		SqlBoxContext.setDefaultContext(ctx);
		ctx.setAllowShowSQL(true);
		Dialect dialect = ctx.getDialect();
		String[] ddlArray = dialect.toDropAndCreateDDL(User.class);
		for (String ddl : ddlArray)
			ctx.quiteExecute(ddl);
		for (int i = 1; i <= 100; i++) {
			User u = new User();
			u.setFirstName("Foo" + i);
			u.setLastName("Bar" + i);
			u.setAge(i);
			u.insert();
		}
		 

		TransactionFactory transactionFactory = new JdbcTransactionFactory();
		Environment environment = new Environment("demo", transactionFactory, dataSource);
		Configuration configuration = new Configuration(environment);
		configuration.addMapper(UserMapper.class);
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
		configuration.addInterceptor(new JDialectsPlugin(dialect));

		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			Connection conn=session.getConnection();
			Assert.assertEquals(100, ctx.nQueryForLongValue(conn, "select count(*) from users"));
			
			Assert.assertEquals((Object) 100, session.getMapper(UserMapper.class).countUsers());
			List<Map<String, Object>> users;
			try {
				pageNo.set(5);
				pageSize.set(10);
				users = session.getMapper(UserMapper.class).getOlderThan(50);
			} finally {
				pageNo.remove();
				pageSize.remove();
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
