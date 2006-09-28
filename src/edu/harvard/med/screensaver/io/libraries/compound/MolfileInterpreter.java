// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import org.apache.log4j.Logger;

/**
 * An interpreter of the MDL molfile records embedded in the SD file records. Performs
 * various tricks, such as translating the molfile into a SMILES string, and normalizing,
 * canonicalizing, and splitting the SMILES into individual compounds.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class MolfileInterpreter
{
  
  // static members

  private static Logger log = Logger.getLogger(MolfileInterpreter.class);


  // instance data members

  private String _molfile;
  private String _smiles;
  
  
  // public constructors and methods
  
  public MolfileInterpreter(String molfile)
  {
    _molfile = molfile;
    initialize();
  }

  public String getMolfile()
  {
    return _molfile;
  }
  
  public String getSmiles()
  {
    return _smiles;
  }
  
  
  // private methods

  private void initialize()
  {
    // TODO: initialize the smiles from the molfile
    _smiles = "UNINITIALIZED";
  }
}

