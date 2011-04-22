// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;


import java.io.IOException;
import java.io.InputStream;
import java.util.SortedSet;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;

import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.annotations.ToOne;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

/**
 * A file (document or data file) that can be associated with either a {@link Screen} or {@link ScreeningRoomUser}.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@Table(uniqueConstraints={ @UniqueConstraint(columnNames={ "screenId", "screensaverUserId", "filename" }) })
@org.hibernate.annotations.Proxy
@ContainedEntity(containingEntityClass = Screen.class, containingEntityClasses = { Screen.class, ScreeningRoomUser.class })
public class AttachedFile extends AuditedAbstractEntity<Integer> implements Comparable<AttachedFile>
{

  // static fields

  private static final Logger log = Logger.getLogger(AttachedFile.class);
  private static final long serialVersionUID = 0L;

  public static final RelationshipPath<AttachedFile> fileType = RelationshipPath.from(AttachedFile.class).to("fileType", Cardinality.TO_ONE);
  public static final RelationshipPath<AttachedFile> screen = RelationshipPath.from(AttachedFile.class).to("screen", Cardinality.TO_ONE);
  public static final RelationshipPath<AttachedFile> screeningRoomUser = RelationshipPath.from(AttachedFile.class).to("screeningRoomUser", Cardinality.TO_ONE);


  // instance fields

  private Integer _version;
  private Screen _screen;
  private ScreeningRoomUser _screeningRoomUser;
  private String _filename;
  private AttachedFileType _fileType;
  private byte[] _fileContents;


  // public constructor

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
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
    return getEntityId();
  }

  @ManyToMany(fetch = FetchType.LAZY, cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable(name="attachedFileUpdateActivity", 
             joinColumns=@JoinColumn(name="attachedFileId", nullable=false, updatable=false),
             inverseJoinColumns=@JoinColumn(name="updateActivityId", nullable=false, updatable=false, unique=true))
  @org.hibernate.annotations.Cascade(value={org.hibernate.annotations.CascadeType.SAVE_UPDATE})
  @Sort(type=SortType.NATURAL)            
  @ToMany(singularPropertyName="updateActivity", hasNonconventionalMutation=true /* model testing framework doesn't understand this is a containment relationship, and so requires addUpdateActivity() method*/)
  @Override
  public SortedSet<AdministrativeActivity> getUpdateActivities()
  {
    return _updateActivities;
  }

  @ManyToOne(fetch=FetchType.LAZY,
             cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="screenId", nullable=true, updatable=false)
  @org.hibernate.annotations.ForeignKey(name="fk_attached_file_to_screen")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @ToOne(hasNonconventionalSetterMethod=true /* mutually-exclusive parenting relationships */) 
  public Screen getScreen()
  {
    return _screen;
  }

  private void setScreen(Screen screen)
  {
    _screen = screen;
  }

  @ManyToOne(fetch=FetchType.LAZY,
             cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="screensaverUserId", nullable=true, updatable=false)
  @org.hibernate.annotations.ForeignKey(name="fk_attached_file_to_screening_room_user")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @ToOne(hasNonconventionalSetterMethod=true /* mutually-exclusive parenting relationships */) 
  public ScreeningRoomUser getScreeningRoomUser()
  {
    return _screeningRoomUser;
  }

  private void setScreeningRoomUser(ScreeningRoomUser screeningRoomUser)
  {
    _screeningRoomUser = screeningRoomUser;
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

  @ManyToOne(fetch=FetchType.LAZY, cascade={ CascadeType.MERGE })
  @JoinColumn(name="attachedFileTypeId", nullable=false)
  @org.hibernate.annotations.ForeignKey(name="fk_attached_file_to_attached_file_type")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ToOne(unidirectional=true)
  public AttachedFileType getFileType()
  {
    return _fileType;
  }

  public void setFileType(AttachedFileType fileType)
  {
    _fileType = fileType;
  }

  /**
   * Get the file contents.
   * @return the file contents
   */
  @Lob
  @Basic(fetch = FetchType.LAZY)
  @Column(nullable = false, updatable = false)
  @Type(type = "org.hibernate.type.PrimitiveByteArrayBlobType")
  public byte[] getFileContents()
  {
    return _fileContents;
  }

  /**
   * Set the file contents.
   * @param fileContents the new file contents
   */
  private void setFileContents(byte[] fileContents)
  {
    _fileContents = fileContents;
  }


  // package constructor

  /**
   * Construct an initialized <code>AttachedFile</code>. Intended only for use
   * by {@link Screen}; use
   * {@link Screen#createAttachedFile(String, AttachedFileType, InputStream)} or
   * {@link Screen#createAttachedFile(String, AttachedFileType, String)}.
   * 
   * @param screen the screen
   * @param filename the filename
   * @param fileContents the file contents
   * @throws IOException
   */
  public AttachedFile(Screen screen, String filename, AttachedFileType fileType, InputStream fileContents) throws IOException
  {
    super(null); /* TODO */
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
    _filename = filename;
    _fileType = fileType;
    _fileContents = IOUtils.toByteArray(fileContents);
  }

  /**
   * Construct an initialized <code>AttachedFile</code>. Intended only for use
   * by {@link ScreeningRoomUser}; use
   * {@link ScreeningRoomUser#createAttachedFile(String, AttachedFileType, InputStream)} or
   * {@link ScreeningRoomUser#createAttachedFile(String, AttachedFileType, String)}.
   * 
   * @param screeningRoomUser the screening room user
   * @param filename the filename
   * @param fileContents the file contents
   * @throws IOException
   */
  public AttachedFile(ScreeningRoomUser screeningRoomUser, String filename, AttachedFileType fileType, InputStream fileContents) throws IOException
  {
    super(null); /* TODO */
    if (screeningRoomUser == null) {
      throw new NullPointerException();
    }
    _screeningRoomUser = screeningRoomUser;
    _filename = filename;
    _fileType = fileType;
    _fileContents = IOUtils.toByteArray(fileContents);

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
    setEntityId(attachedFileId);
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

  private void setScreeningroomUser(ScreeningRoomUser screeningRoomUser)
  {
    _screeningRoomUser = screeningRoomUser;
  }

  public int compareTo(AttachedFile other)
  {
    int result = getDateCreated().compareTo(other.getDateCreated());
    if (result == 0) {
      result = getFileType().compareTo(other.getFileType());
      if (result == 0) {
        result = getFilename().compareTo(other.getFilename());
      }
    }
    return result;
  }
}
