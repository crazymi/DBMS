package tazo;

import java.util.ArrayList;

public class Column
{		
	/* columnDefinition */
	String name = null; // columnName : LEGAL_IDENTIFIER
	public int type; // dataType : 0 INT 1 CHAR 2 DATE
	public int char_length = 0;
	public boolean is_not_null = false;
	
	// Constructor
	public Column(String name) {
		this.name = name;
	}
	
}
