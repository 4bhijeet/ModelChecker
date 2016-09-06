package graphSearch;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class SymbolMap {
	private Map<String, Integer> map;
	private int positionCount;
	
	private boolean done;	// to indicate whether building symbolMap is done
	
	public SymbolMap() {
		map = new TreeMap<String, Integer>();
		positionCount = 0;
		done = false;
	}
	
	public void add(String varName) {
		map.put(varName, ++positionCount);
	}
	
	/*	to indicate that nothing more can be added	*/
	public void addDone() {
		done = true;
	}
	
	public boolean contains(String varName) {
		return map.containsKey(varName);
	}

	public int getPosition(String varName) {
		return map.get(varName);
	}

	public boolean isDone() {
		return done;
	}
	
	public Map<String, Integer> getMap() {
		return map;
	}

	public Set<String> keySet() {
		return map.keySet();
	}
	
	public void clear() {
		map.clear();
	}
	
	public void display() {
		System.out.println("\nThe symbols are ...");
		for(Map.Entry<String, Integer> entry : map.entrySet())
			System.out.println("variable=" + entry.getKey() + ", position=" + entry.getValue());
	}
}
