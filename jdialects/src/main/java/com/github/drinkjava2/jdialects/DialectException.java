/*
* jDialects, a tiny SQL dialect tool 
*
* License: GNU Lesser General Public License (LGPL), version 2.1 or later.
* See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
*/
package com.github.drinkjava2.jdialects;

/**
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class DialectException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DialectException() {
		// Default constructor
	}

	public DialectException(String message) {
		super(message);
	}

	public static Object throwEX(Exception e, String errorMsg) {
		throw new DialectException(errorMsg);
	}

	public static Object throwEX(String errorMsg) {
		return throwEX(null, errorMsg);
	}

	public static void assureNotNull(Object obj, String... optionMessages) {
		if (obj == null)
			throw new NullPointerException(optionMessages.length == 0 ? "" : optionMessages[0]);
	}

}
