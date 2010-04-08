// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import edu.harvard.med.screensaver.model.AttachedFileType;

@Entity
@DiscriminatorValue("user")
@org.hibernate.annotations.Proxy
public class UserAttachedFileType extends AttachedFileType
{
  private static final long serialVersionUID = 1L;
  
  private UserAttachedFileType()
  {
  }
  
  public UserAttachedFileType(String value) 
  {
    super(value);
  }
}
