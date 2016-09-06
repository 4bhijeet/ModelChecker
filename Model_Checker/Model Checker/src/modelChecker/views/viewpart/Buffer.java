package modelChecker.views.viewpart;

class Buffer {

	private String line = "";
	private int position = -1;

	public Buffer(String formula) {
		this.line = formula;
	} // Buffer

	public char getChar() {
		position++;
		if (position >= line.length()) {
			return '^';
		}
		return line.charAt(position);
	}
	
    public void goBack() {
        position--;
    }

} // class Buffer
