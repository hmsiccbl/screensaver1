// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Abstraction of a Lab. At the physical schema layer, a lab is represented by a
 * LabHead. However, at the logical entity model layer, it is conceptually
 * clearer to model a Lab as its own class. This allows the lab head and its lab
 * members to all share and reference the same Lab object.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
// TODO: this class was more important before the introduction of LabHead. Now
// that we have LabHead, it would quite reasonable to replace Lab with LabHead.
public class Lab
{
  // static members

  private static Logger log = Logger.getLogger(Lab.class);


  // instance data members

  private LabHead _labHead;
  private Set<ScreeningRoomUser> _labMembers = new HashSet<ScreeningRoomUser>();
  private LabAffiliation _labAffiliation;


  // public constructors and methods

  public Lab(LabHead labHead)
  {
    _labHead = labHead;
  }

  /**
   * Get the name of the lab this user is associated with.
   * 
   * @return the lab name, which is a concatenation of the name of the lab head,
   *         last and first, and the lab affiliation name
   */
  public String getLabName()
  {
    if (getLabHead() == null) {
      return "";
    }
    StringBuilder labName = new StringBuilder(getLabHead().getFullNameLastFirst());
    String labAffiliation = getLabAffiliationName();
    if (labAffiliation != null && labAffiliation.length() > 0) {
      labName.append(" - ").append(labAffiliation);
    }
    return labName.toString();
  }

  public LabHead getLabHead()
  {
    return _labHead;
  }

  public LabAffiliation getLabAffiliation()
  {
    return _labAffiliation;
  }

  public void setLabAffiliation(LabAffiliation labAffiliation)
  {
    _labAffiliation = labAffiliation;
  }

  public String getLabAffiliationName()
  {
    if (_labAffiliation == null) {
      return "";
    }
    return _labAffiliation.getAffiliationName();
  }

  public Set<ScreeningRoomUser> getLabMembers()
  {
    return _labMembers;
  }

  /**
   * @param labMembers the members of this lab, which does not include the lab
   *          head
   */
  void setLabMembers(Set<ScreeningRoomUser> labMembers)
  {
    _labMembers = labMembers;
  }

  void setLabHead(LabHead labHead)
  {
    _labHead = labHead;
  }


  // private methods

}
