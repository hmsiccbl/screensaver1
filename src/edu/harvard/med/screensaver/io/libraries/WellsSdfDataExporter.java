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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntitySetDataFetcher;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;

public class WellsSdfDataExporter implements DataExporter<Collection<String>>
{
  // static members

  private static Logger log = Logger.getLogger(WellsSdfDataExporter.class);


  // instance data members

  private GenericEntityDAO _dao;


  // public constructors and methods

  public WellsSdfDataExporter(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  public InputStream export(final Collection<String> wellKeys)
  {
    // TODO: logUserActivity("downloadWellSearchResults");
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    _dao.doInTransaction(new DAOTransaction()
    {
      @SuppressWarnings("unchecked")
      public void runTransaction()
      {
        WellSdfWriter writer = new WellSdfWriter(new PrintWriter(out));
        EntityDataFetcher<Well,String> dataFetcher = new EntitySetDataFetcher<Well,String>(Well.class, new HashSet<String>(wellKeys), _dao);
        ArrayList<RelationshipPath<Well>> relationships = new ArrayList<RelationshipPath<Well>>();
        relationships.add(new RelationshipPath<Well>(Well.class, "compounds.compoundNames"));
        relationships.add(new RelationshipPath<Well>(Well.class, "compounds.casNumbers"));
        relationships.add(new RelationshipPath<Well>(Well.class, "compounds.nscNumbers"));
        relationships.add(new RelationshipPath<Well>(Well.class, "compounds.pubchemCids"));
        relationships.add(new RelationshipPath<Well>(Well.class, "compounds.chembankIds"));
        relationships.add(new RelationshipPath<Well>(Well.class, "silencingReagents.gene.genbankAccessionNumbers"));
        relationships.add(new RelationshipPath<Well>(Well.class, "molfileList"));
        dataFetcher.setRelationshipsToFetch(relationships);
        writeSDFileSearchResults(writer, wellKeys, dataFetcher);
        writer.close();
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

  private void writeSDFileSearchResults(WellSdfWriter writer,
                                        Collection<String> keys,
                                        EntityDataFetcher<Well,String> dataFetcher)
  {
    Map<String,Well> entities = dataFetcher.fetchData(new HashSet<String>(keys));
    for (String key : keys) {
      Well well = entities.get(key);
      if (well.getLibrary().getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
        writer.write(well);
      }
    }
  }
}
