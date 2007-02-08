// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

/**
 * This class generates unique data header names for a ScreenResult. It does so
 * by appending increasing numeric suffixes to non-unique names.
 * 
 * @motivation Existing screen results can have non-unique data header names,
 *             but this creates ambiguity in the user interface.
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class UniqueDataHeaderNames
{
  private Map<ResultValueType,String> _resultValueType2UniqueName;
  private Map<String,ResultValueType> _uniqueName2ResultValueType;
//  private List<String> _orderedUniqueNames;

  public UniqueDataHeaderNames(ScreenResult screenResult)
  {
    _resultValueType2UniqueName = new LinkedHashMap<ResultValueType,String>();
    _uniqueName2ResultValueType = new HashMap<String,ResultValueType>();
    if (screenResult == null) {
      return;
    }
    List<String> names = new ArrayList<String>();
    for (ResultValueType rvt : screenResult.getResultValueTypes()) {
      names.add(rvt.getName());
    }
    List<String> names2 = new ArrayList<String>();
    for (ResultValueType rvt : screenResult.getResultValueTypes()) {
      String name = rvt.getName();
      if (Collections.frequency(names, name) > 1) {
        names2.add(name);
        name += " (" + Collections.frequency(names2, name) + ")";
      }
      _resultValueType2UniqueName.put(rvt, name);
      _uniqueName2ResultValueType.put(name, rvt);
    }
  }

  public String get(ResultValueType rvt)
  {
    return _resultValueType2UniqueName.get(rvt);
  }

  public ResultValueType get(String uniqueDataHeaderName)
  {
    return _uniqueName2ResultValueType.get(uniqueDataHeaderName);
  }
  
  public List<String> get(List<ResultValueType> rvts) {
    List<String> uniqueDataHeaderNames = new ArrayList<String>(rvts.size());
    for (ResultValueType rvt : rvts) {
      uniqueDataHeaderNames.add(_resultValueType2UniqueName.get(rvt));
    }
    return uniqueDataHeaderNames;
  }

  public List<String> asList()
  {
    return new ArrayList<String>(_resultValueType2UniqueName.values());
  }
  
  public String[] asArray()
  {
    return asList().toArray(new String[size()]);
  }

  public int size()
  {
    return _resultValueType2UniqueName.size();
  }
}
