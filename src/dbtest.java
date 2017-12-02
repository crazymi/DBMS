import java.io.File;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.google.gson.Gson;
import com.sleepycat.je.Cursor;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class dbtest {

	// dbtest prints saved schema as JSON format
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		insertTest();
		
		/*
		String str = new String("1234");
		int intt = Integer.parseInt(str.substring(1, 2));
		System.out.println(intt);
		*/
		// test();
	}
	
	public static void insertTest()
	{
		ArrayList<String> columnList = new ArrayList<>();
		ArrayList<String> cList = new ArrayList<>();
		ArrayList<String> vList = new ArrayList<>();
		ArrayList<String> newclist = new ArrayList<>();
		ArrayList<String> newvlist = new ArrayList<>();
		columnList.add("a"); cList.add("b"); vList.add("2");
		columnList.add("b"); cList.add("a"); vList.add("1");
		columnList.add("c"); cList.add("d"); vList.add("4");
		columnList.add("d"); cList.add("c"); vList.add("3");
		columnList.add("e"); cList.add("f"); vList.add("6");
		columnList.add("f"); //  cList.add("e"); vList.add("5");
		int idx = 0;
		for(String c : columnList)
		{
			newclist.add(c);
			// if no column
			if((idx = cList.indexOf(c)) != -1)
			{
				newvlist.add(vList.get(idx));
			}
			else 
			{
				newvlist.add(null);
			}
		}
		
		for(int i=0;i<newclist.size();i++) {
			System.out.printf("%s %s\n", newclist.get(i), newvlist.get(i));
		}
	}
	
	public static void regexTest()
	{
		String req = "1-1234 2'123dacjd$ bvc# ' 1 2'' 3 31994-41-12 1241 32017-12-42 1+234 ";

		Pattern pattern = Pattern.compile("1[+,-]?[0-9]*\\s|2'[^']*+'\\s|3[[0-9]{4}-[0-9]{2}-[0-9]{2}]*\\s");
		Matcher matcher = pattern.matcher(req);

		while(matcher.find()) {
		    System.out.println(matcher.group(0));
		}		
	}
	
	public static void test()
	{

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
	    myDatabase = myDbEnvironment.openDatabase(null, "schema", dbConfig);

	    Cursor cursor = null;
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
	      } catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	      }
	    } while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS);
	    if (cursor != null) cursor.close();
	    System.out.println("-----");
	    
	    if (myDatabase != null) myDatabase.close();
	    if (myDbEnvironment != null) myDbEnvironment.close();
	}
	
}
