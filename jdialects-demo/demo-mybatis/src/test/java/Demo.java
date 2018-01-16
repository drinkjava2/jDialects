import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.zaxxer.hikari.HikariDataSource;

public class Demo {

	@Table(name = "users")
	public static class User {
		@Id
		private String firstName;

		@Id
		private String lastName;

		private Integer age;

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

	@SuppressWarnings("unchecked")
	public static <T> T queryForObject(Connection conn, String sql, Object... params) {
		ResultSet rs = null;
		PreparedStatement pst = null;
		try {
			int i = 1;
			pst = conn.prepareStatement(sql);// NOSONAR
			for (Object obj : params)
				pst.setObject(i++, obj);
			rs = pst.executeQuery();
			if (rs.next())
				return (T) rs.getObject(1);
			else
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (pst != null)
				try {
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	public static List<Map<String, Object>> queryForMapList(Connection conn, String sql, Object... params) {
		ResultSet rs = null;
		PreparedStatement pst = null;
		List<Map<String, Object>> list = new ArrayList<>();
		try {
			int i = 1;
			pst = conn.prepareStatement(sql);// NOSONAR
			for (Object obj : params)
				pst.setObject(i++, obj);
			rs = pst.executeQuery();
			ResultSetMetaData md = rs.getMetaData();
			int columnCount = md.getColumnCount();
			while (rs.next()) {
				LinkedHashMap<String, Object> rowData = new LinkedHashMap<String, Object>();
				for (int j = 1; j <= columnCount; j++)
					rowData.put(md.getColumnName(j).toUpperCase(), rs.getObject(j));
				list.add(rowData);
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (pst != null)
				try {
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	public int execute(Connection conn, String sql, Object... params) throws SQLException {
		PreparedStatement pst = null;
		try {
			int i = 1;
			pst = conn.prepareStatement(sql);// NOSONAR
			for (Object obj : params)
				pst.setObject(i++, obj);
			pst.execute();
			return 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		} finally {
			if (pst != null)
				try {
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	@Test
	public void doTest() {
		HikariDataSource ds = new HikariDataSource();// DataSource

		// H2 is a memory database
		ds.setDriverClassName("org.h2.Driver");
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setUsername("sa");
		ds.setPassword("");

		// MySQL
		// ds.setDriverClassName("com.mysql.jdbc.Driver");
		// ds.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/test?rewriteBatchedStatements=true&useSSL=false");
		// ds.setUsername("root");
		// ds.setPassword("root888");

		// MS-SqlServer
		// ds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		// ds.setJdbcUrl("jdbc:sqlserver://localhost:1433;databaseName=test");
		// ds.setUsername("sa");
		// ds.setPassword("root888");

		// ORACLE
		// ds.setDriverClassName("oracle.jdbc.OracleDriver");
		// ds.setJdbcUrl("jdbc:oracle:thin:@127.0.0.1:1521:XE");
		// ds.setUsername("root");
		// ds.setPassword("root888");

		Dialect dialect = Dialect.guessDialect(ds);
		Dialect.allowLogOutput = true;

		Connection conn = null;
		try {
			conn = ds.getConnection();
			String[] ddlArray = dialect.toDropAndCreateDDL(User.class);
			for (String ddl : ddlArray)
				try {
					execute(conn, ddl);
				} catch (Exception e) {
				}

			for (int i = 1; i <= 100; i++)
				execute(conn, "insert into users (firstName, lastName, age) values(?,?,?)", "Foo" + i, "Bar" + i, i);

			Assert.assertEquals(100L, ((Number) queryForObject(conn, "select count(*) from users")).longValue());

			List<Map<String, Object>> users = queryForMapList(conn, dialect.paginAndTrans(2, 10,
					"select concat(firstName, ' ', lastName) as UserName, age from users where age>?"), 50);

			Assert.assertEquals(10, users.size());

			for (Map<String, Object> map : users)
				System.out.println("UserName=" + map.get("USERNAME") + ", age=" + map.get("AGE"));

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		ds.close();
	}
}
