// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.libraries.compound;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.io.libraries.compound.StructureImageProvider;
import edu.harvard.med.screensaver.model.libraries.Compound;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class SmallMoleculeLibraryStructureImageGenerator
{
  private static Logger log = Logger.getLogger(SmallMoleculeLibraryStructureImageGenerator.class);
  
  //private static final String IMAGE_FILE_EXTENSION = ".png";


  public static void main(String[] args) throws ParseException
  {
    CommandLineApplication app = new CommandLineApplication(args);
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("library short name").withLongOpt("library-name").withDescription("Short name of the library").create("l"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("directory").withLongOpt("output-directory").withDescription("Output directory for generated images").create("d"));
    try {
      app.processOptions(true, true);
    }
    catch (ParseException e1) {
      System.exit(1);
    }
    File outputDirectory = app.getCommandLineOptionValue("d", File.class);

    final String libraryShortName = app.getCommandLineOptionValue("l");
    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    final StructureImageProvider imageProvider = new DaylightOrchestraStructureImageProvider();
    List<Compound> compounds = dao.runQuery(new Query() {
      public List execute(org.hibernate.Session session) 
      {
         org.hibernate.Query hqlQuery = session.createQuery("select c from Library l join l.wells w join w.compounds c where l.shortName = :shortName");
         hqlQuery.setString("shortName", libraryShortName);
         return hqlQuery.list();
      }; 
    });
    
    int nImagesGenerated = 0;
    for (Compound compound : compounds) {
      String smiles = compound.getSmiles();
      try {
        File imageFile =
          new File(outputDirectory, 
                   StaticHashedSmilesStructureImageProvider.makeRelativeImageFilePath(smiles));
        FileUtils.forceMkdir(imageFile.getParentFile());
        OutputStream imageOut = new BufferedOutputStream(new FileOutputStream(imageFile));
        InputStream in = imageProvider.getImage(compound);
        IOUtils.copy(in, imageOut);
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(imageOut);
        log.info("created image file " + imageFile + " for smiles " + smiles); 
        if (++nImagesGenerated % 1000 == 0) {
          log.debug("generated " + nImagesGenerated + " images");
        }
      }
      catch (Exception e) {
        log.error("error generating image for " + smiles + ": " + e.getMessage());
        e.printStackTrace();
      }
    }

    log.info("generated " + nImagesGenerated + " images");
  }


  // instance data members

  // public constructors and methods

  // private methods

}
