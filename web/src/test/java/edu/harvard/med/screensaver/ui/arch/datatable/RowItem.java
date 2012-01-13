// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable;

import java.net.URL;
import java.util.Arrays;

import edu.harvard.med.screensaver.util.StringUtils;

@SuppressWarnings("serial")
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

