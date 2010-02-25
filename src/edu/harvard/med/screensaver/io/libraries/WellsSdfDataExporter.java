// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntitySetDataFetcher;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class WellsSdfDataExporter implements DataExporter<Collection<String>>
{
  private static final Logger log = Logger.getLogger(WellsSdfDataExporter.class);
  
  private static final int MAX_FETCH_SIZE = 1 << 8;
  
  private GenericEntityDAO _dao;
  
  private LibraryContentsVersion _libraryContentsVersion;

  /** for CGLIB2 */
  protected WellsSdfDataExporter() {}

  public WellsSdfDataExporter(GenericEntityDAO dao)
  {
    _dao = dao;
  }
  
  public void setLibraryContentsVersion(LibraryContentsVersion lcv)
  { 
    _libraryContentsVersion = lcv;
  }

  public WellsSdfDataExporter(GenericEntityDAO dao,
                              LibraryContentsVersion libraryContentsVersion)
  {
    _libraryContentsVersion = libraryContentsVersion;
  }

  @Transactional(readOnly=true, propagation=Propagation.NEVER) /* avoid accumulating entity objects in the Hibernate session, for scalability */
  public InputStream export(final Collection<String> wellKeys) throws IOException
  {
   WellSdfWriter writer = null;
   File outFile = null;
    try {
      outFile = File.createTempFile("wellsSdfDataExporter", "sdf");
      log.debug("creating temp file: " + outFile);
      outFile.deleteOnExit();
      FileWriter outWriter = new FileWriter(outFile);
      writer = new WellSdfWriter(new PrintWriter(outWriter));
      EntityDataFetcher<Well,String> dataFetcher = new EntitySetDataFetcher<Well,String>(Well.class, new HashSet<String>(wellKeys), _dao);
      ArrayList<RelationshipPath<Well>> relationships = new ArrayList<RelationshipPath<Well>>();
      relationships.add(Well.library);
      RelationshipPath<Well> toReagentPath;
      if (_libraryContentsVersion == null) {
        toReagentPath = Well.latestReleasedReagent;
      }
      else {
        toReagentPath = Well.reagents.restrict("libraryContentsVersion", _libraryContentsVersion);
      }
      relationships.add(toReagentPath.to(Reagent.libraryContentsVersion));
      relationships.add(toReagentPath.to(SmallMoleculeReagent.compoundNames));
      relationships.add(toReagentPath.to(SmallMoleculeReagent.pubchemCids));
      relationships.add(toReagentPath.to(SmallMoleculeReagent.chembankIds));
      relationships.add(toReagentPath.to(SilencingReagent.facilityGene).to(Gene.genbankAccessionNumbers));
      relationships.add(toReagentPath.to(SmallMoleculeReagent.molfileList));
      dataFetcher.setRelationshipsToFetch(relationships);
      writeSDFileSearchResults(writer, wellKeys, dataFetcher);
    }
    finally {
      IOUtils.closeQuietly(writer);
    }
    return new FileInputStream(outFile);
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

  private void writeSDFileSearchResults(WellSdfWriter writer,
                                        Collection<String> keys,
                                        EntityDataFetcher<Well,String> dataFetcher)
  {
    Iterable<List<String>> partitions = Iterables.partition(keys, MAX_FETCH_SIZE);
    for (Iterable<String> partition : partitions) {
      Map<String,Well> entities = dataFetcher.fetchData(Sets.newHashSet(partition));
      for (Well well : entities.values()) {
        if (well.getLibrary().getReagentType().equals(SmallMoleculeReagent.class)) {
          writer.write(well, _libraryContentsVersion);
        }
      }
    }
    // allow garbage collection
    _libraryContentsVersion = null;
  }
}
