// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.reports.icbg;

/**
 * accepted assay_categories are:
ANTI-BACTERIAL
ANTI-CANCER
ANTI-FUNGAL
ANTI-HIV_AIDS
ANTI-MALARIA
ANTI-TUBERCULOSIS
ANTI-VIRAL_NO_HIV
CARDIOVASCULAR
CENTRAL_NERVOUS_SYSTEM
CHAGAS_DISEASE
CONTRACEPTION
CYTOTOXICITY
INFLAMMATION
LEISHMANIASIS
OBESITY_DIABETES
OTHER
TRYPANOSOMIASIS
WOMENS_HEALTH
 */
public class AssayInfo
{
  private String _assayName;           // screen.id
  private String _assayCategory;       // ??
  private String _assayDate;           // max(status_date) from screen_status
  private String _investigator;        // screen.user_id.lab_name.last
  private String _protocolDescription; // screen.title
  private String _pNote;               // screen.summary
  
  // quick-generated accessors
  public String getAssayCategory() {
    return _assayCategory;
  }
  public void setAssayCategory(String assayCategory) {
    _assayCategory = assayCategory;
  }
  public String getAssayDate() {
    return _assayDate;
  }
  public void setAssayDate(String assayDate) {
    _assayDate = assayDate;
  }
  public String getAssayName() {
    return _assayName;
  }
  public void setAssayName(String assayName) {
    _assayName = assayName;
  }
  public String getInvestigator() {
    return _investigator;
  }
  public void setInvestigator(String investigator) {
    _investigator = investigator;
  }
  public String getPNote() {
    return _pNote;
  }
  public void setPNote(String note) {
    _pNote = note;
  }
  public String getProtocolDescription() {
    return _protocolDescription;
  }
  public void setProtocolDescription(String protocolDescription) {
    _protocolDescription = protocolDescription;
  }
}
