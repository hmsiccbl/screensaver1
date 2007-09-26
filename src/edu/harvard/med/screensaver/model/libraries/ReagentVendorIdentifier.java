// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import org.apache.log4j.Logger;

/**
 * Not yet part of the data model, but needed by UI code. (We really need a
 * Reagent entity type!)
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ReagentVendorIdentifier implements Comparable<ReagentVendorIdentifier>
{
  // instance data members

  private String _vendorName;
  private String _vendorIdentifier;

  transient private String _asString;
  transient private String _id;


  // public constructors and methods

  public ReagentVendorIdentifier(String vendorName, String reagentIdentifier)
  {
    // convert nulls to empty strings, for safety
    _vendorName = vendorName == null ? "" : vendorName;
    _vendorIdentifier = reagentIdentifier == null ? "" : reagentIdentifier;
  }

  /**
   * @return vendor the library vendor (from {@link Library#getVendor}).
   * @hibernate.property
   */
  public String getVendorName()
  {
    return _vendorName;
  }

  /**
   * @return vendorId the vendor ID (from {@link Well#getVendorIdentifier()})
   * @hibernate.property
   */
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
    return _vendorName.hashCode() * 7 + _vendorIdentifier.hashCode() * 17;
  }

  @Override
  public String toString()
  {
    if (_asString == null) {
      _asString = _vendorName + " " + _vendorIdentifier;
    }
    return _asString;
  }

  public String getId()
  {
    if (_id == null) {
      _id = _vendorName + ":" + _vendorIdentifier;
    }
    return _id;
  }

  private void setVendorName(String vendorName)
  {
    _vendorName = vendorName;
  }

  private void setVendorIdentifier(String reagentIdentifier)
  {
    _vendorIdentifier = reagentIdentifier;
  }

  /**
   * @motivation for Hibernate
   */
  private ReagentVendorIdentifier()
  {
  }

  public int compareTo(ReagentVendorIdentifier other)
  {
    int result = other._vendorName.compareTo(_vendorName);
    if (result == 0) {
      result = other._vendorIdentifier.compareTo(_vendorIdentifier);
    }
    return result;
  }
}
