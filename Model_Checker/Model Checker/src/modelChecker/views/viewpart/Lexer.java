package modelChecker.views.viewpart;

class Lexer {

    static public char nextch = ' ';
	static public char ch = ' ';
	static public char ident = ' ';
	static public int nextToken;
	static public char nextChar;
	static public int intValue;
    static public String atomicProposition = "";
    static public Buffer buffer;
    
    public static void initialise(String formula){
    	buffer = new Buffer(formula);
    }
    
	public static int lex() {
		atomicProposition = "";
		ch = buffer.getChar();
		if (Character.isLetter(ch)) {
			ident = ch;
			ch = buffer.getChar();
            nextch = buffer.getChar();
			if (ident == 'A' && ch == 'X' && nextch == '[') {
				nextToken = Token.KEY_AX;
			} else if (ident == 'A' && ch == 'F' && nextch == '[') {
				nextToken = Token.KEY_AF;
			} else if (ident == 'A' && ch == 'G' && nextch == '[') {
				nextToken = Token.KEY_AG;
			} else if (ident == 'E' && ch == 'X' && nextch == '[') {
					nextToken = Token.KEY_AX;
			} else if (ident == 'E' && ch == 'F' && nextch == '[') {
				nextToken = Token.KEY_EF;
			} else if (ident == 'E' && ch == 'G' && nextch == '[') {
				nextToken = Token.KEY_EG;
			} else {
                buffer.goBack();
                buffer.goBack();
                ch = ident;
                nextToken = Token.ATOMIC_PROPOSITION;
                while(ch != '^' && ch != '~' && ch != '/' && ch != '\\' && ch != '-' && ch != '(' && ch != ')' && ch != ']'){
                    atomicProposition += Character.toString(ch);
                    ch = buffer.getChar();
                }
                buffer.goBack();
            }
		} else {
			nextChar = ch;
			switch (ch) {
			case '^':
				nextToken = Token.QUIT;
				break;
			case '~':
				nextToken = Token.NOT_OP;
				break;
			case '/':
				nextToken = Token.AND_OP;
				ch = buffer.getChar();
				break;
			case '\\':
				nextToken = Token.OR_OP;
				ch = buffer.getChar();
				break;
			case '-':
				nextToken = Token.IMPLIES_OP;
				ch = buffer.getChar();
                ch = buffer.getChar();
				break;
			case '(':
				nextToken = Token.LEFT_PAREN;
				break;
			case ')':
				nextToken = Token.RIGHT_PAREN;
				break;
			case ']':
				nextToken = Token.RIGHT_SQUARE; 
				break;
			default:
				error("Illegal character " + ch);
				break;
			}
		}
		return nextToken;
	} // lex

	public Character identifier() {
		return ident;
	} // letter

	public static void error(String msg) {
		System.err.println(msg);
		System.exit(1);
	} // error

}