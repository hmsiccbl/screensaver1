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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;


/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Embeddable
public class ReagentVendorIdentifier implements Serializable, Comparable<ReagentVendorIdentifier>
{
  private static final long serialVersionUID = 1L;


  // instance data members

  private String _id;

  private transient String _vendorName;
  private transient String _vendorIdentifier;
  private transient String _asString;


  // public constructors and methods

  public ReagentVendorIdentifier(String id)
  {
    setReagentId(id);
  }

  public ReagentVendorIdentifier(String vendorName, String reagentIdentifier)
  {
    // convert nulls to empty strings, for safety and db constraints
    _vendorName = vendorName == null ? "" : vendorName;
    _vendorIdentifier = reagentIdentifier == null ? "" : reagentIdentifier;
  }

  @Column
  @org.hibernate.annotations.Type(type="text")
  public String getReagentId()
  {
    if (_id == null) {
      _id = _vendorName + ":" + _vendorIdentifier;
    }
    return _id;
  }

  public void setReagentId(String id)
  {
    String[] parts = id.split(":");
    _vendorName = parts[0];
    _vendorIdentifier = parts[1];
  }

  /**
   * @return vendor the library vendor (from {@link Library#getVendor}).
   */
  @Transient
  public String getVendorName()
  {
    return _vendorName;
  }

  /**
   * @return vendorId the vendor ID
   */
  @Transient
  public String getVendorIdentifier()
  {
    return _vendorIdentifier;
  }

  @Override
  public boolean equals(Object other)
  {
    return ((ReagentVendorIdentifier) other).getVendorName().equals(_vendorName) &&
    ((ReagentVendorIdentifier) other).getVendorIdentifier().equals(_vendorIdentifier);
  }

  @Override
  public int hashCode()
  {
    return getReagentId().hashCode();
  }

  @Override
  public String toString()
  {
    if (_asString == null) {
      _asString = _vendorName + " " + _vendorIdentifier;
    }
    return _asString;
  }

  private void setVendorName(String vendorName)
  {
    _vendorName = vendorName;
  }

  private void setVendorIdentifier(String vendorIdentifier)
  {
    _vendorIdentifier = vendorIdentifier;
  }

  /**
   * @motivation for Hibernate
   */
  private ReagentVendorIdentifier()
  {
  }

  public int compareTo(ReagentVendorIdentifier other)
  {
    int result = _vendorName.compareTo(other._vendorName);
    if (result == 0) {
      result = _vendorIdentifier.compareTo(other._vendorIdentifier);
    }
    return result;
  }
}
