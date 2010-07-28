// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.activities;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.Concentration;
import edu.harvard.med.screensaver.model.ConcentrationUnit;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screens.AssayProtocolType;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestAllocator;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.EditResult;
import edu.harvard.med.screensaver.ui.SearchResultContextEditableEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.cherrypickrequests.SelectableRow;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.searchresults.ActivitySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.LibrarySearchResults;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;

public class ActivityViewer extends SearchResultContextEditableEntityViewerBackingBean<Activity,Activity>
{
  private static Logger log = Logger.getLogger(ActivityViewer.class);

  private LibrariesDAO _librariesDao;
  private ScreenViewer _screenViewer;
  private CherryPickRequestViewer _cherryPickRequestViewer;
  private CherryPickRequestAllocator _cherryPickRequestAllocator;
  private LibrarySearchResults _librarySearchResults;
  private DataModel _platesScreenedDataModel;

  private UISelectOneEntityBean<ScreensaverUser> _performedBy;
  private UISelectOneBean<AssayProtocolType> _assayProtocolType;
  private UISelectOneBean<ConcentrationUnit> _concentrationType;
  private UISelectOneBean<VolumeUnit> _volumeType;
  private String _concentrationValue;
  private String _volumeValue;
  private DataModel _cherryPickPlatesDataModel;
  private AbstractBackingBean _returnToViewAfterEdit;

  private boolean _editingNewEntity;

  private Integer _newPlateRangeScreenedStartPlate;
  private Integer _newPlateRangeScreenedEndPlate;
  private UISelectOneBean<String> _newPlateRangeScreenedCopy;

  private List<Copy> _copies;

  

  /**
   * @motivation for CGLIB2
   */
  protected ActivityViewer()
  {
  }

  public ActivityViewer(ActivityViewer thisProxy,
                        GenericEntityDAO dao,
                        LibrariesDAO librariesDao,
                        ActivitySearchResults activitiesBrowser,
                        ScreenViewer screenViewer,
                        CherryPickRequestViewer cherryPickRequestViewer,
                        CherryPickRequestAllocator cherryPickRequestAllocator,
                        LibrarySearchResults librarySearchResults)
  {
    super(thisProxy, Activity.class, BROWSE_ACTIVITIES, VIEW_ACTIVITY, dao, activitiesBrowser);
    _screenViewer = screenViewer;
    _cherryPickRequestViewer = cherryPickRequestViewer;
    _librariesDao = librariesDao;
    _cherryPickRequestAllocator = cherryPickRequestAllocator;
    _librarySearchResults = librarySearchResults;
  }

  @Override
  protected void initializeEntity(Activity activity)
  {
    getDao().needReadOnly(activity,
                          "screen.labHead",
                          "screen.leadScreener",
                          "screen.collaborators",
                          "performedBy");
    // HACK: performedBy is not being eager fetched when this method is called
    // from viewActivity(); chalking this up to a Hibernate bug for now, since
    // the relationships *are* being eager fetched; problem manifests in
    // ActivityViewer.getPerformedBy() method
    activity.getPerformedBy().getEntityId();

    if (activity instanceof LibraryScreening) {
      getDao().needReadOnly(activity, LibraryScreening.Screen.to(Screen.assayPlates).getPath());
      getDao().needReadOnly(activity, LibraryScreening.assayPlatesScreened.to(AssayPlate.plateScreened).to(Plate.copy).to(Copy.library).getPath());
    }
    if (activity instanceof CherryPickScreening) {
      getDao().needReadOnly(activity, CherryPickScreening.cherryPickRequest.to(CherryPickRequest.cherryPickAssayPlates).to(CherryPickAssayPlate.cherryPickLiquidTransfer).getPath());
      getDao().needReadOnly(activity, CherryPickScreening.cherryPickAssayPlatesScreened.getPath());
    }
    if (activity instanceof CherryPickLiquidTransfer) {
      getDao().needReadOnly(activity, CherryPickLiquidTransfer.cherryPickAssayPlates.to(CherryPickAssayPlate.cherryPickRequest).to(CherryPickRequest.cherryPickAssayPlates).to(CherryPickAssayPlate.cherryPickLiquidTransfer).getPath());
    }
    _editingNewEntity = false;
  }
  
  @Override
  protected void initializeNewEntity(Activity activity)
  {
    if (activity instanceof LabActivity) {
      // note: activity must be transient, while activity.screen must be managed
      getDao().needReadOnly(((LabActivity) activity).getScreen(), Screen.labHead.getPath(), Screen.leadScreener.getPath());
      getDao().needReadOnly(((LabActivity) activity).getScreen(), Screen.collaborators.getPath());
    }

    if (activity instanceof CherryPickScreening) {
      getDao().needReadOnly(((CherryPickScreening) activity).getCherryPickRequest(), CherryPickRequest.cherryPickAssayPlates.to(CherryPickAssayPlate.cherryPickLiquidTransfer).to(Activity.performedBy).getPath());
    }

    if (activity instanceof CherryPickLiquidTransfer) {
      getDao().needReadOnly(((CherryPickLiquidTransfer) activity).getCherryPickRequest(), CherryPickRequest.cherryPickAssayPlates.to(CherryPickAssayPlate.cherryPickLiquidTransfer).to(Activity.performedBy).getPath());
    }
    // set null dateOfActivity, to force user to enter a valid date
    // TODO: this model shouldn't allow this null value, and we should really set to null at the UI component level only
    if (activity instanceof LibraryScreening) {
      activity.setDateOfActivity(null);
      getDao().needReadOnly(((LibraryScreening) activity).getScreen(), Screen.assayPlates.getPath());
    }
    _editingNewEntity = true;
  }
  
  protected void initializeViewer(Activity activity)
  {
    _performedBy = null;
    _cherryPickPlatesDataModel = null;
    _newPlateRangeScreenedStartPlate = null;
    _newPlateRangeScreenedEndPlate = null;
    _newPlateRangeScreenedCopy = null;
    _copies = null;
    _platesScreenedDataModel = null;
    _concentrationType = null;
    _concentrationValue = null;
    _volumeType = null;
    _volumeValue = null;
  }
  

  public UISelectOneBean<ScreensaverUser> getPerformedBy()
  {
    if (_performedBy == null) {
      Set<ScreensaverUser> performedByCandidates = getEntity().getPerformedByCandidates();
      if (performedByCandidates == null) {
        performedByCandidates = Sets.newTreeSet();
        performedByCandidates.addAll(getDao().findAllEntitiesOfType(ScreensaverUser.class));
      }
      _performedBy = new UISelectOneEntityBean<ScreensaverUser>(
        performedByCandidates,
        getEntity().getPerformedBy(),
        getDao()) {
        @Override
        protected String makeLabel(ScreensaverUser t) { return t.getFullNameLastFirst(); }
      };
    }
    return _performedBy;
  }
  

  public UISelectOneBean<VolumeUnit> getVolumeTransferredPerWellType()
  {
    try {
      if (_volumeType == null) {
        Volume v = (getEntity() instanceof LabActivity ?
                                    ((LabActivity) getEntity()).getVolumeTransferredPerWell() :
                                    null );
        VolumeUnit unit = ( v == null ? VolumeUnit.NANOLITERS : v.getUnits());

        _volumeType = new UISelectOneBean<VolumeUnit>(VolumeUnit.DISPLAY_VALUES, unit )
          {
            @Override
            protected String makeLabel(VolumeUnit t)
            {
              return t.getValue();
            }
          };
      }
      return _volumeType;
    } catch (Exception e) {
      log.error("err: " + e);
      return null;
    }
  }


  /**
   * This method exists to grab the value portion of the Quantity stored
  */
  public String getVolumeTransferredPerWellValue()
  {
    if (_volumeValue == null) {
      if (getEntity() instanceof LabActivity) {
        Volume volumeTransferredPerWell = ((LabActivity) getEntity()).getVolumeTransferredPerWell();
        if (volumeTransferredPerWell != null) {
          _volumeValue = volumeTransferredPerWell.getDisplayValue().toString();
        }
      }
    }
    return _volumeValue;
  }
  
  /**
   * This method exists to set the value portion of the Quantity stored
   * @see #save()
  */
  public void setVolumeTransferredPerWellValue( String value )
  {
    _volumeValue = value;
  }

  
  public Concentration getConcentration()
  {
    return getEntity() instanceof LabActivity ? ((LabActivity) getEntity()).getConcentration(): null;
  }


  
  public UISelectOneBean<ConcentrationUnit> getConcentrationType()
  {
    try {
      if (_concentrationType == null) {
        setConcentrationType( getEntity() instanceof LabActivity ? ((LabActivity) getEntity()).getConcentrationUnits() : null);
      }
      return _concentrationType;
    } catch (Exception e) {
      log.error("err: " + e);
      return null;
    }
  }

  private void setConcentrationType( ConcentrationUnit unit )
  {
    _concentrationType =
      new UISelectOneBean<ConcentrationUnit>(ConcentrationUnit.DISPLAY_VALUES, unit)
      {
        @Override
        protected String makeLabel(ConcentrationUnit t) { return t.getValue(); }
      };
  }

  
  /**
   * This method exists to grab the value portion of the Quantity stored
  */
  public String getConcentrationValue()
  {
    if( _concentrationValue == null )
    {
      _concentrationValue =
        getEntity() instanceof LabActivity ? ((LabActivity) getEntity()).getConcentrationValue(): null;
    }
    return _concentrationValue;
  }

  
  /**
   * This method exists to set the value portion of the Quantity stored
   * @see #save()
  */
  public void setConcentrationValue( String value )
  {
    _concentrationValue = value;
  }

  public UISelectOneBean<AssayProtocolType> getAssayProtocolType()
  {
    if (_assayProtocolType == null) {
      _assayProtocolType = new UISelectOneBean<AssayProtocolType>(Arrays.asList(AssayProtocolType.values()),
        getEntity() instanceof Screening ? ((Screening) getEntity()).getAssayProtocolType() : null) {
        @Override
        protected String makeLabel(AssayProtocolType t) { return t.getValue(); }
      };
    }
    return _assayProtocolType;
  }


  
  @Override
  protected boolean validateEntity(Activity entity)
  {
    boolean valid = true;
    if (entity instanceof Screening) {
      try {
        Volume.makeVolume(getVolumeTransferredPerWellValue(),
                          getVolumeTransferredPerWellType().getSelection());
      } 
      catch (Exception e) {
        showFieldInputError("Volume Transferred Per Replicate", e.getLocalizedMessage());
        valid = false;
      }
      try {
        Concentration.makeConcentration(getConcentrationValue(),
                                        getConcentrationType().getSelection());
      } 
      catch (Exception e) {
        showFieldInputError("Concentration", e.getLocalizedMessage());
        valid = false;
      }
    }
    if (entity instanceof CherryPickScreening) {
      for (SelectableRow<CherryPickAssayPlate> row : (List<SelectableRow<CherryPickAssayPlate>>) getCherryPickPlatesDataModel().getWrappedData()) {
        CherryPickAssayPlate plate = row.getData();
        if (row.isSelected()  && !((CherryPickScreening) entity).getCherryPickAssayPlatesScreened().contains(row.getData())) {
          if (!plate.isPlated() || plate.isPlatedAndScreened()) {
            reportApplicationError("Plate " + plate.getName() + " cannot be screened because its status is " + plate.getStatusLabel());
            row.setSelected(false);
            valid = false;
          }
        }
      }
    }
    return valid;
  }

  
  @Override
  protected void updateEntityProperties(Activity entity) 
  {
    if (entity instanceof Screening) {
      ((Screening) entity).setVolumeTransferredPerWell(Volume.makeVolume(_volumeValue, _volumeType.getSelection()));
      ((Screening) entity).setConcentration(Concentration.makeConcentration(_concentrationValue, _concentrationType.getSelection()));
    }
    if (entity instanceof CherryPickScreening) {
      CherryPickScreening screening = (CherryPickScreening) entity;
      for (SelectableRow<CherryPickAssayPlate> row : (List<SelectableRow<CherryPickAssayPlate>>) getCherryPickPlatesDataModel().getWrappedData()) {
        if (row.isSelected() && !screening.getCherryPickAssayPlatesScreened().contains(row.getData())) {
          screening.addCherryPickAssayPlateScreened(row.getData());
        }
        else if (!row.isSelected() && screening.getCherryPickAssayPlatesScreened().contains(row.getData())) {
          screening.removeCherryPickAssayPlateScreened(row.getData());
        }
        // save/update every assay plate, to ensure that all assay plates for newly created CPS are updated 
        getDao().saveOrUpdateEntity(row.getData());
      }
    }
    if (entity instanceof CherryPickLiquidTransfer) {
      if (_editingNewEntity) {
        CherryPickLiquidTransfer cplt = (CherryPickLiquidTransfer) entity;
        for (CherryPickAssayPlate cpap : cplt.getCherryPickAssayPlates()) {
          getDao().saveOrUpdateEntity(cpap);
          for (LabCherryPick lcp : cpap.getLabCherryPicks()) {
            getDao().saveOrUpdateEntity(lcp);
          }
        }
        // do calculations necessitated by invalidate calls on the screen 
        // (i.e. for screen.getFulfilledLabCherryPicksCount invalidated in CPAP.setCherryPickLiquidTransfer )...
        cplt.getScreen().update();
      }
    }
    if (entity instanceof LibraryScreening) {
      // explicitly save/update the parent screen to update assay plates that have been added or deleted  
      getDao().saveOrUpdateEntity(((LibraryScreening) entity).getScreen());
      ((LibraryScreening) entity).getScreen().update();
      entity.update();
    }
    // note: execute this after parent entity (Screen/CPR) is reattached, to avoid NonUniqueObjectExceptions
    entity.setPerformedBy(getPerformedBy().getSelection());
  }
  
  public DataModel getPlatesScreenedDataModel()
  {
    if (_platesScreenedDataModel == null) {
      if (getEntity() instanceof LibraryScreening) {
        LibraryScreening libraryScreening = (LibraryScreening) getEntity();
        _platesScreenedDataModel = new ListDataModel(PlateRange.splitIntoPlateCopyRanges(libraryScreening.getAssayPlatesScreened()));
      }
    }
    return _platesScreenedDataModel;
  }

  public Integer getNewPlateRangeScreenedStartPlate()
  {
    return _newPlateRangeScreenedStartPlate;
  }

  public void setNewPlateRangeScreenedStartPlate(Integer newPlateRangeScreenedStartPlate)
  {
    this._newPlateRangeScreenedStartPlate = newPlateRangeScreenedStartPlate;
  }
  
  public Integer getNewPlateRangeScreenedEndPlate()
  {
    return _newPlateRangeScreenedEndPlate;
  }
  
  public void setNewPlateRangeScreenedEndPlate(Integer newPlateRangeScreenedEndPlate)
  {
    this._newPlateRangeScreenedEndPlate = newPlateRangeScreenedEndPlate;
  }
  
  public UISelectOneBean<String> getNewPlateRangeScreenedCopy()
  {
    if (_newPlateRangeScreenedCopy == null) {
      _copies = getAllCopies();
      SortedSet<String> copyNames = Sets.newTreeSet(Iterables.transform(_copies, Copy.ToName)); 
      _newPlateRangeScreenedCopy = new UISelectOneBean<String>(copyNames, null, true) {
        @Override protected String getEmptyLabel() { return "<select>"; }
      };
    }
    return _newPlateRangeScreenedCopy;
  }

  public DataModel getCherryPickPlatesDataModel()
  {
    if (_cherryPickPlatesDataModel == null) {
      List<SelectableRow<CherryPickAssayPlate>> rows = Lists.newArrayList();
      List<CherryPickAssayPlate> plates = Lists.newArrayList();
      Set<CherryPickAssayPlate> selectedPlates = Sets.newHashSet();
      if (getEntity() instanceof CherryPickScreening) {
        CherryPickScreening cherryPickScreening = (CherryPickScreening) getEntity();
        plates = cherryPickScreening.getCherryPickRequest().getActiveCherryPickAssayPlates();
        selectedPlates = cherryPickScreening.getCherryPickAssayPlatesScreened();
      }
      else if (getEntity() instanceof CherryPickLiquidTransfer) {
        CherryPickLiquidTransfer cplt = (CherryPickLiquidTransfer) getEntity();
        if (cplt.getCherryPickRequest() != null) {
          plates = cplt.getCherryPickRequest().getActiveCherryPickAssayPlates();
          selectedPlates = cplt.getCherryPickAssayPlates();
        }
      }
      for (CherryPickAssayPlate plate : plates) {
        rows.add(new SelectableRow<CherryPickAssayPlate>(plate, selectedPlates.contains(plate)));
      }
      _cherryPickPlatesDataModel = new ListDataModel(rows);
    }
    return _cherryPickPlatesDataModel;
  }

//  @UICommand
//  public String addPlatesScreened()
//  {
//    if (getEntity() instanceof LibraryScreening) {
//      LibraryScreening libraryScreenings = (LibraryScreening) getEntity();
//      if (getNewPlatesScreened().getStartPlate() != null &&
//        getNewPlatesScreened().getCopyName() != null &&
//        getNewPlatesScreened().getCopyName().length() != 0) {
//        if (getNewPlatesScreened().getEndPlate() == null) {
//          getNewPlatesScreened().setEndPlate(getNewPlatesScreened().getStartPlate());
//        }
//        for (PlatesUsed platesUsed : splitPlateRangeByLibrary(getNewPlatesScreened())) {
//          libraryScreenings.createPlatesUsed(platesUsed.getStartPlate(),
//                                            platesUsed.getEndPlate(),
//                                            platesUsed.getCopy());
//        }
//      }
//        _libraryAndPlatesScreenedDataModel = null;
//        _newPlatesScreened = null;
//      }
//    return REDISPLAY_PAGE_ACTION_RESULT;
//  }

//  private Set<PlatesUsed> splitPlateRangeByLibrary(PlatesUsed platesUsed)
//  {
//    int startPlate = platesUsed.getStartPlate();
//    int endPlate = platesUsed.getEndPlate();
//    Library libraryWithEndPlate = _librariesDao.findLibraryWithPlate(endPlate);
//    if (libraryWithEndPlate == null) {
//      reportApplicationError("plate " + endPlate + " is not contained in any library");
//      return Collections.emptySet();
//    }
//    Set<PlatesUsed> result = new HashSet<PlatesUsed>();
//    do {
//      Library libraryWithStartPlate = _librariesDao.findLibraryWithPlate(startPlate);
//      if (libraryWithStartPlate == null) {
//        reportApplicationError("plate " + endPlate + " is not contained in any library");
//        return Collections.emptySet();
//      }
//      PlatesUsed platesUsed2 = new PlatesUsed();
//      platesUsed2.setStartPlate(startPlate);
//      platesUsed2.setEndPlate(Math.min(libraryWithStartPlate.getEndPlate(), endPlate));
//      platesUsed2.setCopy(new Copy(libraryWithStartPlate,
//                                   CopyUsageType.FOR_LIBRARY_SCREENING,
//                                   platesUsed.getCopyName()));
//      result.add(platesUsed2);
//      startPlate = libraryWithStartPlate.getEndPlate() + 1;
//    } while (startPlate <= endPlate);
//    return result;
//  }

  
  public List<Copy> getAllCopies()
  {
    return getDao().findEntitiesByProperty(Copy.class, 
                                           "usageType", 
                                           CopyUsageType.FOR_LIBRARY_SCREENING, 
                                           true, 
                                           Copy.library.getPath());
  }
  
  
  @UICommand
  @Transactional
  public String addNewPlateRangeScreened()
  {
    if (getEntity() instanceof LibraryScreening) {
      LibraryScreening libraryScreening = (LibraryScreening) getEntity();
      if (libraryScreening.getNumberOfReplicates() == null ||
        libraryScreening.getNumberOfReplicates() == 0) {
        reportApplicationError("To add a plate range, the number of replicates must first be specified");
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
      if (getNewPlateRangeScreenedCopy().getSelection() == null) {
        reportApplicationError("Please select a copy for the plate range");
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
      if (getNewPlateRangeScreenedStartPlate() != null &&
        getNewPlateRangeScreenedEndPlate() != null &&
        getNewPlateRangeScreenedCopy() != null) {
        Set<Plate> newPlates = Sets.newHashSet();
        Library library = null;
        for (int plateNumber = getNewPlateRangeScreenedStartPlate(); plateNumber <= getNewPlateRangeScreenedEndPlate(); ++plateNumber) {
          final Plate plate = _librariesDao.findPlate(plateNumber, getNewPlateRangeScreenedCopy().getSelection());
          if (plate == null) {
            reportApplicationError("Unknown plate number and/or copy: " + plateNumber + ":" +  
                                   getNewPlateRangeScreenedCopy().getSelection());
            return REDISPLAY_PAGE_ACTION_RESULT;
          }
          if (library == null) {
            library = plate.getCopy().getLibrary();
          }
          // reinstate the following code to prevent the new plate range from spanning multiple libraries
          /*
          else {
            if (!library.equals(plate.getCopy().getLibrary())) {
              reportApplicationError("Plate range spans multiple libraries");
              return REDISPLAY_PAGE_ACTION_RESULT;
            }
          }
          */
          if (Iterables.any(libraryScreening.getAssayPlatesScreened(), 
                            new Predicate<AssayPlate>() { public boolean apply(AssayPlate ap) { return ap.getPlateNumber() == plate.getPlateNumber(); } })) {
            reportApplicationError("A plate number can only be screened once per library screening (" + plate.getPlateNumber() + ")");  
            return REDISPLAY_PAGE_ACTION_RESULT;
          }
          newPlates.add(plate);
        }
        for (Plate newPlate : newPlates) {
          libraryScreening.addAssayPlatesScreened(newPlate);
        }
      }
      _platesScreenedDataModel = null;
      _newPlateRangeScreenedCopy = null;
      _newPlateRangeScreenedEndPlate = null;
      _newPlateRangeScreenedStartPlate = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String deletePlateRange()
  {
    if (getEntity() instanceof LibraryScreening) {
      LibraryScreening libraryScreening = (LibraryScreening) getEntity();
      PlateRange plateRange = (PlateRange) getPlatesScreenedDataModel().getRowData();
      for (AssayPlate assayPlate : plateRange) {
        libraryScreening.removeAssayPlatesScreened(assayPlate.getPlateScreened());
      }
      _platesScreenedDataModel = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @Override
  protected String postEditAction(EditResult editResult)
  {
    if (getEntity() instanceof LibraryScreening) {
      switch (editResult) {
        case CANCEL_EDIT:
          return getThisProxy().reload();
        case SAVE_EDIT:
          return getThisProxy().reload();
        case CANCEL_NEW:
          return _screenViewer.reload();
        case SAVE_NEW:
          return _screenViewer.reload();
      }
    }
    else if (getEntity() instanceof CherryPickLiquidTransfer) {
      switch (editResult) {
        case CANCEL_EDIT:
          return getThisProxy().reload();
        case SAVE_EDIT:
          return getThisProxy().reload();
        case CANCEL_NEW:
          return _cherryPickRequestViewer.reload();
        case SAVE_NEW: {
          if (((CherryPickLiquidTransfer) getEntity()).isFailed()) {
            _cherryPickRequestViewer.createNewAssayPlatesForFailed();
          }
          return _cherryPickRequestViewer.reload();
        }
      }
    }
    else if (getEntity() instanceof CherryPickScreening) {
      switch (editResult) {
        case CANCEL_EDIT:
          return getThisProxy().reload();
        case SAVE_EDIT:
          return getThisProxy().reload();
        case CANCEL_NEW:
          return _cherryPickRequestViewer.reload();
        case SAVE_NEW:
          return _cherryPickRequestViewer.reload();
      }
    } 
    return null;
  }
  
  @UICommand 
  public String browseLibrariesScreened()
  {
    if (getEntity() instanceof LibraryScreening) { 
      _librarySearchResults.searchLibrariesScreened((LibraryScreening) getEntity());
      return BROWSE_LIBRARIES;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
    
  @UICommand
  public String browsePlatesScreened()
  {
    if (getEntity() instanceof LibraryScreening) {
      _screenViewer.getPlateSearchResults().searchPlatesScreenedByLibraryScreening((LibraryScreening) getEntity());
      return BROWSE_PLATES_SCREENED;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
}
