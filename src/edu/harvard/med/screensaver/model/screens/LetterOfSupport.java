// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Date;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.ToOneRelationship;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a letter of support.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public class LetterOfSupport extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(LetterOfSupport.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _letterOfSupportId;
  private Integer _version;
  private Screen _screen;
  private Date _dateWritten;
  private String _writtenBy;


  // public constructor

  /**
   * Constructs an initialized <code>LetterOfSupport</code> object.
   *
   * @param screen the screen
   * @param dateWritten the date written
   * @param writtenBy the written by
   * @throws DuplicateEntityException 
   */
  public LetterOfSupport(
    Screen screen,
    Date dateWritten,
    String writtenBy) throws DuplicateEntityException
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
    _dateWritten = truncateDate(dateWritten);
    _writtenBy = writtenBy;
    if (!_screen.getLettersOfSupport().add(this)) {
      throw new DuplicateEntityException(_screen, this);
    }
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getLetterOfSupportId();
  }

  /**
   * Get the id for the letter of support.
   *
   * @return the id for the letter of support
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="letter_of_support_id_seq"
   */
  public Integer getLetterOfSupportId()
  {
    return _letterOfSupportId;
  }

  /**
   * Get the screen.
   *
   * @return the screen
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.Screen"
   *   column="screen_id"
   *   not-null="true"
   *   foreign-key="fk_letter_of_support_to_screen"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  @ToOneRelationship(nullable=false)
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Get the date written.
   *
   * @return the date written
   */
  public Date getDateWritten()
  {
    return _dateWritten;
  }

  /**
   * Set the date written.
   *
   * @param dateWritten the new date written
   */
  public void setDateWritten(Date dateWritten)
  {
    _screen.getLettersOfSupport().remove(this);
    _dateWritten = truncateDate(dateWritten);
    _screen.getLettersOfSupport().add(this);
  }

  /**
   * Get the written by.
   *
   * @return the written by
   */
  public String getWrittenBy()
  {
    return _writtenBy;
  }

  /**
   * Set the written by.
   *
   * @param writtenBy the new written by
   */
  public void setWrittenBy(String writtenBy)
  {
    _screen.getLettersOfSupport().remove(this);
    _writtenBy = writtenBy;
    _screen.getLettersOfSupport().add(this);
  }


  // protected methods

  /**
   * A business key class for the well.
   */
  private class BusinessKey
  {
    
    /**
     * Get the screen.
     *
     * @return the screen
     */
    public Screen getScreen()
    {
      return _screen;
    }
    
    /**
     * Get the date written.
     *
     * @return the date written
     */
    public Date getDateWritten()
    {
      return _dateWritten;
    }
    
    /**
     * Get the written by.
     *
     * @return the written by
     */
    public String getWrittenBy()
    {
      return _writtenBy;
    }
    
    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        getScreen().equals(that.getScreen()) &&
        getDateWritten().equals(that.getDateWritten()) &&
        getWrittenBy().equals(that.getWrittenBy());
    }

    @Override
    public int hashCode()
    {
      return
        getScreen().hashCode() +
        getDateWritten().hashCode() +
        getWrittenBy().hashCode();
    }

    @Override
    public String toString()
    {
      return getScreen() + ":" + getDateWritten() + ":" + getWrittenBy();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }


  // private constructor

  /**
   * Construct an uninitialized <code>LetterOfSupport</code> object.
   *
   * @motivation for hibernate
   */
  private LetterOfSupport() {}


  // private methods

  /**
   * Set the screen.
   * Throw a NullPointerException when the screen is null.
   *
   * @param screen the new screen
   * @throws NullPointerException when the screen is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  private void setScreen(Screen screen)
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
  }

  /**
   * Set the id for the letter of support.
   *
   * @param letterOfSupportId the new id for the letter of support
   * @motivation for hibernate
   */
  private void setLetterOfSupportId(Integer letterOfSupportId) {
    _letterOfSupportId = letterOfSupportId;
  }

  /**
   * Get the version for the letter of support.
   *
   * @return the version for the letter of support
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the letter of support.
   *
   * @param version the new version for the letter of support
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the date written.
   *
   * @return the date written
   * @hibernate.property
   *   column="date_written"
   *   not-null="true"
   * @motivation for hibernate
   */
  private Date getHbnDateWritten()
  {
    return _dateWritten;
  }

  /**
   * Set the date written.
   *
   * @param dateWritten the new date written
   * @motivation for hibernate
   */
  private void setHbnDateWritten(Date dateWritten)
  {
    _dateWritten = dateWritten;
  }

  /**
   * Get the written by.
   *
   * @return the written by
   * @hibernate.property
   *   column="written_by"
   *   type="text"
   *   not-null="true"
   * @motivation for hibernate
   */
  private String getHbnWrittenBy()
  {
    return _writtenBy;
  }

  /**
   * Set the written by.
   *
   * @param writtenBy the new written by
   * @motivation for hibernate
   */
  private void setHbnWrittenBy(String writtenBy)
  {
    _writtenBy = writtenBy;
  }
}
