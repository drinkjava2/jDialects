package test.hql;

import java.util.List;

import org.apache.log4j.Level;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.CUBRIDDialect;
import org.hibernate.dialect.MySQL55Dialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.query.Query;

import com.github.drinkjava2.jsqlbox.Dao;

import test.config.PrepareTestContext;
import test.config.po.Customer;

@SuppressWarnings({ "unused", "deprecation", "rawtypes" })
public class HqlPaginationUsage {

	private static void createTablesByjSqlBox() {
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		String innoDB = Dao.getDefaultDatabaseType().isMySql() ? "ENGINE=InnoDB DEFAULT CHARSET=utf8;" : "";
		Dao.executeQuiet("drop table customertable");
		Dao.execute(Customer.CREATE_SQL + innoDB);
	}

	private static void openHibernateLog(SessionFactory sf) {
		JdbcServices serv = sf.getSessionFactory().getJdbcServices();
		SqlStatementLogger log = serv.getSqlStatementLogger();
		org.apache.log4j.Logger.getLogger("org.hibernate").setLevel(Level.TRACE);
		log.setLogToStdout(true);

	}

	private static void insertDataByHibernate() {
		try {

			Configuration c = new Configuration().configure();
			SessionFactory sf = c.buildSessionFactory();

			Session session = sf.openSession();
			Transaction tx = session.beginTransaction();
			for (int i = 0; i < 10; i++) {
				Customer customer = new Customer();
				customer.setCustomerName("Tom");
				session.save(customer);
			}
			tx.commit();
			session.close();
			sf.close();
		} catch (HibernateException e) {
			e.printStackTrace();
		}
	}

	private static Configuration buildConfig(String dialect) {
		Configuration c = new Configuration();
		c.setProperty("hibernate.dialect", dialect);
		c.setProperty("hibernate.connection.url",
				"jdbc:mysql://localhost:3306/test?autoReconnect=true&amp;useSSL=false");
		c.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
		c.setProperty("hibernate.connection.username", "root");
		c.setProperty("hibernate.connection.password", "root888");
		c.addResource("Customer.hbm.xml");
		return c;
	}

	private static void nativeQuery() {
		Configuration c = buildConfig(MySQL55Dialect.class.getName());
		SessionFactory sf = c.buildSessionFactory();

		Session session = sf.openSession();
		Query query = session
				.createNativeQuery("select a.* from customertable a, customertable b where a.id=b.id order by a.id");
		query.setFirstResult(7);
		query.setMaxResults(3);
		openHibernateLog(sf);
		List l = query.list();
		for (Object object : l) {
			Object[] objs = (Object[]) object;
			for (Object object2 : objs) {
				System.out.print(object2 + " , ");
			}
			System.out.println();
		}
		session.close();
		sf.close();
	}

	public static void main(String[] args) {
		// createTablesByjSqlBox();
		// insertDataByHibernate();
		nativeQuery();

	}

}