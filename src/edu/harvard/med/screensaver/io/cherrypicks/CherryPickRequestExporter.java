//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.io.cherrypicks;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.io.workbook2.Workbook2Utils;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.ScreenerCherryPick;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;

/**
 * Exports the cherry picks of a CherryPickRequest to an Excel file, to be
 * provided to the screener(s).
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class CherryPickRequestExporter
{

  // static members

  private static final String LIST_OF_VALUES_DELIMITER = ", ";

  private static Logger log = Logger.getLogger(CherryPickRequestExporter.class);
  
  private static final int SCREENER_CHERRY_PICKS_SHEET_INDEX = 0;
  private static final int LAB_CHERRY_PICKS_SHEET_INDEX = 1;

  @SuppressWarnings("static-access")
  public static void main(String[] args) throws Exception
  {
    final CommandLineApplication app = new CommandLineApplication(args);
    app.addCommandLineOption(OptionBuilder.isRequired().hasArg().withArgName("cherry pick request #").create("n"));
    app.addCommandLineOption(OptionBuilder.isRequired().hasArg().withArgName("output workbook file").create("f"));
    if (!app.processOptions(true, true)) {
      System.exit(1);
    }
    final CherryPickRequestExporter exporter = (CherryPickRequestExporter) app.getSpringBean("cherryPickRequestExporter");
    exporter.getDao().doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        try { 
          Integer cherryPickRequestNumber = app.getCommandLineOptionValue("n", Integer.class);
          RNAiCherryPickRequest rnaiCherryPickRequest = 
            exporter.getDao().findEntityByProperty(RNAiCherryPickRequest.class, 
                                                   "cherryPickRequestNumber", 
                                                   cherryPickRequestNumber);
          if (rnaiCherryPickRequest == null) {
            throw new IllegalArgumentException("no such cherry pick request number " + cherryPickRequestNumber);
          }
          Workbook workbook = exporter.exportRNAiCherryPickRequest(rnaiCherryPickRequest);
          File file = app.getCommandLineOptionValue("f", File.class);
          WritableWorkbook workbook2 = Workbook.createWorkbook(file, workbook);
          workbook2.write();
          workbook2.close();
          log.info("cherry pick request exported to " + file);
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }
    });
  }


  // instance data members
  
  private DAO _dao;
  

  // public constructors and methods

  public CherryPickRequestExporter(DAO dao)
  {
    this._dao = dao;
  }

  public DAO getDao()
  {
    return _dao;
  }

  public Workbook exportRNAiCherryPickRequest(final RNAiCherryPickRequest cherryPickRequestIn)
  {
    final Workbook[] result = new Workbook[1];
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction() 
      {
        try {
          RNAiCherryPickRequest cherryPickRequest = (RNAiCherryPickRequest) _dao.reloadEntity(cherryPickRequestIn);
          
          ByteArrayOutputStream rawBytes = new ByteArrayOutputStream();
          OutputStream out = new BufferedOutputStream(rawBytes);
          WritableWorkbook workbook = Workbook.createWorkbook(out);

          writeCherryPicks(workbook, cherryPickRequest);
          workbook.write();
          workbook.close();
          result[0] = Workbook.getWorkbook(new ByteArrayInputStream(rawBytes.toByteArray()));
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }
    });
    return result[0];
  }


  // private methods

  private void writeCherryPicks(WritableWorkbook workbook, CherryPickRequest cherryPickRequest) throws RowsExceededException, WriteException
  {
    writeScreenerCherryPicks(workbook, cherryPickRequest);
    writeLabCherryPicks(workbook, cherryPickRequest);
  }

  private void writeLabCherryPicks(WritableWorkbook workbook, CherryPickRequest cherryPickRequest) throws RowsExceededException, WriteException
  {
    // TODO: this sheet is not always representing duplexes (it could be representing cherry picked pool wells, e.g.), 
    // but there is no *easy* way to determine if this is the case, so we'll just default to naming this sheet "Duplexes"
    WritableSheet sheet = workbook.createSheet("Duplexes", LAB_CHERRY_PICKS_SHEET_INDEX);
    writeLabCherryPicksHeaders(sheet);

    int iRow = 0;
    for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
      if (!labCherryPick.isFailed()) {
        writeLabCherryPick(labCherryPick, sheet, ++iRow);
      }
    }
  }

  private void writeLabCherryPick(LabCherryPick labCherryPick, WritableSheet sheet, int iRow) throws RowsExceededException, WriteException
  {
    Well sourceWell = labCherryPick.getSourceWell();
    Gene gene = sourceWell.getGene();
    Workbook2Utils.writeRow(sheet,
                            iRow, 
                            (gene == null ? null : gene.getEntrezgeneSymbol()),
                            (gene == null ? null : gene.getEntrezgeneId()),
                            (gene == null ? null : StringUtils.makeListString(gene.getGenbankAccessionNumbers(), LIST_OF_VALUES_DELIMITER)),
                            (gene == null ? null : gene.getGeneName()),
                            (labCherryPick.getAssayPlate() == null ? null : labCherryPick.getAssayPlate().getPlateOrdinal() + 1),
                            (labCherryPick.getAssayPlateWellName() == null ? null : labCherryPick.getAssayPlateWellName()),
                            StringUtils.makeListString(CollectionUtils.
                                                       collect(sourceWell.getSilencingReagents(),
                                                                             new Transformer() 
                                                       {
                                                         public Object transform(Object silencingReagent) 
                                                         { 
                                                           return ((SilencingReagent) silencingReagent).getSequence(); 
                                                         } 
                                                       }), LIST_OF_VALUES_DELIMITER),
                            sourceWell.getVendorIdentifier());
  }

  private List<Map<WellKey,ResultValue>> getResultValuesForDataHeaders(ScreenerCherryPick screenerCherryPick)
  {
    List<Map<WellKey,ResultValue>> result = new ArrayList<Map<WellKey,ResultValue>>();
    ScreenResult screenResult = screenerCherryPick.getCherryPickRequest().getScreen().getScreenResult();
    if (screenResult != null) {
      for (ResultValueType rvt : screenResult.getResultValueTypes()) {
        result.add(rvt.getResultValues());
      }
    }
    return result;
  }


  private void writeScreenerCherryPicks(WritableWorkbook workbook, CherryPickRequest cherryPickRequest) throws RowsExceededException, WriteException
  {
    // TODO: this sheet is not always representing pools (it could be representing duplex cherry pick wells, e.g.), 
    // but there is no *easy* way to determine if this is the case, so we'll just default to naming this sheet "Pools"
    WritableSheet sheet = workbook.createSheet("Pools", SCREENER_CHERRY_PICKS_SHEET_INDEX);
    writeScreenerCherryPicksHeaders(sheet, cherryPickRequest.getScreen().getScreenResult());

    int iRow = 0;
    for (ScreenerCherryPick screenerCherryPick : cherryPickRequest.getScreenerCherryPicks()) {
      writeScreenerCherryPick(screenerCherryPick, sheet, ++iRow);
    }
  }

  private void writeScreenerCherryPick(ScreenerCherryPick screenerCherryPick, WritableSheet sheet, int iRow) throws RowsExceededException, WriteException
  {
    Well screenedWell = screenerCherryPick.getScreenedWell();
    Gene gene = screenedWell.getGene();
    Workbook2Utils.writeRow(sheet,
                            iRow, 
                            (gene == null ? null : gene.getEntrezgeneSymbol()),
                            (gene == null ? null : gene.getEntrezgeneId()),
                            (gene == null ? null : StringUtils.makeListString(gene.getGenbankAccessionNumbers(), 
                                                                              LIST_OF_VALUES_DELIMITER)),
                            (gene == null ? null : gene.getGeneName()),
                            StringUtils.makeListString(CollectionUtils.
                                                       collect(screenedWell.getSilencingReagents(),
                                                                             new Transformer() 
                                                       {
                                                         public Object transform(Object silencingReagent) 
                                                         { 
                                                           return ((SilencingReagent) silencingReagent).getSequence(); 
                                                         } 
                                                       }), LIST_OF_VALUES_DELIMITER),
                            screenedWell.getVendorIdentifier());
    
    List<Map<WellKey,ResultValue>> resultValueMaps = getResultValuesForDataHeaders(screenerCherryPick);
    int resultValueCol = 0;
    for (Map<WellKey,ResultValue> resultValues : resultValueMaps) {
      ResultValue resultValue = resultValues.get(screenedWell.getWellKey());
      Workbook2Utils.writeCell(sheet,
                               iRow,
                               6 + resultValueCol++,
                               resultValue == null ? "" : resultValue.getValue());
    }
  }

  private void writeLabCherryPicksHeaders(WritableSheet sheet) throws RowsExceededException, WriteException
  {
    Workbook2Utils.writeRow(sheet,
                            0,
                            "Cherry Pick Plate #",
                            "Cherry Pick Plate Well",
                            "Entrez Gene Symbol",
                            "Entrez Gene ID",
                            "Genbank Acc. No.",
                            "Gene Name",
                            "Sequence",
                            "Vendor ID");
  }

  private void writeScreenerCherryPicksHeaders(WritableSheet sheet, ScreenResult screenResult) throws RowsExceededException, WriteException
  {
    Workbook2Utils.writeRow(sheet,
                            0,
                            "Entrez Gene Symbol",
                            "Entrez Gene ID",
                            "Genbank Acc. No.",
                            "Gene Name",
                            "Sequences",
                            "Vendor IDs");
    int resultValueCol = 0;
    if (screenResult != null) {
      for (ResultValueType rvt : screenResult.getResultValueTypes()) {
        Workbook2Utils.writeCell(sheet,
                                 0,
                                 6 + resultValueCol++,
                                 rvt.getName());
      }
    }
  }
}

