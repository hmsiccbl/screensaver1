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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import edu.harvard.med.screensaver.model.DataModelViolationException;


/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Embeddable
public class ReagentVendorIdentifier implements Serializable, Comparable<ReagentVendorIdentifier>
{
  private static final long serialVersionUID = 1L;

  private static final String COMPOUND_KEY_DELIMITER = ":";


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
    setVendorName(vendorName);
    setVendorIdentifier(reagentIdentifier);
  }

  @Column
  @org.hibernate.annotations.Type(type="text")
  public String getReagentId()
  {
    if (_id == null) {
      _id = _vendorName + COMPOUND_KEY_DELIMITER + _vendorIdentifier;
    }
    return _id;
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

  public int compareTo(ReagentVendorIdentifier other)
  {
    int result = _vendorName.compareTo(other._vendorName);
    if (result == 0) {
      result = _vendorIdentifier.compareTo(other._vendorIdentifier);
    }
    return result;
  }


  // private methods

  /**
   * Set the value for this ReagentVendorIdentifier via a string that contains
   * both the vendor name and a reagent identifier (assigned by the vendor).
   *
   * @param id compound key value for reagent, in format "<vendor name>::<vendor
   *          reagent identifier>"
   */
  private void setReagentId(String id)
  {
    int delimPos = id.indexOf(COMPOUND_KEY_DELIMITER);
    if (delimPos < 0) {
      throw new DataModelViolationException("illegal reagent ID '" + id +
                                            "': expected format '<vendor name>" +
                                            COMPOUND_KEY_DELIMITER + "<vendor reagent identifier>'");
    }
    setVendorName(id.substring(0, delimPos));
    setVendorIdentifier(id.substring(delimPos + 1));
  }

  private void setVendorName(String vendorName)
  {
    if (vendorName == null) {
      // convert nulls to empty strings, for safety and db constraints
      vendorName = "";
    }
    if (vendorName.contains(COMPOUND_KEY_DELIMITER)) {
      throw new DataModelViolationException("vendorName '" + vendorName + "' may not contain '" + COMPOUND_KEY_DELIMITER + "'");
    }
    _vendorName = vendorName;
  }

  private void setVendorIdentifier(String vendorIdentifier)
  {
    if (vendorIdentifier == null || vendorIdentifier.length() == 0) {
      throw new DataModelViolationException("vendorIdentifier may not be null or empty");
    }
    _vendorIdentifier = vendorIdentifier;
  }

  /**
   * @motivation for Hibernate
   */
  private ReagentVendorIdentifier()
  {
  }
}
