package modelChecker.views.viewpart;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Function {
	public static ArrayList<Integer> setOr(ArrayList<Integer> or1,ArrayList<Integer> or2){
		if((or1==null || or1.isEmpty()) && (or2==null || or2.isEmpty())) {
			return or1;
		}
		else if(or1==null || or1.isEmpty()) {
			Collections.sort(or2);
			return or2;
		}
		else if(or2==null || or2.isEmpty()) {
			Collections.sort(or1);
			return or1;
		}
		else{
			or1.removeAll(or2);
			or1.addAll(or2);
			Collections.sort(or1);
			return or1;
		}
	}

	public static ArrayList<Integer> setAnd(ArrayList<Integer> and1,ArrayList<Integer> and2){
		if((and1==null || and1.isEmpty()) && (and2==null || and2.isEmpty())){
			return and1;
		}
	    else if(and1==null || and1.isEmpty()) {
			Collections.sort(and2);
			return and2;
		}
		else if(and2==null || and2.isEmpty()) {
			Collections.sort(and1);
			return and1;
		}
		else{
			and1.retainAll(and2);
			Collections.sort(and1);
			return and1;
		}
	}
	
	public static ArrayList<Integer> setNot(ArrayList<Integer> not1) {
		ArrayList<Integer> result= new ArrayList();
		result.addAll(NestedModel.universe_current);
		if(not1==null || not1.isEmpty()){
			return result;
		}
		else{
			result.removeAll(not1);
			if(result==null || result.isEmpty()) return result;
			Collections.sort(result);
			return result;
		}
	}
	
	public static ArrayList<Integer> setImplies(ArrayList<Integer> imp1,ArrayList<Integer> imp2){
		ArrayList<Integer> result1 = setNot(imp1);
		ArrayList<Integer> result2 = setOr(result1,imp2);
		if(result2==null || result2.isEmpty()) return result2;
		Collections.sort(result2);
		return result2;
	}
	
	public static ArrayList<Integer> basicEX(ArrayList<Integer> input) {
		if(input==null || input.isEmpty()) return input;
		ArrayList<Integer> result = new ArrayList();
		for(int i: input) {
			if(NestedModel.revAdjacencyList_current[i]==null || NestedModel.revAdjacencyList_current[i].isEmpty()) continue;
			result.addAll(NestedModel.revAdjacencyList_current[i]);
		}
		if(result==null || result.isEmpty()) return result;
		Set<Integer> hs = new HashSet<>();
		hs.addAll(result);
		result.clear();
		result.addAll(hs);
		Collections.sort(result);
		return result;
	}
	
	public static ArrayList<Integer> basicAX(ArrayList<Integer> input) {
		if(input==null || input.isEmpty()) return input;
		ArrayList<Integer> result = new ArrayList();
		for(int i=1;i<=NestedModel.countOfNodes_current;i++) {
			if(NestedModel.adjacencyList_current[i]==null || NestedModel.adjacencyList_current[i].isEmpty()) continue;
			if(input.containsAll(NestedModel.adjacencyList_current[i])) result.add(i);
		}
		if(result==null || result.isEmpty()) return result;
		Collections.sort(result);
		return result;
	}
	
	public static ArrayList<Integer> basicEF(ArrayList<Integer> input) {
		if(input==null || input.isEmpty()) return input;
		HashSet<Integer> result = new HashSet(input);
		HashSet<Integer> preresult = new HashSet(result);
		do {
			result.addAll(preresult);
            preresult.addAll(result);
			for(int i: preresult) {
				if(NestedModel.revAdjacencyList_current[i]==null || NestedModel.revAdjacencyList_current[i].isEmpty()) continue;
				result.addAll(NestedModel.revAdjacencyList_current[i]);
			}
			result.removeAll(preresult);
			if(result.isEmpty()) break;
		}while(true);
		ArrayList<Integer> result2 = new ArrayList();
		if(preresult==null || preresult.isEmpty()) return result2;
		result2.addAll(preresult);
		Collections.sort(result2);
		return result2;
	}
	
	public static ArrayList<Integer> basicEG(ArrayList<Integer> input) {
		if(input==null || input.isEmpty()) return input;
		ArrayList<Integer> notInput = input;
		ArrayList<Integer> g[] = NestedModel.duplicateAdjacencyList(notInput);
		ArrayList<Integer> result = SCC2.scc(g);
		//ArrayList<Integer> result = SCC.getSCComponents(g);
	//	result.removeAll(notInput);
		result.remove(new Integer(0));
		/*for(int i:input){
			if(!result.contains(i)){
				try{
					if(Function.setAnd(result, Model.adjacencyList[i]).size()==Model.adjacencyList[i].size()) result.add(i);
				}catch(Exception e){
					
				}
			}
		}*/
		if(result==null || result.isEmpty()) return result;
		Collections.sort(result);
		return result;
	}
	
	public static ArrayList<Integer> basicAF(ArrayList<Integer> input) {
		if(input==null || input.isEmpty()) return input;
		ArrayList<Integer> result1 = setNot(input);
		ArrayList<Integer> result2 = basicEG(result1);
		ArrayList<Integer> result3 = setNot(result2);
		if(result3==null || result3.isEmpty()) return result3;
		Collections.sort(result3);
		return result3;
	}
	
	public static ArrayList<Integer> basicAG(ArrayList<Integer> input) {
		if(input==null || input.isEmpty()) return input;
		ArrayList<Integer> result1 = setNot(input);
		ArrayList<Integer> result2 = basicEF(result1);
		ArrayList<Integer> result3 = setNot(result2);
		if(result3==null || result3.isEmpty()) return result3;
		Collections.sort(result3);
		return result3;
	}
}
