// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;


/**
 * A Hibernate entity bean representing a equipment used.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=LabActivity.class)
public class EquipmentUsed extends AbstractEntity
{

  // private static date

  private static final Logger log = Logger.getLogger(EquipmentUsed.class);
  private static final long serialVersionUID = 0L;


  // private instance data

  private Integer _equipmentUsedId;
  private Integer _version;
  private LabActivity _labActivity;
  private String _equipment;
  private String _protocol;
  private String _description;


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getEquipmentUsedId();
  }

  /**
   * Get the id for the equipment used.
   * @return the id for the equipment used
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="equipment_used_id_seq",
    strategy="sequence",
    parameters = { @org.hibernate.annotations.Parameter(name="sequence", value="equipment_used_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="equipment_used_id_seq")
  public Integer getEquipmentUsedId()
  {
    return _equipmentUsedId;
  }

  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="labActivityId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_equipment_used_to_lab_activity")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ToOne(inverseProperty="equipmentUsed")
  public LabActivity getLabActivity()
  {
    return _labActivity;
  }

  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getEquipment()
  {
    return _equipment;
  }

  public void setEquipment(String equipment)
  {
    _equipment = equipment;
  }

  @org.hibernate.annotations.Type(type="text")
  public String getProtocol()
  {
    return _protocol;
  }

  public void setProtocol(String protocol)
  {
    _protocol = protocol;
  }

  @org.hibernate.annotations.Type(type="text")
  public String getDescription()
  {
    return _description;
  }

  public void setDescription(String description)
  {
    _description = description;
  }


  // package constructor

  /**
   * Construct an initialized <code>EquipmentUsed</code>. Intended only for use by {@link LabActivity}.
   * @param labActivity the screening room activity
   * @param equipment the equipment
   * @param protocol the protocol
   * @param description the description
   */
  EquipmentUsed(
    LabActivity labActivity,
    String equipment,
    String protocol,
    String description)
  {
    if (labActivity == null) {
      throw new NullPointerException();
    }
    _labActivity = labActivity;
    _equipment = equipment;
    _protocol = protocol;
    _description = description;
  }


  // protected constructor

  /**
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected EquipmentUsed() {}


  // private constructor and instance methods

  /**
   * @motivation for hibernate
   */
  private void setLabActivity(LabActivity labActivity)
  {
    _labActivity = labActivity;
  }

  /**
   * @motivation for hibernate
   */
  private void setEquipmentUsedId(Integer equipmentUsedId)
  {
    _equipmentUsedId = equipmentUsedId;
  }

  /**
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }
}
