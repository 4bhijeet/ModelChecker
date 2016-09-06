package util;
import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


public class ComboText extends Composite {
	Combo combo;
	Text otherText;
	
	private int otherIndex = -1;
	
	private final static String OTHER = "-other-";
	
	public ComboText(Composite parent, int style) {
		super(parent, SWT.NONE);
		
		combo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		/*combo.add(defaultText, 0);
		combo.select(0);*/
		
		combo.add("*", 0);		// make "*" first entry
		combo.select(0);
		
		otherText = new Text(this, SWT.BORDER);
		otherText.setEnabled(false);
		
		RowLayout valLayout = new RowLayout(SWT.VERTICAL);
		valLayout.fill = true;
		
		setLayout(valLayout);
		
		combo.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				checkAndEnableOrDisableOther();
			}
		});
	}
	
	private void addOther() {
		otherIndex = combo.getItemCount();
		combo.add(OTHER, otherIndex);
	}
	
	private void checkAndEnableOrDisableOther() {
		int selectedIndex = combo.getSelectionIndex();
		
		if(selectedIndex == otherIndex) {
			if(otherText.getEnabled() == false)
				otherText.setEnabled(true);
		}
		else {
			if(otherText.getEnabled() == true)
				otherText.setEnabled(false);
		}
	}
	
	public boolean isOtherEnabled() {
		return otherText.getEnabled();
	}
	
	public void addAll(Collection<String> items) {
		items.forEach( item -> combo.add(item) );	//<--- Java 8 lambda expression
		/*for(String item : items)
			combo.add(item);*/
		
		addOther();		
	}
	
	public String getText() {
		if(otherText.getEnabled() == false)	//if OTHER is not selected
			return combo.getText();
		else								//if OTHER is selected
			return otherText.getText();			
	}
	
	public void setFocusOnOther() {
		if(isOtherEnabled())
			otherText.setFocus();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		combo.setEnabled(enabled);
		
		if(enabled == true)
			checkAndEnableOrDisableOther();
		
		else {
			if(otherText.getEnabled() == true)
				otherText.setEnabled(false);
		}
			
	}
}
