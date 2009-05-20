/**
 * Bobo Browse Engine - High performance faceted/parametric search implementation 
 * that handles various types of semi-structured data.  Written in Java.
 * 
 * Copyright (C) 2005-2006  John Wang
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * To contact the project administrators for the bobo-browse project, 
 * please go to https://sourceforge.net/projects/bobo-browse/, or 
 * send mail to owner@browseengine.com.
 */

package com.browseengine.bobo.bench;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;

import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.common.Test;
import net.grinder.plugininterface.GrinderPlugin;
import net.grinder.plugininterface.PluginException;
import net.grinder.plugininterface.PluginProcessContext;
import net.grinder.plugininterface.PluginThreadContext;
import net.grinder.plugininterface.ThreadCallbacks;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import com.browseengine.bobo.server.qlog.QueryLog;
import com.browseengine.bobo.server.qlog.QueryLog.LogLine;

public class BoboPlugin implements GrinderPlugin
{
  private Set      _tests;
  private String   _path;
  private String   _host;
  private int      _port;
  private int      _timeout;
  private File     _queryLog;
  private String[] _requests;

  public BoboPlugin()
  {
    super();
  }

  public void initialize(PluginProcessContext processContext, Set testsFromPropertiesFile) throws PluginException
  {

    _tests = testsFromPropertiesFile;
    final GrinderProperties parameters = processContext.getPluginParameters();
    try
    {
      _path = parameters.getMandatoryProperty("path");
      _host = parameters.getMandatoryProperty("host");
      _port = parameters.getInt("port", 80);
      _timeout = parameters.getInt("timeout", 0);
      _queryLog = new File(parameters.getMandatoryProperty("querylog"));
      if (!_queryLog.exists())
      {
        throw new PluginException("invalid query log: " + _queryLog);
      }
      _requests = loadLogfile(_queryLog);
    }
    catch (GrinderException ge)
    {
      throw new PluginException("Missing property", ge);
    }
    catch (IOException ioe)
    {
      throw new PluginException(ioe.getMessage(), ioe);
    }
  }

  public static String[] loadLogfile(File qlog) throws IOException
  {
    FileInputStream fin = null;
    ArrayList<String> list = new ArrayList<String>();
    try
    {
      fin = new FileInputStream(qlog);
      BufferedReader reader = new BufferedReader(new InputStreamReader(fin, "UTF-8"));
      String q;
      while (true)
      {
        q = reader.readLine();
        if (q == null)
        {
          break;
        }
        else
        {
          list.add(q);
        }
      }

      return list.toArray(new String[list.size()]);
    }
    finally
    {
      if (fin != null)
      {
        fin.close();
      }
    }
  }

  public ThreadCallbacks createThreadCallbackHandler() throws PluginException
  {
    try
    {
      return new BoboThreadCallbacks(_host, _port, _path, _requests, _timeout);
    }
    catch (GrinderException ge)
    {
      throw new PluginException(ge.getMessage(), ge);
    }
  }

  public Set getTests() throws PluginException
  {
    return _tests;
  }

  private static class BoboThreadCallbacks implements ThreadCallbacks
  {

    private String              _host;
    private String              _path;
    private int                 _port;
    private PluginThreadContext _threadContext;
    private HttpClient          _httpClient;
    private String[]            _lines;
    private int                 _numIndex;

    BoboThreadCallbacks(String host, int port, String path, String[] requests, int timeout) throws GrinderException
    {
      _numIndex = 0;
      _host = host;
      _path = path;
      _port = port;
      _lines = requests;

      MultiThreadedHttpConnectionManager connmgr =
          new MultiThreadedHttpConnectionManager();
      connmgr = new MultiThreadedHttpConnectionManager();
      connmgr.getParams().setDefaultMaxConnectionsPerHost(100);
      connmgr.getParams().setMaxTotalConnections(100);
      connmgr.getParams().setStaleCheckingEnabled(true);

      if (timeout > 0)
      {
        connmgr.getParams().setSoTimeout(timeout);
      }

      _httpClient = new HttpClient(connmgr);
      HostConfiguration hostConfig = new HostConfiguration();
      hostConfig.setHost(_host, _port);

      _httpClient.setHostConfiguration(hostConfig);

    }

    public void beginCycle() throws PluginException
    {

    }

    private String getLine()
    {
      String q;
      synchronized (this)
      {
        q = _lines[_numIndex];
        _numIndex = (_numIndex + 1) % _lines.length;
      }
      return q;
    }

    public boolean doTest(Test testDefinition) throws PluginException
    {

      String reqData = getLine();
      LogLine logLine = QueryLog.readLog(reqData);

      boolean success = false;

      if (reqData != null)
      {
        StringBuffer tmpBuffer = new StringBuffer(_path);
        if (!_path.endsWith("/"))
        {
          tmpBuffer.append('/');
        }
        tmpBuffer.append(logLine.getMethod());
        PostMethod post = new PostMethod(tmpBuffer.toString());
        // RequestEntity req=new StringRequestEntity(reqData,"text/xml","UTF-8");
        // post.setRequestEntity(req);
        try
        {
          post.addParameter("reqstring", logLine.getRequest());
          post.addParameter("proto", logLine.getProtocol());
          // Execute request
          _httpClient.executeMethod(post);
          int status = post.getStatusCode();

          if (200 == status)
          {
            success = true;
          }

          return success;
        }
        catch (HttpException e)
        {
          e.printStackTrace();
          throw new PluginException(e.getMessage(), e);
        }
        catch (IOException e)
        {
          e.printStackTrace();
          throw new PluginException(e.getMessage(), e);
        }
        finally
        {
          // Release current connection to the connection pool once you are done
          post.releaseConnection();
        }
      }
      else
      {
        success = true;

      }

      return success;
    }

    public void endCycle() throws PluginException
    {

    }

    public void initialize(PluginThreadContext pluginThreadContext) throws PluginException
    {
      _threadContext = pluginThreadContext;
    }

  }
}
