// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;


import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

/**
 * The attached file type vocabulary.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@Immutable
@org.hibernate.annotations.Proxy
@DiscriminatorColumn(discriminatorType=DiscriminatorType.STRING, name="forEntityType")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class AttachedFileType extends AbstractEntity<Integer> implements Comparable<AttachedFileType>
{
  private static final long serialVersionUID = 1L;

  private String _value;

  /**
   * @motivation for hibernate
   */
  protected AttachedFileType()
  {
  }
  
  /**
   * Constructs an <code>AttachedFileType</code> vocabulary term.
   * @param value The value of the term.
   */
  public AttachedFileType(String value)
  {
    _value = value;
  }

  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="attached_file_type_id_seq",
    strategy="sequence",
    parameters = { @Parameter(name="sequence", value="attached_file_type_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="attached_file_type_id_seq")
  public Integer getAttachedFileTypeId()
  {
    return getEntityId();
  }
  
  private void setAttachedFileTypeId(Integer attachedFileTypeId)
  {
    setEntityId(attachedFileTypeId);
  }

  @Column(unique=true, nullable=false)
  @Type(type="text")
  public String getValue()
  {
    return _value;
  }

  private void setValue(String value)
  {
    _value = value;
  }

  @Override
  public String toString()
  {
    return getValue();
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  public int compareTo(AttachedFileType other)
  {
    if (other == null) { 
      return 1;
    }
    return this.getValue().compareTo(other.getValue());
  }
}
