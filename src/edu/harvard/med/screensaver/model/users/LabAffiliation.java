// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;


/**
 * A Hibernate entity bean representing a lab affiliation.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
public class LabAffiliation extends AbstractEntity<Integer> implements Comparable<LabAffiliation>
{

  // static fields

  private static final Logger log = Logger.getLogger(LabAffiliation.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _version;
  private String _affiliationName;
  private AffiliationCategory _affiliationCategory;


  // public constructor


  /**
   * Construct an uninitialized <code>labAffiliation</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   * @motivation for UI entity creation
   */
  public LabAffiliation() {}

  /**
   * Construct an initialized <code>labAffiliation</code>.
   * @param affiliationName the affiliation name
   * @param affiliationCategory the affiliation category
   */
  public LabAffiliation(String affiliationName, AffiliationCategory affiliationCategory)
  {
    _affiliationName = affiliationName;
    _affiliationCategory = affiliationCategory;
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="lab_affiliation_id_seq",
    strategy="sequence",
    parameters = { @Parameter(name="sequence", value="lab_affiliation_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="lab_affiliation_id_seq")
  public Integer getLabAffiliationId()
  {
    return getEntityId();
  }

  private void setLabAffiliationId(Integer labAffiliationId)
  {
    setEntityId(labAffiliationId);
  }

  @Column(nullable=false, unique=true)
  @org.hibernate.annotations.Type(type="text")
  public String getAffiliationName()
  {
    return _affiliationName;
  }

  public void setAffiliationName(String affiliationName)
  {
    _affiliationName = affiliationName;
  }

  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.users.AffiliationCategory$UserType")
  public AffiliationCategory getAffiliationCategory()
  {
    return _affiliationCategory;
  }

  public void setAffiliationCategory(AffiliationCategory affiliationCategory)
  {
    _affiliationCategory = affiliationCategory;
  }

  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  private void setVersion(Integer version)
  {
    _version = version;
  }

  public int compareTo(LabAffiliation other)
  {
    if (other == null) {
      return 1;
    }
    return other.getAffiliationName().compareTo(other.getAffiliationName());
  }
}
