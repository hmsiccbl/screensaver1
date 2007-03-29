// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.namevaluetable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.ListDataModel;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.ui.control.LibrariesController;

/**
 * A NameValueTable for the Compound Viewer.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class CompoundNameValueTable extends NameValueTable
{
  
  // private static final fields
  
  private static final Logger log = Logger.getLogger(CompoundNameValueTable.class);
  private static final String SCREENSAVER0_IMAGE_RENDERER_URL_PREFIX =
    "http://screensaver1.med.harvard.edu/render_molecule.png?smiles=";
  private static final String PUBCHEM_CID_LOOKUP_URL_PREFIX =
    "http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=";
  
  // the row names
  private static final String STRUCTURE = "Structure";
  private static final String SMILES = "Smiles";
  private static final String INCHI = "InChI";
  private static final String COMPOUND_NAMES = "Compound&nbsp;Names";
  private static final String MOLECULAR_MASS = "Molecular&nbsp;Mass";
  private static final String MOLECULAR_FORMULA = "Molecular&nbsp;Formula";
  private static final String PUBCHEM_CIDS = "PubChem&nbsp;CIDs";
  private static final String CHEMBANK_IDS = "ChemBank&nbsp;ID";
  private static final String IS_SALT = "Is&nbsp;Salt";
  private static final String NSC_NUMBERS = "NSC&nbsp;Numbers";
  private static final String CAS_NUMBERS = "CAS&nbsp;Numbers";

  
  // private instance fields
  
  private LibrariesController _librariesController;
  private Compound _compound;
  private List<String> _names = new ArrayList<String>();
  private List<Object> _values = new ArrayList<Object>();
  private List<ValueType> _valueTypes = new ArrayList<ValueType>();
  
  
  // public constructor and implementations of NameValueTable abstract methods
  
  public CompoundNameValueTable(LibrariesController librariesController, Compound compound)
  {
    _librariesController = librariesController;
    _compound = compound;
    initializeLists(compound);
    setDataModel(new ListDataModel(_values));
  }

  @Override
  public int getNumRows()
  {
    return _names.size();
  }

  @Override
  public String getName(int index)
  {
    return _names.get(index);
  }

  @Override
  public ValueType getValueType(int index)
  {
    return _valueTypes.get(index);
  }

  @Override
  public Object getValue(int index)
  {
    return _values.get(index);
  }

  @Override
  public String getAction(int index, String value)
  {
    String name = getName(index);
    if (name.equals(SMILES)) {
      return _librariesController.viewCompound(_compound, null);
    }
    // other fields do not have actions
    return null;
  }
  
  @Override
  public String getLink(int index, String value)
  {
    String name = getName(index);
    if (name.equals(PUBCHEM_CIDS)) {
      return PUBCHEM_CID_LOOKUP_URL_PREFIX + value;
    }
    if (name.equals(STRUCTURE)) {
      try {
        value = URLEncoder.encode(value, "UTF-8");
      }
      catch (UnsupportedEncodingException ex){
        throw new RuntimeException("UTF-8 not supported", ex);
      }
      return SCREENSAVER0_IMAGE_RENDERER_URL_PREFIX + value;
    }
    // other fields do not have links
    return null;
  }
  
  
  // private instance methods

  /**
   * Initialize the lists {@link #_names}, {@link #_values}, and {@link #_valueTypes}. Don't
   * add rows for missing values.
   * @param compound
   */
  private void initializeLists(Compound compound) {
    addItem(STRUCTURE, compound.getSmiles(), ValueType.IMAGE);
    addItem(SMILES, compound.getSmiles(), ValueType.COMMAND);
    addItem(INCHI, compound.getInchi(), ValueType.TEXT);
    if (compound.getNumCompoundNames() > 0) {
      addItem(COMPOUND_NAMES, compound.getCompoundNames(),  ValueType.TEXT_LIST);
    }
    addItem(MOLECULAR_MASS, compound.getMolecularMass(), ValueType.TEXT);
    addItem(MOLECULAR_FORMULA, compound.getMolecularFormula(), ValueType.UNESCAPED_TEXT);
    if (compound.getNumPubchemCids() > 0) {
      addItem(PUBCHEM_CIDS, compound.getPubchemCids(), ValueType.LINK_LIST);
    }
    if (compound.getChembankId() != null) {
      addItem(CHEMBANK_IDS, compound.getChembankId(), ValueType.TEXT);
    }
    addItem(IS_SALT, compound.isSalt(), ValueType.TEXT);
    if (compound.getNumNscNumbers() > 0) {
      addItem(NSC_NUMBERS, compound.getNscNumbers(), ValueType.TEXT_LIST);
    }
    if (compound.getNumCasNumbers() > 0) {
      addItem(CAS_NUMBERS, compound.getCasNumbers(), ValueType.TEXT_LIST);
    }
  }

  private void addItem(String name, Object value, ValueType valueType)
  {
    _names.add(name);
    _values.add(value);
    _valueTypes.add(valueType);
  }
}

