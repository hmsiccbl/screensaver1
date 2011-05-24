// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.util.NullSafeComparator;
import edu.harvard.med.screensaver.util.StringUtils;


@Entity
@org.hibernate.annotations.Proxy
public class Publication extends AbstractEntity<Integer> implements Comparable<Publication>
{
  private static final Logger log = Logger.getLogger(Publication.class);
  private static final long serialVersionUID = 0L;
  public static final String PUBLICATION_ATTACHED_FILE_TYPE_VALUE = "Publication";
  static private Pattern CleanTitlePattern = Pattern.compile("(.+)\\. *");

  private Integer _publicationId;
  private Integer _pubmedCentralId;
  private Integer _version;
  private Integer _pubmedId;
  private String _yearPublished;
  private String _authors;
  private String _title;
  private String _journal;
  private String _volume;
  private String _pages;
  private AttachedFile _attachedFile;


  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

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
    return getEntityId();
  }

  public Integer getPubmedId()
  {
    return _pubmedId;
  }

  public void setPubmedId(Integer pubmedId)
  {
    _pubmedId = pubmedId;
  }

  @org.hibernate.annotations.Type(type="text")
  public String getYearPublished()
  {
    return _yearPublished;
  }

  public void setYearPublished(String yearPublished)
  {
    _yearPublished = yearPublished;
  }

  @org.hibernate.annotations.Type(type="text")
  public String getAuthors()
  {
    return _authors;
  }

  public void setAuthors(String authors)
  {
    _authors = authors;
  }

  @org.hibernate.annotations.Type(type="text")
  public String getTitle()
  {
    return _title;
  }

  public void setTitle(String title)
  {
    _title = title;
  }
  
  // note: attached file persistence is managed by Screen, and should be created via Screen.createAttachedFile
  @OneToOne(cascade={}, fetch=FetchType.LAZY)
  @JoinColumn(name="attachedFileId", nullable=true, updatable=true, unique=true)
  @org.hibernate.annotations.ForeignKey(name="fk_publication_to_attached_file")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ToOne(unidirectional=true, 
                                                       hasNonconventionalSetterMethod=true)
  public AttachedFile getAttachedFile()
  {
    return _attachedFile;
  }
  
  public void setAttachedFile(AttachedFile attachedFile)
  {
    _attachedFile = attachedFile; 
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
      String title = _title;
      Matcher matcher = CleanTitlePattern.matcher(_title);
      if (matcher.matches()) {
        title = matcher.group(1);
      }
      citation.append(title).append(". ");
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
   * @motivation for hibernate
   */
  private void setPublicationId(Integer publicationId)
  {
    setEntityId(publicationId);
  }

  /**
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  public Integer getPubmedCentralId()
  {
    return _pubmedCentralId;
  }

  public void setPubmedCentralId(Integer pubmedCentralId)
  {
    _pubmedCentralId = pubmedCentralId;
  }

  NullSafeComparator<Integer> comparator = new NullSafeComparator<Integer>() {

    @Override
    protected int doCompare(Integer o1, Integer o2)
    {
      return o1.compareTo(o2);
    }
  };

  @Override
  public int compareTo(Publication o)
  {
    return comparator.compare(this.getEntityId(), o.getEntityId());
  }
}
