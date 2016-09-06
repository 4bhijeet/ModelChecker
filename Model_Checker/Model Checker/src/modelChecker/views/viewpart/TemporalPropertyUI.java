package modelChecker.views.viewpart;

import graphSearch.Datatype;
import graphSearch.TemporalProperty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import util.PropertyUI;
import util.PropertyWrapper;

public class TemporalPropertyUI extends Composite {
	private final static String DEF = "?";
	
	private Label temporalPropertyLabel;
	
	private Combo cbTP;
	private Combo cbStateSource;
	
	private HashMap<Integer, PropertyUI> propertyUIs_first;
	private HashMap<Integer, PropertyUI> propertyUIs_second;
	
	private Composite expandComposite_first;
	private Button plus_first, minus_first;
	private LinkedList<PropertyWrapper> propsList_first;
	
	private Composite expandComposite_second;
	private Button plus_second, minus_second;
	private LinkedList<PropertyWrapper> propsList_second;
	
	private Map<String, Integer> stateNameMap;
	private Map<Integer, Datatype> symbolTypeMap;
	private Map<Integer, Set<String>> valueMap;
	private TreeMap<Integer, String> sortedVarsByPosition;
	private Map<Datatype, Set<Integer>> varsTypeColl;
	
	private Label untilLabel;
	
	private ScrolledComposite rootScrollComposite;
	private Composite mainComposite;
	
	private boolean secondPropertiesEnabled;

	public TemporalPropertyUI(Composite parent, int style, String labelText) {
		super(parent, style);
		
		RowLayout temporalLayout = new RowLayout(SWT.HORIZONTAL);
		temporalLayout.marginTop = 20;
		setLayout(temporalLayout);
		
		temporalPropertyLabel = new Label(this, SWT.NONE);
		temporalPropertyLabel.setText(labelText);	
		
		setSize( computeSize(SWT.DEFAULT, SWT.DEFAULT) );
	}
	
	public void setDependentComposites(ScrolledComposite rootScrollComposite, Composite mainComposite) {
		this.rootScrollComposite = rootScrollComposite;
		this.mainComposite = mainComposite;
	}

	public void loadMappings(Map<String, Integer> stateNameMap, Map<Integer, Datatype> symbolTypeMap, Map<Integer, Set<String>> valueMap, Map<Datatype, Set<Integer>> varsTypeColl, TreeMap<Integer, String> sortedVarsByPosition) {
		this.stateNameMap = stateNameMap;
		this.symbolTypeMap = symbolTypeMap;
		this.valueMap = valueMap;
		this.varsTypeColl = varsTypeColl;
		this.sortedVarsByPosition = sortedVarsByPosition;
	}
	
	public void createUI() {
		cbTP = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		cbTP.add(DEF, 0);
		cbTP.select(0);
		
		for(TemporalProperty tp : TemporalProperty.values())
			cbTP.add( tp.toString() );
		
		/*cbTP.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				cbTPOnSelection(event);
			}
		});*/
		cbTP.addListener(SWT.Selection, (event) -> cbTPOnSelection(event));	//<----------------------- Java 8 Lambda expression
		
		cbTP.pack();
		
		cbStateSource = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		cbStateSource.add(DEF, 0);
		cbStateSource.select(0);
		
		/*	StateNameMap contains <StateName, Code>	*/
		stateNameMap.keySet().forEach( (stateName) -> cbStateSource.add(stateName) );		//<--- Java 8 lambda expression		
		
		cbStateSource.pack();	
		
		/*	propertyUIs_first stores a map of PropertyUIs based on position of the corresponding state property in the UI, of the part before the UNTIL	*/
		propertyUIs_first = new HashMap<Integer, PropertyUI>();
		
		/* expandComposite expands and shrinks vertically corresponding to the pressing of '+' or '-' buttons by the user	*/
		expandComposite_first = new Composite(this, SWT.BORDER);
		expandComposite_first.setLayout(new RowLayout(SWT.VERTICAL));
		
		/* propsComposite holds the PropertyUIs corresponding to the state properties	*/
		Composite propsComposite_first = new Composite(expandComposite_first, SWT.NONE);		
		propsComposite_first.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		untilLabel = new Label(this, SWT.FILL);
		untilLabel.setText("UNTIL");
		untilLabel.setEnabled(false);
		
		/*	propertyUIs_first stores a map of PropertyUIs based on position of the corresponding state property in the UI, of the part after the UNTIL	*/
		propertyUIs_second = new HashMap<Integer, PropertyUI>();
		
		/* expandComposite expands and shrinks vertically corresponding to the pressing of '+' or '-' buttons by the user	*/
		expandComposite_second = new Composite(this, SWT.BORDER);
		expandComposite_second.setLayout(new RowLayout(SWT.VERTICAL));
		
		/* propsComposite holds the PropertyUIs corresponding to the state properties	*/
		Composite propsComposite_second = new Composite(expandComposite_second, SWT.NONE);		
		propsComposite_second.setLayout(new RowLayout(SWT.HORIZONTAL));		
		
		secondPropertiesEnabled = false;
		
		for(Map.Entry<Integer, String> entry : sortedVarsByPosition.entrySet()) {
			int position = entry.getKey();
			String symbolName = entry.getValue();
						
			PropertyUI propertyUI_first = new PropertyUI(propsComposite_first, SWT.NONE, symbolTypeMap.get(position));
			propertyUI_first.setVarName(symbolName);			
			
			PropertyUI propertyUI_second = new PropertyUI(propsComposite_second, SWT.NONE, symbolTypeMap.get(position));
			propertyUI_second.setVarName(symbolName);
			//<-----------------------------
			Set<Integer> depVarPosts = varsTypeColl.get( symbolTypeMap.get(position) );
			
			if(depVarPosts.size() > 1) {
				Set<String> depVars = new HashSet<>();
				for(int pos : depVarPosts)
					if(pos != position)
						depVars.add( "\'" + sortedVarsByPosition.get(pos) + "\'" );
				System.out.println("########## For var:" + symbolName + ", the other vars are : " +  depVars);
				
				Set<String> augmentedSet = new HashSet<>();
				
				augmentedSet.addAll( valueMap.get(position) );
				augmentedSet.addAll(depVars);
				
				propertyUI_first.addVarValues(augmentedSet);		
				propertyUI_second.addVarValues(augmentedSet);		
			}
			else {
				propertyUI_first.addVarValues( valueMap.get(position) );
				propertyUI_second.addVarValues( valueMap.get(position) );
			}
			//<---------------------------------
			
			propertyUI_second.setEnabled( secondPropertiesEnabled );
			
			propertyUIs_first.put(position, propertyUI_first);
			propertyUIs_second.put(position, propertyUI_second);
		}
		
		/*	"propsList_first" and "propsList_second" stores each Property Listing UI, which will get added or removed when user presses '+' or '-' buttons	*/
		propsList_first = new LinkedList<PropertyWrapper>();		
		propsList_first.add(new PropertyWrapper(propsComposite_first, propertyUIs_first));
		
		propsList_second = new LinkedList<PropertyWrapper>();
		propsList_second.add(new PropertyWrapper(propsComposite_second, propertyUIs_second));
		
		plus_first = new Button(propsComposite_first, SWT.PUSH);
		plus_first.setText("+");
		
		minus_first = new Button(propsComposite_first, SWT.PUSH);
		minus_first.setText("-");
		minus_first.setEnabled(false);
		
		plus_second = new Button(propsComposite_second, SWT.PUSH);
		plus_second.setText("+");
		plus_second.setEnabled( secondPropertiesEnabled );
		
		minus_second = new Button(propsComposite_second, SWT.PUSH);
		minus_second.setText("-");
		minus_second.setEnabled(false);
		
		plus_first.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {	
				plus_firstOnSelected(e);
			}			
		});
		
		minus_first.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {	
				minus_firstOnSelected(e);
			}			
		});
		
		plus_second.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {	
				plus_secondOnSelected(e);
			}			
		});
		
		minus_second.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {	
				minus_secondOnSelected(e);
			}			
		});
	}
	
	public boolean validate() {
		if(cbTP.getSelectionIndex()==0 || cbStateSource.getSelectionIndex()==0) {	// index 0 has 'DEF' text
			MessageDialog.openError(new Shell( Display.getCurrent() ), "Missing Field", "Please select a value in all fields");
			return false;
		}
		
		for(PropertyWrapper pw : propsList_first) {
			for(PropertyUI p : pw.getPropertyUIs_any().values())
				if(p.validate() == false) {
					MessageDialog.openError(new Shell( Display.getCurrent() ), "Datatype Error", p.validateErrorMsg);
					p.setFocusOnOther();
					return false;
				}
		}
		
		if(secondPropertiesEnabled) {
			for(PropertyWrapper pw : propsList_second) {
				for(PropertyUI p : pw.getPropertyUIs_any().values())
					if(p.validate() == false) {
						MessageDialog.openError(new Shell( Display.getCurrent() ), "Datatype Error", p.validateErrorMsg);
						p.setFocusOnOther();
						return false;
					}
			}
		}
		
		System.out.println("To check : " + getTemporalPropertyText());
		
		return true;
	}
	
	public String getTemporalPropertyText() {
		StringBuffer sb = new StringBuffer();
		sb.append( cbTP.getText() );
		sb.append(" (").append( cbStateSource.getText() ).append(")  <");
		
		sb.append( getSelectedPropertyText_any(propsList_first) );
		
		if(secondPropertiesEnabled) {
			sb.append("\tUNTIL\t");
			sb.append( getSelectedPropertyText_any(propsList_second) );
		}
		sb.append(">");
		
		return sb.toString();
	}
	
	private String getSelectedPropertyText_any(LinkedList<PropertyWrapper> propsList_any) {
		StringBuffer sb = new StringBuffer();
		
		for(PropertyWrapper pw : propsList_any) {
			if(! pw.isFirstEntry())
				sb.append(" ").append( pw.getCompCombo().getText() ).append(" ");
			
			StringJoiner sj = new StringJoiner(", ");
			for(PropertyUI p : pw.getPropertyUIs_any().values())
				sj.add(p.getVar() + p.getRelOp() + p.getValue());
			sb.append( sj.toString() );
		}
		
		return sb.toString();
	}
	
	public void clearLoadedItems() {
		if(cbTP !=null)
		cbTP.dispose();
		if(cbStateSource !=null)
		cbStateSource.dispose();
		
		if(plus_first != null)
		plus_first.dispose();
		if(minus_first != null)
		minus_first.dispose();
		
		if(propertyUIs_first != null){
			for(PropertyWrapper pw : propsList_first) {
				if(! pw.isFirstEntry())
					pw.getCompCombo().dispose();
				
				pw.getPropertyUIs_any().forEach( (position, propertyUI) -> propertyUI.dispose() );	//<----- Java 8 Lambda expression
				
				propertyUIs_first.clear();
				
				pw.getPropsComposite().dispose();
			}	
			propsList_first.clear();
		}
		
		
		if(expandComposite_first!=null)
		expandComposite_first.dispose();
		if(untilLabel!=null)
		untilLabel.dispose();
		
		if(plus_second!=null)
		plus_second.dispose();
		
		if(minus_second!=null)
		minus_second.dispose();
		
		if(propertyUIs_second != null){
			for(PropertyWrapper pw : propsList_second) {
				if(! pw.isFirstEntry())
					pw.getCompCombo().dispose();
				
				pw.getPropertyUIs_any().forEach( (position, propertyUI) -> propertyUI.dispose() );	//<----- Java 8 Lambda expression
				
				propertyUIs_second.clear();
				
				pw.getPropsComposite().dispose();
			}		
			propsList_second.clear();
		}
		if(expandComposite_second!=null)
		expandComposite_second.dispose();
	}
	
	public int getSourceStateIndex() {
		return cbStateSource.getSelectionIndex();
	}
	
	public String getSelectedTemporalProperty() {
		return cbTP.getText();
	}

	public LinkedList<PropertyWrapper> getPropsList_first() {
		return propsList_first;
	}
	
	public LinkedList<PropertyWrapper> getPropsList_second() {
		return propsList_second;
	}

	private void cbTPOnSelection(Event event) {
		String selected = cbTP.getText();
		
		if(selected.equals(DEF)) {
			if(secondPropertiesEnabled == true) {
				untilLabel.setEnabled(false);
				
				for(PropertyWrapper pw : propsList_second) {					
					if(! pw.isFirstEntry())	// the first entry does not have compCombo that lists {AND, OR}
						pw.getCompCombo().setEnabled(false);
					
					pw.getPropertyUIs_any().forEach( (position, propertyUI) -> propertyUI.setEnabled(false) );	//<----------- Java 8 Lambda expression
				}
				plus_second.setEnabled(false);
				minus_second.setEnabled(false);
				
				secondPropertiesEnabled = false;
			}
			 
			return;
		}
		
		TemporalProperty selectedProperty = null;
		
		try {
			selectedProperty = TemporalProperty.valueOf(selected);
		}
		catch(IllegalArgumentException e) {
			return;
		}
		
		if(selectedProperty == TemporalProperty.AU || selectedProperty == TemporalProperty.EU) {
			if(secondPropertiesEnabled == false) {
				untilLabel.setEnabled(true);
				
				for(PropertyWrapper pw : propsList_second) {
					if(! pw.isFirstEntry())	// the first entry does not have compCombo that lists {AND, OR}
						pw.getCompCombo().setEnabled(true);
					
					pw.getPropertyUIs_any().forEach( (position, propertyUI) -> propertyUI.setEnabled(true) );	//<----------- Java 8 Lambda expression
				}
				plus_second.setEnabled(true);
				if(propsList_second.size() != 1)
					minus_second.setEnabled(true);
				
				secondPropertiesEnabled = true;
			}
		}
		else {
			if(secondPropertiesEnabled == true) {
				untilLabel.setEnabled(false);
				
				for(PropertyWrapper pw : propsList_second) {					
					if(! pw.isFirstEntry())	// the first entry does not have compCombo that lists {AND, OR}
						pw.getCompCombo().setEnabled(false);
					
					pw.getPropertyUIs_any().forEach( (position, propertyUI) -> propertyUI.setEnabled(false) );	//<----------- Java 8 Lambda expression
				}
				plus_second.setEnabled(false);
				minus_second.setEnabled(false);
				
				secondPropertiesEnabled = false;
			}
		}
	}
	
	private void plus_firstOnSelected(SelectionEvent e) {
		if(minus_first.getEnabled() == false)
			minus_first.setEnabled(true);
						
		Composite propsComposite1 = new Composite(expandComposite_first, SWT.BORDER);		
		propsComposite1.setLayout(new RowLayout(SWT.HORIZONTAL));
			
		Combo compCombo = new Combo(propsComposite1, SWT.READ_ONLY);					
		compCombo.add("OR", 0);
		compCombo.add("AND");
		compCombo.select(0);
			
		HashMap<Integer, PropertyUI> propertyUIs_temp = new HashMap<>();

		for(Map.Entry<Integer, String> entry : sortedVarsByPosition.entrySet()) {
			int position = entry.getKey();
			String symbolName = entry.getValue();						
				
			PropertyUI propertyUI = new PropertyUI(propsComposite1, SWT.NONE, symbolTypeMap.get(position));
			propertyUI.setVarName(symbolName);
			
			Set<Integer> depVarPosts = varsTypeColl.get( symbolTypeMap.get(position) );
			
			if(depVarPosts.size() > 1) {
				Set<String> depVars = new HashSet<>();
				for(int pos : depVarPosts)
					if(pos != position)
						depVars.add( "\'" + sortedVarsByPosition.get(pos) + "\'" );
				//System.out.println("########## For var:" + symbolName + ", the other vars are : " +  depVars);
				
				Set<String> augmentedSet = new HashSet<>();
				
				augmentedSet.addAll( valueMap.get(position) );
				augmentedSet.addAll(depVars);
				
				propertyUI.addVarValues(augmentedSet);		
			}
			else
				propertyUI.addVarValues( valueMap.get(position) );
			
			propertyUIs_temp.put(position, propertyUI);
		}
			
		propsList_first.add(new PropertyWrapper(propsComposite1, compCombo, propertyUIs_temp));
		
		expandComposite_first.pack();
		rootScrollComposite.setMinSize( mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT) );
	}
	
	private void minus_firstOnSelected(SelectionEvent e) {
		PropertyWrapper pw = propsList_first.removeLast();	// returns and removes the last element from the list
		
		if(propsList_first.size() == 1)
			minus_first.setEnabled(false);
		
		pw.getCompCombo().dispose();
		
		HashMap<Integer, PropertyUI> propertyUIs_temp = pw.getPropertyUIs_any();
		for(PropertyUI pui : propertyUIs_temp.values())
			pui.dispose();
		propertyUIs_temp.clear();
							
		pw.getPropsComposite().dispose();		
		
		expandComposite_first.pack();
		rootScrollComposite.setMinSize( mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT) );
	}
	
	private void plus_secondOnSelected(SelectionEvent e) {
		if(minus_second.getEnabled() == false)
			minus_second.setEnabled(true);
						
		Composite propsComposite1 = new Composite(expandComposite_second, SWT.BORDER);		
		propsComposite1.setLayout(new RowLayout(SWT.HORIZONTAL));
			
		Combo compCombo = new Combo(propsComposite1, SWT.READ_ONLY);					
		compCombo.add("OR", 0);
		compCombo.add("AND");
		compCombo.select(0);
			
		HashMap<Integer, PropertyUI> propertyUIs_temp = new HashMap<>();

		for(Map.Entry<Integer, String> entry : sortedVarsByPosition.entrySet()) {
			int position = entry.getKey();
			String symbolName = entry.getValue();						
				
			PropertyUI propertyUI = new PropertyUI(propsComposite1, SWT.NONE, symbolTypeMap.get(position));
			propertyUI.setVarName(symbolName);			
			
			Set<Integer> depVarPosts = varsTypeColl.get( symbolTypeMap.get(position) );
			
			if(depVarPosts.size() > 1) {
				Set<String> depVars = new HashSet<>();
				for(int pos : depVarPosts)
					if(pos != position)
						depVars.add( "\'" + sortedVarsByPosition.get(pos) + "\'" );
				//System.out.println("########## For var:" + symbolName + ", the other vars are : " +  depVars);
				
				Set<String> augmentedSet = new HashSet<>();
				
				augmentedSet.addAll( valueMap.get(position) );
				augmentedSet.addAll(depVars);
				
				propertyUI.addVarValues(augmentedSet);		
			}
			else
				propertyUI.addVarValues( valueMap.get(position) );	
			
			propertyUIs_temp.put(position, propertyUI);
		}
			
		propsList_second.add(new PropertyWrapper(propsComposite1, compCombo, propertyUIs_temp));					
		
		expandComposite_second.pack();
		rootScrollComposite.setMinSize( mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT) );
	}
	
	private void minus_secondOnSelected(SelectionEvent e) {
		PropertyWrapper pw = propsList_second.removeLast();	// returns and removes the last element from the list
		
		if(propsList_second.size() == 1)
			minus_second.setEnabled(false);
		
		pw.getCompCombo().dispose();
		
		HashMap<Integer, PropertyUI> propertyUIs_temp = pw.getPropertyUIs_any();
		for(PropertyUI pui : propertyUIs_temp.values())
			pui.dispose();
		propertyUIs_temp.clear();
							
		pw.getPropsComposite().dispose();		
		
		expandComposite_second.pack();
		rootScrollComposite.setMinSize( mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT) );
	}
}
