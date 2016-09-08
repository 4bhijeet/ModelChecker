package modelChecker.views.viewpart;


import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
public class NestedModel {
	static ArrayList<Integer> universe,adjacencyList[],revAdjacencyList[],propertiesTrueAt[];
	static ArrayList<String> properties;
	static HashMap<Integer,String> nodeNames;
	static HashMap<String,String> varSet;
	static int countOfNodes,countOfProperties;
	
	static ArrayList<Integer> universe_run,adjacencyList_run[],revAdjacencyList_run[],propertiesTrueAt_run[];
	static ArrayList<String> properties_run;
	static HashMap<Integer,String> nodeNames_run;
	static int countOfNodes_run,countOfProperties_run;
	
	static ArrayList<Integer> universe_current,adjacencyList_current[],revAdjacencyList_current[],propertiesTrueAt_current[];
	static ArrayList<String> properties_current;
	static HashMap<Integer,String> nodeNames_current;
	static int countOfNodes_current,countOfProperties_current;
	
	public static void Modeling(String file) {
		String line,line_split[];
		try {
			FileReader f = new FileReader(file);
			Scanner kbd = new Scanner(f);
			line = kbd.nextLine();
			line = line.replaceAll("\\s+","");
			kbd.nextLine();
			line_split = line.split(":");
			countOfNodes = Integer.parseInt(line_split[1]);
			adjacencyList = new ArrayList[countOfNodes+1];
			revAdjacencyList = new ArrayList[countOfNodes+1];
			universe = new ArrayList();
			nodeNames = new HashMap();
			varSet = new HashMap();
			for(int i=0;i<=countOfNodes;i++){
				adjacencyList[i] = new ArrayList();
				revAdjacencyList[i] = new ArrayList();
				universe.add(i);
			}
			universe.remove(new Integer(0));
			
			for(int i=1;i<=countOfNodes;i++){
				line = kbd.nextLine();
                line = line.replaceAll("\\s+","");
				if(line.contains("Atomic")) break;
				line_split = line.split(":");
				if(line_split.length>1)
				{	
					line_split = line_split[1].split(",");
					for(String s: line_split) {
						adjacencyList[i].add(Integer.parseInt(s));
						revAdjacencyList[Integer.parseInt(s)].add(i);
					}
				}
			}
			line = kbd.nextLine();
			line = line.replaceAll("\\s+","");
			line_split = line.split(":");
			countOfProperties = Integer.parseInt(line_split[1]);
			propertiesTrueAt = new ArrayList[countOfProperties];
			properties = new ArrayList();
			for(int i=0;i<countOfProperties;i++) propertiesTrueAt[i] = new ArrayList();
			for(int i=0;i<countOfProperties;i++) {
				line=kbd.nextLine();
				line = line.replaceAll("\\s+","");
				line_split = line.split(":");
				properties.add(line_split[0]);
				String prop = new String(line_split[0]);
				line_split = line_split[1].split(",");
				for(String s: line_split) {
					int n = Integer.parseInt(s);
					if(nodeNames.containsKey(n)){
						String name = nodeNames.get(n);
						name = name + "," +prop;
						nodeNames.put(n, name);
					}
					else {
						String name = prop;
						nodeNames.put(n, name);
					}
					propertiesTrueAt[i].add(Integer.parseInt(s));
				}
			}
			
			line=kbd.nextLine();
			if(line.startsWith("Abbreviations")) {
				line = kbd.nextLine();
                line = line.replaceAll("\\s+","");
				for(int i=0;i<countOfProperties;i++) {
					if(line.startsWith("Quit")) break;
					line_split = line.split(":");
					varSet.put(line_split[1], line_split[0]);
					line = kbd.nextLine();
                    line = line.replaceAll("\\s+","");
				}				
			}
				
			f.close();
			kbd.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void printProperties(){
		for(int i=0;i<countOfProperties;i++) {
			System.out.println(properties.get(i));
		}
	}
	
	public static ArrayList<Integer>[] duplicateAdjacencyList(ArrayList<Integer> notInput) {
		ArrayList<Integer> result[] = new ArrayList[countOfNodes+1];
		ArrayList<Integer> a = Function.setNot(notInput);
		result[0] = new ArrayList();
		for(int i=1;i<=countOfNodes;i++) {
			if(!notInput.contains(i)) {
				result[i] = new ArrayList();
			}
			else {
				result[i] = new ArrayList(adjacencyList[i]);
				if(result[i]==null || result[i].isEmpty() || a==null || a.isEmpty()){
					
				}
				else {
					result[i].removeAll(a);
				}
			}
		}
		return result;
	}
	
	
	public static void umlModeling(String file)
	{
		String line,line_split[];
		try {
			varSet = new HashMap(); 
			File f = new File(file);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(f);

			doc.getDocumentElement().normalize();

			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
					
			NodeList nList = doc.getElementsByTagName("subvertex"); 
			
			Set<Element> targetElements = new HashSet<Element>();
			for (int i = 0; i < nList.getLength(); i++) {
			  Element e = (Element)nList.item(i);
			  if (e.getAttribute("xmi:type").equals("uml:Pseudostate")) {
			    targetElements.add(e);
			  }
			}
			for (Element e: targetElements) {
			  e.getParentNode().removeChild(e);
			}
			
			int totalNodes = nList.getLength();
			HashMap stateMap = new HashMap<String, List>();
			HashMap idNameMap = new HashMap<String, String>();
			nodeNames = new HashMap<>();
			for (int temp = 0; temp < totalNodes; temp++) {
				Node nNode = nList.item(temp);
				System.out.println("\nCurrent Element :" + nNode.getNodeName());
				Element eElement = (Element) nNode;
				
				String elementName = eElement.getAttribute("name");
				String nodeID = eElement.getAttribute("xmi:id");
				String[] stateArray = elementName.split(",");
				for(int i=0; i<stateArray.length; i++)
				{
					List destinationList = new LinkedList<>();
					if(stateMap.containsKey(stateArray[i]))
					{
						destinationList = (List) stateMap.get(stateArray[i]);
						destinationList.add(nodeID);
						stateMap.remove(stateArray[i]);
						stateMap.put(stateArray[i], destinationList);
					}
					else
					{
						destinationList.add(nodeID);
						stateMap.put(stateArray[i], destinationList);
					}
					
				}
				nodeNames.put(temp+1, elementName.replaceAll(" ", ""));
				
			}	
			
			List stateNodeList = new ArrayList<>();
			List stateNameList = new ArrayList<>();
			for (int temp = 0; temp < totalNodes; temp++) {
				Node nNode = nList.item(temp);
				Element eElement = (Element) nNode;
				String nodeID = eElement.getAttribute("xmi:id");
				
				stateNodeList.add(nodeID);
				stateNameList.add(eElement.getAttribute("name"));
			}		
			
					
			HashMap stateTransitionMap = new HashMap<String, List>();
			nList = doc.getElementsByTagName("transition");			
			int totalTransitions = nList.getLength();
			int countState=0;
			
			
			for (int temp = 0; temp < totalTransitions; temp++) {
				Node nNode = nList.item(temp);
				System.out.println("\nCurrent Element :" + nNode.getNodeName());
				Element eElement = (Element) nNode;
				String source = eElement.getAttribute("source");
				String destination = eElement.getAttribute("target");
				List destinationList = new LinkedList<>();
				if(stateTransitionMap.containsKey(source))
				{
					destinationList = (List) stateTransitionMap.get(source);
					destinationList.add(destination);
					stateTransitionMap.remove(source);
					stateTransitionMap.put(source, destinationList);
				}
				else
				{
					destinationList.add(destination);
					stateTransitionMap.put(source, destinationList);
				}
				
				
			}
			
			Iterator it = stateNodeList.iterator();
			HashMap stateTransitionMapFinal = new HashMap<>();
			int keyIndex = 0;
			while(it.hasNext())
			{
				String key = it.next().toString();
				List destinationList = new ArrayList<>();
				if(stateTransitionMap.containsKey(key))
				{
					destinationList = (List) stateTransitionMap.get(key);
					Iterator internalListIterator = stateNodeList.iterator();
					int i=0;
					while(internalListIterator.hasNext())
					{
						String nodeID = internalListIterator.next().toString();
						if(destinationList.contains(nodeID))
						{
							destinationList.remove(nodeID);
							destinationList.add(i);
						}
						i++;
					}
				}
				stateTransitionMapFinal.put(keyIndex, destinationList);
				keyIndex++;
				
			}
			
			it = stateNodeList.iterator();
						
			countOfNodes = totalNodes;
			adjacencyList = new ArrayList[countOfNodes+1];
			revAdjacencyList = new ArrayList[countOfNodes+1];
			universe = new ArrayList();
			for(int i=0;i<=countOfNodes;i++){
				adjacencyList[i] = new ArrayList();
				revAdjacencyList[i] = new ArrayList();
				universe.add(i);
			}
			universe.remove(0);
		
			it = stateTransitionMapFinal.entrySet().iterator();
			int count = 1;
		    while (it.hasNext()) {
		        Map.Entry pair = (Map.Entry)it.next();
		        //System.out.println(pair.getKey() + " = " + pair.getValue());
		        List innerList = (List) pair.getValue();
		        for(int i=0; i<innerList.size(); i++)
		        {
		        	adjacencyList[count].add(Integer.parseInt(innerList.get(i).toString())+1);
		        	revAdjacencyList[Integer.parseInt(innerList.get(i).toString())+1].add(count);
		        }		       
				count++;
		       // it.remove(); // avoids a ConcurrentModificationException
		    }	
		    
		    
		    
			countOfProperties = stateMap.size();			
			propertiesTrueAt = new ArrayList[countOfProperties];
			properties = new ArrayList();
			//nodeNames.remove(0);
			for(int i=0;i<countOfProperties;i++) propertiesTrueAt[i] = new ArrayList();
			for(int i=0;i<countOfProperties;i++)
			{
				it = nodeNames.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
			        String[] states = pair.getValue().toString().split(",");
			        for(int j=0; j<states.length; j++)
			        {
			        	if(!properties.contains(states[j]))
			        		properties.add(states[j]);
			        }    			        
			    }   
			}
			
			for(int i=0;i<properties.size(); i++)
			{
				String property = properties.get(i).toString();
				for(int j=0; j<stateNameList.size(); j++)
				{
					if(stateNameList.get(j).toString().contains(property))
					{
						propertiesTrueAt[i].add(j+1);
					}
				}
					
			}
			System.out.println("end");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void RuntimeToModelConvertor(String file) {
		String line,line_split[],line_split2[];
		try {
			int propertyVariables=0;
			countOfProperties_run = 0;
			countOfNodes_run = 0;
			properties_run = new ArrayList();
			universe_run = new ArrayList();
			nodeNames_run = new HashMap();
			HashMap<Integer,ArrayList<Integer>> propertiesTrueAtHM = new HashMap();
			FileReader f = new FileReader(file);
			Scanner kbd = new Scanner(f);
			line = kbd.nextLine();
            line = line.replaceAll("\\s+","");
			line = kbd.nextLine();
            line = line.replaceAll("\\s+","");
			line = kbd.nextLine();
            line = line.replaceAll("\\s+","");
			while(!line.startsWith("$$$")) {
				line_split = line.split("-");
				line_split[1] = line_split[1].replaceAll(" ","");
				line_split2 = line_split[1].split(",");
				if(propertyVariables==0) propertyVariables = line_split2.length;
				universe_run.add(Integer.parseInt(line_split[0]));
				countOfNodes_run++;
				String nodeName="";
				for(int i=0; i < propertyVariables; i++) {
					line_split2[i] = "p"+(i+1)+"="+line_split2[i];
					if(!properties_run.contains(line_split2[i])) {
						properties_run.add(line_split2[i]);
						ArrayList<Integer> a = new ArrayList();
						a.add(Integer.parseInt(line_split[0]));
						propertiesTrueAtHM.put(properties_run.size()-1, a);
						countOfProperties_run++;
					}
					else{
						int position = properties_run.indexOf(line_split2[i]);
						ArrayList<Integer> a = propertiesTrueAtHM.get(position);
						a.add(Integer.parseInt(line_split[0]));
					}
					if(nodeName=="") nodeName += line_split2[i];
					else nodeName += "," + line_split2[i];
				}
				nodeNames_run.put(Integer.parseInt(line_split[0]), nodeName);
				line = kbd.nextLine();
                line = line.replaceAll("\\s+","");
			}
			adjacencyList_run = new ArrayList[countOfNodes_run+1];
			revAdjacencyList_run = new ArrayList[countOfNodes_run+1];
			propertiesTrueAt_run = new ArrayList[countOfProperties_run];
			for(int i=0;i<=countOfNodes_run;i++){
				adjacencyList_run[i] = new ArrayList();
				revAdjacencyList_run[i] = new ArrayList();
			}
			line = kbd.nextLine();
            line = line.replaceAll("\\s+","");
			int count=1;
			while(count<countOfNodes_run+1) {
				line = kbd.nextLine();
                //line = line.replaceAll("\\s+","");
				line_split = line.split(" ");
				for(int i=1;i<line_split.length;i++){
					if(line_split[i].equals("1")) {
						adjacencyList_run[count].add(i);
						revAdjacencyList_run[i].add(count);
					}
				}
				count++;
			}
			for(int i=0;i<countOfProperties_run;i++){
				propertiesTrueAt_run[i] = new ArrayList();
				propertiesTrueAt_run[i].addAll(propertiesTrueAtHM.get(i));
			}
			
			line = kbd.nextLine();
            line = line.replaceAll("\\s+","");
			line = kbd.nextLine();
            line = line.replaceAll("\\s+","");
			line_split = line.split("=");
			line_split = line_split[1].split(",");
			
			if(!varSet.isEmpty() && varSet != null)
			{
				for(int i=0;i<countOfProperties_run;i++){
					String prop = properties_run.get(i);
					line_split2 = prop.split("=");
					int j = Integer.parseInt(line_split2[0].substring(1));
					String prop2 = line_split[j-1] + "=" + line_split2[1];
					String temp = varSet.get(prop2);
					properties_run.remove(i);
					properties_run.add(i, temp);
					for(int k=1;k<=countOfNodes_run;k++) {
						String name = nodeNames_run.get(k);
						name = name.replaceAll(prop,temp);
						//name.replaceFirst(prop, temp);
						nodeNames_run.put(k, name);
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean checkConsistency() {
		ArrayList<Integer> adjacencyList_temp[];
		HashMap<Integer,String> nodeNames_temp;
		for(int i=1;i<=countOfNodes_run;i++) {
			String name = nodeNames_run.get(i);
			if(nodeNames.containsValue(name)) {
				int j = getKey(name);
				for(int k:adjacencyList_run[i]) {
					if(!adjacencyList[j].contains(getKey(nodeNames_run.get(k)))) return false;
				}
			} else return false;
		}
		return true;
	}
	
	private static Integer getKey(String value){
	    for(int key : nodeNames.keySet()){
	        if(nodeNames.get(key).equals(value)){
	            return key; //return the first found
	        }
	    }
	    return null;
	}
	
	public static void make_design_current() {
		universe_current=universe;
		adjacencyList_current=adjacencyList;
		revAdjacencyList_current=revAdjacencyList;
		propertiesTrueAt_current=propertiesTrueAt;
		properties_current=properties;
		nodeNames_current=nodeNames;
		countOfNodes_current=countOfNodes;
		countOfProperties_current=countOfProperties;
	}
	
	public static void make_run_current() {
		universe_current=universe_run;
		adjacencyList_current=adjacencyList_run;
		revAdjacencyList_current=revAdjacencyList_run;
		propertiesTrueAt_current=propertiesTrueAt_run;
		properties_current=properties_run;
		nodeNames_current=nodeNames_run;
		countOfNodes_current=countOfNodes_run;
		countOfProperties_current=countOfProperties_run;
	}
}
