package edu.harvard.med.screensaver.service.libraries;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellCopy;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.policy.CherryPickPlateSourceWellMinimumVolumePolicy;

public class WellCopyVolumeAdjuster
{
  private static final Logger log = Logger.getLogger(WellCopyVolumeAdjuster.class);

  GenericEntityDAO _dao;

  private CherryPickPlateSourceWellMinimumVolumePolicy _cherryPickPlateSourceWellMinimumVolumePolicy;

  protected WellCopyVolumeAdjuster()
  {}

  public WellCopyVolumeAdjuster(GenericEntityDAO dao,
                                CherryPickPlateSourceWellMinimumVolumePolicy cherryPickPlateSourceWellMinimumVolumePolicy)
  {
    _dao = dao;
    _cherryPickPlateSourceWellMinimumVolumePolicy = cherryPickPlateSourceWellMinimumVolumePolicy;
  }

  @Transactional
  public void adjustWellCopyVolumes(AdministratorUser adminUser,
                                    Map<WellCopy,Volume> newRemainingVolumes,
                                    String comments)
  {
    if (!adminUser.isUserInRole(ScreensaverUserRole.LIBRARIES_ADMIN)) {
      throw new BusinessRuleViolationException("only libraries administrators can adjust well volumes");
    }
    if (newRemainingVolumes.size() > 0) {
      AdministratorUser administratorUser = (AdministratorUser) _dao.reloadEntity(adminUser);
      WellVolumeCorrectionActivity wellVolumeCorrectionActivity =
        new WellVolumeCorrectionActivity(administratorUser, new LocalDate());
      wellVolumeCorrectionActivity.setComments(comments);
      for (Map.Entry<WellCopy,Volume> entry : newRemainingVolumes.entrySet()) {
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
      _dao.persistEntity(wellVolumeCorrectionActivity);
    }
  }

  @Transactional
  public void setWellCopyVolumesToEmpty(AdministratorUser adminUser,
                                        Set<WellCopy> wellCopies,
                                        String comments)
  {
    Map<WellCopy,Volume> newRemainingVolumes = Maps.newHashMap();
    for (WellCopy wellCopy : wellCopies) {
      newRemainingVolumes.put(wellCopy, _cherryPickPlateSourceWellMinimumVolumePolicy.getMinimumVolumeAllowed(wellCopy.getWell()));
    }
    adjustWellCopyVolumes(adminUser, newRemainingVolumes, comments);
  }
}
