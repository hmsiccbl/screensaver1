// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import joelib2.io.BasicIOType;
import joelib2.io.BasicIOTypeHolder;
import joelib2.io.BasicReader;
import joelib2.io.IOTypeHolder;
import joelib2.io.MoleculeIOException;
import joelib2.molecule.BasicConformerMolecule;

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
    try {
      IOTypeHolder typeHolder = BasicIOTypeHolder.instance();
      BasicIOType sdfType = typeHolder.getIOType("SDF");
      BasicIOType smilesType = typeHolder.getIOType("SMILES");
      
      ByteArrayInputStream inputStream = new ByteArrayInputStream(_molfile.getBytes());
      BasicReader reader = new BasicReader(inputStream, sdfType);
      
      BasicConformerMolecule molecule = new BasicConformerMolecule(sdfType, smilesType);
      reader.readNext(molecule);
      
      String moleculeAsString = molecule.toString();
      Pattern pattern = Pattern.compile("\\S+");
      Matcher matcher = pattern.matcher(moleculeAsString);
      matcher.find();
      _smiles = matcher.group();
    }
    catch (IOException e) {
      log.error("highly unexpected IOException initializing MolfileInterpreter!", e);
    }
    catch (MoleculeIOException e) {
      log.error("unexpected MoleculeIOException initializing MolfileInterpreter!", e);
    }
  }
}

