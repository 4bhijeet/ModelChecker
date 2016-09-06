package modelChecker.views.viewpart;

class Token {
	public static final int NOT_OP = 0;
	public static final int AND_OP = 1;
	public static final int OR_OP = 2;
	public static final int IMPLIES_OP = 3;
	public static final int LEFT_PAREN = 4;
	public static final int RIGHT_PAREN = 5;
	public static final int RIGHT_SQUARE = 6;
	public static final int KEY_AX = 7;
	public static final int KEY_AF = 8;
	public static final int KEY_AG = 9;
	public static final int KEY_EX = 10;
	public static final int KEY_EF = 11;
	public static final int KEY_EG = 12;
	public static final int ATOMIC_PROPOSITION = 13;
	public static final int QUIT = 14;

	private static String[] lexemes = { 
		"~", "/\\", "\\/", "-->", "(", ")", "]", "AX[", "AF[", "AG[", "EX[", "EF[", "EG[" ,
		"ATOMIC_PROPOSITION" , "^"
	};

	public static String toString(int i) {
		if (i < 0 || i > 14)
			return "";
		else
			return lexemes[i];
	}
}
