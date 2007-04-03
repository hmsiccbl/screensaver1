// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.screens.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.util.CSVPrintWriter;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.log4j.Logger;

/**
 * For a cherry pick request, builds the CSV files that define the assay plate
 * mapping.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class CherryPickRequestPlateMapFilesBuilder
{
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


  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestPlateMapFilesBuilder.class);


  // instance data members
  
  private DAO dao;


  // public constructors and methods

  public CherryPickRequestPlateMapFilesBuilder(DAO dao)
  {
    this.dao = dao;
  }


  // public constructors and methods

  public InputStream buildZip(final CherryPickRequest cherryPickRequestIn,
                              final Set<CherryPickAssayPlate> plates) throws IOException
  {
    CherryPickRequest cherryPickRequest = (CherryPickRequest) dao.reattachEntity(cherryPickRequestIn);
    return doBuildZip(cherryPickRequest, plates);
  }

  
  // private methods

  @SuppressWarnings("unchecked")
  private InputStream doBuildZip(CherryPickRequest cherryPickRequest,
                                 Set<CherryPickAssayPlate> forPlates) throws IOException
  {
    ByteArrayOutputStream zipOutRaw = new ByteArrayOutputStream();
    ZipOutputStream zipOut = new ZipOutputStream(zipOutRaw);
    CSVPrintWriter csv = new CSVPrintWriter(new OutputStreamWriter(zipOut));
    MultiMap/*<String,SortedSet<CherryPick>>*/ files2CherryPicks = buildCherryPickFiles(cherryPickRequest, forPlates);
    for (Iterator iter = files2CherryPicks.keySet().iterator(); iter.hasNext();) {
      String fileName = (String) iter.next();
      ZipEntry zipEntry = new ZipEntry(fileName);
      zipOut.putNextEntry(zipEntry);
      writeHeadersRow(csv);
      for (LabCherryPick cherryPick : (SortedSet<LabCherryPick>) files2CherryPicks.get(fileName)) {
        writeCherryPickRow(csv, cherryPick);
      }
      csv.flush();
    }
    csv.close();
    return new ByteArrayInputStream(zipOutRaw.toByteArray());
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
    MultiMap assayPlate2SourcePlateTypes = getSourcePlatesTypesForEachAssayPlate(cherryPickRequest);
    
    MultiMap result = MultiValueMap.decorate(new TreeMap<String,SortedSet<LabCherryPick>>(),
                                             new Factory() 
    {
      public Object create() { return new TreeSet<LabCherryPick>(PlateMappingCherryPickComparator.getInstance()); } 
    });
    for (LabCherryPick cherryPick : cherryPickRequest.getLabCherryPicks()) {
      if (cherryPick.isAllocated()) {
        CherryPickAssayPlate assayPlate = cherryPick.getAssayPlate();
        if (forPlates != null && !forPlates.contains(assayPlate)) {
          continue;
        }
        Set<PlateType> sourcePlateTypes = (Set<PlateType>) assayPlate2SourcePlateTypes.get(assayPlate.getName());
        String fileName = makeFilename(cherryPick, sourcePlateTypes.size());
        result.put(fileName, cherryPick);
      }
    }
    return result;
  }

  private String makeFilename(LabCherryPick cherryPick, int sourcePlates)
  {
    StringBuilder fileName = new StringBuilder(cherryPick.getAssayPlate().getName());
    
    if (sourcePlates > 1) {
      fileName.append(' ');
      fileName.append(cherryPick.getSourceCopy().getCopyInfo(cherryPick.getSourceWell().getPlateNumber()).getPlateType().toString());
    }
    
    int attempt = cherryPick.getAssayPlate().getAttemptOrdinal() + 1;
    if (attempt > 0) {
      fileName.append( " (Run").append(attempt).append(")");
    }
    
    fileName.append(".CSV");
    
    return fileName.toString();
  }

  private MultiMap getSourcePlatesTypesForEachAssayPlate(CherryPickRequest cherryPickRequest)
  {
    MultiMap sourcePlate2PlateTypes = MultiValueMap.decorate(new HashMap(),
                                                             new Factory() 
    {
      public Object create() { return new HashSet(); } 
    });
                                                             
    for (LabCherryPick cherryPick : cherryPickRequest.getLabCherryPicks()) {
      if (cherryPick.isAllocated()) {
        sourcePlate2PlateTypes.put(cherryPick.getAssayPlate().getName(),
                                   cherryPick.getSourceCopy().getCopyInfo(cherryPick.getSourceWell().getPlateNumber()).getPlateType());
      }
    }
    return sourcePlate2PlateTypes;
  }


  private void writeCherryPickRow(CSVPrintWriter csv, LabCherryPick cherryPick)
  {
    csv.print(cherryPick.getSourceWell().getPlateNumber());
    csv.print(cherryPick.getSourceCopy().getName());
    csv.print(cherryPick.getSourceWell().getWellName());
    csv.print(cherryPick.getSourceCopy().getCopyInfo(cherryPick.getSourceWell().getPlateNumber()).getPlateType().getFullName());
    csv.print(cherryPick.getAssayPlateWellName());
    csv.print(cherryPick.getAssayPlate().getAssayPlateType().getFullName());
    csv.print(cherryPick.getCherryPickRequest().getRequestedBy().getFullNameFirstLast());
    csv.print(cherryPick.getCherryPickRequest().getMicroliterTransferVolumePerWellApproved());
    csv.println();
  }

  private void writeHeadersRow(CSVPrintWriter csv)
  {
    for (String string : CSV_FILE_HEADERS) {
      csv.print(string);
    }
    csv.println();
  }
}
