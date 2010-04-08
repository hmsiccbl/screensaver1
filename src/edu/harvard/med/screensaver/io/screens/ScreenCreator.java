// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screens;

import java.io.FileNotFoundException;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.workbook2.WorkbookParseError;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Study;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Command-line application that creates a new screen in the database.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenCreator extends StudyCreator
{
  private static Logger log = Logger.getLogger(ScreenCreator.class);
  
  public static void main(String[] args)
  {
    try {
      ScreenCreator screenCreator = new ScreenCreator(args);
      screenCreator.createStudy();
    }
    catch (ParseException e) {
      String msg = "bad command line argument: " + /*app.getLastAccessOption().getOpt() +*/ e.getMessage();
      System.out.println(msg);
      log.error(msg);
      System.exit(1);
    }
    catch (DataIntegrityViolationException e) {
      String msg = "data integrity error: " + e.getMessage();
      System.out.println(msg);
      log.error(msg);
      System.exit(1);
    }
    catch (Exception e) {
      e.printStackTrace();
      log.error(e.toString());
      System.err.println("error: " + e.toString());
      System.exit(1);
    }
  }
  
  protected String protocol;

  public ScreenCreator(String args[]) throws ParseException
  {
    super(args);
    protocol = app.isCommandLineFlagSet("p") ? app.getCommandLineOptionValue("p") : null;
  }

  @Override
  protected void validateStudyNumber()
  {
    if (studyNumber >= Study.MIN_STUDY_NUMBER) {
      throw new IllegalArgumentException("screen number must < " + Study.MIN_STUDY_NUMBER);
    }
  }
  
  @SuppressWarnings("static-access")
  @Override
  protected void configureCommandLineArguments(CommandLineApplication app)
  {
    super.configureCommandLineArguments(app);
    app.addCommandLineOption(OptionBuilder.hasArg().withArgName("text").withLongOpt("protocol").create("p"));
  }
  
  @Override
  protected Study newStudy()
  {
    Screen study = (Screen) super.newStudy();
    study.setPublishableProtocol(protocol);
    return study;
  }
  
  @Override
  protected void importData(Study screen) 
  {
    if (screenResultFile != null) {
      ScreenResultParser parser = (ScreenResultParser) app.getSpringBean("screenResultParser");
      try {
        parser.parse((Screen) screen, screenResultFile, true);
      }
      catch (FileNotFoundException e) {
        String msg = "Screen result file not found: " + screenResultFile;
        log.error(msg);
        throw new DAOTransactionRollbackException(msg);
      }
      if (parser.getHasErrors()) {
        log.error("errors found in screen result file");
        for (WorkbookParseError error : parser.getErrors()) {
          log.error(error.toString());
        }
      }
      else {
        log.info("screen result successfully imported");
      }
    }
  }
}
