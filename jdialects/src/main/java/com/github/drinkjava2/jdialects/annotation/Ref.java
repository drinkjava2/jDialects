/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

 
/**
 * Specifies a foreign key for only single column, for multiple columns foreign key, please use &#064;FKey annotation
  
 * <pre>
 *   Example:
 *
 *   &#064;Ref("OtherTable, field1")
 *   private String someField;
 * </pre> 
 *
 * @since jDialects 1.0.5
 */
@Target(FIELD) 
@Retention(RUNTIME)
public @interface Ref {
    /**
     * (Optional) The name of the foreign key. 
     */
    String name() default "";
 
    /**
     * Referenced table name and columns, separated by ","
     */
    String[] ref() default {};	
}
