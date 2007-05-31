// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.io.Serializable;

import edu.harvard.med.screensaver.model.AbstractEntity;

/**
 * @hibernate.class
 * 
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class Child extends AbstractEntity
{
  private static final long serialVersionUID = 1L;
  private Integer _id;
  private Integer _version;
  private String _value;
  
  public Child() {}

  public Child(String value)
  {
    _value = value;
  }
  
  /**
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="child_id_seq"
   */
  public Integer getId()
  {
    return _id;
  }
  public void setId(Integer id)
  {
    _id = id;
  }
  /**
   * @hibernate.property
   * @return
   */
  public String getValue()
  {
    return _value;
  }
  public void setValue(String value)
  {
    _value = value;
  }
  /**
   * @hibernate.version
   */
  public Integer getVersion()
  {
    return _version;
  }
  public void setVersion(Integer version)
  {
    _version = version;
  }
  @Override
  protected Object getBusinessKey()
  {
    return _value;
  }
  @Override
  public Serializable getEntityId()
  {
    return _id;
  }
}

