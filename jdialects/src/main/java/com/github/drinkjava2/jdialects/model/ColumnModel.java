/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

import java.util.Iterator;
import java.util.List;

import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.id.IdGenerator;
import com.github.drinkjava2.jdialects.utils.StrUtils;

/**
 * A ColumnModel definition represents a platform dependent column in a Database
 * Table, from 1.0.5 this class name changed from "Column" to "ColumnModel" to
 * avoid naming conflict to JPA's "@Column" annotation
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class ColumnModel {
	private String columnName;// no need explain
	private TableModel tableModel; // not important, only used for singleXxx()
									// shortcut methods
	private Type columnType;// See com.github.drinkjava2.jdialects.Type
	private Boolean pkey = false; // if is primary key
	private Boolean nullable = true; // if nullable

	private String check; // DDL check string
	private String defaultValue; // DDL default value

	private IdGenerator idGenerator = null;

	private Boolean identity = false; // if use native identity type

	/** bind column to a sequence */
	private String sequence;

	/** bind column to a tableGenerator */
	private String tableGenerator;

	/**
	 * Bind column to Auto Id generator, can be SequenceGen or TableGen,
	 * determined by jDialects
	 */
	private Boolean autoGenerator = false;

	/** Optional, put an extra tail String at end of column definition DDL */
	private String tail;

	/** Comment of this column */
	private String comment;

	/** length, precision, scale all share use lengths array */
	private Integer[] lengths = new Integer[] {};

	// =====Below fields are only used by JPA and ORM tools==========
	/** Map to a Java POJO field, for JPA or ORM tool use only */
	private String pojoField;

	/** Value of Java POJO field, for JPA or ORM tool use only */
	private Object value;

	/** The column length, for JPA or ORM tool use only */
	private Integer length = 255;

	/** The numeric precision, for JPA or ORM tool use only */
	private Integer precision = 0;

	/** The numeric scale, for JPA or ORM tool use only */
	private Integer scale = 0;

	/** If insert-able or not, for JPA or ORM tool use only */
	private Boolean insertable = true;

	/** If update-able or not, for JPA or ORM tool use only */
	private Boolean updatable = true;

	/** If this is a Transient type, for JPA or ORM tool use only */
	private Boolean transientable = false;

	public ColumnModel(String columnName) {
		if (StrUtils.isEmpty(columnName))
			DialectException.throwEX("columnName is not allowed empty");
		this.columnName = columnName;
	}

	/** Add a not null DDL piece if support */
	public ColumnModel notNull() {
		this.nullable = false;
		return this;
	}

	/** Add a column check DDL piece if support */
	public ColumnModel check(String check) {
		this.check = check;
		return this;
	}

	public ColumnModel newCopy() {
		ColumnModel col = new ColumnModel(columnName);
		col.columnType = columnType;
		col.pkey = pkey;
		col.nullable = nullable;
		col.identity = identity;
		col.check = check;
		col.defaultValue = defaultValue;
		col.sequence = sequence;
		col.tableGenerator = tableGenerator;
		col.tail = tail;
		col.autoGenerator = autoGenerator;
		col.comment = comment;
		col.lengths = lengths;
		col.pojoField = pojoField;
		col.length = length;
		col.precision = precision;
		col.scale = scale;
		col.insertable = insertable;
		col.updatable = updatable;
		if (idGenerator != null)
			col.idGenerator = idGenerator.newCopy();
		return col;
	}

	/**
	 * A shortcut method to add a index for single column, for multiple columns
	 * index please use tableModel.index() method
	 */
	public ColumnModel singleIndex(String indexName) {
		DialectException.assureNotNull(this.tableModel,
				"index() shortcut method used only as tableModel.column().index() format");
		DialectException.assureNotEmpty(indexName, "indexName can not be empty");
		this.tableModel.index(indexName).columns(this.getColumnName());
		return this;
	}

	/**
	 * A shortcut method to add a index for single column, for multiple columns
	 * index please use tableModel.index() method
	 */
	public ColumnModel singleIndex() {
		DialectException.assureNotNull(this.tableModel,
				"index() shortcut method used only as tableModel.column().index() format");
		this.tableModel.index().columns(this.getColumnName());
		return this;
	}

	/**
	 * A shortcut method to add a unique constraint for single column, for
	 * multiple columns index please use tableModel.unique() method
	 */
	public ColumnModel singleUnique(String uniqueName) {
		DialectException.assureNotNull(this.tableModel,
				"unique() shortcut method used only as tableModel.column().index() format");
		DialectException.assureNotEmpty(uniqueName, "indexName can not be empty");
		this.tableModel.unique(uniqueName).columns(this.getColumnName());
		return this;
	}

	/**
	 * A shortcut method to add a unique constraint for single column, for
	 * multiple columns index please use tableModel.unique() method
	 */
	public ColumnModel singleUnique() {
		DialectException.assureNotNull(this.tableModel,
				"unique() shortcut method used only as tableModel.column().index() format");
		this.tableModel.unique().columns(this.getColumnName());
		return this;
	}

	/**
	 * Mark a field will use database's native identity type. Note:Note all
	 * databases support identity
	 */
	public ColumnModel identity() {
		this.identity = true;
		return this;
	}

	/** Default value for column's definition DDL */
	public ColumnModel defaultValue(String value) {
		this.defaultValue = value;
		return this;
	}

	/** Add comments at end of column definition DDL */
	public ColumnModel comment(String comment) {
		this.comment = comment;
		return this;
	}

	/** Mark primary key, if more than one will build compound Primary key */
	public ColumnModel pkey() {
		this.pkey = true;
		return this;
	}

	/**
	 * A shortcut method to add Foreign constraint for single column, for
	 * multiple columns please use tableModel.fkey() method instead
	 */
	public FKeyModel singleFKey(String... refTableAndColumns) {
		DialectException.assureNotNull(this.tableModel,
				"singleFKey() method can only be called when columnModel belong to a TableModel");
		if (refTableAndColumns == null || refTableAndColumns.length > 2)
			throw new DialectException(
					"singleFKey() first parameter should be table name, second parameter(optional) should be column name");
		return this.tableModel.fkey().columns(this.columnName).refs(refTableAndColumns);
	}

	/** The value of this column will be generated by an idGenerator */
	public ColumnModel idGenerator(String idGeneratorName) {
		DialectException.assureNotNull(this.tableModel,
				"idGenerator() method can only be called when columnModel belong to a TableModel");
		this.idGenerator = this.tableModel.getIdGenerator(idGeneratorName);
		return this;
	}
  
	/**
	 * bind column to a global Auto Id generator, can be SequenceGen(if support)
	 * or a Table to store maximum current ID, determined by jDialects, to get
	 * next auto generated ID value, need run dialect.getNextAutoID(connection)
	 * method
	 */
	public ColumnModel autoID() {
		this.autoGenerator = true;
		return this;
	}

	/**
	 * Put an extra tail String manually at the end of column definition DDL
	 */
	public ColumnModel tail(String tail) {
		this.tail = tail;
		return this;
	}

	/**
	 * Mark this column map to a Java POJO's field, if exist other columns map
	 * to this field, delete other columns. This method only designed for ORM
	 * tool
	 */
	public ColumnModel pojoField(String pojoField) {
		DialectException.assureNotEmpty(pojoField, "pojoField can not be empty");
		this.pojoField = pojoField;
		if (this.tableModel != null) {
			List<ColumnModel> oldColumns = this.tableModel.getColumns();
			Iterator<ColumnModel> columnIter = oldColumns.iterator();
			while (columnIter.hasNext()) {
				ColumnModel column = columnIter.next();
				if (pojoField.equals(column.getPojoField()) && !this.getColumnName().equals(column.getColumnName()))
					columnIter.remove();
			}
		}
		return this;
	}

	/** Mark a field insertable=true, only for JPA or ORM tool use */
	public ColumnModel insertable(Boolean insertable) {
		this.insertable = insertable;
		return this;
	}

	/** Mark a field updatable=true, only for JPA or ORM tool use */
	public ColumnModel updatable(Boolean updatable) {
		this.updatable = updatable;
		return this;
	}

	//@formatter:off shut off eclipse's formatter
	public ColumnModel LONG() {this.columnType=Type.BIGINT;return this;} 
	public ColumnModel BOOLEAN() {this.columnType=Type.BOOLEAN;return this;} 
	public ColumnModel DOUBLE() {this.columnType=Type.DOUBLE;return this;} 
	public ColumnModel FLOAT(Integer... lengths) {this.columnType=Type.FLOAT;this.lengths=lengths;return this;} 
	public ColumnModel INTEGER() {this.columnType=Type.INTEGER;return this;} 
	public ColumnModel SHORT() {this.columnType=Type.SMALLINT;return this;} 
	public ColumnModel BIGDECIMAL(Integer precision, Integer scale) {this.columnType=Type.NUMERIC; this.lengths= new Integer[]{precision,scale}; return this;} 
	public ColumnModel STRING(Integer length) {this.columnType=Type.VARCHAR;this.lengths=new Integer[]{length}; return this;} 
	
	public ColumnModel DATE() {this.columnType=Type.DATE;return this;} 
	public ColumnModel TIME() {this.columnType=Type.TIME;return this;} 
	public ColumnModel TIMESTAMP() {this.columnType=Type.TIMESTAMP;return this;} 
	public ColumnModel BIGINT() {this.columnType=Type.BIGINT;return this;} 
	public ColumnModel BINARY(Integer... lengths) {this.columnType=Type.BINARY;this.lengths=lengths; return this;} 
	public ColumnModel BIT() {this.columnType=Type.BIT;return this;} 
	public ColumnModel BLOB(Integer... lengths) {this.columnType=Type.BLOB;this.lengths=lengths;return this;} 
	public ColumnModel CHAR(Integer... lengths) {this.columnType=Type.CHAR;this.lengths=lengths;return this;} 
	public ColumnModel CLOB(Integer... lengths) {this.columnType=Type.CLOB;this.lengths=lengths;return this;} 
	public ColumnModel DECIMAL(Integer... lengths) {this.columnType=Type.DECIMAL;this.lengths=lengths;return this;} 
	public ColumnModel JAVA_OBJECT() {this.columnType=Type.JAVA_OBJECT;return this;} 
	public ColumnModel LONGNVARCHAR(Integer length) {this.columnType=Type.LONGNVARCHAR;this.lengths=new Integer[]{length};return this;} 
	public ColumnModel LONGVARBINARY(Integer... lengths) {this.columnType=Type.LONGVARBINARY;this.lengths=lengths;return this;} 
	public ColumnModel LONGVARCHAR(Integer... lengths) {this.columnType=Type.LONGVARCHAR;this.lengths=lengths;return this;} 
	public ColumnModel NCHAR(Integer length) {this.columnType=Type.NCHAR;this.lengths=new Integer[]{length};return this;} 
	public ColumnModel NCLOB() {this.columnType=Type.NCLOB;return this;} 
	public ColumnModel NUMERIC(Integer... lengths) {this.columnType=Type.NUMERIC;this.lengths=lengths;return this;} 
	public ColumnModel NVARCHAR(Integer length) {this.columnType=Type.NVARCHAR;   this.lengths=new Integer[]{length};return this;} 
	public ColumnModel OTHER(Integer... lengths) {this.columnType=Type.OTHER;this.lengths=lengths;return this;} 
	public ColumnModel REAL() {this.columnType=Type.REAL;return this;} 
	public ColumnModel SMALLINT() {this.columnType=Type.SMALLINT;return this;} 
	public ColumnModel TINYINT() {this.columnType=Type.TINYINT;return this;} 
	public ColumnModel VARBINARY(Integer... lengths) {this.columnType=Type.VARBINARY;this.lengths=lengths;return this;} 
	public ColumnModel VARCHAR(Integer length) {this.columnType=Type.VARCHAR;this.lengths=new Integer[]{length};return this;}
	//@formatter:on

	// getter & setters==============
	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Type getColumnType() {
		return columnType;
	}

	public void setColumnType(Type columnType) {
		this.columnType = columnType;
	}

	public Boolean getPkey() {
		return pkey;
	}

	public void setPkey(Boolean pkey) {
		this.pkey = pkey;
	}

	public Boolean getNullable() {
		return nullable;
	}

	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}

	public Boolean getIdentity() {
		return identity;
	}

	public void setIdentity(Boolean identity) {
		this.identity = identity;
	}

	public String getCheck() {
		return check;
	}

	public void setCheck(String check) {
		this.check = check;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String getTableGenerator() {
		return tableGenerator;
	}

	public void setTableGenerator(String tableGenerator) {
		this.tableGenerator = tableGenerator;
	}

	public String getTail() {
		return tail;
	}

	public void setTail(String tail) {
		this.tail = tail;
	}

	public Boolean getAutoGenerator() {
		return autoGenerator;
	}

	public void setAutoGenerator(Boolean autoGenerator) {
		this.autoGenerator = autoGenerator;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Integer[] getLengths() {
		return lengths;
	}

	public void setLengths(Integer[] lengths) {
		this.lengths = lengths;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getPrecision() {
		return precision;
	}

	public void setPrecision(Integer precision) {
		this.precision = precision;
	}

	public Integer getScale() {
		return scale;
	}

	public void setScale(Integer scale) {
		this.scale = scale;
	}

	public Boolean getInsertable() {
		return insertable;
	}

	public void setInsertable(Boolean insertable) {
		this.insertable = insertable;
	}

	public Boolean getUpdatable() {
		return updatable;
	}

	public void setUpdatable(Boolean updatable) {
		this.updatable = updatable;
	}

	public String getPojoField() {
		return pojoField;
	}

	public void setPojoField(String pojoField) {
		this.pojoField = pojoField;
	}

	public TableModel getTableModel() {
		return tableModel;
	}

	public void setTableModel(TableModel tableModel) {
		this.tableModel = tableModel;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Boolean getTransientable() {
		return transientable;
	}

	public void setTransientable(Boolean transientable) {
		this.transientable = transientable;
	}

	public IdGenerator getIdGenerator() {
		return idGenerator;
	}

	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

}
