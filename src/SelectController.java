import java.util.ArrayList;
import java.util.HashMap;

public class SelectController {
	public static class MySelectQuery{
		SelectList selectList;
		TableExpression tableExpression;
		
		public MySelectQuery(SelectList selectList, TableExpression tableExpression)
		{this.selectList=selectList; this.tableExpression=tableExpression;}
	}
	public static class SelectList{
		boolean isAsterisk = false;
		ArrayList<SelectedColumn> selectedColumnList = new ArrayList<>();
		
		public void setAsterisk() {isAsterisk = true;}
		public void add(SelectedColumn sc) {selectedColumnList.add(sc);}
	}
	
	public static class SelectedColumn{
		String tableName = null;
		String columnName = null;
		String asName = null;
		
		public void setTableName(String tableName) {this.tableName=tableName;}
		public void setColumnName(String columnName) {this.columnName=columnName;}
		public void setAsName(String asName) {this.asName=asName;}
	}
	
	public static class TableExpression{
		FromClause fromClause = null;
		WhereController.MyWhereClause whereClause = null;
		
		public void setFromClause(FromClause fc) {this.fromClause=fc;}
		public void setWhereClause(WhereController.MyWhereClause wc) {this.whereClause = wc;}
	}
	
	public static class FromClause{
		HashMap<String, String> tableMap = new HashMap<>();
		
		// note that <K,V> is <as, table>
		// because most of lookup called by 'asname' which saved in selectList
		public void add(String tname, String asname)
		{
			// if asname not defined, use original
			if(asname == null) tableMap.put(tname, tname);
			else tableMap.put(asname, tname);
		}
	}
}
