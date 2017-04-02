/*
 * AllDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.codegenerator;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;
import static com.github.drinkjava2.jsqlbox.SqlHelper.valuesAndQuestions;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.TypeNames;
import org.hibernate.engine.jdbc.dialect.internal.DialectFactoryImpl;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.alldialects.StrUtils;
import com.github.drinkjava2.jbeanbox.springsrc.ReflectionUtils;
import com.github.drinkjava2.jsqlbox.Dao;

import test.config.PrepareTestContext;

/**
 * This is not a unit test class, it's a code generator tool to create source code in Dialect.java
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings({ "unchecked" })
public class TypeCodeGenerator {
	@Before
	public void setup() {
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	private static Dialect buildDialectByName(Class<?> dialect) {
		BootstrapServiceRegistry bootReg = new BootstrapServiceRegistryBuilder()
				.applyClassLoader(HibernateDialectsList.class.getClassLoader()).build();
		StandardServiceRegistry registry = new StandardServiceRegistryBuilder(bootReg).build();
		DialectFactoryImpl dialectFactory = new DialectFactoryImpl();
		dialectFactory.injectServices((ServiceRegistryImplementor) registry);
		final Map<String, String> configValues = new HashMap<>();
		configValues.put(Environment.DIALECT, dialect.getName());
		return dialectFactory.buildDialect(configValues, null);
	}

	@Test
	public void transferTypeNames() {
		org.apache.log4j.Logger.getLogger("org.hibernate").setLevel(Level.OFF);
		String createSQL = "create table tb_typeNames ("// Save TypeNames into DB
				+ "line integer,"//
				+ "dialect varchar(100),"//
				+ "t_BIGINT varchar(300),"//
				+ "t_BINARY varchar(300),"//
				+ "t_BIT varchar(300),"//
				+ "t_BLOB varchar(300),"//
				+ "t_BOOLEAN varchar(300),"//
				+ "t_CHAR varchar(300),"//
				+ "t_CLOB varchar(300),"//
				+ "t_DATE varchar(300),"//
				+ "t_DECIMAL varchar(300),"//
				+ "t_DOUBLE varchar(300),"//
				+ "t_FLOAT varchar(300),"//
				+ "t_INTEGER varchar(300),"//
				+ "t_JAVA_OBJECT varchar(300),"//
				+ "t_LONGNVARCHAR varchar(300),"//
				+ "t_LONGVARBINARY varchar(300),"//
				+ "t_LONGVARCHAR varchar(300),"//
				+ "t_NCHAR varchar(300),"//
				+ "t_NCLOB varchar(300),"//
				+ "t_NUMERIC varchar(300),"//
				+ "t_NVARCHAR varchar(300),"//
				+ "t_OTHER varchar(300),"//
				+ "t_REAL varchar(300),"//
				+ "t_SMALLINT varchar(300),"//
				+ "t_TIME varchar(300),"//
				+ "t_TIMESTAMP varchar(300),"//
				+ "t_TINYINT varchar(300),"//
				+ "t_VARBINARY varchar(300),"//
				+ "t_VARCHAR varchar(300)"//
				+ ")";
		Dao.executeQuiet("drop table tb_typeNames");
		Dao.execute(createSQL);
		exportDialectTypeNames();
	}

	public void exportDialectTypeNames() {
		int line = 0;
		List<Class<? extends Dialect>> dialects = HibernateDialectsList.SUPPORTED_DIALECTS;
		for (Class<? extends Dialect> class1 : dialects) {
			Dialect dia = buildDialectByName(class1);
			TypeNames t = (TypeNames) findFieldObject(dia, "typeNames");
			String insertSQL = "insert into tb_typeNames ("//
					+ "line," + empty(++line)//
					+ "dialect," + empty(dia.getClass().getSimpleName())//
					+ "t_BIGINT," + empty(getTypeNameDefString(t, (Types.BIGINT)))//
					+ "t_BINARY," + empty(getTypeNameDefString(t, (Types.BINARY)))//
					+ "t_BIT," + empty(getTypeNameDefString(t, (Types.BIT)))//
					+ "t_BLOB," + empty(getTypeNameDefString(t, (Types.BLOB)))//
					+ "t_BOOLEAN," + empty(getTypeNameDefString(t, (Types.BOOLEAN)))//
					+ "t_CHAR," + empty(getTypeNameDefString(t, (Types.CHAR)))//
					+ "t_CLOB," + empty(getTypeNameDefString(t, (Types.CLOB)))//
					+ "t_DATE," + empty(getTypeNameDefString(t, (Types.DATE)))//
					+ "t_DECIMAL," + empty(getTypeNameDefString(t, (Types.DECIMAL)))//
					+ "t_DOUBLE," + empty(getTypeNameDefString(t, (Types.DOUBLE)))//
					+ "t_FLOAT," + empty(getTypeNameDefString(t, (Types.FLOAT)))//
					+ "t_INTEGER," + empty(getTypeNameDefString(t, (Types.INTEGER)))//
					+ "t_JAVA_OBJECT," + empty(getTypeNameDefString(t, (Types.JAVA_OBJECT)))//
					+ "t_LONGNVARCHAR," + empty(getTypeNameDefString(t, (Types.LONGNVARCHAR)))//
					+ "t_LONGVARBINARY," + empty(getTypeNameDefString(t, (Types.LONGVARBINARY)))//
					+ "t_LONGVARCHAR," + empty(getTypeNameDefString(t, (Types.LONGVARCHAR)))//
					+ "t_NCHAR," + empty(getTypeNameDefString(t, (Types.NCHAR)))//
					+ "t_NCLOB," + empty(getTypeNameDefString(t, (Types.NCLOB)))//
					+ "t_NUMERIC," + empty(getTypeNameDefString(t, (Types.NUMERIC)))//
					+ "t_NVARCHAR," + empty(getTypeNameDefString(t, (Types.NVARCHAR)))//
					+ "t_OTHER," + empty(getTypeNameDefString(t, (Types.OTHER)))//
					+ "t_REAL," + empty(getTypeNameDefString(t, (Types.REAL)))//
					+ "t_SMALLINT," + empty(getTypeNameDefString(t, (Types.SMALLINT)))//
					+ "t_TIME," + empty(getTypeNameDefString(t, (Types.TIME)))//
					+ "t_TIMESTAMP," + empty(getTypeNameDefString(t, (Types.TIMESTAMP)))//
					+ "t_TINYINT," + empty(getTypeNameDefString(t, (Types.TINYINT)))//
					+ "t_VARBINARY," + empty(getTypeNameDefString(t, (Types.VARBINARY)))//
					+ "t_VARCHAR" + empty(getTypeNameDefString(t, (Types.VARCHAR)))//
					+ ")" //
					+ valuesAndQuestions();
			Dao.executeInsert(insertSQL);
		}

		// ============now start generate source code=======
		StringBuilder sb = new StringBuilder();
		sb.append("private void initializeTypeMappings() {// NOSONAR").append("\r\n");
		sb.append("switch (this.toString()) { // NOSONAR").append("\r\n");

		List<Map<String, Object>> lst = Dao.queryForList("select * from tb_typeNames");
		for (Map<String, Object> map : lst) {
			String dialect = (String) map.get("dialect");
			sb.append("case \"").append(dialect).append("\": {//NOSONAR\r\n");

			for (Entry<String, Object> entry : map.entrySet()) {
				String key = entry.getKey();
				key = StrUtils.replace(key, "T_", "$");
				key = StrUtils.replace(key, "t_", "$");
				String value = "" + entry.getValue();
				if (!"LINE".equals(key) && !"line".equals(key) && !"DIALECT".equals(key) && !"dialect".equals(key)) {
					sb.append("typeMappings.put(\"" + key + "\", \"" + value + "\");//NOSONAR\r\n");
				}
			}
			if (StrUtils.containsIgnoreCase(dialect, "innoDB"))
				sb.append("typeMappings.put(\"$ENGINE\", \"engine=innoDB\");//NOSONAR\r\n");
			if (StrUtils.containsIgnoreCase(dialect, "MyISAM"))
				sb.append("typeMappings.put(\"$ENGINE\", \"engine=MyISAM\");//NOSONAR\r\n");

			sb.append("}\r\n");
			sb.append("break;\r\n");

		}
		sb.append("default:\r\n");
		sb.append("}\r\n");
		sb.append("}\r\n");
		System.out.println(sb.toString());
	}

	private static String getTypeNameDefString(TypeNames t, int typeCode) {
		String s = "N/A";
		try {
			s = t.get(typeCode);
		} catch (Exception e) {
		}
		Map<Integer, Map<Long, String>> weighted = (Map<Integer, Map<Long, String>>) findFieldObject(t, "weighted");
		Map<Long, String> map = weighted.get(typeCode);
		if (map != null && map.size() > 0) {
			for (Map.Entry<Long, String> entry : map.entrySet()) {
				s += "|" + entry.getKey() + "<" + entry.getValue();
			}
		}
		return s;
	}

	private static Object findFieldObject(Object obj, String fieldname) {
		try {
			Field field = ReflectionUtils.findField(obj.getClass(), fieldname);
			field.setAccessible(true);
			Object o = field.get(obj);
			return o;
		} catch (Exception e) {
			return null;
		}
	}

}