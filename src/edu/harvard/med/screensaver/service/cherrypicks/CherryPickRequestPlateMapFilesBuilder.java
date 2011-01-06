// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cherrypicks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.util.CSVPrintWriter;
import edu.harvard.med.screensaver.util.CustomNewlinePrintWriter;

/**
 * For a cherry pick request, builds the CSV files that define the assay plate
 * mapping.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class CherryPickRequestPlateMapFilesBuilder
{

  /**
   * File extension for each plate map file in the zip file.
   * Stewart Rudnicki has indicated that this file extension should lower-case..
   */
  private static final String PLATE_MAP_FILE_EXTENSION = ".csv";

  /**
   * Windows newline must be used! The consumer of the files we're generating is
   * a machine in the lab that has this requirement, so cross-platform concerns
   * are not a concern.
   */
  private static final String NEWLINE = "\r\n";

  private static final String[] CSV_FILE_HEADERS = {
    "Source Plate",
    "Source Copy",
    "Source Well",
    "Source Plate Type",
    "Destination Well",
    "Destination Plate Type",
    "Person Visiting",
    "Screen Number",
    "Volume"};


  private static final String README_FILE_NAME = "README.txt";


  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestPlateMapFilesBuilder.class);


  // instance data members

  private GenericEntityDAO genericEntityDao;
  private CherryPickRequestPlateMapper cherryPickRequestPlateMapper;


  // public constructors and methods

  public CherryPickRequestPlateMapFilesBuilder(GenericEntityDAO dao,
                                               CherryPickRequestPlateMapper cherryPickRequestPlateMapper)
  {
    this.genericEntityDao = dao;
    this.cherryPickRequestPlateMapper = cherryPickRequestPlateMapper;
  }


  // public constructors and methods

  public InputStream buildZip(final CherryPickRequest cherryPickRequestIn,
                              final Set<CherryPickAssayPlate> plates) throws IOException
  {
    CherryPickRequest cherryPickRequest = (CherryPickRequest) genericEntityDao.reattachEntity(cherryPickRequestIn);
    return doBuildZip(cherryPickRequest, plates);
  }


  // private methods

  @SuppressWarnings("unchecked")
  private InputStream doBuildZip(CherryPickRequest cherryPickRequest,
                                 Set<CherryPickAssayPlate> forPlates) throws IOException
  {
    ByteArrayOutputStream zipOutRaw = new ByteArrayOutputStream();
    ZipOutputStream zipOut = new ZipOutputStream(zipOutRaw);
    PrintWriter out = new CSVPrintWriter(new OutputStreamWriter(zipOut), NEWLINE);
    MultiMap/*<String,SortedSet<CherryPick>>*/ files2CherryPicks = buildCherryPickFiles(cherryPickRequest, forPlates);
    buildReadme(cherryPickRequest, zipOut);
    for (Iterator iter = files2CherryPicks.keySet().iterator(); iter.hasNext();) {
      String fileName = (String) iter.next();
      ZipEntry zipEntry = new ZipEntry(fileName);
      zipOut.putNextEntry(zipEntry);
      writeHeadersRow(out);
      for (LabCherryPick cherryPick : (SortedSet<LabCherryPick>) files2CherryPicks.get(fileName)) {
        writeCherryPickRow(out, cherryPick);
      }
      out.flush();
    }
    out.close();
    return new ByteArrayInputStream(zipOutRaw.toByteArray());
  }

  @SuppressWarnings("unchecked")
  private void buildReadme(CherryPickRequest cherryPickRequest,
                           ZipOutputStream zipOut)
    throws IOException
  {

    ZipEntry zipEntry = new ZipEntry(README_FILE_NAME);
    zipOut.putNextEntry(zipEntry);
    PrintWriter writer = new CustomNewlinePrintWriter(zipOut, NEWLINE);

    writer.println("This zip file contains plate mappings for Cherry Pick Request " +
                   cherryPickRequest.getCherryPickRequestNumber());
    writer.println();

    {
      StringBuilder buf = new StringBuilder();
      for (CherryPickAssayPlate assayPlate : cherryPickRequest.getActiveCherryPickAssayPlates()) {
        buf.
        append(assayPlate.getName()).
        append("\t").append(assayPlate.getStatusLabel());
        if (assayPlate.isPlatedAndScreened()) {
          buf.append("\t(").
          append(assayPlate.getCherryPickScreening().getDateOfActivity()).
          append(" by ").
          append(assayPlate.getCherryPickScreening().getPerformedBy().getFullNameFirstLast()).
          append(')');
        }
        else if (assayPlate.isPlated()) {
          buf.append("\t(").
          append(assayPlate.getCherryPickLiquidTransfer().getDateOfActivity()).
          append(" by ").
          append(assayPlate.getCherryPickLiquidTransfer().getPerformedBy().getFullNameFirstLast()).
          append(')');
        }
        buf.append(NEWLINE);
      }
      if (buf.length() > 0) {
        writer.println("Cherry pick plates:");
        writer.print(buf.toString());
        writer.println();
      }
    }

    Map<CherryPickAssayPlate,Integer> platesRequiringReload =
      cherryPickRequestPlateMapper.getAssayPlatesRequiringSourcePlateReload(cherryPickRequest);
    if (platesRequiringReload.size() > 0) {
      writer.println("WARNING: Some cherry pick plates will be created from the same source plate!");
      writer.println("You will need to reload one or more source plates for each of the following cherry pick plates:");
      for (CherryPickAssayPlate assayPlate : platesRequiringReload.keySet()) {
        writer.println("\tCherry pick plate '" + assayPlate.getName() +
                       "' requires reload of source plate " + platesRequiringReload.get(assayPlate));
      }
      writer.println();
    }

    {
      StringBuilder buf = new StringBuilder();
      MultiMap sourcePlateTypesForEachAssayPlate = getSourcePlateTypesForEachAssayPlate(cherryPickRequest);
      for (CherryPickAssayPlate assayPlate : cherryPickRequest.getActiveCherryPickAssayPlates()) {
        Set<PlateType> sourcePlateTypes = (Set<PlateType>) sourcePlateTypesForEachAssayPlate.get(assayPlate.getName());
        if (sourcePlateTypes != null && sourcePlateTypes.size() > 1) {
          buf.append(assayPlate.getName()).append(NEWLINE);
        }
      }
      if (buf.length() > 0) {
        writer.println("WARNING: Some cherry pick plates will be created from multiple source plates of non-uniform plate types!");
        writer.println("The following cherry pick plates are specified across multiple files:");
        writer.print(buf.toString());
        writer.println();
      }
    }

    writer.flush();
  }


  @SuppressWarnings("unchecked")
  /**
   * Normally, we create 1 file per assay plate. However, in the case where an
   * assay plate is comprised of wells from library copy plates that have
   * different plate types, we need to generate a separate file for each source
   * plate type (i.e., the assay plate will be defined over multiple files).
   * @return a MultiMap that partitions the cherry picks by file,
   * ordering both the file names and cherry picks for each file.
   */
  private MultiMap/*<String,SortedSet<CherryPick>>*/ buildCherryPickFiles(CherryPickRequest cherryPickRequest,
                                                                          Set<CherryPickAssayPlate> forPlates)
  {
    MultiMap assayPlate2SourcePlateTypes = getSourcePlateTypesForEachAssayPlate(cherryPickRequest);

    MultiMap result = MultiValueMap.decorate(new TreeMap<String,SortedSet<LabCherryPick>>(),
                                             new Factory()
    {
      public Object create() { return new TreeSet<LabCherryPick>(PlateMappingCherryPickComparator.getInstance()); }
    });

    // HACK: transform set of CPAP into a set of IDs, for purpose of checking
    // set membership; we can't rely upon CPAP.equals(), since we're comparing
    // non-managed entities with managed entities, and therefore we do not have
    // the guarantee of instance equality for entities with the same ID
    Set<Serializable> forPlateIds = new HashSet<Serializable>(forPlates.size());
    for (CherryPickAssayPlate cpap : forPlates) {
      if (cpap.getEntityId() == null) {
        throw new IllegalArgumentException("all members of 'forPlates' must already be persisted and have a database identifier");
      }
      forPlateIds.add(cpap.getEntityId());
    }

    for (LabCherryPick cherryPick : cherryPickRequest.getLabCherryPicks()) {
      if (cherryPick.isAllocated()) {
        CherryPickAssayPlate assayPlate = cherryPick.getAssayPlate();
        if (forPlates == null || (assayPlate != null && forPlateIds.contains(assayPlate.getEntityId()))) {
          Set<PlateType> sourcePlateTypes = (Set<PlateType>) assayPlate2SourcePlateTypes.get(assayPlate.getName());
          String fileName = makeFilename(cherryPick, sourcePlateTypes.size());
          result.put(fileName, cherryPick);
        }
      }
    }
    return result;
  }

  private String makeFilename(LabCherryPick cherryPick, int distinctSourcePlateTypes)
  {
    StringBuilder fileName = new StringBuilder(cherryPick.getAssayPlate().getName());

    if (distinctSourcePlateTypes > 1) {
      fileName.append(' ');
      fileName.append(cherryPick.getSourceCopy().findPlate(cherryPick.getSourceWell().getPlateNumber()).getPlateType().toString());
    }

    int attempt = cherryPick.getAssayPlate().getAttemptOrdinal() + 1;
    if (attempt > 0) {
      fileName.append( " (Run").append(attempt).append(")");
    }

    fileName.append(PLATE_MAP_FILE_EXTENSION);

    return fileName.toString();
  }

  private MultiMap getSourcePlateTypesForEachAssayPlate(CherryPickRequest cherryPickRequest)
  {
    MultiMap assayPlateName2PlateTypes = MultiValueMap.decorate(new HashMap(),
                                                             new Factory()
    {
      public Object create() { return new HashSet(); }
    });

    for (LabCherryPick cherryPick : cherryPickRequest.getLabCherryPicks()) {
      if (cherryPick.isAllocated() && cherryPick.isMapped()) {
        assayPlateName2PlateTypes.put(cherryPick.getAssayPlate().getName(),
                                      cherryPick.getSourceCopy().findPlate(cherryPick.getSourceWell().getPlateNumber()).getPlateType());
      }
    }
    return assayPlateName2PlateTypes;
  }


  private void writeCherryPickRow(PrintWriter out, LabCherryPick cherryPick)
  {
    out.print(cherryPick.getSourceWell().getPlateNumber());
    out.print(cherryPick.getSourceCopy().getName());
    out.print(cherryPick.getSourceWell().getWellName());
    out.print(cherryPick.getSourceCopy().findPlate(cherryPick.getSourceWell().getPlateNumber()).getPlateType());
    out.print(cherryPick.getAssayPlateWellName());
    out.print(cherryPick.getAssayPlate().getAssayPlateType());
    out.print(cherryPick.getCherryPickRequest().getRequestedBy().getFullNameFirstLast());
    out.print(cherryPick.getCherryPickRequest().getScreen().getFacilityId());
    out.print(cherryPick.getCherryPickRequest().getTransferVolumePerWellApproved().convert(VolumeUnit.MICROLITERS).getValue().setScale(2, RoundingMode.HALF_UP));
    out.println();
  }

  private void writeHeadersRow(PrintWriter out)
  {
    for (String string : CSV_FILE_HEADERS) {
      out.print(string);
    }
    out.println();
  }
}
