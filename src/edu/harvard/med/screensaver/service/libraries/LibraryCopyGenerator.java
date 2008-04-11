// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.screenresults.ExtantLibraryException;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.PlateType;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public class LibraryCopyGenerator
{
  // static members

  private static Logger log = Logger.getLogger(LibraryCopyGenerator.class);


  // instance data members

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;

  
  // public constructors and methods
  
  public LibraryCopyGenerator(GenericEntityDAO dao,
                              LibrariesDAO librariesDao)
  {
    _dao = dao;
    _librariesDao = librariesDao;
  }
                                   
  @Transactional
  public List<CopyInfo> createPlateCopies(List<Integer> plateNumbers,
                                          List<String> copyNames,
                                          BigDecimal volume,
                                          PlateType plateType,
                                          Date datePlated) 
    throws ExtantLibraryException
  {
    List<CopyInfo> result = new ArrayList<CopyInfo>(copyNames.size());
    for (Integer plateNumber : plateNumbers) {
      result.addAll(createPlateCopies(plateNumber, copyNames, volume, plateType, datePlated));
    }
    return result;
  }
  
  @Transactional
  public List<CopyInfo> createPlateCopies(Integer plateNumber,
                                          List<String> copyNames,
                                          BigDecimal volume,
                                          PlateType plateType,
                                          Date datePlated) 
    throws ExtantLibraryException
  {
    List<CopyInfo> result = new ArrayList<CopyInfo>(copyNames.size());
    Library library = _librariesDao.findLibraryWithPlate(plateNumber);
    if (library == null) {
      throw new ExtantLibraryException("no library for plate " + plateNumber);
    }
    for (String copyName : copyNames) {
      Copy copy = library.getCopy(copyName);
      if (copy == null) {
        copy = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, copyName);
        log.info("created " + copy + " for library " + library.getLibraryName());
      }
      CopyInfo copyInfo = createPlateCopy(copy, plateNumber, volume, plateType, datePlated);
      if (copyInfo != null) {
        result.add(copyInfo);
      }
    }
    return result;
  }

  /**
   * Create a new plate copy for the specified copy and plate number
   */
  @Transactional
  public CopyInfo createPlateCopy(Copy copy,
                                  Integer plateNumber,
                                  BigDecimal volume,
                                  PlateType plateType,
                                  Date datePlated)
  {
    CopyInfo copyInfo = copy.getCopyInfo(plateNumber);
    if (copyInfo == null) {
      copyInfo = copy.createCopyInfo(plateNumber, "<unknown>", plateType, volume);
      copyInfo.setDatePlated(datePlated);
      log.info("created " + copyInfo + " for " + copy);
      return copyInfo;
    }
    else {
      if (!(copyInfo.getMicroliterWellVolume().equals(volume) &&
        copyInfo.getPlateType().equals(plateType))) {
        throw new DataModelViolationException("attempted to create a new plate copy that already exists (and that has different values for plate type and/or initial volume)"); 
      }
      return null;
    }
  }
  
  // protected methods

  /**
   * @motivation for CGLIB2
   */
  protected LibraryCopyGenerator()
  {
  }

  // private methods

}

