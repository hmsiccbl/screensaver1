// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.smallmolecule;

import java.math.BigDecimal;
import java.util.List;

import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.MolecularFormula;
import edu.harvard.med.screensaver.model.libraries.WellName;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * Data-only class containing all the necessary information to be parsed from as
 * SDFile record.
 */
class SDRecord
{
  private Integer _plateNumber;
  private WellName _wellName;
  private String _facilityId;
  private LibraryWellType _libraryWellType;
  private String _molfile;
  private String _vendor;
  private String _vendorIdentifier;
  private String _smiles;
  private String _inChi;
  private BigDecimal _molecularMass;
  private BigDecimal _molecularWeight;
  private MolecularFormula _molecularFormula;
  private List<String> _compoundNames = Lists.newLinkedList();
  private List<Integer> _pubchemCids = Lists.newLinkedList();
  private List<Integer> _chembankIds = Lists.newLinkedList();
  
  public LibraryWellType getLibraryWellType() { return _libraryWellType; }
  public void setLibraryWellType(LibraryWellType libraryWellType) { _libraryWellType = libraryWellType; }
  public String getSmiles() { return _smiles; }
  public void setSmiles(String smiles) { _smiles = smiles; }
  public String getInChi() { return _inChi; }
  public void setInChi(String inChi) { _inChi = inChi; }
  String getFacilityId() { return _facilityId; }
  void setFacilityId(String facilityId) { _facilityId = facilityId; }
  String getMolfile() { return _molfile; }
  void setMolfile(String molfile) { _molfile = molfile; }
  public BigDecimal getMolecularMass() { return _molecularMass; }
  public void setMolecularMass(BigDecimal molecularMass) { _molecularMass = molecularMass; }
  public BigDecimal getMolecularWeight() { return _molecularWeight; }
  public void setMolecularWeight(BigDecimal molecularWeight) { _molecularWeight = molecularWeight; }
  public MolecularFormula getMolecularFormula() { return _molecularFormula; }
  public void setMolecularFormula(MolecularFormula molecularFormula) { _molecularFormula = molecularFormula; }
  Integer getPlateNumber() { return _plateNumber; }
  void setPlateNumber(Integer plateNumber) { _plateNumber = plateNumber; }
  String getVendor() { return _vendor; }
  void setVendor(String vendor) { _vendor = vendor; }
  String getVendorIdentifier() { return _vendorIdentifier; }
  void setVendorIdentifier(String vendorIdentifier) { _vendorIdentifier = vendorIdentifier; }
  WellName getWellName() { return _wellName; }
  void setWellName(WellName wellName) { _wellName = wellName; }
  public void setCompoundNames(List<String> compoundNames) { _compoundNames = compoundNames; }
  public void setPubchemCids(List<Integer> pubchemCids) { _pubchemCids = pubchemCids; }
  public void setChembankIds(List<Integer> chembankIds) { _chembankIds = chembankIds; }  
  public List<String> getCompoundNames() { return _compoundNames; }
  public List<Integer> getPubchemCids() { return _pubchemCids; }
  public List<Integer> getChembankIds() { return _chembankIds; }
  
  public String toString() {
    return
      "MOLFILE = " + _molfile          + "\n" +
      "MOLMASS = " + _molecularMass    + "\n" +
      "MOLFORM = " + _molecularFormula + "\n" +
      "PLATE   = " + _plateNumber      + "\n" +
      "WELL    = " + _wellName         + "\n" +
      "VEND ID = " + _vendorIdentifier + "\n" +
      "FACY_ID = " + _facilityId       + "\n" +
      "C NAMES  = " + Joiner.on(",").join(_compoundNames) + "\n" +
      "PC IDS  = " + Joiner.on(",").join(_pubchemCids)+ "\n" +
      "CB IDS  = " + Joiner.on(",").join(_chembankIds)+ "\n";
  }
}
