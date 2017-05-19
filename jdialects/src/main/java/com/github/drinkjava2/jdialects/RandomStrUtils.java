/**
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import java.security.SecureRandom;

/**
 * Generate a random String, use 0-9 a-z characters
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class RandomStrUtils {

	private static final SecureRandom random = new SecureRandom();
	private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

	public static String getRandomString(int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(ALPHABET[random.nextInt(32)]);
		}
		return sb.toString();
	}

}
