/*
* jDialects, a tiny SQL dialect tool 
*
* License: GNU Lesser General Public License (LGPL), version 2.1 or later.
* See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
*/
package com.github.drinkjava2.jdialects;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * For logger output, to avoid logger jar version conflict, default use JDK log,
 * if found commons log, use it, if found Log4j use it..., by this way this
 * project has no dependency to any logger jar.
 * 
 * @author Yong Zhu
 * @since 1.0.1
 */
public class DialectLogger {
	private Object commonLogger;
	private Method commonLoggerInfoMethod;
	private Method commonLoggerWarnMethod;
	private Method commonLoggerErrorMethod;
	private Logger jdkLogger;
	private static boolean showedErrMsg = false;

	private static boolean enableLog = true;

	public DialectLogger(Class<?> targetClass) {
		if (targetClass == null)
			throw new AssertionError("DialectLogger error: targetClass can not be null.");
		try {
			Class<?> logFactoryClass = Class.forName("org.apache.commons.logging.LogFactory");
			Method method = logFactoryClass.getMethod("getLog", Class.class);
			commonLogger = method.invoke(logFactoryClass, targetClass);
			commonLoggerInfoMethod = commonLogger.getClass().getMethod("info", Object.class);
			commonLoggerWarnMethod = commonLogger.getClass().getMethod("warn", Object.class);
			commonLoggerErrorMethod = commonLogger.getClass().getMethod("error", Object.class);
		} catch (Exception e) {
			DialectException.eatException(e);
		}
		if (commonLogger == null || commonLoggerWarnMethod == null) {
			if (!showedErrMsg)
				System.err.println("jDialects failed to load org.apache.commons.logging.LogFactory, use JDK logger."); //NOSONAR
			jdkLogger = Logger.getLogger(targetClass.getName());// use JDK log
		}

	}

	public static DialectLogger getLog(Class<?> targetClass) {
		return new DialectLogger(targetClass);
	}

	public static void setEnableLog(boolean enablelog) {
		enableLog = enablelog;
	}

	public void info(String msg) {
		if (!enableLog)
			return;
		if (jdkLogger != null) {
			jdkLogger.log(Level.INFO, msg);
			return;
		}
		try {
			commonLoggerInfoMethod.invoke(commonLogger, msg);
		} catch (Exception e) {
			DialectException.eatException(e);
			throw new AssertionError(e.getMessage());
		}
	}

	public void warn(String msg) {
		if (!enableLog)
			return;
		if (jdkLogger != null) {
			jdkLogger.log(Level.WARNING, msg);
			return;
		}
		try {
			commonLoggerWarnMethod.invoke(commonLogger, msg);
		} catch (Exception e) {
			DialectException.eatException(e);
			throw new AssertionError(e.getMessage());
		}
	}

	public void error(String msg) {
		if (!enableLog)
			return;
		if (jdkLogger != null) {
			jdkLogger.log(Level.SEVERE, msg);
			return;
		}
		try {
			commonLoggerErrorMethod.invoke(commonLogger, msg);
		} catch (Exception e) {
			DialectException.eatException(e);
			throw new AssertionError(e.getMessage());
		}
	}
}
