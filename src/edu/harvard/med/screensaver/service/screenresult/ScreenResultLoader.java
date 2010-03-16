// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.screenresult;

import javax.persistence.EntityExistsException;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.io.ParseErrorsException;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.service.EntityNotFoundException;

import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * Loads a {@link ScreenResult} into the database. This class wraps
 * {@link ScreenResultParser}, and is responsible for making newly parsed
 * {@link ScreenResult}s persistent.
 */
public abstract class ScreenResultLoader
{
  private static Logger log = Logger.getLogger(ScreenResultLoader.class);

  private GenericEntityDAO _dao;
  private ScreenResultsDAO _screenResultsDao;

  public static enum MODE
  {
    APPEND_IF_EXISTS,
    DELETE_IF_EXISTS,
    ERROR_IF_EXISTS
  };

  
  private boolean _ignoreDuplicateErrors;
  
  /**
   * @motivation for CGLIB2
   */
  protected ScreenResultLoader() {}

  public ScreenResultLoader(GenericEntityDAO dao,
                            ScreenResultsDAO screenResultsDao)
  {
    _dao = dao;
    _screenResultsDao = screenResultsDao;
  }
  
  public void setIgnoreDuplicateErrors(boolean value)
  {
    _ignoreDuplicateErrors = value;
  }
  
  /**
   * @motivation We make use of Spring's "Lookup method injection" feature here
   *             to ensure that we always have a new instance of the parser,
   *             since the parser is stateful and should not be reused.
   */
  abstract protected ScreenResultParser createScreenResultParser(); 
  
  
  /**
   * Load the screen results from a workbook into the database.
   * @param workbook 
   * @param finalPlateNumberRange
   * @param mode determines action on finding an already existent ScreenResult: delete, append, or throw an exception
   * @param screenNumber
   * @param incrementalFlush force the loader to periodically flush cached ResultValues and other Entities being held by the 
   *        Hibernate session.  Use this value to limit memory requirements for large datasets.  
   * @return
   * @throws ParseErrorsException
   * @throws EntityNotFoundException
   * @throws EntityExistsException
   */
  @Transactional(propagation=Propagation.REQUIRES_NEW /* to ensure that errors cause rollback */, 
                 rollbackFor={ParseErrorsException.class, EntityNotFoundException.class, EntityExistsException.class})
  public ScreenResult parseAndLoad(final Workbook workbook,
                                   final IntRange finalPlateNumberRange,
                                   MODE mode,
                                   final Integer screenNumber,
                                   boolean incrementalFlush)
  throws ParseErrorsException, EntityNotFoundException, EntityExistsException
  {
    Screen screen = _dao.findEntityByProperty(Screen.class, "screenNumber", screenNumber);
    
    if(screen == null)
    {
      throw new EntityNotFoundException(Screen.class, screenNumber);
    }

    if (screen.getScreenResult() != null) {
      if(mode == MODE.ERROR_IF_EXISTS)
      {
        throw new EntityExistsException(ScreenResult.class.getName() + ": " + screenNumber + " exists.");
      }
      else if(mode == MODE.APPEND_IF_EXISTS){
        log.info("appending existing screen result (loading existing screen result data)");
        _dao.need(screen.getScreenResult(), "dataColumns");//.resultValues");
      }
      else{ //(mode == MODE.DELETE_IF_EXISTS){
        log.info("deleting existing screen result for " + screen);
        _screenResultsDao.deleteScreenResult(screen.getScreenResult());
      }
    }
    
    return parseAndLoad(workbook, finalPlateNumberRange, screen, incrementalFlush);
  }

  /**
   * Load the screen results from a workbook into the database.
   * @param workbook 
   * @param finalPlateNumberRange
   * @param incrementalFlush force the loader to periodically flush cached ResultValues and other Entities being held by the 
   *        Hibernate session.  Use this value to limit memory requirements for large datasets.  
   * @return
   * @throws ParseErrorsException
   * @throws EntityNotFoundException
   * @throws EntityExistsException
   */
  @Transactional(propagation=Propagation.REQUIRES_NEW /* to ensure that errors cause rollback */, 
                 rollbackFor={ParseErrorsException.class, EntityNotFoundException.class, EntityExistsException.class})
  public ScreenResult parseAndLoad(final Workbook workbook,
                                        final IntRange finalPlateNumberRange,
                                        Screen screen,
                                        boolean incrementalFlush)
    throws ParseErrorsException
  {
    screen = _dao.reloadEntity(screen);
    ScreenResultParser screenResultParser = createScreenResultParser();
    screenResultParser.setIgnoreDuplicateErrors(_ignoreDuplicateErrors);
    ScreenResult screenResult = screenResultParser.parse(screen,
                                                         workbook,
                                                         finalPlateNumberRange,
                                                         incrementalFlush);
    if (screenResultParser.getHasErrors())
    {
      // TODO: it _is_ rather silly to collect all the exceptions, store them, then get them again, and rethrow them - sde4
      throw new ParseErrorsException(screenResultParser.getErrors());
    }
    _dao.saveOrUpdateEntity(screenResult);
    
    if(incrementalFlush)
    {
      log.info("populating screen_result_well_link table");
      _screenResultsDao.populateScreenResultWellLinkTable(screenResult.getScreenResultId());
    }
    
    _dao.saveOrUpdateEntity(screen);
    
    log.info("Import completed successfully!");
    return screenResult;
  }
}
