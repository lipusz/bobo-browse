package com.browseengine.bobo.demo.cardemo;

import java.text.NumberFormat;
import java.util.Map;

import org.hibernate.search.bridge.ParameterizedBridge;
import org.hibernate.search.bridge.StringBridge;

public class PaddedFloatBridge implements StringBridge,ParameterizedBridge{
	private final NumberFormat _formatter;
	
	public PaddedFloatBridge(){
		_formatter = NumberFormat.getInstance();
		_formatter.setMinimumFractionDigits(2);
		_formatter.setMaximumFractionDigits(2);
	}
	
	public String objectToString(Object param) {
		return _formatter.format((Float)param);
	}

	public void setParameterValues(Map params) {
		int padding = (Integer)params.get( "padding" ); 
		_formatter.setMaximumIntegerDigits(padding);
		_formatter.setMinimumIntegerDigits(padding);
	}
}
