import java.io.File;
import java.io.UnsupportedEncodingException;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Cursor;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class dbtest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("hello world!");
		
		 // Environment & Database define
	    Environment myDbEnvironment = null;
	    Database myDatabase = null;

	    /* OPENING DB */

	    // Open Database Environment or if not, create one.
	    EnvironmentConfig envConfig = new EnvironmentConfig();
	    envConfig.setAllowCreate(true);
	    myDbEnvironment = new Environment(new File("db/"), envConfig);

	    // Open Database or if not, create one.
	    DatabaseConfig dbConfig = new DatabaseConfig();
	    dbConfig.setAllowCreate(true);
	    dbConfig.setSortedDuplicates(true);
	    myDatabase = myDbEnvironment.openDatabase(null, "sampleDatabase", dbConfig);

	    /* PUT <K,V > INTO DB */
	    Cursor cursor = null;
	    DatabaseEntry key;
	    DatabaseEntry data;

	    try {
	      cursor = myDatabase.openCursor(null, null);
	      key = new DatabaseEntry("key".getBytes("UTF-8"));
	      data = new DatabaseEntry("value".getBytes("UTF-8"));
	      cursor.put(key, data);
	    } catch (DatabaseException de) {
	    } catch (UnsupportedEncodingException e) {
	      e.printStackTrace();
	    } finally {
			if(cursor != null) cursor.close();
		}


	    /* GET <K,V > FROM DB */
	    DatabaseEntry foundKey = new DatabaseEntry();
	    DatabaseEntry foundData = new DatabaseEntry();

	    cursor = myDatabase.openCursor(null, null);
	    cursor.getFirst(foundKey, foundData, LockMode.DEFAULT);

	    do {
	      try {
	        String keyString = new String(foundKey.getData(), "UTF-8");
	        String dataString = new String(foundData.getData(), "UTF-8");
	        System.out.println("<" + keyString + ", " + dataString + ">");
	        if(keyString.equals("k")) cursor.delete();
	      } catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	      }
	    } while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS);
	    if (cursor != null) cursor.close();
	    System.out.println("-----");
	    
	    /* CLOSING DB */
	    if (myDatabase != null) myDatabase.close();
	    if (myDbEnvironment != null) myDbEnvironment.close();
	}

}
