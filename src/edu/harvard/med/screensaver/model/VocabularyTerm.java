// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

/**
 * An interface for the terms in the controlled vocabularies of the model. The
 * vocabularies are implemented as enums.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public interface VocabularyTerm
{
  /**
   * Get the value of the vocabulary term.
   * @return the value of the vocabulary term
   */
  public String getValue();
}
