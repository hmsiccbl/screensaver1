// $HeadURL:
// http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/2920-rev2/core/src/main/java/edu/harvard/med/screensaver/ui/libraries/LibraryCopyDetail.java
// $
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
import java.util.Arrays;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.collect.ImmutableSet;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.MolarUnit;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.libraries.PlateUpdater;
import edu.harvard.med.screensaver.ui.arch.util.JSFUtils;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.arch.view.EditResult;
import edu.harvard.med.screensaver.ui.arch.view.EditableEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.util.NullSafeUtils;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 */
public class LibraryCopyDetail extends EditableEntityViewerBackingBean<Copy>
{
  private static Logger log = Logger.getLogger(LibraryCopyDetail.class);

  private static final MolarUnit DEFAULT_PLATE_MOLAR_CONCENTRATION_UNITS 
    = MolarUnit.MILLIMOLAR;

  private LibraryCopyViewer _libraryCopyViewer;
  private LibraryViewer _libraryViewer;
  private PlateUpdater _plateUpdater;

  private LibrariesDAO _librariesDao;

  private UISelectOneBean<MolarUnit> _molarConcentrationType;
  private String _molarConcentrationValue;
  private BigDecimal _mgMlConcentration;
  private BigDecimal _dilutionFactor;

  /**
   * @motivation for CGLIB2
   */
  protected LibraryCopyDetail()
  {}

  public LibraryCopyDetail(LibraryCopyDetail libraryCopyDetailProxy,
                           GenericEntityDAO dao,
                           LibrariesDAO librariesDao,
                           LibraryCopyViewer libraryCopyViewer,
                           LibraryViewer libraryViewer,
                           PlateUpdater plateUpdater)
  {
    super(libraryCopyDetailProxy,
          Copy.class,
          EDIT_LIBRARY_COPY,
          dao);
    _librariesDao = librariesDao;
    _libraryCopyViewer = libraryCopyViewer;
    _libraryViewer = libraryViewer;
    _plateUpdater = plateUpdater;
    _molarConcentrationValue = null;
    _molarConcentrationType = null;
    _mgMlConcentration = null;
    _dilutionFactor = null;
  }

  @Override
  protected void initializeViewer(Copy entity)
  {}

  @Override
  protected void initializeEntity(Copy copy)
  {
    getDao().needReadOnly(copy, Copy.library);
    _librariesDao.calculateCopyVolumeStatistics(ImmutableSet.of(copy));
    _molarConcentrationValue = null;
    _molarConcentrationType = null;
    _mgMlConcentration = null;
    _dilutionFactor = null;
  }

  @Override
  protected boolean validateEntity(Copy entity)
  {
    boolean valid = true;

    if (getWellConcentrationDilutionFactor() != null 
          && !StringUtils.isEmpty(getMolarConcentrationValue()) ||
        getWellConcentrationDilutionFactor() != null 
          && getMgMlConcentration() != null ||
        !StringUtils.isEmpty(getMolarConcentrationValue()) 
          && getMgMlConcentration() != null) {
      showMessage("libraries.enterOnlyOneConcentrationValue");
      valid = false;
    }
    
    if (!StringUtils.isEmpty(getMolarConcentrationValue())) {
      MolarConcentration newConcentration = null;
      try {
        newConcentration = 
            MolarConcentration.makeConcentration(
                getMolarConcentrationValue(),
                getMolarConcentrationType().getSelection());
      }
      catch (Exception e) {
        showMessage("invalidUserInput", "Concentration (molar)", e.getLocalizedMessage());
        return false;
      }
      if(entity.getPrimaryWellMolarConcentration() == null )
      { 
        showMessage("invalidUserInput", "Molar Concentration", 
            "Cannot set the dilution factor using an absolute concentration if "
            + "the library well concentrations for the copy plates have not been set.  "
            + "Set using the dilution factor instead"); 
        // TODO: [#2920] make an error message property for this
        return false;
      }
      if(!!! NullSafeUtils.nullSafeEquals(
            entity.getMaxMolarConcentration(), entity.getMinMolarConcentration()) ||
         !!! NullSafeUtils.nullSafeEquals(
             entity.getMaxMolarConcentration(), entity.getPrimaryWellMolarConcentration()) )
      {
        showMessage("invalidUserInput", "Molar Concentration",
            "Cannot set the plate dilution factor using an absolute concentration "
            + "if the wells of the plate have varying concentrations.  "
            + "Set using the plate dilution factor instead"); 
        // TODO:  [#2920] make an error message property for this
        valid = false;
      }        
      if(entity.getPrimaryWellMolarConcentration().compareTo(
          newConcentration) < 0 ) {
        showMessage("invalidUserInput", "molar Concentration", 
                    "Target concentration cannot be more than the undiluted "
                    + "primary well concentration (" 
                        + entity.getPrimaryWellMolarConcentration()
                        + ") (only dilution is allowed)."); 
        // TODO:  [#2920] make an error message property for this
        valid = false;
      }
      
      if(!valid) return false;
      
      // 2. calculate a dilution factor
      BigDecimal newDilutionFactor = 
          entity.getPrimaryWellMolarConcentration().getValue().divide(
              newConcentration.getValue(), RoundingMode.HALF_UP);
      newDilutionFactor = 
          newDilutionFactor.scaleByPowerOfTen(
              newConcentration.getUnits().getScale() 
              - entity.getPrimaryWellMolarConcentration().getUnits().getScale());
      newDilutionFactor = newDilutionFactor.setScale(
          ScreensaverConstants.PLATE_DILUTION_FACTOR_SCALE, RoundingMode.HALF_UP); 
      //TODO: rename "copy dilution factor" or "dilution factor"
      
      _dilutionFactor = newDilutionFactor;
    }

    if (getMgMlConcentration() != null) {
      if(entity.getPrimaryWellMgMlConcentration() == null )
      {
        showMessage("invalidUserInput", "mg/mL Concentration",
                    "Cannot set the plate dilution factor using an absolute "
                    + "concentration if the library well concentrations for the "
                    + "plate have not been set.  "
                    + "Set using the plate dilution factor instead"); 
        // TODO:  [#2920] make an error message property for this
        return false;
      }
      if(!!! NullSafeUtils.nullSafeEquals(
          entity.getMaxMgMlConcentration(), entity.getMinMgMlConcentration()) || 
        !!! NullSafeUtils.nullSafeEquals(
            entity.getMaxMgMlConcentration(), entity.getPrimaryWellMgMlConcentration()) )
      {
        showMessage("invalidUserInput", "mg/mL Concentration",
                    "Cannot set the plate dilution factor using an absolute "
                    + "concentration if the wells of the plate have varying concentrations.  "
                    + "Set using the plate dilution factor instead"); 
        // TODO:  [#2920] make an error message property for this
        return false;
      }
      if(entity.getPrimaryWellMgMlConcentration().compareTo(getMgMlConcentration()) < 0 ) {
        showMessage("invalidUserInput", "mg/mL Concentration", 
                    "Target concentration cannot be more than the undiluted primary "
                    + "well concentration (" + entity.getPrimaryWellMgMlConcentration() 
                    + ") (only dilution is allowed)."); 
        // TODO:  [#2920] make an error message property for this
        valid = false;
      }
      BigDecimal newDilutionFactor = 
          entity.getPrimaryWellMgMlConcentration().divide(
              getMgMlConcentration(), 
              ScreensaverConstants.PLATE_DILUTION_FACTOR_SCALE, 
              RoundingMode.HALF_UP);  
      // TODO: An error occurred during the requested operation: 
      // Non-terminating decimal expansion; no exact representable decimal result.
      _dilutionFactor = newDilutionFactor;
    }   
    
    if (_dilutionFactor != null 
        && _dilutionFactor.compareTo(new BigDecimal("1.0")) < 0 )
    {
      showMessage("invalidUserInput", "dilution factor", 
          "The dilution factor must be greater than 1");
      return false;
    }


    if(!valid) return false;
    return super.validateEntity(entity);
  }
  

  
  @Override
  protected void updateEntityProperties(Copy entity)
  {
    super.updateEntityProperties(entity);
    
    ScreensaverUser screensaverUser = getCurrentScreensaverUser().getScreensaverUser();
    if (! (screensaverUser instanceof AdministratorUser) ||
          !((AdministratorUser)screensaverUser).isUserInRole(
              ScreensaverUserRole.LIBRARY_COPIES_ADMIN)) {
      throw new BusinessRuleViolationException(
          "only library copies administrators can edit library copy plates");
    }
    AdministratorUser adminUser = getDao().reloadEntity((AdministratorUser) getScreensaverUser());

    if( getWellConcentrationDilutionFactor() != null)
    {
      if (!!!NullSafeUtils.nullSafeEquals(
          getWellConcentrationDilutionFactor(), 
          entity.getWellConcentrationDilutionFactor())) {
        StringBuilder updateComments =
          new StringBuilder().append("Dilution factor changed from '")
            .append(NullSafeUtils.toString(
                entity.getWellConcentrationDilutionFactor(), 
                "<not specified>"))
            .append("' to '")
            .append(getWellConcentrationDilutionFactor()).append("'");
        entity.createUpdateActivity(adminUser, updateComments.toString());
        entity.setWellConcentrationDilutionFactor(getWellConcentrationDilutionFactor());
      }
    }   
    if(entity.getConcentrationStatistics() == null) { 
      // if the concentration hasn't been set, then it may be a new entity  
      // TODO: is there a better way to figure out if this is a new entity?
      _plateUpdater.updatePrimaryPlateConcentrations(entity);
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
        MolarUnit unit = (c == null ? 
            DEFAULT_PLATE_MOLAR_CONCENTRATION_UNITS 
            : c.getUnits()); // Todo: can we set default SM = mM, RNA = uM

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

  public void setWellConcentrationDilutionFactor(BigDecimal value)
  {
    _dilutionFactor = value;
  }

  public BigDecimal getWellConcentrationDilutionFactor()
  {
    return _dilutionFactor;
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
      _libraryViewer.getContextualSearchResults().reload();
      return _libraryViewer.viewEntity(library);
    }
    catch (JpaSystemException e) {
      if (e.contains(ConstraintViolationException.class)) {
        showMessage("cannotDeleteEntityInUse", "Copy " + copyName);
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
      else {
        throw e;
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
        return _libraryViewer.reload();
      case SAVE_NEW:
        return _libraryViewer.viewEntity(getDao().reloadEntity(getEntity(),
            true, Copy.library).getLibrary());
      default:
        return null;
    }
  }

  public List<SelectItem> getCopyUsageTypeSelectItems()
  {
    List<CopyUsageType> values = Arrays.asList(CopyUsageType.values());
    if (getEntity().getUsageType() == null) {
      return JSFUtils.createUISelectItemsWithEmptySelection(
          values, REQUIRED_VOCAB_FIELD_PROMPT);
    }
    return JSFUtils.createUISelectItems(values);
  }
}
