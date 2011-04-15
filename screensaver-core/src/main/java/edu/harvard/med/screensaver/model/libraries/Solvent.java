// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 

// Solventsaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;
import edu.harvard.med.screensaver.model.screens.ScreenType;

public enum Solvent implements VocabularyTerm
{
  DMSO("DMSO", ImmutableSet.of(ScreenType.SMALL_MOLECULE)),
  RNAI_BUFFER("RNAi buffer", ImmutableSet.of(ScreenType.RNAI)),
  OTHER_AQUEOUS("Other (aqueous)", ImmutableSet.of(ScreenType.values()));
  
  public static Map<ScreenType,Solvent> defaultSolventType = ImmutableMap.of(ScreenType.SMALL_MOLECULE, DMSO,
                                                                             ScreenType.RNAI, RNAI_BUFFER);

  /**
   * A Hibernate <code>UserType</code> to map the {@link Solvent} vocabulary.
   */
  public static class UserType extends VocabularyUserType<Solvent>
  {
    public UserType()
    {
      super(Solvent.values());
    }
  }

  private String _value;
  private Set<ScreenType> _validForScreenTypes;

  private Solvent(String value, Set<ScreenType> validForScreenTypes)
  {
    _value = value;
    _validForScreenTypes = validForScreenTypes;
  }

  /**
   * Get the value of the vocabulary term.
   * 
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

  public Set<ScreenType> getValidForScreenTypes()
  {
    return _validForScreenTypes;
  }

  public boolean isValidForScreenType(ScreenType screenType)
  {
    return _validForScreenTypes.contains(screenType);
  }

  public static Solvent getDefaultSolventType(ScreenType screenType)
  {
    return defaultSolventType.get(screenType);
  }
}
