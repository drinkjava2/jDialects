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
import com.github.drinkjava2.jdialects.model.AutoIdGen;
import com.github.drinkjava2.jdialects.utils.StrUtils;

/**
 * AutoGenerator will depends database's id generator mechanism like MySql's
 * Identity, Oracle's Sequence...
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class AutoGenerator implements IdGenerator {
	public static final AutoGenerator INSTANCE = new AutoGenerator();

	@Override
	public Object getNextID(NormalJdbcTool jdbc, Dialect dialect) {
		Long result;
		if (dialect.getDdlFeatures().supportBasicOrPooledSequence()) {
			String sql = StrUtils.replace(dialect.getDdlFeatures().getSequenceNextValString(), "_SEQNAME",
					AutoIdGen.JDIALECTS_AUTOID);
			result = jdbc.nQueryForObject(sql);
			DialectException.assureNotNull(result,
					"Null value found when fetch Auto-Generated ID from sequence '" + AutoIdGen.JDIALECTS_AUTOID + "'");
		} else {
			String sql = "update " + AutoIdGen.JDIALECTS_AUTOID + " set " + AutoIdGen.NEXT_VAL + "=("
					+ AutoIdGen.NEXT_VAL + "+1)";
			int updatedCount = jdbc.nUpdate(sql);
			if (updatedCount != 1)
				throw new DialectException(
						"When fetch Auto-Generated ID, can not read from \"" + AutoIdGen.JDIALECTS_AUTOID + "\" table");
			result = jdbc.nQueryForObject( "select " + AutoIdGen.NEXT_VAL + " from " + AutoIdGen.JDIALECTS_AUTOID);
			DialectException.assureNotNull(result,
					"Null value found when fetch Auto-Generated ID from table \"" + AutoIdGen.JDIALECTS_AUTOID + "\"");
		}
		return result;
	}

}
