package modelChecker.views.viewpart;

import graphSearch.Datatype;
import graphSearch.ReadUML;
import graphSearch.StateProp;
import graphSearch.SymbolMap;

import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;

import matrix.DesigntimeMtx;
import matrix.RuntimeMtx;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;

import util.GetPropertyValues;

public class ModelCheckerView extends ViewPart {	
	private final static int LOAD_RUNTIME_STATUS_LENGTH = 100;
	
	private Display display;
	
	private Label fileLabel;
	private Text fileText;
	private Button browseButton;	
	
	private TemporalPropertyUI temporalPropertyUI_DT, temporalPropertyUI_RT;	
	private ModelCheckUI modelCheckUI_DT, modelCheckUI_RT;
	
	private Button refreshButton;	
	private Button exportButton;
	
	private Button loadRuntimeButton;
	private Text runtimeLoadStatusText;
	private Button permittedStateButton;
	private Button verifyRuntimeButton;
	
	private Composite browseComposite;
	private Composite graphControlsComposite;
		
	private ScrolledComposite rootScrollComposite;
	private Composite mainComposite, subComposite;
	
	private IStatusLineManager statusLineManager;

	private String startStateName;
	private Map<String, Integer> stateNameMapDT, stateNameMapRT;
	private Map<Integer, StateProp> statePropMapDT, statePropMapRT;
	private Map<Integer, Datatype> symbolTypeMap;
	private Map<Integer, Set<String>> valueMapDT, valueMapRT;
	private TreeMap<Integer, String> sortedVarsByPosition;
	
	private SymbolMap symbolMap;
	private Map<Datatype, Set<Integer>> varsTypeColl;

	private Set<String> permittedStates;
	
	private CTabFolder tabFolder;
	private CTabItem graphareaDesignTime;
	private CTabItem graphareaRuntime;
	private Graph graphDT, graphRT;
	private TreeMap<Integer, GraphNode> nodesDT, nodesRT, nodesSML, nodesSMLRuntime;
	private Map<Edge, GraphConnection> edgesDT, edgesRT, edgesSML, edgesSMLRuntime;
	
	private Map<String, Integer> tempNodeNameToCodeMap;
	private Map<Integer, GraphNode> tempNodes;
	private Map<Edge, GraphConnection> tempEdges;	
	
	private int previousLineWidthDT, previousLineWidthRT, previousLineWidthSML;
	
	private Color previousEdgeColorDT, previousEdgeColorRT, previousEdgeColorSML;
	private Set<Edge> coloredEdgesDT;
	private boolean areEdgesColoredDT;
	
	private Color previousNodeBackgroundColorDT, previousNodeForegroundColorDT, previousNodeForegroundColorSML;
	private Set<Integer> coloredNodes;
	private boolean areNodesColored;
	
	public boolean umlLoaded;
	public boolean runtimeLoaded = false;
	
	private boolean areGraphControlsEnabled;
	
	private DesigntimeMtx designtimeMtx;
	private RuntimeMtx runtimeMtx;
	private File runtimeFile;	
	
	private Properties props = null;

	//SML Changes
	private Button browseRunTimeButton;
	private Label umlFileLabel, runtimeFileLabel;
	private Text umlFileText, runtimeFileText;
	private Button browseUMLButton;
	private Button verifySMLButton;
	private Button syntaxSMLButton;
	private Text smlFormulaText;
	private Label smlFormulaLabel;
	private Composite subRadioComposite;
	private Button[] radios = new Button[2];	//Toggle for New Code and Old Code
	private Button showPathInspectorUI;
	private Button[] radiosDTRT = new Button[2];
	private List nodeList;
	private Graph smlGraph, smlGraphRuntime;
	private String loadedRuntimeFileName = "";
	String runFileName;
	
	public ModelCheckerView() {
		IPath path = ResourcesPlugin.getPlugin().getStateLocation();
		File stateLocationDir = path.toFile();
		File runtimeDir = new File(stateLocationDir.getPath() + File.separator + "runtime_model");
		runtimeFile = new File(runtimeDir, "runtime.txt");
		
		umlLoaded = false;
		runtimeLoaded = false;
		
		areGraphControlsEnabled = false;
		areEdgesColoredDT = false;
		areNodesColored = false;
		
		coloredEdgesDT = new HashSet<Edge>();
		coloredNodes = new HashSet<Integer>();
		
		permittedStates = new HashSet<String>();
		
		tempNodeNameToCodeMap = new HashMap<String, Integer>();
		tempNodes = new HashMap<Integer, GraphNode>();
		tempEdges = new HashMap<Edge, GraphConnection>();
	}
	
	private void clearTempNodesAndEdgesDT() {
		if(! tempEdges.isEmpty()) {
			for(Edge e : tempEdges.keySet())
				tempEdges.get(e).dispose();
			tempEdges.clear();
		}
		
		if(! tempNodes.isEmpty()) {
			for(int code : tempNodes.keySet())
				tempNodes.get(code).dispose();		
			tempNodes.clear();
			tempNodeNameToCodeMap.clear();
		}
	}
	
	private void clearDT() {
		temporalPropertyUI_DT.clearLoadedItems();
		
		if(stateNameMapDT != null){
			stateNameMapDT.clear();
			statePropMapDT.clear();
			valueMapDT.clear();
			
			clearTempNodesAndEdgesDT();
			
			for(Edge e : edgesDT.keySet())
				edgesDT.get(e).dispose();
			for(int code : nodesDT.keySet())
				nodesDT.get(code).dispose();
			edgesDT.clear();
			nodesDT.clear();
			
			if(areEdgesColoredDT) {
				areEdgesColoredDT = false;
				coloredEdgesDT.clear();
			}
			
			if(areNodesColored) {
				areNodesColored = false;
				coloredNodes.clear();
			}
			
			umlLoaded = false;
			
			modelCheckUI_DT.reset();
		}
	}
	
	private void clearRT() {
		temporalPropertyUI_RT.clearLoadedItems();
		
		if(statePropMapRT != null) {
			statePropMapRT.clear();
			statePropMapRT = null;
		}
		
		if(edgesRT != null || nodesRT != null) {
			for(Edge e : edgesRT.keySet())
				edgesRT.get(e).dispose();
			for(int code : nodesRT.keySet())
				nodesRT.get(code).dispose();
			edgesRT.clear();
			nodesRT.clear();
			
			edgesRT = null;
			nodesRT = null;
		}
		
		if(runtimeLoaded) {
			runtimeLoaded = false;
			runtimeLoadStatusText.setText( props.getProperty("runtimeLoadStatusText_NOT_LOADED") );
		}
		
		modelCheckUI_RT.reset();
	}
	
	private void clearLoadedItems() {
		System.out.println("\nClearing Items ...");
		
		statusLineManager.setMessage(null);
		
		clearDT();
		
		if(symbolMap != null){
			symbolTypeMap.clear();		
			symbolMap.clear();		
		}
		clearRT();
		if(permittedStates != null){
			permittedStates.clear();
			if(! permittedStates.isEmpty())
				permittedStates.clear();
		}
	}
	
	private boolean loadUMLData(String fileName) {		
		ReadUML readUML = new ReadUML();
		
		if(umlLoaded) {	//if dataLoaded == true, then destroy the related widgets
			clearLoadedItems();
			temporalPropertyUI_DT.pack();
		}
		
		if(readUML.read(fileName) == false) {
			MessageDialog.openError(new Shell( Display.getCurrent() ), "Error in File", readUML.errorMsg);
			return false;
		}
			
		startStateName = readUML.getStartStateName();
		stateNameMapDT = readUML.getStateNameMap();
		statePropMapDT = readUML.getStatePropMap();
		symbolTypeMap = readUML.getSymbolTypeMap();
		valueMapDT = readUML.getValueMap();
		symbolMap = readUML.getSymbolMap();
		varsTypeColl = readUML.getVarsTypeColl();
		
		sortedVarsByPosition = new TreeMap<Integer, String>();
		
		symbolMap.getMap().forEach( (varName, position) -> sortedVarsByPosition.put(position, varName) );		//<--- Java 8 lambda expression
		
		temporalPropertyUI_DT.loadMappings(stateNameMapDT, symbolTypeMap, valueMapDT, varsTypeColl, sortedVarsByPosition);
		temporalPropertyUI_DT.setDependentComposites(rootScrollComposite, mainComposite);
		
		temporalPropertyUI_DT.createUI();
		
		if(! umlLoaded)
			umlLoaded = true;
		
		temporalPropertyUI_DT.pack();
		
		return true;		
	}
	
	private void loadGraphDT() {
		if( statePropMapDT.isEmpty() )
			return;
		
		nodesDT = new TreeMap<Integer, GraphNode>();
		
		Dimension nodeDimension = null;
	    
	    for(int code : new TreeSet<Integer>( statePropMapDT.keySet() )) {
	    	StateProp stateProp = statePropMapDT.get(code);
	    	String nodeName = stateProp.getName();
	    	
	    	GraphNode node = new GraphNode(graphDT, SWT.NONE, nodeName);
	    	node.setBackgroundColor(display.getSystemColor(SWT.COLOR_YELLOW));
	    	node.setForegroundColor(display.getSystemColor(SWT.COLOR_DARK_RED));
	    	node.setBorderColor(display.getSystemColor(SWT.COLOR_BLACK));
	    	
	    	if(nodeDimension == null)
	    		nodeDimension = node.getSize();
	    	else
	    		nodeDimension.union( node.getSize() );
	    	
	    	if(previousNodeBackgroundColorDT == null) {	//for storing the color of node, only at the beginning of this loop
    			previousNodeBackgroundColorDT = node.getBackgroundColor();
    			previousNodeForegroundColorDT = node.getForegroundColor();
	    	}
	    	
	    	nodesDT.put(code, node);
	    }
	    
	    System.out.println("$$$$$$$$$$$$$ Node dimension : " + nodeDimension);
	    
	    edgesDT = new HashMap<Edge, GraphConnection>();
	    
	    graphDT.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
	    for(int code : statePropMapDT.keySet()) {
	    	StateProp stateSource = statePropMapDT.get(code);
	    	
	    	for(String targetNodeName : stateSource.getNeighbours()) {
	    		int targetNodeCode = stateNameMapDT.get(targetNodeName);
	    		
	    		GraphConnection graphConnection = new GraphConnection(graphDT, SWT.NONE, nodesDT.get(code), nodesDT.get(targetNodeCode));
	    		
	    		if(previousEdgeColorDT == null) {	//for storing the color of edge, only at the beginning of this loop
		    		previousLineWidthDT = graphConnection.getLineWidth();
	    			previousEdgeColorDT = graphConnection.getLineColor();
	    		}
	    		
	    		if(code == targetNodeCode)
	    			graphConnection.setCurveDepth(20);
	    		else
	    			graphConnection.setCurveDepth(5);
	    		graphConnection.changeLineColor(display.getSystemColor(SWT.COLOR_BLACK));
	    		edgesDT.put(new Edge(code, targetNodeCode), graphConnection);
	    	}
	    }
	    
	    //int width = graph.getBounds().width;
	    int reqWidth = (int) (nodeDimension.width * nodesDT.size() * 0.75);
	    
	    System.out.println("\t ^^^^^^^^^^^^ Required Width = " + reqWidth);
	    
	    int displayWidth = display.getBounds().width;
	    //int newWidth;
	    graphareaDesignTime.setControl(graphDT);
	    Point preferedSize = graphDT.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	    if(preferedSize.x < displayWidth)	    	
	    	graphDT.setPreferredSize(reqWidth, reqWidth/2);
	    else
	    	graphDT.setPreferredSize(preferedSize.x, preferedSize.y);
	    
	    /*if(reqWidth > displayWidth) {
	    	//newWidth = displayWidth;
	    	//graph.setPreferredSize(newWidth, newWidth/2);
	    	graph.setPreferredSize(reqWidth, reqWidth/2);
	    }
	    else
	    	graph.setPreferredSize(graph.getBounds().width, graph.getBounds().height);   */     
	    
	    tabFolder.setSelection(graphareaDesignTime);
	    
	    SpringLayoutAlgorithm layoutAlgorithm = new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
	    graphDT.setLayoutAlgorithm(layoutAlgorithm, true);
	    
	    modelCheckUI_DT.loadGraphDependencies(edgesDT, previousLineWidthDT, previousEdgeColorDT);
		modelCheckUI_DT.loadMappings(statePropMapDT, stateNameMapDT, symbolTypeMap, symbolMap);
	}
	
	private void resetGraphColors(){
		
	}
	
	private void loadSMLGraph() {
		mainComposite.pack();
		rootScrollComposite.setMinSize( mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT) );	
		if( NestedModel.adjacencyList.length ==0)
			return;
		if(umlLoaded)
		{	
			if(edgesSML != null)
			{
				for(Edge e : edgesSML.keySet())
					edgesSML.get(e).dispose();
				for(int code : nodesSML.keySet())
					nodesSML.get(code).dispose();
				edgesSML.clear();
				nodesSML.clear();
			}
		}
		clearTempNodesAndEdgesDT();
		nodesSML = new TreeMap<Integer, GraphNode>();
		
		Dimension nodeDimension = null;
	    
	    for(int code : new TreeSet<Integer>( NestedModel.nodeNames.keySet() )) {
	    	String nodeName = NestedModel.nodeNames.get(code);
	    	
	    	GraphNode node = new GraphNode(smlGraph, SWT.NONE, nodeName);
	    	node.setBackgroundColor(display.getSystemColor(SWT.COLOR_YELLOW));
	    	node.setForegroundColor(display.getSystemColor(SWT.COLOR_DARK_RED));
	    	node.setBorderColor(display.getSystemColor(SWT.COLOR_BLACK));
	    	if(nodeDimension == null)
	    		nodeDimension = node.getSize();
	    	else
	    		nodeDimension.union( node.getSize() );
	    	
	    	if(previousNodeForegroundColorSML == null)	//for storing the color of node, only at the beginning of this loop
	    		previousNodeForegroundColorSML = node.getBackgroundColor();
	    	
	    	nodesSML.put(code, node);
	    }
	    
	    System.out.println("$$$$$$$$$$$$$ Node dimension : " + nodeDimension);
	    
	    edgesSML = new HashMap<Edge, GraphConnection>();
	    
	    smlGraph.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
	    
	    for(int code=0; code<NestedModel.adjacencyList.length; code++)
	    {
	    	String nodeName = NestedModel.nodeNames.get(code);
	    	List transitionList = NestedModel.adjacencyList[code];
	    	if(transitionList != null)
	    	{
		    	Iterator it = transitionList.iterator();
		    	while(it.hasNext())
		    	{
		    		int targetNodeCode = (int) it.next();
		    		GraphConnection graphConnection = new GraphConnection(smlGraph, SWT.NONE, nodesSML.get(code), nodesSML.get(targetNodeCode));
		    		
		    		if(previousEdgeColorSML == null)	//for storing the color of edge, only at the beginning of this loop
		    		{
		    			previousLineWidthDT = graphConnection.getLineWidth();
		    			previousEdgeColorSML = graphConnection.getLineColor();
		    		}
		    			
		    		
		    		if(code == targetNodeCode)
		    			graphConnection.setCurveDepth(20);
		    		else
		    			graphConnection.setCurveDepth(5);
		    		graphConnection.changeLineColor(display.getSystemColor(SWT.COLOR_BLACK));
		    		edgesSML.put(new Edge(code, targetNodeCode), graphConnection);
		    	}
	    	} 	
	    }
	    
	    int reqWidth = (int) (nodeDimension.width * nodesSML.size() * 0.75);
	    
	    System.out.println("\t ^^^^^^^^^^^^ Required Width = " + reqWidth);
	    	    
	    int displayWidth = display.getBounds().width;
	    graphareaDesignTime.setControl(smlGraph);    
	    Point preferedSize = smlGraph.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	    if(preferedSize.x < displayWidth)
	    	smlGraph.setPreferredSize(reqWidth, reqWidth/2);
	    else
	    	smlGraph.setPreferredSize(preferedSize.x, preferedSize.y);
	    
	    SpringLayoutAlgorithm layoutAlgorithm = new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
	    tabFolder.setSelection(graphareaDesignTime);
	    
	    smlGraph.setLayoutAlgorithm(layoutAlgorithm, true);
	    umlLoaded = true;
	    areGraphControlsEnabled = true;
		refreshButton.setEnabled(areGraphControlsEnabled);
		exportButton.setEnabled(areGraphControlsEnabled);
	    mainComposite.pack();
		rootScrollComposite.setMinSize( mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT) );	    
	}

	

	private void loadSMLRuntimeGraph() {
		mainComposite.pack();
		rootScrollComposite.setMinSize( mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT) );	
		if( NestedModel.adjacencyList_run.length ==0)
			return;
		if(edgesSMLRuntime != null && !edgesSMLRuntime.isEmpty())
		{	
			for(Edge e : edgesSMLRuntime.keySet())
				edgesSMLRuntime.get(e).dispose();
			for(int code : nodesSMLRuntime.keySet())
				nodesSMLRuntime.get(code).dispose();
			nodesSMLRuntime.clear();
			nodesSMLRuntime.clear();
		}
		nodesSMLRuntime = new TreeMap<Integer, GraphNode>();
		
		Dimension nodeDimension = null;
	    
	    for(int code : new TreeSet<Integer>( NestedModel.nodeNames_run.keySet() )) {
	    	String nodeName = NestedModel.nodeNames_run.get(code);
	    	
	    	GraphNode node = new GraphNode(smlGraphRuntime, SWT.NONE, nodeName);
	    	node.setBackgroundColor(display.getSystemColor(SWT.COLOR_YELLOW));
	    	node.setForegroundColor(display.getSystemColor(SWT.COLOR_DARK_RED));
	    	node.setBorderColor(display.getSystemColor(SWT.COLOR_BLACK));
	    	if(nodeDimension == null)
	    		nodeDimension = node.getSize();
	    	else
	    		nodeDimension.union( node.getSize() );
	    	
	    	if(previousNodeForegroundColorSML == null)	//for storing the color of node, only at the beginning of this loop
	    		previousNodeForegroundColorSML = node.getBackgroundColor();
	    	
	    	nodesSMLRuntime.put(code, node);
	    }
	    
	    System.out.println("$$$$$$$$$$$$$ Node dimension : " + nodeDimension);
	    
	    edgesSMLRuntime = new HashMap<Edge, GraphConnection>();
	    
	    smlGraphRuntime.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
	    
	    for(int code=0; code<NestedModel.adjacencyList_run.length; code++)
	    {
	    	String nodeName = NestedModel.nodeNames_run.get(code);
	    	List transitionList = NestedModel.adjacencyList_run[code];
	    	if(transitionList != null)
	    	{
		    	Iterator it = transitionList.iterator();
		    	while(it.hasNext())
		    	{
		    		int targetNodeCode = (int) it.next();
		    		GraphConnection graphConnection = new GraphConnection(smlGraphRuntime, SWT.NONE, nodesSMLRuntime.get(code), nodesSMLRuntime.get(targetNodeCode));
		    		
		    		if(previousEdgeColorSML == null)	//for storing the color of edge, only at the beginning of this loop
		    		{
		    			previousLineWidthDT = graphConnection.getLineWidth();
		    			previousEdgeColorSML = graphConnection.getLineColor();
		    		}
		    			
		    		
		    		if(code == targetNodeCode)
		    			graphConnection.setCurveDepth(20);
		    		else
		    			graphConnection.setCurveDepth(5);
		    		graphConnection.changeLineColor(display.getSystemColor(SWT.COLOR_BLACK));
		    		edgesSMLRuntime.put(new Edge(code, targetNodeCode), graphConnection);
		    	}
	    	} 	
	    }
	    
	    int reqWidth = (int) (nodeDimension.width * nodesSMLRuntime.size() * 0.75);
	    
	    System.out.println("\t ^^^^^^^^^^^^ Required Width = " + reqWidth);
	    	    
	    int displayWidth = display.getBounds().width;
	    graphareaRuntime.setControl(smlGraphRuntime);    
	    Point preferedSize = smlGraphRuntime.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	    if(preferedSize.x < displayWidth)
	    	smlGraphRuntime.setPreferredSize(reqWidth, reqWidth/2);
	    else
	    	smlGraphRuntime.setPreferredSize(preferedSize.x, preferedSize.y);
	    
	    SpringLayoutAlgorithm layoutAlgorithm = new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
	    tabFolder.setSelection(graphareaRuntime);
	    
	    smlGraphRuntime.setLayoutAlgorithm(layoutAlgorithm, true);
	    runtimeLoaded = true;
	    areGraphControlsEnabled = true;
		refreshButton.setEnabled(areGraphControlsEnabled);
		exportButton.setEnabled(areGraphControlsEnabled);
	    mainComposite.pack();
		rootScrollComposite.setMinSize( mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT) );	    
	}


	
	
	private void loadGraphRT() {
		if( statePropMapRT.isEmpty() )
			return;
		
		nodesRT = new TreeMap<Integer, GraphNode>();
		
		Dimension nodeDimension = null;
	    
	    for(int code : new TreeSet<Integer>( statePropMapRT.keySet() )) {
	    	StateProp stateProp = statePropMapRT.get(code);
	    	String nodeName = stateProp.getName();
	    	
	    	GraphNode node = new GraphNode(graphRT, SWT.NONE, nodeName);
	    	node.setBackgroundColor(display.getSystemColor(SWT.COLOR_YELLOW));
	    	node.setForegroundColor(display.getSystemColor(SWT.COLOR_DARK_RED));
	    	node.setBorderColor(display.getSystemColor(SWT.COLOR_BLACK));
	    	
	    	if(nodeDimension == null)
	    		nodeDimension = node.getSize();
	    	else
	    		nodeDimension.union( node.getSize() );
	    	
	    	nodesRT.put(code, node);
	    }
	    
	    System.out.println("$$$$$$$$$$$$$ Node dimension : " + nodeDimension);
	    
	    edgesRT = new HashMap<Edge, GraphConnection>();
	    
	    graphRT.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
	    for(int code : statePropMapRT.keySet()) {
	    	StateProp stateSource = statePropMapRT.get(code);	    	
	    	
	    	for(String targetNodeName : stateSource.getNeighbours()) {
	    		int targetNodeCode =  runtimeMtx.getStateNameToIndexMap().get(targetNodeName);  //stateNameMap.get(targetNodeName);
	    		
	    		GraphConnection graphConnection = new GraphConnection(graphRT, SWT.NONE, nodesRT.get(code), nodesRT.get(targetNodeCode));
	    		
	    		if(previousEdgeColorRT == null) {	//for storing the color of edge, only at the beginning of this loop
		    		previousLineWidthRT = graphConnection.getLineWidth();
	    			previousEdgeColorRT = graphConnection.getLineColor();
	    		}
	    		
	    		if(code == targetNodeCode)
	    			graphConnection.setCurveDepth(20);
	    		else
	    			graphConnection.setCurveDepth(5);
	    		graphConnection.changeLineColor(display.getSystemColor(SWT.COLOR_BLACK));
	    		edgesRT.put(new Edge(code, targetNodeCode), graphConnection);
	    	}
	    }
	    
	    //int width = graph.getBounds().width;
	    int reqWidth = (int) (nodeDimension.width * nodesRT.size() * 0.75);
	    
	    System.out.println("\t ^^^^^^^^^^^^ Required Width = " + reqWidth);
	    
	    int displayWidth = display.getBounds().width;
	    //int newWidth;
	    graphareaRuntime.setControl(graphRT);
	    Point preferedSize = graphRT.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	    if(preferedSize.x < displayWidth)
	    	graphRT.setPreferredSize(reqWidth, reqWidth/2);
	    else
	    	graphRT.setPreferredSize(preferedSize.x, preferedSize.y);
	   
	    
	    tabFolder.setSelection(graphareaRuntime);
	    
	    SpringLayoutAlgorithm layoutAlgorithm = new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
	    graphRT.setLayoutAlgorithm(layoutAlgorithm, true);
	    
	    modelCheckUI_RT.loadGraphDependencies(edgesRT, previousLineWidthRT, previousEdgeColorRT);
		modelCheckUI_RT.loadMappings(statePropMapRT, stateNameMapRT, symbolTypeMap, symbolMap);
	}

	@Override
	public void createPartControl(Composite parent) {	
		statusLineManager = getViewSite().getActionBars().getStatusLineManager();
		
		display = parent.getDisplay();
		
		GridLayout layoutParent = new GridLayout();
		layoutParent.numColumns = 1;
		parent.setLayout(layoutParent);
		
		rootScrollComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		rootScrollComposite.setLayout(new GridLayout(1, false));
		rootScrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		rootScrollComposite.setExpandHorizontal(true);
		rootScrollComposite.setExpandVertical(true);
		
		mainComposite = new Composite(rootScrollComposite, SWT.NONE);
		rootScrollComposite.setContent(mainComposite);
		
		mainComposite.setLayout(new GridLayout(1, false));
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		
		GetPropertyValues uiProperties = new GetPropertyValues("ui.properties");
		
		if(uiProperties.loadProperties() == false)
			System.out.println("\'ui.properties\' not found");
		else
			props = uiProperties.getProperties();
		
		//<----------------------------------------------------
		tabFolder = new CTabFolder(mainComposite, SWT.TOP | SWT.BORDER);
		tabFolder.setLayout(new GridLayout());
		tabFolder.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		tabFolder.setSimple(false);
		
		graphareaDesignTime = new CTabItem(tabFolder, SWT.NONE);
		if(props != null)
			graphareaDesignTime.setText( props.getProperty("graphDesignTime") );
		graphareaRuntime = new CTabItem(tabFolder, SWT.NONE);
		if(props != null)
			graphareaRuntime.setText( props.getProperty("graphRuntime") );
		        
		graphDT = new Graph(tabFolder, SWT.NONE);
		graphDT.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));			    
		graphareaDesignTime.setControl(graphDT);   
		
		graphRT = new Graph(tabFolder, SWT.NONE);
		graphRT.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));		
		graphareaRuntime.setControl(graphRT);

		
		smlGraph = new Graph(tabFolder, SWT.NONE);
		smlGraph.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,true));	    
		graphareaDesignTime.setControl(smlGraph);
		
		smlGraphRuntime = new Graph(tabFolder, SWT.NONE);
		smlGraphRuntime.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,true));	    
		graphareaRuntime.setControl(smlGraphRuntime);
		
		//<----------------------------------------------------
				
		final Composite radioComposite = new Composite(mainComposite, SWT.BORDER);
		radioComposite.setLayout(new GridLayout(1, false));
		radioComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));		
		
		subRadioComposite = new Composite(radioComposite, SWT.NONE);
		GridLayout radioLayout = new GridLayout(3, false);
		subRadioComposite.setLayout(radioLayout);		
		radioLayout.marginLeft = 0;
		radioLayout.marginHeight = 0;
		radioLayout.marginTop = 0;
		subRadioComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));		
		
					
		graphControlsComposite = new Composite(subRadioComposite, SWT.NONE);
		RowLayout graphControlsLayout = new RowLayout(SWT.HORIZONTAL);
		graphControlsLayout.marginTop = 0;
		graphControlsLayout.marginBottom = 0;
		graphControlsComposite.setLayout(graphControlsLayout);
		GridData radioGrid = new GridData();
		radioGrid.grabExcessHorizontalSpace = true;
		radioGrid.horizontalAlignment = SWT.RIGHT;
		graphControlsComposite.setLayoutData(radioGrid);
		
		refreshButton = new Button(graphControlsComposite, 0);
		if(props != null)
			refreshButton.setText( props.getProperty("refreshButton") );
		refreshButton.setEnabled(areGraphControlsEnabled);
		
		exportButton = new Button(graphControlsComposite, 0);
		if(props != null)
			exportButton.setText( props.getProperty("exportButton") );
		exportButton.setEnabled(areGraphControlsEnabled);
		
		
		final Composite contentPanel = new Composite(mainComposite, SWT.BORDER);
	    final StackLayout layout = new StackLayout();
	    contentPanel.setLayout(layout);		
	    contentPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	    
	    subComposite = new Composite(contentPanel, SWT.NONE);	
	    GridLayout radioButtonDTRTLayout = new GridLayout(1, false);
		subComposite.setLayout(radioButtonDTRTLayout);
		subComposite.setLayoutData(new GridData(SWT.FILL, 1, true, false));

	    
	    final Composite radioComposite1 = new Composite(subComposite, SWT.NONE);
	    GridLayout radioComposite1Layout = new GridLayout(1, false);
	    radioComposite1Layout.marginWidth = 0;
	    radioComposite1.setLayout(radioComposite1Layout);
	    radioComposite1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	    final Composite subRadioComposite1 = new Composite(radioComposite1, SWT.BORDER);
		GridLayout radioLayout1 = new GridLayout(2, false);
		subRadioComposite1.setLayout(radioLayout1);		
		subRadioComposite1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));		
		
		radiosDTRT[0] = new Button(subRadioComposite1, SWT.RADIO);
		radiosDTRT[0].setSelection(true);
		radiosDTRT[0].setText("Design Time");
		radiosDTRT[0].setBounds(10, 5, 75, 30);
		radiosDTRT[0].pack();
		
		radiosDTRT[1] = new Button(subRadioComposite1, SWT.RADIO);
		radiosDTRT[1].setSelection(false);
		radiosDTRT[1].setText("Run Time");
		radiosDTRT[1].setBounds(100, 5, 75, 30);
		radiosDTRT[1].pack();
		subRadioComposite1.setSize(radioComposite1.getBorderWidth(), 30);
		
	    
	    final Composite oldModelCheckerContentPanel = new Composite(subComposite, SWT.BORDER);
	    final StackLayout layoutDTRT = new StackLayout();
	    oldModelCheckerContentPanel.setLayout(layoutDTRT);		
	    oldModelCheckerContentPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));  
		
	    
	    final Composite dtComposite = new Composite(oldModelCheckerContentPanel, SWT.NONE);
	    dtComposite.setLayout(new GridLayout(2, false));		
	    dtComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));	
		
	    final Composite dtCompositeBrowse = new Composite(dtComposite, SWT.NONE);
	    dtCompositeBrowse.setLayout(new GridLayout(1, false));		
	    dtCompositeBrowse.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));	
		
		browseComposite = new Composite(dtCompositeBrowse, SWT.NONE);
		browseComposite.setLayout(new GridLayout(3, false));
		browseComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		fileLabel = new Label(browseComposite, SWT.FILL);
		if(props != null)
			fileLabel.setText( props.getProperty("browseLabel") );
		
		fileText = new Text(browseComposite, SWT.READ_ONLY);
		fileText.setBackground( Display.getCurrent().getSystemColor(SWT.COLOR_GRAY) );
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		fileText.setLayoutData(gd);
		
		browseButton = new Button(browseComposite, SWT.PUSH);
		if(props != null){
			browseButton.setText( props.getProperty("browseButton") );		
			
		}
		
		/*	Model Checking UI for Design-time Model	*/
		Composite designTimeMCGroup = new Composite(dtCompositeBrowse, SWT.SHADOW_IN);
		/*if(props != null)
			designTimeMCGroup.setText( props.getProperty("designTimeModelCheckGroup") );*/
		designTimeMCGroup.setLayout(new GridLayout(2, false));
		designTimeMCGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		if(props != null){
			temporalPropertyUI_DT = new TemporalPropertyUI(designTimeMCGroup, SWT.BORDER, props.getProperty("temporalPropertyLabelDT"));
			//
		}
		
		Composite modelCheckButtonCompositeDT = new Composite(dtCompositeBrowse, SWT.NONE);
		modelCheckButtonCompositeDT.setLayout(new GridLayout(2, false));
		modelCheckButtonCompositeDT.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		modelCheckUI_DT = new ModelCheckUI(modelCheckButtonCompositeDT, SWT.NONE, this, display, Model.DESIGN_TIME_MODEL, temporalPropertyUI_DT, props.getProperty("modelCheckButtonDT"));
		//invisibleCompositeDT.setVisible(false);
		
		final Composite rtComposite = new Composite(oldModelCheckerContentPanel, SWT.NONE);
		rtComposite.setLayout(new GridLayout(1, false));		
		rtComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));	
		
		/*	Model Checking UI for Runtime Model	*/
		Composite runtimeMCGroup = new Composite(rtComposite, SWT.SHADOW_IN);
		/*if(props != null)
			runtimeMCGroup.setText( props.getProperty("runtimeModelCheckGroup") );*/
		runtimeMCGroup.setLayout(new GridLayout(1, false));
		runtimeMCGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite loadRuntimeComposite = new Composite(runtimeMCGroup, SWT.NONE);
		GridLayout loadRuntimeLayout = new GridLayout(3, false);
		loadRuntimeLayout.marginWidth = 0;
		loadRuntimeComposite.setLayout(loadRuntimeLayout);
		loadRuntimeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		loadRuntimeButton = new Button(loadRuntimeComposite, SWT.PUSH);
		
		if(props != null)
			loadRuntimeButton.setText( props.getProperty("loadRuntimeButton") );
		
		runtimeLoadStatusText = new Text(loadRuntimeComposite, SWT.READ_ONLY | SWT.CENTER);
		runtimeLoadStatusText.setBackground( Display.getCurrent().getSystemColor(SWT.COLOR_GRAY) );
		gd = new GridData();
		gd.widthHint = LOAD_RUNTIME_STATUS_LENGTH;
		gd.heightHint = SWT.DEFAULT;
		gd.horizontalAlignment = SWT.FILL;
		runtimeLoadStatusText.setLayoutData(gd);		
		
		Composite loadRuntimeTempPropComposite = new Composite(rtComposite, SWT.NONE);
		loadRuntimeTempPropComposite.setLayout(new GridLayout(1, false));
		loadRuntimeTempPropComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		if(props != null){
			runtimeLoadStatusText.setText( props.getProperty("runtimeLoadStatusText_NOT_LOADED") );
		}
		if(props != null) {
			temporalPropertyUI_RT = new TemporalPropertyUI(loadRuntimeTempPropComposite, SWT.BORDER, props.getProperty("temporalPropertyLabelRT"));		
		}		
		
		Composite loadRuntimePermStatesComposite = new Composite(rtComposite, SWT.NONE);
		loadRuntimePermStatesComposite.setLayout(new GridLayout(3, false));
		loadRuntimePermStatesComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
		modelCheckUI_RT = new ModelCheckUI(loadRuntimePermStatesComposite, SWT.NONE, this, display, Model.RUNTIME_MODEL, temporalPropertyUI_RT, props.getProperty("modelCheckButtonRT"));
		verifyRuntimeButton = new Button(loadRuntimePermStatesComposite, SWT.PUSH);
		verifyRuntimeButton.setText( props.getProperty("verifyRuntimeButton") );
		permittedStateButton = new Button(loadRuntimePermStatesComposite, SWT.PUSH);
		permittedStateButton.setText( props.getProperty("permittedStateButton") );		
		
		//SML Composite
		final Composite subSMLComposite = new Composite(contentPanel, SWT.NONE);
		subSMLComposite.setLayout(new GridLayout(1, false));
		subSMLComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));		
			
		Composite smlComposite = new Composite(subSMLComposite, SWT.NONE);		
		GridLayout smlLayout = new GridLayout(3, false);
		smlComposite.setLayout(smlLayout);		
		smlComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false) );		
				
		Composite smlComposite2 = new Composite(subSMLComposite, SWT.NONE);		
		GridLayout smlLayout2 = new GridLayout(4, false);
		smlComposite2.setLayout(smlLayout2);		
		smlComposite2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false) );		
		
		//SML Changes
		browseUMLButton = new Button(smlComposite2, SWT.PUSH);
		browseUMLButton.setText("Browse ");
		
		umlFileLabel = new Label(smlComposite2, SWT.FILL);
		umlFileLabel.setText("JSL/UML File :");
		
		umlFileText = new Text(smlComposite2, SWT.READ_ONLY);
		umlFileText.setBackground( Display.getCurrent().getSystemColor(SWT.COLOR_GRAY) );
		GridData gd2 = new GridData();
		gd2.grabExcessHorizontalSpace = true;
		gd2.horizontalAlignment = SWT.FILL;
		umlFileText.setLayoutData(gd2);		
	
		syntaxSMLButton = new Button(smlComposite2, SWT.PUSH);
		syntaxSMLButton.setText("Syntax ");
		syntaxSMLButton.setSize(browseUMLButton.getSize());		
		
		Composite smlComposite1 = new Composite(subSMLComposite, SWT.NONE);
		GridLayout smlLayout1 = new GridLayout(3, false);
		smlComposite1.setLayout(smlLayout1);		
		smlComposite1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		verifySMLButton = new Button(smlComposite1, SWT.PUSH);
		verifySMLButton.setText("  Verify  ");
		verifySMLButton.setSize(browseUMLButton.getSize());		
		smlFormulaLabel = new Label(smlComposite1, SWT.FILL);
		smlFormulaLabel.setText("CTL Formula   :");
		smlFormulaText = new Text(smlComposite1, SWT.NONE);
		smlFormulaText.setLayoutData(gd2);
		smlFormulaText.setSize(umlFileText.getSize());
		
		
		
		Composite smlCompositeMCResult = new Composite(subSMLComposite, SWT.NONE);
		smlCompositeMCResult.setLayout(new GridLayout(2, false));		
		smlCompositeMCResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Composite runtimeMCGroupSML = new Composite(subSMLComposite, SWT.SHADOW_IN);
		runtimeMCGroupSML.setLayout(new GridLayout(1, false));
		runtimeMCGroupSML.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Composite loadRuntimeCompositeSML = new Composite(runtimeMCGroupSML, SWT.NONE);
		loadRuntimeCompositeSML.setLayout(new GridLayout(4, false));
		
		browseRunTimeButton = new Button(loadRuntimeCompositeSML, SWT.PUSH);
		if(props != null)
			browseRunTimeButton.setText( props.getProperty("loadRuntimeButton") );
		
		runtimeFileText = new Text(loadRuntimeCompositeSML, SWT.READ_ONLY | SWT.CENTER);
		runtimeFileText.setBackground( Display.getCurrent().getSystemColor(SWT.COLOR_GRAY) );
		gd = new GridData();
		gd.widthHint = LOAD_RUNTIME_STATUS_LENGTH;
		gd.heightHint = SWT.DEFAULT;
		gd.horizontalAlignment = SWT.FILL;
		runtimeFileText.setLayoutData(gd);		
		
		runtimeFileText.setText(props.getProperty("runtimeLoadStatusText_NOT_LOADED"));
		
		Button showVarsetButton = new Button(loadRuntimeCompositeSML, SWT.PUSH);
		showVarsetButton.setText("Atomic Propositions");
		showVarsetButton.setEnabled(false);
		
		Button consistencyCheckButton = new Button(loadRuntimeCompositeSML, SWT.PUSH);
		consistencyCheckButton.setText("Check Consistency");
				
		
		Composite pathInspectorComposite = new Composite(mainComposite, SWT.BORDER);
		GridLayout pathInspectorLayout = new GridLayout(1, false);
		pathInspectorComposite.setLayout(pathInspectorLayout);		
		pathInspectorLayout.marginLeft = 0;
		pathInspectorLayout.marginHeight = 0;
		pathInspectorLayout.marginTop = 0;
		pathInspectorComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));	
		showPathInspectorUI= new Button(pathInspectorComposite, SWT.CHECK);
		showPathInspectorUI.setSelection(false);
		showPathInspectorUI.setText("Show Path Inspector");
		showPathInspectorUI.setBounds(10, 5, 75, 30);
		showPathInspectorUI.pack();
		subRadioComposite.setSize(radioComposite.getBorderWidth(), 30);
		
		rootScrollComposite.setMinSize( mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT) );
		
		if(showPathInspectorUI.getSelection())
		{			
			clearDT();
			clearRT();
			mainComposite.pack();
			rootScrollComposite.setMinSize( mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT) );			
			showVarsetButton.setEnabled(false);
			layout.topControl = oldModelCheckerContentPanel;
		}
		else
		{
			clearDT();
			clearRT();
			mainComposite.pack();
			rootScrollComposite.setMinSize( mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT) );	
			showVarsetButton.setEnabled(true);
			layout.topControl = subSMLComposite;
		}
		
		if(radiosDTRT[0].getSelection())
		{
			layoutDTRT.topControl = dtComposite;
		}
		else
		{
			layoutDTRT.topControl = rtComposite;
		}
		mainComposite.pack();
		rootScrollComposite.setMinSize( mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT) );	
		oldModelCheckerContentPanel.layout();
		//invisibleCompositeDT.setVisible(false);
		oldModelCheckerContentPanel.setVisible(true);
		
		contentPanel.layout();
		contentPanel.setVisible(true);
		
		/*	to check when a VIEW is resized		*/
		parent.addListener(SWT.Resize, (event) -> parentCompositeOnResize(event) );	//<----------------------- Java 8 Lambda expression
		/*parent.addListener(SWT.Resize, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				parentCompositeOnResize(event);				
			}
		});*/	
		
		showVarsetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(!umlFileText.getText().isEmpty() && umlFileText.getText().contains(".jsl")){
					Iterator it = NestedModel.varSet.entrySet().iterator();
				    StringJoiner st = new StringJoiner("\n");
				    while (it.hasNext()) {
				        Map.Entry pair = (Map.Entry)it.next();
						String objName = pair.getValue() + ": \t" + pair.getKey().toString().trim();							
						st.add(objName);
				    }
				    
				    MessageDialog.openInformation(new Shell( Display.getCurrent() ), "Atomic Propositions", st.toString());
				}
				else{
			    	MessageDialog.openInformation(new Shell( Display.getCurrent() ), "Atomic Propositions", "Please load JIVE State Language Design Time Model before proceeding further");
				}
			}
		});
		
		consistencyCheckButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(!umlFileText.getText().isEmpty() && runtimeFileText.getText().equals(props.getProperty("runtimeLoadStatusText_LOADED"))){
					boolean isConsistent = NestedModel.checkConsistency();
					String consitencyMessage = "";
					if(isConsistent)
						consitencyMessage = "Design-Time and Run-Time Models are consistent";
					else
						consitencyMessage = "Design-Time and Run-Time Models are not consistent";
				    MessageDialog.openInformation(new Shell( Display.getCurrent() ), "Consistency Check", consitencyMessage);
				}
				else{
			    	MessageDialog.openInformation(new Shell( Display.getCurrent() ), "Variable Set Information", "Please load both Design-Time and Run-Time Models before proceeding further");
				}
			}
		});
		
		
		
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseButtonOnSelected(e);
			}
		});
		
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshButtonOnSelected(e);
			}
		});
		
		exportButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportButtonOnSelected(e);
			}
		});
		
		browseUMLButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(new Shell( Display.getCurrent() ), SWT.OPEN);
				fd.setText("Open UML/JSL file");
				
				String[] filterExtensions = {"*.jsl", "*.uml"};
				fd.setFilterExtensions(filterExtensions);
				
				String fileName = fd.open();
				
				if(fileName == null)
					return;				

				umlFileText.setText(fileName);
				
				tabFolder.setSelection(graphareaDesignTime);
				/*	starting a new Thread	*/
				display.asyncExec(new Runnable() {

					@Override
					public void run() {
						if(fd.getFileName().contains(".uml"))
							NestedModel.umlModeling(fileName);
						else
							NestedModel.Modeling(fileName);
						/*	again, starting a new Thread	*/
						display.asyncExec(new Runnable() {

							@Override
							public void run() {
								loadSMLGraph();
							}
						});
						
						refreshButton.setEnabled(true);
					}
				});
				
				
			}
			
		});

		
		verifySMLButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String formulaText = smlFormulaText.getText().replaceAll(" ","");
				
				List newNodes = new ArrayList<>();
				CTabItem currentTab = tabFolder.getSelection();
				Graph graph = null;
				Map<Edge, GraphConnection> edges = null;
				TreeMap<Integer, GraphNode> nodes;
				if(currentTab.getText().equalsIgnoreCase("Design-time Model")){
					List graphNodes = smlGraph.getNodes();
					graph = smlGraph;
					nodes = nodesSML;
					if(umlFileText.getText().contains(".uml"))
						NestedModel.umlModeling(umlFileText.getText());
					else
						NestedModel.Modeling(umlFileText.getText());
					NestedModel.make_design_current();
				}
				else
				{
					List graphNodes = smlGraphRuntime.getNodes();
					graph = smlGraphRuntime;
					nodes = nodesSMLRuntime;
					NestedModel.RuntimeToModelConvertor(runFileName);
					NestedModel.make_run_current();
				}
				for(int i=1; i<=nodes.size(); i++)
				{
					GraphNode node =  nodes.get(i);
					node.setBackgroundColor(display.getSystemColor(SWT.COLOR_YELLOW));
			    	node.setForegroundColor(display.getSystemColor(SWT.COLOR_DARK_RED));
			    	node.setBorderColor(display.getSystemColor(SWT.COLOR_BLACK));
				}
				Lexer.initialise(formulaText);
				
				nodeList = FormulaEval.eval();
				Dimension nodeDimension = null;
				if(!nodeList.isEmpty() && nodeList != null)
				{
					for(int i=0; i<nodeList.size(); i++)
					{
						GraphNode node =  nodes.get(nodeList.get(i));
						node.setBackgroundColor(display.getSystemColor(SWT.COLOR_GREEN));
						node.setForegroundColor(display.getSystemColor(SWT.COLOR_DARK_RED));
						node.setBorderColor(display.getSystemColor(SWT.COLOR_BLACK));
						nodes.remove(nodeList.get(i));
						nodes.put(Integer.parseInt(nodeList.get(i).toString()), node);
						if(nodeDimension == null)
				    		nodeDimension = node.getSize();
				    	else
				    		nodeDimension.union( node.getSize() );
					}
					graph.applyLayout();
					edges = new HashMap<Edge, GraphConnection>();
				    
				    graph.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
				    
				    
				    for(int code=0; code<NestedModel.adjacencyList.length; code++)
				    {
				    	String nodeName = NestedModel.nodeNames.get(code);
				    	List transitionList = NestedModel.adjacencyList[code];
				    	if(transitionList != null)
				    	{
					    	Iterator it = transitionList.iterator();
					    	while(it.hasNext())
					    	{
					    		int targetNodeCode = (int) it.next();
					    		GraphConnection graphConnection = new GraphConnection(smlGraph, SWT.NONE, nodesSML.get(code), nodesSML.get(targetNodeCode));
					    		
					    		if(previousEdgeColorSML == null)	//for storing the color of edge, only at the beginning of this loop
						    		previousEdgeColorSML = graphConnection.getLineColor();
					    		
					    		if(code == targetNodeCode)
					    			graphConnection.setCurveDepth(20);
					    		else
					    			graphConnection.setCurveDepth(5);
					    		graphConnection.changeLineColor(display.getSystemColor(SWT.COLOR_BLACK));
					    		edges.put(new Edge(code, targetNodeCode), graphConnection);
					    	}
				    	} 	
				    }
				    
				    int reqWidth = (int) (nodeDimension.width * nodes.size() * 0.75);
				    
				    System.out.println("\t ^^^^^^^^^^^^ Required Width = " + reqWidth);
				    	    
				    int displayWidth = display.getBounds().width;
				   		    
				    Point preferedSize = smlGraph.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				    if(preferedSize.x < displayWidth)
				    	graph.setPreferredSize(reqWidth, reqWidth/2);
				    else
				    	graph.setPreferredSize(preferedSize.x, preferedSize.y);
				    
				    SpringLayoutAlgorithm layoutAlgorithm = new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
				    	    
				    graph.setLayoutAlgorithm(layoutAlgorithm, true);
				    
				    if(currentTab.getText().equalsIgnoreCase("Design-time Model")){
						edgesSML = edges;
					}else{
						edgesSMLRuntime = edges;
					}
				}
				else
					MessageDialog.openInformation(new Shell( Display.getCurrent() ), "Information", "The CTL Formula is not true at any state.");
			}
		});
		
		syntaxSMLButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StringBuilder sb = new StringBuilder();
				sb.append("CTL Formula Syntax:");
				sb.append("\n");
				sb.append("F  ::=  ~F  |  F /\\ F  |  F \\/ F  | F --> F |  ( F )  |  T");
				sb.append("\n");
				sb.append("T  ::=  EX [F]   | EF [F]  | EG [ F]  |  AX [F]  | AF [F]  | AG [F]");
				sb.append("\n\n");
				sb.append("Operator Precedence:");
				sb.append("\n");
				sb.append("Negation (~) has the highest precedence, followed by /\\, \\/, and --> in this order.");
				MessageDialog.openInformation(new Shell( Display.getCurrent() ), "Atomic Propositions",sb.toString());
			}
		});
		
		browseRunTimeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if(!umlFileText.getText().isEmpty()){
					if(! runtimeFile.exists()) {		
						MessageDialog.openInformation(new Shell( Display.getCurrent() ), "Information", "Please generate the runtime model by using the State Diagram feature and then click the VERIFY button. For now, select the \"*.txt\" file containing runtime model.");
	
						FileDialog fd = new FileDialog(new Shell( Display.getCurrent() ), SWT.OPEN);
						fd.setText("Open Runtime file");
	
						String[] filterExtensions = {"*.txt"};
						fd.setFilterExtensions(filterExtensions);
	
						runFileName = fd.open();
	
						if(runFileName == null)
							return;				
					}
					else
						runFileName = runtimeFile.getAbsolutePath();
					
					loadedRuntimeFileName = runFileName;
					if(runtimeLoaded)	// if runtime model is already loaded, clear it
						clearRT();	// while clearing 'runtimeLoaded' will be set to false	
	
					tabFolder.setSelection(graphareaRuntime);
					/*	starting a new Thread	*/
					display.asyncExec(new Runnable() {
	
						@Override
						public void run() {
							NestedModel.RuntimeToModelConvertor(runFileName);
							/*	again, starting a new Thread	*/
							display.asyncExec(new Runnable() {
	
								@Override
								public void run() {
									loadSMLRuntimeGraph();
								}
							});
							
							refreshButton.setEnabled(true);
	
							
						/*	if( runtimeFile.exists() )	//if runtimeFile exists, delete that
								if( runtimeFile.delete() == false)
									MessageDialog.openError(new Shell( Display.getCurrent() ), "Error in accessing Runtime file", "Error in deleting \"runtimeFile.txt\"");*/
						}
					});
					
					runtimeFileText.setText( props.getProperty("runtimeLoadStatusText_LOADED") );
				}
				else
				{
					MessageDialog.openInformation(new Shell( Display.getCurrent() ), "Information", "Please load Design Time Model before proceeding further");
				}
			}
		});
		
		
		//Listener for Flow Selector Radio Buttons
		
		showPathInspectorUI.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			    if(!((Button)e.getSource()).getSelection()){
			    	layout.topControl = subSMLComposite;
			    	showVarsetButton.setEnabled(true);
			    	contentPanel.layout();
					contentPanel.setVisible(true);
		        }
			    else{
					layout.topControl = subComposite;
					showVarsetButton.setEnabled(false);
					contentPanel.layout();
					contentPanel.setVisible(true);
		        }
				clearDT();
				clearRT();
				mainComposite.pack();
				rootScrollComposite.setMinSize( mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT) );
			}
			
		});
		
//		radios[1].addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				if(((Button)e.getSource()).getSelection()){
//					layout.topControl = subComposite;
//					contentPanel.layout();
//					contentPanel.setVisible(true);
//		        }
//			}
//		});

		
		radiosDTRT[0].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			    if(((Button)e.getSource()).getSelection()){
			    	layoutDTRT.topControl = dtComposite;
			    	oldModelCheckerContentPanel.layout();
			    	oldModelCheckerContentPanel.setVisible(true);
		        }
			}
			
		});
		
		radiosDTRT[1].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(((Button)e.getSource()).getSelection()){
					layoutDTRT.topControl = rtComposite;
					oldModelCheckerContentPanel.layout();
					oldModelCheckerContentPanel.setVisible(true);
		        }
			}
		});

		
		loadRuntimeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadRuntimeButtonOnSelected(e);
			}
		});
		
		permittedStateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				permittedStateButtonOnSelected(e);
			}
		});
		
		verifyRuntimeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				verifyRuntimeButtonOnSelected(e);
			}
		});
	    
	}
	
	private void parentCompositeOnResize(Event event) {
		rootScrollComposite.setMinSize( mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT) );
	}
	
	private void browseButtonOnSelected(SelectionEvent e) {
		FileDialog fd = new FileDialog(new Shell( Display.getCurrent() ), SWT.OPEN);
		fd.setText("Open UML file");
		
		String[] filterExtensions = {"*.uml"};
		fd.setFilterExtensions(filterExtensions);
		
		String fileName = fd.open();
		
		if(fileName == null)
			return;				

		fileText.setText(fileName);
		
		/*	starting a new Thread	*/
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				boolean isLoaded = loadUMLData(fileName);
				
				/*	again, starting a new Thread	*/
				display.asyncExec( () -> loadGraphDT() );		//<----------- Java 8 Lambda expression
				/*display.asyncExec(new Runnable() {
					@Override
					public void run() {
						loadGraphDT();
					}
				});*/
				
				if(isLoaded) {
					if(areGraphControlsEnabled == false) {
						areGraphControlsEnabled = true;
						refreshButton.setEnabled(areGraphControlsEnabled);
						exportButton.setEnabled(areGraphControlsEnabled);
					}
				}
				else {
					if(areGraphControlsEnabled == true) {
						areGraphControlsEnabled = false;
						refreshButton.setEnabled(areGraphControlsEnabled);
						exportButton.setEnabled(areGraphControlsEnabled);
					}
				}				
				
				mainComposite.pack();
				rootScrollComposite.setMinSize( mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT) );
				
				if( runtimeFile.exists() )	//if runtimeFile exists, delete that
					if( runtimeFile.delete() == false)
						MessageDialog.openError(new Shell( Display.getCurrent() ), "Error in accessing Runtime file", "Error in deleting \"runtimeFile.txt\"");
			}
		});
		
		/*fileText.setText(fileName);
		loadUMLData();		
		loadGraph();		
		subComposite.pack();*/
	}

	private void refreshButtonOnSelected(SelectionEvent e) {
		if(tabFolder.getSelection() == graphareaDesignTime){
			graphDT.applyLayout();
			smlGraph.applyLayout();
		}
		else {	// if selected tab is graphareaRuntime
			if(runtimeLoaded){
				graphRT.applyLayout();
				smlGraphRuntime.applyLayout();
			}
		}
	}
	
	private void exportButtonOnSelected(SelectionEvent e) {
		String title = "Export ";
		
		Graph graph = null;
		if(tabFolder.getSelection() == graphareaDesignTime) {
			title += "Design-time Model";			
			if(graphDT != null)
				graph = graphDT;
			if(smlGraph != null)
				graph = smlGraph;
		}
		else {	// if selected tab is graphareaRuntime
			title += "Runtime Model";
			
			if(! runtimeLoaded) {
				MessageDialog.openWarning(new Shell( Display.getCurrent() ), title, "No image to export. Please load the Runtime Model");
				return;
			}
			else
				if(graphRT != null)	
					graph = graphRT;
				if(smlGraphRuntime != null)
					graph = smlGraphRuntime;
		}
		
		FileDialog fd = new FileDialog(new Shell( Display.getCurrent() ), SWT.SAVE);
		fd.setText(title);
		
		String[] filterNames = {"Portable Network Graphics (*.png)", "Bitmap Image (*.bmp)", "JPEG (*.jpg)", "Scalable Vector Graphics (*.svg)"};
		String[] filterExtensions = {"*.png", "*.bmp", "*.jpg", "*.svg"};
		
		fd.setFilterNames(filterNames);
		fd.setFilterExtensions(filterExtensions);
		
		String fileName = fd.open();
		
		if(fileName == null)
			return;
		
		int fileType = fd.getFilterIndex();
		
		int format = -1;
		switch(fileType) {
			case 0: format = SWT.IMAGE_PNG;
					break;
			case 1: format = SWT.IMAGE_BMP;
					break;
			case 2: format = SWT.IMAGE_JPEG;
					break; 
			case 3: format = -5;	// an unassigned value denoting SVG format
		}
		
		ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog( new Shell( Display.getCurrent() ) );
		ExportImage exportImage = new ExportImage(graph, display, fileName, format);
		
		try {
			monitorDialog.run(true, false, exportImage);
		} catch (InvocationTargetException | InterruptedException e1) {
			System.out.println("An exception occured while displaying the Progress Dialog : " + e1);
		}
				
		MessageDialog.openInformation(new Shell( Display.getCurrent() ), title, "The image has been successfully exported to \'" + fileName + "\'");
	}
	
	private void loadRuntimeButtonOnSelected(SelectionEvent e) {
		String runFileName;
		if(!fileText.getText().equals("")){
			if(! runtimeFile.exists()) {		
				MessageDialog.openInformation(new Shell( Display.getCurrent() ), "Information", "Please generate the runtime model by using the State Diagram feature and then click the VERIFY button. For now, select the \"*.txt\" file containing runtime model.");
				
				FileDialog fd = new FileDialog(new Shell( Display.getCurrent() ), SWT.OPEN);
				fd.setText("Open Runtime file");
							
				String[] filterExtensions = {"*.txt"};
				fd.setFilterExtensions(filterExtensions);
			
				runFileName = fd.open();
			
				if(runFileName == null)
					return;				
			}
			else
				runFileName = runtimeFile.getAbsolutePath();
			
			if(runtimeLoaded)	// if runtime model is already loaded, clear it
				clearRT();	// while clearing 'runtimeLoaded' will be set to false	
			
			runtimeMtx = new RuntimeMtx(symbolMap);
			
			boolean isSuccessful;
			
			isSuccessful = runtimeMtx.readFile(runFileName, new LinkedList<String>( sortedVarsByPosition.values() ) , startStateName);
			
			if(! isSuccessful) {
				MessageDialog.openError(new Shell( Display.getCurrent() ), "ERROR : Incompatible Runtime Model", runtimeMtx.errorMsgCompatibility);
				return;
			}
			//runtimeMtx.display();
			
			if(runtimeMtx.generateStateMaps(symbolTypeMap, valueMapDT) == false) {
				MessageDialog.openError(new Shell( Display.getCurrent() ), "ERROR while loading runtime model", runtimeMtx.errorMsg);
				return;
			}
			
			runtimeLoaded = true;		
			runtimeLoadStatusText.setText( props.getProperty("runtimeLoadStatusText_LOADED") );
			
			statePropMapRT = runtimeMtx.getStatePropMap();
			//statePropMapRT.forEach( (index, stateProp) -> System.out.println(index + " ----> " + stateProp));
				
			valueMapRT = runtimeMtx.getValueMap();
				
			stateNameMapRT = runtimeMtx.getStateNameToIndexMap();
				
			temporalPropertyUI_RT.loadMappings(stateNameMapRT, symbolTypeMap, valueMapRT, varsTypeColl, sortedVarsByPosition);			
			temporalPropertyUI_RT.setDependentComposites(rootScrollComposite, mainComposite);			
			temporalPropertyUI_RT.createUI();			
				
			temporalPropertyUI_RT.pack();
				
			loadGraphRT();
				
			mainComposite.pack();
			rootScrollComposite.setMinSize( mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT) );
		}
		else
		{
			MessageDialog.openInformation(new Shell( Display.getCurrent() ), "Information", "Please load Design Time Model before proceeding further");
		}
	}
	
	private void permittedStateButtonOnSelected(SelectionEvent e) {
		FileDialog fd = new FileDialog(new Shell( Display.getCurrent() ), SWT.OPEN);
		fd.setText("Open permitted states file");
		
		String[] filterExtensions = {"*.txt"};
		fd.setFilterExtensions(filterExtensions);
		
		String fileName = fd.open();
		
		if(fileName == null)
			return;
		
		System.out.println("\n####### Reading permitted states #########");
		if(! permittedStates.isEmpty())
			permittedStates.clear();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));					
			String line;
			
			while((line=br.readLine()) != null) {
				String entry = line.trim();
				String name = null;
				if(entry.compareToIgnoreCase("Start") == 0)
					name = startStateName;
					//name = entry[0];
				else {
					String[] portions = entry.split("\\s*,\\s*");
					//for(String str : portions)
						//System.out.println("str : #" + str + "#");
					int i=0;
					StringJoiner sj = new StringJoiner(", ");
					for(String var : sortedVarsByPosition.values()) {
						//System.out.println(var + "=" + portions[i]);
						sj.add(var + "=" + portions[i]);								
						i++;
					}
					name = sj.toString();
				}
				
				permittedStates.add(name);
				//System.out.println("name : " + name);
			}
			
			br.close();
		} catch (Exception e1) {
			System.out.println("Exception while reading file : " + e1);
		}
	}
	
	private void verifyRuntimeButtonOnSelected(SelectionEvent e) {
		if(! runtimeLoaded) {
			MessageDialog.openWarning(new Shell( Display.getCurrent() ), "Warning", "Please load the Runtime Model");
			return;
		}
		
		resetDesignTimeGraphColor();
		
		modelCheckUI_DT.reset();
		modelCheckUI_RT.reset();
		
		/* create DesigntimeMtx from the already existing representation	*/
		designtimeMtx = new DesigntimeMtx(stateNameMapDT, statePropMapDT);
		//designtimeMtx.display();
		
		tabFolder.setSelection(graphareaDesignTime);
		
		compareDTAndRT();	
	}

	private void compareDTAndRT() {		
		HashSet<String> commonStates = new HashSet<>();
		HashSet<String> illegalStates = new HashSet<>();
		
		int count = 0;
		for(String currStateName : runtimeMtx.getStates()) {
			if(currStateName.compareToIgnoreCase(startStateName) == 0)	// to change the name of "Start" state so as to be compliant with the design-time start state
				currStateName = new String(startStateName);
			
			if(! stateNameMapDT.containsKey(currStateName)) {
				System.out.println("NO corresponding STATE[" + currStateName + "] found in Design-time model");
				
				if(! permittedStates.contains(currStateName))					
					illegalStates.add(currStateName);
				
				continue;
			}
			else {
				count++;
				
				int currStateCode = stateNameMapDT.get(currStateName);
				nodesDT.get(currStateCode).setBackgroundColor( display.getSystemColor(SWT.COLOR_DARK_GREEN) );
				nodesDT.get(currStateCode).setForegroundColor( display.getSystemColor(SWT.COLOR_WHITE));
				coloredNodes.add(currStateCode);
				
				if(! areNodesColored)
					areNodesColored = true;
				
				commonStates.add(currStateName);
			}
		}
		
		int destnCode = nodesDT.lastKey() + 1;		
		for(String stateName : illegalStates) {			
			GraphNode graphNode = new GraphNode(graphDT, SWT.NONE, stateName);
			graphNode.setBackgroundColor( display.getSystemColor(SWT.COLOR_RED) );
			graphNode.setForegroundColor( display.getSystemColor(SWT.COLOR_WHITE));
			
			tempNodeNameToCodeMap.put(stateName, destnCode);
			tempNodes.put(destnCode, graphNode);			
			
			destnCode++;
		}
		
		System.out.println("\n************** After Phase 1 ****************");
		System.out.println("######## Number of common states = " + count);
		System.out.println("######## Number of illegal states = " + illegalStates.size());
		System.out.println("######## Number of states in design-time model = " + stateNameMapDT.size());		
		
		ResultStat resultStat = compareTransitions(commonStates, illegalStates);
		int countIllegalTransVtoV = resultStat.getCountIllegalTransVtoV();
		
		System.out.println("\n************** After Phase 2 ****************");
		System.out.println("######## Number of Matching Transitions = " + resultStat.getCountMatchingTrans());
		System.out.println("######## Number of Illegal Transitions = " + countIllegalTransVtoV);
		System.out.println("######## Number of transitions in design-time model = " + designtimeMtx.getTotalTransitions());
		
		System.out.println("--------------------------------------------------");
		
		if( illegalStates.isEmpty() && countIllegalTransVtoV==0) {
			String message = "CONSISTENT";
			
			System.out.println("\n\n\t" + message);
			
			statusLineManager.setMessage(message);
			
			MessageDialog.openInformation(new Shell( Display.getCurrent() ), "Result", "The Runtime Model IS CONSISTENT with Design-time Model");
		}
		else {
			if(! illegalStates.isEmpty())
				graphDT.applyLayout();
			
			String message = "NOT CONSISTENT";
			
			System.out.println("\n\n\t" + message);
			
			String illegalMessage = "Number of illegal states found : " + illegalStates.size();
			System.out.println(illegalMessage);
			
			String invalidMessage = "Number of invalid transitions found : " + countIllegalTransVtoV;
			System.out.println(invalidMessage);
			
			StringBuffer sb = new StringBuffer(message);
			sb.append(" | ").append(illegalMessage).append(" | ").append(invalidMessage);
			
			statusLineManager.setMessage( sb.toString() );
			
			MessageDialog.openInformation(new Shell( Display.getCurrent() ), "Result", "The Runtime Model is NOT CONSISTENT with Design-time Model");
		}
	}
	
	/* Compare transitions of Design-time and Runtime matrices	*/
	private ResultStat compareTransitions(HashSet<String> commonStates, HashSet<String> illegalStates) {
		int countMatchingTrans = 0;
		int countIllegalTransVtoV = 0;		
		
		HashSet<String> missingStates = new HashSet<>( stateNameMapDT.keySet() );
		missingStates.removeAll(commonStates);		
		
		for(int indexSrcDT : designtimeMtx.getIndexToStateNameMap().keySet()) {
			String s = designtimeMtx.getIndexToStateNameMap().get(indexSrcDT);
			
			if(! missingStates.contains(s)) {
				Set<String> neighboursOfSInRT = runtimeMtx.getNeighbours(s);
				
				for(String nb : neighboursOfSInRT) {
					if( commonStates.contains(nb) ) {						
						int codeSource = stateNameMapDT.get(s);
						int codeDestination = stateNameMapDT.get(nb);
						
						int indexDestDT = designtimeMtx.getStateNameToIndexMap().get(nb);
						
						if(designtimeMtx.adjMatrix[indexSrcDT][indexDestDT] == 1) {	// if the transition is in both DT and RT 
							countMatchingTrans++;
							//System.out.println(s + "->" + nb + " : GREEN");
							
							Color color = display.getSystemColor(SWT.COLOR_DARK_GREEN);
							colorEdgeDT(codeSource, codeDestination, color);
						}
						else {														// if the transition is only in RT 
							countIllegalTransVtoV++;
							//System.out.println(s + "->" + nb + " : MAGENTA");
																					
							//Color color = display.getSystemColor(SWT.COLOR_MAGENTA);
							Color color = display.getSystemColor(SWT.COLOR_RED);						
							createTempEdge(codeSource, codeDestination, color);
						}
					}
					else if( illegalStates.contains(nb) ) {
						//System.out.println(s + "->" + nb + " : RED");
						
						int codeSource = stateNameMapDT.get(s);
						int codeDestination = tempNodeNameToCodeMap.get(nb);
						
						Color color = display.getSystemColor(SWT.COLOR_RED);					
						createTempEdge(codeSource, codeDestination, color);
					}
				}				
			}
		}
		
		for(String iS : illegalStates) {
			Set<String> neighboursOfISInRT = runtimeMtx.getNeighbours(iS);
			
			for(String nb : neighboursOfISInRT) {
				if( illegalStates.contains(nb) ) {
					//System.out.println(iS + "->" + nb + " : RED ------------");
					
					int codeSource = tempNodeNameToCodeMap.get(iS);						
					int codeDestination = tempNodeNameToCodeMap.get(nb);
					
					Color color = display.getSystemColor(SWT.COLOR_RED);				
					createTempEdge(codeSource, codeDestination, color);
				}
				else if( commonStates.contains(nb) ) {
					//System.out.println(iS + "->" + nb + " : RED ------------");
					
					int codeSource = tempNodeNameToCodeMap.get(iS);						
					int codeDestination = stateNameMapDT.get(nb);					
					
					Color color = display.getSystemColor(SWT.COLOR_RED);				
					createTempEdge(codeSource, codeDestination, color);
				}
			}
		}
		
		/*	intermediateStates <- ((permittedStates - commonStates) INTERSECTION runtimeStates)	*/
		HashSet<String> intermediateStates = new HashSet<>( permittedStates );
		intermediateStates.removeAll(commonStates);
		intermediateStates.retainAll( runtimeMtx.getStates() );
		
		System.out.println("Intermediate States : " + intermediateStates);
		
		for(String interState : intermediateStates) {
			Set<String> nonInterParents = runtimeMtx.findNonIntermediateParents(interState, intermediateStates);
			Set<String> nonInterChildren = runtimeMtx.findNonIntermediateChildren(interState, intermediateStates);			
			
			//System.out.println("Parents of " + interState + " : " + nonInterParents);
			//System.out.println("Children of " + interState + " : " + nonInterChildren);
			
			if((! nonInterParents.isEmpty()) && (! nonInterChildren.isEmpty())) {
				for(String parent : nonInterParents)
					for(String child : nonInterChildren) {
						//System.out.println(parent + "->" + child + " : RED ==========");
						
						int codeSource, codeDestination;
						if( commonStates.contains(parent) )
							codeSource = stateNameMapDT.get(parent);
						else	// parent is an illegalState
							codeSource = tempNodeNameToCodeMap.get(parent);
						
						if( commonStates.contains(child) )
							codeDestination = stateNameMapDT.get(child);
						else	// child is an illegalState							
							codeDestination = tempNodeNameToCodeMap.get(child);
						
						/* create an edge only if it is not already existing in 'tempEdges'	*/
						
						boolean exists = tempEdges.keySet().stream()
								.parallel()
								.anyMatch( (edge) -> edge.equals(new Edge(codeSource, codeDestination)) ); 		// <--------------------- Java 8 Lambda expression
						
						if(! exists ) {
							Color color = display.getSystemColor(SWT.COLOR_RED);
							//Color color = display.getSystemColor(SWT.COLOR_BLUE);
							createTempEdge(codeSource, codeDestination, color);
						}
						
						/*if(tempEdges.containsKey(new Edge(codeSource, codeDestination)) == false) {
							Color color = display.getSystemColor(SWT.COLOR_RED);
							//Color color = display.getSystemColor(SWT.COLOR_BLUE);
							createTempEdge(codeSource, codeDestination, color);
						}*/
					}
			}
		}
		
		return new ResultStat(countMatchingTrans, countIllegalTransVtoV);
	}
	
	private void createTempEdge(int codeSource, int codeDestination, Color color) {
		GraphNode nodeSource;
		if( nodesDT.containsKey(codeSource) )
			nodeSource = nodesDT.get(codeSource);
		else
			nodeSource = tempNodes.get(codeSource);
		
		GraphNode nodeDestination;
		if( nodesDT.containsKey(codeDestination) )
			nodeDestination = nodesDT.get(codeDestination);
		else
			nodeDestination = tempNodes.get(codeDestination);
		
		GraphConnection graphConnection = new GraphConnection(graphDT, SWT.NONE, nodeSource, nodeDestination);
		
		if(nodeSource == nodeDestination)
			graphConnection.setCurveDepth(20);
		else
			graphConnection.setCurveDepth(5);
		
		graphConnection.setLineColor(color);
		
		tempEdges.put(new Edge(codeSource, codeDestination), graphConnection);
	}
	
	private void colorEdgeDT(int codeSource, int codeDestination, Color color) {
		Edge edgeToColor = new Edge(codeSource, codeDestination);
		edgesDT.get(edgeToColor).setLineColor(color);
		
		coloredEdgesDT.add(edgeToColor);
		
		if(! areEdgesColoredDT)
			areEdgesColoredDT = true;
	}
	
	public void setGraphTab(Model model) {
		switch(model) {
		case DESIGN_TIME_MODEL	: tabFolder.setSelection(graphareaDesignTime);
								  break;
		case RUNTIME_MODEL		: tabFolder.setSelection(graphareaRuntime);
		}
	}
	
	public void resetDesignTimeGraphColor() {
		statusLineManager.setMessage(null);
		
		clearTempNodesAndEdgesDT();
		
		resetColoredEdgesDT();
		resetColoredNodesDT();
	}
	
	private void resetColoredEdgesDT() {
		if(areEdgesColoredDT) {
			for(Edge e : coloredEdgesDT) {
				GraphConnection graphConnection = edgesDT.get(e);
				
				graphConnection.setLineColor(previousEdgeColorDT);
			}
			
			areEdgesColoredDT = false;
			coloredEdgesDT.clear();
		}
	}
	
	private void resetColoredNodesDT() {
		if(areNodesColored) {
			for(int code : coloredNodes) {
				GraphNode node = nodesDT.get(code);
				node.setBackgroundColor(previousNodeBackgroundColorDT);
				node.setForegroundColor(previousNodeForegroundColorDT);
			}
			
			areNodesColored = false;
			coloredNodes.clear();
		}
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

}
