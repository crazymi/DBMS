import java.util.ArrayList;

public class SelectController {
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
		FromClause fc = null;
		WhereController.MyWhereClause wc = null;
		
		public void setFromClause(FromClause fc) {this.fc=fc;}
		public void setWhereClause(WhereController.MyWhereClause wc) {this.wc = wc;}
	}
	
	public static class FromClause{
		ArrayList<ReferedTable> tableReferenceList = new ArrayList<>();
		
		public void add(ReferedTable rt) {tableReferenceList.add(rt);}
	}
	
	public static class ReferedTable{
		String tableName = null;
		String asName = null;
		
		public void setTableName(String tableName) {this.tableName=tableName;}
		public void setAsName(String asName) {this.asName=asName;}
	}
}
