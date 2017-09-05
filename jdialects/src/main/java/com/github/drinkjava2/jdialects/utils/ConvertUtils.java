/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.github.drinkjava2.jdialects.ColumnDef;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.model.VColumn;
import com.github.drinkjava2.jdialects.model.VTable;
import com.github.drinkjava2.jdialects.springsrc.utils.ReflectionUtils;
import com.github.drinkjava2.jdialects.springsrc.utils.StringUtils;

/**
 * This utility tool should have below methods:
 * 
 * pojo2VTable() method: Convert POJO or JPA annotated POJO classes to "VTable"
 * Object, this method only support below JPA Annotations:
 * Column,GeneratedValue,GenerationType,Id,Index,Table,Transient,UniqueConstraint
 * 
 * vTable2Excel() method: Convert VTable Object to Excel CSV format text
 *
 * excel2VTable() method: Convert Excel CSV format text to VTable Object
 * 
 * vTable2JpaPOJO() method: Convert VTable Object to JPA annotated POJO Java
 * Source code
 * 
 * vTable2DdlPOJO() method: Convert VTable Objects to jDialects style POJO Java
 * Source code, i.e., pure POJO no annotations but has a method: "public static
 * VTable vTable(){} "
 * 
 * @author Yong Zhu
 * @since 1.0.5
 */
public abstract class ConvertUtils {

	@Entity
	@Table(name = "persn", //
			uniqueConstraints = { @UniqueConstraint(columnNames = { "field1" }),
					@UniqueConstraint(columnNames = { "field1", "field2" }) }, //
			indexes = { @Index(name = "my_index_name", columnList = "iso_code, anothercode", unique = true),
					@Index(name = "my_index_name2", columnList = "name", unique = false) }//
	)
	public static class POJO {
		@Id
		@Column(columnDefinition = ColumnDef.VARCHAR, length = 20)
		public String field1;

		@Transient
		@Column(name = "field2", nullable = false, columnDefinition = ColumnDef.BIGINT)
		public String field2;

		public String getField1() {
			return field1;
		}

		public void setField1(String field1) {
			this.field1 = field1;
		}

		public String getField2() {
			return field2;
		}

		public void setField2(String field2) {
			this.field2 = field2;
		}
	}

	public static void main(String[] args) {
		String[] ddls = Dialect.H2Dialect.toCreateDDL(ConvertUtils.pojo2VTable(POJO.class));
		for (String ddl : ddls)
			System.out.println(ddl);
	}

	/**
	 * Convert POJO or JPA annotated POJO classes to "VTable" Object, this method
	 * only support below JPA Annotations: Entity, Table, Column, GeneratedValue,
	 * GenerationType, Id, Index, Transient, UniqueConstraint
	 * 
	 * @param pojoClass
	 * @return VTable
	 */
	public static VTable pojo2VTable(Class<?> pojoClass) {
		DialectException.assureNotNull(pojoClass, "pojo2VTable method does not accept a null parameter");
		String tableName = null;
		Entity entity = pojoClass.getAnnotation(Entity.class);// Entity
		if (entity != null)
			tableName = entity.name();
		Table table = pojoClass.getAnnotation(Table.class);// Table
		if (table != null && !StrUtils.isEmpty(table.name()))
			tableName = table.name();
		if (StrUtils.isEmpty(tableName))
			tableName = pojoClass.getSimpleName();
		VTable vtable = new VTable(tableName);

		BeanInfo beanInfo = null;
		PropertyDescriptor[] pds = null;
		try {
			beanInfo = Introspector.getBeanInfo(pojoClass);
			pds = beanInfo.getPropertyDescriptors();
		} catch (Exception e) {
			DialectException.throwEX(e, "pojo2VTable can not get bean info");
		}

		for (PropertyDescriptor pd : pds) {
			String entityField = pd.getName();
			Class<?> propertyClass = pd.getPropertyType();
			if (ColumnDef.canMapToSqlType(propertyClass)) {
				Field field = ReflectionUtils.findField(pojoClass, entityField);
				if (null == field.getAnnotation(Transient.class)) {// Not Transient
					VColumn vcolumn = new VColumn(entityField);
					vcolumn.entityField(entityField);
					Column col = field.getAnnotation(Column.class);// Column
					if (col != null) {
						if (!col.nullable())
							vcolumn.setNullable(false);
						if (!StrUtils.isEmpty(col.name()))
							vcolumn.setColumnName(col.name());
						vcolumn.setLength(col.length());
						vcolumn.setPrecision(col.precision());
						vcolumn.setScale(col.scale());
						vcolumn.setLengths(new Integer[] { col.length(), col.precision(), col.scale() });
						vcolumn.setColumnType(ColumnDef.toType(col.columnDefinition()));
					} else {
						vcolumn.setColumnType(ColumnDef.toType(propertyClass));
						vcolumn.setLengths(new Integer[] { 255, 0, 0 });
					}
					if (null != field.getAnnotation(Id.class))// Id
						vcolumn.pkey();
					vtable.addColumn(vcolumn);
				}
			}
		}
		if (table != null) {
			Index[] indexes = table.indexes();// Index
			if (indexes != null) {
				for (Index index : indexes) {
					String indexName = index.name();
					String columns = index.columnList();
					if (!StrUtils.isEmpty(columns)) {
						String[] indexColumnList;
						if (columns.indexOf(',') >= 0)
							indexColumnList = StringUtils.split(columns, ",");
						else
							indexColumnList = new String[] { columns };

						if (indexColumnList != null && indexColumnList.length > 0) {
							vtable.index(indexName).columns(indexColumnList).setUnique(index.unique());
						}
					}
				}
			}

			UniqueConstraint[] uniques = table.uniqueConstraints();// Unique
			if (uniques != null) {
				for (UniqueConstraint unique : uniques) {
					String indexName = unique.name();
					String[] columns = unique.columnNames();
					if (columns != null && columns.length > 0)
						vtable.unique(indexName).columns(columns);
				}
			}
		}

		return vtable;
	}

}
