package test.hql;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.github.drinkjava2.jsqlbox.Dao;

import test.config.PrepareTestContext;
import test.config.po.Customer;

public class HqlPaginationUsage {

	public static void createTables() {
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		String innoDB = Dao.getDefaultDatabaseType().isMySql() ? "ENGINE=InnoDB DEFAULT CHARSET=utf8;" : "";
		Dao.executeQuiet("drop table customertable");
		Dao.execute(Customer.CREATE_SQL + innoDB);
	}

	public static void main(String[] args) {
		createTables();

		try {
			SessionFactory sf = new Configuration().configure().buildSessionFactory();
			Session session = sf.openSession();
			Transaction tx = session.beginTransaction();

			for (int i = 0; i < 10; i++) {
				Customer customer = new Customer();
				customer.setCustomerName("Tom");
				session.save(customer);
				customer.setCustomerName("Sam");
			}

			tx.commit();
			session.close();
			sf.close();
		} catch (HibernateException e) {
			e.printStackTrace();
		}
	}
}