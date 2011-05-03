// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;

public class ReagentFinder extends AbstractBackingBean
{
  private static final Logger log = Logger.getLogger(ReagentFinder.class);

  private WellSearchResults _wellsBrowser;

  private String _reagentIdentifiersInput;

  /**
   * @motivation for CGLIB2
   */
  protected ReagentFinder()
  {}

  public ReagentFinder(WellSearchResults wellsBrowser)
  {
    _wellsBrowser = wellsBrowser;
  }

  public String getReagentIdentifiers()
  {
    return _reagentIdentifiersInput;
  }

  public void setReagentIdentifiers(String reagentIdentifiersInput)
  {
    _reagentIdentifiersInput = reagentIdentifiersInput;
  }

  @UICommand
  public String findReagents() throws IOException
  {
    _wellsBrowser.searchReagents(parseInput());
    return BROWSE_WELLS;
  }

  private Set<String> parseInput() throws IOException
  {
    SortedSet<String> parsedIdentifiers = Sets.newTreeSet();
    BufferedReader inputReader = new BufferedReader(new StringReader(_reagentIdentifiersInput));
    String identifier;
    while ((identifier = inputReader.readLine()) != null) {
      identifier = identifier.trim();
      if (identifier.length() > 0) {
        parsedIdentifiers.add(identifier);
      }
    }
    return parsedIdentifiers;
  }
}
