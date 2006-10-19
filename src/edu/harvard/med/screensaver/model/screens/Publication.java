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
 * A Hibernate entity bean representing a publication.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
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


  // public constructor

  /**
   * Constructs an initialized <code>Publication</code> object.
   *
   * @param screen the screen
   * @param pubmedId the pubmed id
   * @param yearPublished the year published
   * @param authors the authors
   * @param title the title
   * @throws DuplicateEntityException 
   */
  public Publication(
    Screen screen,
    String pubmedId,
    String yearPublished,
    String authors,
    String title) throws DuplicateEntityException
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
    _pubmedId = pubmedId;
    _yearPublished = yearPublished;
    _authors = authors;
    _title = title;
    if (!_screen.getPublications().add(this)) {
      throw new DuplicateEntityException(_screen, this);
    }
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getPublicationId();
  }

  /**
   * Get the id for the publication.
   *
   * @return the id for the publication
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="publication_id_seq"
   */
  public Integer getPublicationId()
  {
    return _publicationId;
  }

  /**
   * Get the screen.
   *
   * @return the screen
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.Screen"
   *   column="screen_id"
   *   not-null="true"
   *   foreign-key="fk_publication_to_screen"
   *   cascade="save-update"
   */
  @ToOneRelationship(nullable=false)
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Get the pubmed id.
   *
   * @return the pubmed id
   */
  public String getPubmedId()
  {
    return _pubmedId;
  }

  /**
   * Set the pubmed id.
   *
   * @param pubmedId the new pubmed id
   */
  public void setPubmedId(String pubmedId)
  {
    _screen.getPublications().remove(this);
    _pubmedId = pubmedId;
    _screen.getPublications().add(this);
  }

  /**
   * Get the year published.
   *
   * @return the year published
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getYearPublished()
  {
    return _yearPublished;
  }

  /**
   * Set the year published.
   *
   * @param yearPublished the new year published
   */
  public void setYearPublished(String yearPublished)
  {
    _yearPublished = yearPublished;
  }

  /**
   * Get the authors.
   *
   * @return the authors
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getAuthors()
  {
    return _authors;
  }

  /**
   * Set the authors.
   *
   * @param authors the new authors
   */
  public void setAuthors(String authors)
  {
    _authors = authors;
  }

  /**
   * Get the title.
   *
   * @return the title
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getTitle()
  {
    return _title;
  }

  /**
   * Set the title.
   *
   * @param title the new title
   */
  public void setTitle(String title)
  {
    _title = title;
  }


  // protected methods

  @Override
  protected Object getBusinessKey()
  {
    return getPubmedId();
  }


  // private constructor

  /**
   * Construct an uninitialized <code>Publication</code> object.
   *
   * @motivation for hibernate
   */
  private Publication() {}


  // private methods

  /**
   * Set the screen.
   *
   * @param screen the new screen
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  private void setScreen(Screen screen)
  {
    _screen = screen;
  }

  /**
   * Set the id for the publication.
   *
   * @param publicationId the new id for the publication
   * @motivation for hibernate
   */
  private void setPublicationId(Integer publicationId) {
    _publicationId = publicationId;
  }

  /**
   * Get the version for the publication.
   *
   * @return the version for the publication
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the publication.
   *
   * @param version the new version for the publication
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the pubmed id.
   *
   * @return the pubmed id
   * @hibernate.property
   *   column="pubmed_id"
   *   type="text"
   *   not-null="true"
   * @motivation for hibernate
   */
  private String getHbnPubmedId()
  {
    return _pubmedId;
  }

  /**
   * Set the pubmed id.
   *
   * @param pubmedId the new pubmed id
   * @motivation for hibernate
   */
  private void setHbnPubmedId(String pubmedId)
  {
    _pubmedId = pubmedId;
  }
}
