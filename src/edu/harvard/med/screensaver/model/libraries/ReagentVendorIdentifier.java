// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.io.Serializable;

import javax.persistence.Embeddable;

import edu.harvard.med.screensaver.model.RequiredPropertyException;

import org.hibernate.annotations.Type;


/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Embeddable
public class ReagentVendorIdentifier implements Serializable, Comparable<ReagentVendorIdentifier>
{
  private static final long serialVersionUID = 1L;

  static final ReagentVendorIdentifier NULL_VENDOR_ID = new ReagentVendorIdentifier();
  static { NULL_VENDOR_ID._asString = ""; }

  private String _vendorName;
  private String _vendorIdentifier;
  private transient String _asString;

  public ReagentVendorIdentifier(String vendorName, String reagentIdentifier)
  {
    if (vendorName == null) {
      throw new RequiredPropertyException(Reagent.class, "reagent vendor name");
    }
    if (reagentIdentifier == null) {
      throw new RequiredPropertyException(Reagent.class, "reagent vendor identifier");
    }
    setVendorName(vendorName);
    setVendorIdentifier(reagentIdentifier);
  }

  /**
   * @return vendor the library vendor (from {@link Library#getVendor}).
   */
  @Type(type="text")
  public String getVendorName()
  {
    return _vendorName;
  }

  /**
   * @return vendorId the vendor ID
   */
  @Type(type="text")
  public String getVendorIdentifier()
  {
    return _vendorIdentifier;
  }

  @Override
  public boolean equals(Object o)
  {
    if (o == null) { 
      return false;
    }
    ReagentVendorIdentifier other = (ReagentVendorIdentifier) o;
    if (_vendorName == null) {
      return other.getVendorName() == null;
    }
    assert _vendorIdentifier != null;
    return _vendorName.equals(other.getVendorName()) && _vendorIdentifier.equals(other.getVendorIdentifier());
  }

  @Override
  public int hashCode()
  {
    return toString().hashCode();
  }

  @Override
  public String toString()
  {
    if (_asString == null) {
      _asString = _vendorName + " " + _vendorIdentifier;
    }
    return _asString;
  }

  public int compareTo(ReagentVendorIdentifier other)
  {
    int result = _vendorName.compareTo(other._vendorName);
    if (result == 0) {
      result = _vendorIdentifier.compareTo(other._vendorIdentifier);
    }
    return result;
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
   * @motivation for Hibernate and NULL_VENDOR_ID
   */
  private ReagentVendorIdentifier()
  {
  }
}
