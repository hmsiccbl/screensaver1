// $HeadURL: svn+ssh://js163@orchestra/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

/**
 * A little capsule containing all the necessary information to be parsed from
 * as SDFile record.
 * 
 * @author s
 */
class SDRecordData
{

  // private instance fields
  
  private String _molfile;
  private Integer _plateNumber;
  private String _wellName;
  private String _vendorIdentifier;
  private String _iccbNumber;
  private String _compoundName;
  private String _casNumber;
  
  
  // package instance methods
  
  String getCompoundName() {
    return _compoundName;
  }
  void setCompoundName(String compoundName) {
    _compoundName = compoundName;
  }
  String getIccbNumber() {
    return _iccbNumber;
  }
  void setIccbNumber(String icbbNumber) {
    _iccbNumber = icbbNumber;
  }
  String getMolfile() {
    return _molfile;
  }
  void setMolfile(String molfile) {
    _molfile = molfile;
  }
  Integer getPlateNumber() {
    return _plateNumber;
  }
  void setPlateNumber(Integer plateNumber) {
    _plateNumber = plateNumber;
  }
  String getVendorIdentifier() {
    return _vendorIdentifier;
  }
  void setVendorIdentifier(String vendorIdentifier) {
    _vendorIdentifier = vendorIdentifier;
  }
  String getWellName() {
    return _wellName;
  }
  void setWellName(String wellName) {
    _wellName = wellName;
  }
  String getCasNumber() {
    return _casNumber;
  }
  void setCasNumber(String casNumber) {
    _casNumber = casNumber;
  }

  public String toString() {
    return
      "MOLFILE = " + _molfile          + "\n" +
      "PLATE   = " + _plateNumber      + "\n" +
      "WELL    = " + _wellName         + "\n" +
      "VEND ID = " + _vendorIdentifier + "\n" +
      "ICCB NO = " + _iccbNumber       + "\n" +
      "C NAME  = " + _compoundName     + "\n" +
      "CAS NO  = " + _casNumber        + "\n";
  }
}
