package modelChecker.views.viewpart;

import graphSearch.Datatype;
import graphSearch.StateProp;
import graphSearch.SymbolMap;
import graphSearch.TemporalProperty;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.zest.core.widgets.GraphConnection;

public class ModelCheckUI extends Composite {
	private final static int LINE_WIDTH = 2;
	private final static int MODEL_CHECK_STATUS_LENGTH = 400;	// the length is 400 pixels
	
	private Button mcButton;
	private StyledText stResult;
	
	private ModelCheckerView modelCheckerView;
	private Display display;
	
	private Model model;
	
	private TemporalPropertyUI temporalPropertyUI;
	
	private Map<Edge, GraphConnection> edges;
	private Set<Edge> coloredEdges;
	
	private boolean isLineWidthChanged;
	private boolean areEdgesColored;
	
	private int previousLineWidth;
	private Color previousEdgeColor;
	
	private Map<Integer, StateProp> statePropMap;
	private Map<String, Integer> stateNameMap;
	private Map<Integer, Datatype> symbolTypeMap;
	private SymbolMap symbolMap;

	public ModelCheckUI(Composite parent, int style, ModelCheckerView modelCheckerView, Display display, Model model, TemporalPropertyUI temporalPropertyUI, String modelCheckButtonText) {
		super(parent, style);
		
		this.modelCheckerView = modelCheckerView;
		this.display = display;
		this.model = model;
		this.temporalPropertyUI = temporalPropertyUI;
		
		setLayout(new GridLayout(1, false));		
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		mcButton = new Button(this, SWT.PUSH);
		mcButton.setText(modelCheckButtonText);
		
		//stResult = new StyledText(this, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
		//stResult.setAlignment(SWT.CENTER);		
		
		GridData gridData = new GridData();
		gridData.widthHint = MODEL_CHECK_STATUS_LENGTH;
		gridData.heightHint = SWT.DEFAULT;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		//stResult.setLayoutData(gridData);
		
		isLineWidthChanged = false;
		areEdgesColored = false;
		
		coloredEdges = new HashSet<Edge>();
		
		mcButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mcButtonOnSelected(e);
			}			
		});
	}

	public void loadGraphDependencies(Map<Edge, GraphConnection> edges, int previousLineWidth, Color previousEdgeColor) {
		this.edges = edges;
		this.previousLineWidth = previousLineWidth;		
		this.previousEdgeColor = previousEdgeColor;
	}
	
	public void loadMappings(Map<Integer, StateProp> statePropMap, Map<String, Integer> stateNameMap, Map<Integer, Datatype> symbolTypeMap, SymbolMap symbolMap) {
		this.statePropMap = statePropMap;
		this.stateNameMap = stateNameMap;
		this.symbolTypeMap = symbolTypeMap;
		this.symbolMap = symbolMap;
	}
	
	public void colorEdge(int codeSource, int codeDestination, Color color) {
		Edge edgeToColor = new Edge(codeSource, codeDestination);
		
		GraphConnection graphConnection = edges.get(edgeToColor);
		graphConnection.setLineColor(color);
		graphConnection.setLineWidth(LINE_WIDTH);		
		
		coloredEdges.add(edgeToColor);
		if(! isLineWidthChanged)
			isLineWidthChanged = true;
		
		if(! areEdgesColored)
			areEdgesColored = true;		
	}
	
	public void reset() {
		if(areEdgesColored)
			resetColoredEdges();
		
		//stResult.setText("");
	}
	
	public void resetColoredEdges() {
		if(areEdgesColored) {
			for(Edge e : coloredEdges) {
				GraphConnection graphConnection = edges.get(e);
				
				graphConnection.setLineColor(previousEdgeColor);
				
				if(isLineWidthChanged)
					graphConnection.setLineWidth(previousLineWidth);
			}
			
			areEdgesColored = false;
			coloredEdges.clear();
			
			if(isLineWidthChanged)
				isLineWidthChanged = false;
		}
	}
	
	private void resetGraphColor() {
		resetColoredEdges();		
		modelCheckerView.resetDesignTimeGraphColor();
	}
	
	private void mcButtonOnSelected(SelectionEvent e) {
		resetGraphColor();
		
		switch(model) {
		case DESIGN_TIME_MODEL 	: if(modelCheckerView.umlLoaded == false) {
									MessageDialog.openError(new Shell( Display.getCurrent() ), "No Input File", "Please specify a UML file");			
									return;
								  }
								  break;
								  
		case RUNTIME_MODEL 		: if(modelCheckerView.runtimeLoaded == false) {
									MessageDialog.openError(new Shell( Display.getCurrent() ), "Runtime Model Not Loaded", "Please load the runtime model");			
									return;
								  }
		}
		
		
		if(temporalPropertyUI.validate() == false)
			return;
		
		int status = doModelChecking();
		
		//setMCStatus(status);
		
		modelCheckerView.setGraphTab(model);
	}
	
	private int doModelChecking() {
		TemporalProperty tp = null;
		try {
			tp = TemporalProperty.valueOf( temporalPropertyUI.getSelectedTemporalProperty() );
		}
		catch(IllegalArgumentException e) {
			return -1;
		}		
		
		int status = -1;
		int startStateCode = temporalPropertyUI.getSourceStateIndex() - 1;	// code of state is the index of selection minus the default entry
		
		ModelCheck modelCheck = new ModelCheck(this, display, statePropMap, stateNameMap, symbolTypeMap, symbolMap);
		modelCheck.loadProperties(temporalPropertyUI.getPropsList_first(), temporalPropertyUI.getPropsList_second());
		
		switch(tp) {		
			case AX :	status = modelCheck.checkAX(startStateCode);
						break;
			case EX : 	status = modelCheck.checkEX(startStateCode);
						break;
			case AG : 	status = modelCheck.checkAG(startStateCode);
						break;
			case EG : 	status = modelCheck.checkEG(startStateCode);
						break;
			case AF : 	status = modelCheck.checkAF(startStateCode);
						break;
			case EF :	status = modelCheck.checkEF(startStateCode);
						break;
			case AU :	status = modelCheck.checkAU(startStateCode);
						break;
			case EU :	status = modelCheck.checkEU(startStateCode);
		}
		return status;					
	}
	
	private void setMCStatus(int status) {
		String messagePrefix = "The temporal property is ";
		String result, message;
		
		StyleRange[] styleRanges = new StyleRange[2];
		Color foregroundColor, backgroundColor;
		
		switch(status) {
			case 1 :	//MessageDialog.openInformation(new Shell( Display.getCurrent() ), "Result", "The temporal property is SATISFIED");
						result = "SATISFIED";
						message = messagePrefix + result;
						
						backgroundColor = display.getSystemColor(SWT.COLOR_GREEN);
						foregroundColor = display.getSystemColor(SWT.COLOR_BLACK);
						
						stResult.setText(message);
						stResult.setLineBackground(0, 1, backgroundColor);
						
						styleRanges[0] = new StyleRange(0, messagePrefix.length(), foregroundColor, null);						
						
						styleRanges[1] = new StyleRange();
						styleRanges[1].start = message.indexOf(result);
						styleRanges[1].length = result.length();
						styleRanges[1].foreground = foregroundColor;
						styleRanges[1].fontStyle = SWT.BOLD;
						
						stResult.setStyleRanges(styleRanges);
						
						break;
					
			case 0 :	//MessageDialog.openInformation(new Shell( Display.getCurrent() ), "Result", "The temporal property is NOT SATISFIED");
						result = "NOT SATISFIED";
						message = messagePrefix + result;
						
						backgroundColor = display.getSystemColor(SWT.COLOR_RED);
						foregroundColor = display.getSystemColor(SWT.COLOR_WHITE);
						
						stResult.setText(message);	 
						stResult.setLineBackground(0, 1, backgroundColor);						
						
						styleRanges[0] = new StyleRange(0, messagePrefix.length(), foregroundColor, null);						
												
						styleRanges[1] = new StyleRange();
						styleRanges[1].start = message.indexOf(result);
						styleRanges[1].length = result.length();
						styleRanges[1].foreground = foregroundColor;
						styleRanges[1].fontStyle = SWT.BOLD;
						
						stResult.setStyleRanges(styleRanges);
						break;
					
			case -1:	MessageDialog.openError(new Shell( Display.getCurrent() ), "Not Supported", "The operator \'" + temporalPropertyUI.getSelectedTemporalProperty() + "\' is NOT SUPPORTED yet!");
						System.out.println("Not supported yet!");
		}
	}
}
