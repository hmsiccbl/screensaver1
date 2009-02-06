// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.namevaluetable;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.io.libraries.compound.StructureImageProvider;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.ui.libraries.CompoundViewer;
import edu.harvard.med.screensaver.ui.libraries.ReagentViewer;

import org.apache.log4j.Logger;

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
  private static final String PUBCHEM_CID_LOOKUP_URL_PREFIX =
    "http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=";
  private static final String CHEMBANK_ID_LOOKUP_URL_PREFIX =
    "http://chembank.broad.harvard.edu/chemistry/viewMolecule.htm?cbid=";

  // the row names
  private static final String STRUCTURE = "Structure";
  private static final String SMILES = "Smiles";
  private static final String INCHI = "InChI";
  private static final String COMPOUND_NAMES = "Compound Names";
  private static final String MOLECULAR_MASS = "Molecular Mass";
  private static final String MOLECULAR_FORMULA = "Molecular Formula";
  private static final String PUBCHEM_CIDS = "PubChem CIDs";
  private static final String CHEMBANK_IDS = "ChemBank IDs";
  private static final String IS_SALT = "Is Salt";
  private static final String NSC_NUMBERS = "NSC Numbers";
  private static final String CAS_NUMBERS = "CAS Numbers";


  // private instance fields

  private CompoundViewer _compoundViewer;
  private Compound _compound;
  private StructureImageProvider _imageProvider;
  private ReagentViewer _parentViewer;
  private List<String> _names = new ArrayList<String>();
  private List<Object> _values = new ArrayList<Object>();
  private List<ValueType> _valueTypes = new ArrayList<ValueType>();
  private List<String> _descriptions = new ArrayList<String>();


  // public constructor and implementations of NameValueTable abstract methods

  public CompoundNameValueTable(Compound compound,
                                CompoundViewer compoundViewer,
                                StructureImageProvider imageProvider)
  {
    this(compound, compoundViewer, imageProvider, null);
  }

  public CompoundNameValueTable(
    Compound compound,
    CompoundViewer compoundViewer,
    StructureImageProvider imageProvider,
    ReagentViewer parentViewer)
  {
    _compoundViewer = compoundViewer;
    _compound = compound;
    _imageProvider = imageProvider;
    _parentViewer = parentViewer;
    initializeLists(compound);
    setDataModel(new ListDataModel(_values));
  }


  @Override
  public int getNumRows()
  {
    return _names.size();
  }

  @Override
  public String getDescription(int index)
  {
    return _descriptions.get(index);
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
    // only link to the compound viewer page from the smiles when embedded in the well viewer
    if (isEmbedded()) {
      String name = getName(index);
      if (name.equals(SMILES)) {
        return _compoundViewer.viewCompound(_compound);
      }
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
    if (name.equals(CHEMBANK_IDS)) {
      return CHEMBANK_ID_LOOKUP_URL_PREFIX + value;
    }
    // other fields do not have links
    return null;
  }


  // private instance methods

  private boolean isEmbedded()
  {
    return _parentViewer != null;
  }

  /**
   * Initialize the lists {@link #_names}, {@link #_values}, and {@link #_valueTypes}. Don't
   * add rows for missing values.
   * @param compound
   */
  private void initializeLists(Compound compound) {
    if (compound.getPubchemCids().size() > 0) {
      addItem(STRUCTURE, _imageProvider.getImageUrl(compound), ValueType.IMAGE, "A 2D structure image of the compound");
    }
    addItem(SMILES, compound.getSmiles(), isEmbedded() ? ValueType.COMMAND : ValueType.TEXT, "The SMILES string for the compound");
    addItem(INCHI, compound.getInchi(), ValueType.TEXT, "The InChI string for the compound");
    if (compound.getNumCompoundNames() > 0) {
      addItem(COMPOUND_NAMES, compound.getCompoundNames(),  ValueType.TEXT_LIST, "The various names the compound goes by");
    }
    addItem(MOLECULAR_MASS, compound.getMolecularMass(), ValueType.TEXT, "The molecular mass for the compound");
    addItem(MOLECULAR_FORMULA, compound.getMolecularFormula(), ValueType.UNESCAPED_TEXT, "The molecular formula for the compound");
    if (compound.getNumPubchemCids() > 0) {
      addItem(PUBCHEM_CIDS, compound.getPubchemCids(), ValueType.LINK_LIST, "The PubChem Compound Identifiers");
    }
    if (compound.getNumChembankIds() > 0) {
      addItem(CHEMBANK_IDS, compound.getChembankIds(), ValueType.LINK_LIST, "The ChemBank IDs for the compound");
    }
    addItem(IS_SALT, compound.isSalt(), ValueType.TEXT, "Typically, this indicates a non-bioactive solvent in the well");
    if (compound.getNumNscNumbers() > 0) {
      addItem(NSC_NUMBERS, compound.getNscNumbers(), ValueType.TEXT_LIST, "NSC numbers for the compound");
    }
    if (compound.getNumCasNumbers() > 0) {
      addItem(CAS_NUMBERS, compound.getCasNumbers(), ValueType.TEXT_LIST, "CAS numbers for the compound");
    }
  }

  private void addItem(String name, Object value, ValueType valueType, String description)
  {
    _names.add(name);
    _values.add(value);
    _valueTypes.add(valueType);
    _descriptions.add(description);
  }
}

