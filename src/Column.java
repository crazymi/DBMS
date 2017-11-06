public class Column
{		
	/* columnDefinition */
	String name = null; // columnName : LEGAL_IDENTIFIER
	public int type; // dataType : (INT 1) (CHAR 2) (DATE 3)
	public int char_length = 0;
	public boolean is_not_null = false;
	
	boolean is_primary= false;
	boolean is_foreign = false;
	
	String reference_table = null;
	String reference_column = null;
	
	// Constructor
	public Column(String name) {
		this.name = name;
	}
	
	public void addForeign(String r_table, String r_column) {
		//assert(!is_foreign);
		
		is_foreign = true;
		this.reference_table = r_table;
		this.reference_column = r_column; 
	}
	
}
