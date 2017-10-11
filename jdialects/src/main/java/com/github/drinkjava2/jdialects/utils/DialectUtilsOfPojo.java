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
import java.util.concurrent.ConcurrentHashMap;

import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.TypeUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.springsrc.utils.ReflectionUtils;

/**
 * The tool to convert POJO classes to TableModels
 * 
 * @author Yong Zhu
 * @since 1.0.6
 */
public abstract class DialectUtilsOfPojo {// NOSONAR

	private static Map<Class<?>, TableModel> tableModelCache = new ConcurrentHashMap<Class<?>, TableModel>();

	private static boolean matchNameCheck(String annotationName, String cName) {
		if (("javax.persistence." + annotationName).equals(cName))
			return true;
		if (("com.github.drinkjava2.jdialects.annotation.jpa." + annotationName).equals(cName))
			return true;
		if (("com.github.drinkjava2.jdialects.annotation.jdia." + annotationName).equals(cName))
			return true;
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

	private static boolean existPojoAnnotation(Object targetClass, String annotationName) {
		Map<String, Object> annotion = getFirstPojoAnnotation(targetClass, annotationName);
		return annotion.size() == 1;
	}

	/** Change Annotation fields values into a Map */
	private static Map<String, Object> changeAnnotationValuesToMap(Annotation annotation,
			Class<? extends Annotation> type) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("AnnotationExist", true);
		for (Method method : type.getDeclaredMethods())
			try {
				result.put(method.getName(), method.invoke(annotation, (Object[]) null));
			} catch (Exception e) {// NOSONAR
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

	/**
	 * Convert a Java POJO class or JPA annotated POJO classes to "TableModel"
	 * Object, if this class has a "config(TableModel tableModel)" method, will also
	 * call it
	 */
	public static TableModel pojo2Model(Class<?> pojoClass) {
		DialectException.assureNotNull(pojoClass, "POJO class can not be null");
		TableModel model = tableModelCache.get(pojoClass);
		if (model != null)
			return model.newCopy();
		model = pojo2ModelIgnoreConfigMethod(pojoClass);
		Method method = null;
		try {
			method = pojoClass.getMethod("config", TableModel.class);
		} catch (Exception e) {// NOSONAR
		}
		if (method != null)
			try {
				method.invoke(null, model);
			} catch (Exception e) {
				throw new DialectException(e);
			}

		if (model != null)
			tableModelCache.put(pojoClass, model);
		return model.newCopy();
	}

	/**
	 * Convert a Java POJO class or JPA annotated POJO classes to "TableModel"
	 * Object, ignore config method
	 */
	private static TableModel pojo2ModelIgnoreConfigMethod(Class<?> pojoClass) {
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
		Map<String, Object> seqMap = getFirstPojoAnnotation(pojoClass, "SequenceGenerator");
		if (!seqMap.isEmpty()) {
			model.sequenceGenerator((String) seqMap.get("name"), (String) seqMap.get("sequenceName"),
					(Integer) seqMap.get("initialValue"), (Integer) seqMap.get("allocationSize"));
		}

		// TableGenerator
		Map<String, Object> tableGenMap = getFirstPojoAnnotation(pojoClass, "TableGenerator");
		if (!tableGenMap.isEmpty()) {
			model.tableGenerator((String) tableGenMap.get("name"), (String) tableGenMap.get("table"),
					(String) tableGenMap.get("pkColumnName"), (String) tableGenMap.get("valueColumnName"),
					(String) tableGenMap.get("pkColumnValue"), (Integer) tableGenMap.get("initialValue"),
					(Integer) tableGenMap.get("allocationSize"));
		}

		// UUIDAny
		Map<String, Object> uuidAnyMp = getFirstPojoAnnotation(pojoClass, "UUIDAny");
		if (!uuidAnyMp.isEmpty()) {
			model.uuidAny((String) uuidAnyMp.get("name"), (Integer) uuidAnyMp.get("length"));
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
			String pojofieldName = pd.getName();
			if ("class".equals(pojofieldName) || "simpleName".equals(pojofieldName)
					|| "canonicalName".equals(pojofieldName))
				continue;
			Class<?> propertyClass = pd.getPropertyType();

			if (TypeUtils.canMapToSqlType(propertyClass)) {
				Field field = ReflectionUtils.findField(pojoClass, pojofieldName);
				if (field == null)
					continue;
				// DialectException.assureNotNull(field, "Pojo field '" + pojofieldName + "'
				// found a null value");

				if (!getFirstPojoAnnotation(field, "Transient").isEmpty()) {
					ColumnModel col = new ColumnModel(pojofieldName);
					col.setColumnType(TypeUtils.toType(propertyClass));
					col.setLengths(new Integer[] { 255, 0, 0 });
					col.setTransientable(true);
					col.setTableModel(model);
					model.addColumn(col);
				} else {

					// SequenceGenerator
					Map<String, Object> map = getFirstPojoAnnotation(field, "SequenceGenerator");
					if (!map.isEmpty()) {
						model.sequenceGenerator((String) map.get("name"), (String) map.get("sequenceName"),
								(Integer) map.get("initialValue"), (Integer) map.get("allocationSize"));
					}

					// TableGenerator
					map = getFirstPojoAnnotation(field, "TableGenerator");
					if (!map.isEmpty()) {
						model.tableGenerator((String) map.get("name"), (String) map.get("table"),
								(String) map.get("pkColumnName"), (String) map.get("valueColumnName"),
								(String) map.get("pkColumnValue"), (Integer) map.get("initialValue"),
								(Integer) map.get("allocationSize"));
					}

					// UUIDAny
					map = getFirstPojoAnnotation(field, "UUIDAny");
					if (!map.isEmpty()) {
						model.uuidAny((String) map.get("name"), (Integer) map.get("length"));
					}

					ColumnModel col = new ColumnModel(pojofieldName);
					col.pojoField(pojofieldName);
					// Column
					Map<String, Object> colMap = getFirstPojoAnnotation(field, "Column");
					if (!colMap.isEmpty()) {
						if (!(Boolean) colMap.get("nullable"))
							col.setNullable(false);
						if (!StrUtils.isEmpty(colMap.get("name")))
							col.setColumnName((String) colMap.get("name"));
						col.setLength((Integer) colMap.get("length"));
						col.setPrecision((Integer) colMap.get("precision"));
						col.setScale((Integer) colMap.get("scale"));
						col.setLengths(new Integer[] { col.getLength(), col.getPrecision(), col.getScale() });
						if (!StrUtils.isEmpty(colMap.get("columnDefinition")))
							col.setColumnType(TypeUtils.toType((String) colMap.get("columnDefinition")));
						else
							col.setColumnType(TypeUtils.toType(propertyClass));
						col.setInsertable((Boolean) colMap.get("insertable"));
						col.setUpdatable((Boolean) colMap.get("updatable"));
					} else {
						col.setColumnType(TypeUtils.toType(propertyClass));
						col.setLengths(new Integer[] { 255, 0, 0 });
					}

					// Id
					if (!getFirstPojoAnnotation(field, "Id").isEmpty()
							|| !getFirstPojoAnnotation(field, "PKey").isEmpty())
						col.pkey();

					col.setPojoField(pojofieldName);
					col.setTableModel(model);
					// col will also set TableModel field point to its owner
					model.addColumn(col);

					// shortcut Id generator annotations
					if (existPojoAnnotation(field, "AutoId"))
						col.autoId();
					if (existPojoAnnotation(field, "IdentityId"))
						col.identityId();
					if (existPojoAnnotation(field, "TimeStampId"))
						col.timeStampId();
					if (existPojoAnnotation(field, "UUID25"))
						col.uuid25();
					if (existPojoAnnotation(field, "UUID32"))
						col.uuid32();
					if (existPojoAnnotation(field, "UUID36"))
						col.uuid36();

					// GeneratedValue
					Map<String, Object> gvMap = getFirstPojoAnnotation(field, "GeneratedValue");
					if (!gvMap.isEmpty()) {
						Object strategy = gvMap.get("strategy");
						if (strategy != null) {
							String strategyStr = strategy.toString();

							if ("AUTO".equals(strategyStr))
								col.autoId();
							else if ("IDENTITY".equals(strategyStr))
								col.identityId();
							else if ("UUID25".equals(strategyStr))
								col.uuid25();
							else if ("UUID32".equals(strategyStr))
								col.uuid32();
							else if ("UUID36".equals(strategyStr))
								col.uuid36();
							else if ("TIMESTAMP".equals(strategyStr))
								col.timeStampId();
							else {
								String generator = (String) gvMap.get("generator");
								if (StrUtils.isEmpty(generator))
									throw new DialectException(
											"GeneratedValue strategy '" + strategyStr + "' can not find a generator");
								col.idGenerator(generator);
							}
						}
					}

					// SingleFKey is a shortcut format of FKey, only for 1
					// column
					Map<String, Object> refMap = getFirstPojoAnnotation(field, "SingleFKey");
					if (!refMap.isEmpty())
						model.fkey((String) refMap.get("name")).columns(col.getColumnName())
								.refs((String[]) refMap.get("refs"));

					// SingleIndex is a ShortCut format of Index, only for 1
					// column
					Map<String, Object> idxMap = getFirstPojoAnnotation(field, "SingleIndex");
					if (!idxMap.isEmpty())
						model.index((String) idxMap.get("name")).columns(col.getColumnName());

					// SingleUnique is a ShortCut format of Unique, only for 1
					// column
					Map<String, Object> uniMap = getFirstPojoAnnotation(field, "SingleUnique");
					if (!uniMap.isEmpty())
						model.unique((String) uniMap.get("name")).columns(col.getColumnName());
				}
			}
		} // End of columns loop
		return model;
	}

}
