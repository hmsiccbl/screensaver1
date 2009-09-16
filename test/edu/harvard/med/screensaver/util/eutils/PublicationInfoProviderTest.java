// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

import junit.framework.TestCase;
import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.model.screens.Publication;

/**
 * Test the {@link PublicationInfoProvider}.
 * <p>
 * WARNING: this test requires an internet connection.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class PublicationInfoProviderTest extends TestCase
{
  private PublicationInfoProvider _publicationInfoProvider = new PublicationInfoProvider();

  // Watts DC
  public void testGetPublicationInfoForPubmedId()
  {
    try {
      Publication publication = _publicationInfoProvider.getPublicationForPubmedId(77);
      assertNotNull(publication);
      assertEquals(publication.getYearPublished(), "1975");
      assertEquals(publication.getAuthors(), "Chegwidden WR, Watts DC");
      assertEquals(publication.getTitle(), "Kinetic studies and effects of anions on creatine phosphokinase from skeletal muscle of rhesus monkey (Macaca mulatta).");
      assertEquals(publication.getJournal(), "Biochimica et biophysica acta");
      assertEquals(publication.getVolume(), "410");
      assertEquals(publication.getPages(), "99-114");
    }
    catch (EutilsException e) {
      fail("PublicationInforProvider threw an exception: " + e.getMessage());
    }
  }
}
