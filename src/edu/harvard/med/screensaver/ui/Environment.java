// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.servlet.http.Cookie;

import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.control.UIControllerMethod;

import org.springframework.dao.ConcurrencyFailureException;

/**
 * Backing bean for developer-only view that displays environment information
 * for the running web application.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class Environment extends AbstractBackingBean
{
  private String _db;
  private String _user;
  private String _host;
  private String _url;

  public static class Row
  {
    private String _name;
    private String _value;
    public Row(String name, String value)
    {
      _name = name;
      _value = value;
    }
    public String getName()
    {
      return _name;
    }
    public void setName(String name)
    {
      _name = name;
    }
    public String getValue()
    {
      return _value;
    }
    public void setValue(String value)
    {
      _value = value;
    }
  }

  public DataModel getJvmModel()
  {
    List<Row> data = new ArrayList<Row>();
    data.add(new Row("Max Memory",
                     String.format("%.2fMB",
                                   Runtime.getRuntime().maxMemory() / (1024*1024.0))));
    data.add(new Row("Total Memory",
                     String.format("%.2fMB",
                                   Runtime.getRuntime().totalMemory() / (1024*1024.0))));
    data.add(new Row("Free Memory",
                     String.format("%.2fMB",
                                   Runtime.getRuntime().freeMemory() / (1024*1024.0))));
    return new ListDataModel(data);
  }

  public DataModel getRequestParamsModel()
  {
    List<Row> data = new ArrayList<Row>();
    Map reqParamMap = getExternalContext().getRequestParameterMap();
    for (Object paramName : reqParamMap.keySet()) {
      data.add(new Row(paramName.toString(),
                      reqParamMap.get(paramName).toString()));
    }
    String[] dbParamNames = {
      "SCREENSAVER_PGSQL_SERVER",
      "SCREENSAVER_PGSQL_DB",
      "SCREENSAVER_PGSQL_USER",
      "SCREENSAVER_PGSQL_PASSWORD",
    };
    for (int i = 0; i < dbParamNames.length; i++) {
      String paramName = dbParamNames[i];
      Object value= reqParamMap.get(paramName);
      data.add(new Row(paramName,
                       value == null ? "<null>" : value.toString()));
    }
    return new ListDataModel(data);
  }

  public DataModel getSessionParamsModel()
  {
    List<Row> data = new ArrayList<Row>();
    Map sessionParamMap = getExternalContext().getSessionMap();
    for (Object paramName : sessionParamMap.keySet()) {
      data.add(new Row(paramName.toString(),
                      sessionParamMap.get(paramName).toString()));
    }
    return new ListDataModel(data);
  }

  public DataModel getApplicationParamsModel()
  {
    List<Row> data = new ArrayList<Row>();
    Map sessionParamMap = getExternalContext().getApplicationMap();
    for (Object paramName : sessionParamMap.keySet()) {
      data.add(new Row(paramName.toString(),
                      sessionParamMap.get(paramName).toString()));
    }
    return new ListDataModel(data);
  }

  @SuppressWarnings("unchecked")
  public DataModel getCookiesTableModel()
  {
    List<Row> cookies = new ArrayList<Row>();
    Map<String,Cookie> cookieMap = (Map<String,Cookie>) getExternalContext().getRequestCookieMap();
    for (String cookieName : cookieMap.keySet()) {
      cookies.add(new Row(cookieName, cookieMap.get(cookieName).getValue()));
    }
    return new ListDataModel(cookies);
  }

  public DataModel getUserSecurityTableModel()
  {
    List<Row> userSecurityItems = new ArrayList<Row>();

    userSecurityItems.add(new Row("remoteUser", getExternalContext().getRemoteUser()));
    Principal principal = getExternalContext().getUserPrincipal();
    userSecurityItems.add(new Row("userPrincipal", principal == null ? "<none>" : principal.getName()));
    ScreensaverUser screensaverUser = getCurrentScreensaverUser().getScreensaverUser();
    userSecurityItems.add(new Row("currentScreensaverUser",
                                  screensaverUser == null ? "<none>" : screensaverUser.getFullNameFirstLast()));

    for (ScreensaverUserRole role : ScreensaverUserRole.values()) {
      userSecurityItems.add(new Row("in role '" + role.toString() + "'",
                                    Boolean.toString(isUserInRole(role))));
    }
    userSecurityItems.add(new Row("authenticationType", getExternalContext().getAuthType()));
    return new ListDataModel(userSecurityItems);
  }

  public DataModel getEnvTableModel()
  {
    List<Row> env = new ArrayList<Row>();
    Map<String,String> envMap = System.getenv();
    for (String envVar : envMap.keySet()) {
      env.add(new Row(envVar, envMap.get(envVar)));
    }
    return new ListDataModel(env);
  }

  public DataModel getSysPropsTableModel()
  {
    List<Row> propsData = new ArrayList<Row>();
    Properties properties = System.getProperties();
    for (Object propKey : properties.keySet()) {
      propsData.add(new Row(propKey.toString(),
                            properties.get(propKey).toString()));
    }
    return new ListDataModel(propsData);
  }

  public String getUrl()
  {
    return _url;
  }

  public void setUrl(String url)
  {
    _url = url;
  }

  public String getDb()
  {
    return _db;
  }

  public void setDb(String db)
  {
    _db = db;
  }

  public String getHost()
  {
    return _host;
  }

  public void setHost(String host)
  {
    _host = host;
  }

  public String getUser()
  {
    return _user;
  }

  public void setUser(String user)
  {
    _user = user;
  }

  /** for testing purposes only! */
  public String throwAnException() throws Exception
  {
    throw new Exception("You asked for it!");
  }

  @UIControllerMethod
  public String throwAConcurrencyFailureException() throws Exception
  {
    throw new ConcurrencyFailureException("You asked for it!");
  }

  public String runGC()
  {
    Runtime.getRuntime().gc();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

}
