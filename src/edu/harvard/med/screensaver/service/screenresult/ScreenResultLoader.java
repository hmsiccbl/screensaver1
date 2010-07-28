// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.screenresult;

import javax.persistence.EntityExistsException;

import com.google.common.collect.Sets;
import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.io.ParseErrorsException;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.service.EntityNotFoundException;


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
   * @param admin 
   * @param finalPlateNumberRange
   * @param incrementalFlush force the loader to periodically flush cached ResultValues and other Entities being held by the 
   *        Hibernate session.  Use this value to limit memory requirements for large datasets.  
   * @throws ParseErrorsException
   * @throws EntityNotFoundException
   * @throws EntityExistsException
   */
  @Transactional(propagation=Propagation.REQUIRES_NEW /* to ensure that errors cause rollback */, 
                 rollbackFor={ParseErrorsException.class, EntityNotFoundException.class, EntityExistsException.class})
  public ScreenResult parseAndLoad(Screen screen, 
                                   Workbook workbook,
                                   AdministratorUser admin,
                                   String comments,
                                   IntRange finalPlateNumberRange,
                                   boolean incrementalFlush)
    throws ParseErrorsException
  {
    screen = _dao.reloadEntity(screen);
    admin = _dao.reloadEntity(admin);
    ScreenResultParser screenResultParser = createScreenResultParser();
    screenResultParser.setIgnoreDuplicateErrors(_ignoreDuplicateErrors);
    ScreenResult screenResult = screenResultParser.parse(screen,
                                                         workbook,
                                                         finalPlateNumberRange,
                                                         incrementalFlush);
    if (screenResultParser.getHasErrors()) {
      // we communicate back any parse errors as a ParseErrorsException, as this
      // serves to rollback the transaction, preventing persistence of invalid
      // screen result entity
      throw new ParseErrorsException(screenResultParser.getErrors());
    }

    _dao.saveOrUpdateEntity(screen);

    screen.getScreenResult().createScreenResultDataLoading(admin, screenResultParser.getPlateNumbersLoadedWithMaxReplicates(), comments);
    int assayPlatesCreated = Sets.difference(screen.getAssayPlatesDataLoaded(), screen.getAssayPlatesScreened()).size();
    if (assayPlatesCreated > 0) {
      log.info("created " + assayPlatesCreated + " assay plate(s) that were not previously recorded as having been screened");
    }

    screen.update();

    log.info("Screen result data loading completed successfully!");
    return screenResult;
  }
}
