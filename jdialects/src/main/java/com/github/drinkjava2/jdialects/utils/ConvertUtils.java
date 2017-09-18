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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdialects.ColumnDef;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.annotation.GenerationType;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.SequenceGen;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.springsrc.utils.ReflectionUtils;

/**
 * This utility tool should have below methods:
 * 
 * pojo2Model() method: Convert POJO or JPA annotated POJO classes to
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
 * TableModel2PurePOJO(ifHas) method: Convert TableModel Objects to pure POJO or
 * jDialects style POJO Java Source code. i.e., jDialects POJO is a pure POJO
 * with a method: "public static TableModel TableModel(){} "
 * 
 * @author Yong Zhu
 * @since 1.0.5
 */
public abstract class ConvertUtils {

	private static boolean matchNameCheck(String annotationName, String cName) {
		if (("javax.persistence." + annotationName).equals(cName))
			return true;
		if (("com.github.drinkjava2.jdialects.annotation." + annotationName).equals(cName))
			return true;
		for (int i = 1; i <= 3; i++) {// Java6 no allow repeat annotation, have to use FKey1, Fkey2, Fkey3
			if (("com.github.drinkjava2.jdialects.annotation." + annotationName + i).equals(cName))
				return true;
		}
		return false;
	}

	private static List<Map<String, Object>> getPojoAnnotations(Object targetClass, String annotationName) {
		Annotation[] anno = null;
		if (targetClass instanceof Field)
			anno = ((Field) targetClass).getAnnotations();
		else
			anno = ((Class<?>) targetClass).getAnnotations();
		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		for (Annotation annotation : anno) {
			Class<? extends Annotation> type = annotation.annotationType();
			String cName = type.getName();
			if (matchNameCheck(annotationName, cName)) {
				l.add(changeAnnotationValuesToMap(annotation, type));
			}
		}
		return l;
	}

	private static Map<String, Object> getFirstPojoAnnotation(Object targetClass, String annotationName) {
		Annotation[] anno = null;
		if (targetClass instanceof Field)
			anno = ((Field) targetClass).getAnnotations();
		else
			anno = ((Class<?>) targetClass).getAnnotations();
		for (Annotation annotation : anno) {
			Class<? extends Annotation> type = annotation.annotationType();
			String cName = type.getName();
			if (matchNameCheck(annotationName, cName))
				return changeAnnotationValuesToMap(annotation, type);
		}
		return new HashMap<String, Object>();
	}

	/** Change Annotation fields values into a Map */
	private static Map<String, Object> changeAnnotationValuesToMap(Annotation annotation,
			Class<? extends Annotation> type) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("AnnotationExist", true);
		for (Method method : type.getDeclaredMethods())
			try {
				result.put(method.getName(), method.invoke(annotation, (Object[]) null));
			} catch (Exception e) {
			}
		return result;
	}

	/**
	 * Convert POJO or JPA annotated POJO classes to "TableModel" Object,
	 * 
	 * <pre>
	 * This method support below JPA Annotations:  
	 * Entity, Table, Column, GeneratedValue, GenerationType, Id, Index, Transient, UniqueConstraint
	 * 
	 * And below annotations are added by jDialects:
	 * FKey, FKey1, FKey2, FKey3, Ref
	 * </pre>
	 * 
	 * @param pojoClass
	 * @return TableModel
	 */
	public static TableModel[] pojos2Models(Class<?>... pojoClasses) {
		List<TableModel> l = new ArrayList<TableModel>();
		for (Class<?> clazz : pojoClasses) {
			l.add(pojo2Model(clazz));
		}
		return l.toArray(new TableModel[l.size()]);
	}

	public static TableModel pojo2Model(Class<?> pojoClass) {
		DialectException.assureNotNull(pojoClass, "pojo2Model method does not accept a null class");

		// Entity
		String tableName = null;
		Map<String, Object> entityMap = getFirstPojoAnnotation(pojoClass, "Entity");
		tableName = (String) entityMap.get("name");

		// Table
		Map<String, Object> tableMap = getFirstPojoAnnotation(pojoClass, "Table");
		if (!StrUtils.isEmpty(tableMap.get("name")))
			tableName = (String) tableMap.get("name");
		if (StrUtils.isEmpty(tableName))
			tableName = pojoClass.getSimpleName();
		TableModel model = new TableModel(tableName); // Build the tableModel

		if (!tableMap.isEmpty()) {
			// Index
			Annotation[] indexes = (Annotation[]) tableMap.get("indexes");
			if (indexes != null && indexes.length > 0)
				for (Annotation anno : indexes) {
					Map<String, Object> mp = changeAnnotationValuesToMap(anno, anno.annotationType());
					String columnListString = (String) mp.get("columnList");
					String[] columns;
					if (columnListString.indexOf(',') >= 0)
						columns = columnListString.split(",");
					else
						columns = new String[] { columnListString };
					if (columns.length > 0)
						model.index((String) mp.get("name")).columns(columns).setUnique((Boolean) mp.get("unique"));
				}

			// Unique
			Annotation[] uniques = (Annotation[]) tableMap.get("uniqueConstraints");
			if (uniques != null && uniques.length > 0)
				for (Annotation anno : uniques) {
					Map<String, Object> mp = changeAnnotationValuesToMap(anno, anno.annotationType());
					String[] columnNames = (String[]) mp.get("columnNames");
					if (columnNames != null && columnNames.length > 0)
						model.unique((String) mp.get("name")).columns(columnNames);
				}
		}

		// SequenceGenerator
		List<Map<String, Object>> sequences = getPojoAnnotations(pojoClass, "SequenceGenerator");
		for (Map<String, Object> map : sequences) {
			model.sequenceGenerator(new SequenceGen((String) map.get("name"), (String) map.get("sequenceName"),
					(Integer) map.get("initialValue"), (Integer) map.get("allocationSize")));
		}

		// TableGenerator
		List<Map<String, Object>> tableGens = getPojoAnnotations(pojoClass, "TableGenerator");
		for (Map<String, Object> map : tableGens) {
			model.tableGenerator((String) map.get("name"), (String) map.get("table"), (String) map.get("pkColumnName"),
					(String) map.get("valueColumnName"), (String) map.get("pkColumnValue"),
					(Integer) map.get("initialValue"), (Integer) map.get("allocationSize"));
		}

		// FKey
		List<Map<String, Object>> fkeys = getPojoAnnotations(pojoClass, "FKey");
		for (Map<String, Object> map : fkeys) {
			model.fkey((String) map.get("name")).columns((String[]) map.get("columns"))
					.refs((String[]) map.get("refs"));
		}

		BeanInfo beanInfo = null;
		PropertyDescriptor[] pds = null;
		try {
			beanInfo = Introspector.getBeanInfo(pojoClass);
			pds = beanInfo.getPropertyDescriptors();
		} catch (Exception e) {
			DialectException.throwEX(e, "pojo2Model can not get bean info");
		}

		for (PropertyDescriptor pd : pds) {
			String entityField = pd.getName();
			Class<?> propertyClass = pd.getPropertyType();
			if (ColumnDef.canMapToSqlType(propertyClass)) {
				Field field = ReflectionUtils.findField(pojoClass, entityField);
				// Transient
				if (getFirstPojoAnnotation(field, "Transient").isEmpty()) {
					ColumnModel vcolumn = new ColumnModel(entityField);
					vcolumn.pojoField(entityField);
					// Column
					Map<String, Object> colMap = getFirstPojoAnnotation(field, "Column");
					if (!colMap.isEmpty()) {
						if (!(Boolean) colMap.get("nullable"))
							vcolumn.setNullable(false);
						if (!StrUtils.isEmpty(colMap.get("name")))
							vcolumn.setColumnName((String) colMap.get("name"));
						vcolumn.setLength((Integer) colMap.get("length"));
						vcolumn.setPrecision((Integer) colMap.get("precision"));
						vcolumn.setScale((Integer) colMap.get("scale"));
						vcolumn.setLengths(
								new Integer[] { vcolumn.getLength(), vcolumn.getPrecision(), vcolumn.getScale() });
						if (!StrUtils.isEmpty(colMap.get("columnDefinition")))
							vcolumn.setColumnType(ColumnDef.toType((String) colMap.get("columnDefinition")));
						else
							vcolumn.setColumnType(ColumnDef.toType(propertyClass));
						vcolumn.setInsertable((Boolean) colMap.get("insertable"));
						vcolumn.setUpdatable((Boolean) colMap.get("updatable"));
					} else {
						vcolumn.setColumnType(ColumnDef.toType(propertyClass));
						vcolumn.setLengths(new Integer[] { 255, 0, 0 });
					}

					// SequenceGenerator
					Map<String, Object> seqMap = getFirstPojoAnnotation(field, "SequenceGenerator");
					if (!seqMap.isEmpty())
						model.sequenceGenerator(
								new SequenceGen((String) seqMap.get("name"), (String) seqMap.get("sequenceName"),
										(Integer) seqMap.get("initialValue"), (Integer) seqMap.get("allocationSize")));

					// Id
					if (!getFirstPojoAnnotation(field, "Id").isEmpty())
						vcolumn.pkey();

					model.addColumn(vcolumn);// Should add column first, otherwise ref() method will error

					// GeneratedValue
					Map<String, Object> gvMap = getFirstPojoAnnotation(field, "GeneratedValue");
					if (!gvMap.isEmpty()) {
						GenerationType typ = (GenerationType) gvMap.get("annotationType");
						if (GenerationType.AUTO.equals(typ))
							vcolumn.autoID();
						else if (GenerationType.SEQUENCE.equals(typ))
							vcolumn.sequence((String) gvMap.get("generator"));
						else if (GenerationType.IDENTITY.equals(typ))
							vcolumn.identity();
						else if (GenerationType.TABLE.equals(typ))
							vcolumn.tableGenerator((String) gvMap.get("generator"));
					}

					// SingleFKey is a shortcut format of FKey, only for 1 column
					Map<String, Object> refMap = getFirstPojoAnnotation(field, "SingleFKey");
					if (!refMap.isEmpty())
						model.fkey((String) refMap.get("name")).columns(vcolumn.getColumnName())
								.refs((String[]) refMap.get("refs"));

					// SingleIndex is a ShortCut format of Index, only for 1 column
					Map<String, Object> idxMap = getFirstPojoAnnotation(field, "SingleIndex");
					if (!idxMap.isEmpty())
						model.index((String) idxMap.get("name")).columns(vcolumn.getColumnName());

					// SingleUnique is a ShortCut format of Unique, only for 1 column
					Map<String, Object> uniMap = getFirstPojoAnnotation(field, "SingleUnique");
					if (!uniMap.isEmpty())
						model.unique((String) uniMap.get("name")).columns(vcolumn.getColumnName());
				}
			}
		} // End of columns loop
		return model;
	}

}
