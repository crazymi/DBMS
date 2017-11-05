import java.util.ArrayList;
import java.util.HashMap;

import com.sleepycat.je.dbi.GetMode;

public class Table {
		
	String name = null; // tableName : LEGAL_IDENTIFIER
	ArrayList<Column> columnList = new ArrayList<>(); // tableElementList
	
	public ArrayList<Column> getColumnList() {
		return columnList;
	}

	public void setColumnList(ArrayList<Column> columnList) {
		this.columnList = columnList;
	}

	public ArrayList<String> getPrimary_list() {
		return primary_list;
	}

	public void setPrimary_list(ArrayList<String> primary_list) {
		this.primary_list = primary_list;
	}

	/* tableConstraintDefinition */
	// primaryKeyConstraint
	boolean has_primary_key_constraint = false;
	ArrayList<String> primary_list = new ArrayList<>(); // columnNameList
	
	// referentialConstraint
	boolean has_reference_constraint = false;
	ArrayList<String> foreign_list = new ArrayList<>(); // columnNameList
	HashMap<String, ArrayList<String>> reference_list = new HashMap<>();
	
	public boolean is_referenced_table = false;
	
	transient DBMSController ctrl;
	
	public Table(String name, DBMSController ctrl) { this.name = name; this.ctrl = ctrl;}
	
	public void addColumn(Column col) throws ParseException {
		for(Column c : columnList) {
			if(c.name.equals(col.name)) {
				// already exist column name
				System.out.println(DBMSException.getMessage(1, null));
				throw new ParseException("hoho");
			}
		}
		
		if(col.type == 2 && col.char_length < 1) {
			System.out.println(DBMSException.getMessage(10, null));
			throw new ParseException("hoho");
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
			System.out.println(DBMSException.getMessage(2, null));
			throw new ParseException("hoho");
		}
		
		ArrayList<String> dup = new ArrayList<>();
		for(String n : list) {
			if(dup.contains(n)) { // duplicate primary key
				System.out.println(DBMSException.getMessage(1, null));
				throw new ParseException("hoho");
			}
			boolean flag = false;
			for(Column c : columnList) {
				if(n.equals(c.name)) {
					flag = true;
					c.is_not_null = true;
				}
			}
			if(!flag) {
				System.out.println(DBMSException.getMessage(11, n));
				throw new ParseException("hoho");
			}
			dup.add(n);
		}
		
		has_primary_key_constraint = true;
		primary_list = list;
	}
	
	// return if given name of column is primary key or not
	public boolean isPrimary(String n) {
		for(String p : primary_list) {
			if(n.equals(p)) {
				return true;
			}
		}
		return false;
	}

	// return if given name of column is foreign key or not
	public boolean isForeign(String n) {
		for(String p : foreign_list) {
			if(n.equals(p)) {
				return true;
			}
		}
		return false;
	}
		
	
	// return if given name of column is exists or not
	public boolean isColumnExists(String n) {
		for(Column c : columnList) {
			if(n.equals(c.name)) {
				return true;
			}
		}
		return false;
	}
	
	public void addReferentialConstraint(ArrayList<String> flist,
			String fname, ArrayList<String> rlist) throws ParseException {
		Table ref_table = null;
		ArrayList<Column> ref_col = null;
		ArrayList<String> ref_pri = null;
		
		if(ctrl.isTableExist(fname)) { // if refer table not exists
			System.out.println(DBMSException.getMessage(6, null));
			throw new ParseException("hoho");
		}
		
		if(fname.equals(this.name)) { // Foreign key는 자신과 같은 테이블에 있는 컬럼을 참조할 수 없다.
			// TODO custom exception msg
			throw new ParseException("hoho");
		}
		ref_table = ctrl.getTableByName(fname);
		
		
		for(String f : flist) {
			if(!this.isColumnExists(f)) {
				System.out.println(DBMSException.getMessage(11, f));
				throw new ParseException("hoho");
			}
		}
		
		for(String r : rlist) {
			if(!ref_table.isColumnExists(r)) {
				System.out.println(DBMSException.getMessage(5, null));
				throw new ParseException("hoho");
			}
			if(!ref_table.isPrimary(r)) {
				System.out.println(DBMSException.getMessage(4, null));
				throw new ParseException("hoho");
			}
		}
		
		has_reference_constraint = true;
		for(String f : flist) {
			if(!foreign_list.contains(f))
				foreign_list.add(f);
		}
		/*
		foregin_list = flist;
		foreign_name = fname;
		reference_list = rlist;
		*/
		ref_table.is_referenced_table = true;
	}
	
	public void introPlease() {
		System.out.println("-------------------------------------------------");
		System.out.println("table_name [" + this.name + "]");
		System.out.println("column_name\ttype\tnull\tkey");
		for(Column c : columnList) {
			System.out.print(c.name + "\t");
			
			if(c.type == 1) {
				System.out.print("int\t");
			} else if (c.type == 3) {
				System.out.print("date\t");
			} else if (c.type == 2){
				System.out.print("char(" + c.char_length + ")\t");
			}
			
			if(c.is_not_null) {
				System.out.print("N\t");
			} else {
				System.out.print("Y\t");
			}
			
			if(this.isPrimary(c.name) && this.isForeign(c.name)) {
				System.out.print("PRI/FOR\n");
			} else if(this.isPrimary(c.name)) {
				System.out.print("PRI\n");
			} else if(this.isForeign(c.name)) {
				System.out.print("FOR\n");
			} else {
				System.out.print("\n");
			}
		}
	}
}
