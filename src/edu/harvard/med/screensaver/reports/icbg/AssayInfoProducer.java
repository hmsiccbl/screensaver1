// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.reports.icbg;

import java.util.Date;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.StatusItem;


/**
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class AssayInfoProducer
{
  private static Logger log = Logger.getLogger(AssayInfoProducer.class);
  
  public AssayInfo getAssayInfoForScreen(Screen screen)
  {
    AssayInfo assayInfo = new AssayInfo();
    assayInfo.setAssayName("ICCBL" + screen.getScreenNumber());
    assayInfo.setProtocolDescription(screen.getTitle());
    assayInfo.setPNote(screen.getSummary());
    assayInfo.setInvestigator(screen.getLabHead().getLastName().toUpperCase());

    String assayCategoryText = "";
    for (String keyword : screen.getKeywords()) {
      assayCategoryText += keyword.toUpperCase() + " ";
    }
    assayCategoryText += screen.getSummary().toUpperCase();
    setAssayCategory(assayInfo, assayCategoryText);

    Date assayDate = null;
    for (StatusItem statusItem: screen.getStatusItems()) {
      Date statusItemDate = statusItem.getStatusDate();
      if (assayDate == null || assayDate.before(statusItemDate)) {
        assayDate = statusItemDate;
      }
    }
    
    // TODO: replace with non-deprecated
    assayInfo.setAssayDate(
      (assayDate.getMonth() + 1) + "/" +
      assayDate.getDate() + "/" +
      (assayDate.getYear() + 1900));
    
    return assayInfo;
  }

  private void setAssayCategory(AssayInfo assayInfo, String assayCategoryText)
  {
    if (
      assayCategoryText.contains("MALARIA")) {
      assayInfo.setAssayCategory("ANTI-MALARIA");
    }
    else if (
      assayCategoryText.contains("DIABETES") ||
      assayCategoryText.contains("OBESITY")) {
      assayInfo.setAssayCategory("OBESITY_DIABETES");
    }
    else if (
      assayCategoryText.contains("TRYPANOSOMIASIS")) {
      assayInfo.setAssayCategory("TRYPANOSOMIASIS");
    }
    else if (
      assayCategoryText.contains("LEISHMANIASIS")) {
      assayInfo.setAssayCategory("LEISHMANIASIS");
    }
    else if (
      assayCategoryText.contains("INFLAM")) {
      assayInfo.setAssayCategory("INFLAMMATION");
    }
    else if (
      assayCategoryText.contains("CYTOTOXIC")) {
      assayInfo.setAssayCategory("CYTOTOXICITY");
    }
    else if (
      assayCategoryText.contains("CONTRACEPTION")) {
      assayInfo.setAssayCategory("CONTRACEPTION");
    }
    else if (
      assayCategoryText.contains("CHAGAS")) {
      assayInfo.setAssayCategory("CHAGAS_DISEASE");
    }
    else if (
      assayCategoryText.contains("CARDIOVASC")) {
      assayInfo.setAssayCategory("CARDIOVASCULAR");
    }
    else if (
      assayCategoryText.contains("CANCER") ||
      assayCategoryText.contains("FANCONI ANEMIA/BRCA PATHWAY") ||
      assayCategoryText.contains("E6-DEPENDENT DEGRADATION OF P53") ||
      assayCategoryText.contains("WNT PATHWAY")) {
      assayInfo.setAssayCategory("ANTI-CANCER");
    }
    else if (
      assayCategoryText.contains("TUBERCULOSIS")) {
      assayInfo.setAssayCategory("ANTI-TUBERCULOSIS");
    }
    else if (
      assayCategoryText.contains("FUNG")) {
      assayInfo.setAssayCategory("ANTI-FUNGAL");
    }
    else if (
      assayCategoryText.contains("ANTHRAX") ||
      assayCategoryText.contains("BOTULINUM") ||
      assayCategoryText.contains("TULARENSIS") ||
      assayCategoryText.contains("TULARENSIS") ||
      assayCategoryText.contains("CHOLER") ||
      assayCategoryText.contains("AERUGINOSA") ||
      assayCategoryText.contains("YERSINIA PESTIS") ||
      assayCategoryText.contains("AMINOARABINOSE") ||
      assayCategoryText.contains("BACTERI")) {
      assayInfo.setAssayCategory("ANTI-BACTERIAL");
    }
    else if (
      assayCategoryText.contains("INFLUENZA") ||
      assayCategoryText.contains("VACCINIA") ||
      assayCategoryText.contains("WEST NILE") ||
      assayCategoryText.contains("VIRUS")) {
      assayInfo.setAssayCategory("ANTI-VIRAL_NO_HIV");
    }
    else if (
      assayCategoryText.contains("AIDS") ||
      assayCategoryText.contains("HIV")) {
      assayInfo.setAssayCategory("ANTI-HIV_AIDS");
    }
    else if (
      assayCategoryText.contains("INHIBIT THE RANGTPASE SYSTEM")) {
      assayInfo.setAssayCategory("OTHER - INHIBIT THE RANGTPASE SYSTEM");
    }
    else if (
      assayCategoryText.contains("CRYPTOSPORIDIUM PARVUM")) {
      assayInfo.setAssayCategory("OTHER - ANTI-PROTOZOA");
    }
    else if (
      assayCategoryText.contains("INHIBIT TNF-ALPHA-MEDIATED NECROSIS IN JURKAT CELLS")) {
      assayInfo.setAssayCategory("OTHER - NEURODEGENERATIVE DISEASE");
    }
    else if (
      assayCategoryText.contains("SHWACHMAN DIAMOND SYNDROME")) {
      assayInfo.setAssayCategory("OTHER - SHWACHMAN DIAMOND SYNDROME");
    }
    else if (
      assayCategoryText.contains("DOWN SYNDROME")) {
      assayInfo.setAssayCategory("OTHER - DOWN SYNDROME");
    }
    else if (
      assayCategoryText.contains("MULLERIAN INHIBITING SUBSTANCE")) {
      assayInfo.setAssayCategory("OTHER - HUMAN INFERTILITY");
    }
    else if (
      assayCategoryText.contains("ADULT ONSET DEAFNESS SYNDROME")) {
      assayInfo.setAssayCategory("OTHER - ADULT ONSET DEAFNESS SYNDROME");
    }
    else {
      log.info("assigning assay category OTHER for assay category text: " + assayCategoryText);
      assayInfo.setAssayCategory("OTHER");
    }
  }
}
