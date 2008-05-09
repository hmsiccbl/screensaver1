// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

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

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;


/**
 * A Hibernate entity bean representing a copy action.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@ContainedEntity(containingEntityClass=CopyInfo.class)
public class CopyAction extends AbstractEntity
{

  // static fields

  private static final Logger log = Logger.getLogger(CopyAction.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _copyActionId;
  private Integer _version;
  private CopyInfo _copyInfo;
  private String _description;
  private LocalDate _date;


  // public constructor

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getCopyActionId();
  }

  /**
   * Get the id for the copy action.
   * @return the id for the copy action
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="copy_action_id_seq",
    strategy="sequence",
    parameters = { @Parameter(name="sequence", value="copy_action_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="copy_action_id_seq")
  public Integer getCopyActionId()
  {
    return _copyActionId;
  }

  /**
   * Get the copy info.
   * @return the copy info
   */
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="copyInfoId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_copy_action_to_copy_info")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public CopyInfo getCopyInfo()
  {
    return _copyInfo;
  }

  /**
   * Get the description.
   * @return the description
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getDescription()
  {
    return _description;
  }

  /**
   * Set the description.
   * @param description the new description
   */
  public void setDescription(String description)
  {
    _description = description;
  }

  /**
   * Get the date.
   * @return the date
   */
  @Column(nullable=false)
  @Type(type="org.joda.time.contrib.hibernate.PersistentLocalDate")
  public LocalDate getDate()
  {
    return _date;
  }

  /**
   * Set the date.
   * @param date the new date
   */
  public void setDate(LocalDate date)
  {
    _date = date;
  }


  // package constructor

  /**
   * Construct a <code>CopyAction</code>.
   * @param copyInfo the copy info
   * @param description the description
   * @param date the date
   */
  CopyAction(
    CopyInfo copyInfo,
    String description,
    LocalDate date)
  {
    _copyInfo = copyInfo;
    _description = description;
    _date = date;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>CopyAction</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected CopyAction() {}


  // private constructor and instance methods

  /**
   * Set the id for the copy action.
   * @param copyActionId the new id for the copy action
   * @motivation for hibernate
   */
  private void setCopyActionId(Integer copyActionId)
  {
    _copyActionId = copyActionId;
  }

  /**
   * Get the version for the copy action.
   * @return the version for the copy action
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the copy action.
   * @param version the new version for the copy action
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the copy info.
   * @param copyInfo the new copy info
   * @motivation for hibernate
   */
  private void setCopyInfo(CopyInfo copyInfo)
  {
    _copyInfo = copyInfo;
  }
}
