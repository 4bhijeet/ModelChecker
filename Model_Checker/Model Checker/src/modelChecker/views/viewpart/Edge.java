package modelChecker.views.viewpart;

public class Edge {
	private int source;
	private int destination;
	
	public Edge(int source, int destination) {
		this.source = source;
		this.destination = destination;
	}

	public int getSource() {
		return source;
	}

	public int getDestination() {
		return destination;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + destination;
		result = prime * result + source;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		if (destination != other.destination)
			return false;
		if (source != other.source)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Edge [source=" + source + ", destination=" + destination + "]";
	}
}
