import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class DBMSController {
	// Environment & Database define
	Environment myDbEnvironment = null;
	Database mySchema = null;
	Database myRecord = null;
    
    /* OPENING DB */
	public DBMSController(){
		File envDir = new File("db");

		// if the directory(/db) does not exist, create
		if (!envDir.exists()) {
		    try{
		    	envDir.mkdir();
		    } 
		    catch(SecurityException se){
		        System.out.println("Fail to initialize DB file");
		    }
		}
		
	    // Open Database Environment or if not, create one.
	    EnvironmentConfig envConfig = new EnvironmentConfig();
	    envConfig.setAllowCreate(true);
	    myDbEnvironment = new Environment(new File("db/"), envConfig);

	    // Open Database or if not, create one.
	    DatabaseConfig dbConfig = new DatabaseConfig();
	    dbConfig.setAllowCreate(true);
	    dbConfig.setSortedDuplicates(true);
	    mySchema = myDbEnvironment.openDatabase(null, "schema", dbConfig);
	    
    	// Open Database or if not, create one.
	    myRecord = myDbEnvironment.openDatabase(null, "record", dbConfig);
	}
	
	/* CLOSING DB */
	public void close() {
	    if (mySchema != null) mySchema.close();
	    if (myRecord != null) myRecord.close();
	    if (myDbEnvironment != null) myDbEnvironment.close();
	}
	
	public boolean isTableExist(String n) {
		boolean flag = false;
		Cursor cursor = null;
	    DatabaseEntry key;
	    DatabaseEntry data;

	    try {
	      cursor = mySchema.openCursor(null, null);
	      key = new DatabaseEntry(n.getBytes("UTF-8"));
	      data = new DatabaseEntry();
	      if(cursor.getSearchKey(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
	    	  flag = true;
	      }
	    } catch (DatabaseException de) {
	    	de.printStackTrace();
	    } catch (UnsupportedEncodingException e) {
	    	e.printStackTrace();
	    } finally {
			if(cursor != null) cursor.close();
		}
	    
	    return flag;
	}
	
	public void createTable(Table t) throws ParseException {
		Cursor cursor = null;
	    DatabaseEntry key;
	    DatabaseEntry data;

	    try {
	    	cursor = mySchema.openCursor(null, null);
		    key = new DatabaseEntry(t.name.getBytes("UTF-8"));
		    data = new DatabaseEntry();
		      
	    	if(isTableExist(t.name)) {
	    		System.out.println(DBMSException.getMessage(7, null));
	    		throw new ParseException("hoho");
	    	} else {
	    		String t2json = new Gson().toJson(t);
	    		// keep date as JSON format
	    		data = new DatabaseEntry(t2json.getBytes("UTF-8"));
		    	cursor.put(key, data);
	    	}
	    } catch (DatabaseException de) {
	    } catch (UnsupportedEncodingException e) {
	      e.printStackTrace();
	    } finally {
			if(cursor != null) cursor.close();
		}
	    
	    // finally we update reference count
	    try {
	    	t.update();
	    } catch (Exception e) {
	    	System.out.println("Unexpected error occured while read/write db");
	    	// e.printStackTrace();
	    }
	    
	    System.out.println("'" + t.name + "'" + " table is created");
	}
	
	public Table getTableByName(String n){
		Cursor cursor = null;
	    DatabaseEntry key;
	    DatabaseEntry data;
	    Table result = null;
	    
	    try {
	    	cursor = mySchema.openCursor(null, null);
	    	key = new DatabaseEntry(n.getBytes("UTF-8"));
	    	data = new DatabaseEntry();
	    	if(cursor.getSearchKey(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
	    		String dataString = new String(data.getData(), "UTF-8");
	    		result = new Gson().fromJson(dataString, Table.class);
	    		result.ctrl = this;
	    	} else { // == NOTFOUND
	    		result = null;
	    	}
	    } catch (DatabaseException de) {
	    	de.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if(cursor != null) cursor.close();
	    
	    return result;
	}
	
	public void dropTable(String n) throws ParseException {
		Table target = null;
		Cursor cursor = null;
	    DatabaseEntry key;
	    DatabaseEntry data;

	    try {
	      cursor = mySchema.openCursor(null, null);
	      key = new DatabaseEntry(n.getBytes("UTF-8"));
	      data = new DatabaseEntry();
	      if(cursor.getSearchKey(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
	    	  String dataString = new String(data.getData(), "UTF-8");
	    	  target = new Gson().fromJson(dataString, Table.class);
	    	  // can't drop referenced table
	    	  if(target.referenced_count > 0){
	    		  System.out.println(DBMSException.getMessage(12, target.name));
	    		  throw new ParseException("hoho");
	    	  }
	    	  for(Column c : target.columnList) {
	    		  if(c.is_foreign) {
	    			  // decrease refernced count
	    			  refUpdate(c.reference_table, -1);
	    		  }
	    	  }
	    	  cursor.delete();
	      } else { // == NOTFOUND
	    	  System.out.println(DBMSException.getMessage(9, null));
	    	  throw new ParseException("hoho");
	      }
	    } catch (DatabaseException de) {
	    	de.printStackTrace();
	    } catch (UnsupportedEncodingException e) {
	    	e.printStackTrace();
	    } finally {
			if(cursor != null) cursor.close();
		}
	    
	    System.out.println("'" + n + "'" + " table is dropped");
	}
	
	public void showTables() throws ParseException {
		Cursor cursor = mySchema.openCursor(null, null); 
	    DatabaseEntry foundKey = new DatabaseEntry();
	    DatabaseEntry foundData = new DatabaseEntry();

	    // if no tables
	    if(cursor.getFirst(foundKey, foundData, LockMode.DEFAULT) != OperationStatus.SUCCESS) {
	    	cursor.close();
	    	System.out.println(DBMSException.getMessage(8, null));
	    	throw new ParseException("hoho");
	    }
	    
	    System.out.println("----------------");
		do {
			try {
				String keyString = new String(foundKey.getData(), "UTF-8");
				System.out.println(keyString);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS);
		System.out.println("----------------");
		
		if (cursor != null) cursor.close();
	}
	
	public void descTable(String n) throws ParseException {
		Table tb = getTableByName(n);
		if(tb == null) {
			System.out.println(DBMSException.getMessage(9, null));
			throw new ParseException("hoho");
		}
		tb.introPlease();
	}
	
	// increase referenced count for 'diff'
	public void refUpdate(String n, int diff) {
		Cursor cursor = null;
	    DatabaseEntry key;
	    DatabaseEntry data;
	    Table result = null;
	    
	    try {
	    	cursor = mySchema.openCursor(null, null);
	    	key = new DatabaseEntry(n.getBytes("UTF-8"));
	    	data = new DatabaseEntry();
	    	if(cursor.getSearchKey(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
	    		String dataString = new String(data.getData(), "UTF-8");
	    		result = new Gson().fromJson(dataString, Table.class);
	    		result.referenced_count = result.referenced_count + diff;
	    		
	    		String t2json = new Gson().toJson(result);
	    		data = new DatabaseEntry(t2json.getBytes("UTF-8"));
	    		
	    		// TODO how to overwrite? putCurrent not works
	    		// -> just delete and put it again
	    		cursor.delete();
	    		cursor.put(key, data);
	    	} else { // == NOTFOUND
	    		result = null;
	    	}
	    } catch (DatabaseException de) {
	    	de.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			if(cursor != null) cursor.close();
		}
	}
	
	public String parseData2Disk(ArrayList<String> valueList)
	{
		StringBuilder sb = new StringBuilder("");
		for(String s : valueList)
		{
			// note that null should be represent as non-character,
			// for exampe null with int type value will be in represent as "1 " and it's going to match with regex
			// also value in valusList must start with type prefix(1 or 2 or 3)
			if(s == null) sb.append("null");
			else	sb.append(s);
			
			sb.append(" ");
		}
		return sb.toString();
	}
	
	public ArrayList<String> parseDisk2Data(String tvalue) throws IllegalStateException, IndexOutOfBoundsException
	{
		Pattern pattern;
		Matcher matcher;
		ArrayList<String> valueList = new ArrayList<>();
		String found;
		try {
			// this regex pattern is identical to , '1<INT> or 2<CHAR_STRING> or 3<DATE>' in token matching
			pattern = Pattern.compile("1[+,-]?[0-9]*\\s|2'[^']*+'\\s|3[[0-9]{4}-[0-9]{2}-[0-9]{2}]*\\s|(null\\s)");
			matcher = pattern.matcher(tvalue);
			while(matcher.find()) {
				found = matcher.group(0);
				// remove last char, because regex include \s too.
				if(!found.equals("null ")) valueList.add(found.substring(0, found.length() - 1));
				else valueList.add(null);
			}
		} catch (IllegalStateException e){
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return valueList;
	}
	
	public ArrayList<ArrayList<String>> readRecords(String tname)
	{
		Cursor cursor = null;
		DatabaseEntry key = null;
		DatabaseEntry data = null;
		String dataString = null;
		ArrayList<ArrayList<String>> recordList = new ArrayList<ArrayList<String>>();
		
		try {
			cursor = myRecord.openCursor(null, null);
			key = new DatabaseEntry(tname.getBytes("UTF-8"));
			data = new DatabaseEntry();
			if(cursor.getSearchKey(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				do {
					dataString = new String(data.getData(), "UTF-8");
					recordList.add(parseDisk2Data(dataString));
				} while (cursor.getNextDup(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS);	
			}			
		} catch (DatabaseException de) {
			de.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if(cursor != null) cursor.close();
		
		return recordList;
	}
	
	public void insertRecord(String tname, ArrayList<String> valueList)
	{
		Cursor cursor = null;
		DatabaseEntry key = null;
		DatabaseEntry data = null;
		String dataString = null;
		
		try {
			cursor = myRecord.openCursor(null, null);
			key = new DatabaseEntry(tname.getBytes("UTF-8"));	
			dataString = parseData2Disk(valueList);
			data = new DatabaseEntry(dataString.getBytes("UTF-8"));
			cursor.put(key, data);
		} catch (DatabaseException de) {
			de.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if(cursor != null) cursor.close();
	}
	
	// cascade helper function
	// where table t. column c
	// replace all cvalue with null
	public void cascadeHelper(Table t, String cname, String cvalue)
	{
		Cursor cursor = null;
		Cursor addor = null;
		DatabaseEntry key = null;
		DatabaseEntry data = null;
		String dataString = null;
		ArrayList<String> valueList = null;
		
		int targetIdx = 0;
		for (Column c : t.columnList)
		{
			if(c.name.equals(cname)) break;
			targetIdx++;
		}
		
		try {
			cursor = myRecord.openCursor(null, null);			
			key = new DatabaseEntry(t.name.getBytes("UTF-8"));
			data = new DatabaseEntry();
			if(cursor.getSearchKey(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				do {
					dataString = new String(data.getData(), "UTF-8");
					valueList = parseDisk2Data(dataString);
					// if identical then replace to null
					if(valueList.get(targetIdx) != null && valueList.get(targetIdx).equals(cvalue))
					{
						valueList.set(targetIdx, "null");
						dataString = parseData2Disk(valueList);
						data = new DatabaseEntry(dataString.getBytes("UTF-8"));
						addor = myRecord.openCursor(null, null);
						cursor.delete();
						addor.put(key, data);
						addor.close();
					}
				} while (cursor.getNextDup(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS);	
			}			
		} catch (DatabaseException de) {
			System.out.println(de.getMessage());
			de.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if(cursor != null) cursor.close();
	}
	
	public boolean cascadeUpdate(Table t, String cname, String cvalue) throws ParseException
	{
		int refCount = t.referenced_count;
		ArrayList<String> refTableList = new ArrayList<String>();
		Boolean cascadeFlag = true;
		// step 1. find all referencing table
		Cursor cursor = mySchema.openCursor(null, null); 
	    DatabaseEntry foundKey = new DatabaseEntry();
	    DatabaseEntry foundData = new DatabaseEntry();
	    
	    if(cursor.getFirst(foundKey, foundData, LockMode.DEFAULT) != OperationStatus.SUCCESS) {
	    	// program can't get into here, because cascadeUpdate is called when there are other tables
	    	cursor.close();
	    	System.out.println("something goes wrong");
	    	throw new ParseException("hohoho");
	    }
    
		do {
			try {
				String keyString = new String(foundKey.getData(), "UTF-8");
				// note that, self reference is inhibited
				if(!keyString.equals(t.name))
				{
					Table target = getTableByName(keyString);
					for(Column c : target.columnList)
					{
						// found reference column
						if(c.is_foreign && c.reference_table.equals(t.name))
						{
							refCount--;
							String referedColumn = c.reference_column;
							// if one of reference column is not nullable,
							// cascade update fail, reject deletion
							if(c.is_not_null)
							{
								refCount = 0;
								cascadeFlag = false;
							}
						}
						
						// mark for faster search in step 2
						if(!refTableList.contains(target.name))
							refTableList.add(target.name);
					}
				}
				
				// searched all reference tables
				if(refCount <= 0) break;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS);
		if (cursor != null) cursor.close();
		
		// System.out.printf("%B\n", cascadeFlag);
		// step 2. check cascade option
		if(!cascadeFlag)
		{ // Case 2 in referential spec, one of ref column is not nullable
			
			return false;
		}
		
		// Case 1. replace all reference value as null
		cursor = mySchema.openCursor(null, null); 
	    foundKey = new DatabaseEntry();
	    foundData = new DatabaseEntry();
	    
	    if(cursor.getFirst(foundKey, foundData, LockMode.DEFAULT) != OperationStatus.SUCCESS) {
	    	// program can't get into here, because cascadeUpdate is called when there are other tables
	    	System.out.println("something goes wrong");
	    	throw new ParseException("hohoho");
	    }
    
		do {
			try {
				String keyString = new String(foundKey.getData(), "UTF-8");

				// if marked,
				if(refTableList.contains(keyString))
				{
					Table target = getTableByName(keyString);
					for(Column c : target.columnList)
					{
						// if this one, replace null
						if(c.is_foreign && c.reference_column.equals(cname))
						{
							cascadeHelper(target, c.name, cvalue);
							refCount--;
						}
					}
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS);
		if (cursor != null) cursor.close();
		
		return true;
	}
		
	// delete records with given wc conditions
	public int deleteRecord(WhereController.MyWhereClause wc, Table t) throws ParseException
	{
		ArrayList<String> columnNameList = new ArrayList<String>();
		int deleteCount = 0;
		int failCount = 0;
		
		for(Column c : t.columnList)
			columnNameList.add(c.name);
		
		Cursor cursor = null;
		DatabaseEntry key = null;
		DatabaseEntry data = null;
		String dataString = null;
		ArrayList<String> columnValueList = null;
		Boolean flag;
		Boolean cascadeFlag;
		
		try {
			cursor = myRecord.openCursor(null, null);
			key = new DatabaseEntry(t.name.getBytes("UTF-8"));
			data = new DatabaseEntry();
			if(cursor.getSearchKey(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				do {
					dataString = new String(data.getData(), "UTF-8");
					columnValueList = parseDisk2Data(dataString);
					wc.setEvalArgs(t, columnNameList, columnValueList, this, 1);
					flag = wc.eval();
					if(flag)
					{
						cascadeFlag = true;
						// check referential integrity constraints
						// if this table is referenced by other table
						if(t.referenced_count > 0)
						{
							int idx = 0;
							for(Column c : t.columnList)
							{
								if(c.is_primary)
								{
									cascadeFlag = cascadeFlag &	 cascadeUpdate(t, c.name, columnValueList.get(idx));
								}
								idx++;
							}
						}

						// if all satisfy conditions, DELETE
						if(cascadeFlag)
						{ // true if cascade option 1
							cursor.delete();
							deleteCount++;
						} else 
						{ // false if cascade option 2
							failCount++;
						}
					}
					
					// DEBUG, print every records
					// System.out.printf("%B, %s\n", flag, dataString);
				} while (cursor.getNextDup(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS);	
			}			
		} catch (DatabaseException de) {
			System.out.println(de.getMessage());
			de.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if(cursor != null) cursor.close();
		
		// if there is any failure,
		if (failCount > 0)
		{
			System.out.println(DBMSException.getMessage(26, String.valueOf(failCount)));	
		}
		
		return deleteCount;
	}
	
	// delete all rows in table t
	public int deleteAll(Table t) throws ParseException
	{
		Cursor cursor = null;
		DatabaseEntry key = null;
		DatabaseEntry data = null;
		int deleteCount = 0;
		try {
			cursor = myRecord.openCursor(null, null);
			key = new DatabaseEntry(t.name.getBytes("UTF-8"));
			data = new DatabaseEntry();
			if(cursor.getSearchKey(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				do {
					cursor.delete();
					deleteCount++;
				} while (cursor.getNextDup(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS);	
			}			
		} catch (DatabaseException de) {
			de.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if(cursor != null) cursor.close();
		
		return deleteCount;
	}
	
	public class Tuple{
		int x; int y;
		public Tuple(int x, int y) {this.x=x; this.y=y;}
	}
	
	/*
	 * create Caretsian Product
	 * time complexity : O(MN) when m rows X n rows
	 * IN : mySelectQuery
	 * OUT : 
	 * 		outCartesianResult : records of result table (return value)
	 * 		outColumnNameList : cnl of result table 
	 * 		outCartesianTable : table object of result table
	 */
	
	public void selectQuery(SelectController.MySelectQuery mySelectQuery) throws ParseException
	{
		// Step 1. do cartesian product for given tables
		// case 1. two tables -> single function call
		// case 2. three or more tables -> nested function call
		
		// Step 2. resolve where conditions
		// how? call eval(), evalArgs needs 4 params which are
		// Table t, ArrayList<String> cnl, ArrayList<String> vl, DBMSController ctrl
		// t : make virtual table which results of select (on memory)
		// cnl : result of cartesian product
		// vl : for iteration of (result of cartesian product)
		// ctrl : this
		
		// note that, wherecontroller doesn't support referencing
		// so need to handle in all-in-one table, if t.c is refered then find column named t.c in cnl
		
		ArrayList<Integer> outColumnOrderList = new ArrayList<>();
		ArrayList<String> outColumnNameList = new ArrayList<>();
		ArrayList<ArrayList<String>> outCartesianResult = new ArrayList<>();
		ArrayList<Integer> searchIdx = new ArrayList<>();
		
		// this is only virtual table
		Table newTable = new Table("cartesian", this);
		
		// SELECT sl.scl : T.C as C' | C as C' 
		// FROM  te.fc : T as T'
		// WHERE te.wc : T.C | C'
		
		// Step 0. get original information from FromClause
		HashMap<String, Table> originalTableMap = new HashMap<>();
		HashMap<String, Column> originalColumnMap = new HashMap<>();
		
		HashMap<String, Tuple> columnIndexMap = new HashMap<>();
		
		ArrayList<String> fullColumnNameList = new ArrayList<>();
		ArrayList<String> fullValueList = new ArrayList<>();
		
		ArrayList<String> tableNameList = new ArrayList<>();

		// search original column
		int tidx = 0;
		int cidx = 0;
		ArrayList<String> dupCheck = new ArrayList<>();
		ArrayList<String> dup = new ArrayList<>();
		for(String key : mySelectQuery.tableExpression.fromClause.tableMap.keySet())
		{
			String originalTableName = mySelectQuery.tableExpression.fromClause.tableMap.get(key);
			Table originalTable = getTableByName(originalTableName);
			// fail to get table
			// case 28, SelectTableExistenceError(#tableName)
			if(originalTable == null)
			{
				System.out.println(DBMSException.getMessage(28, originalTableName));
				throw new ParseException("hohoho");
			}
			// note that, table map contains 'as name' = T' as key
			originalTableMap.put(key, originalTable);
			cidx = 0;
			for(Column c : originalTable.columnList)
			{
				// column map save single column as two pairs
				// 1. T'.C - column
				// 2. C - column
				// both can use in next Step		
				originalColumnMap.put(key + "." + c.name, c);
				originalColumnMap.put(c.name, c);
				columnIndexMap.put(key + "." + c.name, new Tuple(tidx, cidx));
				columnIndexMap.put(c.name, new Tuple(tidx, cidx));
				cidx++;
				
				// dupCheck : unique column name list
				// dup : duplicated column name list
				if(dupCheck.contains(c.name))
					dup.add(c.name);
				else
					dupCheck.add(c.name);
				
				fullColumnNameList.add(key + "." + c.name);
				fullValueList.add(null);
				Column cp = c;
				cp.name = key + "." + c.name;
				newTable.addColumn(cp);
			}
			searchIdx.add(0);
			tidx++;
			tableNameList.add(key);
		}
		
		// Step 1. resolve table name
		// in from clause some tables are renamed, so resolves them at selectlist
		for(SelectController.SelectedColumn sc : mySelectQuery.selectList.selectedColumnList)
		{
			Column originColumn = null;
			String tmpName;
			if(sc.tableName == null)
			{
				// C
				if(sc.asName == null)
				{
					// can't resolve, not unique
					// Case 27, SelectColumnResolveError
					if(dup.contains(sc.columnName))
					{
						System.out.println(DBMSException.getMessage(27, sc.columnName));
						throw new ParseException("hohoho");
					}
					// get table Name
					sc.tableName = tableNameList.get(columnIndexMap.get(sc.columnName).x);
					tmpName = sc.tableName + "." + sc.columnName;
				} else // C as C' 
				{
					tmpName = sc.asName;
				}
				originColumn = originalColumnMap.get(sc.columnName);
			}
			else
			{
				// T.C
				if(sc.asName == null)
				{
					tmpName = sc.tableName + "." + sc.columnName;
				}
				else // T.C as C'
				{
					tmpName = sc.asName;
				}
				originColumn = originalColumnMap.get(sc.tableName + "." + sc.columnName);
			}
			
			// rename
			originColumn.name = tmpName;
			outColumnOrderList.add(fullColumnNameList.indexOf(sc.tableName + "." + sc.columnName));
			outColumnNameList.add(tmpName);
		}
		// now, t and cnl is created
				
		// Step 1. calculate, joined product's number of columns
		if(mySelectQuery.selectList.isAsterisk)
		{
			// case 1. asterisk -> auto matching
			// auto matching : if two columns' type and name are identical then count as one
			int iter = 0;
			for(String s : fullColumnNameList)
			{
				outColumnOrderList.add(iter++);
				outColumnNameList.add(s);
			} 
		}
		// case 2. given -> number of element in msq.SelectList
		
		// Step 3. do cartesian product
		// searchIdx = (p,q,r,...) tuples which represents
		// p'th in T1, q'th in T2, r'th in T3
		
		// all records table
		ArrayList<ArrayList<ArrayList<String>>> wholeRecords = new ArrayList<>();
		Boolean toxicFlag = false;
		for(String key : mySelectQuery.tableExpression.fromClause.tableMap.keySet())
		{
			ArrayList<ArrayList<String>> vlist = readRecords(mySelectQuery.tableExpression.fromClause.tableMap.get(key));
			if(vlist.size() == 0) {
				toxicFlag = true;
				break;
			}
				
			wholeRecords.add(vlist);
		}

		ArrayList<Integer> maxIdx = new ArrayList<>();
		for(ArrayList<ArrayList<String>> tr : wholeRecords)
		{
			maxIdx.add(tr.size()-1);
		}
		
		/*
		 * ArrayList<ArrayList<String>> outCartesianResult = new ArrayList<>();
			ArrayList<Integer> searchIdx = new ArrayList<>();
			ArrayList<String> currentValueList = new ArrayList<>();
		 */
		if(!toxicFlag)
		{
			do
			{
				// read value
				for(int j=0;j<fullValueList.size();j++)
				{
					String cname = fullColumnNameList.get(j);
					Tuple ctuple = columnIndexMap.get(cname);
					// tidx table @ ith rows @ tidx value
					String cvalue = wholeRecords.get(ctuple.x)
												.get(searchIdx.get(ctuple.x))
												.get(ctuple.y);
					fullValueList.set(j, cvalue);
				}
				
				// eval
				if(mySelectQuery.tableExpression.whereClause == null)
				{
					ArrayList<String> cpList = (ArrayList<String>) fullValueList.clone();
					outCartesianResult.add(cpList);
				}
				else {
					mySelectQuery.tableExpression.whereClause.setEvalArgs(newTable, fullColumnNameList, fullValueList, this, 0);
					if(mySelectQuery.tableExpression.whereClause.eval())
					{ // if pass
						ArrayList<String> cpList = (ArrayList<String>) fullValueList.clone();
						outCartesianResult.add(cpList);
					}
				}
			} while(getNextIdx(searchIdx, maxIdx));
		}

		// Step 4. print
		assert(outColumnNameList.size() == outColumnOrderList.size());
		
		ArrayList<Integer> maxSpaceList = new ArrayList<>();
		for(String s : outColumnNameList)
			maxSpaceList.add(s.length()-2);
			
		for(ArrayList<String> vvll : outCartesianResult)
		{
			int iter = 0;
			for(int i : outColumnOrderList)
			{
				String tmp = vvll.get(i);
				if(tmp == null) tmp = "null";
				if(tmp.length() > maxSpaceList.get(iter))
					maxSpaceList.set(iter, tmp.length());
				iter++;
			}
		}
		
		// print title
		for(int l : maxSpaceList)
		{
			System.out.print("+");
			for(int i=0;i<l+2;i++)
				System.out.print("-");
		}
		System.out.print("+");
		System.out.println();
		
		int pad = 0;
		for(int i=0;i<outColumnNameList.size();i++)
		{
			String tmp = outColumnNameList.get(i);
			tmp = tmp.substring(2, tmp.length());
			pad = maxSpaceList.get(i) - tmp.length();
			System.out.print("| ");
			for(int j=0;j<pad/2;j++)
				System.out.print(" ");
			System.out.print(tmp);
			for(int j=0;j<pad-pad/2+1;j++)
				System.out.print(" ");
		}
		System.out.print("|");
		System.out.println();
		
		for(int l : maxSpaceList)
		{
			System.out.print("+");
			for(int i=0;i<l+2;i++)
				System.out.print("-");
		}
		System.out.print("+");
		System.out.println();
		
		// print data
		
		for(ArrayList<String> vvll : outCartesianResult)
		{
			for(int i=0;i<outColumnNameList.size();i++)
			{
				String tmp = tvalue2string(vvll.get(outColumnOrderList.get(i)));
				pad = maxSpaceList.get(i) - tmp.length();
				System.out.print("| ");
				System.out.print(tmp);
				for(int j=0;j<pad+1;j++)
					System.out.print(" ");
			}
			System.out.print("|");
			System.out.println("");
		}
		
		// print close
		for(int l : maxSpaceList)
		{
			System.out.print("+");
			for(int i=0;i<l+2;i++)
				System.out.print("-");
		}
		System.out.print("+");
		System.out.println();
	}
	
	public static String tvalue2string(String tvalue)
	{
		if(tvalue == null) return "null";
		int type = Integer.valueOf(tvalue.substring(0, 1));
		if(type == 2)
		{
			return tvalue.substring(2, tvalue.length()-1);
		}
		else
		{
			return tvalue.substring(1, tvalue.length());
		}
	}
	
	// increase cur as 1, until max, return false if max
	public static boolean getNextIdx(ArrayList<Integer> cur, ArrayList<Integer>max)
	{
		int len = cur.size() - 1;
		cur.set(len, cur.get(len) + 1);
		for(int i=len;i>=0;i--)
		{
			if(max.get(i) < cur.get(i))
			{
				if(i == 0)
				{
					// System.out.println("END");
					return false;
				}
				cur.set(i, 0);
				cur.set(i-1, cur.get(i-1) + 1);
			}
			else
			{
				break;
			}
		}
		return true;
	}
}
