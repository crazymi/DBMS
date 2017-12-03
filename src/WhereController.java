import java.util.ArrayList;

// <WHERE CLAUSE> ::= where <BOOLEAN VALUE EXPRESSION>
public class WhereController {
	
	// <BOOLEAN VALUE EXPRESSION> ::= <BOOLEAN TERM> ( or <BOOLEAN TERM>)*
	public static class BooleanValueExpression{
		ArrayList<BooleanTerm> booleanTermList;
		
		public BooleanValueExpression(){ booleanTermList = new ArrayList<>(); }
		
		boolean eval() {
			return false;
		}
	}
	
	// <BOOLEAN TERM> ::= <BOOLEAN FACTOR>( and <BOOLEAN FACTOR> )*
	public static class BooleanTerm{
		ArrayList<BooleanFactor> booleanFactorList;
		
		public BooleanTerm() { booleanFactorList = new ArrayList<>(); }
		
		boolean eval() {
			return false;
		}
	}
	
	// <BOOLEAN FACTOR> ::= [not] <BOOLEAN TEST>
	public static class BooleanFactor{
		boolean isNot = false;
		BooleanTest booleanTest;
		
		boolean eval() {
			return (isNot ? !booleanTest.eval() : booleanTest.eval());
		}
	}
	
	// <BOOLEAN TEST> ::= <PREDICATE>
	// | <PARENTHESIZED BOOLEAN EXPRESSION>
	public static abstract class BooleanTest{
		abstract boolean eval();
	}
	
	// <PARENTHESIZED BOOLEAN EXPRESSION> ::= <LEFT PAREN> <BOOLEAN VALUE EXPRESSION> <RIGHT PAREN>
	public static class ParenthesizedBooleanExpression extends BooleanTest{
		BooleanValueExpression booleanValueExpression;
		
		@Override
		boolean eval() {
			return booleanValueExpression.eval();
		}
	}
	
	// <PREDICATE> ::= <COMPARISON PREDICATE>
	// | <NULL PREDICTE>
	public static abstract class Predicate extends BooleanTest{
		abstract boolean eval();
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
		boolean eval() {
			// TODO Auto-generated method stub
			return false;
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
		boolean eval() {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
}
