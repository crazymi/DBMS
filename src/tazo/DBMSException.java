package tazo;

public class DBMSException {
	
	public static final String SYNTAX_ERROR = "Syntax error";
	public static final String DUPLICATE_COLUMN_DEF_ERROR = "Create table has failed: column definition is duplicated";
	public static final String DUPLICATE_PRIMARY_KEY_DEF_ERROR = "Create table has failed: primary key definition is duplicated";
	public static final String REFERENCE_TYPE_ERROR = "Create table has failed: foreign key references wrong type";
	public static final String REFERENCE_NON_PRIMARY_KEY_ERROR = "Create table has failed: foreign key references non primary key column";
	public static final String REFERENCE_COLUMN_EXISTENCE_ERROR = "Create table has failed: foreign key references non existing column";
	public static final String REFERENCE_TABLE_EXISTENCE_ERROR = "Create table has failed: foreign key references non existing table";
	public static final String TABLE_EXISTENCE_ERROR="Create table has failed: table with the same name already exists";
	public static final String SHOW_TABLES_NO_TABLE = "There is no table";
	public static final String NO_SUCH_TABLE = "No such table";
	public static final String CHAR_LENGTH_ERROR = "Char length should be over 0";
	
	/*
	CreateTableSuccess(#tableName)	'[#tableName]' table is created
	
	NonExistingColumnDefError(#colName)	Create table has failed: '[#colName]' does not exists in column definition
	
	DropSuccess(#tableName)	'[#tableName]' table is dropped
	DropReferencedTableError(#tableName)	Drop table has failed: '[#tableName]' is referenced by other table
*/
}
