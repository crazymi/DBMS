import java.util.ArrayList;

// <WHERE CLAUSE> ::= where <BOOLEAN VALUE EXPRESSION>
public class WhereController {
	
	public static class EvalArgs
	{
		Table table;
		ArrayList<String> columnNameList;
		ArrayList<String> valueList;
		DBMSController ctrl;
		int type; // 0 for select 1 for delete
		
		public EvalArgs(Table t, ArrayList<String> cnl, ArrayList<String> vl, DBMSController ctrl, int type)
		{this.table = t; this.columnNameList=cnl; this.valueList=vl; this.ctrl=ctrl; this.type=type;}
	}
	
	public static class MyWhereClause
	{
		BooleanValueExpression booleanValueExpression;
		EvalArgs evalArgs = null;
		
		public void setBooleanValueExpression(BooleanValueExpression booleanValueExpression) {this.booleanValueExpression = booleanValueExpression;}
		public void setEvalArgs(Table t, ArrayList<String> cnl, ArrayList<String> vl, DBMSController ctrl, int type) {evalArgs = new EvalArgs(t, cnl, vl, ctrl, type);}
		
		Boolean eval() throws ParseException{
			assert(evalArgs != null);
			return booleanValueExpression.eval(evalArgs).eval();
		}
		
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
		public String toString()
		{
			switch (value)
			{
			case 0:
				return "UNKNOWN";
			case 1:
				return "TRUE";
			case 2:
				return "FALSE";
			default:
					return "";
			}
		}
		
		/* unknown operation specification, 03_SQL p26
		 * OR: (unknown or true)   = true
		 *     (unknown or false)  = unknown 
		 *     (unknown or unknown) = unknown
		 * AND:(true and unknown)  = unknown, 
		 *     (false and unknown) = false,
		 *     (unknown and unknown) = unknown
		 * NOT:(not unknown) = unknown
		 * 
		 *  p q AND OR
		 *  T T  T  T
		 *  T F  F  T
		 *  T U  U  T
		 *  F F  F  F
		 *  F U  F  U
		 *  U U  U  U
		 */
		
		public MyBoolean or(MyBoolean target) {
			if (this.value == UNKNOWN) {
				// U|T = T
				if(target.value == TRUE) return target;
				// U|F = U, U|U = U
				else return this;
			}
			else if (this.value == TRUE) { 
				// T|? = T
				return this;
			} else {
				// F|? = ?
				return target;
			}
		}
		
		public MyBoolean and(MyBoolean target) {
			// T & ? = ?
			if (this.value == TRUE)
				return target;
			// F & ? = F
			else if (this.value == FALSE)
				return this;
			else {
				// U & F = F 
				if (target.value == FALSE) return target;
				// U & T = U, U & U = U
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
		
		MyBoolean eval(EvalArgs evalArgs) throws ParseException{
			MyBoolean flag = new MyBoolean(MyBoolean.FALSE);
			for(BooleanTerm bt : booleanTermList)
			{
				// DEBUG
				// System.out.print(bt.eval(evalArgs) + " ");
				flag = flag.or(bt.eval(evalArgs));
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
		
		MyBoolean eval(EvalArgs evalArgs) throws ParseException {
			MyBoolean flag = new MyBoolean(MyBoolean.TRUE);
			for(BooleanFactor bf : booleanFactorList)
			{
				// DEBUG
				// System.out.print(bf.eval(evalArgs) + " ");
				flag = flag.and(bf.eval(evalArgs));
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
		
		MyBoolean eval(EvalArgs evalArgs) throws ParseException {
			return (isNot ? booleanTest.eval(evalArgs).not() : booleanTest.eval(evalArgs));
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
		abstract MyBoolean eval(EvalArgs evalArgs) throws ParseException;

		abstract void print();
	}
	
	// <PARENTHESIZED BOOLEAN EXPRESSION> ::= <LEFT PAREN> <BOOLEAN VALUE EXPRESSION> <RIGHT PAREN>
	public static class ParenthesizedBooleanExpression extends BooleanTest{
		BooleanValueExpression booleanValueExpression;
		
		public void setBooleanValueExpression(BooleanValueExpression booleanValueExpression) {this.booleanValueExpression = booleanValueExpression;}
		
		@Override
		MyBoolean eval(EvalArgs evalArgs) throws ParseException {
			return booleanValueExpression.eval(evalArgs);
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
		abstract MyBoolean eval(EvalArgs evalArgs) throws ParseException;
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
		MyBoolean eval(EvalArgs evalArgs) throws ParseException {
			String lstr = leftValue.getValue(evalArgs);
			String rstr = rightValue.getValue(evalArgs);
			
			// any comparison with null returns unknown
			if(lstr == null || rstr == null)
			{
				return new MyBoolean(MyBoolean.UNKNOWN);
			}
			
			String ltype = lstr.substring(0, 1);
			String rtype = rstr.substring(0, 1);
			// if type mismatch,
			// case 21, WHERE_INCOMPARABLE_ERROR
			if(!ltype.equals(rtype))
			{
				System.out.println(DBMSException.getMessage(21, null));
				throw new ParseException("hohoho");
			}
			
			int cp = 0; // result of compare
			if(ltype.equals("1")) // integer compare
			{
				int lint = Integer.valueOf(lstr.substring(1));
				int rint = Integer.valueOf(rstr.substring(1));
				cp = lint-rint;
			}
			else // char, date compare 
			{
				// just use default comparer
				cp = lstr.compareTo(rstr);
			}
			
			// cp > 0, left > right, LB>,LEB>=,NEQ!= true, else false
			// cp = 0, left = right, EQ=,LEB>=,REB<= true, else false
			// cp < 0, left < right, RB<,REB<=,NEQ!= true, else false
			
			if(cp > 0)
			{
				if(compOp.equals(LB) || compOp.equals(LEB) ||  compOp.equals(NEQ))
					return new MyBoolean(MyBoolean.TRUE);
				else
					return new MyBoolean(MyBoolean.FALSE);
			} else if (cp == 0)
			{
				if(compOp.equals(EQ) || compOp.equals(LEB) || compOp.equals(REB))
					return new MyBoolean(MyBoolean.TRUE);
				else
					return new MyBoolean(MyBoolean.FALSE);
			} else { // cp < 0
				if(compOp.equals(RB) || compOp.equals(REB) || compOp.equals(NEQ))
					return new MyBoolean(MyBoolean.TRUE);
				else
					return new MyBoolean(MyBoolean.FALSE);
			}
		}
		
		@Override
		void print() {
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
		
		public String getValue(EvalArgs evalArgs) throws ParseException
		{
			// if compValue type, then just return
			if(OperandType == 0) return comparableValue;
			
			if(evalArgs.type == 1) // DELETE
			{
				if(tableName.equals("") || tableName.equals(evalArgs.table.name))
				{
					if(columnName == "") throw new ParseException("hohoho");
					int idx = evalArgs.columnNameList.indexOf(columnName);
					// case 23, WHERE_COLUMN_NOT_EXIST
					if(idx == -1) {
						System.out.println(DBMSException.getMessage(23, null));
						throw new ParseException("hohoho");
					}
					return evalArgs.valueList.get(idx);
				} else
				{
					// when tableName exists and not equal to given table
					// Case 22, WHERE_TABLE_NOT_SPECIFIED
					System.out.println(DBMSException.getMessage(22, null));
					throw new ParseException("hohoho");
				}
			}
			else // SELECT 
			{
				if(tableName.equals(""))
				{
					if(columnName == "") throw new ParseException("hohoho");
					
					// need table guessing
					boolean toxicFlag = true;
					for(String s : evalArgs.columnNameList)
					{
						if(s.contains("." + columnName) || s.equals(columnName))
						{
							columnName = s;
							toxicFlag = false;
							break;
						}
					}
					
					// if fail to guess
					// Case 24, WHERE_AMBIGUOUS_REFERENCE
					if(toxicFlag)
					{
						System.out.println(DBMSException.getMessage(24, null));
						throw new ParseException("hohoho");
					}
					
					int idx = evalArgs.columnNameList.indexOf(columnName);
					// case 23, WHERE_COLUMN_NOT_EXIST
					if(idx == -1) {
						System.out.println(DBMSException.getMessage(23, null));
						throw new ParseException("hohoho");
					}
					return evalArgs.valueList.get(idx);
				}
				else
				{
					if(columnName == "") throw new ParseException("hohoho");
					int idx = evalArgs.columnNameList.indexOf(tableName + "." + columnName);
					// case 23, WHERE_COLUMN_NOT_EXIST
					if(idx == -1) {
						System.out.println(DBMSException.getMessage(23, null));
						throw new ParseException("hohoho");
					}
					return evalArgs.valueList.get(idx);
				}
			}
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
		MyBoolean eval(EvalArgs evalArgs) throws ParseException {
			if(columnName == "") throw new ParseException("hohoho");
			
			if(evalArgs.type == 1) // DELETE
			{
				if(tableName.equals("") || tableName.equals(evalArgs.table.name))
				{
					if(columnName == "") throw new ParseException("hohoho");
					int idx = evalArgs.columnNameList.indexOf(columnName);
					// case 23, WHERE_COLUMN_NOT_EXIST
					if(idx == -1) {
						System.out.println(DBMSException.getMessage(23, null));
						throw new ParseException("hohoho");
					}

					if(evalArgs.valueList.get(idx) == null) {
						if(isNull) return new MyBoolean(MyBoolean.TRUE);
						else return new MyBoolean(MyBoolean.FALSE);
					} else {
						if(isNull) return new MyBoolean(MyBoolean.FALSE);
						else return new MyBoolean(MyBoolean.TRUE);
					}
				} else
				{
					// when tableName exists and not equal to given table
					// Case 22, WHERE_TABLE_NOT_SPECIFIED
					System.out.println(DBMSException.getMessage(22, null));
					throw new ParseException("hohoho");
				}
			}
			else // SELECT 
			{
				if(tableName.equals(""))
				{
					if(columnName == "") throw new ParseException("hohoho");
					
					// need table guessing
					boolean toxicFlag = true;
					for(String s : evalArgs.columnNameList)
					{
						if(s.contains("." + columnName) || s.equals(columnName))
						{
							columnName = s;
							toxicFlag = false;
							break;
						}
					}
					
					// if fail to guess
					// Case 24, WHERE_AMBIGUOUS_REFERENCE
					if(toxicFlag)
					{
						System.out.println(DBMSException.getMessage(24, null));
						throw new ParseException("hohoho");
					}
					
					int idx = evalArgs.columnNameList.indexOf(columnName);
					// case 23, WHERE_COLUMN_NOT_EXIST
					if(idx == -1) {
						System.out.println(DBMSException.getMessage(23, null));
						throw new ParseException("hohoho");
					}

					if(evalArgs.valueList.get(idx) == null) {
						if(isNull) return new MyBoolean(MyBoolean.TRUE);
						else return new MyBoolean(MyBoolean.FALSE);
					} else {
						if(isNull) return new MyBoolean(MyBoolean.FALSE);
						else return new MyBoolean(MyBoolean.TRUE);
					}
				}
				else
				{
					if(columnName == "") throw new ParseException("hohoho");
					int idx = evalArgs.columnNameList.indexOf(tableName + "." + columnName);
					// case 23, WHERE_COLUMN_NOT_EXIST
					if(idx == -1) {
						System.out.println(DBMSException.getMessage(23, null));
						throw new ParseException("hohoho");
					}
					
					if(evalArgs.valueList.get(idx) == null) {
						if(isNull) return new MyBoolean(MyBoolean.TRUE);
						else return new MyBoolean(MyBoolean.FALSE);
					} else {
						if(isNull) return new MyBoolean(MyBoolean.FALSE);
						else return new MyBoolean(MyBoolean.TRUE);
					}
				}
			}
		}
		@Override
		void print() {
			System.out.print("\t\t\t\t\t\t");
			if(this.isNull) System.out.print("IS NULL "); else System.out.print("IS NOT NULL "); 
			System.out.print(tableName + " ");
			System.out.print(columnName + "\n");
		}
	}
	
}
