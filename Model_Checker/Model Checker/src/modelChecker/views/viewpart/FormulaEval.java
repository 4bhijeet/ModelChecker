package modelChecker.views.viewpart;

import java.util.ArrayList;

class FormulaEval {
	static Form form1;
	ArrayList<Integer> result;
	public static ArrayList<Integer> eval() {
		/*form -> sexpr
         *temp -> ( AX | AF | AG | EX | EF | EG) '[' sexpr ']' 
         *sexpr -> expr [ '-->' sexpr ]
         *expr -> term [ '\/' expr ]
         *term -> fact [ '/\' term ]
         *sfact -> '(' ] sexpr  ')' | fact
         *fact -> [ '~' ] ( p | temp | sexpr )
         */
		form1 = new Form();
		return form1.result;
	}
}

class Form { //form -> sexpr
	Sexpr sexpr;
	ArrayList<Integer> result;
	public Form() {
		Lexer.lex();//Read first token
		sexpr = new Sexpr();
		result = sexpr.result;
	}
}

class Temp { //temp -> ( AX | AF | AG | EX | EF | EG) '[' sexpr ']'
	Sexpr sexpr;
	ArrayList<Integer> result;
	public Temp() {
		switch (Lexer.nextToken) {
		case Token.KEY_AX:
			Lexer.lex();//Consume AX'['
			sexpr = new Sexpr();
			result = Function.basicAX(sexpr.result);
			Lexer.lex();//Consume ']'
			break;
		case Token.KEY_AF:
			Lexer.lex();//Consume AF'['
			sexpr = new Sexpr();
			result = Function.basicAF(sexpr.result);
			Lexer.lex();//Consume ']'
			break;
		case Token.KEY_AG:
			Lexer.lex();//Consume AG'['
			sexpr = new Sexpr();
			result = Function.basicAG(sexpr.result);
			Lexer.lex();//Consume ']'
			break;
		case Token.KEY_EX:
			Lexer.lex();//Consume EX'['
			sexpr = new Sexpr();
			result = Function.basicEX(sexpr.result);
			Lexer.lex();//Consume ']'
			break;
		case Token.KEY_EF:
			Lexer.lex();//Consume EF'['
			sexpr = new Sexpr();
			result = Function.basicEF(sexpr.result);
			Lexer.lex();//Consume ']'
			break;
		case Token.KEY_EG:
			Lexer.lex();//Consume EG'['
			sexpr = new Sexpr();
			result = Function.basicEG(sexpr.result);
			Lexer.lex();//Consume ']'
			break;
		}
	}
}

class Sexpr { //sexpr -> expr [ '-->' sexpr ]
	Expr expr;
	Sexpr sexpr;
	ArrayList<Integer> result;
	public Sexpr(){
		expr = new Expr();
		if(Lexer.nextToken == Token.IMPLIES_OP) {
			Lexer.lex();//Consume '-->'
			sexpr = new Sexpr();
			result = Function.setImplies(expr.result, sexpr.result);
		} else {
			result = expr.result;
		}
	}
}
class Expr { //expr -> term [ '\/' expr ]
	Term term;
	Expr expr;
	ArrayList<Integer> result;
	public Expr() {
		term = new Term();
		if(Lexer.nextToken == Token.OR_OP) {
			Lexer.lex();//Consume '\/'
			expr = new Expr();
			result = Function.setOr(term.result, expr.result);
		} else {
			result = term.result;
		}
	}
}

class Term { //term -> sfact [ '/\' term ]
	Sfact sfact;
	Term term;
	ArrayList<Integer> result;
	public Term() {
		sfact = new Sfact();
		if(Lexer.nextToken == Token.AND_OP) {
			Lexer.lex();//Consume '/\'
			term = new Term();
			result = Function.setAnd(sfact.result, term.result);
		} else {
			result = sfact.result;
		}
	}
}

class Sfact { //sfact -> [ '(' ] fact [ ')' ]
	//sfact -> '(' ] sexpr  ')' | fact
	Fact fact;
	Sexpr sexpr;
	ArrayList<Integer> result;
	public Sfact() {
		if(Lexer.nextToken == Token.LEFT_PAREN) {
			Lexer.lex();//Consume '('
			sexpr = new Sexpr();
			Lexer.lex();//Consume ')'
			result = sexpr.result;
		} else {
			fact = new Fact();
			result = fact.result;
		}
	}
}

class Fact { //fact -> [ '~' ] ( p | temp | sexpr )
	Temp temp;
	Sexpr sexpr;
	ArrayList<Integer> result;
	public Fact() {
		if(Lexer.nextToken == Token.QUIT) {
			System.out.println("Inside Quit");
			return;
		}
		if(Lexer.nextToken == Token.NOT_OP) {
			Lexer.lex();//Consume '~'
			if(Lexer.nextToken == Token.ATOMIC_PROPOSITION) {
				int position = NestedModel.properties_current.indexOf(Lexer.atomicProposition);
				if(position == -1) {
					System.out.println("Invalid Atomic Proposition: " + Lexer.atomicProposition);
					System.exit(0);
				}
				result = new ArrayList();
				result.addAll(NestedModel.propertiesTrueAt_current[position]);
				result = Function.setNot(result);
				Lexer.lex();
			} else if(Lexer.nextToken == Token.KEY_AF || Lexer.nextToken == Token.KEY_EF ||
					Lexer.nextToken == Token.KEY_AG || Lexer.nextToken == Token.KEY_EG || 
					Lexer.nextToken == Token.KEY_AX || Lexer.nextToken == Token.KEY_EX) {
				temp = new Temp();
				result = Function.setNot(temp.result);
			} else {
				sexpr = new Sexpr();
				result = Function.setNot(sexpr.result);
			}
		} else {
			if(Lexer.nextToken == Token.ATOMIC_PROPOSITION) {
				int position = NestedModel.properties_current.indexOf(Lexer.atomicProposition);
				if(position == -1) {
					System.out.println("Invalid Atomic Proposition: " + Lexer.atomicProposition);
					//System.exit(0);
				}
				result = new ArrayList();
				result.addAll(NestedModel.propertiesTrueAt_current[position]);
				Lexer.lex();
			} else if(Lexer.nextToken == Token.KEY_AF || Lexer.nextToken == Token.KEY_EF ||
					Lexer.nextToken == Token.KEY_AG || Lexer.nextToken == Token.KEY_EG || 
					Lexer.nextToken == Token.KEY_AX || Lexer.nextToken == Token.KEY_EX) {
				temp = new Temp();
				result = temp.result;
			} else {
				sexpr = new Sexpr();
				result = sexpr.result;
			}
		}
	}
}