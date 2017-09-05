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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.github.drinkjava2.jdialects.ColumnDef;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.SequenceGen;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.springsrc.utils.ReflectionUtils;
import com.github.drinkjava2.jdialects.springsrc.utils.StringUtils;

/**
 * This utility tool should have below methods:
 * 
 * pojo2TableModel() method: Convert POJO or JPA annotated POJO classes to
 * "TableModel" Object, this method only support below JPA Annotations:
 * Entity,Column,GeneratedValue,GenerationType,Id,Index,Table,Transient,UniqueConstraint,sequenceGenerator,TableGenerator
 * 
 * TableModel2Excel() method: Convert TableModel Object to Excel CSV format text
 *
 * excel2TableModel() method: Convert Excel CSV format text to TableModel Object
 * 
 * TableModel2JpaPOJO() method: Convert TableModel Object to JPA annotated POJO
 * Java Source code
 * 
 * TableModel2DdlPOJO() method: Convert TableModel Objects to jDialects style
 * POJO Java Source code, i.e., pure POJO no annotations but has a method:
 * "public static TableModel TableModel(){} "
 * 
 * @author Yong Zhu
 * @since 1.0.5
 */
public abstract class ConvertUtils {

	@Entity
	@Table(name = "testpo", //
			uniqueConstraints = { @UniqueConstraint(columnNames = { "field1" }),
					@UniqueConstraint(name = "cons2", columnNames = { "field1", "field2" }) }, //
			indexes = { @Index(columnList = "field1,field2", unique = true),
					@Index(name = "idx2", columnList = "field2", unique = false) }//
	)
	public static class POJO {
		@Id
		@Column(columnDefinition = ColumnDef.VARCHAR, length = 20)
		public String field1;

		@Transient
		@Column(name = "field2", nullable = false, columnDefinition = ColumnDef.BIGINT)
		public String field2;

		@GeneratedValue(strategy = GenerationType.TABLE, generator = "CUST_GEN")
		public Integer field3;

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
		String[] ddls = Dialect.H2Dialect.toCreateDDL(ConvertUtils.pojo2TableModel(POJO.class));
		for (String ddl : ddls)
			System.out.println(ddl);
	}

	/**
	 * Convert POJO or JPA annotated POJO classes to "TableModel" Object, this
	 * method only support below JPA Annotations: Entity, Table, Column,
	 * GeneratedValue, GenerationType, Id, Index, Transient, UniqueConstraint
	 * 
	 * @param pojoClass
	 * @return TableModel
	 */
	public static TableModel pojo2TableModel(Class<?> pojoClass) {
		DialectException.assureNotNull(pojoClass, "pojo2TableModel method does not accept a null parameter");
		String tableName = null;
		Entity entity = pojoClass.getAnnotation(Entity.class);// Entity
		if (entity != null)
			tableName = entity.name();
		Table table = pojoClass.getAnnotation(Table.class);// Table
		if (table != null && !StrUtils.isEmpty(table.name()))
			tableName = table.name();
		if (StrUtils.isEmpty(tableName))
			tableName = pojoClass.getSimpleName();
		TableModel TableModel = new TableModel(tableName);

		SequenceGenerator seqGen = pojoClass.getAnnotation(SequenceGenerator.class);// SequenceGenerator
		if (seqGen != null)
			TableModel.addSequence(new SequenceGen(seqGen.name(), seqGen.sequenceName(), seqGen.initialValue(),
					seqGen.allocationSize()));

		TableGenerator tb = pojoClass.getAnnotation(TableGenerator.class);// TableGenerator
		if (tb != null)
			TableModel.addTableGenerator(tb.name(), tb.table(), tb.pkColumnName(), tb.valueColumnName(),
					tb.pkColumnValue(), tb.initialValue(), tb.allocationSize());

		BeanInfo beanInfo = null;
		PropertyDescriptor[] pds = null;
		try {
			beanInfo = Introspector.getBeanInfo(pojoClass);
			pds = beanInfo.getPropertyDescriptors();
		} catch (Exception e) {
			DialectException.throwEX(e, "pojo2TableModel can not get bean info");
		}

		for (PropertyDescriptor pd : pds) {
			String entityField = pd.getName();
			Class<?> propertyClass = pd.getPropertyType();
			if (ColumnDef.canMapToSqlType(propertyClass)) {
				Field field = ReflectionUtils.findField(pojoClass, entityField);
				if (null == field.getAnnotation(Transient.class)) {// Transient
					ColumnModel vcolumn = new ColumnModel(entityField);
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

					SequenceGenerator seq = field.getAnnotation(SequenceGenerator.class);// SequenceGenerator
					if (seq != null)
						TableModel.addSequence(new SequenceGen(seq.name(), seq.sequenceName(), seq.initialValue(),
								seq.allocationSize()));

					TableGenerator tb2 = pojoClass.getAnnotation(TableGenerator.class);// TableGenerator
					if (tb2 != null)
						TableModel.addTableGenerator(tb2.name(), tb2.table(), tb2.pkColumnName(), tb2.valueColumnName(),
								tb2.pkColumnValue(), tb2.initialValue(), tb2.allocationSize());

					if (null != field.getAnnotation(Id.class))// Id
						vcolumn.pkey();
					TableModel.addColumn(vcolumn);

					GeneratedValue gv = field.getAnnotation(GeneratedValue.class);// GeneratedValue
					if (gv != null) {
						if (GenerationType.AUTO.equals(gv.annotationType()))
							vcolumn.autoID();
						else if (GenerationType.SEQUENCE.equals(gv.annotationType()))
							vcolumn.sequence(gv.generator());
						else if (GenerationType.IDENTITY.equals(gv.annotationType()))
							vcolumn.identity();
						else if (GenerationType.TABLE.equals(gv.annotationType()))
							vcolumn.tableGenerator(gv.generator());
					}
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
							TableModel.index(indexName).columns(indexColumnList).setUnique(index.unique());
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
						TableModel.unique(indexName).columns(columns);
				}
			}
		}

		return TableModel;
	}

}
