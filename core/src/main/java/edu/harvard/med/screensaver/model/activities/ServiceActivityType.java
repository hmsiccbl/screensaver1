// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
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
//  TRAINING_CELLWORX("Training - CellWoRx"),
  TRAINING_ACUMEN("Training - Acumen"),
  TRAINING_COMBI_NL("Training - Combi nL"),
  TRAINING_OCTET("Training - Octet"),
  TRAINING_HP_D300("Training - HP D300"),
  TRAINING_LUMINA_II("Training - Lumina II"),
  TRAINING_IXM("Training - IXM"),
  TRAINING_VELOS("Training - Velos"),
  TRAINING_QPCR_ABI("Training - QPCR-ABI"),
  TRAINING_QPCR_ROCHE("Training - QPCR-Roche"),
  TRAINING_WELLMATE("Training - Wellmate"),
  TRAINING_ENVISION("Training - Envision"),
  TRAINING_BIOMEKFX("Training - BiomekFX"),
  TRAINING_CYBIO("Training - CyBio"),
  TRAINING_FLEXSTATION_III("Training - Flexstation III"),
  TRAINING_M5("Training - M5"),
  TRAINING_OTHER("Training - Other"),
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
