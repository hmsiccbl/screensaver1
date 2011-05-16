package edu.harvard.med.screensaver.io.screens;

import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.CommandLineApplication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.service.screens.ScreenDerivedPropertiesUpdater;

public class ScreeningStatisticsUpdater extends CommandLineApplication
{
  private static final Logger log = Logger.getLogger(ScreeningStatisticsUpdater.class);

  public static void main(String[] args)
  {
    new ScreeningStatisticsUpdater(args);
  }

  private int nModified = 0;
  private int nProcessed = 0;

  private ScreeningStatisticsUpdater(String[] args)
  {
    super(args);
    addCommandLineOption(OptionBuilder.hasArg(false).withDescription("do not modify database; report modified counts only").withLongOpt("no-updates").create("n"));
    processOptions(true, false);
    final boolean noUpdates = isCommandLineFlagSet("n");
    final GenericEntityDAO dao = getSpringBean("genericEntityDao", GenericEntityDAO.class);
    final ScreenDerivedPropertiesUpdater screenDerivedPropertiesUpdater = getSpringBean("screenDerivedPropertiesUpdater", ScreenDerivedPropertiesUpdater.class);
    for (final Screen screenIn : dao.findAllEntitiesOfType(Screen.class)) {
      if (screenIn.isStudyOnly()) {
        continue;
      }
      dao.doInTransaction(new DAOTransaction() {
        @Override
        public void runTransaction()
        {
          log.info("processing screen " + screenIn.getFacilityId());
          Screen screenOut = screenDerivedPropertiesUpdater.updateScreeningStatistics(screenIn);
          if (!noUpdates) {
            dao.flush();
          }
          dao.clear();
          if (!screenIn.isEquivalent(screenOut)) {
            log.info("modified screen " + screenIn.getFacilityId());
            ++nModified;
          }
          ++nProcessed;
        }
      });
    }
    log.info((noUpdates ? "would have " : "") + "modified " + nModified + " of " + nProcessed + " screen(s)");
  }
}
