// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

public enum LabHeadAppointmentDepartment implements VocabularyTerm
{

  // the vocabulary
  
  BCMP("BCMP"),
  BIOMEDICAL_INFORMATICS("Biomedical Informatics"),
  CELL_BIO("Cell Bio"),
  GENETICS("Genetics"),
  MICRO_AND_IMMUNO("Micro and Immuno"),
  NEURO("Neuro"),
  SYS_BIO("Sys Bio")
  ;

 
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link LabHeadAppointmentDepartment} vocabulary.
   */
  public static class UserType extends VocabularyUserType<LabHeadAppointmentDepartment>
  {
    public UserType()
    {
      super(LabHeadAppointmentDepartment.values());
    }
  }


  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>LabHeadAppointmentDepartment</code> vocabulary term.
   * @param value The value of the term.
   */
  private LabHeadAppointmentDepartment(String value)
  {
    _value = value;
  }


  // public instance methods

  /**
   * Get the value of the vocabulary term.
   * @return the value of the vocabulary term
   */
  public String getValue()
  {
    return _value;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return getValue();
  }
}
