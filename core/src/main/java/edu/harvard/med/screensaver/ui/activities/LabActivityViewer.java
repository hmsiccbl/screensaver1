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
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.EntityInflator;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.MolarUnit;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
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
import edu.harvard.med.screensaver.service.libraries.LibraryScreeningDerivedPropertiesUpdater;
import edu.harvard.med.screensaver.service.screens.ScreenDerivedPropertiesUpdater;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.EditResult;
import edu.harvard.med.screensaver.ui.arch.view.SearchResultContextEditableEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.cherrypickrequests.SelectableRow;
import edu.harvard.med.screensaver.ui.libraries.LibrarySearchResults;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;

public class LabActivityViewer extends SearchResultContextEditableEntityViewerBackingBean<LabActivity,LabActivity>
{
  private static Logger log = Logger.getLogger(LabActivityViewer.class);

  private LibrariesDAO _librariesDao;
  private ScreenDAO _screensDao;
  private ScreenViewer _screenViewer;
  private CherryPickRequestViewer _cherryPickRequestViewer;
  private LibrarySearchResults _librarySearchResults;
  private ScreenDerivedPropertiesUpdater _screenDerivedPropertiesUpdater;
  private LibraryScreeningDerivedPropertiesUpdater _libraryScreeningDerivedPropertiesUpdater;

  private Screen _screen;
  private UISelectOneEntityBean<ScreensaverUser> _performedBy;
  private UISelectOneBean<AssayProtocolType> _assayProtocolType;
  private UISelectOneBean<MolarUnit> _concentrationType;
  private UISelectOneBean<VolumeUnit> _volumeType;
  private String _concentrationValue;
  private String _volumeValue;
  private DataModel _cherryPickPlatesDataModel;

  private DataModel _platesScreenedDataModel;
  private Integer _newPlateRangeScreenedStartPlate;
  private Integer _newPlateRangeScreenedEndPlate;
  private UISelectOneBean<String> _newPlateRangeScreenedCopy;
  private List<Copy> _copies;

  private AbstractBackingBean _returnToViewAfterEdit;
  private boolean _editingNewEntity;

  /**
   * @motivation for CGLIB2
   */
  protected LabActivityViewer()
  {
  }

  public LabActivityViewer(LabActivityViewer thisProxy,
                           GenericEntityDAO dao,
                           LibrariesDAO librariesDao,
                           ScreenDAO screensDao,
                           ActivitySearchResults activitiesBrowser,
                           ScreenViewer screenViewer,
                           CherryPickRequestViewer cherryPickRequestViewer,
                           LibrarySearchResults librarySearchResults,
                           ScreenDerivedPropertiesUpdater screenDerivedPropertiesUpdater,
                           LibraryScreeningDerivedPropertiesUpdater libraryScreeningDerivedPropertiesUpdater)
  {
    super(thisProxy, LabActivity.class, BROWSE_ACTIVITIES, VIEW_ACTIVITY, dao, activitiesBrowser);
    _screenViewer = screenViewer;
    _cherryPickRequestViewer = cherryPickRequestViewer;
    _librariesDao = librariesDao;
    _screensDao = screensDao;
    _librarySearchResults = librarySearchResults;
    _screenDerivedPropertiesUpdater = screenDerivedPropertiesUpdater;
    _libraryScreeningDerivedPropertiesUpdater = libraryScreeningDerivedPropertiesUpdater;
  }

  @Override
  protected void initializeEntity(LabActivity activity)
  {
    Hibernate.initialize(activity.getPerformedBy());
    //getDao().needReadOnly(activity, Activity.performedBy);
    getDao().needReadOnly(activity, LabActivity.Screen);
    
    if (activity instanceof LibraryScreening) {
      getDao().needReadOnly(activity, LibraryScreening.Screen.to(Screen.assayPlates));
      getDao().needReadOnly((LibraryScreening) activity, LibraryScreening.assayPlatesScreened.to(AssayPlate.plateScreened).to(Plate.copy).to(Copy.library));
    }
    if (activity instanceof CherryPickScreening) {
      getDao().needReadOnly((CherryPickScreening) activity, CherryPickScreening.cherryPickRequest.to(CherryPickRequest.requestedBy));
      getDao().needReadOnly((CherryPickScreening) activity, CherryPickScreening.cherryPickAssayPlatesScreened);
    }
    if (activity instanceof CherryPickLiquidTransfer) {
      getDao().needReadOnly((CherryPickLiquidTransfer) activity, CherryPickLiquidTransfer.cherryPickAssayPlates.to(CherryPickAssayPlate.cherryPickRequest).to(CherryPickRequest.requestedBy));
    }
    _editingNewEntity = false;
  }
  
  @Override
  protected void initializeNewEntity(LabActivity activity)
  {
    // set null dateOfActivity, to force user to enter a valid date
    // TODO: this model shouldn't allow this null value, and we should really set to null at the UI component level only
    if (activity instanceof LibraryScreening) {
      activity.setDateOfActivity(null);
    }
    _editingNewEntity = true;
  }
  
  @Override
  protected void initializeViewer(LabActivity activity)
  {
    _screen = null;
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
  
  /**
   * @motivation hack to avoid Hibernate exceptions related to cases when the Activity.screen.{labHead,leadScreener} and
   *             Activity.performedBy relationships are the same entity; this method returns an independently retrieved
   *             Screen object from the LabActivity object, and so is not reattached when the Activity is saved/updated.
   */
  @Transactional
  public Screen getScreen()
  {
    if (_screen == null) {
      _screen = getDao().reloadEntity(getEntity().getScreen());
      getDao().needReadOnly(_screen, Screen.labHead);
      getDao().needReadOnly(_screen, Screen.leadScreener);
    }
    return _screen;
  }

  public UISelectOneBean<ScreensaverUser> getPerformedBy()
  {
    if (_performedBy == null) {
      _performedBy = new UISelectOneEntityBean<ScreensaverUser>(_screensDao.findLabActivityPerformedByCandidates(getEntity()),
                                                                getEntity().getPerformedBy(),
                                                                getDao()) {
        @Override
        protected String makeLabel(ScreensaverUser t)
        {
          return t.getFullNameLastFirst();
        }
      };
    }
    return _performedBy;
  }
  
  public UISelectOneBean<VolumeUnit> getVolumeTransferredPerWellType()
  {
    try {
      if (_volumeType == null) {
        Volume v = getEntity().getVolumeTransferredPerWell();
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
      Volume volumeTransferredPerWell = ((LabActivity) getEntity()).getVolumeTransferredPerWell();
      if (volumeTransferredPerWell != null) {
        _volumeValue = volumeTransferredPerWell.getDisplayValue().toString();
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

  public MolarConcentration getMolarConcentration()
  {
    return getEntity().getMolarConcentration();
  }
  
  public UISelectOneBean<MolarUnit> getMolarConcentrationType()
  {
    try {
      if (_concentrationType == null) {
        setMolarConcentrationType(getEntity().getMolarConcentrationUnits());
      }
      return _concentrationType;
    } catch (Exception e) {
      log.error("err: " + e);
      return null;
    }
  }

  private void setMolarConcentrationType( MolarUnit unit )
  {
    _concentrationType =
      new UISelectOneBean<MolarUnit>(MolarUnit.DISPLAY_VALUES, unit)
      {
        @Override
        protected String makeLabel(MolarUnit t) { return t.getValue(); }
      };
  }

  
  /**
   * This method exists to grab the value portion of the Quantity stored
  */
  public String getMolarConcentrationValue()
  {
    if (_concentrationValue == null) {
      _concentrationValue = getEntity().getMolarConcentrationValue();
    }
    return _concentrationValue;
  }

  
  /**
   * This method exists to set the value portion of the Quantity stored
   * @see #save()
  */
  public void setMolarConcentrationValue( String value )
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
  protected boolean validateEntity(LabActivity entity)
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
        MolarConcentration.makeConcentration(getMolarConcentrationValue(),
                                             getMolarConcentrationType().getSelection());
      } 
      catch (Exception e) {
        showFieldInputError("molarConcentration", e.getLocalizedMessage());
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
  protected void updateEntityProperties(LabActivity entity)
  {
    if (entity instanceof Screening) {
      ((Screening) entity).setVolumeTransferredPerWell(Volume.makeVolume(_volumeValue, _volumeType.getSelection()));
      ((Screening) entity).setMolarConcentration(MolarConcentration.makeConcentration(_concentrationValue, _concentrationType.getSelection()));
    }
    if (entity instanceof CherryPickScreening) {
      CherryPickScreening screening = (CherryPickScreening) entity;
      for (SelectableRow<CherryPickAssayPlate> row : (List<SelectableRow<CherryPickAssayPlate>>) getCherryPickPlatesDataModel().getWrappedData()) {
        if (row.isSelected() && !screening.getCherryPickAssayPlatesScreened().contains(row.getData())) {
          screening.addCherryPickAssayPlateScreened(getDao().mergeEntity(row.getData()));
        }
        else if (!row.isSelected() && screening.getCherryPickAssayPlatesScreened().contains(row.getData())) {
          screening.removeCherryPickAssayPlateScreened(getDao().mergeEntity(row.getData()));
        }
      }
    }
    if (entity instanceof CherryPickLiquidTransfer) {
      if (_editingNewEntity) {
        _screenDerivedPropertiesUpdater.updateTotalPlatedLabCherryPickCount(((CherryPickLiquidTransfer) entity).getScreen());
      }
    }
    if (entity instanceof LibraryScreening) {
      _libraryScreeningDerivedPropertiesUpdater.updateScreeningStatistics((LibraryScreening) entity);
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
        @Override
        protected String getEmptyLabel()
        {
          return ScreensaverConstants.REQUIRED_VOCAB_FIELD_PROMPT;
        }
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
        CherryPickRequest cpr =
          new EntityInflator<CherryPickRequest>(getDao(), ((CherryPickScreening) getEntity()).getCherryPickRequest(), true).
            need(CherryPickRequest.requestedBy).
            need(CherryPickRequest.screen).
            need(CherryPickRequest.cherryPickAssayPlates.to(CherryPickAssayPlate.cherryPickScreenings)).
            need(CherryPickRequest.cherryPickAssayPlates.to(CherryPickAssayPlate.cherryPickLiquidTransfer)).inflate();
        plates = cpr.getActiveCherryPickAssayPlates();
        selectedPlates = ((CherryPickScreening) getEntity()).getCherryPickAssayPlatesScreened();
      }
      else if (getEntity() instanceof CherryPickLiquidTransfer) {
        CherryPickLiquidTransfer cplt = (CherryPickLiquidTransfer) getEntity();
        if (cplt.getCherryPickRequest() != null) {
          CherryPickRequest cpr =
            new EntityInflator<CherryPickRequest>(getDao(), cplt.getCherryPickRequest(), true).
              need(CherryPickRequest.requestedBy).
              need(CherryPickRequest.screen).
              need(CherryPickRequest.cherryPickAssayPlates.to(CherryPickAssayPlate.cherryPickScreenings)).
              need(CherryPickRequest.cherryPickAssayPlates.to(CherryPickAssayPlate.cherryPickLiquidTransfer).to(CherryPickLiquidTransfer.cherryPickAssayPlates)).inflate();
          plates = cpr.getActiveCherryPickAssayPlates();
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
//                                   CopyUsageType.LIBRARY_SCREENING_PLATES,
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
                                           CopyUsageType.LIBRARY_SCREENING_PLATES, 
                                           true, 
                                           Copy.library);
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
        getNewPlateRangeScreenedCopy() != null) {
        Set<Plate> newPlates = Sets.newHashSet();
        Library library = null;
        if (getNewPlateRangeScreenedEndPlate() == null) {
          setNewPlateRangeScreenedEndPlate(getNewPlateRangeScreenedStartPlate());
        }
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
  public String browseLibraryPlatesScreened()
  {
    if (getEntity() instanceof LibraryScreening) {
      _screenViewer.getPlateSearchResults().searchLibraryPlatesScreenedByLibraryScreening((LibraryScreening) getEntity());
      return BROWSE_LIBRARY_PLATES_SCREENED;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
}