// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.PrintWriter;
import java.io.Writer;

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Well;

import org.apache.log4j.Logger;

public class WellSdfWriter extends PrintWriter 
{
  // static members

  private static Logger log = Logger.getLogger(WellSdfWriter.class);


  // instance data members

  
  // public constructors and methods

  public WellSdfWriter(Writer writer)
  {
    super(writer);
  }

  /**
   * Write the well contents out as an SD file record to the print writer.
   * @param pw the SD file print writer
   */
  public void write(Well well)
  {
    if (well.getMolfile() == null) {
      return;
    }
    println(well.getMolfile());
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
    println(well.getWellType().getValue());
    println();
    println(">  <Smiles>");
    println(well.getSmiles());
    println();
    if (well.getIccbNumber() != null) {
      println(">  <ICCB_Number>");
      println(well.getIccbNumber());
      println();
    }
    if (well.getReagent() != null) {
      println(">  <Vendor_Identifier>");
      println(well.getReagent().getReagentId().getVendorIdentifier());
      println();
    }
    Compound compound = well.getPrimaryCompound();
    if (compound != null) {
      for (String compoundName : compound.getCompoundNames()) {
        println(">  <Compound_Name>");
        println(compoundName);
        println();
      }
      for (String casNumber : compound.getCasNumbers()) {
        println(">  <CAS_Number>");
        println(casNumber);
        println();
      }
      for (String nscNumber : compound.getNscNumbers()) {
        println(">  <NSC_Number>");
        println(nscNumber);
        println();
      }
      for (String pubchemCid : compound.getPubchemCids()) {
        println(">  <PubChem_CID>");
        println(pubchemCid);
        println();
      }
      for (String chembankId : compound.getChembankIds()) {
        println(">  <ChemBank_ID>");
        println(chembankId);
        println();
      }
    }
    println("$$$$");
  }

  
  // private methods

}
