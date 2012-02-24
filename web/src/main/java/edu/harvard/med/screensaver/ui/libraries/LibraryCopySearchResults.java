// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.Criterion.Operator;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.ConcentrationStatistics;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.VolumeStatistics;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.activities.ActivitySearchResults;
import edu.harvard.med.screensaver.ui.arch.datatable.column.DateColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.FixedDecimalColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.IntegerColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.VolumeColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.EnumEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.FixedDecimalEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;
import edu.harvard.med.screensaver.ui.arch.searchresults.SearchResults;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * A {@link SearchResults} for {@link Copy Copies}.
* 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibraryCopySearchResults extends EntityBasedEntitySearchResults<Copy,Integer>
{
  private static final Logger log = Logger.getLogger(LibraryCopySearchResults.class);

  protected static final int DECIMAL_SCALE = 1; // number of decimal digits (to the right of decimal point) for plate screening count average

  private static final String COLUMN_GROUP_CONCENTRATION = "Concentration";

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private LibraryCopyViewer _libraryCopyViewer;
  private LibraryViewer _libraryViewer;
  private ActivitySearchResults _activitesBrowser;
  private LibraryCopyPlateSearchResults _libraryCopyPlateSearchResults;

  /**
   * @motivation for CGLIB2
   */
  protected LibraryCopySearchResults()
  {
  }

  public LibraryCopySearchResults(GenericEntityDAO dao,
                                  LibrariesDAO librariesDao,
                                  LibraryCopyViewer libraryCopyViewer,
                                  LibraryViewer libraryViewer,
                                  ActivitySearchResults activitiesBrowser,
                                  LibraryCopyPlateSearchResults libraryCopyPlateSearchResults)
  {
    super(libraryCopyViewer);
    _dao = dao;
    _librariesDao = librariesDao;
    _libraryCopyViewer = libraryCopyViewer;
    _libraryViewer = libraryViewer;
    _activitesBrowser = activitiesBrowser;
    _libraryCopyPlateSearchResults = libraryCopyPlateSearchResults;
  }

  /**
   * For command line usage only
   */
  public LibraryCopySearchResults(GenericEntityDAO dao,
                                  LibrariesDAO librariesDao)
  {
    super(null);
    _dao = dao;
    _librariesDao = librariesDao;
  }

  @Override
  public void searchAll()
  {
    setTitle("Library Copies");
    initialize(new EntityDataFetcher<Copy,Integer>(Copy.class, _dao));
  }

  public void searchCopiesByLibrary(final Library library)
  {
    setTitle("Library Copies for library " + library.getLibraryName());
    EntityDataFetcher<Copy,Integer> copyDataFetcher =
      new EntityDataFetcher<Copy,Integer>(Copy.class, _dao) {
        @Override
        public void addDomainRestrictions(HqlBuilder hql)
        {
          hql.where(getRootAlias(), Copy.library.getLeaf(), Operator.EQUAL, library);
        }
      };
    initialize(copyDataFetcher);
  }

  private void initialize(EntityDataFetcher<Copy,Integer> copyDataFetcher)
  {
    initialize(new InMemoryEntityDataModel<Copy,Integer,Copy>(copyDataFetcher) {
      @Override
      public void fetch(List<? extends TableColumn<Copy,?>> columns)
      {
        super.fetch(columns);
        _librariesDao.calculateCopyScreeningStatistics(_unfilteredData);
        _librariesDao.calculateCopyVolumeStatistics(_unfilteredData);
      }
    });
  }

  @Override
  protected List<TableColumn<Copy,?>> buildColumns()
  {
    List<TableColumn<Copy,?>> columns = Lists.newArrayList();

    columns.add(new TextEntityColumn<Copy>(Copy.library,
                                           "Library",
                                           "The library that this copy represents",
                                           TableColumn.UNGROUPED) {

      @Override
      public String getCellValue(Copy copy)
      {
        return copy.getLibrary().getLibraryName();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(Copy copy)
      {
        return _libraryViewer.viewEntity(copy.getLibrary());
      }
    });
    Iterables.getLast(columns).setVisible(!isNested());
    
    columns.add(new EnumEntityColumn<Copy,ScreenType>(RelationshipPath.from(Copy.class).toProperty("library"),
                                                      "Screen Type", "'RNAi' or 'Small Molecule'",
                                                      TableColumn.UNGROUPED, ScreenType.values()) {
      @Override
      public ScreenType getCellValue(Copy copy)
      {
        return copy.getLibrary().getScreenType();
      }
    });    
    Iterables.getLast(columns).setVisible(false);
    
    String names = Joiner.on("','").join(StringUtils.getVocabularyTerms(LibraryType.values()));
    columns.add(new EnumEntityColumn<Copy,LibraryType>(RelationshipPath.from(Copy.class).toProperty("library"),
                                                                  "Library Type", names,
                                                                  TableColumn.UNGROUPED, LibraryType.values()) {
      @Override
      public LibraryType getCellValue(Copy copy)
      {
        return copy.getLibrary().getLibraryType();
      }
    });
    Iterables.getLast(columns).setVisible(false);

    columns.add(new TextEntityColumn<Copy>(PropertyPath.from(Copy.class).toProperty("name"),
                                           "Copy Name",
                                           "The name of the the copy",
                                           TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Copy copy)
      {
        return copy.getName();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(Copy copy)
      {
        if (isNested()) {
          LibraryCopySearchResults libraryCopySearchResults = (LibraryCopySearchResults) _libraryCopyViewer.getContextualSearchResults();
          libraryCopySearchResults.searchCopiesByLibrary(copy.getLibrary());
          libraryCopySearchResults.findEntity(copy);
          return BROWSE_LIBRARY_COPIES;
        }
        return viewSelectedEntity();
      }
    });
    ((TextEntityColumn<Copy>) Iterables.getLast(columns)).addRelationshipPath(Copy.library);

    columns.add(new EnumEntityColumn<Copy,CopyUsageType>(PropertyPath.from(Copy.class).toProperty("usageType"),
                                                         "Usage Type",
                                                         "The usage type of the copy",
                                                         TableColumn.UNGROUPED,
                                                         CopyUsageType.values()) {
      @Override
      public CopyUsageType getCellValue(Copy copy)
      {
        return copy.getUsageType();
      }
    });

    columns.add(new TextEntityColumn<Copy>(PropertyPath.from(Copy.class).toProperty("primaryPlateLocation"),
                                           "Primary Plate Location",
                                           "The most common plate location for plates of this copy",
                                           TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Copy copy)
      {
        if (copy.getPrimaryPlateLocation() == null) {
          return null;
        }
        return copy.getPrimaryPlateLocation().toDisplayString();
      }
    });

    columns.add(new IntegerEntityColumn<Copy>(PropertyPath.from(Copy.class).toProperty("plateLocationsCount"),
                                              "Plate Locations",
                                              "The number different plate locations found for plates of this copy",
                                              TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getPlateLocationsCount();
      }
    });

    columns.add(new EnumEntityColumn<Copy,PlateStatus>(PropertyPath.from(Copy.class).toProperty("primaryPlateStatus"),
                                                       "Primary Plate Status",
                                                       "The most common plate status for plates of this copy",
                                                       TableColumn.UNGROUPED,
                                                       PlateStatus.values()) {
      @Override
      public PlateStatus getCellValue(Copy copy)
      {
        return copy.getPrimaryPlateStatus();
      }
    });
    
    buildConcentrationColumns(columns);
    
    columns.add(new IntegerEntityColumn<Copy>(PropertyPath.from(Copy.class).toProperty("platesAvailable"),
                                              "Plates Available",
                                              "The number of plates with a status of \"Available\"",
                                              TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getPlatesAvailable();
      }
    });

    columns.add(new FixedDecimalColumn<Copy>("Plate Screening Count Average",
                                             "The average number of times this copy's plates have been screened, ignoring replicates",
                                             TableColumn.UNGROUPED) {
      @Override
      public BigDecimal getCellValue(Copy copy)
      {
        if (copy.getScreeningStatistics().getPlateCount() == 0) return null;
        if (copy.getScreeningStatistics().getPlateScreeningCount() == 0) return BigDecimal.ZERO;
        BigDecimal val = new BigDecimal(copy.getScreeningStatistics().getPlateScreeningCount()).divide(new BigDecimal(copy.getScreeningStatistics().getPlateCount()),
                                                                                                       DECIMAL_SCALE,
                                                                                                       RoundingMode.CEILING);
        return val;
      }
    });

    columns.add(new VolumeColumn<Copy>("Average Plate Remaining Volume",
                                       "The average well volume remaining across all library screening plates of this copy",
                                       TableColumn.UNGROUPED) {
      @Override
      public Volume getCellValue(Copy copy)
      {
        return getNullSafeVolumeStatistics(copy).getAverageRemaining();
      }
    });
    columns.add(new VolumeColumn<Copy>("Min Plate Remaining Volume",
                                       "The minimum well volume remaining across all library screening plates of this copy",
                                       TableColumn.UNGROUPED) {
      @Override
      public Volume getCellValue(Copy copy)
    {
      return getNullSafeVolumeStatistics(copy).getMinRemaining();
    }
    });
    columns.add(new VolumeColumn<Copy>("Max Plate Remaining Volume",
                                       "The maximum well volume remaining across all library screening plates of this copy",
                                       TableColumn.UNGROUPED) {
      @Override
      public Volume getCellValue(Copy copy)
    {
      return getNullSafeVolumeStatistics(copy).getMaxRemaining();
    }
    });

    columns.add(new DateColumn<Copy>("Last Date Screened",
                                     "The date the copy was last screened",
                                     TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Copy copy)
    {
      return copy.getScreeningStatistics().getLastDateScreened();
    }
    });

    columns.add(new DateColumn<Copy>("First Date Screened",
                                     "The date the copy was first screened",
                                     TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Copy copy)
      {
        return copy.getScreeningStatistics().getFirstDateScreened();
      }
    });

    columns.add(new DateEntityColumn<Copy>(PropertyPath.from(Copy.class).toProperty("datePlated"),
                                           "Date Plated",
                                           "The earliest date on which a plate of this copy was created",
                                           TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Copy copy)
      {
        return copy.getDatePlated();
      }
    });

    columns.add(new IntegerEntityColumn<Copy>(Copy.library.toProperty("startPlate"),
                                              "Start Plate",
                                              "The first plate number of the library",
                                              TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getLibrary().getStartPlate();
      }
    });
    Iterables.getLast(columns).setVisible(!isNested());

    columns.add(new IntegerEntityColumn<Copy>(Copy.library.toProperty("startPlate"),
                                              "End Plate",
                                              "The last plate number of the library",
                                              TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getLibrary().getEndPlate();
      }
    });
    Iterables.getLast(columns).setVisible(!isNested());

    columns.add(new IntegerEntityColumn<Copy>(Copy.library,
                                              "Library Plate Count",
                                              "The number of plates in the library",
                                              TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getLibrary().getEndPlate() - copy.getLibrary().getStartPlate() + 1;
      }
    });
    Iterables.getLast(columns).setVisible(false);

    columns.add(new IntegerColumn<Copy>("Copy Plate Count",
                                        "The number of plates in this copy",
                                        TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getScreeningStatistics().getPlateCount();
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }

      @Override
      public Object cellAction(Copy copy)
      {
        _libraryCopyPlateSearchResults.searchPlatesForCopy(copy);
        return BROWSE_LIBRARY_COPY_PLATES;
      }
    });

    columns.add(new IntegerColumn<Copy>("Assay Plate Count",
                                        "The number of assay plates screened for this copy",
                                        TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getScreeningStatistics().getAssayPlateCount();
      }
    });

    columns.add(new IntegerColumn<Copy>("Screening Count",
                                        "The total number of times this copy has been screened, ignoring replicates",
                                        TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getScreeningStatistics().getScreeningCount();
      }
      @Override
      public boolean isCommandLink()
      {
        return true;
      }
      @Override
      public Object cellAction(Copy copy)
      {
        _activitesBrowser.searchLibraryScreeningActivitiesForCopy(copy);
        return BROWSE_ACTIVITIES;
      }
    });

    columns.add(new IntegerColumn<Copy>("Plate Screening Count",
                                        "The total number of times individual plates from this copy have been screened, ignoring replicates",
                                        TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getScreeningStatistics().getPlateScreeningCount();
      }
    });
    Iterables.getLast(columns).setVisible(false);

    columns.add(new IntegerColumn<Copy>("Data Loading Count",
                                        "The number of screen results loaded for screens of this copy",
                                        TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Copy copy)
      {
        return copy.getScreeningStatistics().getDataLoadingCount();
      }
    });

    columns.add(new DateColumn<Copy>("First Date Data Loaded",
                                     "The date of the first screen result data loading activity",
                                     TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Copy copy)
      {
        return copy.getScreeningStatistics().getFirstDateDataLoaded();
      }
    });
    Iterables.getLast(columns).setVisible(false);

    columns.add(new DateColumn<Copy>("Last Date Data Loaded",
                                     "The date of the last screen result data loading activity",
                                     TableColumn.UNGROUPED) {
      @Override
      public LocalDate getDate(Copy copy)
      {
        return copy.getScreeningStatistics().getLastDateDataLoaded();
      }
    });
    Iterables.getLast(columns).setVisible(false);

    return columns;
  }

  private void buildConcentrationColumns(List<TableColumn<Copy,?>> columns)
  {
    // Concentration Columns

    // molar values
    
    columns.add(new TextEntityColumn<Copy>(PropertyPath.from(Copy.class).toProperty("minMolarConcentration"),
                                                          "Minimum Well Molar Concentration",
                                                          "The minimum molar concentration (diluted) of the wells of the copy's plates",
                                                          COLUMN_GROUP_CONCENTRATION) {
      @Override
      public String getCellValue(Copy copy)
      {
        MolarConcentration value = copy.getNullSafeConcentrationStatistics().getDilutedMinMolarConcentration(copy.getWellConcentrationDilutionFactor());
        return value == null ? "n/a": value.toString();
      }
    });
    Iterables.getLast(columns).setVisible(true);

    columns.add(new TextEntityColumn<Copy>(PropertyPath.from(Copy.class).toProperty("maxMolarConcentration"),
                                                          "Maximum Well Molar Concentration",
                                                          "The maximum molar concentration (diluted) of the wells of the copy's plates",
                                                          COLUMN_GROUP_CONCENTRATION) {
      @Override
      public String getCellValue(Copy copy)
      {
        MolarConcentration value = copy.getNullSafeConcentrationStatistics().getDilutedMaxMolarConcentration(copy.getWellConcentrationDilutionFactor());
        return value == null ? "n/a": value.toString();
      }
    });
    Iterables.getLast(columns).setVisible(true);
    
    columns.add(new TextEntityColumn<Copy>(PropertyPath.from(Copy.class).toProperty("primaryWellMolarConcentration"),
                                                          "Primary Well Molar Concentration",
                                                          "The value of the most frequent plate well molar concentration (diluted)",
                                                          COLUMN_GROUP_CONCENTRATION) {
      @Override
      public String getCellValue(Copy copy)
      {
        MolarConcentration value = copy.getNullSafeConcentrationStatistics().getDilutedPrimaryWellMolarConcentration(copy.getWellConcentrationDilutionFactor());
        return value == null ? "n/a": value.toString();
      }
    });
    Iterables.getLast(columns).setVisible(false);

     // mg/mL
    
    columns.add(new TextEntityColumn<Copy>(PropertyPath.from(Copy.class).toProperty("minMgMlConcentration"),
                                                    "Minimum Well Concentration (mg/mL)",
                                                    "The minimum mg/mL concentration (diluted) of the wells of the copy's plates",
                                                    COLUMN_GROUP_CONCENTRATION) {
      @Override
      public String getCellValue(Copy copy)
      {
        BigDecimal value = copy.getNullSafeConcentrationStatistics().getDilutedMinMgMlConcentration(copy.getWellConcentrationDilutionFactor());
        return value == null ? "n/a" : value.toString();        
      }
    });
    Iterables.getLast(columns).setVisible(true);
    
    columns.add(new TextEntityColumn<Copy>(PropertyPath.from(Copy.class).toProperty("maxMgMlConcentration"),
                                                    "Maximum Well Concentration (mg/mL)",
                                                    "The minimum mg/mL concentration (diluted) of the wells of the copy's plates",
                                                    COLUMN_GROUP_CONCENTRATION) {
      @Override
      public String getCellValue(Copy copy)
      {
        BigDecimal value = copy.getNullSafeConcentrationStatistics().getDilutedMaxMgMlConcentration(copy.getWellConcentrationDilutionFactor());
        return value == null ? "n/a" : value.toString();
      }
    });
    Iterables.getLast(columns).setVisible(true);    
    
    columns.add(new TextEntityColumn<Copy>(PropertyPath.from(Copy.class).toProperty("primaryWellMgMlConcentration"),
                                                    "Primary Well Concentration (mg/mL)",
                                                    "The value of the most frequent plate well mg/mL concentration (diluted)",
                                                    COLUMN_GROUP_CONCENTRATION) {
      @Override
      public String getCellValue(Copy copy)
      {
        BigDecimal value = copy.getNullSafeConcentrationStatistics().getDilutedPrimaryWellMgMlConcentration(copy.getWellConcentrationDilutionFactor());
        return value == null ? "n/a" : value.toString();
      }
    });
    Iterables.getLast(columns).setVisible(false);
    
    columns.add(new FixedDecimalEntityColumn<Copy>(PropertyPath.from(Copy.class).toProperty("wellConcentrationDilutionFactor"),
                                                    "Primary Well Concentration Dilution Factor",
                                                    "The most often ocurring factor by which the original library well concentration is diluted for this copy's plates",
                                                    COLUMN_GROUP_CONCENTRATION) {
      @Override
      public BigDecimal getCellValue(Copy copy)
      {
        return copy.getWellConcentrationDilutionFactor();
      }
    });
    Iterables.getLast(columns).setVisible(true);
  }
  
  private static VolumeStatistics getNullSafeVolumeStatistics(Copy copy)
  {
    VolumeStatistics volumeStatistics = copy.getVolumeStatistics();
    if (volumeStatistics == null) {
      return VolumeStatistics.Null;
    }
    return volumeStatistics;
  }  

  @Override
  protected Copy rowToEntity(Copy row)
  {
    return row;
  }
}
