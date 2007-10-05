// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import java.beans.IntrospectionException;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;

import org.apache.log4j.Logger;

public class CherryPickAssayPlateTest extends AbstractEntityInstanceTest<CherryPickAssayPlate>
{
  private static Logger log = Logger.getLogger(CherryPickAssayPlateTest.class);

  public CherryPickAssayPlateTest() throws IntrospectionException
  {
    super(CherryPickAssayPlate.class);
  }
}

