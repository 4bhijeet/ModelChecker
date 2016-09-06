package util;

import graphSearch.Datatype;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class PropertyUI extends Composite {
	public static final String RANGE_K = "..";
	
	public static final String EQUALS = "=";
	public static final String NOT_EQUALS = "\u2260";
	public static final String LESS_THAN = "<";
	public static final String GREATER_THAN = ">";
	public static final String LESS_THAN_EQUALS = "\u2264";
	public static final String GREATER_THAN_EQUALS = "\u2265";
	
	Label varName;
	Combo relOp;
	
	ComboText ctValue;
	Combo cbValue;
	
	private Range range;	// to hold the range of values
	
	private Datatype type;
	private boolean hasComboText;
	
	public String validateErrorMsg;

	public PropertyUI(Composite parent, int style, Datatype datatype) {
		super(parent, SWT.BORDER);
		this.type = datatype;
		
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		setLayout(layout);
		
		varName = new Label(this, SWT.NONE);
		
		relOp = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		
		/*relOp.add(defaultText, 0);
		relOp.select(0);	*/		
		
		loadOperators();
	}
	
	private void createSimpleCombo() {
		cbValue = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		/*cbValue.add(defaultText, 0);
		cbValue.select(0);*/
		
		cbValue.add("*", 0);		// make "*" first entry
		cbValue.select(0);
		
		hasComboText = false;
	}
	
	private void createComboText() {
		ctValue = new ComboText(this, SWT.NONE);
		
		hasComboText = true;
	}
	
	private void loadOperators() {
		
		switch(type) {
		case STRING:
		case BOOLEAN:	relOp.add(EQUALS, 0);	// make "=" first entry
						relOp.add(NOT_EQUALS);
						createSimpleCombo();
						break;
						
		case INTEGER:
		case FLOAT:
		case DOUBLE: 	relOp.add(EQUALS, 0);	// make "=" first entry
						relOp.add(NOT_EQUALS);
						relOp.add(LESS_THAN);
						relOp.add(GREATER_THAN);						
						relOp.add(LESS_THAN_EQUALS);
						relOp.add(GREATER_THAN_EQUALS);
						createComboText();
		}
		
		relOp.select(0);
	}
	
	private boolean readRangeValues() {
		boolean valid = true;
		
		String value = ctValue.getText();
		
		String min_string = value.substring(0, value.indexOf(RANGE_K)).trim();
		String max_string = value.substring(value.indexOf(RANGE_K) + RANGE_K.length()).trim();
		
		double minimum = 0.0;
		double maximum = 0.0;
		
		try {
			minimum = Double.parseDouble(min_string);
		}
		catch(NumberFormatException e) {
			valid = false;
		}
		
		try {
			maximum = Double.parseDouble(max_string);
		}
		catch(NumberFormatException e) {
			valid = false;
		}		
		
		if(valid == true) {
			if(minimum > maximum)
				valid = false;
			else
				range = new Range(minimum, maximum);
		}
		
		return valid;
	}
	
	public Range getRangeValues() {		
		return range;
	}
	
	public boolean validate() {
		if(hasComboText == false)
			return true;
		else if(! ctValue.isOtherEnabled())
				return true;
		else {
			String value = ctValue.getText();
			
			boolean valid = true;
			
			if(value.contains(RANGE_K)) {
				valid = readRangeValues();
				if(valid == false) 
					validateErrorMsg = "Please give a valid range as in \"min..max\", for the field \'" + getVar() + "\'. Invalid RANGE.";
			}
			
			else {
				switch(type) {
				case INTEGER : 	try {
									Integer.parseInt(value);
								}
								catch(NumberFormatException e) {
									valid = false;
								}
								break;
							
				case DOUBLE	:	try {
									Double.parseDouble(value);
								}
								catch(NumberFormatException e) {
									valid = false;
								}
								break;
							
				case FLOAT	:	try {
									Float.parseFloat(value);
								}
								catch(NumberFormatException e) {
									valid = false;
								}
				}
				
				if(valid == false)
					validateErrorMsg = "Please give a valid value or a range for the field \'" + getVar() + "\'. " + type + " expected OR a RANGE";
			}
			
			return valid;
		}
		
	}
	
	public void setVarName(String name) {
		varName.setText(name);
	}
	
	public void addVarValues(Collection<String> items) {
		if(hasComboText)
			ctValue.addAll(items);
		else {
			items.forEach( item -> cbValue.add(item) );	//<--- Java 8 lambda expression
			/*for(String item : items)
				cbValue.add(item);*/
		}
	}
	
	public String getVar() {
		return varName.getText();
	}
	
	public String getRelOp() {
		return relOp.getText();
	}
	
	public String getValue() {
		if(hasComboText)
			return ctValue.getText();
		else
			return cbValue.getText();
	}
	
	public void setFocusOnOther() {
		if(hasComboText)
			ctValue.setFocusOnOther();				
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		for(Control child : this.getChildren())
			child.setEnabled(enabled);
	}
}
