// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

/**
 * The funding support vocabulary.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@Immutable
@org.hibernate.annotations.Proxy
public class FundingSupport extends AbstractEntity<Integer> implements Comparable<FundingSupport>
{
  private static final long serialVersionUID = 1L;
  
  private String _value;

  private FundingSupport()
  {
  }

  public FundingSupport(String value)
  {
    _value = value;
  }

  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="funding_support_id_seq",
    strategy="sequence",
    parameters = { @Parameter(name="sequence", value="funding_support_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="funding_support_id_seq")
  public Integer getFundingSupportId()
  {
    return getEntityId();
  }
  
  private void setFundingSupportId(Integer fundingSupportId)
  {
    setEntityId(fundingSupportId);
  }

  @Column(unique=true)
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

  public int compareTo(FundingSupport other)
  {
    if (other == null) { 
      return 1;
    }
    return this.getValue().compareTo(other.getValue());
  }
}
