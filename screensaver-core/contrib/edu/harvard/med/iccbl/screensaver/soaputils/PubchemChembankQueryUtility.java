// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.soaputils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.harvard.med.iccbl.screensaver.soaputils.PugSoapUtil.NonSuccessStatus;
import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.util.NullSafeComparator;
import edu.harvard.med.screensaver.util.Pair;
import edu.harvard.med.screensaver.util.StringUtils;
import edu.mit.broad.chembank.shared.mda.webservices.service.FindBySimilarity1Fault1;
import edu.mit.broad.chembank.shared.mda.webservices.service.MoleculeWebServiceStub;
import edu.mit.broad.chembank.shared.mda.webservices.service.MoleculeWebServiceStub.FindBySimilarity;

/**
 * Perform a query of the Pubchem and the Chembank SOAP services for respective
 * ID's.<br>
 * Input to the query will be the SMILES string for each compound in the
 * respective Library.<br>
 * see the WSDL defined at:<br>
 * <a href="http://pubchem.ncbi.nlm.nih.gov/pug_soap/pug_soap.cgi?wsdl">http://
 * pubchem.ncbi.nlm.nih.gov/pug_soap/pug_soap.cgi?wsdl</a> <br>
 * and<br>
 * <a href=
 * "http://chembank.broad.harvard.edu/webservices/MoleculeWebService?wsdl"
 * >http://chembank.broad.harvard.edu/webservices/MoleculeWebService?wsdl</a> <br>
 * <br>
 * Example usage:<br>
 * bash ./bin/run.sh
 * edu.harvard.med.iccbl.screensaver.soaputils.PubchemChembankQueryUtility -D
 * screensaver -U screensaver -H localhost -library ChemDivAM -o ChemDivAM_1.csv
 * -r 20 --query-chembank --query-pubchem <br>
 */
public class PubchemChembankQueryUtility extends CommandLineApplication
{
  private static Logger log = Logger.getLogger(PubchemChembankQueryUtility.class);

  public static final int SHORT_OPTION_INDEX = 0;
  public static final int ARG_INDEX = 1;
  public static final int LONG_OPTION_INDEX = 2;
  public static final int DESCRIPTION_INDEX = 3;

  public static final String DELIMITER = ",";
  public static final String LIST_DELIMITER = ";";
  public static String HEADERLINE = Joiner.on(DELIMITER)
                  .join(new String[] {"id", "well_type", "smiles", "old_cids", "standardized_smiles", "new_cids", "chembank_ids"});


  public static final String[] LIBRARY_NAME = {
    "library", "library",
    "library-name",
    "Name of the library to be exported with CID Lookups"
    };

  public static final String[] QUERY_ALL_LIBRARIES = {
    "query_all_libraries", "",
    "query-all-libraries",
    "Perform queries for all of the small molecule libraries in the database (this will take a while)"
    };

  public static final String[] OUTPUT_FILE = {
    "o",
    "file",
    "output-file",
    "Name of the file to write to, if exists, then the utility will append to this file, and assume that records found there should not be queried in the current run."
    };

  public static final String[] TRY_LIMIT = {
    "r", "#",
    "try-limit",
    "Number of pubchem lookup retries to perform before giving up on a record"
    };

  public static final String[] INTERVAL_BETWEEN_TRIES = {
    "i", "ms",
    "interval-between-tries",
    "wait time between tries (milliseconds)"
    };

  public static final String[] QUERY_PUBCHEM = {
    "p", "",
    "query-pubchem",
    "perform the query of the Pubchem server for CID's"
    };

  public static final String[] QUERY_CHEMBANK = {
    "c", "",
    "query-chembank",
    "perform the query of the Chembank Server for ID's"
    };

  private Map<String,String> _extantSmilesLookups = Maps.newHashMap();

  public PubchemChembankQueryUtility(String[] cmdLineArgs)
  {
    super(cmdLineArgs);
  }

  @SuppressWarnings("static-access")
  public static void main(String[] args)
    throws IOException,
    InterruptedException
  {
    final PubchemChembankQueryUtility app = new PubchemChembankQueryUtility(args);

    String[] option = LIBRARY_NAME;
    app.addCommandLineOption(OptionBuilder.hasArg()
      .withArgName(option[ARG_INDEX])
      .withDescription(option[DESCRIPTION_INDEX])
      .withLongOpt(option[LONG_OPTION_INDEX])
      .create(option[SHORT_OPTION_INDEX]));
    
    option = QUERY_ALL_LIBRARIES;
    app.addCommandLineOption(OptionBuilder
      .withArgName(option[ARG_INDEX])
      .withDescription(option[DESCRIPTION_INDEX])
      .withLongOpt(option[LONG_OPTION_INDEX])
      .create(option[SHORT_OPTION_INDEX]));

    option = OUTPUT_FILE;
    app.addCommandLineOption(OptionBuilder.hasArg()
      .withArgName(option[ARG_INDEX])
      .withDescription(option[DESCRIPTION_INDEX])
      .withLongOpt(option[LONG_OPTION_INDEX])
      .create(option[SHORT_OPTION_INDEX]));

    option = TRY_LIMIT;
    app.addCommandLineOption(OptionBuilder.hasArg()
      .withArgName(option[ARG_INDEX])
      .withDescription(option[DESCRIPTION_INDEX])
      .withLongOpt(option[LONG_OPTION_INDEX])
      .create(option[SHORT_OPTION_INDEX]));

    option = INTERVAL_BETWEEN_TRIES;
    app.addCommandLineOption(OptionBuilder.hasArg()
      .withArgName(option[ARG_INDEX])
      .withDescription(option[DESCRIPTION_INDEX])
      .withLongOpt(option[LONG_OPTION_INDEX])
      .create(option[SHORT_OPTION_INDEX]));

    option = QUERY_PUBCHEM;
    app.addCommandLineOption(OptionBuilder
      .withArgName(option[ARG_INDEX])
      .withDescription(option[DESCRIPTION_INDEX])
      .withLongOpt(option[LONG_OPTION_INDEX])
      .create(option[SHORT_OPTION_INDEX]));

    option = QUERY_CHEMBANK;
    app.addCommandLineOption(OptionBuilder
      .withArgName(option[ARG_INDEX])
      .withDescription(option[DESCRIPTION_INDEX])
      .withLongOpt(option[LONG_OPTION_INDEX])
      .create(option[SHORT_OPTION_INDEX]));
    try {
      if (!app.processOptions(/* acceptDatabaseOptions= */true,
                                    /* showHelpOnError= */true)) {
        return;
      }

      final boolean queryPubchem = app.isCommandLineFlagSet(QUERY_PUBCHEM[SHORT_OPTION_INDEX]);
      final boolean queryChembank = app.isCommandLineFlagSet(QUERY_CHEMBANK[SHORT_OPTION_INDEX]);

      if (!(queryPubchem || queryChembank)) {
        log.error("Must specify either " + QUERY_PUBCHEM[LONG_OPTION_INDEX] +
          " or " + QUERY_CHEMBANK[LONG_OPTION_INDEX]);
        app.showHelp();
        return;
      }
      if(!app.isCommandLineFlagSet(LIBRARY_NAME[SHORT_OPTION_INDEX])
        && !app.isCommandLineFlagSet(QUERY_ALL_LIBRARIES[SHORT_OPTION_INDEX])) {
        log.error("Must specify either " + LIBRARY_NAME[LONG_OPTION_INDEX] +
          " or " + QUERY_ALL_LIBRARIES[LONG_OPTION_INDEX]);
        app.showHelp();
        return;
      }
      if(app.isCommandLineFlagSet(LIBRARY_NAME[SHORT_OPTION_INDEX])
        && app.isCommandLineFlagSet(QUERY_ALL_LIBRARIES[SHORT_OPTION_INDEX])) {
        log.error("Must specify either " + LIBRARY_NAME[LONG_OPTION_INDEX] +
          " or " + QUERY_ALL_LIBRARIES[LONG_OPTION_INDEX]);
        app.showHelp();
        return;
      }
      if(app.isCommandLineFlagSet(QUERY_ALL_LIBRARIES[SHORT_OPTION_INDEX])
        && app.isCommandLineFlagSet(OUTPUT_FILE[SHORT_OPTION_INDEX])) {
        log.error("option \"" + OUTPUT_FILE[LONG_OPTION_INDEX] + "\" not allowed with \"" + QUERY_ALL_LIBRARIES[LONG_OPTION_INDEX] + "\" option.");
        app.showHelp();
        return;
      }
      //      if(app.isCommandLineFlagSet(LIBRARY_NAME[SHORT_OPTION_INDEX])
      //        && !app.isCommandLineFlagSet(OUTPUT_FILE[SHORT_OPTION_INDEX])) {
      //        log.error("option \"" + OUTPUT_FILE[LONG_OPTION_INDEX] + "\" must be specified with \"" + LIBRARY_NAME[LONG_OPTION_INDEX] + "\" option.");
      //        app.showHelp();
      //        return;
      //      }

      final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
      dao.doInTransaction(new DAOTransaction() {
        public void runTransaction(){
          PrintWriter writer = null;
          PrintWriter errorWriter = null;
          try {

            int intervalMs = PugSoapUtil.INTERVAL_BETWEEN_TRIES_MS;
            if (app.isCommandLineFlagSet(INTERVAL_BETWEEN_TRIES[SHORT_OPTION_INDEX])) {
              intervalMs = app.getCommandLineOptionValue(INTERVAL_BETWEEN_TRIES[SHORT_OPTION_INDEX],
                                                         Integer.class);
            }

            int numberOfTries = PugSoapUtil.TRY_LIMIT;
            if (app.isCommandLineFlagSet(TRY_LIMIT[SHORT_OPTION_INDEX])) {
              numberOfTries = app.getCommandLineOptionValue(TRY_LIMIT[SHORT_OPTION_INDEX],
                                                            Integer.class);
            }
            
            List<Library> libraries = Lists.newArrayList();
            if(app.isCommandLineFlagSet(LIBRARY_NAME[SHORT_OPTION_INDEX]))
            {
              String temp = app.getCommandLineOptionValue(LIBRARY_NAME[SHORT_OPTION_INDEX]);
              for(String libraryName:temp.split(","))
              {
                Library library = dao.findEntityByProperty(Library.class,
                                                           "shortName",
                                                           libraryName.trim());
                if (library == null) {
                  throw new IllegalArgumentException("no library with short name: " +
                    libraryName);
                }
                libraries.add(library);
              }
              
              // if there is only one library to query, then set these values from the command line option
              if(libraries.size() == 1) {
                String outputFilename = app.getCommandLineOptionValue(OUTPUT_FILE[SHORT_OPTION_INDEX]);
                writer = app.getOutputFile(outputFilename);
                errorWriter = app.getOutputFile(outputFilename + ".errors");
              }
            } else if (app.isCommandLineFlagSet(QUERY_ALL_LIBRARIES[SHORT_OPTION_INDEX])) {
              libraries = dao.findEntitiesByProperty(Library.class, "screenType", ScreenType.SMALL_MOLECULE );
              for(Iterator<Library> iter = libraries.iterator(); iter.hasNext(); )
              {
                Library library = iter.next();
                if(library.getLibraryType() == LibraryType.ANNOTATION 
                  || library.getLibraryType() == LibraryType.NATURAL_PRODUCTS )
                {
                  iter.remove();
                }
              }
            }
            
            Collections.sort(libraries, new NullSafeComparator<Library>() {
              @Override
              protected int doCompare(Library o1, Library o2)
              {
                return o1.getShortName().compareTo(o2.getShortName());
              }

            });

            List<String> libraryNames = Lists.transform(libraries, new Function<Library,String>() {
              @Override
              public String apply(Library from)
              {
                return from.getShortName();
              }
            });
            log.info("libraries to process:\n" + libraryNames);
            
            int i = 0;
            for(Library library:libraries)
            {
              if(writer == null || i>0)
              {
                writer = app.getOutputFile(library.getShortName());
              }
              if(errorWriter == null || i>0)
              {
                errorWriter = app.getOutputFile(library.getShortName() + ".errors");
              }
              log.info("\nProcessing the library: " + library.getShortName() 
                       + "\nlong name: " + library.getLibraryName() 
                       + "\noutput file: " + library.getShortName() + ".csv");
              app.query(library,
                    queryPubchem,
                    queryChembank,dao,intervalMs,numberOfTries,writer,errorWriter);
              i++;
            }
          }
          catch (Exception e) {
            throw new DAOTransactionRollbackException(e);
          }
          finally {
            if(writer != null ) writer.close();
            if(errorWriter != null) errorWriter.close();
          }
        }

      });
      System.exit(0);
    }
    catch (ParseException e) {
      log.error("error parsing command line options: " + e.getMessage());
    }
  }
  private PrintWriter getOutputFile(String outputFilenamebase) throws FileNotFoundException
  {
    PrintWriter writer;
    File file = new File(outputFilenamebase + ".csv");
    boolean append = file.exists();
    if (file.exists())
    {
      readFileForExistingRecords(file);
    }
    log.info("opened file: " + file + " append: " + append);
    // will have only one of these if specifying the library
    writer = new PrintWriter(new FileOutputStream(file,append));
    if (!append) writer.println(HEADERLINE);
    return writer;
  }

  private void query(final Library library,
                     final boolean queryPubchem,
                     final boolean queryChembank,
                     final GenericEntityDAO dao,
                     int intervalMs,
                     int numberOfTries,
                     PrintWriter writer,
                     PrintWriter errorWriter) throws InterruptedException, FindBySimilarity1Fault1
  {
    Set<Well> wells = library.getWells();
    log.info("begin: " + wells.size() + " compounds to look up.");

    StringWriter buf = new StringWriter();
    boolean fail = false;
    String errMsgPC = "";
    String errMsgCB = "";
    long startTime = System.currentTimeMillis();
    int smilesCount = 1;
    int failCount = 0;
    for (Well well : wells)
    {
      if (hasWellBeenReadAlready(well.getWellId()))
      {
        log.info("This well has already been read: " +
          well.getWellId());
        continue;
      }
      else
      {
        if (well.getLibraryWellType()
          .equals(LibraryWellType.EXPERIMENTAL))
        {
          long loopTime = System.currentTimeMillis();
          well = dao.reloadEntity(well, true,
                                  Well.reagents);
          SmallMoleculeReagent reagent = (SmallMoleculeReagent) well.getReagents()
            .get(library.getLatestContentsVersion());
          if (reagent == null)
          {
            log.warn("Null reagent for well: " + well.getWellId());
          }
          else {
            String smiles = reagent.getSmiles();
            
            buf.write(well.getWellId());
            buf.write(DELIMITER);

            buf.write("" + well.getLibraryWellType());
            buf.write(DELIMITER);

            buf.write(smiles);
            buf.write(DELIMITER);

            buf.write(Joiner.on(LIST_DELIMITER)
              .join(reagent.getPubchemCids()));
            buf.write(DELIMITER);

            if(StringUtils.isEmpty(smiles)) {
              errMsgPC = "Smiles string is empty";
              errMsgCB = errMsgPC;
              fail = true;
            }
            
            if (!fail && queryPubchem)
            {
              try
              {
                Pair<String,int[]> result = PugSoapUtil.standardizeAndIdentitySearch(smiles,
                                                                                     intervalMs,
                                                                                     numberOfTries);
                buf.write(result.getFirst());
                buf.write(DELIMITER);

                List<Integer> cids = Lists.newLinkedList();
                for (int cid : result.getSecond())
                  cids.add(cid);
                if (!cids.isEmpty())
                                    buf.write(Joiner.on(LIST_DELIMITER)
                  .join(cids));
                buf.write(DELIMITER);

                smilesCount++;
              }
              catch (NonSuccessStatus e) {
                errMsgPC = e.getMessage();
                String errMsg = "Non successful lookup: "
                  + "\twell: " + well.getWellId()
                  + ", SMILES: " + smiles;
                log.error(errMsg, e);
                fail = true;
              }
              catch (RemoteException e) {
                errMsgPC = "Lookup Failed: " + e.getClass()
                  .getName() +
                  ", msg: " + e.getLocalizedMessage();
                String errMsg = "Lookup Failed: _"
                  + "\twell: " + well.getWellId()
                  + ", SMILES: " + smiles;
                log.error(errMsg, e);
                fail = true;
              }
            }
            else {
              buf.write(DELIMITER);
              buf.write(DELIMITER);
            }

            if (!fail && queryChembank) {
              try
              {
                List<String> chembankIDs = getChembankIdsForSmiles(smiles);
                if (!chembankIDs.isEmpty())
                                           buf.write(Joiner.on(LIST_DELIMITER)
                  .join(chembankIDs));
              }
              catch (RemoteException e) {
                errMsgCB = "Lookup Failed: " + e.getClass()
                  .getName() +
                  ", msg: " + e.getLocalizedMessage();
                String errMsg = "Lookup Failed:"
                  + "\twell: " + well.getWellId()
                  + ", SMILES: " + smiles;
                log.error(errMsg, e);
                fail = true;
              }
            }
            else {
                      // last element does not need a delimiter
            }

            if (!fail) {
              writer.write(buf.toString());
              writer.println();
              writer.flush();
              long currentTime = System.currentTimeMillis();
              log.info("Well: " + well.getWellId() + ", SMILES: " + smiles +
                ", Loop time: " + (currentTime - loopTime) +
                ", avg: " + (currentTime - startTime) / smilesCount +
                ", cumulative: " + (float) (((double) (currentTime - startTime)) / (double) 60000) + " min");
            }
            else {
              log.warn("Line not written due to errors");
              errorWriter.write(buf.toString());

              if (!errMsgPC.isEmpty()) {
                errorWriter.write(DELIMITER);
                errorWriter.write(errMsgPC);
                errMsgPC = "";
              }
              if (!errMsgCB.isEmpty()) {
                errorWriter.write(DELIMITER);
                errorWriter.write(errMsgCB);
                errMsgCB = "";
              }
              errorWriter.println();
              errorWriter.flush();
              if(fail) failCount++;
              fail = false;
            }
            buf = new StringWriter();
          }
        } // experimental
        else {
          log.info("not experimental: " + well.getWellId() + ", " + well.getLibraryWellType());
        }
      }
    } // for wells
    log.info("Finished: Library: " + library.getShortName() + ", failCount: " + failCount);
  }

  public static List<String> getChembankIdsForSmiles(String smiles)
    throws FindBySimilarity1Fault1,
    RemoteException
  {
    log.debug("query chembank against SMILES " + smiles);

    MoleculeWebServiceStub soap = new MoleculeWebServiceStub();

    FindBySimilarity req = new FindBySimilarity();
    req.setSmiles(smiles);
    req.setThreshold((double) 1.0);
    edu.mit.broad.chembank.shared.mda.webservices.service.MoleculeWebServiceStub.ArrayOfMolecule array =
      soap.findBySimilarity(req)
      .getFindBySimilarityReturn();

    List<String> ids = Lists.newLinkedList();
    for (edu.mit.broad.chembank.shared.mda.webservices.service.MoleculeWebServiceStub.Molecule molecule : array.getMolecule()) {
      ids.add(molecule.getChembankId());
    }
    return ids;
  }
  
  private boolean hasWellBeenReadAlready(String wellId)
  {
    return _extantSmilesLookups.containsKey(wellId);
  }

  private void readFileForExistingRecords(File file)
  {
    _extantSmilesLookups = Maps.newHashMap();
    log.info("Read in the csv file");
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = reader.readLine();
      int lineNumber = 0;
      while (line != null) {
        String[] fields = line.split(DELIMITER);
        if (fields.length < 3) {
          log.error("line: " + lineNumber + ": " + line +
            " of the existing file " + file.getCanonicalPath() +
            " is incorrect (wrong number of fields), please correct or delete this line.");
        }
        else {
          _extantSmilesLookups.put(fields[0], fields[2]);
        }

        line = reader.readLine();
        lineNumber++;
      }
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }
    catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

  }
}
