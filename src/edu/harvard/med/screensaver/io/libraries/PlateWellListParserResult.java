// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

public class PlateWellListParserResult 
{
  // static members

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(PlateWellListParserResult.class);

  
  // instance data members
  
  private List<Pair<Integer,String>> _errors = new ArrayList<Pair<Integer,String>>();
  private SortedSet<WellKey> _parsedWellKeys = new TreeSet<WellKey>();

  
  // public constructors and methods
  
  public PlateWellListParserResult()
  {
  }
  
  public void addParsedWellKey(WellKey wellKey)
  {
    _parsedWellKeys.add(wellKey);
  }
  
  public void addError(int line, String error)
  {
    _errors.add(new Pair<Integer,String>(line, error));
  }
  
  /**
   * Return true if either syntax or fatal errors were found. Specified wells
   * that do not exist in the database are not considered errors (see
   * {@link #getWellsNotFound()}).
   * 
   * @return true if errors were found while parsing and/or looking up wells in
   *         the database.
   */
  public boolean hasErrors()
  {
    return _errors.size() > 0; 
  }    

  public List<Pair<Integer,String>> getErrors()
  {
    return _errors;
  }
  
  /**
   * Get the list of well keys parsed from the input (whether or not they exist
   * in the database).
   */
  public SortedSet<WellKey> getParsedWellKeys()
  {
    return _parsedWellKeys; 
  }
}

