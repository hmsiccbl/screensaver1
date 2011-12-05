// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/atolopko/2189/core/src/main/java/edu/harvard/med/screensaver/model/ServiceActivityType.java $
// $Id: ServiceActivityType.java 6005 2011-06-15 20:53:08Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.activities;

import com.google.common.base.Predicate;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

public enum ServiceActivityType implements VocabularyTerm
{
  MEDCHEM_ANALYTICAL("MedChem - Analytical"),
  MEDCHEM_CONSULT("MedChem - Consult"),
  MEDCHEM_SYNTHESIS("MedChem - Synthesis"),
  INFORMATICS("Informatics"),
  AUTOMATION("Automation"),
  ASSAY_DEV_CONSULT("Assay Dev Consult"),
  IMAGE_ANALYSIS("Image Analysis"),
  OTHER("Other");

  public Predicate<ServiceActivity> isValuePredicate()
  {
    return new Predicate<ServiceActivity>() { 
      @Override
      public boolean apply(ServiceActivity activity)
      {
        return activity.getType() == ServiceActivityType.this;
      }
    };
  }

  /**
   * A Hibernate <code>UserType</code> to map the {@link ServiceActivityType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<ServiceActivityType>
  {
    public UserType()
    {
      super(ServiceActivityType.values());
    }
  }

  private String _value;

  private ServiceActivityType(String value)
  {
    _value = value;
  }

  public String getValue()
  {
    return _value;
  }

  @Override
  public String toString()
  {
    return getValue();
  }
}
