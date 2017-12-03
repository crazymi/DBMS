

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
	public static final String SELF_REFERENCE_ERROR = "Create table has failed: can't reference self";
	public static final String DUPLICATE_FOREIGN_KEY_DEF_ERROR = "Create table has failed: same foreign key can't references multiple columns";
	
	public static final String INSERT_TYPE_MISMATCH_ERROR = "Insertion has failed: Types are not matched";
	public static final String INSERT_DUPLICATE_PRIMARY_KEY_ERROR = "Insertion has failed: Primary key duplication";
	public static final String INSERT_REFERENTIAL_INTEGRITY_ERROR = "Insertion has failed: Referential integrity violation";
	
	public static final String INSERT_RESULT = "The row is inserted";
	
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
		case 13:
			return SELF_REFERENCE_ERROR;
		case 14:
			return DUPLICATE_FOREIGN_KEY_DEF_ERROR;
		/* below is added at project 1-3 */
		case 15:
			return "Insertion has failed: '" + msg + "' does not exist";
		case 16:
			return INSERT_TYPE_MISMATCH_ERROR;
		case 17: // InsertColumnNonNullableError
			return "Insertion has failed: '" + msg + "' is not nullable";
		case 18:
			return INSERT_DUPLICATE_PRIMARY_KEY_ERROR;
		case 19:
			return INSERT_REFERENTIAL_INTEGRITY_ERROR;
		case 20:
			return INSERT_RESULT;
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
