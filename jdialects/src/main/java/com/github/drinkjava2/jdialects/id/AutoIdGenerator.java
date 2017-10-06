/**
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jdialects.id;

import com.github.drinkjava2.jdbpro.NormalJdbcTool;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.annotation.GenerationType;
import com.github.drinkjava2.jdialects.utils.StrUtils;

/**
 * AutoGenerator will depends database's id generator mechanism like MySql's
 * Identity, Oracle's Sequence...
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class AutoIdGenerator implements IdGenerator {
	public static final String JDIALECTS_AUTOID = "jdialects_autoid";
	public static final String NEXT_VAL = "next_val";

	public static final AutoIdGenerator INSTANCE = new AutoIdGenerator();

	@Override
	public GenerationType getGenerationType() {
		return GenerationType.AUTO;
	}

	@Override
	public String getIdGenName() {
		return "AUTO";
	}

	@Override
	public IdGenerator newCopy() {
		return INSTANCE;
	};

	@Override
	public Object getNextID(NormalJdbcTool jdbc, Dialect dialect) {
		Long result;
		if (dialect.getDdlFeatures().supportBasicOrPooledSequence()) {
			String sql = StrUtils.replace(dialect.getDdlFeatures().getSequenceNextValString(), "_SEQNAME",
					JDIALECTS_AUTOID);
			result = jdbc.nQueryForObject(sql);
			DialectException.assureNotNull(result,
					"Null value found when fetch Auto-Generated ID from sequence '" + JDIALECTS_AUTOID + "'");
		} else {
			String sql = "update " + JDIALECTS_AUTOID + " set " + NEXT_VAL + "=(" + NEXT_VAL + "+1)";
			int updatedCount = jdbc.nUpdate(sql);
			if (updatedCount != 1)
				throw new DialectException(
						"When fetch Auto-Generated ID, can not read from \"" + JDIALECTS_AUTOID + "\" table");
			result = jdbc.nQueryForObject("select " + NEXT_VAL + " from " + JDIALECTS_AUTOID);
			DialectException.assureNotNull(result,
					"Null value found when fetch Auto-Generated ID from table \"" + JDIALECTS_AUTOID + "\"");
		}
		return result;
	}

}
