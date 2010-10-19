// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.1.0-dev/src/edu/harvard/med/screensaver/ui/users/UserViewer.java $
// $Id: UserViewer.java 4484 2010-08-04 19:20:14Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.Observable;
import java.util.Observer;

import com.google.common.collect.Lists;
import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.dao.DataIntegrityViolationException;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.ui.EditResult;
import edu.harvard.med.screensaver.ui.EditableEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 */
public class LibraryCopyDetail extends EditableEntityViewerBackingBean<Copy>
{
  private static final VolumeUnit DEFAULT_PLATE_WELL_VOLUME_UNITS = VolumeUnit.MICROLITERS;

  private static Logger log = Logger.getLogger(LibraryCopyDetail.class);

  private LibraryCopyViewer _libraryCopyViewer;
  private LibraryViewer _libraryViewer;

  private UISelectOneBean<CopyUsageType> _copyUsageType;
  private UISelectOneBean<PlateType> _plateType;
  private String _location;
  private String facilityId;
  private UISelectOneBean<VolumeUnit> _volumeType;
  private String _volumeValue;
  private LocalDate _datePlated;
  private LocalDate _dateRetired;
  private String _comments;
  private Integer _startPlate;
  private Integer _endPlate;


  /**
   * @motivation for CGLIB2
   */
  protected LibraryCopyDetail()
  {
  }

  public LibraryCopyDetail(LibraryCopyDetail libraryCopyDetailProxy,
                           GenericEntityDAO dao,
                           LibraryCopyViewer libraryCopyViewer,
                           LibraryViewer libraryViewer)
  {
    super(libraryCopyDetailProxy,
          Copy.class,
          EDIT_LIBRARY_COPY,
          dao);
    _libraryCopyViewer = libraryCopyViewer;
    _libraryViewer = libraryViewer;
  }

  public UISelectOneBean<CopyUsageType> getCopyUsageType()
  {
    if (_copyUsageType == null) {
      _copyUsageType = new UISelectOneBean<CopyUsageType>(Lists.newArrayList(CopyUsageType.values()),
                                                          getEntity().isTransient() ? null : getEntity().getUsageType(),
                                                          getEntity().isTransient()) {
        @Override
        protected String getEmptyLabel()
        {
          return "<select>";
        }
      };
      _copyUsageType.addObserver(new Observer() {

        @Override
        public void update(Observable o, Object value)
        {
          getEntity().setUsageType(_copyUsageType.getSelection());
        }
      });
    }
    return _copyUsageType;
  }

  @Override
  protected void initializeViewer(Copy copy)
  {
    _copyUsageType = null;
    _plateType = null;
    _volumeValue = null;
    _volumeType = null;
    _location = null;
    facilityId = null;
    _datePlated = null;
    _dateRetired = null;
    _startPlate = copy.getLibrary().getStartPlate();
    _endPlate = copy.getLibrary().getEndPlate();
  }

  @Override
  protected void initializeEntity(Copy copy)
  {
    getDao().needReadOnly(copy, Copy.library.getPath());
  }
  
  @UICommand
  @Override
  public String delete()
  {
    String copyName = getEntity().getName();
    
    try {
      Library library = getEntity().getLibrary();
      getDao().deleteEntity(getEntity());
      showMessage("deletedEntity", "copy " + copyName);
      return _libraryViewer.viewEntity(library);
    }
    catch (DataIntegrityViolationException e) {
      showMessage("cannotDeleteEntityInUse", "Copy " + copyName);
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
  }

  @Override
  protected boolean validateEntity(Copy entity)
  {
    boolean valid = true;

    try {
      Volume.makeVolume(getVolumeValue(),
                        getVolumeType().getSelection());
    }
    catch (Exception e) {
      showFieldInputError("Volume", e.getLocalizedMessage());
      valid = false;
    }

    if (getEndPlate() < getStartPlate()) {
      showFieldInputError("To Plate #", "must be >= From Plate #");
      valid = false;
      int temp = getStartPlate();
      setStartPlate(getEndPlate());
      setEndPlate(temp);
    }
    if (getStartPlate() < entity.getLibrary().getStartPlate()) {
      showFieldInputError("From Plate #", "must be >= library start plate: " + entity.getLibrary().getStartPlate());
      valid = false;
      setStartPlate(entity.getLibrary().getStartPlate());
    }
    if (getEndPlate() > entity.getLibrary().getEndPlate()) {
      showFieldInputError("To Plate #", "must be <= library end plate: " + entity.getLibrary().getEndPlate());
      valid = false;
      setEndPlate(entity.getLibrary().getEndPlate());
    }

    return valid;
  }

  @Override
  protected void updateEntityProperties(Copy copy)
  {
    updatePlates();
  }

  private void updatePlates()
  {
    Copy copy = getEntity();
    IntRange plateRange = new IntRange(getStartPlate(), getEndPlate());
    for (Plate plate : copy.getPlates().values()) {
      if (plateRange.containsInteger(plate.getPlateNumber())) {
        if (!StringUtils.isEmpty(getVolumeValue())) {
          plate.setWellVolume(new Volume(getVolumeValue(),
                                         getVolumeType().getSelection()));
        }
        if (getPlateType().getSelection() != null) {
          plate.setPlateType(getPlateType().getSelection());
        }
        if (getDatePlated() != null) {
          plate.setDatePlated(getDatePlated());
        }
        if (getDateRetired() != null) {
          plate.setDateRetired(getDateRetired());
        }
        if (!StringUtils.isEmpty(getLocation())) {
          plate.setLocation(getLocation());
        }
        if (!StringUtils.isEmpty(getFacilityId())) {
          plate.setFacilityId(getFacilityId());
        }
        if (!StringUtils.isEmpty(getComments())) {
          plate.setComments(getComments());
        }
      }
    }
  }

  @Override
  protected String postEditAction(EditResult editResult)
  {
    switch (editResult) {
      case CANCEL_EDIT:
      case SAVE_EDIT:
        return _libraryCopyViewer.reload();
      case CANCEL_NEW:
      case SAVE_NEW:
        return _libraryViewer.viewEntity(getEntity().getLibrary());
      default:
        return null;
    }
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

  public UISelectOneBean<PlateType> getPlateType()
  {
    if (_plateType == null) {
      _plateType = new UISelectOneBean<PlateType>(Lists.newArrayList(PlateType.values()), null, true) {
        @Override
        protected String getEmptyLabel()
        {
          return getEntity().isTransient() ? super.getEmptyLabel() : "";
        }
      };
    }
    return _plateType;
  }

  public void setDatePlated(LocalDate value)
  {
    _datePlated = value;
  }

  public LocalDate getDatePlated()
  {
    return _datePlated;
  }

  public LocalDate getDateRetired()
  {
    return _dateRetired;
  }

  public void setDateRetired(LocalDate dateRetired)
  {
    _dateRetired = dateRetired;
  }

  public String getLocation()
  {
    return _location;
  }

  public void setLocation(String location)
  {
    _location = location;
  }

  public String getFacilityId()
  {
    return facilityId;
  }

  public void setFacilityId(String facilityId)
  {
    this.facilityId = facilityId;
  }

  public String getComments()
  {
    return _comments;
  }

  public void setComments(String comments)
  {
    _comments = comments;
  }

  public void setStartPlate(Integer startPlate)
  {
    _startPlate = startPlate;
  }

  public Integer getStartPlate()
  {
    return _startPlate;
  }

  public void setEndPlate(Integer endPlate)
  {
    _endPlate = endPlate;
  }

  public Integer getEndPlate()
  {
    return _endPlate;
  }
}
