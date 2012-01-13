// $HeadURL:
// http://forge.abcd.harvard.edu/svn/screensaver/branches/atolopko/2189/core/src/main/java/edu/harvard/med/screensaver/model/activities/AT.java
// $
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.activities;

import javax.persistence.Transient;

import com.google.common.base.Predicate;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

/**
 * Represents an activity the maintains a type qualifier.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public abstract class TypedActivity<AT extends VocabularyTerm> extends Activity
{
  private static final long serialVersionUID = 1L;

  public static Predicate<TypedActivity> IsOfType(final VocabularyTerm type)
  {
    return new Predicate<TypedActivity>() {
      @Override
      public boolean apply(TypedActivity a)
      {
        return a.getType() == type;
      }
    };
  }

  protected AT _type;

  @Override
  @Transient
  public String getActivityTypeName()
  {
    if (getType() == null) {
      return null;
    }
    return getType().getValue();
  }
  
  public abstract AT getType();

  public TypedActivity(AdministratorUser recordedBy,
                       LocalDate dateOfActivity,
                       AT type)
  {
    this(recordedBy, recordedBy, dateOfActivity, type);
  }

  public TypedActivity(AdministratorUser recordedBy,
                       AdministratorUser performedBy,
                       LocalDate dateOfActivity,
                       AT type)
  {
    super(recordedBy, performedBy, dateOfActivity);
    _type = type;
  }

  /**
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected TypedActivity()
  {}

  public void setType(AT type)
  {
    _type = type;
  }
}
