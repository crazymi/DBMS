package tazo;

import java.util.ArrayList;

public class Table {
		
	String name = null; // tableName : LEGAL_IDENTIFIER
	ArrayList<Column> columnList = new ArrayList<>(); // tableElementList
	
	/* tableConstraintDefinition */
	// primaryKeyConstraint
	boolean has_primary_key_constraint = false;
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
	
	public void addPrimaryKeyConstraint(ArrayList<String> list) {
		has_primary_key_constraint = true;
		primary_list = list;
	}
	
	public void addReferentialConstraint(ArrayList<String> flist, String fname, ArrayList<String> rlist) {
		has_reference_constraint = true;
		foregin_list = flist;
		foreign_name = fname;
		reference_list = rlist;		
	}
}
