// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

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
import edu.harvard.med.screensaver.model.screens.AssayProtocolType;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.PlatesUsed;
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
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;

public class ActivityViewer extends SearchResultContextEditableEntityViewerBackingBean<Activity,Activity>
{
  // static members

  private static Logger log = Logger.getLogger(ActivityViewer.class);

  // instance data members

  private LibrariesDAO _librariesDao;
  private ScreenViewer _screenViewer;
  private CherryPickRequestViewer _cherryPickRequestViewer;
  private CherryPickRequestAllocator _cherryPickRequestAllocator;

  private UISelectOneEntityBean<ScreensaverUser> _performedBy;
  private UISelectOneBean<AssayProtocolType> _assayProtocolType;
  private UISelectOneBean<ConcentrationUnit> _concentrationType;
  private UISelectOneBean<VolumeUnit> _volumeType;
  private String _concentrationValue;
  private String _volumeValue;
  private DataModel _libraryAndPlatesScreenedDataModel;
  private DataModel _cherryPickPlatesDataModel;
  private PlatesUsed _newPlatesScreened;
  private AbstractBackingBean _returnToViewAfterEdit;

  private boolean _editingNewEntity;

  
  // constructors

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
                        CherryPickRequestAllocator cherryPickRequestAllocator)
  {
    super(thisProxy, Activity.class, BROWSE_ACTIVITIES, VIEW_ACTIVITY, dao, activitiesBrowser);
    _screenViewer = screenViewer;
    _cherryPickRequestViewer = cherryPickRequestViewer;
    _librariesDao = librariesDao;
    _cherryPickRequestAllocator = cherryPickRequestAllocator;
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
      getDao().needReadOnly(activity, "platesUsed");
    }
    if (activity instanceof CherryPickScreening) {
      getDao().needReadOnly(activity, CherryPickScreening.cherryPickRequest.to(CherryPickRequest.cherryPickAssayPlates).to(CherryPickAssayPlate.cherryPickLiquidTransfer).getPath());
      getDao().needReadOnly(activity, CherryPickScreening.assayPlatesScreened.getPath());
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
    }
    _editingNewEntity = true;
  }
  
  protected void initializeViewer(Activity activity)
  {
    _performedBy = null;
    _libraryAndPlatesScreenedDataModel = null;
    _cherryPickPlatesDataModel = null;
    _newPlatesScreened = null;
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
    if( _volumeValue == null )
    {
      _volumeValue =
        getEntity() instanceof LabActivity ? ((LabActivity) getEntity()).getVolumeTransferredPerWellValue(): null;
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

  public PlatesUsed getNewPlatesScreened()
  {
    if (_newPlatesScreened == null) {
      _newPlatesScreened = new PlatesUsed();
    }
    return _newPlatesScreened;
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
        if (row.isSelected()  && !((CherryPickScreening) entity).getAssayPlatesScreened().contains(row.getData())) {
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
        if (row.isSelected() && !screening.getAssayPlatesScreened().contains(row.getData())) {
          screening.addAssayPlateScreened(row.getData());
        }
        else if (!row.isSelected() && screening.getAssayPlatesScreened().contains(row.getData())) {
          screening.removeAssayPlateScreened(row.getData());
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
      ((LibraryScreening) entity).getScreen().update();
      entity.update();
    }
    // note: execute this after parent entity (Screen/CPR) is reattached, to avoid NonUniqueObjectExceptions
    entity.setPerformedBy(getPerformedBy().getSelection());
  }

  public DataModel getLibraryAndPlatesScreenedDataModel()
  {
    if (_libraryAndPlatesScreenedDataModel == null) {
      List<LibraryAndPlatesUsed> libraryAndPlatesUsed = new ArrayList<LibraryAndPlatesUsed>();
      if (getEntity() instanceof LibraryScreening) {
        for (PlatesUsed platesUsed : ((LibraryScreening) getEntity()).getPlatesUsed()) {
          libraryAndPlatesUsed.add(new LibraryAndPlatesUsed(_librariesDao, platesUsed));
        }
      }
      _libraryAndPlatesScreenedDataModel = new ListDataModel(libraryAndPlatesUsed);
    }
    return _libraryAndPlatesScreenedDataModel;
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
        selectedPlates = cherryPickScreening.getAssayPlatesScreened();
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

  @UICommand
  public String addPlatesScreened()
  {
    if (getEntity() instanceof LibraryScreening) {
      LibraryScreening libraryScreening = (LibraryScreening) getEntity();
      if (getNewPlatesScreened().getStartPlate() != null &&
        getNewPlatesScreened().getCopyName() != null &&
        getNewPlatesScreened().getCopyName().length() != 0) {
        if (getNewPlatesScreened().getEndPlate() == null) {
          getNewPlatesScreened().setEndPlate(getNewPlatesScreened().getStartPlate());
        }
        for (PlatesUsed platesUsed : splitPlateRangeByLibrary(getNewPlatesScreened())) {
          libraryScreening.createPlatesUsed(platesUsed.getStartPlate(),
                                            platesUsed.getEndPlate(),
                                            platesUsed.getCopy());
        }
      }
        _libraryAndPlatesScreenedDataModel = null;
        _newPlatesScreened = null;
      }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  private Set<PlatesUsed> splitPlateRangeByLibrary(PlatesUsed platesUsed)
  {
    int startPlate = platesUsed.getStartPlate();
    int endPlate = platesUsed.getEndPlate();
    Library libraryWithEndPlate = _librariesDao.findLibraryWithPlate(endPlate);
    if (libraryWithEndPlate == null) {
      reportApplicationError("plate " + endPlate + " is not contained in any library");
      return Collections.emptySet();
    }
    Set<PlatesUsed> result = new HashSet<PlatesUsed>();
    do {
      Library libraryWithStartPlate = _librariesDao.findLibraryWithPlate(startPlate);
      if (libraryWithStartPlate == null) {
        reportApplicationError("plate " + endPlate + " is not contained in any library");
        return Collections.emptySet();
      }
      PlatesUsed platesUsed2 = new PlatesUsed();
      platesUsed2.setStartPlate(startPlate);
      platesUsed2.setEndPlate(Math.min(libraryWithStartPlate.getEndPlate(), endPlate));
      platesUsed2.setCopy(new Copy(libraryWithStartPlate,
                                   CopyUsageType.FOR_LIBRARY_SCREENING,
                                   platesUsed.getCopyName()));
      result.add(platesUsed2);
      startPlate = libraryWithStartPlate.getEndPlate() + 1;
    } while (startPlate <= endPlate);
    return result;
  }

  @UICommand
  public String deletePlatesScreened()
  {
    if (getEntity() instanceof LibraryScreening) {
      LibraryScreening libraryScreening = (LibraryScreening) getEntity();
      LibraryAndPlatesUsed libraryAndPlatesUsed = (LibraryAndPlatesUsed) getLibraryAndPlatesScreenedDataModel().getRowData();
      libraryScreening.deletePlatesUsed(libraryAndPlatesUsed.getPlatesUsed());
      _libraryAndPlatesScreenedDataModel = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public List<String> getAllCopies()
  {
    // TODO: master copies list to be acquired from database
    return Arrays.asList("", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "HA", "HB", "HC", "HD", "HE", "HF", "HG", "HH", "HI", "HJ", "H Stock", "MA", "MB", "MC", "MD", "ME", "MF", "MG", "MH", "MI", "MJ", "M Stock", "St A", "St B", "St C", "St D", "St E");
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
}
