/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines a primary key generator that may be 
 * referenced by name when a generator element is specified for 
 * the {@link GeneratedValue} annotation. A table generator 
 * may be specified on the entity class or on the primary key 
 * field or property. The scope of the generator name is global 
 * to the persistence unit (across all generator types).
 *
 * <pre>
 *    Example 1:
 *    
 *    &#064;Entity public class Employee {
 *        ...
 *        &#064;TableGenerator(
 *            name="empGen", 
 *            table="ID_GEN", 
 *            pkColumnName="GEN_KEY", 
 *            valueColumnName="GEN_VALUE", 
 *            pkColumnValue="EMP_ID", 
 *            allocationSize=1)
 *        &#064;Id
 *        &#064;GeneratedValue(strategy=TABLE, generator="empGen")
 *        int id;
 *        ...
 *    }
 *    
 *    Example 2:
 *    
 *    &#064;Entity public class Address {
 *        ...
 *        &#064;TableGenerator(
 *            name="addressGen", 
 *            table="ID_GEN", 
 *            pkColumnName="GEN_KEY", 
 *            valueColumnName="GEN_VALUE", 
 *            pkColumnValue="ADDR_ID")
 *        &#064;Id
 *        &#064;GeneratedValue(strategy=TABLE, generator="addressGen")
 *        int id;
 *        ...
 *    }
 * </pre>
 *
 * @see GeneratedValue
 *
 * @since Java Persistence 1.0
 */
@Target({TYPE, METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface TableGenerator2 {

    /** 
     * (Required) A unique generator name that can be referenced 
     * by one or more classes to be the generator for id values.
     */
    String name();

    /** 
     * (Optional) Name of table that stores the generated id values. 
     * <p> Defaults to a name chosen by persistence provider.
     */
    String table() default "";

    /** (Optional) The catalog of the table. 
     * <p> Defaults to the default catalog.
     */
    String catalog() default "";

    /** (Optional) The schema of the table. 
     * <p> Defaults to the default schema for user.
     */
    String schema() default "";

    /** 
     * (Optional) Name of the primary key column in the table.
     * <p> Defaults to a provider-chosen name.
     */
    String pkColumnName() default "";

    /** 
     * (Optional) Name of the column that stores the last value generated.
     * <p> Defaults to a provider-chosen name.
     */
    String valueColumnName() default "";

    /**
     * (Optional) The primary key value in the generator table 
     * that distinguishes this set of generated values from others 
     * that may be stored in the table.
     * <p> Defaults to a provider-chosen value to store in the 
     * primary key column of the generator table
     */
    String pkColumnValue() default "";

    /** 
     * (Optional) The initial value to be used to initialize the column
     * that stores the last value generated.
     */
    int initialValue() default 0;

    /**
     * (Optional) The amount to increment by when allocating id 
     * numbers from the generator.
     */
    int allocationSize() default 50;

    /**
     * (Optional) Unique constraints that are to be placed on the 
     * table. These are only used if table generation is in effect. 
     * These constraints apply in addition to primary key constraints.
     * <p> Defaults to no additional constraints.
     */
    UniqueConstraint[] uniqueConstraints() default {};

    /**
     * (Optional) Indexes for the table.  These are only used if
     * table generation is in effect.  Note that it is not necessary
     * to specify an index for a primary key, as the primary key
     * index will be created automatically.
     *
     * @since Java Persistence 2.1 
     */
    Index[] indexes() default {};
}