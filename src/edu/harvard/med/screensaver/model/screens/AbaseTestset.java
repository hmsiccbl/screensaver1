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

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;


/**
 * A Hibernate entity bean representing a abase testset.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
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

  /**
   * Constructs an initialized <code>AbaseTestset</code> object.
   *
   * @param screen the screen
   * @param testsetName the testset name
   */
  public AbaseTestset(
    Screen screen,
    String testsetName)
  {
    _screen = screen;
    _testsetName = testsetName;
    _screen.getHbnAbaseTestsets().add(this);
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getAbaseTestsetId();
  }

  /**
   * Get the id for the abase testset.
   *
   * @return the id for the abase testset
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="abase_testset_id_seq"
   */
  public Integer getAbaseTestsetId()
  {
    return _abaseTestsetId;
  }

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
   * Set the screen.
   *
   * @param screen the new screen
   */
  public void setScreen(Screen screen)
  {
    _screen.getHbnAbaseTestsets().remove(this);
    _screen = screen;
    screen.getHbnAbaseTestsets().add(this);
  }

  /**
   * Get the testset date.
   *
   * @return the testset date
   * @hibernate.property
   */
  public Date getTestsetDate()
  {
    return _testsetDate;
  }

  /**
   * Set the testset date.
   *
   * @param testsetDate the new testset date
   */
  public void setTestsetDate(Date testsetDate)
  {
    _testsetDate = truncateDate(testsetDate);
  }

  /**
   * Get the testset name.
   *
   * @return the testset name
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getTestsetName()
  {
    return _testsetName;
  }

  /**
   * Set the testset name.
   *
   * @param testsetName the new testset name
   */
  public void setTestsetName(String testsetName)
  {
    _screen.getHbnAbaseTestsets().remove(this);
    _testsetName = testsetName;
    _screen.getHbnAbaseTestsets().add(this);
  }

  /**
   * Get the comments.
   *
   * @return the comments
   * @hibernate.property
   *   type="text"
   */
  public String getComments()
  {
    return _comments;
  }

  /**
   * Set the comments.
   *
   * @param comments the new comments
   */
  public void setComments(String comments)
  {
    _comments = comments;
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
     * Get the testset name.
     *
     * @return the testset name
     */
    public String getTestsetName()
    {
      return _testsetName;
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
        getTestsetName().equals(that.getTestsetName());
    }

    @Override
    public int hashCode()
    {
      return
        getScreen().hashCode() +
        getTestsetName().hashCode();
    }

    @Override
    public String toString()
    {
      return getScreen() + ":" + getTestsetName();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }


  // package methods

  /**
   * Set the screen.
   * Throw a NullPointerException when the screen is null.
   *
   * @param screen the new screen
   * @throws NullPointerException when the screen is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setHbnScreen(Screen screen)
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>AbaseTestset</code> object.
   *
   * @motivation for hibernate
   */
  private AbaseTestset() {}


  // private methods

  /**
   * Set the id for the abase testset.
   *
   * @param abaseTestsetId the new id for the abase testset
   * @motivation for hibernate
   */
  private void setAbaseTestsetId(Integer abaseTestsetId) {
    _abaseTestsetId = abaseTestsetId;
  }

  /**
   * Get the version for the abase testset.
   *
   * @return the version for the abase testset
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the abase testset.
   *
   * @param version the new version for the abase testset
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the screen.
   *
   * @return the screen
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.Screen"
   *   column="screen_id"
   *   not-null="true"
   *   foreign-key="fk_abase_testset_to_screen"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private Screen getHbnScreen()
  {
    return _screen;
  }
}
