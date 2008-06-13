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
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;


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
  private Integer _pubmedId;
  private String _yearPublished;
  private String _authors;
  private String _title;
  private String _journal;
  private String _volume;
  private String _pages;


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
  @ManyToOne(fetch=FetchType.LAZY,
             cascade={ CascadeType.PERSIST, CascadeType.MERGE })
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
  public Integer getPubmedId()
  {
    return _pubmedId;
  }

  /**
   * Set the pubmed id.
   * @param pubmedId the new pubmed id
   */
  public void setPubmedId(Integer pubmedId)
  {
    _pubmedId = pubmedId;
  }

  /**
   * Get the year published.
   * @return the year published
   */
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

  /**
   * @return The citation text for this publication, formatted similarly to the
   *         citations in Cell (journal).
   */
  @Transient
  public String getCitation()
  {
    StringBuilder citation = new StringBuilder();
    if (_authors != null) {
      citation.append(_authors).append(' ');
    }
    if (!StringUtils.isEmpty(_yearPublished)) {
      citation.append('(').append(_yearPublished).append("). ");
    }
    if (!StringUtils.isEmpty(_title)) {
      citation.append(_title).append(". ");
    }
    if (!StringUtils.isEmpty(_journal)) {
      citation.append(_journal).append(' ');
    }
    if (!StringUtils.isEmpty(_volume)) {
      citation.append(_volume).append(", ");
    }
    if (!StringUtils.isEmpty(_pages)) {
      citation.append(_pages).append('.');
    }
    return citation.toString();
  }


  // package constructor

  @org.hibernate.annotations.Type(type="text")
  public String getJournal()
  {
    return _journal;
  }

  public void setJournal(String journal)
  {
    _journal = journal;
  }

  @org.hibernate.annotations.Type(type="text")
  public String getVolume()
  {
    return _volume;
  }

  public void setVolume(String volume)
  {
    _volume = volume;
  }

  @org.hibernate.annotations.Type(type="text")
  public String getPages()
  {
    return _pages;
  }

  public void setPages(String pages)
  {
    _pages = pages;
  }

  /**
   * Construct an initialized <code>Publication</code>. Intended for use only by {@link
   * Screen#createPublication}.
   * @param screen the screen
   */
  Publication(Screen screen)
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>Publication</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  public Publication() {}


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
