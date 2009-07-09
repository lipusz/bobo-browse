package com.browseengine.bobo.app.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.directwebremoting.util.Logger;

import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.BrowseHit;
import com.browseengine.bobo.api.FacetAccessible;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.ValueType;

public class BoboAppConverterUtil {
	private static final Logger logger = Logger.getLogger(BoboAppConverterUtil.class);
	
	public static DataTable convert(String name,FacetAccessible facetAccessible){
		List<BrowseFacet> facets = facetAccessible.getFacets();
		DataTable dataTable = new DataTable();
		dataTable.addColumn(new ColumnDescription(name+"-value",ValueType.TEXT,"value"));
		dataTable.addColumn(new ColumnDescription(name+"-count",ValueType.NUMBER,"count"));
		for (BrowseFacet facet : facets){
			TableRow row = new TableRow();
			row.addCell(facet.getValue());
			row.addCell(facet.getHitCount());
			try {
				dataTable.addRow(row);
			} catch (TypeMismatchException e) {
				logger.error(e.getMessageToUser(),e);
			}
		}
		return dataTable;
	}
	
	public static DataTable convert(Set<String> fields,BrowseHit[] hits){
		
		DataTable dataTable = new DataTable();
		dataTable.addColumn(new ColumnDescription("result-docid",ValueType.NUMBER,"docid"));
		dataTable.addColumn(new ColumnDescription("result-score",ValueType.NUMBER,"score"));
		
		String[] fieldnames = fields!=null ? fields.toArray(new String[fields.size()]) : new String[0];
		for (String fieldName : fieldnames){
			dataTable.addColumn(new ColumnDescription("result-field-"+fieldName,ValueType.TEXT,fieldName));
		}
		
		for (BrowseHit hit : hits){
			int docid = hit.getDocid();
			float score = hit.getScore();
			Map<String,String[]> flds = hit.getFieldValues();
			
			TableRow row = new TableRow();
			row.addCell(docid);
			row.addCell(score);
			for (String fieldName : fieldnames){
				String[] fldVals = flds.get(fieldName);
				row.addCell(Arrays.toString(fldVals));
			}
			try {
				dataTable.addRow(row);
			} catch (TypeMismatchException e) {
				logger.error(e.getMessageToUser(),e);
			}
		}
		
		return dataTable;
	}
}
