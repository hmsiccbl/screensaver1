// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
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
  private int _maxQueryInputItems;

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
    SortedSet<String> reagentIdentifiers = parseReagentIdentifier();
    SortedSet<String> keysToShow = reagentIdentifiers;
    if(reagentIdentifiers.size() > getMaxQueryInputItems()) {
      showMessage("maxQueryReached", reagentIdentifiers.size(), getMaxQueryInputItems());
      int i=0;
      for(String key:reagentIdentifiers) {
        if(++i > getMaxQueryInputItems()) {
          keysToShow = reagentIdentifiers.headSet(key);
          break;
        }
      }
    }

    getCurrentScreensaverUser().logActivity("searching for wells by reagent vendor identifier(s): " +
                                            Joiner.on(", ").join(keysToShow));
    _wellsBrowser.searchReagentsByVendorIdentifier(keysToShow);
    return BROWSE_WELLS;
  }

  private SortedSet<String> parseReagentIdentifier() throws IOException
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

    SortedSet<WellKey> wellKeys = findWellKeysForCompoundNameList(nameFacilityVendorIDInputList, getMaxQueryInputItems() +1);  // send 1 extra to determine if limit reached
    wellKeys.addAll(findWellKeysForReagentVendorIDList(nameFacilityVendorIDInputList, getMaxQueryInputItems() +1)); // send 1 extra to determine if limit reached
   
    SortedSet<WellKey> keysToShow = wellKeys;
    if(wellKeys.size() > getMaxQueryInputItems()) {
      showMessage("maxQueryReached", (wellKeys.size()==(getMaxQueryInputItems()+1)*2) ? ("> "+getMaxQueryInputItems()) : wellKeys.size(), getMaxQueryInputItems());
      int i=0;
      for(WellKey key:wellKeys) {
        if(++i > getMaxQueryInputItems()) {
          keysToShow = wellKeys.headSet(key);
          break;
        }
      }
    }    
    log.info("keysToShow: " + keysToShow.size());
    String titleSuffix = StringUtils.isEmpty(_nameFacilityVendorIDInput) ?
      " Browser" :
      (" with compound name, facility ID, or vendor ID matching '" + _nameFacilityVendorIDInput + "'");
    _wellsBrowser.searchWells(keysToShow, "Wells" + titleSuffix);

    if (_wellsBrowser.getRowCount() == 1) {
      _wellsBrowser.getRowsPerPageSelector().setSelection(1);
    }
    if (!!!getApplicationProperties().isFacility(LincsScreensaverConstants.FACILITY_KEY)) {
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

  private SortedSet<WellKey> findWellKeysForCompoundNameList(final Collection<String> nameList, int maxQueryInputItems)
  {
    SortedSet<WellKey> keys = Sets.newTreeSet();
    for(String name:nameList)
    {
      keys.addAll(_librariesDao.findWellKeysForCompoundName(name, maxQueryInputItems));
    }
    return keys;
  }
  
	private SortedSet<WellKey> findWellKeysForReagentVendorIDList(final Collection<String> facilityVendorIdInputList,
			int maxQueryInputItems) {
		SortedSet<WellKey> keys = Sets.newTreeSet();
		for (String nameFacilityVendorIDInput : facilityVendorIdInputList) {
			keys.addAll(_librariesDao.findWellKeysForReagentVendorID(nameFacilityVendorIDInput, maxQueryInputItems));
		}
		return keys;
	}

  private void resetSearchFields()
  {
    _nameFacilityVendorIDInput = null;
    _reagentIdentifiersInput = null;
  }

  public void setMaxQueryInputItems(int _maxQueryInputItems)
  {
    this._maxQueryInputItems = _maxQueryInputItems;
  }

  public int getMaxQueryInputItems()
  {
    return _maxQueryInputItems;
  }
}
