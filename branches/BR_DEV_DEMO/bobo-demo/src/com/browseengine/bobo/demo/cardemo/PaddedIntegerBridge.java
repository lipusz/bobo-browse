package com.browseengine.bobo.demo.cardemo;

import java.text.NumberFormat;
import java.util.Map;

import org.hibernate.search.bridge.ParameterizedBridge;
import org.hibernate.search.bridge.StringBridge;

public class PaddedIntegerBridge implements StringBridge,ParameterizedBridge {
	private final NumberFormat _formatter;
	
	public PaddedIntegerBridge(int pad){
		_formatter = NumberFormat.getInstance();
		_formatter.setMaximumIntegerDigits(pad);
		_formatter.setMinimumIntegerDigits(pad);
	}
	
	public String objectToString(Object param) {
		return _formatter.format((Integer)param);
	}
	
	public void setParameterValues(Map params) {
		int padding = (Integer)params.get( "padding" ); 
		_formatter.setMaximumIntegerDigits(padding);
		_formatter.setMinimumIntegerDigits(padding);
	}
}
