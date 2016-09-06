package matrix;

import graphSearch.Datatype;
import graphSearch.StateProp;
import graphSearch.SymbolMap;
import graphSearch.Value;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class RuntimeMtx {
	private TreeMap<Integer, String> indexToStateNameMap;
	private LinkedHashMap<String, Integer> stateNameToIndexMap;
	public int[][] adjMatrix;
	public int adjMatrixSize;
	
	private Map<Integer, StateProp> statePropMap;
	private Map<Integer, Set<String>> valueMap;
	
	private SymbolMap symbolMap;
	
	private String varsetObjects;
	
	public String errorMsg, errorMsgCompatibility;
	
	public RuntimeMtx(SymbolMap symbolMap) {
		this.symbolMap = symbolMap;
	}	
	
	/* only call this method from within readFile() or after calling readFile(), since it needs 'varsetObjects' which is initialized only in readFile() method	*/
	private boolean checkCompatibilityWithDesignTime() {
		String[] varsArr = varsetObjects.split(",");
		
		LinkedHashSet<String> wrongPositionVars = new LinkedHashSet<>();
		LinkedHashSet<String> newVars = new LinkedHashSet<>();
		boolean hasError = false;		
		for(int i=0; i<varsArr.length; i++) {
			int position = i + 1;
			if( symbolMap.contains(varsArr[i]) ) {
				if(symbolMap.getPosition(varsArr[i]) != position) {
					if(hasError == false) hasError = true;
					wrongPositionVars.add( varsArr[i] );
				}
			}
			else {	// if there is no such variable in the design
				if(hasError == false) hasError = true;
				newVars.add( varsArr[i] );
			}
		}
		
		if(hasError) {
			if((! wrongPositionVars.isEmpty()) && (! newVars.isEmpty())) {
				StringBuffer sb = new StringBuffer("ERROR: The Runtime Model is not compatible with the Design-time Model.");
				sb.append("\nVariable(s) ").append(wrongPositionVars).append(" found at a different position than in the design.");
				sb.append("\nNew variable(s) ").append(newVars).append(" found which is not found in the design.");
				
				errorMsgCompatibility = sb.toString();
			}
			else if(! wrongPositionVars.isEmpty())
				errorMsgCompatibility = "ERROR: The Runtime Model is not compatible with the Design-time Model. Variable(s) \'" + wrongPositionVars + "\' found at a different position than in the design";
			else if(! newVars.isEmpty())
				errorMsgCompatibility = "ERROR: The Runtime Model is not compatible with the Design-time Model. New variable(s) \'" + newVars + "\' found which is not found in the design";
			
			return false;
		}
		else
			return true;
	}
	
	public boolean readFile(String fileName, List<String> vars, String startStateName) {
		indexToStateNameMap = new TreeMap<>();
		stateNameToIndexMap = new LinkedHashMap<>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));	
			
			String line;
			
			/*	read varsetObjects; the first line	*/
			if((line=br.readLine()) != null) {
				varsetObjects = line.split("=")[1];
				
				if(checkCompatibilityWithDesignTime() == false) {
					br.close();
					return false;
				}
			}
			
			/*	read stateNames	*/
			while((line=br.readLine()) != null) {
				if(line.compareTo("$$$") == 0)	// indicates start of Adjacency Matrix, so break
					break;
				
				String[] entry = line.split("-");	//split into two: 0) index 1)state_name
				int index = Integer.parseInt( entry[0].trim() );
				
				String name = null;
				if(entry[1].compareToIgnoreCase("START") == 0)
					name = startStateName;
				else {
					String[] portions = entry[1].split("\\s*,\\s*");
					/*for(String str : portions)
						System.out.println("str : #" + str + "#");*/
					int i=0;
					StringJoiner sj = new StringJoiner(", ");
					for(String var : vars) {
						//System.out.println(var + "=" + portions[i]);	//<------------
						sj.add(var + "=" + portions[i]);								
						i++;
					}
					name = sj.toString();
				}	
				
				indexToStateNameMap.put(index, name);
				stateNameToIndexMap.put(name, index);
				
				//System.out.println("index : " + index + ", name : " + name);
				
				/*System.out.println("-------NEXT LINE--------------");*/
			}
			
			adjMatrixSize = indexToStateNameMap.size();
			adjMatrix = new int[adjMatrixSize][adjMatrixSize];
			
			/*	read adjacency matrix	*/
			for(int i=0; (line=br.readLine()) != null ; i++) {
				if(i >= adjMatrixSize)
					break;
				
				//System.out.println(line);
				String cells[] = line.split(" ");
				
				if(cells.length != adjMatrixSize) {
					System.out.println("\t ERROR: Adjacency matrix row" + i + " has inconsistent length");
					break;
				}					
				
				for(int j=0; j<adjMatrixSize; j++) {
					int value = Integer.parseInt( cells[j] );
					adjMatrix[i][j] = value;
				}
			}
			
			br.close();
		} catch (Exception e1) {
			System.out.println("Exception while reading file : " + e1);
		}
		
		return true;
	}
	
	public void display() {
		System.out.println("\n\n************** RuntimeMtx ******************");
		
		System.out.println("\n indexToStateNameMap ...\n");
		for(Map.Entry<Integer, String> entry : indexToStateNameMap.entrySet()) 
			System.out.println(entry.getKey() + " --> " + entry.getValue());
		
		System.out.println("\n stateNameToIndexMap ...\n");
		for(Map.Entry<String, Integer> entry : stateNameToIndexMap.entrySet()) 
			System.out.println(entry.getKey() + " --> " + entry.getValue());
		
		System.out.println("\nAdjacency Matrix ...\n");
		for(int i=0; i<adjMatrixSize; i++) {
			StringJoiner sj = new StringJoiner(" ");
			for(int j=0; j<adjMatrixSize; j++)
				sj.add( String.valueOf( adjMatrix[i][j] ) );
			
			System.out.println( sj.toString() );
		}
	}
	
	public Set<String> getNeighbours(String stateName) {
		HashSet<String> neighbours = new HashSet<>();
		
		int indexRT = stateNameToIndexMap.get(stateName);
		
		for(int i=0; i<adjMatrixSize; i++)	// for all
			if(adjMatrix[indexRT][i] == 1) {	// transitions in runtime from stateName
				String neighStateName = indexToStateNameMap.get(i);
				
				neighbours.add(neighStateName);
			}
		
		return neighbours;
	}
	
	public Set<String> getParents(String stateName) {
		HashSet<String> parents = new HashSet<>();
		
		int indexRT = stateNameToIndexMap.get(stateName);
		
		for(int i=0; i<adjMatrixSize; i++)	// for all
			if(adjMatrix[i][indexRT] == 1) {	// transitions in runtime to stateName
				String parentStateName = indexToStateNameMap.get(i);
				
				parents.add(parentStateName);
			}
		
		return parents;
	}
	
	public boolean generateStateMaps(Map<Integer, Datatype> symbolTypeMap, Map<Integer, Set<String>> valueMapFromDT) {
		System.out.println("\n$$$$$$$$$$$$$$$$$$ Loading Runtime Model : generateStatePropMap $$$$$$$$$$$$$$$$$$$$");
		
		statePropMap = new TreeMap<Integer, StateProp>();			
		
		valueMap = new HashMap<Integer, Set<String>>( valueMapFromDT );
		valueMap.values().stream()
			.parallel()
			.forEach( (valueSet) -> valueSet.clear() );		//<----------- Java 8 Lambda expression
		
		for(Map.Entry<String, Integer> entry : stateNameToIndexMap.entrySet()) {
			String stateName = entry.getKey();
			int index = entry.getValue();
			
			Set<String> neighbours = getNeighbours(stateName);
			Set<String> parents = getParents(stateName);
			
			if(stateName.compareToIgnoreCase("START") == 0) {
				StateProp stateProp = new StateProp(index, stateName, neighbours, parents, true);				
				statePropMap.put(index, stateProp);
			}
			else {
				StateProp stateProp = new StateProp(index, stateName, neighbours, parents, false);
				
				StringTokenizer st = new StringTokenizer(stateName, ",");
				
				System.out.println();
				while( st.hasMoreTokens() ) {
					String token = st.nextToken();
					System.out.print("token:" + token + ";");
				
					Scanner scanner = new Scanner(token);
					scanner.useDelimiter("=");
				
					String var = scanner.next().trim();
					System.out.print(" var=" + var);
					
					int symbolMapPosition = symbolMap.getPosition(var);
				
					if( scanner.hasNextInt() ) {
						Integer v = new Integer( scanner.nextInt() );
						Value value = new Value(v, Datatype.INTEGER);
						stateProp.addPropertyValue(symbolMapPosition, value);
						
						if(symbolTypeMap.get(symbolMapPosition) != Datatype.INTEGER) {	//if type do not match
							errorMsg = "ERROR: Datatype mismatch. " + symbolTypeMap.get(symbolMapPosition) + " expected for variable \'" + var + "\', at state : \"" + stateName + "\"";
						
							scanner.close();							
							return false;
						}
						
						valueMap.get(symbolMapPosition).add( value.getV().toString() );
						System.out.print(" INT ");
					}						
					else if( scanner.hasNextDouble() ) {
						Double v = new Double( scanner.nextDouble() );
						Value value = new Value(v, Datatype.DOUBLE);
						stateProp.addPropertyValue(symbolMapPosition, value);
						
						if(symbolTypeMap.get(symbolMapPosition) != Datatype.DOUBLE) {	//if type do not match
							errorMsg = "ERROR: Datatype mismatch. " + symbolTypeMap.get(symbolMapPosition) + " expected for variable \'" + var + "\', at state : \"" + stateName + "\"";
						
							scanner.close();							
							return false;
						}
					
						valueMap.get(symbolMapPosition).add( value.getV().toString() );
						System.out.print(" DOUBLE ");
					}
					else if( scanner.hasNextFloat() ) {
						Float v = new Float( scanner.nextFloat() );
						Value value = new Value(v, Datatype.FLOAT);
						stateProp.addPropertyValue(symbolMapPosition, value);
						
						if(symbolTypeMap.get(symbolMapPosition) != Datatype.FLOAT) {	//if type do not match
							errorMsg = "ERROR: Datatype mismatch. " + symbolTypeMap.get(symbolMapPosition) + " expected for variable \'" + var + "\', at state : \"" + stateName + "\"";
						
							scanner.close();
							return false;
						}
					
						valueMap.get(symbolMapPosition).add( value.getV().toString() );
						System.out.print(" FLOAT ");
					}						
					else if( scanner.hasNextBoolean() ) {
						Boolean v = new Boolean( scanner.nextBoolean() );
						Value value = new Value(v, Datatype.BOOLEAN);
						stateProp.addPropertyValue(symbolMapPosition, value);
						
						if(symbolTypeMap.get(symbolMapPosition) != Datatype.BOOLEAN) {	//if type do not match
							errorMsg = "ERROR: Datatype mismatch. " + symbolTypeMap.get(symbolMapPosition) + " expected for variable \'" + var + "\', at state : \"" + stateName + "\"";
						
							scanner.close();
							return false;
						}					
						
						System.out.print(" BOOLEAN ");
					}
					else if( scanner.hasNext() ){
						String v = scanner.next().trim();
						Value value = new Value(v, Datatype.STRING);
						stateProp.addPropertyValue(symbolMapPosition, value);
						
						if(symbolTypeMap.get(symbolMapPosition) != Datatype.STRING) {	//if type do not match
							errorMsg = "ERROR: Datatype mismatch. " + symbolTypeMap.get(symbolMapPosition) + " expected for variable \'" + var + "\', at state : \"" + stateName + "\"";
						
							scanner.close();
							return false;
						}
					
						valueMap.get(symbolMapPosition).add( value.getV().toString() );
						System.out.print(" STRING ");
					}
					else {	// no element after '='
						errorMsg = "ERROR: Invalid format, at state : \"" + stateName + "\"";
						
						scanner.close();
						return false;					
					}
					scanner.close();
				
				}
				System.out.println();				
			
				statePropMap.put(index, stateProp);
			}
		}
		
		System.out.println("\nThe values are ...");
		for(Map.Entry<Integer, Set<String>> entry : valueMap.entrySet())
			System.out.println("position=" + entry.getKey() + ", values=" + entry.getValue());
		
		return true;
	}
	
	public Set<String> findNonIntermediateChildren(String stateName, HashSet<String> intermediateStates) {
		int indexRT = stateNameToIndexMap.get(stateName);
		HashSet<Integer> visited = new HashSet<>();
		
		HashSet<String> foundStates = new HashSet<>();
		
		dfs(indexRT, visited, intermediateStates, foundStates);
		
		return foundStates;
	}
	
	public void dfs(int indexRT, HashSet<Integer> visited, HashSet<String> intermediateStates, HashSet<String> foundStates) {
		visited.add(indexRT);
		
		for(int i=0; i<adjMatrixSize; i++)
			if(adjMatrix[indexRT][i] == 1) {
				if( visited.contains(i) )
					continue;
				String neighStateName = indexToStateNameMap.get(i);
				/*if( intermediateStates.contains(neighStateName) ) {
					foundStates.add(neighStateName);
					
					if(intermediateStates.containsAll(foundStates))
						return;
				}*/
				if(! intermediateStates.contains(neighStateName))
					foundStates.add(neighStateName);
				else
					dfs(i, visited, intermediateStates, foundStates);
			}
	}
	
	public Set<String> findNonIntermediateParents(String stateName, HashSet<String> intermediateStates) {
		int indexRT = stateNameToIndexMap.get(stateName);
		
		HashSet<Integer> visited = new HashSet<>();
		visited.add(indexRT);
		
		HashSet<String> foundStates = new HashSet<>();
		
		int rowOfSTATE = indexRT;
		revDfs(rowOfSTATE, visited, intermediateStates, foundStates);
		
		//System.out.println("\trevDFS(" + stateName + ")");
		
		return foundStates;
	}
	
	public void revDfs(int row, HashSet<Integer> visited, HashSet<String> intermediateStates, HashSet<String> foundStates) {
		visited.add(row);
		
		int colOfROW = row;
		for(int i=0; i<adjMatrixSize; i++)
			if(adjMatrix[i][colOfROW] == 1) {	// look in col(row)
				int rowOfCOL = i;
				if( visited.contains(rowOfCOL) )
					continue;
				
				String parentName = indexToStateNameMap.get(rowOfCOL);
				
				//System.out.println(" \t revDFS --> " + parentName);
				
				if(! intermediateStates.contains(parentName))
					foundStates.add(parentName);
				else
					revDfs(rowOfCOL, visited, intermediateStates, foundStates);
			}
	}
	
	public Set<String> getStates() {
		return stateNameToIndexMap.keySet();
	}

	public TreeMap<Integer, String> getIndexToStateNameMap() {
		return indexToStateNameMap;
	}

	public LinkedHashMap<String, Integer> getStateNameToIndexMap() {
		return stateNameToIndexMap;
	}

	public Map<Integer, StateProp> getStatePropMap() {
		return statePropMap;
	}

	public Map<Integer, Set<String>> getValueMap() {
		return valueMap;
	}
}
