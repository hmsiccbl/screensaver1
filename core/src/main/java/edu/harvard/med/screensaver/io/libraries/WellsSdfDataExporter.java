// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.LibraryContentsVersionReference;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;

public class WellsSdfDataExporter implements DataExporter<String>
{
  private static final Logger log = Logger.getLogger(WellsSdfDataExporter.class);
  
  private static final int MAX_FETCH_SIZE = 1 << 8;
  
  private GenericEntityDAO _dao;
  
  private LibraryContentsVersionReference _libraryContentsVersionRef;

  /** for CGLIB2 */
  protected WellsSdfDataExporter() {}

  public WellsSdfDataExporter(GenericEntityDAO dao,
                              LibraryContentsVersionReference libraryContentsVersionRef)
  {
    _dao = dao;
    _libraryContentsVersionRef = libraryContentsVersionRef;
  }

  @Transactional(readOnly=true, propagation=Propagation.NEVER) /* avoid accumulating entity objects in the Hibernate session, for scalability */
  public InputStream export(final Iterator<String> wellKeyStrs) throws IOException
  {
   WellSdfWriter writer = null;
   File outFile = null;
    try {
      outFile = File.createTempFile("wellsSdfDataExporter", "sdf");
      log.debug("creating temp file: " + outFile);
      outFile.deleteOnExit();
      FileWriter outWriter = new FileWriter(outFile);
      writer = new WellSdfWriter(new PrintWriter(outWriter));
      EntityDataFetcher<Well,String> dataFetcher = new EntityDataFetcher<Well,String>(Well.class, _dao);
      List<PropertyPath<Well>> relationships = Lists.newArrayList();
      relationships.add(Well.library.toFullEntity());
      relationships.add(Well.reagents.to(Reagent.publications).toFullEntity());
      RelationshipPath<Well> toReagentPath;
      if (getLibraryContentsVersion() == null) {
        toReagentPath = Well.latestReleasedReagent;
      }
      else {
        toReagentPath = Well.reagents.restrict("libraryContentsVersion", getLibraryContentsVersion());
      }
      relationships.add(toReagentPath.to(Reagent.libraryContentsVersion).toFullEntity());
      relationships.add(toReagentPath.to(SmallMoleculeReagent.compoundNames));
      relationships.add(toReagentPath.to(SmallMoleculeReagent.pubchemCids));
      relationships.add(toReagentPath.to(SmallMoleculeReagent.chembankIds));
      relationships.add(toReagentPath.to(SmallMoleculeReagent.chemblIds));
      relationships.add(toReagentPath.to(SmallMoleculeReagent.molfileList));
      relationships.add(toReagentPath.to(SilencingReagent.facilityGenes).to(Gene.genbankAccessionNumbers).toFullEntity());
      dataFetcher.setPropertiesToFetch(relationships);

      writeSDFileSearchResults(writer, Lists.newArrayList(wellKeyStrs), dataFetcher);
    }
    finally {
      IOUtils.closeQuietly(writer);
    }
    return new FileInputStream(outFile);
  }

  private LibraryContentsVersion getLibraryContentsVersion()
  {
    if (_libraryContentsVersionRef != null) {
      return _libraryContentsVersionRef.value();
    }
    return null;
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
          writer.write(well, getLibraryContentsVersion());
        }
      }
    }
    // allow garbage collection
    _libraryContentsVersionRef.setValue(null);
  }
}
