import java.util.ArrayList;

// <WHERE CLAUSE> ::= where <BOOLEAN VALUE EXPRESSION>
public class WhereController {
	
	public static class MyBoolean{
		public enum mv {UNKNOWN, TRUE, FALSE}
		public mv value;
		
		public MyBoolean(mv value) {this.value = value;}
		
		public MyBoolean or(MyBoolean target) {
			// unknown | ??? = unknown
			if (this.value == mv.UNKNOWN)
				return this;
			else // true | ??? = ???
				return target;
		}
		
		public MyBoolean and(MyBoolean target) {
			if (this.value == mv.TRUE) // TRUE & ??? = ???
				return target;
			else if (this.value == mv.FALSE) // FALSE & ??? = FALSE
				return this;
			else {
				// UNKNOWN & FALSE = FALSE
				if (target.value == mv.FALSE) return target;
				// UNKNOWN & TRUE = UNKNOWN & UNKNOWN = UNKNOWN
				else return this;
			}
		}
		
		public MyBoolean not() {
			// NOT TRUE = FALSE
			if (this.value == mv.TRUE) return new MyBoolean(mv.FALSE);
			// NOT FALSE = TRUE
			else if (this.value == mv.FALSE) return new MyBoolean(mv.TRUE);
			// NOT UNKNOWN = UNKNOWN
			else return this; 
		}
		
		public boolean eval() {
			if (this.value == mv.TRUE) return true;
			else return false;
		}
	}
	
	// <BOOLEAN VALUE EXPRESSION> ::= <BOOLEAN TERM> ( or <BOOLEAN TERM>)*
	public static class BooleanValueExpression{
		ArrayList<BooleanTerm> booleanTermList;
		
		public BooleanValueExpression(){ booleanTermList = new ArrayList<>(); }
		
		MyBoolean eval() {
			return null;
		}
	}
	
	// <BOOLEAN TERM> ::= <BOOLEAN FACTOR>( and <BOOLEAN FACTOR> )*
	public static class BooleanTerm{
		ArrayList<BooleanFactor> booleanFactorList;
		
		public BooleanTerm() { booleanFactorList = new ArrayList<>(); }
		
		MyBoolean eval() {
			return null;
		}
	}
	
	// <BOOLEAN FACTOR> ::= [not] <BOOLEAN TEST>
	public static class BooleanFactor{
		boolean isNot = false;
		BooleanTest booleanTest;
		
		MyBoolean eval() {
			return (isNot ? booleanTest.eval().not() : booleanTest.eval());
		}
	}
	
	// <BOOLEAN TEST> ::= <PREDICATE>
	// | <PARENTHESIZED BOOLEAN EXPRESSION>
	public static abstract class BooleanTest{
		abstract MyBoolean eval();
	}
	
	// <PARENTHESIZED BOOLEAN EXPRESSION> ::= <LEFT PAREN> <BOOLEAN VALUE EXPRESSION> <RIGHT PAREN>
	public static class ParenthesizedBooleanExpression extends BooleanTest{
		BooleanValueExpression booleanValueExpression;
		
		@Override
		MyBoolean eval() {
			return booleanValueExpression.eval();
		}
	}
	
	// <PREDICATE> ::= <COMPARISON PREDICATE>
	// | <NULL PREDICTE>
	public static abstract class Predicate extends BooleanTest{
		abstract MyBoolean eval();
	}
	
	// <COMPARISON PREDICATE> ::= <COMP OPERAND> <COMP OP> <COMP OPERAND>
	public static class ComparisonPredicate extends Predicate{
		public static enum CompOp{
			// <, >, =, >=, <=, NEQ
			RB, LB, EQ, LEB, REB, NEQ
		}
		
		public CompOp compOp;
		public CompOperand leftValue;
		public CompOperand rightValue;
		
		public ComparisonPredicate() {}

		@Override
		MyBoolean eval() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	// <COMP OPERAND> ::= <COMPARABLE VALUE>
	// | [<TABLE NAME> <PERIOD>] <COLUMN NAME>
	public static class CompOperand{
		int OperandType; // 0 ComparableValue, 1 ColumnName
		String comprableValue;
		String tableName;
		String columnName;
		
		public void initComparableValue(String comprableValue) {
			this.OperandType = 0;
			this.comprableValue = comprableValue;
		}
		
		public void initColumn(String columnName) {
			this.OperandType = 1;
			this.tableName = null;
			this.columnName = columnName;
		}
		
		public void initColumn(String tableName, String columnName) {
			this.initColumn(columnName);
			this.tableName = tableName;
		}
	}
	
	// <NULL PREDICATE> ::= [<TABLE NAME> <PERIOD>] <COLUMN NAME> <NULL OPERATION>
	// <NULL OPERATION> ::= is [not] null
	public static class NullPredicate extends Predicate{
		String tableName;
		String columnName;
		boolean isNull;
		
		@Override
		MyBoolean eval() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
}
