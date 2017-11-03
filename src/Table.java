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
	
	public void addColumn(Column col) throws ParseException {
		for(Column c : columnList) {
			if(c.name.equals(col.name)) {
				// already exist column name
				System.out.println(DBMSException.DUPLICATE_COLUMN_DEF_ERROR);
				throw new ParseException();
			}
		}
		
		if(col.type == 2 && col.char_length < 1) {
			System.out.println(DBMSException.CHAR_LENGTH_ERROR);
			throw new ParseException();
		}
		
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
	
	public void addPrimaryKeyConstraint(ArrayList<String> list) throws ParseException {
		// primary key is already defined
		if(has_primary_key_constraint) {
			System.out.println(DBMSException.DUPLICATE_PRIMARY_KEY_DEF_ERROR);
			throw new ParseException();
		}
		
		for(Column c : columnList) {
			boolean flag = false;
			for(String n : list) {
				if(n.equals(c.name)) {
					flag = true;
					c.is_not_null = true;
				}
			}
			if(!flag) {
				// not existing column def
				throw new ParseException();
			}
		}
		
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
