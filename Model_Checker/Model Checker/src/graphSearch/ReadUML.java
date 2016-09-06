package graphSearch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.Vertex;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;

public class ReadUML {
	private String startStateName;
	private Map<String, Integer> stateNameMap;
	private Map<Integer, StateProp> statePropMap;
	private Map<Integer, Datatype> symbolTypeMap;
	private Map<Integer, Set<String>> valueMap;
	private Map<Datatype, Set<Integer>> varsTypeColl;
	
	private SymbolMap symbolMap;
	
	public String errorMsg;
	
	public ReadUML() {
		stateNameMap = new LinkedHashMap<String, Integer>();
		statePropMap = new TreeMap<Integer, StateProp>();
		
		symbolMap = new SymbolMap();
		symbolTypeMap = new TreeMap<Integer, Datatype>();
		valueMap = new HashMap<Integer, Set<String>>();
	}
	
	private void setValuesForBooleanType() {
		LinkedHashSet<String> valueSet = new LinkedHashSet<>();
		valueSet.add( Boolean.TRUE.toString() );
		valueSet.add( Boolean.FALSE.toString() );
		
		/*for(Map.Entry<Integer, Datatype> entry : symbolTypeMap.entrySet())
			if(entry.getValue() == Datatype.BOOLEAN) {
				valueMap.put(entry.getKey(), new LinkedHashSet<String>(valueSet));
			}*/	
		
		symbolTypeMap.forEach( 
				(symbolMapPosition, type) -> {
					if(type == Datatype.BOOLEAN) 
						valueMap.put(symbolMapPosition, new LinkedHashSet<String>(valueSet)); 
				});	//<-------------------- Java 8 Lambda expression
	}
	
	public boolean read(String filePath) {			
		URI uri = URI.createFileURI(filePath);
		
		int countState = -1;
		
		ResourceSet resourceSet = new ResourceSetImpl();
		
		UMLResourcesUtil.init(resourceSet);
		
		Resource resource = resourceSet.getResource(uri, true);
		
		for(TreeIterator<EObject> itr = resource.getAllContents(); itr.hasNext(); ){
			EObject current = itr.next();
						
			if(current instanceof Vertex) {
				Vertex vertex = (Vertex) current;
				System.out.println("Vertex : " + vertex.getName());	
					
				if(vertex instanceof Pseudostate) {
					Pseudostate pState = (Pseudostate) vertex;
					
					countState++;
					
					String pseudostateName = pState.getName();
					
					if(pseudostateName.compareToIgnoreCase("START") == 0)
						startStateName = pseudostateName;
					
					stateNameMap.put(pseudostateName, countState);
					StateProp stateProp = new StateProp(countState, pState, true);
					
					statePropMap.put(countState, stateProp);
					System.out.println("Visited state : " + stateProp);		//<------------------------
				}				
				else if(vertex instanceof State) {
					State state = (State) vertex;
					
					countState++;
					stateNameMap.put(state.getName(), countState);
					StateProp stateProp = new StateProp(countState, state, false);
				
					StringTokenizer st = new StringTokenizer(state.getName(), ",");
				
					System.out.println();
					while( st.hasMoreTokens() ) {
						String token = st.nextToken();
						System.out.print("token:" + token + ";");
					
						Scanner scanner = new Scanner(token);
						scanner.useDelimiter("=");
					
						String var = scanner.next().trim();
						System.out.print(" var=" + var);
					
						if(! symbolMap.contains(var) ) {
							if( symbolMap.isDone() ) {
								errorMsg = "ERROR: New variable \'" + var + "\' found which is not found in others, at state : \"" + state.getName() + "\"";
							
								scanner.close();
								return false;
							}
							symbolMap.add(var);
						}
						
						int symbolMapPosition = symbolMap.getPosition(var);
					
						if( scanner.hasNextInt() ) {
							Integer v = new Integer( scanner.nextInt() );
							Value value = new Value(v, Datatype.INTEGER);
							stateProp.addPropertyValue(symbolMapPosition, value);
						
							if(! symbolTypeMap.containsKey(symbolMapPosition) )	{ //if not in symbolTypeMap
								symbolTypeMap.put(symbolMapPosition, Datatype.INTEGER);
								
								TreeSet<String> set = new TreeSet<>();
								set.add( value.getV().toString() );
								
								valueMap.put(symbolMapPosition, set);
							}
							
							else if(symbolTypeMap.get(symbolMapPosition) != Datatype.INTEGER) {	//if type do not match
								errorMsg = "ERROR: Datatype mismatch. " + symbolTypeMap.get(symbolMapPosition) + " expected for variable \'" + var + "\', at state : \"" + state.getName() + "\"";
							
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
							
							if(! symbolTypeMap.containsKey(symbolMapPosition) ) {	//if not in symbolTypeMap
								symbolTypeMap.put(symbolMapPosition, Datatype.DOUBLE);
								
								TreeSet<String> set = new TreeSet<>();
								set.add( value.getV().toString() );
								
								valueMap.put(symbolMapPosition, set);
							}
							
							else if(symbolTypeMap.get(symbolMapPosition) != Datatype.DOUBLE) {	//if type do not match
								errorMsg = "ERROR: Datatype mismatch. " + symbolTypeMap.get(symbolMapPosition) + " expected for variable \'" + var + "\', at state : \"" + state.getName() + "\"";
							
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
							
							if(! symbolTypeMap.containsKey(symbolMapPosition) )	{ //if not in symbolTypeMap
								symbolTypeMap.put(symbolMapPosition, Datatype.FLOAT);
								
								TreeSet<String> set = new TreeSet<>();
								set.add( value.getV().toString() );
								
								valueMap.put(symbolMapPosition, set);
							}
							
							else if(symbolTypeMap.get(symbolMapPosition) != Datatype.FLOAT) {	//if type do not match
								errorMsg = "ERROR: Datatype mismatch. " + symbolTypeMap.get(symbolMapPosition) + " expected for variable \'" + var + "\', at state : \"" + state.getName() + "\"";
							
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
							
							if(! symbolTypeMap.containsKey(symbolMapPosition) ) //if not in symbolTypeMap
								symbolTypeMap.put(symbolMapPosition, Datatype.BOOLEAN);
							
							else if(symbolTypeMap.get(symbolMapPosition) != Datatype.BOOLEAN) {	//if type do not match
								errorMsg = "ERROR: Datatype mismatch. " + symbolTypeMap.get(symbolMapPosition) + " expected for variable \'" + var + "\', at state : \"" + state.getName() + "\"";
							
								scanner.close();
								return false;
							}					
							
							System.out.print(" BOOLEAN ");
						}
						else if( scanner.hasNext() ){
							String v = scanner.next().trim();
							Value value = new Value(v, Datatype.STRING);
							stateProp.addPropertyValue(symbolMapPosition, value);
							
							if(! symbolTypeMap.containsKey(symbolMapPosition) )	{ //if not in symbolTypeMap
								symbolTypeMap.put(symbolMapPosition, Datatype.STRING);
								
								TreeSet<String> set = new TreeSet<>();
								set.add( value.getV().toString() );
								
								valueMap.put(symbolMapPosition, set);
							}
							
							else if(symbolTypeMap.get(symbolMapPosition) != Datatype.STRING) {	//if type do not match
								errorMsg = "ERROR: Datatype mismatch. " + symbolTypeMap.get(symbolMapPosition) + " expected for variable \'" + var + "\', at state : \"" + state.getName() + "\"";
							
								scanner.close();
								return false;
							}
						
							valueMap.get(symbolMapPosition).add( value.getV().toString() );
							System.out.print(" STRING ");
						}
						else {	// no element after '='
							errorMsg = "ERROR: Invalid format, at state : \"" + state.getName() + "\"";
							
							scanner.close();
							return false;					
						}
						scanner.close();
					
					}
					System.out.println();				
				
					statePropMap.put(countState, stateProp);
					System.out.println("Visited state : " + stateProp);		//<------------------------
				
					if(! symbolMap.isDone() )
						symbolMap.addDone();
				}
			}
		}
		
		symbolMap.display();	//<---------------------------------------------
		
		setValuesForBooleanType();
		
		System.out.println("\n stateNameMap ...");
		for(Map.Entry<String, Integer> entry : stateNameMap.entrySet())
			System.out.println("name=" + entry.getKey() + ", code=" + entry.getValue());
		
		System.out.println("\nThe symbolTypes are ...");
		for(Map.Entry<Integer, Datatype> entry : symbolTypeMap.entrySet())
			System.out.println("position=" + entry.getKey() + ", type=" + entry.getValue());
		
		varsTypeColl = new HashMap<>();
		for(Map.Entry<Integer, Datatype> entry : symbolTypeMap.entrySet()) {
			int position = entry.getKey();
			Datatype type = entry.getValue();
			
			if(varsTypeColl.containsKey(type))
				varsTypeColl.get(type).add(position);
			else {
				HashSet<Integer> set = new HashSet<>();
				set.add(position);
				varsTypeColl.put(type, set);
			}
		}
		
		System.out.println("\n varsTypeColl ...");
		for(Map.Entry<Datatype, Set<Integer>> entry : varsTypeColl.entrySet())
			System.out.println("type=" + entry.getKey() + ", positions=" + entry.getValue());
		
		
		System.out.println("\nThe values are ...");
		for(Map.Entry<Integer, Set<String>> entry : valueMap.entrySet())
			System.out.println("position=" + entry.getKey() + ", values=" + entry.getValue());
		
		return true;
	}
	
	public String getStartStateName() {
		return startStateName;
	}

	public Map<Integer, StateProp> getStatePropMap() {
		return statePropMap;
	}
	
	public Map<String, Integer> getStateNameMap() {
		return stateNameMap;
	}

	public Map<Integer, Datatype> getSymbolTypeMap() {
		return symbolTypeMap;
	}
	
	public Map<Integer, Set<String>> getValueMap() {
		return valueMap;
	}

	public Map<Datatype, Set<Integer>> getVarsTypeColl() {
		return varsTypeColl;
	}

	public SymbolMap getSymbolMap() {
		return symbolMap;
	}
}
