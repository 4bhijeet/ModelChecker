package graphSearch;

public class Value {
	private Object v;
	private Datatype type;
	
	public Value(Object v, Datatype type) {
		this.v = v;
		this.type = type;
	}

	public Object getV() {
		return v;
	}

	public Datatype getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Value [v=" + v + ", type=" + type + "]";
	}
}
