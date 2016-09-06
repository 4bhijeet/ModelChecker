package graphSearch;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Vertex;

public class StateProp {
	private int code;
	private String name;
	private Set<String> neighbours;
	private Set<String> parents;
	private Map<Integer, Value> properties;
	
	private boolean pseudoState;
	
	public StateProp(int code, Vertex vertex, boolean pseudoState) {
		this.code = code;
		
		name = vertex.getName();
		
		neighbours = new HashSet<String>();
		/* finding out the neighbours of vertex	*/		
		for(Transition trans : vertex.getOutgoings()) {
			String destStateName = trans.getTarget().getName();
			
			neighbours.add(destStateName);
		}
		
		parents = new HashSet<String>();
		/* finding out the parents of vertex	*/		
		for(Transition trans : vertex.getIncomings()) {
			String srcStateName = trans.getSource().getName();
			
			parents.add(srcStateName);
		}
		
		this.pseudoState = pseudoState;
		
		if(! pseudoState )
			properties = new TreeMap<Integer, Value>();
	}
	
	public StateProp(int code, String stateName, Set<String> neighbours, Set<String> parents, boolean pseudoState) {
		this.code = code;
		
		name = stateName;
		
		this.neighbours = neighbours;
		
		this.parents = parents;
		
		this.pseudoState = pseudoState;
		
		if(! pseudoState )
			properties = new TreeMap<Integer, Value>();
	}
	
	public void addPropertyValue(int position, Value value) {
		properties.put(position, value);
	}
	
	public Value getPropertyValue(int position) {
		return properties.get(position);
	}

	public String getName() {
		return name;
	}

	public int getCode() {
		return code;
	}

	public Set<String> getNeighbours() {
		return neighbours;
	}

	public Set<String> getParents() {
		return parents;
	}

	public boolean isPseudoState() {
		return pseudoState;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("StateProp [code=" + code);
		if(! pseudoState ) {
			sb.append(", properties : ");
		
			for(Map.Entry<Integer, Value> entry : properties.entrySet())
				sb.append( entry.getKey() ).append(" -> ").append( entry.getValue() ).append("\t");
			sb.append("]");
		}
		else
			sb.append(", pseudostate=").append(pseudoState).append("]");
		
		sb.append("; name : ").append(name);
		
		return sb.toString();
	}
}