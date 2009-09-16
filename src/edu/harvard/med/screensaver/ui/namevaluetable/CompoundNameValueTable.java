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

import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;

import org.apache.log4j.Logger;

/**
 * A NameValueTable for the Compound Viewer.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class CompoundNameValueTable extends ReagentNameValueTable<SmallMoleculeReagent>
{
  private static final String MOLECULAR_INFO_DISABLED_MESSAGE = "<font color=\"gray\">(Temporarily unavailable. Use PubChem link, below.)</font>";
  private static final Logger log = Logger.getLogger(CompoundNameValueTable.class);
  private static final String PUBCHEM_CID_LOOKUP_URL_PREFIX =
    "http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=";
  private static final String CHEMBANK_ID_LOOKUP_URL_PREFIX =
    "http://chembank.broad.harvard.edu/chemistry/viewMolecule.htm?cbid=";

  private static final String STRUCTURE = "Structure";
  private static final String SMILES = "Smiles";
  private static final String INCHI = "InChI";
  private static final String COMPOUND_NAMES = "Compound Names";
  private static final String MOLECULAR_MASS = "Molecular Mass";
  private static final String MOLECULAR_WEIGHT = "Molecular Weight";
  private static final String MOLECULAR_FORMULA = "Molecular Formula";
  private static final String PUBCHEM_CIDS = "PubChem CIDs";
  private static final String CHEMBANK_IDS = "ChemBank IDs";

  private StructureImageProvider _imageProvider;


  public CompoundNameValueTable(SmallMoleculeReagent compound,
                                StructureImageProvider imageProvider)
  {
    super(compound);
    _imageProvider = imageProvider;
    initializeLists(compound);
    setDataModel(new ListDataModel(_values));
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
    return super.getLink(index, value);
  }

  protected void initializeLists(SmallMoleculeReagent compound) {
    super.initializeLists(compound);
    addItem(STRUCTURE, _imageProvider.getImageUrl(compound), ValueType.IMAGE, "A 2D structure image of the compound");
    addItem(SMILES, compound.getSmiles(), ValueType.TEXT, "The SMILES string for the compound");
    addItem(INCHI, compound.getInchi(), ValueType.TEXT, "The InChI string for the compound");
    addItem(COMPOUND_NAMES, compound.getCompoundNames(),  ValueType.TEXT_LIST, "The various names the compound goes by");
    addItem(MOLECULAR_MASS, compound.getMolecularMass(), ValueType.TEXT, "The molecular mass for the compound");
    addItem(MOLECULAR_WEIGHT, compound.getMolecularWeight(), ValueType.TEXT, "The molecular weight for the compound");
    addItem(MOLECULAR_FORMULA, compound.getMolecularFormula() == null ? "" : compound.getMolecularFormula().toHtml(), ValueType.UNESCAPED_TEXT, "The molecular formula for the compound");
    addItem(PUBCHEM_CIDS, compound.getPubchemCids(), ValueType.LINK_LIST, "The PubChem Compound Identifiers");
    addItem(CHEMBANK_IDS, compound.getChembankIds(), ValueType.LINK_LIST, "The ChemBank IDs for the compound");
  }
}

