package matrix;

import graphSearch.StateProp;

import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

public class DesigntimeMtx {
	private Map<String, Integer> stateNameToCodeMap;
	private Map<Integer, String> codeToStateNameMap;
	
	private int totalTransitions;
	
	public int[][] adjMatrix;
	public int adjMatrixSize;
	
	public DesigntimeMtx(Map<String, Integer> stateNameMap, Map<Integer, StateProp> statePropMap) {
		totalTransitions = 0;
		
		stateNameToCodeMap = stateNameMap;
		Map<Integer, StateProp> codeToStatePropMap = statePropMap;
		
		codeToStateNameMap = new TreeMap<Integer, String>();
		
		adjMatrixSize = stateNameToCodeMap.size();
		adjMatrix = new int[adjMatrixSize][adjMatrixSize];
		
		/* initializing adjMatrix to zeroes	*/
		for(int i=0; i<adjMatrixSize; i++)
			for(int j=0; j<adjMatrixSize; j++)
				adjMatrix[i][j] = 0;
		
		//System.out.println("\n\nThe transitions are ...");
		for(int code : codeToStatePropMap.keySet()) {
			int row = code;
			
			StateProp srcState = codeToStatePropMap.get(code);
			String srcStateName = srcState.getName();
			
			codeToStateNameMap.put(code, srcStateName);
			
			//StringBuffer sb = new StringBuffer(srcStateName);
			//sb.append("(").append(row).append(")");
			//sb.append(" : ");			
			
			for(String destStateName : srcState.getNeighbours()) {				
				int col = stateNameToCodeMap.get(destStateName);
				
				//sb.append(destStateName).append("(").append(col).append(")").append(";");
				
				adjMatrix[row][col] = 1;
				totalTransitions++;
			}
			
			//System.out.println("\t" + sb.toString());
		}
	}
	
	public void display() {	
		System.out.println("\n\n************** DesigntimeMtx ******************");
		
		System.out.println("\n stateNameToCodeMap ...\n");
		for(Map.Entry<String, Integer> entry : stateNameToCodeMap.entrySet()) 
			System.out.println(entry.getKey() + " --> " + entry.getValue());
		
		System.out.println("\n codeToStateNameMap ...\n");
		for(Map.Entry<Integer, String> entry : codeToStateNameMap.entrySet()) 
			System.out.println(entry.getKey() + " --> " + entry.getValue());		
		
		System.out.println("\nAdjacency Matrix ...\n");
		for(int i=0; i<adjMatrixSize; i++) {
			StringJoiner sj = new StringJoiner(" ");
			for(int j=0; j<adjMatrixSize; j++)
				sj.add( String.valueOf( adjMatrix[i][j] ) );
			
			System.out.println( sj.toString() );
		}
	}

	public Map<String, Integer> getStateNameToIndexMap() {
		return stateNameToCodeMap;
	}

	public Map<Integer, String> getIndexToStateNameMap() {
		return codeToStateNameMap;
	}

	public int getTotalTransitions() {
		return totalTransitions;
	}
}
