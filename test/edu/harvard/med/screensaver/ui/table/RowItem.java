// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.net.URL;
import java.util.Arrays;

import edu.harvard.med.screensaver.util.StringUtils;

public class RowItem
{
  private Integer _id;
  private String _name;
  private Status _status;
  private Double _value;
  private URL _url;

  public RowItem(Integer id, String name, Status status, Double value, URL url)
  {
    _id = id;
    _name = name;
    _status = status;
    _value = value;
    _url = url;
  }

  public Integer getId()
  {
    return _id;
  }

  public void setId(Integer id)
  {
    _id = id;
  }

  public String getName()
  {
    return _name;
  }

  public void setName(String name)
  {
    _name = name;
  }

  public Double getValue()
  {
    return _value;
  }

  public void setValue(Double number)
  {
    _value = number;
  }

  public Status getStatus()
  {
    return _status;
  }

  public void setStatus(Status status)
  {
    _status = status;
  }

  public URL getUrl()
  {
    return _url;
  }

  public void setUrl(URL url)
  {
    _url = url;
  }
  
  @Override
  public String toString()
  {
    return StringUtils.makeListString(Arrays.asList(_id, _name, _value, _status, _url), ", ");
  }
}
