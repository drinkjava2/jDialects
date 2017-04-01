/*
 * HiDialect, a tiny SQL dialect tool 
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

import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.TypeNames;
import org.hibernate.engine.jdbc.dialect.internal.DialectFactoryImpl;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.springsrc.ReflectionUtils;
import com.github.drinkjava2.jsqlbox.Dao;

import test.TestBase;

/**
 * This is not a unit test class, it's a code generator tool to create source
 * code in Dialect.java
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings({ "unchecked" })
public class TypeCodeGenerator extends TestBase {

	@Test
	public void doBuild() {
		transferTypeNames();// Save TypeNames & HibernateTypeNames into DB
	}

	private static Dialect buildDialectByName(Class<?> dialect) {
		BootstrapServiceRegistry bootReg = new BootstrapServiceRegistryBuilder()
				.applyClassLoader(CodeGeneratorHelper.class.getClassLoader()).build();
		StandardServiceRegistry registry = new StandardServiceRegistryBuilder(bootReg).build();
		DialectFactoryImpl dialectFactory = new DialectFactoryImpl();
		dialectFactory.injectServices((ServiceRegistryImplementor) registry);
		final Map<String, String> configValues = new HashMap<>();
		configValues.put(Environment.DIALECT, dialect.getName());
		return dialectFactory.buildDialect(configValues, null);
	}

	public void transferTypeNames() {
		String createSQL = "create table tb_typeNames ("//
				+ "line integer,"//
				+ "dialect varchar(100),"//
				+ "Types_BIT varchar(300),"//
				+ "Types_TINYINT varchar(300),"//
				+ "Types_SMALLINT varchar(300),"//
				+ "Types_INTEGER varchar(300),"//
				+ "Types_BIGINT varchar(300),"//
				+ "Types_FLOAT varchar(300),"//
				+ "Types_REAL varchar(300),"//
				+ "Types_DOUBLE varchar(300),"//
				+ "Types_NUMERIC varchar(300),"//
				+ "Types_DECIMAL varchar(300),"//
				+ "Types_CHAR varchar(300),"//
				+ "Types_VARCHAR varchar(300),"//
				+ "Types_LONGVARCHAR varchar(300),"//
				+ "Types_DATE varchar(300),"//
				+ "Types_TIME varchar(300),"//
				+ "Types_TIMESTAMP varchar(300),"//
				+ "Types_BINARY varchar(300),"//
				+ "Types_VARBINARY varchar(300),"//
				+ "Types_LONGVARBINARY varchar(300),"//
				+ "Types_NULL varchar(300),"//
				+ "Types_OTHER varchar(300),"//
				+ "Types_JAVA_OBJECT varchar(300),"//
				+ "Types_DISTINCT varchar(300),"//
				+ "Types_STRUCT varchar(300),"//
				+ "Types_ARRAY varchar(300),"//
				+ "Types_BLOB varchar(300),"//
				+ "Types_CLOB varchar(300),"//
				+ "Types_REF varchar(300),"//
				+ "Types_DATALINK varchar(300),"//
				+ "Types_BOOLEAN varchar(300),"//
				+ "Types_ROWID varchar(300),"//
				+ "Types_NCHAR varchar(300),"//
				+ "Types_NVARCHAR varchar(300),"//
				+ "Types_LONGNVARCHAR varchar(300),"//
				+ "Types_NCLOB varchar(300),"//
				+ "Types_SQLXML varchar(300),"//
				+ "Types_REF_CURSOR varchar(300),"//
				+ "Types_TIME_WITH_TIMEZONE varchar(300),"//
				+ "Types_TIMESTAMP_WITH_TIMEZONE varchar(300) "//
				+ ")";
		Dao.executeQuiet("drop table tb_typeNames");
		Dao.execute(createSQL);
		exportDialectTypeNames();
	}

	public void exportDialectTypeNames() {
		int line = 0;
		System.out.println("exportDialectTypeNames========================");
		List<Class<? extends Dialect>> dialects = CodeGeneratorHelper.SUPPORTED_DIALECTS;
		for (Class<? extends Dialect> class1 : dialects) {
			Dialect dia = buildDialectByName(class1);
			TypeNames t = (TypeNames) findFieldObject(dia, "typeNames");
			String insertSQL = "insert into tb_typeNames ("//
					+ "line," + empty(++line)//
					+ "dialect," + empty(dia.getClass().getSimpleName())//
					+ "Types_BIT," + empty(getTypeNameDefString(t, (Types.BIT)))//
					+ "Types_TINYINT," + empty(getTypeNameDefString(t, (Types.TINYINT)))//
					+ "Types_SMALLINT," + empty(getTypeNameDefString(t, (Types.SMALLINT)))//
					+ "Types_INTEGER," + empty(getTypeNameDefString(t, (Types.INTEGER)))//
					+ "Types_BIGINT," + empty(getTypeNameDefString(t, (Types.BIGINT)))//
					+ "Types_FLOAT," + empty(getTypeNameDefString(t, (Types.FLOAT)))//
					+ "Types_REAL," + empty(getTypeNameDefString(t, (Types.REAL)))//
					+ "Types_DOUBLE," + empty(getTypeNameDefString(t, (Types.DOUBLE)))//
					+ "Types_NUMERIC," + empty(getTypeNameDefString(t, (Types.NUMERIC)))//
					+ "Types_DECIMAL," + empty(getTypeNameDefString(t, (Types.DECIMAL)))//
					+ "Types_CHAR," + empty(getTypeNameDefString(t, (Types.CHAR)))//
					+ "Types_VARCHAR," + empty(getTypeNameDefString(t, (Types.VARCHAR)))//
					+ "Types_LONGVARCHAR," + empty(getTypeNameDefString(t, (Types.LONGVARCHAR)))//
					+ "Types_DATE," + empty(getTypeNameDefString(t, (Types.DATE)))//
					+ "Types_TIME," + empty(getTypeNameDefString(t, (Types.TIME)))//
					+ "Types_TIMESTAMP," + empty(getTypeNameDefString(t, (Types.TIMESTAMP)))//
					+ "Types_BINARY," + empty(getTypeNameDefString(t, (Types.BINARY)))//
					+ "Types_VARBINARY," + empty(getTypeNameDefString(t, (Types.VARBINARY)))//
					+ "Types_LONGVARBINARY," + empty(getTypeNameDefString(t, (Types.LONGVARBINARY)))//
					+ "Types_NULL," + empty(getTypeNameDefString(t, (Types.NULL)))//
					+ "Types_OTHER," + empty(getTypeNameDefString(t, (Types.OTHER)))//
					+ "Types_JAVA_OBJECT," + empty(getTypeNameDefString(t, (Types.JAVA_OBJECT)))//
					+ "Types_DISTINCT," + empty(getTypeNameDefString(t, (Types.DISTINCT)))//
					+ "Types_STRUCT," + empty(getTypeNameDefString(t, (Types.STRUCT)))//
					+ "Types_ARRAY," + empty(getTypeNameDefString(t, (Types.ARRAY)))//
					+ "Types_BLOB," + empty(getTypeNameDefString(t, (Types.BLOB)))//
					+ "Types_CLOB," + empty(getTypeNameDefString(t, (Types.CLOB)))//
					+ "Types_REF," + empty(getTypeNameDefString(t, (Types.REF)))//
					+ "Types_DATALINK," + empty(getTypeNameDefString(t, (Types.DATALINK)))//
					+ "Types_BOOLEAN," + empty(getTypeNameDefString(t, (Types.BOOLEAN)))//
					+ "Types_ROWID," + empty(getTypeNameDefString(t, (Types.ROWID)))//
					+ "Types_NCHAR," + empty(getTypeNameDefString(t, (Types.NCHAR)))//
					+ "Types_NVARCHAR," + empty(getTypeNameDefString(t, (Types.NVARCHAR)))//
					+ "Types_LONGNVARCHAR," + empty(getTypeNameDefString(t, (Types.LONGNVARCHAR)))//
					+ "Types_NCLOB," + empty(getTypeNameDefString(t, (Types.NCLOB)))//
					+ "Types_SQLXML," + empty(getTypeNameDefString(t, (Types.SQLXML)))//
					+ "Types_REF_CURSOR," + empty(getTypeNameDefString(t, (Types.REF_CURSOR)))//
					+ "Types_TIME_WITH_TIMEZONE," + empty(getTypeNameDefString(t, (Types.TIME_WITH_TIMEZONE)))//
					+ "Types_TIMESTAMP_WITH_TIMEZONE" + empty(getTypeNameDefString(t, (Types.TIMESTAMP_WITH_TIMEZONE)))//
					+ ")" //
					+ valuesAndQuestions();
			Dao.executeInsert(insertSQL);

			t = (TypeNames) findFieldObject(dia, "hibernateTypeNames");// Hibernate
																		// Type
			insertSQL = "insert into tb_typeNames ("//
					+ "line," + empty(++line)//
					+ "dialect," + empty("Hib_" + dia.getClass().getSimpleName())//
					+ "Types_BIT," + empty(getTypeNameDefString(t, (Types.BIT)))//
					+ "Types_TINYINT," + empty(getTypeNameDefString(t, (Types.TINYINT)))//
					+ "Types_SMALLINT," + empty(getTypeNameDefString(t, (Types.SMALLINT)))//
					+ "Types_INTEGER," + empty(getTypeNameDefString(t, (Types.INTEGER)))//
					+ "Types_BIGINT," + empty(getTypeNameDefString(t, (Types.BIGINT)))//
					+ "Types_FLOAT," + empty(getTypeNameDefString(t, (Types.FLOAT)))//
					+ "Types_REAL," + empty(getTypeNameDefString(t, (Types.REAL)))//
					+ "Types_DOUBLE," + empty(getTypeNameDefString(t, (Types.DOUBLE)))//
					+ "Types_NUMERIC," + empty(getTypeNameDefString(t, (Types.NUMERIC)))//
					+ "Types_DECIMAL," + empty(getTypeNameDefString(t, (Types.DECIMAL)))//
					+ "Types_CHAR," + empty(getTypeNameDefString(t, (Types.CHAR)))//
					+ "Types_VARCHAR," + empty(getTypeNameDefString(t, (Types.VARCHAR)))//
					+ "Types_LONGVARCHAR," + empty(getTypeNameDefString(t, (Types.LONGVARCHAR)))//
					+ "Types_DATE," + empty(getTypeNameDefString(t, (Types.DATE)))//
					+ "Types_TIME," + empty(getTypeNameDefString(t, (Types.TIME)))//
					+ "Types_TIMESTAMP," + empty(getTypeNameDefString(t, (Types.TIMESTAMP)))//
					+ "Types_BINARY," + empty(getTypeNameDefString(t, (Types.BINARY)))//
					+ "Types_VARBINARY," + empty(getTypeNameDefString(t, (Types.VARBINARY)))//
					+ "Types_LONGVARBINARY," + empty(getTypeNameDefString(t, (Types.LONGVARBINARY)))//
					+ "Types_NULL," + empty(getTypeNameDefString(t, (Types.NULL)))//
					+ "Types_OTHER," + empty(getTypeNameDefString(t, (Types.OTHER)))//
					+ "Types_JAVA_OBJECT," + empty(getTypeNameDefString(t, (Types.JAVA_OBJECT)))//
					+ "Types_DISTINCT," + empty(getTypeNameDefString(t, (Types.DISTINCT)))//
					+ "Types_STRUCT," + empty(getTypeNameDefString(t, (Types.STRUCT)))//
					+ "Types_ARRAY," + empty(getTypeNameDefString(t, (Types.ARRAY)))//
					+ "Types_BLOB," + empty(getTypeNameDefString(t, (Types.BLOB)))//
					+ "Types_CLOB," + empty(getTypeNameDefString(t, (Types.CLOB)))//
					+ "Types_REF," + empty(getTypeNameDefString(t, (Types.REF)))//
					+ "Types_DATALINK," + empty(getTypeNameDefString(t, (Types.DATALINK)))//
					+ "Types_BOOLEAN," + empty(getTypeNameDefString(t, (Types.BOOLEAN)))//
					+ "Types_ROWID," + empty(getTypeNameDefString(t, (Types.ROWID)))//
					+ "Types_NCHAR," + empty(getTypeNameDefString(t, (Types.NCHAR)))//
					+ "Types_NVARCHAR," + empty(getTypeNameDefString(t, (Types.NVARCHAR)))//
					+ "Types_LONGNVARCHAR," + empty(getTypeNameDefString(t, (Types.LONGNVARCHAR)))//
					+ "Types_NCLOB," + empty(getTypeNameDefString(t, (Types.NCLOB)))//
					+ "Types_SQLXML," + empty(getTypeNameDefString(t, (Types.SQLXML)))//
					+ "Types_REF_CURSOR," + empty(getTypeNameDefString(t, (Types.REF_CURSOR)))//
					+ "Types_TIME_WITH_TIMEZONE," + empty(getTypeNameDefString(t, (Types.TIME_WITH_TIMEZONE)))//
					+ "Types_TIMESTAMP_WITH_TIMEZONE" + empty(getTypeNameDefString(t, (Types.TIMESTAMP_WITH_TIMEZONE)))//
					+ ")" //
					+ valuesAndQuestions();
			Dao.executeInsert(insertSQL);

			Dao.executeInsert("insert into tb_typeNames (line)" + empty(++line) + valuesAndQuestions());
		}
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
				s += "|" + entry.getKey() + "," + entry.getValue();
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