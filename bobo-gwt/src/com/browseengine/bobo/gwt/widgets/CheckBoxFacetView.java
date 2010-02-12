package com.browseengine.bobo.gwt.widgets;

import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CheckBoxFacetView extends AbstractFacetView implements ClickHandler {
    private final VerticalPanel _panel;
    private final CheckBox _clearSel;
    private final Label _headingLabel;
    private volatile int _counter;
    
	public CheckBoxFacetView(String name) {
		super(name);
		_counter = 0;
		VerticalPanel topPane = new VerticalPanel();
		_panel = new VerticalPanel();
		_headingLabel = new Label();
		_headingLabel.setText(name);
		topPane.add(_headingLabel);
		topPane.add(_panel);
		
		_clearSel = new CheckBox();
		_clearSel.setName(_name);
		_clearSel.setText("All");
		_clearSel.addClickHandler(this);
		_clearSel.setValue(true);
		initWidget(topPane);
	}
	
	
	@Override
	public void setStyleName(String style) {
		super.setStyleName(style);
		_headingLabel.setStyleName(getBoboStyleName(FACET_HEADING_STYLE));
		_clearSel.setStyleName(getBoboStyleName(FACET_ALL_STYLE));
	}

	@Override
	public void updateSelections(List<FacetValue> selections,Set<String> selected){
		_panel.clear();
		_panel.add(_clearSel);
		_counter = 0;
		if (selections!=null){
			for (FacetValue facet : selections){
				CheckBox sel = new CheckBox();
				sel.setStyleName(getBoboStyleName(FACET_VALUE_STYLE));
				sel.setName(_name);
				String val = facet.getValue();
				if (selected!=null && selected.contains(val)){
					_counter++;
				}
				sel.setFormValue(val);
				sel.setText(String.valueOf(facet));
				sel.addClickHandler(this);
				_panel.add(sel);
			}
		}

		_clearSel.setValue(_counter == 0);
	}

	public void onClick(ClickEvent event) {
		Object src = event.getSource();
		if (src==_clearSel){
		  int wcount = _panel.getWidgetCount();
		  for (int i=0;i<wcount;++i){
			Widget w = _panel.getWidget(i);
			if (w!=_clearSel && w instanceof CheckBox){
				((CheckBox)w).setValue(false);
			}
		  }
		  fireFacetSelectionClearedEvent();
		}
		else{
		  if (src instanceof CheckBox){
			CheckBox sel = (CheckBox)src;
			String facetVal = sel.getFormValue();
			boolean checked = sel.getValue();
			if (checked){
				_counter++;
			}
			else{
				_counter--;
			}
			_clearSel.setValue(_counter == 0);
            fireFacetSelectionEvent(facetVal, checked);
		  }
		}
	}
}