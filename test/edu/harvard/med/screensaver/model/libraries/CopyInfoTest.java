// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.beans.IntrospectionException;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;

import org.apache.log4j.Logger;

public class CopyInfoTest extends AbstractEntityInstanceTest
{
  // static members

  private static Logger log = Logger.getLogger(CopyInfoTest.class);


  // instance data members

  
  // public constructors and methods

  public CopyInfoTest() throws IntrospectionException
  {
    super(CopyInfo.class);
  }
}

