// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.beans.IntrospectionException;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;

public class AnnotationValueTest extends AbstractEntityInstanceTest<AnnotationValue>
{
  // static members

  private static Logger log = Logger.getLogger(AnnotationValueTest.class);


  // instance data members

  protected ScreenResultParser screenResultParser;
  protected LibrariesDAO librariesDao;
  
  
  // public constructors and methods

  public AnnotationValueTest() throws IntrospectionException
  {
    super(AnnotationValue.class);
  }
}

