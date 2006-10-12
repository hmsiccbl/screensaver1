// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.smiles.SmilesGenerator;

/**
 * An interpreter of the MDL molfile records embedded in the SD file records. Performs
 * various tricks, such as translating the molfile into a canonicalized SMILES string, and
 * splitting the SMILES into individual compounds. Some things that it doesn't do yet, but
 * that I would like to see it do in the future, are:
 * 
 * <ul>
 * <li>normalization
 * <li>charge balancing
 * </ul>
 * 
 * For some notes on doing chemistry stuff in Screensaver, see <a href =
 * "https://wiki.med.harvard.edu/ICCBL/ChemistryDiscussion">the chemistry discussion on the
 * Wiki</a>.
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
  private String _primaryCompoundSmiles;
  private List<String> _secondaryCompoundsSmiles = new ArrayList<String>(); 
  
  
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

  public String getPrimaryCompoundSmiles()
  {
    return _primaryCompoundSmiles;
  }

  public List<String> getSecondaryCompoundsSmiles()
  {
    return _secondaryCompoundsSmiles;
  }

  
  // private instance methods
  
  private void initialize()
  {
    Molecule molecule = getMoleculeFromMolfile();
    _smiles = getSmilesForMolecule(molecule);
    
    String [] componentsSmiles = _smiles.split("\\.");
    Arrays.sort(componentsSmiles, new Comparator<String>() {
      public int compare(String s1, String s2) {
        return s2.length() - s1.length();
      }
    });
    
    _primaryCompoundSmiles = componentsSmiles[0];
    _secondaryCompoundsSmiles.addAll(Arrays.asList(componentsSmiles));
    _secondaryCompoundsSmiles.remove(0);
  }

  /**
   * Get the molecule from the molfile. Return it.
   * @return
   */
  private Molecule getMoleculeFromMolfile()
  {
    try {
      MDLReader mdlReader = new MDLReader(new StringReader(_molfile));
      Molecule molecule = new Molecule();
      mdlReader.read(molecule);
      return molecule;
    }
    catch (Exception e) {
      log.error("encountered Exception reading the MDL!", e);
    }
    return null;
  }

  /**
   * Get the SMILES string for the given molecule.
   *  
   * @param molecule the molecule to get the SMILES string for
   * @return the SMILES string
   */
  private String getSmilesForMolecule(Molecule molecule) {
    SmilesGenerator smilesGenerator =
      new SmilesGenerator(DefaultChemObjectBuilder.getInstance());
    return smilesGenerator.createSMILES(molecule);
  }
}
