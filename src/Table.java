import java.util.ArrayList;

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
	ArrayList<String> foreign_list = new ArrayList<>(); // columnNameList
	
	public int referenced_count = 0;
	
	class Foreign{
		String fname;
		int fsize;
		
		Foreign(String fname, int fsize) {this.fname = fname; this.fsize = fsize;}
	}
	ArrayList<Foreign> update_list = new ArrayList<>();

	// note that, DBMSController should be transient
	// means serializer will ignore this.
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
	
	// there are no DROP for column...
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
		
		// list 'dup' is used for check duplicate primary key definition 
		ArrayList<String> dup = new ArrayList<>();
		for(String n : list) {
			if(dup.contains(n)) { // if already defined as primary
				System.out.println(DBMSException.getMessage(1, null));
				throw new ParseException("hoho");
			}
			
			boolean flag = false;
			// primary key should be NOT NULL although not explicitly defined
			for(Column c : columnList) {
				if(n.equals(c.name)) {
					flag = true;
					c.is_not_null = true;
					c.is_primary = true;
				}
			}
			
			// !flag means there are no column named 'n'
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
	
	public Column getColumn(String n) {
		for(Column c: columnList) {
			if(n.equals(c.name)) {
				return c;
			}
		}
		return null;
	}
	
	// flist : list of referenc'ing' columns
	// fname : name of referenc'ed' table
	// rlist : list of referenc'ed' columns
	public void addReferentialConstraint(ArrayList<String> flist,
			String fname, ArrayList<String> rlist) throws ParseException {
		Table ref_table = null;
		
		if(!ctrl.isTableExist(fname)) { // if refer table not exists
			System.out.println(DBMSException.getMessage(6, null));
			throw new ParseException("hoho");
		}
		
		// self reference?
		if(fname.equals(this.name)) {
			System.out.println(DBMSException.getMessage(13, null));
			throw new ParseException("hoho");
		}
		ref_table = ctrl.getTableByName(fname);
		
		// check if referencing column is in definition
		for(String f : flist) {
			if(!this.isColumnExists(f)) {
				System.out.println(DBMSException.getMessage(11, f));
				throw new ParseException("hoho");
			}
		}
		
		// check if referenced column is in definition and primary
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
		
		if(flist.size() != rlist.size()) {
			System.out.println(DBMSException.getMessage(3, null));
			throw new ParseException("hoho");
		}
		
		for(int i=0;i<flist.size();i++) {
			String f = flist.get(i);
			String r = rlist.get(i);
			Column fc = this.getColumn(f);
			Column rc = ref_table.getColumn(r);
			if(fc == null || rc == null) {
				System.out.println("DBMS Error");
				throw new ParseException("hoho");
			}
//			assert(fc != null && rc != null);
			
			if(fc.type != rc.type) {
				System.out.println(DBMSException.getMessage(3, null));
				throw new ParseException("hoho");
			} else if (fc.type == 2 && (fc.char_length != rc.char_length)) {
				System.out.println(DBMSException.getMessage(3, null));
				throw new ParseException("hoho");
			} else {
				// do nothing
			}
		}
		
		// check if column is already defined as foreign key
		// note that, this part is not defined in spec
		// but should be care
		for(String f : flist) {
			if(!foreign_list.contains(f)) {
				foreign_list.add(f);
			} else {
				System.out.println(DBMSException.getMessage(14, null));
				throw new ParseException("hoho");
				// same foreign key can't references multiple columns
			}				
		}
		
		for(int i=0;i<flist.size();i++) {
			String f = flist.get(i);
			String r = rlist.get(i);
			this.getColumn(f).addForeign(fname, r);
		}
		
		// refUpdate update the table's referenced count
		// which used as condition when DROP TABLE sql query
		update_list.add(new Foreign(fname, flist.size()));
		
		/* it is important to do 'lazy update', so below line should be replaced with above one
		 * and later we should call update() function.
		 */
		//ctrl.refUpdate(fname, flist.size());
	}
	
	public void update() {
		for(Foreign f : update_list) {
			ctrl.refUpdate(f.fname, f.fsize);
		}
	}
	
	// print table information 
	// used for DESC sql query
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

	public Column getColumnByIndex(int idx)
	{
		return columnList.get(idx);
	}

	// find current table's column by name, if not exists return null
	public Column getColumnByName(String n)
	{
		for (Column c : columnList)
		{
			if(c.name == n)
				return c;
		}
		return null;
	}
		
	// clist : column name list
	// vlist : value list
	public void insertInto(ArrayList<String> clist, ArrayList<String> vlist) throws ParseException
	{
		int lsize = vlist.size();
		int valtype = 0;
		String strcol, strval;
		// newclist, newvlist is filled & sorted of clist,vlist
		// order : following table's columnlist
		// filled : if no value is given, instead put null
		ArrayList<String> newclist = new ArrayList<String>();
		ArrayList<String> newvlist = new ArrayList<String>();
		Column colfind = null;
		
		// if clist exists, make it as column order.
		if(clist != null)
		{
			// case 16, InsertTypeMismatchError
			// if number of given column and value is not equal
			if(lsize != clist.size())
			{
				System.out.println(DBMSException.getMessage(16, null));
				throw new ParseException("hohoho");
			}
			
			// sort clist -> newclist (sort with saved order and fill null with non value)
			int idx = 0;
			for(Column c : columnList)
			{
				newclist.add(c.name);
				// if given, add given
				if((idx = clist.indexOf(c.name)) != -1)
				{
					newvlist.add(vlist.get(idx));
				} else {	// no given, add null
					newvlist.add(null);
				}
			}
		}
		else 
		{
			for(Column c : columnList)
			{
				newclist.add(c.name);
			}
			newvlist = vlist;
		}
		
		if(newclist.size() != newvlist.size())
		{
			// case 16, InsertTypeMismatchError
			System.out.println(DBMSException.getMessage(16, null));
			throw new ParseException("hohoho");
		}
		
		// check basic integrity
		lsize = newvlist.size();
		for(int i=0;i<lsize;i++)
		{
			strcol = newclist.get(i);
			strval = newvlist.get(i);

			// case 13, InsertColumnExistenceError
			if(!this.isColumnExists(strcol) ||
					(colfind = this.getColumnByName(strcol)) == null)
			{
				System.out.println(DBMSException.getMessage(15, strcol));
				throw new ParseException("hohoho");
			}
			
			if(strval == null)
			{
				// case 15, InsertColumnNonNullableError
				if(colfind.is_not_null || colfind.is_primary) {
					System.out.println(DBMSException.getMessage(17, strcol));
					throw new ParseException("hohoho");
				}
				valtype = colfind.type;
			} else {
				// 1 int, 2 char, 3 date
				// get first char (type flag)
				valtype = Integer.parseInt(strval.substring(0, 1));
				// case 14, InsertTypeMismatchError
				if(colfind.type != valtype)
				{
					System.out.println(DBMSException.getMessage(16, null));
					throw new ParseException("hohoho");
				}
				
				// no need to keep + sign,, 1+???
				if(valtype == 1 && strval.contains("+"))
					strval = "1" + strval.substring(2, strval.length());
				
				// truncate char string
				if(valtype == 2 && strval.length() > colfind.char_length)
					strval = strval.substring(0, colfind.char_length-1);
			}
		}
		
		ArrayList<ArrayList<String>> beforeRecord = ctrl.readRecords(this.name);
		// check primary constraint
		boolean primaryflag = false;
		ArrayList<Integer> primaryIndex = new ArrayList<Integer>();
		int iter = 0;
		for(Column c : columnList) {
			if(c.is_primary) primaryIndex.add(iter);
			iter++;
		}

		for(ArrayList<String> target : beforeRecord)
		{
			if(primaryIndex.size() == 0) continue;
			primaryflag = true;
			for(int i=0;i<primaryIndex.size();i++) {
				if(!target.get(i).equals(newvlist.get(i))) {
					primaryflag = false;
					continue;
				}
			}
			
			// if tuple is identical, 
			// case 18, InsertDuplicatePrimaryKeyError
			if(primaryflag)
			{
				System.out.println(DBMSException.getMessage(18, null));
				throw new ParseException("hohoho");
			}
		}
		
		// check foreign constraint
		iter = -1;
		for(Column c : columnList)
		{
			iter++;
			if(!c.is_foreign) continue;
			
			ArrayList<ArrayList<String>> targetRecord = ctrl.readRecords(c.reference_table);
			Table targetTable = ctrl.getTableByName(c.reference_table);
			int targetColumnIdx = -1;
			for(Column cc : targetTable.columnList)
			{
				targetColumnIdx++;
				if(cc.name.equals(c.reference_column))
				{
					break;
				}
			}
			boolean foreignflag = false;
			
			for(ArrayList<String> target : targetRecord)
			{
				// compare reference(target) table's column value with 
				// newvlist's value (iter)
				if(target.get(targetColumnIdx).equals(newvlist.get(iter))) {
					foreignflag = true;
					break; // no need to lookup rest records
				}
			}
			
			// if there is no matching value, which means ignore constraint
			// case 19, InsertReferentialIntegrityError
			if(!foreignflag)
			{
				System.out.println(DBMSException.getMessage(19, null));
				throw new ParseException("hohoho");
			}
		}
		
		// if all pass for constraint check, INSERT
		ctrl.insertRecord(this.name, newvlist);
		// case 20, InsertResult
		System.out.println(DBMSException.getMessage(20, null));
	}


	public void delete(WhereController.MyWhereClause wc) throws ParseException
	{
		int cnt = 0;
		// if no where clause, drop all records
		if(wc == null) cnt = ctrl.deleteAll(this);
		else cnt = ctrl.deleteRecord(wc,  this);
		
		// after deletion
		System.out.println(DBMSException.getMessage(25, String.valueOf(cnt)));
	}
		
}
