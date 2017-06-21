/*
 * jDialects, a tiny SQL dialect tool 
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

/**
 * Guess Dialect Utils
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class FunctionUtils {

	/**
	 * The render method translate function template to real SQL piece
	 * 
	 * <pre>
	 * Template can be:
	 * "*": standard SQL function, identical to abc($Params)
	 * "abc($Params)": template with special parameter format:  
	   				"$P1, $P2, $P3, $P4, $P5, $P6..."="$Params"
					"$P1,$P2,$P3,$P4,$P5,$P6..."="$Compact_Params"
					"$P1||$P2||$P3||$P4||$P5||$P6..."="$Lined_Params"
					"$P1+$P2+$P3+$P4+$P5+$P6..."="$Add_Params");
					"$P1 in $P2 in $P3 in $P4 in $P5 in $P6..."="$IN_Params"
			        "$P1%pattern$P2%pattern$P3%pattern$P4%pattern$P5%pattern$P6..."="$Pattern_Params"
					"11%startswith$P2%startswith$P3%startswith$P4%startswith$P5%startswith$P6..."= "$Startswith_Params");
					 "nvl($P1, nvl($P2, nvl($P3, nvl($P4, nvl($P5, $P6...)))))"="$NVL_Params");
	 * 
	 * "0=abc()": function do not support parameter
	 * "1=abc($P1)": function only support 1 parameter
	 * "2=abc($P1,$P2)": function only support 2 parameters
	 * "0=abc()|1=abc($P1)|3=abc($P1,$P2,$P3)": function support 0 or 1 or 3 parameters
	 * 
	 * </pre>
	 * 
	 * @param functionName
	 *            function name
	 * @param args
	 *            function parameters
	 * @return A SQL function piece
	 */
	protected static String render(Dialect d, String functionName, Object... args) {
		String template = d.functions.get(functionName);
		DialectException.assureNotEmpty(template, "Dialect \"" + d + "\" does not support \"" + functionName
				+ "\" function, a full list of supported functions of this dialect can see \"DatabaseDialects.xls\"");
		if ("*".equals(template))
			return new StringBuilder(functionName).append("(").append(StrUtils.arrayToString(args)).append(")")
					.toString();
		// TODO work on here
		// bla bla
		return template;
	}
}
