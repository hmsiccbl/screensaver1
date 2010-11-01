// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.AggregateDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellCopy;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellVolume;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.searchresults.EntityBasedEntitySearchResults;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.column.BooleanColumn;
import edu.harvard.med.screensaver.ui.table.column.IntegerColumn;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.TextColumn;
import edu.harvard.med.screensaver.ui.table.column.VolumeColumn;
import edu.harvard.med.screensaver.ui.table.model.InMemoryDataModel;

public class WellCopyVolumeSearchResults extends EntityBasedEntitySearchResults<WellCopy,String>
{
  private static Logger log = Logger.getLogger(WellCopyVolumeSearchResults.class);


  private GenericEntityDAO _dao;
  private LibraryViewer _libraryViewer;
  private WellViewer _wellViewer;
  private WellVolumeSearchResults _wellVolumeSearchResults;

  private Map<WellCopy,Volume> _newRemainingVolumes = new HashMap<WellCopy,Volume>();
  private String _wellVolumeAdjustmentActivityComments;
  private TableColumn<WellCopy,?> _newRemainingVolumeColumn;
  private TableColumn<WellCopy,?> _withdrawalsAdjustmentsColumn;


  /**
   * @motivation for CGLIB2
   */
  protected WellCopyVolumeSearchResults()
  {
  }

  public WellCopyVolumeSearchResults(GenericEntityDAO dao,
                                     LibraryViewer libraryViewer,
                                     WellViewer wellViewer,
                                     WellVolumeSearchResults wellVolumeSearchResults)
  {
    _dao = dao;
    _wellVolumeSearchResults = wellVolumeSearchResults;
    _libraryViewer = libraryViewer;
    _wellViewer = wellViewer;
    setEditingRole(ScreensaverUserRole.LIBRARIES_ADMIN);
  }

  public void searchWells(final Set<WellKey> wellKeys)
  {
    setTitle("Well Copy Volumes Search Result");
    _wellVolumeSearchResults.setTitle("Well Volumes Search Result");
    EntityDataFetcher<WellVolumeAdjustment,Integer> wvaFetcher =
      new EntityDataFetcher<WellVolumeAdjustment,Integer>(WellVolumeAdjustment.class, _dao) {
      @Override
        public void addDomainRestrictions(HqlBuilder hql)
      {
        Set<String> wellKeyStrings = new HashSet<String>();
        for (WellKey wellKey : wellKeys) {
          wellKeyStrings.add(wellKey.toString());
        }
        hql.whereIn(getRootAlias(), "well.id", wellKeyStrings);
      }
    };
    addRelationshipsToFetch(wvaFetcher);

    EntityDataFetcher<WellCopy,String> wellCopyDataFetcher =
      new AggregateDataFetcher<WellCopy,String,WellVolumeAdjustment,Integer>(WellCopy.class, _dao, wvaFetcher) {
      @Override
      protected SortedSet<WellCopy> aggregateData(List<WellVolumeAdjustment> nonAggregatedData)
      {
        SortedSet<WellCopy> result = new TreeSet<WellCopy>();
        for (WellKey wellKey : wellKeys) {
          Well well = _dao.findEntityById(Well.class, wellKey.toString(), true, Well.library.to(Library.copies).to(Copy.plates).getPath());
          makeWellCopyVolumes(well, result);
        }
        return aggregateWellVolumeAdjustments(result, nonAggregatedData);
      }
    };
    doInitialize(wellCopyDataFetcher);
  }

  public void searchWellsForLibrary(final Library library)
  {
    setTitle("Well Copy Volumes for library " + library.getLibraryName());
    EntityDataFetcher<WellVolumeAdjustment,Integer> wvaFetcher =
      new EntityDataFetcher<WellVolumeAdjustment,Integer>(WellVolumeAdjustment.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, WellVolumeAdjustment.well.to(Well.library), library, getRootAlias());
      }
      };
    addRelationshipsToFetch(wvaFetcher);

    EntityDataFetcher<WellCopy,String> wellCopyDataFetcher =
      new AggregateDataFetcher<WellCopy,String,WellVolumeAdjustment,Integer>(WellCopy.class, _dao, wvaFetcher) {
      @Override
      @Transactional
      protected SortedSet<WellCopy> aggregateData(List<WellVolumeAdjustment> nonAggregatedData)
      {
        // reload library and eager fetch some relationships, allowing makeWellCopyVolumes() to work
        final Library[] library2 = new Library[1];
        _dao.doInTransaction(new DAOTransaction() {
          public void runTransaction() {
            library2[0] = _dao.reloadEntity(library, true, "wells");
            _dao.needReadOnly(library2[0], Library.copies.to(Copy.plates).getPath());
          }
        });
        return aggregateWellVolumeAdjustments(makeWellCopyVolumes(library2[0], new TreeSet<WellCopy>()),
                                              nonAggregatedData);
      }
      };
    doInitialize(wellCopyDataFetcher);
  }

  private void doInitialize(EntityDataFetcher<WellCopy,String> wellCopyDataFetcher)
  {
    initialize(new InMemoryDataModel<WellCopy>(wellCopyDataFetcher));
    EntityDataFetcher<WellVolume,String> wellVolumeDataFetcher =
      new AggregateDataFetcher<WellVolume,String,WellCopy,String>(WellVolume.class, _dao, wellCopyDataFetcher) {
        @Override
        protected SortedSet<WellVolume> aggregateData(List<WellCopy> nonAggregatedData)
      {
        return aggregateWellCopies(nonAggregatedData);
      }
      };
    _wellVolumeSearchResults.initialize(new InMemoryDataModel<WellVolume>(wellVolumeDataFetcher));
  }

  public void searchWellsForCherryPickRequest(CherryPickRequest cherryPickRequest,
                                              boolean forUnfulfilledOnly)
  {
    // note: it would be nicer to select the WVAs by their parent CPR, but we
    // can't do this since Well.cherryPickRequests relationship does not exist in
    // our model; instead we just find all the wells for CPR and delegate to the
    // searchWells() method
    Set<WellKey> wellKeys = new HashSet<WellKey>();
    cherryPickRequest = _dao.reloadEntity(cherryPickRequest, true, CherryPickRequest.labCherryPicks.to(LabCherryPick.sourceWell).getPath());
    for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
      if (!forUnfulfilledOnly || labCherryPick.isUnfulfilled()) {
        wellKeys.add(labCherryPick.getSourceWell().getWellKey());
      }
    }
    searchWells(wellKeys);
    setTitle("Well Copy Volumes for cherry pick request " + cherryPickRequest.getCherryPickRequestNumber() + " lab cherry picks");
    _wellVolumeSearchResults.setTitle("Well Volumes for cherry pick request " + cherryPickRequest.getCherryPickRequestNumber() +
      " lab cherry picks");
  }

  @SuppressWarnings("unchecked")
  @Override
  protected List<? extends TableColumn<WellCopy,?>> buildColumns()
  {
    ArrayList<TableColumn<WellCopy,?>> columns = new ArrayList<TableColumn<WellCopy,?>>();
    columns.add(new TextColumn<WellCopy>(
      "Library", "The library containing the well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(WellCopy wellCopy) { return wellCopy.getWell().getLibrary().getLibraryName(); }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(WellCopy wellCopy) { return _libraryViewer.viewEntity(wellCopy.getWell().getLibrary()); }
    });
    columns.add(new IntegerColumn<WellCopy>(
      "Plate", "The number of the plate the well is located on", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(WellCopy wellCopy) { return wellCopy.getWell().getPlateNumber(); }
    });
    columns.add(new TextColumn<WellCopy>(
      "Well", "The plate coordinates of the well", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(WellCopy wellCopy) { return wellCopy.getWell().getWellName(); }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(WellCopy wellCopy) { return _wellViewer.viewEntity(wellCopy.getWell()); }
    });
    columns.add(new TextColumn<WellCopy>(
      "Copy", "The name of the library plate copy", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(WellCopy wellCopy) { return wellCopy.getCopy().getName(); }

      // TODO
//    @Override
//    public boolean isCommandLink() { return true; }

//    @Override
//    public Object cellAction(WellCopyVolume wellCopy) { return _libraryViewer.viewLibraryCopyVolumes(wellVolume.getWell(), WellCopyVolumeSearchResults.this); }
    });
    
    TableColumn<WellCopy,Boolean> col =
      new BooleanColumn<WellCopy>("Is Retired", "Has this copy been retired?", TableColumn.UNGROUPED) 
      {
        @Override
        public Boolean getCellValue(WellCopy wc) 
        {
          Plate ci = wc.getCopy().getPlates().get(wc.getWell().getPlateNumber());
          if(ci != null && ci.isRetired())
          {
            return true;
          }else {
            return false;
          }
        }
      };
    col.clearCriteria();
    col.addCriterion(new Criterion<Boolean>(Criterion.Operator.EQUAL, Boolean.FALSE));
    columns.add(col);
      
    columns.add(new VolumeColumn<WellCopy>(
      "Initial Volume", "The initial volume of this well copy", TableColumn.UNGROUPED) {
      @Override
      public Volume getCellValue(WellCopy wellCopy) { return wellCopy.getInitialVolume(); }
    });
    columns.add(new VolumeColumn<WellCopy>(
      "Consumed Volume", "The volume already used from this well copy", TableColumn.UNGROUPED) {
      @Override
      public Volume getCellValue(WellCopy wellCopy) { return wellCopy.getConsumedVolume(); }
    });
    columns.add(new VolumeColumn<WellCopy>(
      "Remaining Volume", "The remaining volume of this well copy", TableColumn.UNGROUPED) {
      @Override
      public Volume getCellValue(WellCopy wellCopy) { return wellCopy.getRemainingVolume(); }
    });
    _withdrawalsAdjustmentsColumn = new IntegerColumn<WellCopy>(
      "Withdrawals/Adjustments", "The number of withdrawals and administrative adjustments made from this well copy", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(WellCopy wellCopy) { return wellCopy.getWellVolumeAdjustments().size(); }

      @Override
      public boolean isCommandLink() { return getRowData().getWellVolumeAdjustments().size() > 0; }

      @Override
      public Object cellAction(WellCopy entity)
      {
        return null;
        //return showRowDetail();
      }
    };
    columns.add(_withdrawalsAdjustmentsColumn);
    _newRemainingVolumeColumn = new VolumeColumn<WellCopy>(
      "New Remaining Volume", "Enter new remaining volume", TableColumn.UNGROUPED) {
      @Override
      public Volume getCellValue(WellCopy wellCopy) { return _newRemainingVolumes.get(wellCopy); }

      @Override
      public void setCellValue(WellCopy wellCopy, Volume volume)
      {
        if (volume != null) {
          _newRemainingVolumes.put(wellCopy, volume);
        }
        else {
          _newRemainingVolumes.remove(wellCopy);
        }
      }

      @Override
      public boolean isEditable() { return true; }
    };
    columns.add(_newRemainingVolumeColumn);
    _newRemainingVolumeColumn.setVisible(false);

//    TableColumnManager<WellCopy> columnManager = getColumnManager();
//    columnManager.addCompoundSortColumns(columnManager.getColumn("Library"), columnManager.getColumn("Plate"), columnManager.getColumn("Well"), columnManager.getColumn("Copy"));
//    columnManager.addCompoundSortColumns(columnManager.getColumn("Plate"), columnManager.getColumn("Well"), columnManager.getColumn("Copy"));
//    columnManager.addCompoundSortColumns(columnManager.getColumn("Well"), columnManager.getColumn("Plate"), columnManager.getColumn("Copy"));
//    columnManager.addCompoundSortColumns(columnManager.getColumn("Copy"), columnManager.getColumn("Plate"), columnManager.getColumn("Well"), columnManager.getColumn("Copy"));
//    columnManager.addCompoundSortColumns(columnManager.getColumn("Initial Volume"), columnManager.getColumn("Plate"), columnManager.getColumn("Well"), columnManager.getColumn("Copy"));
//    columnManager.addCompoundSortColumns(columnManager.getColumn("Consumed Volume"), columnManager.getColumn("Plate"), columnManager.getColumn("Well"), columnManager.getColumn("Copy"));
//    columnManager.addCompoundSortColumns(columnManager.getColumn("Remaining Volume"), columnManager.getColumn("Plate"), columnManager.getColumn("Well"), columnManager.getColumn("Copy"));

//    return _columns;
    return columns;
  }

  public String getWellVolumeAdjustmentActivityComments()
  {
    return _wellVolumeAdjustmentActivityComments;
  }

  public void setWellVolumeAdjustmentActivityComments(String wellVolumeAdjustmentActivityComments)
  {
    _wellVolumeAdjustmentActivityComments = wellVolumeAdjustmentActivityComments;
  }

//@Override
//protected void makeRowDetail(WellCopy wcv)
//{
//  List<WellVolumeAdjustment> wvas = new ArrayList<WellVolumeAdjustment>(wcv.getWellVolumeAdjustments().size());
//  for (WellVolumeAdjustment wva : wcv.getWellVolumeAdjustments()) {
//    WellVolumeAdjustment wva2 = _dao.reloadEntity(wva,
//                                                  true,
//                                                  "well",
//                                                  "copy",
//                                                  "labCherryPick.wellVolumeAdjustments",
//                                                  "labCherryPick.cherryPickRequest",
//                                                  "labCherryPick.assayPlate.cherryPickLiquidTransfer",
//                                                  "wellVolumeCorrectionActivity.performedBy");
//    wvas.add(wva2);
//  }
//  getRowDetail().setContents(wvas);
//}

  @Override
  public void doEdit()
  {
    _newRemainingVolumes.clear();
    _wellVolumeAdjustmentActivityComments = null;
  }

  @Override
  public void doSave()
  {
    final ScreensaverUser screensaverUser = getCurrentScreensaverUser().getScreensaverUser();
    if (!(screensaverUser instanceof AdministratorUser) || !((AdministratorUser) screensaverUser).isUserInRole(ScreensaverUserRole.LIBRARIES_ADMIN)) {
      throw new BusinessRuleViolationException("only libraries administrators can edit well volumes");
    }
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        if (_newRemainingVolumes.size() > 0) {
          AdministratorUser administratorUser = (AdministratorUser) _dao.reloadEntity(screensaverUser);
          WellVolumeCorrectionActivity wellVolumeCorrectionActivity =
            new WellVolumeCorrectionActivity(administratorUser, new LocalDate());
          wellVolumeCorrectionActivity.setComments(getWellVolumeAdjustmentActivityComments());
          // TODO
          //wellVolumeCorrectionActivity.setApprovedBy();
          for (Map.Entry<WellCopy,Volume> entry : _newRemainingVolumes.entrySet()) {
            WellCopy wellCopyVolume = entry.getKey();
            Volume newRemainingVolume = entry.getValue();
            Copy copy = _dao.reloadEntity(wellCopyVolume.getCopy());
            Well well = _dao.reloadEntity(wellCopyVolume.getWell());
            WellVolumeAdjustment wellVolumeAdjustment =
              wellVolumeCorrectionActivity.createWellVolumeAdjustment(copy,
                                                                      well,
                                                                      newRemainingVolume.subtract(wellCopyVolume.getRemainingVolume()));
            wellCopyVolume.addWellVolumeAdjustment(wellVolumeAdjustment);
            log.debug("added well volume adjustment to well copy " + wellCopyVolume);
          }
          _dao.saveOrUpdateEntity(wellVolumeCorrectionActivity);
        }
      }
    });
    if (_newRemainingVolumes.size() > 0) {
      refetch();
      _wellVolumeSearchResults.refetch();
      // TODO: this showMessage() call prevents data table from being rendered with updated values!
      //showMessage("libraries.updatedWellVolumes", new Integer(_newRemainingVolumes.size()));
    }
    else {
      showMessage("libraries.updatedNoWellVolumes");
    }
  }

  @Override
  protected void setEditMode(boolean isEditMode)
  {
    super.setEditMode(isEditMode);
    _withdrawalsAdjustmentsColumn.setVisible(!isEditMode);
    _newRemainingVolumeColumn.setVisible(isEditMode);
    // TODO: cancel the refetch() caused by changing column visibility
  }

  // private methods

  private SortedSet<WellCopy> makeWellCopyVolumes(Library library,
                                                  SortedSet<WellCopy> wellCopyVolumes)
  {
    for (Well well : library.getWells()) {
      makeWellCopyVolumes(well, wellCopyVolumes);
    }
    return wellCopyVolumes;
  }

  private SortedSet<WellCopy> makeWellCopyVolumes(Well well,
                                                  SortedSet<WellCopy> wellCopyVolumes)
  {
    for (Copy copy : well.getLibrary().getCopies()) {
      wellCopyVolumes.add(new WellCopy(well, copy));
    }
    return wellCopyVolumes;
  }

//  private List<WellCopy> makeWellCopyVolumes(Copy copy, List<WellCopy> wellVolumes)
//  {
//    for (int plateNumber = copy.getLibrary().getStartPlate(); plateNumber <= copy.getLibrary().getEndPlate(); ++plateNumber) {
//      makeWellCopyVolumes(copy, plateNumber, wellVolumes);
//    }
//    return wellVolumes;
//  }
//
//
//  private List<WellCopy> makeWellCopyVolumes(Copy copy, int plateNumber, List<WellCopy> wellVolumes)
//  {
//    for (int iRow = 0; iRow < Well.PLATE_ROWS; ++iRow) {
//      for (int iCol = 0; iCol < Well.PLATE_COLUMNS; ++iCol) {
//        wellVolumes.add(new WellCopy(findWell(new WellKey(plateNumber, iRow, iCol)), copy));
//      }
//    }
//    return wellVolumes;
//  }

  /**
   * Aggregates wellVolumeAdjustments into the provided wellCopyVolumes. The
   * wellCopyVolumes parameter is necessary in order to report the full set well
   * copies including those that have zero well volume adjustments.
   */
  private SortedSet<WellCopy> aggregateWellVolumeAdjustments(SortedSet<WellCopy> wellCopyVolumes,
                                                             List<WellVolumeAdjustment> wellVolumeAdjustments)
  {
    Collections.sort(wellVolumeAdjustments,
                     new Comparator<WellVolumeAdjustment>() {
      public int compare(WellVolumeAdjustment wva1, WellVolumeAdjustment wva2)
      {
        int result = wva1.getWell().compareTo(wva2.getWell());
        if (result == 0) {
          result = wva1.getCopy().getName().compareTo(wva2.getCopy().getName());
        }
        return result;
      }
    });
    Iterator<WellCopy> wcvIter = wellCopyVolumes.iterator();
    Iterator<WellVolumeAdjustment> wvaIter = wellVolumeAdjustments.iterator();
    if (wcvIter.hasNext()) {
      WellCopy wellCopyVolume = wcvIter.next();
      while (wvaIter.hasNext()) {
        WellVolumeAdjustment wellVolumeAdjustment = wvaIter.next();
        while (!wellCopyVolume.getWell().equals(wellVolumeAdjustment.getWell()) ||
          !wellCopyVolume.getCopy().equals(wellVolumeAdjustment.getCopy())) {
          if (!wcvIter.hasNext()) {
            throw new IllegalArgumentException("wellVolumeAdjustments exist for wells that were not in wellCopyVolumes: " +
                                               wellVolumeAdjustment.getWell() + ":" + wellVolumeAdjustment.getCopy().getName());
          }
          wellCopyVolume = wcvIter.next();
        }
        wellCopyVolume.addWellVolumeAdjustment(wellVolumeAdjustment);
      }
    }
    return wellCopyVolumes;
  }

  private SortedSet<WellVolume> aggregateWellCopies(List<WellCopy> nonAggregatedData)
  {
    TreeSet<WellVolume> result = new TreeSet<WellVolume>();
    Collections.sort(nonAggregatedData);
    HashSet<WellCopy> wellCopiesGroup = new HashSet<WellCopy>();
    Well groupByWell = null;
    for (WellCopy wellCopy : nonAggregatedData) {
      if (! wellCopy.getWell().equals(groupByWell)) {
        if (groupByWell != null) {
          result.add(new WellVolume(groupByWell, wellCopiesGroup));
        }
        wellCopiesGroup.clear();
        groupByWell = wellCopy.getWell();
      }
      wellCopiesGroup.add(wellCopy);
    }
    // add the last group
    if (groupByWell != null) {
      result.add(new WellVolume(groupByWell, wellCopiesGroup));
    }

    return result;
  }


  private void addRelationshipsToFetch(EntityDataFetcher<WellVolumeAdjustment,Integer> wvaFetcher)
  {
    List<PropertyPath<WellVolumeAdjustment>> relationships = Lists.newArrayList();
    relationships.add(WellVolumeAdjustment.well.to(Well.library).toFullEntity());
    relationships.add(WellVolumeAdjustment.copy.to(Copy.plates).toFullEntity());
    wvaFetcher.setPropertiesToFetch(relationships);
  }

  @Override
  public void searchAll()
  {
    // TODO Auto-generated method stub
    
  }
}
