/*
 * AllDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test.codegenerator;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.AnsiTrimEmulationFunction;
import org.hibernate.dialect.function.AnsiTrimFunction;
import org.hibernate.dialect.function.CastFunction;
import org.hibernate.dialect.function.CharIndexFunction;
import org.hibernate.dialect.function.ConvertFunction;
import org.hibernate.dialect.function.DerbyConcatFunction;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.NvlFunction;
import org.hibernate.dialect.function.PositionSubstringFunction;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StaticPrecisionFspTimestampFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
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
public class FunctionCodeGenerator extends TestBase {

	@Test
	public void doBuild() {
		transferFunctions();// Save registered functions into DB
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

	@Test
	public void transferFunctions() {
		String createSQL = "create table tb_functions ("//
				+ "fn_name varchar(200)  " + ", constraint const_fn_name primary key (fn_name))";
		Dao.executeQuiet("drop table tb_functions");
		Dao.execute(createSQL);
		exportDialectFunctions();
		// Map<String, SQLFunction> sqlFunctions
	}

	public void exportDialectFunctions() {
		System.out.println("exportDialectFunctions========================");
		List<Class<? extends Dialect>> dialects = HibernateDialectsList.SUPPORTED_DIALECTS;
		for (Class<? extends Dialect> class1 : dialects) {
			Dialect dia = buildDialectByName(class1);
			String diaName = dia.getClass().getSimpleName();
			Dao.execute("alter table tb_functions add  " + diaName + " varchar(200)");
			Dao.executeQuiet("insert into tb_functions (" + diaName + ", fn_name) values(?,?)", empty(diaName),
					empty("FUNCTIONS"));
			Map<String, SQLFunction> sqlFunctions = (Map<String, SQLFunction>) findFieldObject(dia, "sqlFunctions");

			for (Entry<String, SQLFunction> entry : sqlFunctions.entrySet()) {
				String fn_name = entry.getKey();
				Dao.executeQuiet("insert into tb_functions (" + diaName + ", fn_name) values(?,?)", empty("---"),
						empty(fn_name));

				SQLFunction fun = entry.getValue();
				@SuppressWarnings("rawtypes")
				Class funClass = fun.getClass();
				String sqlName = fun.toString();

				if (VarArgsSQLFunction.class.equals(funClass)) {
					sqlName = "" + findFieldObject(fun, "begin") + findFieldObject(fun, "sep")
							+ findFieldObject(fun, "end");
				} else if (NoArgSQLFunction.class.equals(funClass)) {
					sqlName = "" + findFieldObject(fun, "name");
					if ((Boolean) findFieldObject(fun, "hasParenthesesIfNoArguments"))
						sqlName += "()";
				} else if (ConvertFunction.class.equals(funClass)) {
					sqlName = "*convert";
				} else if (CastFunction.class.equals(funClass)) {
					sqlName = "*cast";
				} else if (NvlFunction.class.equals(funClass)) {
					sqlName = "*nul";
				} else if (AnsiTrimFunction.class.equals(funClass)) {
					sqlName = "*trim";
				} else if (DerbyConcatFunction.class.equals(funClass)) {
					sqlName = "*||";
				} else if (StaticPrecisionFspTimestampFunction.class.equals(funClass)) {
					sqlName = "*||";
				} else if (PositionSubstringFunction.class.equals(funClass)) {
					sqlName = "*position/substring";
				} else if (AnsiTrimEmulationFunction.class.equals(funClass)) {
					sqlName = "*TrimEmulation";
				} else if (CharIndexFunction.class.equals(funClass)) {
					sqlName = "*charindex";
				}
				Dao.execute("update tb_functions set " + diaName + "=? where fn_name=?", empty(sqlName),
						empty(fn_name));
			}
		}
	}

}