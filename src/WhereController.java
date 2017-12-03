import java.util.ArrayList;

// <WHERE CLAUSE> ::= where <BOOLEAN VALUE EXPRESSION>
public class WhereController {
	
	public static class MyWhereClause
	{
		BooleanValueExpression booleanValueExpression;
		
		public void setBooleanValueExpression(BooleanValueExpression booleanValueExpression) {this.booleanValueExpression = booleanValueExpression;}
		
		public void print()
		{
			System.out.println("Where:");
			booleanValueExpression.print();
		}
	}
	
	public static class MyBoolean{
		public static int UNKNOWN = 0;
		public static int TRUE = 1;
		public static int FALSE = 2;
		public int value;
		
		public MyBoolean(int value) {this.value = value;}
		
		public MyBoolean or(MyBoolean target) {
			// unknown | ??? = unknown
			if (this.value == UNKNOWN)
				return this;
			else // true | ??? = ???
				return target;
		}
		
		public MyBoolean and(MyBoolean target) {
			if (this.value == TRUE) // TRUE & ??? = ???
				return target;
			else if (this.value == FALSE) // FALSE & ??? = FALSE
				return this;
			else {
				// UNKNOWN & FALSE = FALSE
				if (target.value == FALSE) return target;
				// UNKNOWN & TRUE = UNKNOWN & UNKNOWN = UNKNOWN
				else return this;
			}
		}
		
		public MyBoolean not() {
			// NOT TRUE = FALSE
			if (this.value == TRUE) return new MyBoolean(FALSE);
			// NOT FALSE = TRUE
			else if (this.value == FALSE) return new MyBoolean(TRUE);
			// NOT UNKNOWN = UNKNOWN
			else return this; 
		}
		
		public boolean eval() {
			if (this.value == TRUE) return true;
			else return false;
		}
	}
	
	// <BOOLEAN VALUE EXPRESSION> ::= <BOOLEAN TERM> ( or <BOOLEAN TERM>)*
	public static class BooleanValueExpression{
		ArrayList<BooleanTerm> booleanTermList;
		
		public BooleanValueExpression(){ booleanTermList = new ArrayList<>(); }
		public void add(BooleanTerm booleanTerm) {booleanTermList.add(booleanTerm);}
		
		MyBoolean eval() {
			MyBoolean flag = new MyBoolean(MyBoolean.FALSE);
			for(BooleanTerm bt : booleanTermList)
			{
				flag = flag.or(bt.eval());
			}
			return flag;
		}
		
		public void print()
		{
			System.out.println("\tBVE:");
			for(BooleanTerm bt : booleanTermList)
			{
				bt.print();
			}
		}
	}
	
	// <BOOLEAN TERM> ::= <BOOLEAN FACTOR>( and <BOOLEAN FACTOR> )*
	public static class BooleanTerm{
		ArrayList<BooleanFactor> booleanFactorList;
		
		public BooleanTerm() { booleanFactorList = new ArrayList<>(); }
		public void add(BooleanFactor booleanFactor) { booleanFactorList.add(booleanFactor);}
		
		MyBoolean eval() {
			MyBoolean flag = new MyBoolean(MyBoolean.TRUE);
			for(BooleanFactor bf : booleanFactorList)
			{
				flag = flag.and(bf.eval());
			}
			return flag;
		}
		
		public void print()
		{
			System.out.println("\t\tBT:");
			for(BooleanFactor bf : booleanFactorList)
			{
				bf.print();
			}
		}
	}
	
	// <BOOLEAN FACTOR> ::= [not] <BOOLEAN TEST>
	public static class BooleanFactor{
		boolean isNot = false;
		BooleanTest booleanTest;
		
		public void setIsNot() {this.isNot = true;}
		public void setBooleanTest(BooleanTest booleanTest) {this.booleanTest = booleanTest;}
		
		MyBoolean eval() {
			return (isNot ? booleanTest.eval().not() : booleanTest.eval());
		}
		
		public void print()
		{
			System.out.println("\t\t\tBF:");
			if(isNot) System.out.print("NOT "); 
			booleanTest.print();
		}
	}
	
	// <BOOLEAN TEST> ::= <PREDICATE>
	// | <PARENTHESIZED BOOLEAN EXPRESSION>
	public static abstract class BooleanTest{
		abstract MyBoolean eval();

		abstract void print();
	}
	
	// <PARENTHESIZED BOOLEAN EXPRESSION> ::= <LEFT PAREN> <BOOLEAN VALUE EXPRESSION> <RIGHT PAREN>
	public static class ParenthesizedBooleanExpression extends BooleanTest{
		BooleanValueExpression booleanValueExpression;
		
		public void setBooleanValueExpression(BooleanValueExpression booleanValueExpression) {this.booleanValueExpression = booleanValueExpression;}
		
		@Override
		MyBoolean eval() {
			return booleanValueExpression.eval();
		}
		
		@Override
		public void print()
		{
			System.out.println("\t\t\t\tPBE {");
			booleanValueExpression.print();
			System.out.println("\t\t\t\tPBE }");
		}
	}
	
	// <PREDICATE> ::= <COMPARISON PREDICATE>
	// | <NULL PREDICTE>
	public static abstract class Predicate extends BooleanTest{
		abstract MyBoolean eval();
	}
	
	// <COMPARISON PREDICATE> ::= <COMP OPERAND> <COMP OP> <COMP OPERAND>
	public static class ComparisonPredicate extends Predicate{
		public static String RB = "<";
		public static String LB = ">";
		public static String EQ = "=";
		public static String LEB = ">=";
		public static String REB = "<=";
		public static String NEQ = "!=";
		
		public String compOp;
		public CompOperand leftValue;
		public CompOperand rightValue;
		
		public ComparisonPredicate() {}
		public void setOp(String compOp) {this.compOp=compOp;}
		public void setLeft(CompOperand leftValue) {this.leftValue=leftValue;}
		public void setRight(CompOperand rightValue) {this.rightValue=rightValue;}

		@Override
		MyBoolean eval() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		void print() {
			// TODO Auto-generated method stub
			System.out.println("\t\t\t\t\tCP");
			leftValue.print();
			System.out.println("\t\t\t\t\t\t" + compOp);
			rightValue.print();
		}
	}
	
	// <COMP OPERAND> ::= <COMPARABLE VALUE>
	// | [<TABLE NAME> <PERIOD>] <COLUMN NAME>
	public static class CompOperand{
		int OperandType; // 0 ComparableValue, 1 ColumnName
		String comparableValue;
		String tableName = "";
		String columnName = "";
		
		public void setComparableValue(String comparableValue) {
			this.OperandType = 0;
			this.comparableValue = comparableValue;
		}
		
		public void setTableName(String tableName) {
			this.OperandType = 1;
			this.tableName = tableName;
		}
		
		public void setColumnName(String columnName) {
			this.OperandType = 1;
			this.columnName = columnName;
		}
		
		public void print()
		{
			if(this.OperandType == 1) System.out.println("\t\t\t\t\t\t1:" + tableName + " " + columnName);
			else System.out.println("\t\t\t\t\t\t0:" + comparableValue);
		}
	}
	
	// <NULL PREDICATE> ::= [<TABLE NAME> <PERIOD>] <COLUMN NAME> <NULL OPERATION>
	// <NULL OPERATION> ::= is [not] null
	public static class NullPredicate extends Predicate{
		String tableName = "";
		String columnName = "";
		boolean isNull = false;
		
		public void setTableName(String tableName) {this.tableName = tableName;}
		public void setColumnName(String columnName) {this.columnName = columnName;}
		public void setIsNull() {this.isNull=true;}
		
		@Override
		MyBoolean eval() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		void print() {
			// TODO Auto-generated method stub
			System.out.print("\t\t\t\t\t\t");
			if(this.isNull) System.out.print("IS NULL "); else System.out.print("IS NOT NULL "); 
			System.out.print(tableName + " ");
			System.out.print(columnName + "\n");
		}
	}
	
}
