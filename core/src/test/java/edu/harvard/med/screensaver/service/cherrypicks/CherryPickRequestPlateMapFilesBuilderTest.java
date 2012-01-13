// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cherrypicks;

import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.test.AbstractSpringTest;

public class CherryPickRequestPlateMapFilesBuilderTest extends AbstractSpringTest
{
  private static Logger log = Logger.getLogger(CherryPickRequestPlateMapFilesBuilderTest.class);

  @Autowired
  protected CherryPickRequestPlateMapFilesBuilder builder;

  public void testBuildZip()
  {
    fail("not implemented");
  }
}

