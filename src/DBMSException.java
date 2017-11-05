

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
	
	public static String getMessage(int id, String msg) {
		switch (id) {
		case 0:
			return SYNTAX_ERROR;
		case 1:
			return DUPLICATE_COLUMN_DEF_ERROR;
		case 2:
			return DUPLICATE_PRIMARY_KEY_DEF_ERROR;
		case 3:
			return REFERENCE_TYPE_ERROR;
		case 4:
			return REFERENCE_NON_PRIMARY_KEY_ERROR;
		case 5:
			return REFERENCE_COLUMN_EXISTENCE_ERROR;
		case 6:
			return REFERENCE_TABLE_EXISTENCE_ERROR;
		case 7:
			return TABLE_EXISTENCE_ERROR;
		case 8:
			return SHOW_TABLES_NO_TABLE;
		case 9:
			return NO_SUCH_TABLE;
		case 10:
			return CHAR_LENGTH_ERROR;
		case 11:
			return "Create table has failed: '" + msg + "' does not exists in column definition";
		case 12:
			return "Drop table has failed: '" + msg +"' is referenced by other table";
		default:
			return "";	
		}
	}
	
	/*
	CreateTableSuccess(#tableName)	'[#tableName]' table is created
	
	NonExistingColumnDefError(#colName)	Create table has failed: '[#colName]' does not exists in column definition
	
	DropSuccess(#tableName)	'[#tableName]' table is dropped
	DropReferencedTableError(#tableName)	Drop table has failed: '[#tableName]' is referenced by other table
*/
}
