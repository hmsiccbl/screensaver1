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

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a abase testset.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Screen.class)
public class AbaseTestset extends AbstractEntity
{

  // static fields

  private static final Logger log = Logger.getLogger(AbaseTestset.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _abaseTestsetId;
  private Integer _version;
  private Screen _screen;
  private Date _testsetDate;
  private String _testsetName;
  private String _comments;


  // public constructor

  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getAbaseTestsetId();
  }

  /**
   * Get the id for the abase testset.
   * @return the id for the abase testset
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="abase_testset_id_seq",
    strategy="sequence",
    parameters = {
      @org.hibernate.annotations.Parameter(name="sequence", value="abase_testset_id_seq")
    }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="abase_testset_id_seq")
  public Integer getAbaseTestsetId()
  {
    return _abaseTestsetId;
  }

  /**
   * Get the screen.
   * @return the screen
   */
  @ManyToOne(fetch=FetchType.LAZY,
             cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="screenId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_abase_testset_to_screen")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Get the testset date.
   * @return the testset date
   */
  @Column(nullable=false)
  public Date getTestsetDate()
  {
    return _testsetDate;
  }

  /**
   * Set the testset date.
   * @param testsetDate the new testset date
   */
  public void setTestsetDate(Date testsetDate)
  {
    _testsetDate = truncateDate(testsetDate);
  }

  /**
   * Get the testset name.
   * @return the testset name
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getTestsetName()
  {
    return _testsetName;
  }

  /**
   * Set the testset name.
   * @param testsetName the new testset name
   */
  public void setTestsetName(String testsetName)
  {
    _testsetName = testsetName;
  }

  /**
   * Get the comments.
   * @return the comments
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getComments()
  {
    return _comments;
  }

  /**
   * Set the comments.
   * @param comments the new comments
   */
  public void setComments(String comments)
  {
    _comments = comments;
  }


  // protected constructor

  /**
   * Construct an initialized <code>AbaseTestset</code>. Intended only for use with {@link
   * Screen#createAbaseTestset}.
   * @param screen the screen
   * @param testsetDate the testset date
   * @param testsetName the testset name
   * @param comments the comments
   */
  AbaseTestset(Screen screen, Date testsetDate, String testsetName, String comments)
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
    _testsetDate = testsetDate;
    _testsetName = testsetName;
    _comments = comments;
  }

  // protected constructor

  /**
   * Construct an uninitialized <code>AbaseTestset</code> object.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected AbaseTestset() {}


  // private constructor and instance methods

  /**
   * Set the screen.
   * @param screen the Screen
   * @motivation for hibernate
   */
  private void setScreen(Screen screen)
  {
    _screen = screen;
  }

  /**
   * Set the id for the abase testset.
   * @param abaseTestsetId the new id for the abase testset
   * @motivation for hibernate
   */
  private void setAbaseTestsetId(Integer abaseTestsetId)
  {
    _abaseTestsetId = abaseTestsetId;
  }

  /**
   * Get the version for the abase testset.
   * @return the version for the abase testset
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the abase testset.
   * @param version the new version for the abase testset
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }
}
