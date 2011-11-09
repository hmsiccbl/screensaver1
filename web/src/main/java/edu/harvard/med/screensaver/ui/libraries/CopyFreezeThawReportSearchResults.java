package edu.harvard.med.screensaver.ui.libraries;

import java.math.BigDecimal;

import javax.faces.component.UIData;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverProperties;
import edu.harvard.med.screensaver.db.Criterion;
import edu.harvard.med.screensaver.db.Criterion.Operator;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.activities.ActivitySearchResults;
import edu.harvard.med.screensaver.ui.arch.datatable.column.FixedDecimalColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;

public class CopyFreezeThawReportSearchResults extends LibraryCopySearchResults
{
  private static final Logger log = Logger.getLogger(CopyFreezeThawReportSearchResults.class);

  public CopyFreezeThawReportSearchResults()
  {
    super();
  }

  public CopyFreezeThawReportSearchResults(GenericEntityDAO dao,
                                  LibrariesDAO librariesDao,
                                  LibraryCopyViewer libraryCopyViewer,
                                  LibraryViewer libraryViewer,
                                  ActivitySearchResults activitiesBrowser,
                                  LibraryCopyPlateSearchResults libraryCopyPlateSearchResults,
                                  ScreensaverProperties ssProps)
  {
    super(dao, librariesDao, libraryCopyViewer, libraryViewer, activitiesBrowser, libraryCopyPlateSearchResults);
    initialize();
    setApplicationProperties(ssProps);
    searchAll();
    
    // Note: adding a visible criterion seems to be necessary to trigger proper setup for the sort, to avoid
    // java.lang.NullPointerException -  at org.apache.myfaces.custom.sortheader.HtmlSortHeaderRenderer.encodeEnd(HtmlSortHeaderRenderer.java:68)

    BigDecimal bThreshold = new BigDecimal(12);
    FixedDecimalColumn<Copy> column = (FixedDecimalColumn<Copy>) getColumnManager().getColumn("Plate Screening Count Average");
    column.addCriterion(new Criterion<BigDecimal>(Operator.GREATER_THAN_EQUAL, bThreshold));
    column.setVisible(true);    
      
    TableColumn<Copy,ScreenType> column2 = (TableColumn<Copy,ScreenType>) getColumnManager().getColumn("Screen Type");
    column2.addCriterion(new Criterion<ScreenType>(Operator.EQUAL, ScreenType.SMALL_MOLECULE));
    column2.setVisible(true);

    //      TableColumn<Copy,PlateStatus> column3 = (TableColumn<Copy,PlateStatus>) lcsr.getColumnManager().getColumn("Primary Plate Status");
    //      column3.addCriterion(new Criterion<PlateStatus>(Operator.LESS_THAN, PlateStatus.RETIRED));
    //      column3.setVisible(true);

    TableColumn<Copy,Integer> column3a = (TableColumn<Copy,Integer>) getColumnManager().getColumn("Plates Available");
    column3a.addCriterion(new Criterion<Integer>(Operator.GREATER_THAN, 0));
    column3a.setVisible(true);

    //TODO: katrina has asked about filtering a list of old libraries out; and to filter out "DOS" Library Types
    TableColumn<Copy,LibraryType> column4 = (TableColumn<Copy,LibraryType>) getColumnManager().getColumn("Library Type");
    column4.addCriterion(new Criterion<LibraryType>(Operator.NOT_EQUAL, LibraryType.DOS));
    column4.setVisible(true);
      
    setTitle("Copy Freeze Thaw Report");
  }
  
  @Override
  public void initialize()
  {
    // TODO Auto-generated method stub
    super.initialize();
  }
}
