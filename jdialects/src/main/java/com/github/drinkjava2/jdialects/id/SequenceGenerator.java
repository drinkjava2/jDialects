package com.github.drinkjava2.jdialects.id;

import com.github.drinkjava2.jdbpro.NormalJdbcTool;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.utils.StrUtils; 

/**
 * Define a Sequence type ID generator, supported by Oracle, postgreSQL, DB2
 * 
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */

public class SequenceGenerator implements IdGenerator {

	String sequenceName = "";

	public SequenceGenerator() {
		// Default constructor
	}

	public SequenceGenerator(String sequenceName) {
		this.sequenceName = sequenceName;
	}

	@Override
	public Object getNextID(NormalJdbcTool ctx, Dialect dialect) {
		DialectException.assureNotEmpty(sequenceName, "sequenceName can not be empty");
		String sequenctSQL = dialect.getDdlFeatures().getSequenceNextValString();
		sequenctSQL = StrUtils.replace(sequenctSQL, "_SEQNAME", sequenceName);
		return ctx.nQueryForObject(sequenctSQL);
	}

	// Getter & Setters below

	public String getSequenceName() {
		return sequenceName;
	}

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}

}
