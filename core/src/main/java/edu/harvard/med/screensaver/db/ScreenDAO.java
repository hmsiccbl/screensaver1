// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

public interface ScreenDAO
{
  void deleteStudy(Screen study);
  
  int countScreenedExperimentalWells(Screen screen, boolean distinct);
  
  int countTotalPlatedLabCherryPicks(Screen screen);

  List<Screen> findRelatedScreens(Screen screen);
  
  List<Screen> findAllScreens();

  List<Screen>  findAllStudies();

  boolean isScreenFacilityIdUnique(Screen screen);

  Set<ScreensaverUser> findLabActivityPerformedByCandidates(LabActivity a);

  int countLoadedExperimentalWells(Screen screen);

  /**
   * Use SQL to populate the Study-to-Reagent link table: <br>
   * Reagents can be added to the study in java-hibernate, (using the standard
   * {@link AnnotationType#createAnnotationValue(Reagent, String)}) however, when
   * this is done, the AnnotationType will maintain a collection of AnnotationTypes in
   * memory.<br>
   * <br>
   * <b>
   * NOTE: do not use this method if creating the Annotations through the
   * {@link AnnotationType#createAnnotationValue(Reagent, String)} method.
   * </b>
   */
	int populateStudyReagentLinkTable(int screenId);

}
