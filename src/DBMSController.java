import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
			if(s == null) sb.append("");
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
		try {
			// this regex pattern is identical to , '1<INT> or 2<CHAR_STRING> or 3<DATE>' in token matching
			pattern = Pattern.compile("1[+,-]?[0-9]*\\s|2'[^']*+'\\s|3[[0-9]{4}-[0-9]{2}-[0-9]{2}]*\\s");
			matcher = pattern.matcher(tvalue);
			while(matcher.find()) {
				valueList.add(matcher.group(0));
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
		Cursor cursor = myRecord.openCursor(null, null); 
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
					dataString = data.toString();
					recordList.add(parseDisk2Data(dataString));
				} while (cursor.getNextDup(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS);	
			}			
		} catch (DatabaseException de) {
			de.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			if(cursor != null) cursor.close();
		}
		
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
			data = new DatabaseEntry();
			
			dataString = parseData2Disk(valueList);
			data = new DatabaseEntry(dataString.getBytes("UTF-8"));
			cursor.put(key, data);
		} catch (DatabaseException de) {
			de.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			if(cursor != null) cursor.close();
		}
	}
	
	
}
