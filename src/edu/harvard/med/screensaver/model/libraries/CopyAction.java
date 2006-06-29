// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.Date;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;


/**
 * A Hibernate entity bean representing a copy action.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
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
  private Date _date;


  // public constructor

  /**
   * Constructs an initialized <code>CopyAction</code> object.
   *
   * @param copyInfo the copy info
   * @param description the description
   * @param date the date
   */
  public CopyAction(
    CopyInfo copyInfo,
    String description,
    Date date)
  {
    // TODO: verify the order of assignments here is okay
    _copyInfo = copyInfo;
    _description = description;
    _date = date;
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getCopyActionId();
  }

  /**
   * Get the id for the copy action.
   *
   * @return the id for the copy action
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="copy_action_id_seq"
   */
  public Integer getCopyActionId()
  {
    return _copyActionId;
  }

  /**
   * Get the copy info.
   *
   * @return the copy info
   */
  public CopyInfo getCopyInfo()
  {
    return _copyInfo;
  }

  /**
   * Set the copy info.
   *
   * @param copyInfo the new copy info
   */
  public void setCopyInfo(CopyInfo copyInfo)
  {
    _copyInfo.getHbnCopyActions().remove(this);
    _copyInfo = copyInfo;
    copyInfo.getHbnCopyActions().add(this);
  }

  /**
   * Get the description.
   *
   * @return the description
   */
  public String getDescription()
  {
    return _description;
  }

  /**
   * Set the description.
   *
   * @param description the new description
   */
  public void setDescription(String description)
  {
    _copyInfo.getHbnCopyActions().remove(this);
    _description = description;
    _copyInfo.getHbnCopyActions().add(this);
  }

  /**
   * Get the date.
   *
   * @return the date
   * @hibernate.property
   *   not-null="true"
   */
  public Date getDate()
  {
    return _date;
  }

  /**
   * Set the date.
   *
   * @param date the new date
   */
  public void setDate(Date date)
  {
    _date = date;
  }


  // protected methods

  /**
   * A business key class for the well.
   */
  private class BusinessKey
  {
    
    /**
     * Get the copy info.
     *
     * @return the copy info
     */
    public CopyInfo getCopyInfo()
    {
      return _copyInfo;
    }
    
    /**
     * Get the description.
     *
     * @return the description
     */
    public String getDescription()
    {
      return _description;
    }

    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        getCopyInfo().equals(that.getCopyInfo()) &&
        getDescription().equals(that.getDescription());
    }

    @Override
    public int hashCode()
    {
      return
        getCopyInfo().hashCode() +
        getDescription().hashCode();
    }

    @Override
    public String toString()
    {
      return getCopyInfo() + ":" + getDescription();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    // TODO: assure changes to business key update relationships whose other side is many
    return new BusinessKey();
  }


  // package methods

  /**
   * Set the copy info.
   * Throw a NullPointerException when the copy info is null.
   *
   * @param copyInfo the new copy info
   * @throws NullPointerException when the copy info is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setHbnCopyInfo(CopyInfo copyInfo)
  {
    if (copyInfo == null) {
      throw new NullPointerException();
    }
    _copyInfo = copyInfo;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>CopyAction</code> object.
   *
   * @motivation for hibernate
   */
  private CopyAction() {}


  // private methods

  /**
   * Set the id for the copy action.
   *
   * @param copyActionId the new id for the copy action
   * @motivation for hibernate
   */
  private void setCopyActionId(Integer copyActionId) {
    _copyActionId = copyActionId;
  }

  /**
   * Get the version for the copy action.
   *
   * @return the version for the copy action
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the copy action.
   *
   * @param version the new version for the copy action
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the copy info.
   *
   * @return the copy info
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.libraries.CopyInfo"
   *   column="copy_info_id"
   *   not-null="true"
   *   foreign-key="fk_copy_action_to_copy_info"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private CopyInfo getHbnCopyInfo()
  {
    return _copyInfo;
  }
  
  /**
   * Get the description.
   *
   * @return the description
   * @hibernate.property
   *   column="description"
   *   type="text"
   *   not-null="true"
   * @motivation for hibernate
   */
  private String getHbnDescription()
  {
    return _description;
  }

  /**
   * Set the description.
   *
   * @param description the new description
   * @motivation for hibernate
   */
  private void setHbnDescription(String description)
  {
    _description = description;
  }
}
