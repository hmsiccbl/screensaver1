// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/atolopko/2189/core/src/main/java/edu/harvard/med/screensaver/ui/activities/LabActivityViewer.java $
// $Id: LabActivityViewer.java 5920 2011-05-24 18:31:55Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.activities;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
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
import edu.harvard.med.screensaver.model.activities.Activity;
import edu.harvard.med.screensaver.model.activities.ServiceActivity;
import edu.harvard.med.screensaver.model.activities.ServiceActivityType;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateRange;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screens.AssayProtocolType;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.libraries.LibraryScreeningDerivedPropertiesUpdater;
import edu.harvard.med.screensaver.service.screens.ScreenDerivedPropertiesUpdater;
import edu.harvard.med.screensaver.ui.arch.util.JSFUtils;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.ui.arch.view.EditResult;
import edu.harvard.med.screensaver.ui.arch.view.SearchResultContextEditableEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.cherrypickrequests.SelectableRow;
import edu.harvard.med.screensaver.ui.libraries.LibrarySearchResults;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.users.UserViewer;
import edu.harvard.med.screensaver.util.StringUtils;

public class ActivityViewer extends SearchResultContextEditableEntityViewerBackingBean<Activity,Activity>
{
  private static Logger log = Logger.getLogger(ActivityViewer.class);

  private LibrariesDAO _librariesDao;
  private ScreenDAO _screenDao;
  private ScreenViewer _screenViewer;
  private UserViewer _userViewer;
  private CherryPickRequestViewer _cherryPickRequestViewer;
  private LibrarySearchResults _librarySearchResults;
  private ScreenDerivedPropertiesUpdater _screenDerivedPropertiesUpdater;
  private LibraryScreeningDerivedPropertiesUpdater _libraryScreeningDerivedPropertiesUpdater;

  private UISelectOneEntityBean<ScreensaverUser> _performedBy;
  private Screen _screen;
  private UISelectOneBean<AssayProtocolType> _assayProtocolType;
  private UISelectOneBean<MolarUnit> _concentrationType;
  private UISelectOneBean<VolumeUnit> _volumeTransferredPerWellToAssayPlatesType;
  private UISelectOneBean<VolumeUnit> _volumeTransferredPerWellFromLibraryPlatesType;
  private UISelectOneBean<VolumeUnit> _assayWellVolumeType;
  private String _concentrationValue;
  private String _volumeTransferredPerWellToAssayPlatesValue;
  private String _volumeTransferredPerWellFromLibraryPlatesValue;
  private String _assayWellVolumeValue;
  private DataModel _cherryPickPlatesDataModel;
  private DataModel _platesScreenedDataModel;
  private Integer _newPlateRangeScreenedStartPlate;
  private Integer _newPlateRangeScreenedEndPlate;
  private UISelectOneBean<String> _newPlateRangeScreenedCopy;
  private List<Copy> _copies;
  private TreeSet<Plate> _platesScreened;
  private boolean _editingNewEntity;
  private ScreeningRoomUser _servicedUser;
  private UISelectOneBean<Screen> _servicedScreen;

  /**
   * @motivation for CGLIB2
   */
  protected ActivityViewer()
  {
  }

  public ActivityViewer(ActivityViewer thisProxy,
                        GenericEntityDAO dao,
                        LibrariesDAO librariesDao,
                        ScreenDAO screenDao,
                        ActivitySearchResults activitiesBrowser,
                        ScreenViewer screenViewer,
                        UserViewer userViewer,
                        CherryPickRequestViewer cherryPickRequestViewer,
                        LibrarySearchResults librarySearchResults,
                        ScreenDerivedPropertiesUpdater screenDerivedPropertiesUpdater,
                        LibraryScreeningDerivedPropertiesUpdater libraryScreeningDerivedPropertiesUpdater)
  {
    super(thisProxy, Activity.class, BROWSE_ACTIVITIES, VIEW_ACTIVITY, dao, activitiesBrowser);
    _screenViewer = screenViewer;
    _userViewer = userViewer;
    _cherryPickRequestViewer = cherryPickRequestViewer;
    _librariesDao = librariesDao;
    _screenDao = screenDao;
    _librarySearchResults = librarySearchResults;
    _screenDerivedPropertiesUpdater = screenDerivedPropertiesUpdater;
    _libraryScreeningDerivedPropertiesUpdater = libraryScreeningDerivedPropertiesUpdater;
  }

  public UISelectOneBean<ScreensaverUser> getPerformedBy()
  {
    if (_performedBy == null) {
      _performedBy = new UISelectOneEntityBean<ScreensaverUser>(findPerformedByCandidates(),
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
  
  @Override
  final protected void updateEntityProperties(Activity entity)
  {
    if (getEntity() instanceof ServiceActivity) {
      getDao().needReadOnly((ServiceActivity) entity, ServiceActivity.servicedScreen);
      getDao().needReadOnly((ServiceActivity) entity, ServiceActivity.servicedUser);
      ((ServiceActivity) entity).setServicedScreen(getServicedScreen().getSelection());
    }
    else if (getEntity() instanceof LabActivity) {
      getDao().needReadOnly((LabActivity) entity, LabActivity.Screen);

      if (entity instanceof Screening) {
        ((Screening) entity).setVolumeTransferredPerWellToAssayPlates(
                                                                      Volume.makeVolume(_volumeTransferredPerWellToAssayPlatesValue, _volumeTransferredPerWellToAssayPlatesType.getSelection() )) ;//, RoundingMode.HALF_UP));
        // auto-calculate the volumeTransferredPerWellFromLibraryPlates property, if user left it blank
        if (StringUtils.isEmpty(_volumeTransferredPerWellFromLibraryPlatesValue) &&
          !!!StringUtils.isEmpty(_volumeTransferredPerWellToAssayPlatesValue)) {
          _volumeTransferredPerWellFromLibraryPlatesValue = ((Screening) entity).getVolumeTransferredPerWellToAssayPlates().getValue().multiply(new BigDecimal(((Screening) entity).getNumberOfReplicates())).toString();
          _volumeTransferredPerWellFromLibraryPlatesType.setSelection(_volumeTransferredPerWellToAssayPlatesType.getSelection());
        }
        ((Screening) entity).setVolumeTransferredPerWellFromLibraryPlates(
                                                                          Volume.makeVolume(_volumeTransferredPerWellFromLibraryPlatesValue, _volumeTransferredPerWellFromLibraryPlatesType.getSelection())); //, RoundingMode.HALF_UP));
        ((Screening) entity).setAssayWellVolume(Volume.makeVolume(_assayWellVolumeValue, _assayWellVolumeType.getSelection())); //, RoundingMode.HALF_UP));
        ((Screening) entity).setMolarConcentration(MolarConcentration.makeConcentration(_concentrationValue, _concentrationType.getSelection())); //, RoundingMode.HALF_UP));
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
        LibraryScreening libraryScreening = (LibraryScreening) entity;
        Set<Plate> originalPlates = Sets.newHashSet(Iterables.transform(libraryScreening.getAssayPlatesScreened(), AssayPlate.ToPlate));
        Set<Plate> deletedPlates = Sets.difference(originalPlates, _platesScreened);
        Set<Plate> addedPlates = Sets.difference(_platesScreened, originalPlates);
        for (Plate deletedPlate : deletedPlates) {
          libraryScreening.removeAssayPlatesScreened(deletedPlate);
        }
        for (Plate addedPlate : addedPlates) {
          libraryScreening.addAssayPlatesScreened(addedPlate);
        }
        _libraryScreeningDerivedPropertiesUpdater.updateScreeningStatistics(libraryScreening);
      }
    }
    // note: execute this after related entities are reattached, to avoid NonUniqueObjectExceptions
    entity.setPerformedBy(getPerformedBy().getSelection());
  }

  @Override
  protected void initializeEntity(Activity activity)
  {
    Hibernate.initialize(activity.getPerformedBy());
  
    if (activity instanceof ServiceActivity) {
      getDao().needReadOnly((ServiceActivity) activity, ServiceActivity.servicedScreen);
      getDao().needReadOnly((ServiceActivity) activity, ServiceActivity.servicedUser);
    }
    else if (activity instanceof LabActivity) {
      getDao().needReadOnly((LabActivity) activity, LabActivity.Screen);
      if (activity instanceof LibraryScreening) {
        getDao().needReadOnly((LibraryScreening) activity, LibraryScreening.Screen.to(Screen.assayPlates));
        getDao().needReadOnly((LibraryScreening) activity, LibraryScreening.assayPlatesScreened.to(AssayPlate.plateScreened).to(Plate.copy).to(Copy.library));
        _platesScreened = Sets.newTreeSet(Iterables.transform(((LibraryScreening) activity).getAssayPlatesScreened(), AssayPlate.ToPlate));
      }
      if (activity instanceof CherryPickScreening) {
        getDao().needReadOnly((CherryPickScreening) activity, CherryPickScreening.cherryPickRequest.to(CherryPickRequest.requestedBy));
        getDao().needReadOnly((CherryPickScreening) activity, CherryPickScreening.cherryPickAssayPlatesScreened);
      }
      if (activity instanceof CherryPickLiquidTransfer) {
        getDao().needReadOnly((CherryPickLiquidTransfer) activity, CherryPickLiquidTransfer.cherryPickAssayPlates.to(CherryPickAssayPlate.cherryPickRequest).to(CherryPickRequest.requestedBy));
      }
    }
    _editingNewEntity = false;
  }

  @Override
  protected void initializeNewEntity(Activity activity)
  {
    super.initializeNewEntity(activity);
  
    // set null dateOfActivity, to force user to enter a valid date
    // TODO: this model shouldn't allow this null value, and we should really set to null at the UI component level only
    if (activity instanceof LibraryScreening) {
      activity.setDateOfActivity(null);
      _platesScreened = Sets.newTreeSet();
    }
    _editingNewEntity = true;
  }

  @Override
  protected void initializeViewer(Activity activity)
  {
    _performedBy = null;
    _screen = null;
    _servicedScreen = null;
    _servicedUser = null;
    _cherryPickPlatesDataModel = null;
    _newPlateRangeScreenedStartPlate = null;
    _newPlateRangeScreenedEndPlate = null;
    _newPlateRangeScreenedCopy = null;
    _copies = null;
    _platesScreenedDataModel = null;
    _concentrationType = null;
    _concentrationValue = null;
    _volumeTransferredPerWellFromLibraryPlatesType = null;
    _volumeTransferredPerWellToAssayPlatesType = null;
    _volumeTransferredPerWellToAssayPlatesValue = null;
    _volumeTransferredPerWellFromLibraryPlatesType = null;
    _volumeTransferredPerWellFromLibraryPlatesValue = null;
    _assayWellVolumeValue = null;
    _assayWellVolumeType = null;
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
      if (getEntity() instanceof LabActivity) {
        _screen = getDao().reloadEntity(((LabActivity) getEntity()).getScreen());
      }
      else if (getEntity() instanceof ServiceActivity) {
        _screen = getDao().reloadEntity(((ServiceActivity) getEntity()).getServicedScreen());
      }
      if (_screen != null) {
        getDao().needReadOnly(_screen, Screen.labHead);
        getDao().needReadOnly(_screen, Screen.leadScreener);
      }
    }
    return _screen;
  }

  @Transactional
  public ScreeningRoomUser getServicedUser()
  {
    if (_servicedUser == null) {
      if (getEntity() instanceof ServiceActivity) {
        _servicedUser = getDao().reloadEntity(((ServiceActivity) getEntity()).getServicedUser());
      }
    }
    return _servicedUser;
  }

  public UISelectOneBean<VolumeUnit> getVolumeTransferredPerWellFromLibraryPlatesType()
  {
    if (_volumeTransferredPerWellFromLibraryPlatesType == null && getEntity() instanceof LabActivity) {
      _volumeTransferredPerWellFromLibraryPlatesType = initVolumeType(((LabActivity) getEntity()).getVolumeTransferredPerWellFromLibraryPlates());
    }
    return _volumeTransferredPerWellFromLibraryPlatesType;
    
  }

  public UISelectOneBean<VolumeUnit> getVolumeTransferredPerWellToAssayPlatesType()
  {
    if (_volumeTransferredPerWellToAssayPlatesType == null && getEntity() instanceof Screening) {
      _volumeTransferredPerWellToAssayPlatesType = initVolumeType(((Screening) getEntity()).getVolumeTransferredPerWellToAssayPlates());
    }
    return _volumeTransferredPerWellToAssayPlatesType;
  }

  private UISelectOneBean<VolumeUnit> initVolumeType(Volume initialVolume)
  {
    Volume v = initialVolume;
    VolumeUnit unit = (v == null ? VolumeUnit.NANOLITERS : v.getUnits());
    return new UISelectOneBean<VolumeUnit>(VolumeUnit.DISPLAY_VALUES, unit) {
      @Override
      protected String makeLabel(VolumeUnit t)
      {
        return t.getValue();
      }
    };
  }

  public String getVolumeTransferredPerWellToAssayPlatesValue()
  {
    if (_volumeTransferredPerWellToAssayPlatesValue == null) {
      Volume volumeTransferredPerWell = ((Screening) getEntity()).getVolumeTransferredPerWellToAssayPlates();
      if (volumeTransferredPerWell != null) {
        _volumeTransferredPerWellToAssayPlatesValue = volumeTransferredPerWell.getDisplayValue().toString();
      }
    }
    return _volumeTransferredPerWellToAssayPlatesValue;
  }

  public void setVolumeTransferredPerWellToAssayPlatesValue(String value)
  {
    _volumeTransferredPerWellToAssayPlatesValue = value;
  }

  public String getVolumeTransferredPerWellFromLibraryPlatesValue()
  {
    if (_volumeTransferredPerWellFromLibraryPlatesValue == null) {
      Volume volumeTransferredPerWell = ((LabActivity) getEntity()).getVolumeTransferredPerWellFromLibraryPlates();
      if (volumeTransferredPerWell != null) {
        _volumeTransferredPerWellFromLibraryPlatesValue = volumeTransferredPerWell.getDisplayValue().toString();
      }
    }
    return _volumeTransferredPerWellFromLibraryPlatesValue;
  }

  public void setVolumeTransferredPerWellFromLibraryPlatesValue(String value)
  {
    _volumeTransferredPerWellFromLibraryPlatesValue = value;
  }

  public MolarConcentration getMolarConcentration()
  {
    if (getEntity() instanceof LabActivity) {
      return ((LabActivity) getEntity()).getMolarConcentration();
    }
    return null;
  }

  public UISelectOneBean<MolarUnit> getMolarConcentrationType()
  {
    try {
      if (getEntity() instanceof LabActivity) {
        if (_concentrationType == null) {
          setMolarConcentrationType(((LabActivity) getEntity()).getMolarConcentrationUnits());
        }
      }
      return _concentrationType;
    } catch (Exception e) {
      log.error("err: " + e);
      return null;
    }
  }

  private void setMolarConcentrationType(MolarUnit unit)
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
    if (getEntity() instanceof LabActivity) {
      if (_concentrationValue == null) {
        _concentrationValue = ((LabActivity) getEntity()).getMolarConcentrationValue();
      }
    }
    return _concentrationValue;
  }

  /**
   * This method exists to set the value portion of the Quantity stored
   * @see #save()
  */
  public void setMolarConcentrationValue(String value)
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

  /**
   * This method exists to grab the value portion of the Quantity stored
   */
  public String getAssayWellVolumeValue()
  {
    if (_assayWellVolumeValue == null) {
      if (getEntity() instanceof Screening) {
        Volume volume = ((Screening) getEntity()).getAssayWellVolume();
        if (volume != null) {
          _assayWellVolumeValue = volume.getDisplayValue().toString();
        }
      }
    }
    return _assayWellVolumeValue;
  }

  /**
   * This method exists to set the value portion of the Quantity stored
   * 
   * @see #save()
   */
  public void setAssayWellVolumeValue(String value)
  {
    _assayWellVolumeValue = value;
  }

  public UISelectOneBean<VolumeUnit> getAssayWellVolumeType()
  {
    try {
      if (_assayWellVolumeType == null) {
        Volume v = (getEntity() instanceof Screening ?
                                    ((Screening) getEntity()).getAssayWellVolume() :
                                    null);
        VolumeUnit unit = (v == null ? VolumeUnit.NANOLITERS : v.getUnits());
  
        _assayWellVolumeType = new UISelectOneBean<VolumeUnit>(VolumeUnit.DISPLAY_VALUES, unit)
          {
            @Override
            protected String makeLabel(VolumeUnit t)
            {
              return t.getValue();
            }
          };
      }
      return _assayWellVolumeType;
    }
    catch (Exception e) {
      log.error("err: " + e);
      return null;
    }
  }

  @Override
  protected boolean validateEntity(Activity entity)
  {
    super.validateEntity(entity);
  
    boolean valid = true;
    if (entity instanceof Screening) {
      try {
        Volume.makeVolume(getVolumeTransferredPerWellToAssayPlatesValue(),
                          getVolumeTransferredPerWellToAssayPlatesType().getSelection());//, RoundingMode.HALF_UP);
      }
      catch (ArithmeticException e) {
        showFieldInputError("Volume Transferred Per Replicate To Assay Plates: value is out of range (1 nL to 1 L), rounding not allowed",  e.getLocalizedMessage());
        valid = false;
      }
      catch (Exception e) {
        showFieldInputError("Volume Transferred Per Replicate To Assay Plates", e.getLocalizedMessage());
        valid = false;
      }
      try {
        Volume.makeVolume(getVolumeTransferredPerWellToAssayPlatesValue(),
                          getVolumeTransferredPerWellFromLibraryPlatesType().getSelection());// , RoundingMode.HALF_UP);
      }
      catch (ArithmeticException e) {
        showFieldInputError("Volume Transferred Per Replicate From Library Plates: value is out of range (1 nL to 1 L), rounding not allowed",  e.getLocalizedMessage());
        valid = false;
      }
      catch (Exception e) {
        showFieldInputError("Volume Transferred Per Replicate From Library Plates", e.getLocalizedMessage());
        valid = false;
      }
      try {
        Volume.makeVolume(getAssayWellVolumeValue(),
                          getAssayWellVolumeType().getSelection());
      }
      catch (ArithmeticException e) {
        showFieldInputError("Assay Well Volume: value is out of range (1 nL to 1 L), rounding not allowed",  e.getLocalizedMessage());
        valid = false;
      }
      catch (Exception e) {
        showFieldInputError("Assay Well Volume", e.getLocalizedMessage());
        valid = false;
      }
      try {
        MolarConcentration.makeConcentration(getMolarConcentrationValue(),
                                             getMolarConcentrationType().getSelection()); //, RoundingMode.HALF_UP);
      }
      catch (ArithmeticException e) {
        showFieldInputError("Molar Concentration: value is out of range (1pM to 10 M), rounding not allowed",  e.getLocalizedMessage());
        valid = false;
      }
      catch (Exception e) {
        showFieldInputError("Molar Concentration", e.getLocalizedMessage());
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

  public DataModel getPlatesScreenedDataModel()
  {
    if (_platesScreenedDataModel == null) {
      if (getEntity() instanceof LibraryScreening) {
        _platesScreenedDataModel = new ListDataModel(PlateRange.splitIntoPlateCopyRanges(_platesScreened));
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
        showMessage("activities.numberOfReplicatesRequired");
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
      if (getNewPlateRangeScreenedCopy().getSelection() == null) {
        showMessage("activities.copyRequired");
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
      if (getNewPlateRangeScreenedStartPlate() != null &&
        getNewPlateRangeScreenedCopy() != null) {
        Set<Library> librariesInPlateRange = Sets.newLinkedHashSet();
        if (getNewPlateRangeScreenedEndPlate() == null) {
          setNewPlateRangeScreenedEndPlate(getNewPlateRangeScreenedStartPlate());
        }
        if (getNewPlateRangeScreenedEndPlate() < getNewPlateRangeScreenedStartPlate()) {
          showMessage("activities.plateRangePlateNumbersReversed", getNewPlateRangeScreenedStartPlate(), getNewPlateRangeScreenedEndPlate());
          return REDISPLAY_PAGE_ACTION_RESULT;
        }
        for (int plateNumber = getNewPlateRangeScreenedStartPlate(); plateNumber <= getNewPlateRangeScreenedEndPlate(); ++plateNumber) {
          final Plate plate = _librariesDao.findPlate(plateNumber, getNewPlateRangeScreenedCopy().getSelection());
          if (plate == null) {
            showMessage("activities.unknownPlateNumberAndOrCopy", plateNumber, getNewPlateRangeScreenedCopy().getSelection());
            return REDISPLAY_PAGE_ACTION_RESULT;
          }
          librariesInPlateRange.add(plate.getCopy().getLibrary());
          if (_platesScreened.contains(plate)) {
            showMessage("activities.plateNumberDuplicated", plate.getPlateNumber());
            return REDISPLAY_PAGE_ACTION_RESULT;
          }
          _platesScreened.add(plate);
        }
        if (librariesInPlateRange.size() > 1) {
          showMessage("activities.plateRangeSpansMultipleLibrariesWarning",
                      getNewPlateRangeScreenedStartPlate(),
                      getNewPlateRangeScreenedEndPlate(),
                      Joiner.on(", ").join(Iterables.transform(librariesInPlateRange, Library.ToShortName)));
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
  @Transactional
  public String deletePlateRange()
  {
    if (getEntity() instanceof LibraryScreening) {
      PlateRange plateRange = (PlateRange) getPlatesScreenedDataModel().getRowData();
      for (Plate plate : plateRange) {
        _platesScreened.remove(plate);
        //libraryScreening.removeAssayPlatesScreened(plate);
      }
      _platesScreenedDataModel = null;
      //      getDao().mergeEntity(getEntity().getScreen());
      //      save(); // save NOW! (see above)
      //      edit(); // resume edit mode
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
    else if (getEntity() instanceof ServiceActivity) {
      switch (editResult) {
        case CANCEL_EDIT:
          return getThisProxy().reload();
        case SAVE_EDIT:
          return getThisProxy().reload();
        case CANCEL_NEW:
          return _userViewer.reload();
        case SAVE_NEW:
          return _userViewer.reload();
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

  private Set<ScreensaverUser> findPerformedByCandidates()
  {
    if (getEntity() instanceof LabActivity) {
      return _screenDao.findLabActivityPerformedByCandidates((LabActivity) getEntity());
    }
    else if (getEntity() instanceof ServiceActivity) {
      return findServiceActivityAdminUsers();
    }
    return ImmutableSet.of();
  }
  
  // TODO: refactor this into a UsersDAO method: findUsersInRole() 
  private Set<ScreensaverUser> findServiceActivityAdminUsers()
  {
    String hql = "from ScreensaverUser where ? in elements (screensaverUserRoles)";
    return Sets.newTreeSet(getDao().findEntitiesByHql(ScreensaverUser.class, hql, ScreensaverUserRole.SERVICE_ACTIVITY_ADMIN.getRoleName()));
  }  

  public List<SelectItem> getServiceActivityTypeSelectItems()
  {
    return JSFUtils.createUISelectItemsWithEmptySelection(Arrays.asList(ServiceActivityType.values()),
                                                          "<select>");
  }

  @Transactional
  public UISelectOneBean<Screen> getServicedScreen()
  {
    if (getEntity() instanceof ServiceActivity && _servicedScreen == null && getServicedUser() != null) {
      Set<Screen> associatedScreens = getDao().reloadEntity(_servicedUser, true).getAllAssociatedScreens();
      _servicedScreen = new UISelectOneEntityBean<Screen>(associatedScreens, ((ServiceActivity) getEntity()).getServicedScreen(), true, getDao()) {
        @Override
        protected String makeLabel(Screen screen)
        {
          return screen.getFacilityId() + ": " + screen.getTitle();
        }
      };
    }
    return _servicedScreen;
  }
}
