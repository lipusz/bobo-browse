package com.browseengine.bobo.api;

import java.util.Set;

import com.browseengine.bobo.facets.FacetHandler;

public interface FacetHandlerHome {
  FacetHandler getFacetHandler(String name);
  Set<String> getFacetNames();
}
