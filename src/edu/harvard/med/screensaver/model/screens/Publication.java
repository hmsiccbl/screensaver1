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
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;


/**
 * A Hibernate entity bean representing a publication.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Screen.class)
public class Publication extends AbstractEntity
{

  // static fields

  private static final Logger log = Logger.getLogger(Publication.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _publicationId;
  private Integer _version;
  private Screen _screen;
  private String _pubmedId;
  private String _yearPublished;
  private String _authors;
  private String _title;


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
    return getPublicationId();
  }

  /**
   * Get the id for the publication.
   * @return the id for the publication
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="publication_id_seq",
    strategy="sequence",
    parameters = {
      @org.hibernate.annotations.Parameter(name="sequence", value="publication_id_seq")
    }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="publication_id_seq")
  public Integer getPublicationId()
  {
    return _publicationId;
  }

  /**
   * Get the screen.
   * @return the screen
   */
  @ManyToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="screenId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_publication_to_screen")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Get the pubmed id.
   * @return the pubmed id
   */
  @org.hibernate.annotations.Type(type="text")
  public String getPubmedId()
  {
    return _pubmedId;
  }

  /**
   * Set the pubmed id.
   * @param pubmedId the new pubmed id
   */
  public void setPubmedId(String pubmedId)
  {
    _pubmedId = pubmedId;
  }

  /**
   * Get the year published.
   * @return the year published
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getYearPublished()
  {
    return _yearPublished;
  }

  /**
   * Set the year published.
   * @param yearPublished the new year published
   */
  public void setYearPublished(String yearPublished)
  {
    _yearPublished = yearPublished;
  }

  /**
   * Get the authors.
   * @return the authors
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getAuthors()
  {
    return _authors;
  }

  /**
   * Set the authors.
   * @param authors the new authors
   */
  public void setAuthors(String authors)
  {
    _authors = authors;
  }

  /**
   * Get the title.
   * @return the title
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getTitle()
  {
    return _title;
  }

  /**
   * Set the title.
   * @param title the new title
   * @motivation for hibernate
   */
  public void setTitle(String title)
  {
    _title = title;
  }


  // package constructor

  /**
   * Construct an initialized <code>Publication</code>. Intended for use only by {@link
   * Screen#createPublication}.
   * @param screen the screen
   * @param pubmedId the pubmed id
   * @param yearPublished the year published
   * @param authors the authors
   * @param title the title
   */
  Publication(Screen screen, String pubmedId, String yearPublished, String authors, String title)
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
    _pubmedId = pubmedId;
    _yearPublished = yearPublished;
    _authors = authors;
    _title = title;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>Publication</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected Publication() {}


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
   * Set the id for the publication.
   * @param publicationId the new id for the publication
   * @motivation for hibernate
   */
  private void setPublicationId(Integer publicationId)
  {
    _publicationId = publicationId;
  }

  /**
   * Get the version for the publication.
   * @return the version for the publication
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the publication.
   * @param version the new version for the publication
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }
}
