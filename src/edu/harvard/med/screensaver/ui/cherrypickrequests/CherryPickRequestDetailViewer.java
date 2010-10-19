// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.cherrypickrequests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.model.SelectItem;

import com.google.common.base.Joiner;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.CherryPickRequestDAO;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayProtocolsFollowed;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickFollowupResultsStatus;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.SmallMoleculeCherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryScreeningStatus;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.policy.CherryPickRequestAllowancePolicy;
import edu.harvard.med.screensaver.ui.EditResult;
import edu.harvard.med.screensaver.ui.EditableEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.util.DevelopmentException;

/**
 * Backing bean for Cherry Pick Request Detail Viewer page.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class CherryPickRequestDetailViewer extends EditableEntityViewerBackingBean<CherryPickRequest>
{
  private static Logger log = Logger.getLogger(CherryPickRequestDetailViewer.class);

  private CherryPickRequestDAO _cherryPickRequestDao;
  private CherryPickRequestViewer _cherryPickRequestViewer;
  private ScreenViewer _screenViewer;
  private CherryPickRequestAllowancePolicy<SmallMoleculeCherryPickRequest> _smallMoleculeCherryPickRequestAllowancePolicy;
  private CherryPickRequestAllowancePolicy<RNAiCherryPickRequest> _rnaiCherryPickRequestAllowancePolicy;

  private UISelectOneBean<PlateType> _assayPlateType;
  private UISelectOneEntityBean<ScreeningRoomUser> _requestedBy;
  private UISelectOneEntityBean<AdministratorUser> _volumeApprovedBy;

  private UISelectOneBean<VolumeUnit> _transferVolumePerWellRequestedType;
  private String _transferVolumePerWellRequestedValue;

  private UISelectOneBean<VolumeUnit> _transferVolumePerWellApprovedType;
  private String _transferVolumePerWellApprovedValue;



  /**
   * @motivation for CGLIB2
   */
  protected CherryPickRequestDetailViewer(){}

  public CherryPickRequestDetailViewer(CherryPickRequestDetailViewer thisProxy,
                                       CherryPickRequestViewer cherryPickRequestViewer,
                                       GenericEntityDAO dao,
                                       CherryPickRequestDAO cherryPickRequestDao,
                                       ScreenViewer screenViewer,
                                       CherryPickRequestAllowancePolicy<SmallMoleculeCherryPickRequest> smallMoleculeCherryPickRequestAllowancePolicy,
                                       CherryPickRequestAllowancePolicy<RNAiCherryPickRequest> rnaiCherryPickRequestAllowancePolicy)
  {
    super(thisProxy,
          CherryPickRequest.class,
          EDIT_CHERRY_PICK_REQUEST,
          dao);
    _cherryPickRequestDao = cherryPickRequestDao;
    _cherryPickRequestViewer = cherryPickRequestViewer;
    _screenViewer = screenViewer;
    _smallMoleculeCherryPickRequestAllowancePolicy = smallMoleculeCherryPickRequestAllowancePolicy;
    _rnaiCherryPickRequestAllowancePolicy = rnaiCherryPickRequestAllowancePolicy;
    getIsPanelCollapsedMap().put("cherryPickRequestDetails", false);
    getIsPanelCollapsedMap().put("cherryPickFollowupAssays", true);
  }

  @Override
  public void initializeEntity(CherryPickRequest cherryPickRequest)
  {
    getDao().needReadOnly(cherryPickRequest,
                          CherryPickRequest.requestedBy.getPath());
    getDao().needReadOnly(cherryPickRequest,
                          CherryPickRequest.emptyWellsOnAssayPlate.getPath());
    getDao().needReadOnly(cherryPickRequest,
                          CherryPickRequest.cherryPickScreenings.getPath());
  }

  @Override
  public void initializeViewer(final CherryPickRequest cherryPickRequest)
  {
    SortedSet<ScreeningRoomUser> requestedByCandidates = Sets.newTreeSet();
    requestedByCandidates.addAll(cherryPickRequest.getScreen().getAssociatedScreeningRoomUsers());
    // add the current requestedBy user, even if it's no longer a valid candidate, to avoid inadvertently changing it   
    requestedByCandidates.add(cherryPickRequest.getRequestedBy());
    _requestedBy = new UISelectOneEntityBean<ScreeningRoomUser>(requestedByCandidates,
      cherryPickRequest.getRequestedBy(),
      getDao()) {
      @Override
      protected String makeLabel(ScreeningRoomUser u) { return u.getFullNameLastFirst(); }
    };

    SortedSet<AdministratorUser> candidateVolumeApprovers = new TreeSet<AdministratorUser>(ScreensaverUserComparator.getInstance());
    candidateVolumeApprovers.addAll(getDao().findAllEntitiesOfType(AdministratorUser.class)); // TODO: filter out all but CherryPickAdmins
    _volumeApprovedBy = new UISelectOneEntityBean<AdministratorUser>(candidateVolumeApprovers,
      cherryPickRequest.getVolumeApprovedBy(),
      true,
      getDao()) {
      @Override
      protected String makeLabel(AdministratorUser a) { return a.getFullNameLastFirst(); }
    };

    _assayPlateType = new UISelectOneBean<PlateType>(Arrays.asList(PlateType.values()), cherryPickRequest.getAssayPlateType()) {
      @Override
      protected String makeLabel(PlateType plateType) { return plateType.getFullName(); }
    };

    _transferVolumePerWellRequestedType = null;
    _transferVolumePerWellRequestedValue = null;
    _transferVolumePerWellApprovedType = null;
    _transferVolumePerWellApprovedValue = null;
  }
  
  @Override
  protected void initializeNewEntity(CherryPickRequest entity)
  {
    entity.addEmptyWellsOnAssayPlate(entity.getAssayPlateType().getPlateSize().getEdgeWellNames(2));
  }

  public UISelectOneBean<PlateType> getAssayPlateType()
  {
    return _assayPlateType;
  }

  public UISelectOneEntityBean<ScreeningRoomUser> getRequestedBy()
  {
    return _requestedBy;
  }

  public UISelectOneEntityBean<AdministratorUser> getVolumeApprovedBy()
  {
    return _volumeApprovedBy;
  }

  public int getScreenerCherryPickCount()
  {
    return getEntity().getScreenerCherryPicks().size();
  }

  public int getLabCherryPickCount()
  {
    return getEntity().getLabCherryPicks().size();
  }

  public int getActiveCherryPickPlatesCount()
  {
    return getEntity().getActiveCherryPickAssayPlates().size();
  }

  public int getCompletedCherryPickPlatesCount()
  {
    return getEntity().getCompletedCherryPickAssayPlates().size();
  }

  public UISelectOneBean<VolumeUnit> getTransferVolumePerWellRequestedType()
  {
    try {
      if (_transferVolumePerWellRequestedType == null)
      {
        Volume v = getEntity().getTransferVolumePerWellRequested();
        VolumeUnit unit = (v == null ? VolumeUnit.MICROLITERS : v.getUnits());
        _transferVolumePerWellRequestedType =
          new UISelectOneBean<VolumeUnit>(VolumeUnit.DISPLAY_VALUES, unit)
          {
            @Override
            protected String makeLabel(VolumeUnit t)
            {
              return t.getValue();
            }
          };
      }
      return _transferVolumePerWellRequestedType;
    } catch (Exception e) {
      log.error("err: " + e);
      return null;
    }
  }

  /**
   * This method exists to grab the value portion of the Quantity stored
  */
  public String getTransferVolumePerWellRequestedValue()
  {
    if (_transferVolumePerWellRequestedValue == null)
    {
      if(getEntity().getTransferVolumePerWellRequested() != null)
        _transferVolumePerWellRequestedValue =
           getEntity().getTransferVolumePerWellRequested().getDisplayValue().toString();
      else
        _transferVolumePerWellRequestedValue = null;
    }
    return _transferVolumePerWellRequestedValue;
  }

  /**
   * This method exists to set the value portion of the Quantity stored
   * @see #save()
  */
  public void setTransferVolumePerWellRequestedValue( String value )
  {
    _transferVolumePerWellRequestedValue = value;
  }

  public UISelectOneBean<VolumeUnit> getTransferVolumePerWellApprovedType()
  {
    try {
      if (_transferVolumePerWellApprovedType == null)
      {
        Volume v = getEntity().getTransferVolumePerWellApproved();
        VolumeUnit unit = (v == null ? VolumeUnit.MICROLITERS  : v.getUnits());
        _transferVolumePerWellApprovedType =
          new UISelectOneBean<VolumeUnit>(VolumeUnit.DISPLAY_VALUES, unit)
          {
            @Override
            protected String makeLabel(VolumeUnit t)
            {
              return t.getValue();
            }
          };
      }
      return _transferVolumePerWellApprovedType;
    } catch (Exception e) {
      log.error("err: " + e);
      return null;
    }
  }

  /**
   * This method exists to grab the value portion of the Quantity stored
  */
  public String getTransferVolumePerWellApprovedValue()
  {
    if (_transferVolumePerWellApprovedValue == null)
    {
      if(getEntity().getTransferVolumePerWellApproved() != null)
        _transferVolumePerWellApprovedValue =
           getEntity().getTransferVolumePerWellApproved().getDisplayValue().toString();
      else
        _transferVolumePerWellApprovedValue = null;
    }
    return _transferVolumePerWellApprovedValue;
  }

  /**
   * This method exists to set the value portion of the Quantity stored
   * @see #save()
  */
  public void setTransferVolumePerWellApprovedValue( String value )
  {
    _transferVolumePerWellApprovedValue = value;
  }

  public List<SelectItem> getCherryPickAssayProtocolsFollowedSelectItems()
  {
    return JSFUtils.createUISelectItemsWithEmptySelection(Arrays.asList(CherryPickAssayProtocolsFollowed.values()), 
                                                          "<none>");
  }

  public List<SelectItem> getCherryPickFollowupResultsStatusSelectItems()
  {
    return JSFUtils.createUISelectItemsWithEmptySelection(Arrays.asList(CherryPickFollowupResultsStatus.values()), 
                                                          "<none>");
  }

  @Override
  public boolean isDeleteSupported()
  {
    return !!!getEntity().isAllocated() &&
    !!!(getEntity() instanceof RNAiCherryPickRequest && ((RNAiCherryPickRequest) getEntity()).isScreened());
  }

  @UICommand
  @Override
  public String delete()
  {
    if (!!!isDeleteSupported()) {
      throw new BusinessRuleViolationException("cannot delete a cherry pick request that has been allocated or that has been screened");
    }
    _cherryPickRequestDao.deleteCherryPickRequest(getEntity());
    return _screenViewer.viewEntity(getEntity().getScreen());
  }

  @UICommand
  public String deleteAllCherryPicks()
  {
    _cherryPickRequestDao.deleteAllCherryPicks(getEntity());
    return _cherryPickRequestViewer.reload();
  }

  @Override
  protected boolean validateEntity(CherryPickRequest entity)
  {
    boolean valid = true;
    try {
      Volume.makeVolume(_transferVolumePerWellRequestedValue, _transferVolumePerWellRequestedType.getSelection());
    } 
    catch (Exception e) {
      showFieldInputError("Requested Volume", e.getLocalizedMessage());
      valid = false;
    }
    try {
      Volume.makeVolume(_transferVolumePerWellApprovedValue, _transferVolumePerWellApprovedType.getSelection());
    } 
    catch (Exception e) {
      showFieldInputError("Approved Volume", e.getLocalizedMessage());
      valid = false;
    }
    return valid;
  }

  @Override
  protected void updateEntityProperties(CherryPickRequest entity) 
  {
    getEntity().setTransferVolumePerWellRequested(Volume.makeVolume(_transferVolumePerWellRequestedValue,
                                                                    _transferVolumePerWellRequestedType.getSelection()));
    getEntity().setTransferVolumePerWellApproved(Volume.makeVolume(_transferVolumePerWellApprovedValue,
                                                                   _transferVolumePerWellApprovedType.getSelection()));

    entity.setAssayPlateType(_assayPlateType.getSelection());
    entity.setRequestedBy(_requestedBy.getSelection());
    entity.setVolumeApprovedBy(_volumeApprovedBy.getSelection());
  }

  /**
   * Get the set of empty rows requested by the screener.
   * @return well names that must be left empty on each cherry pick assay plate
   */
  public Set<WellName> getEmptyWellsOnAssayPlate()
  {
    return getEntity().getEmptyWellsOnAssayPlate();
  }

  /**
   * Set the set of empty wells.
   * @param emptyWellsOnAssayPlate wells that screener has requested be
   * left empty on each cherry pick assay plate
   */
  public void setEmptyWellsOnAssayPlate(Set<WellName> emptyWellsOnAssayPlate)
  {
    getEntity().clearEmptyWellsOnAssayPlate();
    getEntity().addEmptyWellsOnAssayPlate(emptyWellsOnAssayPlate);
  }

  @UICommand
  @Transactional
  public void showAdminWarnings()
  {
    // eager fetch all data needed to calculate warnings
    CherryPickRequest cherryPickRequest = getDao().reloadEntity(getEntity(),
                                                            true,
                                                            CherryPickRequest.screen.getPath(),
                                                            CherryPickRequest.labCherryPicks.to(LabCherryPick.screenerCherryPick).to(ScreenerCherryPick.screenedWell).to(Well.deprecationActivity).getPath(),
                                                            CherryPickRequest.labCherryPicks.to(LabCherryPick.sourceWell).to(Well.deprecationActivity).getPath());
    getDao().needReadOnly(cherryPickRequest, CherryPickRequest.screenerCherryPicks.to(ScreenerCherryPick.screenedWell).to(Well.library).getPath());

    boolean warningIssued = false;
    warningIssued |= doWarnOnCherryPickAllowanceExceeded(cherryPickRequest);
    warningIssued |= doWarnOnInvalidSourceWell(cherryPickRequest);
    warningIssued |= doWarnOnDuplicateScreenerCherryPicks(cherryPickRequest);
    warningIssued |= doWarnOnDeprecatedWells(cherryPickRequest);
    warningIssued |= doWarnOnLibraryScreeningStatus(cherryPickRequest);
    if (!warningIssued) {
      showMessage("cherryPicks.allCherryPicksAreValid");
    }
  }

  private <CPR extends CherryPickRequest> boolean doWarnOnCherryPickAllowanceExceeded(CPR cherryPickRequest)
  {
    CherryPickRequestAllowancePolicy<CPR> policy;
    if (cherryPickRequest instanceof SmallMoleculeCherryPickRequest) {
      policy = (CherryPickRequestAllowancePolicy<CPR>) _smallMoleculeCherryPickRequestAllowancePolicy;
    }
    else if (cherryPickRequest instanceof RNAiCherryPickRequest){
      policy = (CherryPickRequestAllowancePolicy<CPR>) _rnaiCherryPickRequestAllowancePolicy;
    }
    else {
      throw new DevelopmentException("unsupported cherry pick request type: " + cherryPickRequest.getClass().getName());
    }
    if (policy.isCherryPickAllowanceExceeded(cherryPickRequest)) {
      showMessage("cherryPicks.cherryPickAllowanceExceeded",
                  policy.getCherryPickAllowanceUsed(cherryPickRequest),
                  policy.getCherryPickAllowance(cherryPickRequest));
      return true;
    }
    return false;
  }

  private boolean doWarnOnInvalidSourceWell(CherryPickRequest cherryPickRequest)
  {
    Set<WellKey> invalidCherryPicks = Sets.newHashSet();
    for (ScreenerCherryPick screenerCherryPick : cherryPickRequest.getScreenerCherryPicks()) {
      if (screenerCherryPick.getLabCherryPicks().size() == 0) {
        invalidCherryPicks.add(screenerCherryPick.getScreenedWell().getWellKey());
      }
    }
    if (!!!invalidCherryPicks.isEmpty()) {
      showMessage("cherryPicks.invalidWells", Joiner.on(", ").join(invalidCherryPicks));
      return true;
    }
    return false;
  }

  private boolean doWarnOnDuplicateScreenerCherryPicks(CherryPickRequest cherryPickRequest)
  {
    Map<WellKey,Number> duplicateScreenerCherryPickWellKeysMap =
      _cherryPickRequestDao.findDuplicateCherryPicksForScreen(cherryPickRequest.getScreen());
    Set<WellKey> duplicateScreenerCherryPickWellKeys = duplicateScreenerCherryPickWellKeysMap.keySet();
    Set<WellKey> ourScreenerCherryPickWellsKeys = new HashSet<WellKey>();
    for (ScreenerCherryPick screenerCherryPick : cherryPickRequest.getScreenerCherryPicks()) {
      ourScreenerCherryPickWellsKeys.add(screenerCherryPick.getScreenedWell().getWellKey());
    }
    duplicateScreenerCherryPickWellKeys.retainAll(ourScreenerCherryPickWellsKeys);
    if (duplicateScreenerCherryPickWellKeysMap.size() > 0) {
      String duplicateWellsList = Joiner.on(", ").join(duplicateScreenerCherryPickWellKeys);
      showMessage("cherryPicks.duplicateCherryPicksInScreen",
                  cherryPickRequest.getScreen().getFacilityId(),
                  duplicateWellsList);
      return true;
    }
    return false;
  }

  private boolean doWarnOnDeprecatedWells(CherryPickRequest cherryPickRequest)
  {
    Multimap<AdministrativeActivity,WellKey> wellDeprecations = TreeMultimap.create();
    for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
      Well well = labCherryPick.getSourceWell();
      if (well.isDeprecated()) {
        wellDeprecations.put(well.getDeprecationActivity(), well.getWellKey());
      }
      well = labCherryPick.getScreenerCherryPick().getScreenedWell();
      if (well.isDeprecated()) {
        wellDeprecations.put(well.getDeprecationActivity(), well.getWellKey());
      }
    }
    for (AdministrativeActivity deprecationActivity : wellDeprecations.keySet()) {
      showMessage("cherryPicks.deprecatedWells",
                  deprecationActivity.getComments(),
                  Joiner.on(", ").join(wellDeprecations.values()));
      return true;
    }
    return false;
  }

  private boolean doWarnOnLibraryScreeningStatus(CherryPickRequest cherryPickRequest)
  {
    Set<Library> libraries = new LinkedHashSet<Library>();
    for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
      libraries.add(labCherryPick.getSourceWell().getLibrary());
      libraries.add(labCherryPick.getScreenerCherryPick().getScreenedWell().getLibrary());
    }
    for (Library library : libraries) {
      if (library.getScreeningStatus() != LibraryScreeningStatus.ALLOWED) {
        if (!cherryPickRequest.getScreen().getLibrariesPermitted().contains(library)) {
          showMessage("libraries.libraryUsageConflict",
                      library.getShortName(),
                      library.getScreeningStatus().getValue());
          return true;
        }
      }
    }
    return false;
  }
  
  @Override
  protected String postEditAction(EditResult editResult)
  {
    switch (editResult) {
    case CANCEL_EDIT: return _cherryPickRequestViewer.reload();
    case SAVE_EDIT: return _cherryPickRequestViewer.reload();
    case CANCEL_NEW: return _screenViewer.reload();
    case SAVE_NEW: return _screenViewer.reload();
    default: return null;
    }
  }
}
