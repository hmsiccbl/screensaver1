// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.libraries.PlateType;

import org.apache.log4j.Logger;

/**
 * @hibernate.subclass discriminator-value="true" 
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class LegacyCherryPickAssayPlate extends CherryPickAssayPlate
{
  // static members

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(LegacyCherryPickAssayPlate.class);


  // instance data members
  
  private String _legacyPlateName;


  // public constructors and methods
  
  public LegacyCherryPickAssayPlate(CherryPickRequest cherryPickRequest,
                                    Integer plateOrdinal,
                                    Integer attemptOrdinal,
                                    PlateType plateType,
                                    String legacyPlateName)
  {
    super(cherryPickRequest, plateOrdinal, attemptOrdinal, plateType);
    _legacyPlateName = legacyPlateName;
  }

  @DerivedEntityProperty
  public String getName()
  {
    return _legacyPlateName;
  }
  
  /**
   * @hibernate.property type="text"
   * @return
   */
  public String getLegacyPlateName()
  {
    return _legacyPlateName;
  }


  @Override
  public Object clone() 
  {
    return 
    new LegacyCherryPickAssayPlate(getCherryPickRequest(),
                                   getPlateOrdinal(), 
                                   getAttemptOrdinal() + 1,
                                   getAssayPlateType(),
                                   getLegacyPlateName());
  }

  // private methods
  
  private LegacyCherryPickAssayPlate() 
  {
  };

  private void setLegacyPlateName(String legacyPlateName)
  {
    _legacyPlateName = legacyPlateName;
  }
}

