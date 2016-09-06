package util;

import java.util.HashMap;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class PropertyWrapper {
	boolean firstEntry = false;
	
	Composite propsComposite;
	Combo compCombo;
	HashMap<Integer, PropertyUI> propertyUIs_any;	// PropertyUI map for any property group
		
	public PropertyWrapper(Composite propsComposite, Combo compCombo, HashMap<Integer, PropertyUI> propertyUIs_any) {
		this.propsComposite = propsComposite;
		this.compCombo = compCombo;
		this.propertyUIs_any = propertyUIs_any;
	}
	
	public PropertyWrapper(Composite propsComposite, HashMap<Integer, PropertyUI> propertyUIs_any) {
		this.propsComposite = propsComposite;
		
		this.firstEntry = true;
		
		this.propertyUIs_any = propertyUIs_any;
	}

	public boolean isFirstEntry() {
		return firstEntry;
	}

	public Composite getPropsComposite() {
		return propsComposite;
	}

	public Combo getCompCombo() {
		return compCombo;
	}

	public HashMap<Integer, PropertyUI> getPropertyUIs_any() {
		return propertyUIs_any;
	}
}
