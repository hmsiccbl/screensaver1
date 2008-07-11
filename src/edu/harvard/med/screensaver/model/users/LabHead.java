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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.screens.Screen;

import org.apache.log4j.Logger;

/**
 * A Hibernate entity bean representing a lab head.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@PrimaryKeyJoinColumn(name="screensaverUserId")
@org.hibernate.annotations.ForeignKey(name="fk_lab_head_to_screening_room_user")
@org.hibernate.annotations.Proxy
public class LabHead extends ScreeningRoomUser
{
  // static members

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(LabHead.class);


  // instance data members

  private Set<Screen> _screensHeaded = new HashSet<Screen>();
  private Set<ScreeningRoomUser> _labMembers = new HashSet<ScreeningRoomUser>();
  private LabAffiliation _labAffiliation;

  // public constructors and methods

  public LabHead()
  {
    setUserClassification(ScreeningRoomUserClassification.PRINCIPAL_INVESTIGATOR);
  }

  public LabHead(String firstName,
                 String lastName,
                 String email,
                 String phone,
                 String mailingAddress,
                 String comments,
                 String commonsId,
                 String harvardId,
                 LabAffiliation labAffilliation)
  {
    super(firstName,
          lastName,
          email,
          phone,
          mailingAddress,
          comments,
          commonsId,
          harvardId,
          ScreeningRoomUserClassification.PRINCIPAL_INVESTIGATOR);
    _labAffiliation = labAffilliation;
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  public LabHead(String firstName,
                 String lastName,
                 String email,
                 LabAffiliation labAffilliation)
  {
    this(firstName,
         lastName,
         email,
         "",
         "",
         "",
         "",
         "",
         labAffilliation);
  }

  @Transient
  public boolean isHeadOfLab()
  {
    return true;
  }

  @Transient
  @Override
  public Lab getLab()
  {
    if (_lab == null) {
      _lab = new Lab(this);
      _lab.setLabAffiliation(_labAffiliation);
      _lab.setLabMembers(_labMembers);
    }
    return _lab;
  }

  @Override
  public void setUserClassification(ScreeningRoomUserClassification userClassification)
  {
    if (userClassification != ScreeningRoomUserClassification.PRINCIPAL_INVESTIGATOR) {
      throw new BusinessRuleViolationException("cannot change the classification of a principal investigator");
    }
    _userClassification = userClassification;
  }

  /**
   * Get the set of screens for which this user was the lab head.
   * @return the set of screens for which this user was the lab head
   */
  @OneToMany(
    mappedBy="labHead",
    fetch=FetchType.LAZY
  )
  @OrderBy("screenNumber")
  @edu.harvard.med.screensaver.model.annotations.OneToMany(singularPropertyName="screenHeaded")
  public Set<Screen> getScreensHeaded()
  {
    return _screensHeaded;
  }

  /**
   * Add the screens for which this user was the lab head.
   * @param screenHeaded the screens for which this user was the lab hea to add
   * @return true iff the screening room user did not already have the screens for which this user was the lab hea
   */
  public boolean addScreenHeaded(Screen screenHeaded)
  {
    if (_screensHeaded.add(screenHeaded)) {
      screenHeaded.setLabHead(this);
      return true;
    }
    return false;
  }

  /**
   * @return a Set of Screens comprised of the screens this user has headed, led and collaborated on.
   */
  @Transient
  @Override
  public Set<Screen> getAllAssociatedScreens()
  {
    Set<Screen> screens = super.getAllAssociatedScreens();
    screens.addAll(getScreensHeaded());
    return screens;
  }

  // private methods

  /**
   * Set the screens for which this user was the lab head.
   * @param screensHeaded the new screens for which this user was the lab head
   * @motivation for hibernate
   */
  private void setScreensHeaded(Set<Screen> screensHeaded)
  {
    _screensHeaded = screensHeaded;
  }

  /**
   * Get the set of lab members. The result will <i>not</i> contain this
   * LabHead.
   *
   * @return the set of lab members
   */
  @OneToMany(mappedBy = "labHead", targetEntity=ScreeningRoomUser.class, fetch = FetchType.LAZY)
  private Set<ScreeningRoomUser> getLabMembers()
  {
    if (_lab != null) {
      return _lab.getLabMembers();
    }
    else {
      return _labMembers;
    }
  }

  /**
   * Set the lab members.
   * @param labMembers the new lab members
   * @motivation for hibernate
   */
  private void setLabMembers(Set<ScreeningRoomUser> labMembers)
  {
    _labMembers = labMembers;
  }

  /**
   * Get the lab affiliation.
   *
   * @return the lab affiliation; always null unless the user is a lab head
   */
  @ManyToOne(fetch=FetchType.EAGER,
             cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="labAffiliationId", nullable=true)
  @org.hibernate.annotations.ForeignKey(name="fk_lab_head_to_lab_affiliation")
  //@org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE
  })
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(unidirectional=true)
  private LabAffiliation getLabAffiliation()
  {
    if (_lab != null) {
      return _lab.getLabAffiliation();
    }
    else {
      return _labAffiliation;
    }
  }

  /**
   * Set the lab affiliation.
   * @param labAffiliation the new lab affiliation
   */
  private void setLabAffiliation(LabAffiliation labAffiliation)
  {
    _labAffiliation = labAffiliation;
  }

  /**
   * Set the lab head.
   * @param labHead the new lab head
   */
  @Override
  protected void setLabHead(LabHead labHead)
  {
    if (labHead != null) {
      throw new DataModelViolationException("a lab head cannot itself have a lab head");
    }
    super.setLabHead(labHead);
  }
}
