import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

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
import com.sleepycat.je.Put;
import com.sleepycat.je.WriteOptions;

public class DBMSController {
	// Environment & Database define
	Environment myDbEnvironment = null;
    Database mySchema = null;
    
    /* OPENING DB */
	public DBMSController(){
	    // Open Database Environment or if not, create one.
	    EnvironmentConfig envConfig = new EnvironmentConfig();
	    envConfig.setAllowCreate(true);
	    myDbEnvironment = new Environment(new File("db/"), envConfig);

	    // Open Database or if not, create one.
	    DatabaseConfig dbConfig = new DatabaseConfig();
	    dbConfig.setAllowCreate(true);
	    dbConfig.setSortedDuplicates(true);
	    mySchema = myDbEnvironment.openDatabase(null, "schema", dbConfig);
		
	}
	
	/* CLOSING DB */
	public void close() {
	    if (mySchema != null) mySchema.close();
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
	    		// System.out.println(t2json);
	    		data = new DatabaseEntry(t2json.getBytes("UTF-8"));
		    	cursor.put(key, data);
	    	}
	    } catch (DatabaseException de) {
	    } catch (UnsupportedEncodingException e) {
	      e.printStackTrace();
	    } finally {
			if(cursor != null) cursor.close();
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
	    	  if(target.is_referenced_table > 0){
	    		  System.out.println(DBMSException.getMessage(12, target.name));
	    		  throw new ParseException("hoho");
	    	  }
	    	  for(Column c : target.columnList) {
	    		  if(c.is_foreign) {
	    			  Table t = getTableByName(c.reference_table);
	    			  refUpdate(c.reference_table, -1);
	    		  }
	    	  }
	    	  cursor.delete();
	      } else { // == NOTFOUND
	    	  System.out.println(DBMSException.getMessage(9, null));
	    	  throw new ParseException("hoho");
	      }
	    } catch (DatabaseException de) {
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
	    		result.is_referenced_table = result.is_referenced_table + diff;
	    		
	    		String t2json = new Gson().toJson(result);
	    		data = new DatabaseEntry(t2json.getBytes("UTF-8"));
	    		
	    		// TODO how to overwrite? putCurrent not works
	    		cursor.delete();
	    		cursor.put(key, data);
	    	} else { // == NOTFOUND
	    		result = null;
	    	}
	    } catch (DatabaseException de) {
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			if(cursor != null) cursor.close();
		}
	}
}
