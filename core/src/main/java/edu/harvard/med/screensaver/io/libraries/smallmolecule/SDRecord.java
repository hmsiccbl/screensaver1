// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.smallmolecule;

import java.math.BigDecimal;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.MolecularFormula;
import edu.harvard.med.screensaver.model.libraries.WellName;

/**
 * Data-only class containing all the necessary information to be parsed from as
 * SDFile record.
 */
public class SDRecord
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
  private List<Integer> _chemblIds = Lists.newLinkedList();
  private MolarConcentration _molarConcentration;
  private BigDecimal _mgMlConcentration;
  private List<Integer> _pubmedIds = Lists.newLinkedList();

  // for LINCS
  private String _vendorBatchId;
  private Integer _facilityBatchId;
  private Integer _saltFormId;
  
  public LibraryWellType getLibraryWellType() { return _libraryWellType; }
  public void setLibraryWellType(LibraryWellType libraryWellType) { _libraryWellType = libraryWellType; }
  public String getSmiles() { return _smiles; }
  public void setSmiles(String smiles) { _smiles = smiles; }
  public String getInChi() { return _inChi; }
  public void setInChi(String inChi) { _inChi = inChi; }

  public String getFacilityId()
  {
    return _facilityId;
  }

  public void setFacilityId(String facilityId)
  {
    _facilityId = facilityId;
  }

  public String getMolfile()
  {
    return _molfile;
  }

  public void setMolfile(String molfile)
  {
    _molfile = molfile;
  }

  public MolarConcentration getMolarConcentration()
  {
    return _molarConcentration;
  }

  public void setMolarConcentration(MolarConcentration value)
  {
    _molarConcentration = value;
  }
    
  public void setMgMlConcentration(BigDecimal _mgMlConcentration)
  {
    this._mgMlConcentration = _mgMlConcentration;
  }
  
  public BigDecimal getMgMlConcentration()
  {
    return _mgMlConcentration;
  }
  
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
  public void setChemblIds(List<Integer> chembIds) { _chembankIds = chembIds; }  
  public List<String> getCompoundNames() { return _compoundNames; }
  public List<Integer> getPubchemCids() { return _pubchemCids; }
  public List<Integer> getChembankIds() { return _chembankIds; }
  public List<Integer> getChemblIds() { return _chemblIds; }
  public List<Integer> getPubmedIds() { return _pubmedIds;  }

  public void setVendorBatchId(String line)
  {
    _vendorBatchId = line;
  }

  public String getVendorBatchId()
  {
    return _vendorBatchId;
  }

  public void setFacilityBatchId(Integer value)
  {
    _facilityBatchId = value;
  }

  public Integer getFacilityBatchId()
  {
    return _facilityBatchId;
  }

  public void setSaltFormId(Integer value)
  {
    _saltFormId = value;
  }
  
  public Integer getSaltFormId()
  {
    return _saltFormId;
  }
  public String toString() {
    return
      "MOLFILE = " + _molfile          + "\n" +
      "MOLMASS = " + _molecularMass    + "\n" +
      "MOLFORM = " + _molecularFormula + "\n" +
      "MOLARCONCENTRATION = " + _molarConcentration + "\n" +
      "MGMLCONCENTRATION = " + _mgMlConcentration + "\n" +
      "PLATE   = " + _plateNumber      + "\n" +
      "WELL    = " + _wellName         + "\n" +
      "VEND ID = " + _vendorIdentifier + "\n" +
      "VEND BID = " + _vendorBatchId + "\n" +
      "FACY_ID = " + _facilityId       + "\n" +
      "FACY_BID = " + _facilityBatchId + "\n" +
      "SALT_ID = " + _saltFormId + "\n" +
      "C NAMES  = " + Joiner.on(",").join(_compoundNames) + "\n" +
      "PC IDS  = " + Joiner.on(",").join(_pubchemCids)+ "\n" +
      "CB IDS  = " + Joiner.on(",").join(_chembankIds)+ "\n" +
      "Chembl IDS  = " + Joiner.on(",").join(_chemblIds) + "\n" +
      "PubMed IDS  = " + Joiner.on(",").join(_pubmedIds) + "\n";
  }
}
