package modelChecker.views.viewpart;

import static util.PropertyUI.EQUALS;
import static util.PropertyUI.GREATER_THAN;
import static util.PropertyUI.GREATER_THAN_EQUALS;
import static util.PropertyUI.LESS_THAN;
import static util.PropertyUI.LESS_THAN_EQUALS;
import static util.PropertyUI.NOT_EQUALS;
import static util.PropertyUI.RANGE_K;
import graphSearch.Datatype;
import graphSearch.StateProp;
import graphSearch.SymbolMap;
import graphSearch.Value;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import util.PropertyUI;
import util.PropertyWrapper;
import util.Range;

public class ModelCheck {	
	private ModelCheckUI modelCheckUI;
	private Display display;
	
	private Map<Integer, StateProp> statePropMap;
	private Map<String, Integer> stateNameMap;
	private Map<Integer, Datatype> symbolTypeMap;
	private SymbolMap symbolMap;
	
	private LinkedList<PropertyWrapper> propsList_first;
	private LinkedList<PropertyWrapper> propsList_second;

	public ModelCheck(ModelCheckUI modelCheckUI, Display display, Map<Integer, StateProp> statePropMap, Map<String, Integer> stateNameMap, Map<Integer, Datatype> symbolTypeMap, SymbolMap symbolMap) {
		this.modelCheckUI = modelCheckUI;
		this.display = display;
		this.statePropMap = statePropMap;
		this.stateNameMap = stateNameMap;
		this.symbolTypeMap = symbolTypeMap;
		this.symbolMap = symbolMap;
	}
	
	public void loadProperties(LinkedList<PropertyWrapper> propsList_first, LinkedList<PropertyWrapper> propsList_second) {
		this.propsList_first = propsList_first;
		this.propsList_second = propsList_second;
	}
	
	private boolean compareString(String relOp, String operand1, String operand2) {
		boolean result = false;
		
		switch(relOp) {
		case EQUALS			:	result = operand1.equals(operand2);
								break;
		case NOT_EQUALS		:	result =  !( operand1.equals(operand2) );
		}
		
		return result;
	}
	
	private boolean compareBoolean(String relOp, boolean operand1, boolean operand2) {
		boolean result = false;
		
		switch(relOp) {
		case EQUALS			:	result = (operand1 == operand2);
								break;
		case NOT_EQUALS		:	result = (operand1 != operand2);
		}
		
		return result;
	}
	
	private boolean compareInt(String relOp, int operand1, int operand2) {
		boolean result = false;
		
		switch(relOp) {
		case EQUALS					:	result = (operand1 == operand2);
										break;
		case NOT_EQUALS				:	result = (operand1 != operand2);
										break;
		case GREATER_THAN			:	result = (operand1 > operand2);
										break;
		case LESS_THAN				:	result = (operand1 < operand2);
										break;
		case GREATER_THAN_EQUALS	:	result = (operand1 >= operand2);
										break;
		case LESS_THAN_EQUALS		:	result = (operand1 <= operand2);
		}
		
		return result;
	}
	
	private boolean compareFloat(String relOp, float operand1, float operand2) {
		boolean result = false;
		
		switch(relOp) {
		case EQUALS					:	result = (operand1 == operand2);
										break;
		case NOT_EQUALS				:	result = (operand1 != operand2);
										break;
		case GREATER_THAN			:	result = (operand1 > operand2);
										break;
		case LESS_THAN				:	result = (operand1 < operand2);
										break;
		case GREATER_THAN_EQUALS	:	result = (operand1 >= operand2);
										break;
		case LESS_THAN_EQUALS		:	result = (operand1 <= operand2);
		}
		
		return result;
	}
	
	private boolean compareDouble(String relOp, double operand1, double operand2) {
		boolean result = false;
		
		switch(relOp) {
		case EQUALS					:	result = (operand1 == operand2);
										break;
		case NOT_EQUALS				:	result = (operand1 != operand2);
										break;
		case GREATER_THAN			:	result = (operand1 > operand2);
										break;
		case LESS_THAN				:	result = (operand1 < operand2);
										break;
		case GREATER_THAN_EQUALS	:	result = (operand1 >= operand2);
										break;
		case LESS_THAN_EQUALS		:	result = (operand1 <= operand2);
		}
		
		return result;
	}	
	
	private boolean doesPropsSatisfy(StateProp stateProp, HashMap<Integer, PropertyUI> propertyUIs) {		
		boolean isSatisfied = true;
				
		if( stateProp.isPseudoState() )
			return false;
		
		for(Map.Entry<Integer, Datatype> entry : symbolTypeMap.entrySet()) {
			int position = entry.getKey();
			Datatype type= entry.getValue();
			
			PropertyUI propertyUI = propertyUIs.get(position);
			String givenValue = propertyUI.getValue();			
			
			if( givenValue.equals("*") )	// "*" means it is satisfied
				continue;
			
			if(givenValue.contains(RANGE_K)) {
				Range range = propertyUI.getRangeValues();
				double lower = range.getMinimum();
				double upper = range.getMaximum();
				
				System.out.println(propertyUI.getVar() + " BETWEEN " + lower + " AND " + upper);
				
				Value value = stateProp.getPropertyValue(position);
				
				switch(type) {
				case INTEGER	:	int v_int = ((Integer) value.getV()).intValue();
									if(v_int < lower || v_int > upper)
										isSatisfied = false;
									break;
				case FLOAT		:	float v_float = ((Float) value.getV()).floatValue();
									if(v_float < lower || v_float > upper)
										isSatisfied = false;
									break;
				case DOUBLE		:	double v_double = ((Double) value.getV()).doubleValue();
									if(v_double < lower || v_double > upper)
										isSatisfied = false;
				}
				
				
				if(isSatisfied)
					continue;
				else
					break;
			}
			
			String relOp = propertyUI.getRelOp();
			
			if(type == Datatype.STRING) {
				Value value = stateProp.getPropertyValue(position);
				String v = (String) value.getV();	
				
				String gValue_string = givenValue;
				
				if(givenValue.startsWith("\'")) {
					String var = givenValue.substring(1, givenValue.length()-1);
					//System.out.println("var = " + var);
					Value gValue = stateProp.getPropertyValue( symbolMap.getPosition(var) );
					System.out.println("Value of var : " + gValue);
					
					gValue_string = (String) gValue.getV();
				}
				
				if(compareString(relOp, v, gValue_string) == false) {
					isSatisfied = false;
					break;
				}
			}
			else if(type == Datatype.BOOLEAN) {
				Value value = stateProp.getPropertyValue(position);
				boolean v = ((Boolean) value.getV()).booleanValue();
				
				boolean gValue_bool;
				if(givenValue.startsWith("\'")) {
					String var = givenValue.substring(1, givenValue.length()-1);
					//System.out.println("var = " + var);
					Value gValue = stateProp.getPropertyValue( symbolMap.getPosition(var) );
					System.out.println("Value of var : " + gValue);
					
					gValue_bool = ((Boolean) gValue.getV()).booleanValue();
				}
				else
					gValue_bool = Boolean.parseBoolean(givenValue);				
				
				if(compareBoolean(relOp, v, gValue_bool) == false) {
					isSatisfied = false;
					break;
				}
			}
			else if(type == Datatype.INTEGER) {
				Value value = stateProp.getPropertyValue(position);
				int v = ((Integer) value.getV()).intValue();
				
				int gValue_int;
				if(givenValue.startsWith("\'")) {
					String var = givenValue.substring(1, givenValue.length()-1);
					//System.out.println("var = " + var);
					Value gValue = stateProp.getPropertyValue( symbolMap.getPosition(var) );
					System.out.println("Value of var : " + gValue);
					
					gValue_int = ((Integer) gValue.getV()).intValue();
				}
				else
					gValue_int = Integer.parseInt(givenValue);
				
				if(compareInt(relOp, v, gValue_int) == false) {
					isSatisfied = false;
					break;
				}
			}
			else if(type == Datatype.FLOAT) {
				Value value = stateProp.getPropertyValue(position);
				float v = ((Float) value.getV()).floatValue();
				
				float gValue_float;
				if(givenValue.startsWith("\'")) {
					String var = givenValue.substring(1, givenValue.length()-1);
					//System.out.println("var = " + var);
					Value gValue = stateProp.getPropertyValue( symbolMap.getPosition(var) );
					System.out.println("Value of var : " + gValue);
					
					gValue_float = ((Float) gValue.getV()).floatValue();
				}
				else
					gValue_float = Float.parseFloat(givenValue);
				
				if(compareFloat(relOp, v, gValue_float) == false) {
					isSatisfied = false;
					break;
				}
			}
			else if(type == Datatype.DOUBLE) {
				Value value = stateProp.getPropertyValue(position);
				double v = ((Double) value.getV()).doubleValue();
				
				double gValue_double;
				if(givenValue.startsWith("\'")) {
					String var = givenValue.substring(1, givenValue.length()-1);
					//System.out.println("var = " + var);
					Value gValue = stateProp.getPropertyValue( symbolMap.getPosition(var) );
					System.out.println("Value of var : " + gValue);
					
					gValue_double = ((Double) gValue.getV()).doubleValue();
				}
				else
					gValue_double = Double.parseDouble(givenValue);
				//double convertedValue = Double.parseDouble(givenValue);
				
				if(compareDouble(relOp, v, gValue_double) == false) {
					isSatisfied = false;
					break;
				}
			}
		}
		
		return isSatisfied;
	}
	
	private boolean satisfy(StateProp stateProp, LinkedList<PropertyWrapper> propsList_any) {
		boolean result = false;
		
		for(PropertyWrapper pw : propsList_any) {
			if( pw.isFirstEntry() ) {
				result = doesPropsSatisfy(stateProp, pw.getPropertyUIs_any());
			}
			else {
				String operator = pw.getCompCombo().getText();
				
				switch(operator) {
				case "OR"	: 	result = result || doesPropsSatisfy(stateProp, pw.getPropertyUIs_any());
								break;
				case "AND"	:	result = result && doesPropsSatisfy(stateProp, pw.getPropertyUIs_any());
				}
			}
		}
		
		return result;
	}
	
	public int checkAX(int startStateCode) {
		int status = -1;
		
		StateProp stateProp = statePropMap.get(startStateCode);
		int codeSource = stateProp.getCode();
		
		for(String succName : stateProp.getNeighbours()) {
			StateProp succState = statePropMap.get( stateNameMap.get(succName) );
			
			status = (satisfy(succState, propsList_first) == true) ? 1 : 0;
			
			if(status == 0) {				
				int codeDest = succState.getCode();
				
				modelCheckUI.colorEdge(codeSource, codeDest, display.getSystemColor(SWT.COLOR_RED));
				
				break;
			}
		}
		
		switch(status) {
		case 1	:	return 1;	// if all children satisfies the property
		case -1	:				// for loop is not executed since there are no successors
		default	:	return 0;	// (status == 0); only when for loop is executed and atleast one successor violates the property 
		}
	}
	
	public int checkEX(int startStateCode) {
		boolean isSatisfied = false;			
		
		StateProp stateProp = statePropMap.get(startStateCode);
		int codeSource = stateProp.getCode();	
		
		for(String succName : stateProp.getNeighbours()) {			
			StateProp succState = statePropMap.get( stateNameMap.get(succName) );
			if(satisfy(succState, propsList_first) == true) {			
				isSatisfied = true;
				
				int codeDest = succState.getCode();
				
				modelCheckUI.colorEdge(codeSource, codeDest, display.getSystemColor(SWT.COLOR_DARK_GREEN));
				
				break;
			}
		}
		
		return isSatisfied ? 1 : 0;
	}
	
	public int checkAG(int startStateCode) {
		Set<Integer> visited = new TreeSet<>();
		
		StateProp stateProp = statePropMap.get(startStateCode);
		
		boolean isSatisfied = checkAGRecursive(stateProp, visited);
		
		return isSatisfied ? 1 : 0;
	}
	
	private boolean checkAGRecursive(StateProp current, Set<Integer> visited) {
		if(visited.size() == stateNameMap.size())	//terminating condition
			return false;
		
		visited.add( current.getCode() );
		
		if(satisfy(current, propsList_first) == false)
			return false;	
		
		boolean isSatisfied = true;
		for(String childName : current.getNeighbours()) {
			StateProp child = statePropMap.get( stateNameMap.get(childName) );
			if(! visited.contains( child.getCode() ) ) {
				if(checkAGRecursive(child, visited) == false) {
					isSatisfied = false;
					
					int codeSource = current.getCode();
					int codeDest = child.getCode();
					
					modelCheckUI.colorEdge(codeSource, codeDest, display.getSystemColor(SWT.COLOR_RED));
					
					//System.out.println("Violated Path : " + current.getCode() + " --> " + child.getCode());		//<---------------
					
					break;
				}
			}
		}
		
		return isSatisfied;
	}
	
	public int checkEG(int startStateCode) {
		Set<Integer> visited = new TreeSet<>();
		
		StateProp stateProp = statePropMap.get(startStateCode);
		
		int status = checkEGRecursive(stateProp, visited, false);
		
		return status;
	}
	
	/*
	 * returns 
	 * 		1: satisfies 
	 * 		0: not satisfies
	 */
	private int checkEGRecursive(StateProp current, Set<Integer> visited, boolean checkCycle) {
		if(checkCycle == true)
			if(satisfy(current, propsList_first) == true)			
				return 1;
		
		if(visited.size() == stateNameMap.size())	//terminating condition
			return 0;
		
		visited.add( current.getCode() );
		
		if(satisfy(current, propsList_first) == false)		
			return 0;			
		
		int status = -1;
		for(String childName : current.getNeighbours()) {
			StateProp child = statePropMap.get( stateNameMap.get(childName) );
			
			if(! visited.contains( child.getCode() ) )
				status = checkEGRecursive(child, visited, false);
			
			/*	check for a cycle	*/
			else {			
				//System.out.println("########### BEFORE checking for a cycle, child=" + childName + " FROM " + current.getVertex().getName());
				
				status = checkEGRecursive(child, visited, true);	
				
				//System.out.println("########### AFTER checking for a cycle, child=" + childName + ", status=" + status);								
			}	
				
			
			if(status == 1) {
				int codeSource = current.getCode();
				int codeDest = child.getCode();
				
				modelCheckUI.colorEdge(codeSource, codeDest, display.getSystemColor(SWT.COLOR_DARK_GREEN));
				
				//System.out.println("Satisfied Path : " + current.getCode() + " --> " + child.getCode());		//<---------------
				
				break;
			}
		}		
		
		switch(status) {
		case 1	:				// if atleast one child satisfies the property
		case -1	:	return 1;	// for loop is not executed since there are no children, and the last child satisfies the property
		default	:	return 0;	// (status == 0); only when for loop is executed and no child satisfies the property 
		}
	}
	
	public int checkAF(int startStateCode) {
		Set<Integer> visited = new TreeSet<>();
		
		StateProp stateProp = statePropMap.get(startStateCode);
		
		int status = checkAFRecursive(stateProp, visited);
		
		return status;
	}
	
	/*
	 * returns 
	 * 		1: satisfies 
	 * 		0: not satisfies
	 */
	private int checkAFRecursive(StateProp current, Set<Integer> visited) {
		if(visited.size() == stateNameMap.size())	//terminating condition
			return 0;
		
		visited.add( current.getCode() );
		
		if(satisfy(current, propsList_first) == true)
			return 1;
		
		int status = -1;
		for(String childName : current.getNeighbours()) {			
			StateProp child = statePropMap.get( stateNameMap.get(childName) );
			if(! visited.contains( child.getCode() ) ) {
				status = checkAFRecursive(child, visited);
				
				if(status == 0) {
					int codeSource = current.getCode();
					int codeDest = child.getCode();
					
					modelCheckUI.colorEdge(codeSource, codeDest, display.getSystemColor(SWT.COLOR_RED));
					
					//System.out.println("Violated Path : " + current.getCode() + " --> " + child.getCode());		//<---------------
					
					break;
				}
			}
		}
		
		switch(status) {
		case 1	:	return 1;	// if all children satisfies the property
		case -1	:				// for loop is not executed since there are no children and last child violates the property
		default	:	return 0;	// (status == 0); only when for loop is executed and atleast one child violates the property 
		}
	}
	
	public int checkEF(int startStateCode) {
		Set<Integer> visited = new TreeSet<>();
		
		StateProp stateProp = statePropMap.get(startStateCode);
		
		boolean isSatisfied = checkEFRecursive(stateProp, visited);
		
		return isSatisfied ? 1 : 0;
	}
	
	private boolean checkEFRecursive(StateProp current, Set<Integer> visited) {
		if(visited.size() == stateNameMap.size())	//terminating condition
			return false;
		
		visited.add( current.getCode() );
		
		if(satisfy(current, propsList_first) == true)
			return true;	
		
		boolean isSatisfied = false;
		for(String childName : current.getNeighbours()) {
			StateProp child = statePropMap.get( stateNameMap.get(childName) );
			if(! visited.contains( child.getCode() ) )				
				if(checkEFRecursive(child, visited) == true) {
					isSatisfied = true;
					
					int codeSource = current.getCode();
					int codeDest = child.getCode();
					
					modelCheckUI.colorEdge(codeSource, codeDest, display.getSystemColor(SWT.COLOR_DARK_GREEN));
					
					break;
				}
		}
		
		return isSatisfied;
	}
	
	public int checkAU(int startStateCode) {
		Set<Integer> visited = new TreeSet<>();
		
		StateProp stateProp = statePropMap.get(startStateCode);
		
		int status = checkAURecursive(stateProp, visited);
		
		return status;
	}
	
	/*
	 * returns 
	 * 		1: satisfies 
	 * 		0: not satisfies
	 */
	private int checkAURecursive(StateProp current, Set<Integer> visited) {
		if(visited.size() == stateNameMap.size())	//terminating condition
			return 0;
		
		visited.add( current.getCode() );		
		
		if(satisfy(current, propsList_first) == false) {
			boolean parentSatisfies = false;
			
			if(satisfy(current, propsList_second) == true) {
				for(String parentName: current.getParents()) {
					StateProp parent = statePropMap.get( stateNameMap.get(parentName) );
					if(satisfy(parent, propsList_first) == true) {
						parentSatisfies = true;
						break;
					}
				}
				
				if(parentSatisfies == true)	// if properties_1 UNTIL propeties_2 holds
					return 1;
				else 						// if parent violate properties_1
					return 0;
			}
			else							// if currentState violate properties_2
				return 0;
		}
		
		int status = -1;
		for(String childName : current.getNeighbours()) {
			StateProp child = statePropMap.get( stateNameMap.get(childName) );
			if(! visited.contains( child.getCode() ) ) {
				status = checkAURecursive(child, visited);
				
				if(status == 0) {					
					int codeSource = current.getCode();
					int codeDest = child.getCode();
					
					modelCheckUI.colorEdge(codeSource, codeDest, display.getSystemColor(SWT.COLOR_RED));
					
					//System.out.println("Violated Path : " + current.getCode() + " --> " + child.getCode());		//<---------------
					
					break;
				}
			}
		}
		
		switch(status) {
		case 1	:	return 1;	// if all children satisfies the properties
		case -1	:				// for loop is not executed since there are no children and last child satisfies only property_first
		default	:	return 0;	// (status == 0); only when for loop is executed and atleast one child violates the properties 
		}
	}
	
	public int checkEU(int startStateCode) {
		Set<Integer> visited = new TreeSet<>();
		
		StateProp stateProp = statePropMap.get(startStateCode);
		
		int status = checkEURecursive(stateProp, visited);
		
		return status;
	}
	
	/*
	 * returns 
	 * 		1: satisfies 
	 * 		0: not satisfies
	 */
	private int checkEURecursive(StateProp current, Set<Integer> visited) {
		if(visited.size() == stateNameMap.size())	//terminating condition
			return 0;
		
		visited.add( current.getCode() );		
		
		if(satisfy(current, propsList_first) == false) {
			boolean parentSatisfies = false;
			
			if(satisfy(current, propsList_second) == true) {
				for(String parentName: current.getParents()) {					
					StateProp parent = statePropMap.get( stateNameMap.get(parentName) );
					if(satisfy(parent, propsList_first) == true) {
						parentSatisfies = true;
						break;
					}
				}
				
				if(parentSatisfies == true)
					return 1;
				else 
					return 0;
			}
			else 
				return 0;
		}
		
		int status = -1;
		for(String childName : current.getNeighbours()) {			
			StateProp child = statePropMap.get( stateNameMap.get(childName) );
			if(! visited.contains( child.getCode() ) ) {
				status = checkEURecursive(child, visited);
				
				if(status == 1) {					
					int codeSource = current.getCode();
					int codeDest = child.getCode();
					
					modelCheckUI.colorEdge(codeSource, codeDest, display.getSystemColor(SWT.COLOR_DARK_GREEN));
					
					//System.out.println("Violated Path : " + current.getCode() + " --> " + child.getCode());		//<---------------
					
					break;
				}
			}
		}
		
		switch(status) {
		case 1	:	return 1;	// if atleast one child satisfies the properties
		case -1	:				// for loop is not executed since there are no children and last child satisfies only property_first
		default	:	return 0;	// (status == 0); only when for loop is executed and all children violates the properties 
		}
	}
}
