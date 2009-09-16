// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.beans.IntrospectionException;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;

public class PublicationTest extends AbstractEntityInstanceTest<Publication>
{
  public static TestSuite suite()
  {
    return buildTestSuite(PublicationTest.class, Publication.class);
  }

  public PublicationTest() throws IntrospectionException
  {
    super(Publication.class);
  }

  public void testPublicationCitation()
  {
    Publication publication = new Publication();
    publication.setTitle("Screensaver: LIMS for HTS Facilities");
    publication.setAuthors("Tolopko, A., Sullivan, J., Lieftink, C.");
    publication.setJournal("Bioinformatics");
    publication.setYearPublished("2019");
    publication.setVolume("218");
    publication.setPages("101-103");
    assertEquals("Tolopko, A., Sullivan, J., Lieftink, C. (2019). Screensaver: LIMS for HTS Facilities. Bioinformatics 218, 101-103.",
                 publication.getCitation());
  }
}

