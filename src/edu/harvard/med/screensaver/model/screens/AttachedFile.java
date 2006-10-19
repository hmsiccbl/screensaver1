// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;


import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.ToOneRelationship;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a attached file.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public class AttachedFile extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(AttachedFile.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _attachedFileId;
  private Integer _version;
  private Screen _screen;
  private String _filename;
  private String _fileContents;


  // public constructor

  /**
   * Constructs an initialized <code>AttachedFile</code> object.
   *
   * @param screen the screen
   * @param filename the filename
   * @param fileContents the file contents
   * @throws DuplicateEntityException 
   */
  public AttachedFile(
    Screen screen,
    String filename,
    String fileContents) throws DuplicateEntityException
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
    _filename = filename;
    _fileContents = fileContents;
    if (!_screen.getAttachedFiles().add(this)) {
      throw new DuplicateEntityException(_screen, this);
    }
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getAttachedFileId();
  }

  /**
   * Get the id for the attached file.
   *
   * @return the id for the attached file
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="attached_file_id_seq"
   */
  public Integer getAttachedFileId()
  {
    return _attachedFileId;
  }

  /**
   * Get the screen.
   *
   * @return the screen
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.Screen"
   *   column="screen_id"
   *   not-null="true"
   *   foreign-key="fk_attached_file_to_screen"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  @ToOneRelationship(nullable=false)
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Get the filename.
   *
   * @return the filename
   */
  public String getFilename()
  {
    return _filename;
  }

  /**
   * Set the filename.
   *
   * @param filename the new filename
   */
  public void setFilename(String filename)
  {
    _screen.getAttachedFiles().remove(this);
    _filename = filename;
    _screen.getAttachedFiles().add(this);
  }

  /**
   * Get the file contents.
   * TODO: this should really be a BLOB, not TEXT.
   * 
   * @return the file contents
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getFileContents()
  {
    return _fileContents;
  }

  /**
   * Set the file contents.
   *
   * @param fileContents the new file contents
   */
  public void setFileContents(String fileContents)
  {
    _fileContents = fileContents;
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
     * Get the filename.
     *
     * @return the filename
     */
    public String getFilename()
    {
      return _filename;
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
        getFilename().equals(that.getFilename());
    }

    @Override
    public int hashCode()
    {
      return
        getScreen().hashCode() +
        getFilename().hashCode();
    }

    @Override
    public String toString()
    {
      return getScreen() + ":" + getFilename();
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
  private void setScreen(Screen screen)
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>AttachedFile</code> object.
   *
   * @motivation for hibernate
   */
  private AttachedFile() {}


  // private methods

  /**
   * Set the id for the attached file.
   *
   * @param attachedFileId the new id for the attached file
   * @motivation for hibernate
   */
  private void setAttachedFileId(Integer attachedFileId) {
    _attachedFileId = attachedFileId;
  }

  /**
   * Get the version for the attached file.
   *
   * @return the version for the attached file
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the attached file.
   *
   * @param version the new version for the attached file
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the filename.
   *
   * @return the filename
   * @hibernate.property
   *   column="filename"
   *   type="text"
   *   not-null="true"
   * @motivation for hibernate
   */
  private String getHbnFilename()
  {
    return _filename;
  }

  /**
   * Set the filename.
   *
   * @param filename the new filename
   * @motivation for hibernate
   */
  private void setHbnFilename(String filename)
  {
    _filename = filename;
  }
}
