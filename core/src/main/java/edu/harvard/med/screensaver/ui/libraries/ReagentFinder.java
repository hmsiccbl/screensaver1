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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import edu.harvard.med.lincs.screensaver.LincsScreensaverConstants;
import edu.harvard.med.lincs.screensaver.ui.libraries.LincsWellSearchResults;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.util.StringUtils;

public class ReagentFinder extends AbstractBackingBean
{
  private static final Logger log = Logger.getLogger(ReagentFinder.class);
  public static final Pattern FACILITY_SALT_BATCH_PATTERN = Pattern.compile("([^-]+)[-]([^-]+)[-]*([^-]*)");

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private WellSearchResults _wellsBrowser;
  private LincsWellSearchResults _reagentsBrowser;

  private String _reagentIdentifiersInput;
  private String _nameFacilityVendorIDInput;


  /**
   * @motivation for CGLIB2
   */
  protected ReagentFinder()
  {}

  public ReagentFinder(GenericEntityDAO dao,
                       LibrariesDAO librariesDao,
                       WellSearchResults wellsBrowser)
  {
    _wellsBrowser = wellsBrowser;
    _dao = dao;
    _librariesDao = librariesDao;
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
  public String findReagentsByVendorIdentifier() throws IOException
  {
    Set<String> reagentIdentifiers = parseReagentIdentifier();
    getCurrentScreensaverUser().logActivity("searching for wells by reagent vendor identifier(s): " +
                                            Joiner.on(", ").join(reagentIdentifiers));
    _wellsBrowser.searchReagentsByVendorIdentifier(reagentIdentifiers);
    return BROWSE_WELLS;
  }

  private Set<String> parseReagentIdentifier() throws IOException
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

  @UICommand
  public String browseReagents()
  {
    browseReagentWells();
    return BROWSE_REAGENTS;
  }

  @UICommand
  public String browseReagentWells()
  {
    _wellsBrowser.searchAllReagents();
    return BROWSE_WELLS;
  }

  public void setNameFacilityVendorIDInput(String compoundSearchName)
  {
    _nameFacilityVendorIDInput = compoundSearchName;
  }

  public String getNameFacilityVendorIDInput()
  {
    return _nameFacilityVendorIDInput;
  }
  
  /**
   * Find Reagents's by the union of compound name, facility id, and reagent ID
   */
  @UICommand
  public String findWellsByNameFacilityVendorID()
  {
    Set<String> nameFacilityVendorIDInputList = parseNameFacilityVendorIDList();
    getCurrentScreensaverUser().logActivity("searching for wells by compound name, facility ID, vendor ID: " +
      Joiner.on(", ").join(nameFacilityVendorIDInputList));

    Set<WellKey> wellKeys = _librariesDao.findWellKeysForCompoundNameList(nameFacilityVendorIDInputList);
    wellKeys.addAll(_librariesDao.findWellKeysForReagentVendorIDList(nameFacilityVendorIDInputList));
    
    String titleSuffix = StringUtils.isEmpty(_nameFacilityVendorIDInput) ?
      " Browser" :
      (" with compound name, facility ID, or vendor ID matching '" + _nameFacilityVendorIDInput + "'");
    _wellsBrowser.searchWells(wellKeys, "Wells" + titleSuffix);

    if (_wellsBrowser.getRowCount() == 1) {
      _wellsBrowser.getRowsPerPageSelector().setSelection(1);
    }
    if (!!!LincsScreensaverConstants.FACILITY_NAME.equals(getApplicationProperties().getFacility())) {
      _wellsBrowser.getColumnManager().setVisibilityOfColumnsInGroup("Compound Reagent Columns", true);
    }
    resetSearchFields();
    return BROWSE_WELLS;
  }

  private Set<String> parseNameFacilityVendorIDList()
  {
    if (StringUtils.isEmpty(_nameFacilityVendorIDInput)) {
      return ImmutableSet.of("");
    }
    
    Set<String> values = Sets.newHashSet();
    for (String value : StringUtils.tokenizeQuotedWordList(_nameFacilityVendorIDInput)) {
      value = value.replaceAll("\"|'+", ""); // remove quote characters left in by the tokenizer
      value = value.trim();
      if (StringUtils.isEmpty(value)) {
        continue;
      }
      // remove zero padding if salt - batch id were entered
      Matcher matcher = FACILITY_SALT_BATCH_PATTERN.matcher(value);
      if(matcher.matches())
      {
        String temp = matcher.group(1);
        try {
          Integer id = Integer.parseInt(matcher.group(2));
          temp += "-" + id;
          // the second id is optional
          if(! StringUtils.isEmpty(matcher.group(3))) {
            id = Integer.parseInt(matcher.group(3));
            temp += "-" + id;
          }
          log.info("search string: " + value + ", formatted: " + temp);
          value = temp;
        }
        catch (NumberFormatException e) {
          log.info("input string parse error for the facility-salt-batch pattern: " + FACILITY_SALT_BATCH_PATTERN + ", \"" + value + "\"", e);
        }
      }
      values.add(value);
    }
    return values;
  }

  private void resetSearchFields()
  {
    _nameFacilityVendorIDInput = null;
    _reagentIdentifiersInput = null;
  }
}
