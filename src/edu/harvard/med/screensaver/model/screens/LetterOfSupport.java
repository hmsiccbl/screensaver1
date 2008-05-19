// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import javax.persistence.CascadeType;
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
import edu.harvard.med.screensaver.model.DuplicateEntityException;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;


/**
 * A Hibernate entity bean representing a letter of support.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Screen.class)
public class LetterOfSupport extends AbstractEntity
{

  // static fields

  private static final Logger log = Logger.getLogger(LetterOfSupport.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _letterOfSupportId;
  private Integer _version;
  private Screen _screen;
  private LocalDate _dateWritten;
  private String _writtenBy;


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
    return getLetterOfSupportId();
  }

  /**
   * Get the id for the letter of support.
   * @return the id for the letter of support
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="letter_of_support_id_seq",
    strategy="sequence",
    parameters = {
      @org.hibernate.annotations.Parameter(name="sequence", value="letter_of_support_id_seq")
    }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="letter_of_support_id_seq")
  public Integer getLetterOfSupportId()
  {
    return _letterOfSupportId;
  }

  /**
   * Get the screen.
   * @return the screen
   */
  @ManyToOne(fetch=FetchType.LAZY,
             cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="screenId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_letter_of_support_to_screen")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(inverseProperty="lettersOfSupport")
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Get the date written.
   * @return the date written
   */
  @Column(nullable=false)
  @Type(type="edu.harvard.med.screensaver.db.hibernate.LocalDateType")
  public LocalDate getDateWritten()
  {
    return _dateWritten;
  }

  /**
   * Set the date written.
   * @param dateWritten the new date written
   */
  public void setDateWritten(LocalDate dateWritten)
  {
    _dateWritten = dateWritten;
  }

  /**
   * Get the written by.
   * @return the written by
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getWrittenBy()
  {
    return _writtenBy;
  }

  /**
   * Set the written by.
   * @param writtenBy the new written by
   */
  public void setWrittenBy(String writtenBy)
  {
    _writtenBy = writtenBy;
  }


  // package constructor

  /**
   * Construct an initialized <code>LetterOfSupport</code>. Intended only for use by {@link
   * Screen#createLetterOfSupport}.
   * @param screen the screen
   * @param dateWritten the date written
   * @param writtenBy the written by
   */
  LetterOfSupport(
    Screen screen,
    LocalDate dateWritten,
    String writtenBy) throws DuplicateEntityException
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
    _dateWritten = dateWritten;
    _writtenBy = writtenBy;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>LetterOfSupport</code> object.
   * @motivation for hibernate and proxy/concrete subclass constructor
   */
  protected LetterOfSupport() {}


  // private constructor and instance methods

  /**
   * Set the screen.
   * @param screen the new screen
   * @motivation for hibernate
   */
  private void setScreen(Screen screen)
  {
    _screen = screen;
  }

  /**
   * Set the id for the letter of support.
   * @param letterOfSupportId the new id for the letter of support
   * @motivation for hibernate
   */
  private void setLetterOfSupportId(Integer letterOfSupportId)
  {
    _letterOfSupportId = letterOfSupportId;
  }

  /**
   * Get the version for the letter of support.
   * @return the version for the letter of support
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the letter of support.
   * @param version the new version for the letter of support
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }
}
