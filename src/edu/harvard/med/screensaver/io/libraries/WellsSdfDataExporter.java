// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;

public class WellsSdfDataExporter implements DataExporter<Well,String>
{
  // static members

  private static Logger log = Logger.getLogger(WellsSdfDataExporter.class);
  private static final String LIST_DELIMITER = "; ";


  // instance data members

  private GenericEntityDAO _dao;


  // public constructors and methods

  public WellsSdfDataExporter(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  public InputStream export(final EntityDataFetcher<Well,String> dataFetcher)
  {
    // TODO: logUserActivity("downloadWellSearchResults");
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    _dao.doInTransaction(new DAOTransaction()
    {
      @SuppressWarnings("unchecked")
      public void runTransaction()
      {
        ArrayList<RelationshipPath<Well>> relationships = new ArrayList<RelationshipPath<Well>>();
        relationships.add(new RelationshipPath<Well>(Well.class, "compounds.compoundNames"));
        relationships.add(new RelationshipPath<Well>(Well.class, "compounds.casNumbers"));
        relationships.add(new RelationshipPath<Well>(Well.class, "compounds.nscNumbers"));
        relationships.add(new RelationshipPath<Well>(Well.class, "compounds.pubchemCids"));
        relationships.add(new RelationshipPath<Well>(Well.class, "compounds.chembankIds"));
        relationships.add(new RelationshipPath<Well>(Well.class, "silencingReagents.gene.genbankAccessionNumbers"));
        dataFetcher.setRelationshipsToFetch(relationships);
        PrintWriter searchResultsPrintWriter = new PrintWriter(out);
        writeSDFileSearchResults(searchResultsPrintWriter, dataFetcher);
        searchResultsPrintWriter.close();
      }
    });
    return new ByteArrayInputStream(out.toByteArray());
  }

  public String getFileName()
  {
    return "wellSearchResults.sdf";
  }

  public String getFormatName()
  {
    return "SD File";
  }

  public String getMimeType()
  {
    return "chemical/x-mdl-sdfile";
  }


  // private methods

  public void writeSDFileSearchResults(PrintWriter searchResultsPrintWriter,
                                       EntityDataFetcher<Well,String> dataFetcher)
  {
    Set<String> keys = new HashSet<String>(dataFetcher.findAllKeys());
    Map<String,Well> entities = dataFetcher.fetchData(keys);
    for (String key : keys) {
      Well well = entities.get(key);
      if (well.getLibrary().getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
        writeToSDFile(searchResultsPrintWriter, well);
      }
    }
  }

  /**
   * Write the well contents out as an SD file record to the print writer.
   * @param pw the SD file print writer
   */
  public void writeToSDFile(PrintWriter pw, Well well)
  {
    if (well.getMolfile() == null) {
      return;
    }
    pw.println(well.getMolfile());
    pw.println(">  <Library>");
    pw.println(well.getLibrary().getLibraryName());
    pw.println();
    pw.println(">  <Plate>");
    pw.println(well.getPlateNumber().intValue());
    pw.println();
    pw.println(">  <Well>");
    pw.println(well.getWellName());
    pw.println();
    pw.println(">  <Plate_Well>");
    pw.println(well.getPlateNumber() + well.getWellName());
    pw.println();
    pw.println(">  <Well_Type>");
    pw.println(well.getWellType().getValue());
    pw.println();
    pw.println(">  <Smiles>");
    pw.println(well.getSmiles());
    pw.println();
    if (well.getIccbNumber() != null) {
      pw.println(">  <ICCB_Number>");
      pw.println(well.getIccbNumber());
      pw.println();
    }
    if (well.getReagent() != null) {
      pw.println(">  <Vendor_Identifier>");
      pw.println(well.getReagent().getReagentId().getVendorIdentifier());
      pw.println();
    }
    Compound compound = well.getPrimaryCompound();
    if (compound != null) {
      for (String compoundName : compound.getCompoundNames()) {
        pw.println(">  <Compound_Name>");
        pw.println(compoundName);
        pw.println();
      }
      for (String casNumber : compound.getCasNumbers()) {
        pw.println(">  <CAS_Number>");
        pw.println(casNumber);
        pw.println();
      }
      for (String nscNumber : compound.getNscNumbers()) {
        pw.println(">  <NSC_Number>");
        pw.println(nscNumber);
        pw.println();
      }
      for (String pubchemCid : compound.getPubchemCids()) {
        pw.println(">  <PubChem_CID>");
        pw.println(pubchemCid);
        pw.println();
      }
      for (String chembankId : compound.getChembankIds()) {
        pw.println(">  <ChemBank_ID>");
        pw.println(chembankId);
        pw.println();
      }
    }
    pw.println("$$$$");
  }
}
