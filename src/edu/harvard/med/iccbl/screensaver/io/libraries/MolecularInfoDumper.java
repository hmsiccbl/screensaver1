// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.libraries;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.util.CSVPrintWriter;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

public class MolecularInfoDumper
{
  private static final int TIMEOUT = 3000;
  private static Logger log = Logger.getLogger(MolecularInfoDumper.class);

  public static void main(String[] args) throws ParseException, IOException
  {
    CommandLineApplication app = new CommandLineApplication(args);
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("output-file").withLongOpt("output-directory").withDescription("Output file").create("f"));
    try {
      app.processOptions(true, true);
    }
    catch (ParseException e1) {
      System.exit(1);
    }
    File outputFile = app.getCommandLineOptionValue("f", File.class);
    final CSVPrintWriter writer = new CSVPrintWriter(new FileWriter(outputFile), "\n", "\t");

    GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    dao.runQuery(new Query() {
      private ScrollableResults _scroll;

      public List execute(Session session)
      {
        org.hibernate.Query query = session.createQuery("from Compound c");
        _scroll = query.scroll();
        int i = 0;
        while (_scroll.next()) {
          ++i;
          Compound compound = (Compound) _scroll.get(0);
          log.info(compound.getSmiles());
          MolecularInfoMaker x = new MolecularInfoMaker(compound);
          Thread thread = new Thread(x);
          thread.start();
          try {
            thread.join(TIMEOUT);
            thread.stop();
            if (x.isDone()) {
              writer.print(compound.getSmiles());
              writer.print(compound.getInchi());
              writer.print(x.getFormula());
              writer.print(x.getMass());
              writer.println();
            }
            else {
              log.error("timeout while calculating mass and formula for " + compound);
            }
          }
          catch (InterruptedException e) {
            log.error("thread interrupted");
          }

          if (i % 100 == 0) {
            session.clear();
            writer.flush();
            log.info("processed " + i + " compounds");
          }
        }
        if (i % 100 != 0) {
          log.info("processed " + i + " compounds");
        }
        writer.close();
        return null;
      }

      class MolecularInfoMaker implements Runnable
      {
        private Compound _compound;
        private String _formula;
        private float _mass;
        private boolean _done;

        MolecularInfoMaker(Compound compound) {
          _compound = compound;
        }

        public void run()
        {
          _formula = getFormattedMolecularFormula(_compound);
          _mass = _compound.getMolecularMass();
          _done = true;
        }

        public String getFormula()
        {
          return _formula;
        }

        public void setFormula(String formula)
        {
          _formula = formula;
        }

        public float getMass()
        {
          return _mass;
        }

        public void setMass(float mass)
        {
          _mass = mass;
        }

        public synchronized boolean isDone()
        {
          return _done;
        }

        public synchronized void setDone(boolean done)
        {
          _done = done;
        }
      };


      private String getFormattedMolecularFormula(Compound compound)
      {
        String formula = compound.getMolecularFormula();
        if (formula != null) {
          formula = formula.replaceAll("<sub>([0-9]+)</sub>", "$1").replaceAll("<sup>(.+)</sup>", "^[$1]");
        }
        return formula;
      }
    });
  }

}
