// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.WellKey;

/**
 * An unmodifiable map from {@link WellKey} to {@link ResultValue} that wraps a map from
 * <code>String</code> to <code>ResultValue</code>.
 * 
 * To modify the collection of <code>ResultValues</code>, call
 * {@link ResultValueType#addResultValue} or {@link ResultValueType#removeResultValue}.
 *
 * @motivation This class provides some added type-safety for the map used by hibernate to
 * implement the collection of <code>ResultValues</code>. With hibernate annotations, we were
 * unable to define this mapping to use <code>WellKey</code> as the key, but we wanted to retain
 * type-safety in user code. (We also did not want to have to rework all the previously
 * developed user code that used the <code>WellKey</code>-keyed maps that we had when we were
 * still using XDoclet!)

 * @author s&amp;@
 */
public class WellKeyToResultValueMap implements Map<WellKey,ResultValue>
{
  private static Logger log = Logger.getLogger(WellKeyToResultValueMap.class);

  private Map<String,ResultValue> _wellIdToResultValueMap;
  
  public WellKeyToResultValueMap(Map<String,ResultValue> wellIdToResultValueMap)
  {
    _wellIdToResultValueMap = wellIdToResultValueMap;
  }
  
  public void clear()
  {
    throw new UnsupportedOperationException();
  }

  public boolean containsKey(Object key)
  {
    WellKey wellKey = (WellKey) key;
    return _wellIdToResultValueMap.containsKey(wellKey.getKey());
  }

  public boolean containsValue(Object value)
  {
    return _wellIdToResultValueMap.containsValue(value);
  }

  class WellKeyToResultValueMapEntry implements Map.Entry<WellKey,ResultValue>
  {
    private String _wellId;
    private ResultValue _resultValue;
    public WellKeyToResultValueMapEntry(String wellId, ResultValue resultValue)
    {
      _wellId = wellId;
      _resultValue = resultValue;
    }
    public WellKey getKey()
    {
      return new WellKey(_wellId);
    }
    public ResultValue getValue()
    {
      return _resultValue;
    }
    public ResultValue setValue(ResultValue value)
    {
      throw new UnsupportedOperationException();
    }
  }

  public Set<Map.Entry<WellKey,ResultValue>> entrySet()
  {
    Set<Map.Entry<String,ResultValue>> stringMapEntries = _wellIdToResultValueMap.entrySet();
    Set<Map.Entry<WellKey,ResultValue>> wellKeyMapEntries =
      new HashSet<Map.Entry<WellKey,ResultValue>>();
    for (Map.Entry<String,ResultValue> stringMapEntry : stringMapEntries) {
      Map.Entry<WellKey,ResultValue> wellKeyMapEntry =
        new WellKeyToResultValueMapEntry(stringMapEntry.getKey(), stringMapEntry.getValue());
      wellKeyMapEntries.add(wellKeyMapEntry);
    }
    return wellKeyMapEntries;
  }

  public ResultValue get(Object key)
  {
    WellKey wellKey = (WellKey) key;
    return _wellIdToResultValueMap.get(wellKey.getKey());
  }

  public boolean isEmpty()
  {
    return _wellIdToResultValueMap.isEmpty();
  }

  public Set<WellKey> keySet()
  {
    Set<String> wellIds = _wellIdToResultValueMap.keySet();
    Set<WellKey> wellKeys = new HashSet<WellKey>(wellIds.size());
    for (String wellId : wellIds) {
      wellKeys.add(new WellKey(wellId));
    }
    return wellKeys;
  }

  public ResultValue put(WellKey key, ResultValue value)
  {
    throw new UnsupportedOperationException();
  }

  public void putAll(Map<? extends WellKey,? extends ResultValue> arg0)
  {
    throw new UnsupportedOperationException();
  }

  public ResultValue remove(Object key)
  {
    throw new UnsupportedOperationException();
  }

  public int size()
  {
    return _wellIdToResultValueMap.size();
  }

  public Collection<ResultValue> values()
  {
    return _wellIdToResultValueMap.values();
  }
}
