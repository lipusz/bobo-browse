package com.browseengine.bobo.service.dataprovider;

import java.io.Serializable;
import java.util.HashMap;

public class PropertiesData implements Serializable
{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  private boolean _skip;
  private final HashMap<String,String> _data;
  private final int _id;

  public PropertiesData(HashMap<String,String> data,int id)
  {
    _data = data;
    _id = id;
    _skip = false;
  }
  
  public int getID()
  {
    return _id;
  }
  
  public HashMap<String,String> getData()
  {
    return _data;
  }
  
  public void setSkip(boolean skip)
  {
    _skip = skip;
  }
  
  public boolean isSkip()
  {
    return _skip;
  }
}
