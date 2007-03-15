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

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

public class PlateWellListParserResult 
{
  // static members

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(PlateWellListParserResult.class);

  
  // instance data members
  
  private List<String> _fatalErrors = new ArrayList<String>();
  private List<Pair<Integer,String>> _syntaxErrors = new ArrayList<Pair<Integer,String>>();
  private SortedSet<WellKey> _notFound = new TreeSet<WellKey>();
  private SortedSet<Well> _found = new TreeSet<Well>();

  
  // public constructors and methods
  
  public PlateWellListParserResult()
  {
  }
  
  public void addWellNotFound(WellKey wellKey) 
  {
    _notFound.add(wellKey);
  }
  
  public void addWell(Well well)
  {
    _found.add(well);
  }
  
  public void addFatalError(String error)
  {
    _fatalErrors.add(error);
  }
  
  public void addSyntaxError(int line, String error)
  {
    _syntaxErrors.add(new Pair<Integer,String>(line, error));
  }
  
  public List<String> getFatalErrors()
  {
    return _fatalErrors;
  }

  public List<Pair<Integer,String>> getSyntaxErrors()
  {
    return _syntaxErrors;
  }
  
  public SortedSet<WellKey> getWellsNotFound()
  {
    return _notFound;
  }    
   
  public SortedSet<Well> getWells()
  {
    return _found;
  }    
}

