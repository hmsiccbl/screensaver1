package edu.harvard.med.screensaver.ui.libraries;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverProperties;
import edu.harvard.med.screensaver.db.Criterion;
import edu.harvard.med.screensaver.db.Criterion.Operator;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.activities.ActivitySearchResults;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.VolumeColumn;

public class CopyMinimumVolumeReportSearchResults extends LibraryCopySearchResults
{
  private static final Logger log = Logger.getLogger(CopyMinimumVolumeReportSearchResults.class);

  public CopyMinimumVolumeReportSearchResults()
  {
    super();
  }

  public CopyMinimumVolumeReportSearchResults(GenericEntityDAO dao,
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

    Volume vThreshold = Volume.makeVolume("7", VolumeUnit.MICROLITERS);
    VolumeColumn<Copy> column = (VolumeColumn<Copy>) getColumnManager().getColumn("Min Plate Remaining Volume");
    column.addCriterion(new Criterion<Volume>(Operator.LESS_THAN, vThreshold));
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
      
    // For #156, omit cherry pick copy plates
    TableColumn<Copy,CopyUsageType> column5 = (TableColumn<Copy,CopyUsageType>) getColumnManager().getColumn("Usage Type");
    column5.addCriterion(new Criterion<CopyUsageType>(Operator.NOT_EQUAL, CopyUsageType.CHERRY_PICK_SOURCE_PLATES));
    column5.setVisible(true);
    
    setTitle("Copy Minimum Volume Remaining Report");
  }
  
  @Override
  public void initialize()
  {
    // TODO Auto-generated method stub
    super.initialize();
  }
}
