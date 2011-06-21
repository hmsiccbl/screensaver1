// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.PrintWriter;
import java.io.Writer;

import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;

// TODO: use common file specification with SDRecordParser
public class WellSdfWriter extends PrintWriter 
{
  public WellSdfWriter(Writer writer)
  {
    super(writer);
  }

  /**
   * Write the well contents out as an SD file record to the print writer.
   */
  public void write(Well well, LibraryContentsVersion lcv)
  {
    assert well.getLibrary().getReagentType().equals(SmallMoleculeReagent.class);
    SmallMoleculeReagent smallMoleculeReagent;
    if (lcv == null) {
      smallMoleculeReagent = (SmallMoleculeReagent) well.getLatestReleasedReagent();
    }
    else {
      smallMoleculeReagent = (SmallMoleculeReagent) well.getReagents().get(lcv);
    }
    if (smallMoleculeReagent == null || smallMoleculeReagent.isRestricted()) {
      smallMoleculeReagent = SmallMoleculeReagent.NullSmallMoleculeReagent;
    }
    else {
      smallMoleculeReagent = (SmallMoleculeReagent) smallMoleculeReagent.restrict();
    }
    if (smallMoleculeReagent.getMolfile() != null) {
      println(smallMoleculeReagent.getMolfile());
    }
    println(">  <Library>");
    println(well.getLibrary().getLibraryName());
    println();
    println(">  <Plate>");
    println(well.getPlateNumber().intValue());
    println();
    println(">  <Well>");
    println(well.getWellName());
    println();
    println(">  <Plate_Well>");
    println(well.getPlateNumber() + well.getWellName());
    println();
    println(">  <Well_Type>");
    println(well.getLibraryWellType().getValue());
    println();
    println(">  <Library_Contents_Version>");
    if (smallMoleculeReagent.getLibraryContentsVersion() != null) {
      println(smallMoleculeReagent.getLibraryContentsVersion().getVersionNumber());
    }
    println();
    println(">  <SMILES>");
    if (smallMoleculeReagent.getSmiles() != null) {
      println(smallMoleculeReagent.getSmiles());
    }
    println();
    println(">  <InChi>");
    if (smallMoleculeReagent.getInchi() != null) {
      println(smallMoleculeReagent.getInchi());
    }
    println();
    println(">  <Molecular_Formula>");
    if (smallMoleculeReagent.getMolecularFormula() != null) {
      println(smallMoleculeReagent.getMolecularFormula());
    }
    println();
    println(">  <Molecular_Mass>");
    if (smallMoleculeReagent.getMolecularMass() != null) {
      println(smallMoleculeReagent.getMolecularMass());
    }
    println();
    println(">  <Molecular_Weight>");
    if (smallMoleculeReagent.getMolecularWeight() != null) {
      println(smallMoleculeReagent.getMolecularWeight());
    }
    println();
    println(">  <Facility_Reagent_ID>");
    if (well.getFacilityId() != null) {
      println(well.getFacilityId());
    } 
    println();
    println(">  <Vendor>");
    if (smallMoleculeReagent.getVendorId().getVendorName() != null) {
      println(smallMoleculeReagent.getVendorId().getVendorName());
    }
    println();
    println(">  <Vendor_Reagent_ID>");
    if (smallMoleculeReagent.getVendorId().getVendorIdentifier() != null) {
      println(smallMoleculeReagent.getVendorId().getVendorIdentifier());
    }
    println();
    for (String compoundName : smallMoleculeReagent.getCompoundNames()) {
      println(">  <Chemical_Name>");
      println(compoundName);
      println();
    }
    for (Integer pubchemCid : smallMoleculeReagent.getPubchemCids()) {
      println(">  <PubChem_CID>");
      println(pubchemCid);
      println();
    }
    for (Integer chembankId : smallMoleculeReagent.getChembankIds()) {
      println(">  <ChemBank_ID>");
      println(chembankId);
      println();
    }
    println("$$$$");
  }
}
