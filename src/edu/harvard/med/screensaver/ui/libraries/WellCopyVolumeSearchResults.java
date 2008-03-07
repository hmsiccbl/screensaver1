// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.AggregateDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.ParentedEntityDataFetcher;
import edu.harvard.med.screensaver.db.hibernate.HqlBuilder;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.searchresults.AggregateSearchResults;
import edu.harvard.med.screensaver.ui.table.column.FixedDecimalColumn;
import edu.harvard.med.screensaver.ui.table.column.IntegerColumn;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.TextColumn;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;
import edu.harvard.med.screensaver.ui.table.model.InMemoryDataModel;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public class WellCopyVolumeSearchResults extends AggregateSearchResults<WellCopy,Pair<WellKey,String>>
{
  // static members

  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole.LIBRARIES_ADMIN;

  private static Logger log = Logger.getLogger(WellCopyVolumeSearchResults.class);


  // instance data members

  private GenericEntityDAO _dao;
  private LibraryViewer _libraryViewer;
  private WellViewer _wellViewer;
  private WellVolumeSearchResults _wellVolumeSearchResults;

  private Map<WellCopy,BigDecimal> _newRemainingVolumes = new HashMap<WellCopy,BigDecimal>();
  private String _wellVolumeAdjustmentActivityComments;
  private TableColumn<WellCopy,?> _newRemainingVolumeColumn;
  private TableColumn<WellCopy,?> _withdrawalsAdjustmentsColumn;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected WellCopyVolumeSearchResults()
  {
    super(new String[] { "editable" });
  }

  public WellCopyVolumeSearchResults(GenericEntityDAO dao,
                                     LibraryViewer libraryViewer,
                                     WellViewer wellViewer,
                                     WellVolumeSearchResults wellVolumeSearchResults,
                                     WellVolumeAdjustmentSearchResults rowDetail)
  {
    this();
    _dao = dao;
    _wellVolumeSearchResults = wellVolumeSearchResults;
    _libraryViewer = libraryViewer;
    _wellViewer = wellViewer;
    //setRowDetail(rowDetail);
  }

  // public methods

  public void searchWells(final Set<WellKey> wellKeys)
  {
    EntityDataFetcher<WellVolumeAdjustment,Integer> wvaFetcher =
      new EntityDataFetcher<WellVolumeAdjustment,Integer>(WellVolumeAdjustment.class, _dao) {
      @Override
      protected void addDomainRestrictions(HqlBuilder hql,
                                           Map<RelationshipPath<WellVolumeAdjustment>,String> path2Alias)
      {
        Set<String> wellKeyStrings = new HashSet<String>();
        for (WellKey wellKey : wellKeys) {
          wellKeyStrings.add(wellKey.toString());
        }
        hql.whereIn(getRootAlias(), "well.id", wellKeyStrings);
      }
    };
    addRelationshipsToFetch(wvaFetcher);

    initialize(new AggregateDataFetcher<WellCopy,Pair<WellKey,String>,WellVolumeAdjustment,Integer>(wvaFetcher) {
      @Override
      protected SortedSet<WellCopy> aggregateData(List<WellVolumeAdjustment> nonAggregatedData)
      {
        SortedSet<WellCopy> result = new TreeSet<WellCopy>();
        for (WellKey wellKey : wellKeys) {
          Well well = _dao.findEntityById(Well.class, wellKey.toString(), true, "library.copies.copyInfos");
          makeWellCopyVolumes(well, result);
        }
        return aggregateWellVolumeAdjustments(result, nonAggregatedData);
      }
    });
  }

  public void searchWellsForLibrary(final Library library)
  {
    EntityDataFetcher<WellVolumeAdjustment,Integer> wvaFetcher =
      new ParentedEntityDataFetcher<WellVolumeAdjustment,Integer>(WellVolumeAdjustment.class,
        new RelationshipPath<WellVolumeAdjustment>(WellVolumeAdjustment.class, "well.library"),
        library,
        _dao);
    addRelationshipsToFetch(wvaFetcher);

    initialize(new AggregateDataFetcher<WellCopy,Pair<WellKey,String>,WellVolumeAdjustment,Integer>(wvaFetcher) {
      @Override
      @Transactional
      protected SortedSet<WellCopy> aggregateData(List<WellVolumeAdjustment> nonAggregatedData)
      {
        // reload library and eager fetch some relationships, allowing makeWellCopyVolumes() to work
        final Library[] library2 = new Library[1];
        _dao.doInTransaction(new DAOTransaction() {
          public void runTransaction() {
            library2[0] = _dao.reloadEntity(library, true, "wells");
            _dao.needReadOnly(library2[0], "copies.copyInfos");
          }
        });
        return aggregateWellVolumeAdjustments(makeWellCopyVolumes(library2[0], new TreeSet<WellCopy>()),
                                              nonAggregatedData);
      }
    });
  }

  public void searchWellsForCherryPickRequest(CherryPickRequest cherryPickRequest,
                                              boolean forUnfulfilledOnly)
  {
    // note: it would be nicer to select the WVAs by their parent CPR, but we
    // can't do this since Well.cherryPickRequests relationship does not exist in
    // our model; instead we just find all the wells for CPR and delegate to the
    // searchWells() method
    Set<WellKey> wellKeys = new HashSet<WellKey>();
    for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
      if (!forUnfulfilledOnly || labCherryPick.isUnfulfilled()) {
        wellKeys.add(labCherryPick.getSourceWell().getWellKey());
      }
    }
    searchWells(wellKeys);
  }

  @Override
  public void initialize(DataFetcher<WellCopy,Pair<WellKey,String>,Object> dataFetcher)
  {
    super.initialize(dataFetcher);

    _wellVolumeSearchResults.initialize(new AggregateDataFetcher<WellVolume,WellKey,WellCopy,Pair<WellKey,String>>(dataFetcher) {
      @Override
      protected SortedSet<WellVolume> aggregateData(List<WellCopy> nonAggregatedData)
      {
        return aggregateWellCopies(nonAggregatedData);
      }
    });
  }

  public WellVolumeSearchResults getWellVolumeSearchResults()
  {
    return _wellVolumeSearchResults;
  }

  @Override
  protected ScreensaverUserRole getEditableAdminRole()
  {
    return EDITING_ROLE;
  }

  @Override
  protected DataTableModel<WellCopy> buildDataTableModel(DataFetcher<WellCopy,Pair<WellKey,String>,Object> dataFetcher,
                                                         List<? extends TableColumn<WellCopy,?>> columns)
  {
    return new InMemoryDataModel<WellCopy>(dataFetcher);
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
      public Object cellAction(WellCopy wellCopy) { return _libraryViewer.viewLibrary(wellCopy.getWell().getLibrary()); }
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
      public Object cellAction(WellCopy wellCopy) { return _wellViewer.viewWell(wellCopy.getWell()); }
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
    columns.add(new FixedDecimalColumn<WellCopy>(
      "Initial Volume", "The initial volume of this well copy", TableColumn.UNGROUPED) {
      @Override
      public BigDecimal getCellValue(WellCopy wellCopy) { return wellCopy.getInitialMicroliterVolume(); }
    });
    columns.add(new FixedDecimalColumn<WellCopy>(
      "Consumed Volume", "The volume already used from this well copy", TableColumn.UNGROUPED) {
      @Override
      public BigDecimal getCellValue(WellCopy wellCopy) { return wellCopy.getConsumedMicroliterVolume(); }
    });
    columns.add(new FixedDecimalColumn<WellCopy>(
      "Remaining Volume", "The remaining volume of this well copy", TableColumn.UNGROUPED) {
      @Override
      public BigDecimal getCellValue(WellCopy wellCopy) { return wellCopy.getRemainingMicroliterVolume(); }
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
    _newRemainingVolumeColumn = new FixedDecimalColumn<WellCopy>(
      "New Remaining Volume", "Enter new remaining volume", TableColumn.UNGROUPED) {
      @Override
      public BigDecimal getCellValue(WellCopy wellCopy) { return _newRemainingVolumes.get(wellCopy); }

      @Override
      public void setCellValue(WellCopy wellCopy, Object value)
      {
        if (value != null) {
          _newRemainingVolumes.put(wellCopy, (BigDecimal) value);
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
            new WellVolumeCorrectionActivity(administratorUser, new Date());
          wellVolumeCorrectionActivity.setComments(getWellVolumeAdjustmentActivityComments());
          // TODO
          //wellVolumeCorrectionActivity.setApprovedBy();
          for (Map.Entry<WellCopy,BigDecimal> entry : _newRemainingVolumes.entrySet()) {
            WellCopy wellCopyVolume = entry.getKey();
            BigDecimal newRemainingVolume = entry.getValue();
            Copy copy = _dao.reloadEntity(wellCopyVolume.getCopy());
            Well well = _dao.reloadEntity(wellCopyVolume.getWell());
            WellVolumeAdjustment wellVolumeAdjustment =
              wellVolumeCorrectionActivity.createWellVolumeAdjustment(copy,
                                                                      well,
                                                                      newRemainingVolume.subtract(wellCopyVolume.getRemainingMicroliterVolume()));
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
    List<RelationshipPath<WellVolumeAdjustment>> relationships = new ArrayList<RelationshipPath<WellVolumeAdjustment>>();
    relationships.add(new RelationshipPath<WellVolumeAdjustment>(WellVolumeAdjustment.class, "well.library"));
    relationships.add(new RelationshipPath<WellVolumeAdjustment>(WellVolumeAdjustment.class, "copy.copyInfos"));
    wvaFetcher.setRelationshipsToFetch(relationships);
  }
}
