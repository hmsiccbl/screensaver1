// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/model/screens/ScreenType.java $
// $Id: ScreenType.java 1723 2007-08-20 20:26:50Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

/**
 * The attached file type vocabulary.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum AttachedFileType implements VocabularyTerm
{
  
  // the vocabulary
  APPLICATION("Application", Screen.class),
  PRIMARY_SCREEN_REPORT("Primary Screen Report", Screen.class), // a document containing both original and publishable protocol, from which the publishable protocol field will ultimately be popuplated (manually)
  LETTER_OF_SUPPORT("Letter of Support", Screen.class),
  SCREENER_CORRESPONDENCE("Screener Correspondence", Screen.class),
  MIARE_DOCUMENT("MIARE Document", Screen.class),
  PUBLICATION("Publication", Screen.class),
  
  SMALL_MOLECULE_USER_AGREEMENT("ICCB-L/NSRB Small Molecule User Agreement", ScreeningRoomUser.class),
  RNAI_USER_AGREEMENT("ICCB-L/NSRB RNAi User Agreement", ScreeningRoomUser.class);
  
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link AttachedFileType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<AttachedFileType>
  {
    public UserType()
    {
      super(AttachedFileType.values());
    }
  }


  // private instance field and constructor

  private String _value;
  private Class<? extends AbstractEntity> _forUseWithType;

  /**
   * Constructs an <code>AttachedFileType</code> vocabulary term.
   * @param value The value of the term.
   */
  private AttachedFileType(String value, Class<? extends AbstractEntity> forUseWithType)
  {
    _value = value;
    _forUseWithType = forUseWithType;
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

  @Override
  public String toString()
  {
    return getValue();
  }
}
