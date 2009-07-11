package com.browseengine.bobo.app.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.directwebremoting.util.Logger;
import org.json.JSONObject;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.browseengine.bobo.api.BrowseException;
import com.browseengine.bobo.api.BrowseHit;
import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.api.FacetAccessible;
import com.browseengine.bobo.server.protocol.BoboHttpRequestParam;
import com.browseengine.bobo.server.protocol.BoboQueryBuilder;
import com.browseengine.bobo.server.protocol.BoboRequestBuilder;
import com.browseengine.bobo.service.BrowseService;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.render.JsonRenderer;

public class BoboAppServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(BoboAppServlet.class);
	
	private static final String QUERY_BUILDER_BEAN_NAME="bobo-query-builder";
	private static final String BROWSE_SERVICE_BEAN_NAME="browse-service";
	
	private BoboQueryBuilder _queryBuilder = null;
	private BrowseService _browseService = null;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		service(req,resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		service(req,resp);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BoboHttpRequestParam reqParam = new BoboHttpRequestParam(req);
		BrowseRequest boboReq = BoboRequestBuilder.buildRequest(reqParam, _queryBuilder);
		BrowseResult boboRes = null;
		Set<String> fieldsToFetch = boboReq.getFieldsToFetch();
		try{
			boboRes = _browseService.browse(boboReq);
			JSONObject retObj = new JSONObject();
			retObj.put("numhits", boboRes.getNumHits());
			retObj.put("totaldocs", boboRes.getTotalDocs());
			retObj.put("time", boboRes.getTime());
			BrowseHit[] hits = boboRes.getHits();

			DataTable hitsTable = BoboAppConverterUtil.convert(fieldsToFetch,hits);
			retObj.put("hits", JsonRenderer.renderDataTable(hitsTable,true,false));
			
			Map<String,FacetAccessible> facets = boboRes.getFacetMap();
			JSONObject facetObj = new JSONObject();
			Set<Entry<String,FacetAccessible>> entries = facets.entrySet();
			for (Entry<String,FacetAccessible> entry : entries){
				DataTable facetDataTable = BoboAppConverterUtil.convert(entry.getKey(), entry.getValue());
				facetObj.put(entry.getKey(), JsonRenderer.renderDataTable(facetDataTable, true, false));
			}
			retObj.put("facets", facetObj);
			
			String jsonString = retObj.toString();
			OutputStream ostream = resp.getOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(ostream,"UTF-8");
			writer.write(jsonString);
			writer.flush();
		}
		catch(BrowseException be){
			throw new ServletException(be.getMessage(),be);
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		WebApplicationContext springCtx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
		_browseService = (BrowseService)springCtx.getBean(BROWSE_SERVICE_BEAN_NAME);
		if (springCtx.containsBean(QUERY_BUILDER_BEAN_NAME)){
		  _queryBuilder = (BoboQueryBuilder)springCtx.getBean(QUERY_BUILDER_BEAN_NAME);
		}
	}

	@Override
	public void destroy() {
		if (_browseService!=null){
			try {
				_browseService.close();
			} catch (BrowseException e) {
				logger.error(e.getMessage(),e);
			}
		}
	}	
}
