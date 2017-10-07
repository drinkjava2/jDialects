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
import com.github.drinkjava2.jdialects.annotation.GenerationType;

/**
 * AutoGenerator will depends database's id generator mechanism like MySql's
 * Identity, Oracle's Sequence...
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class AutoIdGenerator implements IdGenerator {
	private static final String JDIALECTS_AUTOID_NAME = "jdia_autoid";
	private static final String JDIALECTS_AUTOID_TABLE = "jdia_autoid_tab";
	private static final String JDIALECTS_AUTOID_SEQUENCE = "jdia_autoid_seq";

	public static final AutoIdGenerator INSTANCE = new AutoIdGenerator();

	private static final TableIdGenerator TABLEIDGENERATOR_INSTANCE = new TableIdGenerator(JDIALECTS_AUTOID_NAME,
			JDIALECTS_AUTOID_TABLE, "idcolumn", "valuecolumn", "next_val", 1, 50);

	private static final SequenceIdGenerator SEQUENCEIDGENERATOR_INSTANCE = new SequenceIdGenerator(
			JDIALECTS_AUTOID_NAME, JDIALECTS_AUTOID_SEQUENCE, 1, 1);

	@Override
	public GenerationType getGenerationType() {
		return GenerationType.AUTO;
	}

	@Override
	public String getIdGenName() {
		return JDIALECTS_AUTOID_NAME;
	}

	@Override
	public IdGenerator newCopy() {
		return INSTANCE;
	};

	@Override
	public Object getNextID(NormalJdbcTool jdbc, Dialect dialect) {
		if (dialect.getDdlFeatures().supportBasicOrPooledSequence())
			return TABLEIDGENERATOR_INSTANCE.getNextID(jdbc, dialect);
		else
			return SEQUENCEIDGENERATOR_INSTANCE.getNextID(jdbc, dialect);
	}

	/**
	 * Return a real IdGenerator, can be a TableIdGenerator or
	 * SequenceIdGenerator determined by dialect
	 */
	public IdGenerator getRealIdgenerator(Dialect dialect) {
		if (dialect.getDdlFeatures().supportBasicOrPooledSequence())
			return TABLEIDGENERATOR_INSTANCE;
		else
			return SEQUENCEIDGENERATOR_INSTANCE;
	}
}
