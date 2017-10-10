/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.utils;

import java.sql.Connection;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * This utility tool should have below methods:
 * 
 * pojos2Models() method: Convert POJO classes or annotated POJO classes to
 * TableModel Objects
 * 
 * db2Models() method: Convert JDBC database to TableModels
 * 
 * Models2Excel() method: Convert TableModel Objects to Excel CSV format text
 *
 * excel2Models() method: Convert Excel CSV format text to TableModel Objects
 * 
 * Models2PojoSrc method: Convert TableModel Object to Annotated POJO Java
 * Source code
 * 
 * Models2JavaSrc method: Convert TableModel Objects to Java source code
 * 
 * @author Yong Zhu
 * @since 1.0.5
 */
public abstract class DialectUtils {
	/**
	 * Convert POJO or JPA annotated POJO class to "TableModel" Object, if class
	 * have a "config(TableModel tableModel)" method, will also call it. This method
	 * support annotations on POJO, detail see README.md.
	 * 
	 * @param pojoClass
	 * @return TableModel
	 */
	public static TableModel pojo2Model(Class<?> pojoClass) {
		return DialectUtilsOfPojo.pojo2Model(pojoClass);
	}

	/**
	 * Convert POJO or JPA annotated POJO classes to "TableModel" Object, if these
	 * classes have a "config(TableModel tableModel)" method, will also call it.
	 * This method support Annotations on POJO, detail see README.md.
	 * 
	 * @param pojoClass
	 * @return TableModel
	 */
	public static TableModel[] pojos2Models(Class<?>... pojoClasses) {
		return DialectUtilsOfPojo.pojos2Models(pojoClasses);
	}

	/**
	 * Convert JDBC connected database structure to TableModels, note: <br/>
	 * 1)This method does not close connection, do not forgot close it later <br/>
	 * 2)This method does not support sequence, foreign keys, primary keys..., only
	 * read the database structure, but in future version may support
	 */
	public static TableModel[] db2Models(Connection con, Dialect dialect) {
		return DialectUtilsOfDb.db2Models(con, dialect);
	}
}
