// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import javax.faces.model.SelectItem;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.MolarUnit;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateLocation;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.libraries.PlateUpdater;
import edu.harvard.med.screensaver.ui.arch.util.JSFUtils;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.util.NullSafeUtils;
import edu.harvard.med.screensaver.util.StringUtils;

public class LibraryCopyPlateBatchEditor extends AbstractBackingBean
{
  public class LocationPartUISelectOneBean extends UISelectOneBean<String>
  {
    private String _locationPartName;

    public LocationPartUISelectOneBean(String locationPartName)
    {
      super(Sets.newTreeSet(getDao().<PlateLocation,String>findDistinctPropertyValues(PlateLocation.class, locationPartName)),
            null,
            true);
      _locationPartName = locationPartName;
    }
    
    public void refresh()
    {
      setDomain(getDao().<PlateLocation,String>findDistinctPropertyValues(PlateLocation.class, _locationPartName), 
                getSelection());
    }

    @Override
    protected String getEmptyLabel()
    {
      return NO_CHANGE;
    }
  }

  private static final Logger log = Logger.getLogger(LibraryCopyPlateBatchEditor.class);

  private static final String NO_CHANGE = "<no change>";
  private static final VolumeUnit DEFAULT_PLATE_WELL_VOLUME_UNITS = VolumeUnit.MICROLITERS;

  private static final MolarUnit DEFAULT_PLATE_MOLAR_CONCENTRATION_UNITS = MolarUnit.MICROMOLAR;

  private PlateUpdater _plateUpdater;
  private GenericEntityDAO _dao;

  private UISelectOneBean<PlateType> _plateType;
  private UISelectOneBean<PlateStatus> _plateStatus;
  private ActivityDTO _statusChangeActivity;
  private LocationPartUISelectOneBean _room;
  private LocationPartUISelectOneBean _freezer;
  private LocationPartUISelectOneBean _shelf;
  private LocationPartUISelectOneBean _bin;
  private String _newPlateLocationRoom;
  private String _newPlateLocationFreezer;
  private String _newPlateLocationShelf;

  private String _newPlateLocationBin;
  private ActivityDTO _locationChangeActivity;
  private UISelectOneBean<VolumeUnit> _volumeType;
  private String _volumeValue;
  private UISelectOneBean<MolarUnit> _molarConcentrationType;
  private String _molarConcentrationValue;
  private BigDecimal _mgMlConcentration;
  private String _comments;


  /** @motivation for CGLIB2 */
  protected LibraryCopyPlateBatchEditor()
  {}

  public LibraryCopyPlateBatchEditor(GenericEntityDAO dao,
                                     PlateUpdater plateUpdater)
  {
    super();
    _dao = dao;
    _plateUpdater = plateUpdater;
    getIsPanelCollapsedMap().put("batchEdit", true);
  }

  public void initialize()
  {
    _plateType = null;
    _plateStatus = null;
    _statusChangeActivity = new ActivityDTO();
    _statusChangeActivity.setDateOfActivity(new LocalDate());
    _statusChangeActivity.setPerformedBy(getScreensaverUser());
    _volumeValue = null;
    _volumeType = null;
    _molarConcentrationValue = null;
    _molarConcentrationType = null;
    _room = null;
    _freezer = null;
    _shelf = null;
    _bin = null;
    _locationChangeActivity = new ActivityDTO();
    _locationChangeActivity.setDateOfActivity(new LocalDate());
    _locationChangeActivity.setPerformedBy(getScreensaverUser());
    _newPlateLocationRoom = null;
    _newPlateLocationFreezer = null;
    _newPlateLocationShelf = null;
    _newPlateLocationBin = null;
    _comments = null;
  }

  private boolean validate()
  {
    boolean valid = true;

    try {
      Volume.makeVolume(getVolumeValue(),
                        getVolumeType().getSelection());
    }
    catch (Exception e) {
      showMessage("invalidUserInput", "Volume", e.getLocalizedMessage());
      valid = false;
    }

    try {
      MolarConcentration.makeConcentration(getMolarConcentrationValue(),
                                           getMolarConcentrationType().getSelection());
    }
    catch (Exception e) {
      showMessage("invalidUserInput", "Concentration (molar)", e.getLocalizedMessage());
      valid = false;
    }

    if (getPlateStatus().getSelection() != null) {
      if (_statusChangeActivity.getPerformedBy() == null) {
        showMessage("requiredValue", "plate status change performed by");
        valid = false;
      }
      if (_statusChangeActivity.getDateOfActivity() == null) {
        showMessage("requiredValue", "plate status change date");
        valid = false;
      }
    }

    if (getRoom().getSelection() != null || getNewPlateLocationRoom() != null ||
      getFreezer().getSelection() != null || getNewPlateLocationFreezer() != null ||
      getShelf().getSelection() != null || getNewPlateLocationShelf() != null ||
      getBin().getSelection() != null || getNewPlateLocationBin() != null) {
      if (_locationChangeActivity.getPerformedBy() == null) {
        showMessage("requiredValue", "plate location change performed by");
        valid = false;
      }
      if (_locationChangeActivity.getDateOfActivity() == null) {
        showMessage("requiredValue", "plate location change date");
        valid = false;
      }
    }

    return valid;
  }

  @UICommand
  @Transactional
  public boolean updatePlates(Set<Plate> plates)
  {
    ScreensaverUser screensaverUser = getCurrentScreensaverUser().getScreensaverUser();
    if (!(screensaverUser instanceof AdministratorUser) ||
      !((AdministratorUser) screensaverUser).isUserInRole(ScreensaverUserRole.LIBRARY_COPIES_ADMIN)) {
      throw new BusinessRuleViolationException("only library copies administrators can edit library copy plates");
    }
    if (!validate()) {
      return false;
    }

    int modifiedCount = 0;
    AdministratorUser adminUser = getDao().reloadEntity((AdministratorUser) getScreensaverUser());
    for (Plate plate : plates) {
      boolean modified = false;
      if (!StringUtils.isEmpty(getVolumeValue())) {
        Volume newVolume = new Volume(getVolumeValue(), getVolumeType().getSelection());
        modified |= _plateUpdater.updateWellVolume(plate, newVolume, adminUser);
      }
      if (!StringUtils.isEmpty(getMolarConcentrationValue())) {
        MolarConcentration newConcentration = new MolarConcentration(getMolarConcentrationValue(), getMolarConcentrationType().getSelection());
        modified |= _plateUpdater.updateMolarConcentration(plate, newConcentration, adminUser);
      }
      if (getMgMlConcentration() != null) {
        modified |= _plateUpdater.updateMgMlConcentration(plate, getMgMlConcentration(), adminUser);
      }
      if (getPlateType().getSelection() != null) {
        modified |= _plateUpdater.updatePlateType(plate, getPlateType().getSelection(), adminUser);
      }
      if (getPlateStatus().getSelection() != null) {
        modified |= _plateUpdater.updatePlateStatus(plate,
                                                    getPlateStatus().getSelection(),
                                                    adminUser,
                                                    (AdministratorUser) _statusChangeActivity.getPerformedBy(),
                                                    _statusChangeActivity.getDateOfActivity());
      }
      if (!StringUtils.isEmpty(_comments)) {
        _plateUpdater.addComment(plate, adminUser, _comments);
        modified = true;
      }
      PlateLocation newLocation = makeNewLocation(plate);
      if (newLocation != null) {
        modified |= _plateUpdater.updatePlateLocation(plate,
                                                      newLocation,
                                                      adminUser,
                                                      (AdministratorUser) _locationChangeActivity.getPerformedBy(),
                                                      _locationChangeActivity.getDateOfActivity());
      }
      if (modified) {
        // note: if no other properties were modified, the facility ID will not be modified, 
        // even if it is not up-to-date; this is less confusing to the user, who would 
        // otherwise expect only properties with specified values to affect the modified count
        _plateUpdater.updateFacilityId(plate, adminUser);
      }

      if (modified) {
        ++modifiedCount;
        getDao().flush(); // HACK: due to commented-out flush() in PlateUpdate.findPlateLocation()
      }
    }
    _plateUpdater.validateLocations();
    showMessage("libraries.updatedPlates", modifiedCount, plates.size());
    getRoom().refresh();
    getFreezer().refresh();
    getShelf().refresh();
    getBin().refresh();
    return true;
  }

  private PlateLocation makeNewLocation(Plate plate)
  {
    PlateLocation oldLocation = plate.getLocation();
    String room = getNewPlateLocationRoom() != null ? getNewPlateLocationRoom()
      : getRoom().getSelection() != null
      ? getRoom().getSelection() : null;
    String freezer = getNewPlateLocationFreezer() != null ? getNewPlateLocationFreezer()
      : getFreezer().getSelection() != null ? getFreezer().getSelection() : null;
    String shelf = getNewPlateLocationShelf() != null ? getNewPlateLocationShelf()
      : getShelf().getSelection() != null
      ? getShelf().getSelection() : null;
    String bin = getNewPlateLocationBin() != null ? getNewPlateLocationBin()
      : getBin().getSelection() != null
      ? getBin().getSelection() : null;

    if (room == null && freezer == null && shelf == null && bin == null) {
      return null;
    }

    if (oldLocation != null) {
      if (room == null) {
        room = oldLocation.getRoom();
      }
      if (freezer == null) {
        freezer = oldLocation.getFreezer();
      }
      if (shelf == null) {
        shelf = oldLocation.getShelf();
      }
      if (bin == null) {
        bin = oldLocation.getBin();
      }

      if (NullSafeUtils.nullSafeEquals(room, oldLocation.getRoom()) &&
        NullSafeUtils.nullSafeEquals(freezer, oldLocation.getFreezer()) &&
        NullSafeUtils.nullSafeEquals(shelf, oldLocation.getShelf()) &&
        NullSafeUtils.nullSafeEquals(bin, oldLocation.getBin())) {
        // no change
        return null;
      }
    }
    else {
      room = room == null ? PlateUpdater.NO_ROOM : room;
      freezer = freezer == null ? PlateUpdater.NO_FREEZER : freezer;
      shelf = shelf == null ? PlateUpdater.NO_SHELF : shelf;
      bin = bin == null ? PlateUpdater.NO_BIN : bin;
    }

    PlateLocation newLocation = new PlateLocation(room, freezer, shelf, bin);
    return newLocation;
  }

  private GenericEntityDAO getDao()
  {
    return _dao;
  }

  public String getVolumeValue()
  {
    return _volumeValue;
  }

  public void setVolumeValue(String value)
  {
    _volumeValue = value;
  }

  public UISelectOneBean<VolumeUnit> getVolumeType()
  {
    try {
      if (_volumeType == null) {
        Volume v = null;
        VolumeUnit unit = (v == null ? DEFAULT_PLATE_WELL_VOLUME_UNITS : v.getUnits());

        _volumeType = new UISelectOneBean<VolumeUnit>(VolumeUnit.DISPLAY_VALUES, unit)
        {
          @Override
          protected String makeLabel(VolumeUnit t)
          {
            return t.getValue();
          }
        };
      }
      return _volumeType;
    }
    catch (Exception e) {
      log.error("err: " + e);
      return null;
    }
  }

  public String getMolarConcentrationValue()
  {
    return _molarConcentrationValue;
  }

  public void setMolarConcentrationValue(String value)
  {
    _molarConcentrationValue = value;
  }

  public UISelectOneBean<MolarUnit> getMolarConcentrationType()
  {
    try {
      if (_molarConcentrationType == null) {
        MolarConcentration c = null;
        MolarUnit unit = (c == null ? DEFAULT_PLATE_MOLAR_CONCENTRATION_UNITS : c.getUnits());

        _molarConcentrationType = new UISelectOneBean<MolarUnit>(MolarUnit.DISPLAY_VALUES, unit)
        {
          @Override
          protected String makeLabel(MolarUnit t)
          {
            return t.getValue();
          }
        };
      }
      return _molarConcentrationType;
    }
    catch (Exception e) {
      log.error("err: " + e);
      return null;
    }
  }

  public BigDecimal getMgMlConcentration()
  {
    return _mgMlConcentration;
  }

  public void setMgMlConcentration(BigDecimal mgMlConcentration)
  {
    _mgMlConcentration = mgMlConcentration;
  }

  public UISelectOneBean<PlateType> getPlateType()
  {
    if (_plateType == null) {
      _plateType = new UISelectOneBean<PlateType>(Lists.newArrayList(PlateType.values()), null, true) {
        @Override
        protected String getEmptyLabel()
        {
          return NO_CHANGE;
        }
      };
    }
    return _plateType;
  }

  public UISelectOneBean<PlateStatus> getPlateStatus()
  {
    if (_plateStatus == null) {
      _plateStatus = new UISelectOneBean<PlateStatus>(Lists.newArrayList(PlateStatus.values()), null, true) {
        @Override
        protected String getEmptyLabel()
        {
          return NO_CHANGE;
        }
      };
    }
    return _plateStatus;
  }

  public ActivityDTO getStatusChangeActivity()
  {
    return _statusChangeActivity;
  }

  public ActivityDTO getLocationChangeActivity()
  {
    return _locationChangeActivity;
  }

  public LocationPartUISelectOneBean getRoom()
  {
    if (_room == null) {
      _room = new LocationPartUISelectOneBean("room");
    }

    return _room;
  }

  public LocationPartUISelectOneBean getFreezer()
  {
    if (_freezer == null) {
      _freezer = new LocationPartUISelectOneBean("freezer");
    }

    return _freezer;
  }

  public LocationPartUISelectOneBean getShelf()
  {
    if (_shelf == null) {
      _shelf = new LocationPartUISelectOneBean("shelf");
    }
    return _shelf;
  }

  public LocationPartUISelectOneBean getBin()
  {
    if (_bin == null) {
      _bin = new LocationPartUISelectOneBean("bin");
    }

    return _bin;
  }

  public List<SelectItem> getAdministratorUserSelectItems()
  {
    return JSFUtils.createUISelectItemsWithEmptySelection(getDao().findAllEntitiesOfType(AdministratorUser.class),
                                                          ScreensaverConstants.REQUIRED_VOCAB_FIELD_PROMPT,
                                                          ScreensaverUser.ToDisplayStringFunction);
  }

  public String getComments()
  {
    return _comments;
  }

  public void setComments(String comments)
  {
    _comments = comments;
  }

  public String getNewPlateLocationRoom()
  {
    return _newPlateLocationRoom;
  }

  public void setNewPlateLocationRoom(String newPlateLocationRoom)
  {
    _newPlateLocationRoom = newPlateLocationRoom;
  }

  public String getNewPlateLocationFreezer()
  {
    return _newPlateLocationFreezer;
  }

  public void setNewPlateLocationFreezer(String newPlateLocationFreezer)
  {
    _newPlateLocationFreezer = newPlateLocationFreezer;
  }

  public String getNewPlateLocationShelf()
  {
    return _newPlateLocationShelf;
  }

  public void setNewPlateLocationShelf(String newPlateLocationShelf)
  {
    _newPlateLocationShelf = newPlateLocationShelf;
  }

  public String getNewPlateLocationBin()
  {
    return _newPlateLocationBin;
  }

  public void setNewPlateLocationBin(String newPlateLocationBin)
  {
    _newPlateLocationBin = newPlateLocationBin;
  }
}

