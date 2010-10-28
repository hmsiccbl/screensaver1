// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

import java.io.Serializable;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.annotations.Derived;

/**
 * Invokes the {@link edu.harvard.med.screensaver.domainlogic.EntityUpdater} appropriate for the specified entity class
 * on the specified entity and then persists the
 * updated entity. This utility is useful if data updates have been made to the database (outside of Screensaver's
 * purview) that require recalculation of {@link Derived @Derived} entity properties.
 * 
 * @author atolopko
 */
public class EntityUpdater
{
  private static final Logger log = Logger.getLogger(EntityUpdater.class);

  public static void main(String[] args) throws ParseException, ClassNotFoundException
  {
    CommandLineApplication app = new CommandLineApplication(args);
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withLongOpt("--entity-type").create("e"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withLongOpt("--entity-id").create("i"));
    if (!app.processOptions(true, true, true)) {
      System.exit(0);
    }
    updateEntity(Class.forName(app.getCommandLineOptionValue("e")),
                 app.getCommandLineOptionValue("i", Integer.class),
                 (GenericEntityDAO) app.getSpringBean("genericEntityDao"));
  }

  private static void updateEntity(final Class entityClass,
                                   final Serializable entityId,
                                   final GenericEntityDAO dao)
  {
    dao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Entity entity = dao.findEntityById(entityClass, entityId);
        if (entity == null) {
          log.error("no such entity " + entityClass + " for ID " + entityId);
        }
        log.info("updating entity " + entity);
        entity.invalidate();
        entity.update();
        dao.flush();
        log.info("entity updated");
      }
    });
  }
}
