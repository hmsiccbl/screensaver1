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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;


/**
 * A Hibernate entity bean representing a attached file.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@Table(uniqueConstraints={ @UniqueConstraint(columnNames={ "screenId", "filename" }) })
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Screen.class)
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

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getAttachedFileId();
  }

  /**
   * Get the id for the attached file.
   * @return the id for the attached file
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="attached_file_id_seq",
    strategy="sequence",
    parameters = {
      @org.hibernate.annotations.Parameter(name="sequence", value="attached_file_id_seq")
    }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="attached_file_id_seq")
  public Integer getAttachedFileId()
  {
    return _attachedFileId;
  }

  /**
   * Get the screen.
   * @return the screen
   */
  @ManyToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="screenId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_attached_file_to_screen")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Get the filename.
   * @return the filename
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getFilename()
  {
    return _filename;
  }

  /**
   * Set the filename.
   * @param filename the new filename
   */
  public void setFilename(String filename)
  {
    _filename = filename;
  }

  /**
   * Get the file contents.
   * TODO: this should really be a BLOB, not TEXT.
   * @return the file contents
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getFileContents()
  {
    return _fileContents;
  }

  /**
   * Set the file contents.
   * @param fileContents the new file contents
   */
  public void setFileContents(String fileContents)
  {
    _fileContents = fileContents;
  }


  // package constructor

  /**
   * Construct an initialized <code>AttachedFile</code>. Intended only for use by {@link
   * Screen#createAttachedFile}.
   * @param screen the screen
   * @param filename the filename
   * @param fileContents the file contents
   */
  AttachedFile(Screen screen, String filename, String fileContents)
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
    _filename = filename;
    _fileContents = fileContents;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>AttachedFile</code> object.
   *
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected AttachedFile() {}


  // private constructor and instance methods

  /**
   * Set the id for the attached file.
   * @param attachedFileId the new id for the attached file
   * @motivation for hibernate
   */
  private void setAttachedFileId(Integer attachedFileId)
  {
    _attachedFileId = attachedFileId;
  }

  /**
   * Get the version for the attached file.
   * @return the version for the attached file
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the attached file.
   * @param version the new version for the attached file
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the screen.
   * @param screen the new screen
   * @motivation for hibernate
   */
  private void setScreen(Screen screen)
  {
    _screen = screen;
  }
}
