// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.SemanticIDAbstractEntity;


/**
 * A Hibernate entity bean representing a lab affiliation.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
public class LabAffiliation extends SemanticIDAbstractEntity implements Comparable<LabAffiliation>
{

  // static fields

  private static final Logger log = Logger.getLogger(LabAffiliation.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _version;
  private Set<ScreeningRoomUser> _screeningRoomUsers = new HashSet<ScreeningRoomUser>();
  private String _affiliationName;
  private AffiliationCategory _affiliationCategory;


  // public constructor

  /**
   * Construct an initialized <code>LabAffiliation</code>.
   * @param affiliationName the affiliation name
   * @param affiliationCategory the affiliation category
   */
  public LabAffiliation(String affiliationName, AffiliationCategory affiliationCategory)
  {
    _affiliationName = affiliationName;
    _affiliationCategory = affiliationCategory;
  }


  // public methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public String getEntityId()
  {
    return getLabAffiliationId();
  }

  /**
   * Get the id for the lab affiliation.
   * @return the id for the lab affiliation
   */
  @Id
  @org.hibernate.annotations.Type(type="text")
  public String getLabAffiliationId()
  {
    return getAffiliationName();
  }

  /**
   * Get the affiliation name.
   * @return the affiliation name
   */
  @org.hibernate.annotations.Immutable
  @Column(nullable=false, unique=true)
  @org.hibernate.annotations.Type(type="text")
  public String getAffiliationName()
  {
    return _affiliationName;
  }

  /**
   * Get the affiliation category.
   * @return the affiliation category
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.users.AffiliationCategory$UserType")
  public AffiliationCategory getAffiliationCategory()
  {
    return _affiliationCategory;
  }

  /**
   * Set the affiliation category.
   * @param affiliationCategory the new affiliation category
   */
  public void setAffiliationCategory(AffiliationCategory affiliationCategory)
  {
    _affiliationCategory = affiliationCategory;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>LabAffiliation</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected LabAffiliation() {}


  // private instance methods

  /**
   * Set the id for the lab affiliation.
   * @param labAffiliationId the new id for the lab affiliation
   * @motivation for hibernate
   */
  private void setLabAffiliationId(String labAffiliationId)
  {
  }

  /**
   * Get the version for the lab affiliation.
   * @return the version for the lab affiliation
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the lab affiliation.
   * @param version the new version for the lab affiliation
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the affiliation name.
   * @param affiliationName the new affiliation name
   * @motivation for hibernate - nobody but hibernate would think of calling a setter on an
   * Immutable property!
   */
  private void setAffiliationName(String affiliationName)
  {
    _affiliationName = affiliationName;
  }


  public int compareTo(LabAffiliation other)
  {
    if (other == null) {
      return 1;
    }
    return other.getAffiliationName().compareTo(other.getAffiliationName());
  }
}
