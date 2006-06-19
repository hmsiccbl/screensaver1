// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

/**
 * Used to report a failed attempt to find an extant
 * {@link edu.harvard.med.screensaver.model.libraries.Library} or
 * {@link edu.harvard.med.screensaver.model.libraries.Well} or
 * {@link edu.harvard.med.screensaver.model.libraries.Compound}, indicating
 * that the Library was expected to have been loaded, but apparently was not.
 * 
 * @author ant
 */
public class ExtantLibraryException extends Exception
{
  /**
   * 
   */
  private static final long serialVersionUID = -1176739726988293758L;

  public ExtantLibraryException(String message)
  {
    super(message);
  }
}
