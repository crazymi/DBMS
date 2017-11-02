package tazo;

import java.util.ArrayList;

public class Table {
		
	String name = null; // tableName : LEGAL_IDENTIFIER
	ArrayList<Column> columnList = new ArrayList<>(); // tableElementList
	
	/* tableConstraintDefinition */
	// primaryKeyConstraint
	public boolean has_primary_key_constraint = false;
	ArrayList<String> primary_list = new ArrayList<>(); // columnNameList
	
	// referentialConstraint
	boolean has_reference_constraint = false;
	ArrayList<String> foregin_list = new ArrayList<>(); // columnNameList
	String foreign_name = null; // tableName
	ArrayList<String> reference_list = new ArrayList<>(); // columnNameList
	
	public Table(String name) { this.name = name; }
	
	public void addColumn(Column col) {
		columnList.add(col);
	}
	
	public void dropColumn(String name) {
		for(Column col : columnList) {
			if(name.equals(col.name)) {
				columnList.remove(col);
				return;
			}
		}
		
		// fail to find
	}
	
	public void addPrimaryKeyConstraint() {
		
	}
}
