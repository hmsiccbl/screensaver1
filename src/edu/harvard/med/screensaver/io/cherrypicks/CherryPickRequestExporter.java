// $HeadURL$
// $Id$

// Copyright © 2006, 2010 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.cherrypicks;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.workbook2.Workbook2Utils;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

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

  private static final String[][] SCREENER_CHERRY_PICK_HEADERS = new String[2][];
  {
    SCREENER_CHERRY_PICK_HEADERS[ScreenType.RNAI.ordinal()] = new String[] {
      "Entrez Gene Symbol",
      "Entrez Gene ID",
      "Genbank Acc. No.",
      "Gene Name",
      "Sequence",
      "Vendor Name",
      "Vendor ID"
    };
    SCREENER_CHERRY_PICK_HEADERS[ScreenType.SMALL_MOLECULE.ordinal()] = new String[] {
      "ICCB Number",
      "Vendor Name",
      "Vendor ID"
    };
  };
  private static final String[][] LAB_CHERRY_PICK_HEADERS = new String[2][];
  {
    LAB_CHERRY_PICK_HEADERS[ScreenType.RNAI.ordinal()] = new String[] { 
      "Cherry Pick Plate #",
      "Cherry Pick Plate Well",
      "Entrez Gene Symbol",
      "Entrez Gene ID",
      "Genbank Acc. No.",
      "Gene Name",
      "Sequence",
      "Vendor Name",
      "Vendor ID" 
    };
    LAB_CHERRY_PICK_HEADERS[ScreenType.SMALL_MOLECULE.ordinal()] = new String[] { 
      "Cherry Pick Plate #",
      "Cherry Pick Plate Well",
      "ICCB Number",
      "Vendor Name",
      "Vendor ID"
    }; 
  }

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
    Integer cherryPickRequestNumber = app.getCommandLineOptionValue("n", Integer.class);
    RNAiCherryPickRequest rnaiCherryPickRequest =
      exporter.getDao().findEntityByProperty(RNAiCherryPickRequest.class,
                                             "legacyCherryPickRequestNumber",
                                             cherryPickRequestNumber);
    if (rnaiCherryPickRequest == null) {
      rnaiCherryPickRequest =
        exporter.getDao().findEntityByProperty(RNAiCherryPickRequest.class,
                                               "cherryPickRequestId",
                                               cherryPickRequestNumber);
      if (rnaiCherryPickRequest == null) {
        throw new IllegalArgumentException("no such cherry pick request number " + cherryPickRequestNumber);
      }
    }
    Workbook workbook = exporter.exportCherryPickRequest(rnaiCherryPickRequest);
    File file = app.getCommandLineOptionValue("f", File.class);
    WritableWorkbook workbook2 = Workbook.createWorkbook(file, workbook);
    workbook2.write();
    workbook2.close();
    log.info("cherry pick request exported to " + file);
  }


  // instance data members

  private GenericEntityDAO _dao;


  // public constructors and methods

  public CherryPickRequestExporter(GenericEntityDAO dao)
  {
    this._dao = dao;
  }

  public GenericEntityDAO getDao()
  {
    return _dao;
  }

  @Transactional(readOnly=true)
  public Workbook exportCherryPickRequest(final CherryPickRequest cherryPickRequestIn)
  {
    
    try {
      CherryPickRequest cherryPickRequest =
        _dao.reloadEntity(cherryPickRequestIn,
                          true,
                          CherryPickRequest.screen.to(Screen.screenResult).to(ScreenResult.dataColumns).getPath());
      _dao.needReadOnly(cherryPickRequest,
                        CherryPickRequest.cherryPickAssayPlates.to(CherryPickAssayPlate.cherryPickLiquidTransfer).getPath());
      _dao.needReadOnly(cherryPickRequest,
                        CherryPickRequest.labCherryPicks.to(LabCherryPick.sourceWell).to(Well.latestReleasedReagent).getPath(),
                        CherryPickRequest.labCherryPicks.to(LabCherryPick.wellVolumeAdjustments).getPath());
      _dao.needReadOnly(cherryPickRequest,
                        CherryPickRequest.screenerCherryPicks.to(ScreenerCherryPick.screenedWell).to(Well.latestReleasedReagent).to(SilencingReagent.facilityGene).to(Gene.genbankAccessionNumbers).getPath());

      ByteArrayOutputStream rawBytes = new ByteArrayOutputStream();
      OutputStream out = new BufferedOutputStream(rawBytes);
      WritableWorkbook workbook = Workbook.createWorkbook(out);

      writeCherryPicks(workbook, cherryPickRequest);
      workbook.write();
      workbook.close();
      return Workbook.getWorkbook(new ByteArrayInputStream(rawBytes.toByteArray()));
    }
    catch (Exception e) {
      throw new DAOTransactionRollbackException(e);
    }
  }


  // private methods

  /**
   * @motivation for CGLIB2
   */
  protected CherryPickRequestExporter()
  {
  }

  private void writeCherryPicks(WritableWorkbook workbook, CherryPickRequest cherryPickRequest) throws RowsExceededException, WriteException
  {
    writeScreenerCherryPicks(workbook, cherryPickRequest);
    writeLabCherryPicks(workbook, cherryPickRequest);
  }

  private void writeLabCherryPicks(WritableWorkbook workbook, CherryPickRequest cherryPickRequest) throws RowsExceededException, WriteException
  {
    WritableSheet sheet = workbook.createSheet("Lab Cherry Picks", LAB_CHERRY_PICKS_SHEET_INDEX);
    writeLabCherryPicksHeaders(sheet, cherryPickRequest.getScreen().getScreenType());

    int iRow = 0;
    for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
      if (!labCherryPick.isFailed()) {
        writeLabCherryPick(labCherryPick, sheet, ++iRow);
      }
    }
  }

  private void writeLabCherryPick(LabCherryPick labCherryPick, WritableSheet sheet, int iRow) throws RowsExceededException, WriteException
  {
    Workbook2Utils.writeRow(sheet,
                            iRow,
                            getLabCherryPickData(labCherryPick));
  }

  private Object[] getLabCherryPickData(LabCherryPick labCherryPick)
  {
    List<Object> data = new ArrayList<Object>();
    Well sourceWell = labCherryPick.getSourceWell();
    data.add(labCherryPick.getAssayPlate() == null ? null : labCherryPick.getAssayPlate().getPlateOrdinal() + 1);
    data.add(labCherryPick.getAssayPlateWellName() == null ? null : labCherryPick.getAssayPlateWellName());
    getReagentData(labCherryPick.getCherryPickRequest().getScreen().getScreenType(),
                   sourceWell, 
                   data);
    return data.toArray();
  }

  private void writeScreenerCherryPicks(WritableWorkbook workbook, CherryPickRequest cherryPickRequest) throws RowsExceededException, WriteException
  {
    WritableSheet sheet = workbook.createSheet("Screener Cherry Picks", SCREENER_CHERRY_PICKS_SHEET_INDEX);
    writeScreenerCherryPicksHeaders(sheet, cherryPickRequest.getScreen());

    int iRow = 0;
    for (ScreenerCherryPick screenerCherryPick : cherryPickRequest.getScreenerCherryPicks()) {
      writeScreenerCherryPick(screenerCherryPick, sheet, ++iRow);
    }
  }

  private void writeScreenerCherryPick(ScreenerCherryPick screenerCherryPick, WritableSheet sheet, int iRow) throws RowsExceededException, WriteException
  {
    Workbook2Utils.writeRow(sheet,
                            iRow,
                            getScreenerCherryPickData(screenerCherryPick));
  }

  private Object[] getScreenerCherryPickData(ScreenerCherryPick screenerCherryPick)
  {
    List<Object> data = new ArrayList<Object>();
    Well screenedWell = screenerCherryPick.getScreenedWell();
    getReagentData(screenerCherryPick.getCherryPickRequest().getScreen().getScreenType(),
                   screenedWell,
                   data);

    ScreenResult screenResult = screenerCherryPick.getCherryPickRequest().getScreen().getScreenResult();
    if (screenResult != null) {
      Set<DataColumn> cols = screenResult.getDataColumns();
      for (DataColumn col : cols) {
        Object value = null;
        ResultValue rv = screenedWell.getResultValues().get(col);
        if (rv != null) {
          value = rv.getTypedValue();
        }
        data.add(value);
      }
    }
    return data.toArray();
  }

  private void getReagentData(ScreenType screenType,
                              Well well,
                              List<Object> data)
  {
    if (screenType == ScreenType.RNAI) {
      SilencingReagent reagent = (SilencingReagent) well.<Reagent>getLatestReleasedReagent();
      Gene gene = reagent == null ? null : reagent.getFacilityGene();
      data.add(gene == null || gene.getEntrezgeneSymbols().isEmpty()  ? null : gene.getEntrezgeneSymbols().iterator().next());
      data.add(gene == null ? null : gene.getEntrezgeneId());
      data.add(gene == null ? null : StringUtils.makeListString(gene.getGenbankAccessionNumbers(), LIST_OF_VALUES_DELIMITER));
      data.add(gene == null ? null : gene.getGeneName());
      data.add(reagent == null ? null : reagent.getSequence());
    }
    else {
      data.add(well.getFacilityId());
    }
    Reagent reagent = well.<Reagent>getLatestReleasedReagent(); 
    data.add(reagent == null ? null : reagent.getVendorId().getVendorName());
    data.add(reagent == null ? null : reagent.getVendorId().getVendorIdentifier());
  }

  private void writeLabCherryPicksHeaders(WritableSheet sheet, ScreenType screenType) throws RowsExceededException, WriteException
  {
    Workbook2Utils.writeRow(sheet,
                            0,
                            (Object[]) LAB_CHERRY_PICK_HEADERS[screenType.ordinal()]);
  }

  private void writeScreenerCherryPicksHeaders(WritableSheet sheet, Screen screen) throws RowsExceededException, WriteException
  {
    String[] headers = SCREENER_CHERRY_PICK_HEADERS[screen.getScreenType().ordinal()];
    Workbook2Utils.writeRow(sheet,
                            0,
                            (Object[]) headers);
    int resultValueCol = 0;
    if (screen.getScreenResult() != null) {
      for (DataColumn col : screen.getScreenResult().getDataColumns()) {
        Workbook2Utils.writeCell(sheet,
                                 0,
                                 headers.length + resultValueCol++,
                                 col.getName());
      }
    }
  }
}

