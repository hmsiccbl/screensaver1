// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.namevaluetable;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.model.libraries.Gene;

import org.apache.log4j.Logger;

/**
 * A NameValueTable for the Gene Viewer.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class GeneNameValueTable extends NameValueTable
{

  // private static final fields

  private static final Logger log = Logger.getLogger(GeneNameValueTable.class);
  private static final String GENBANK_ACCESSION_NUMBER_LOOKUP_URL_PREFIX =
    "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?val=";
  private static final String ENTREZGENE_ID_LOOKUP_URL_PREFIX =
    "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=Retrieve&dopt=full_report&list_uids=";

  // the row names
  private static final String GENE_NAME = "Gene Name";
  private static final String ENTREZGENE_ID = "EntrezGene ID";
  private static final String ENTREZGENE_SYMBOL = "EntrezGene Symbol";
  private static final String OLD_ENTREZGENE_IDS = "Old EntrezGene IDs";
  private static final String GENBANK_ACCESSION_NUMBERS = "GenBank Accession Numbers";
  private static final String SPECIES_NAME = "Species Name";


  // private instance fields

  private Gene _gene;
  private List<String> _names = new ArrayList<String>();
  private List<Object> _values = new ArrayList<Object>();
  private List<ValueType> _valueTypes = new ArrayList<ValueType>();
  private List<String> _descriptions = new ArrayList<String>();


  // public constructor and implementations of NameValueTable abstract methods

  public GeneNameValueTable(Gene gene)
  {
    _gene = gene;
    initializeLists(gene);
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
    return null;
  }

  @Override
  public String getLink(int index, String value)
  {
    String name = getName(index);
    if (name.equals(ENTREZGENE_ID) || name.equals(OLD_ENTREZGENE_IDS)) {
      log.info("LINK TO " + ENTREZGENE_ID_LOOKUP_URL_PREFIX + value);
      return ENTREZGENE_ID_LOOKUP_URL_PREFIX + value;
    }
    if (name.equals(GENBANK_ACCESSION_NUMBERS)) {
      return GENBANK_ACCESSION_NUMBER_LOOKUP_URL_PREFIX + value;
    }
    // other fields do not have links
    return null;
  }


  // private instance methods

  /**
   * Initialize the lists {@link #_names}, {@link #_values}, and {@link #_valueTypes}. Don't
   * add rows for missing values.
   */
  private void initializeLists(Gene gene)
  {
    if (gene == null) { 
      gene = Gene.NullGene;
    }
    addItem(GENE_NAME, gene.getGeneName(), ValueType.TEXT, "The name of the gene, as labelled in EntrezGene");
    addItem(ENTREZGENE_ID, gene.getEntrezgeneId(), ValueType.LINK, "The EntrezGene ID, a.k.a. Locus ID");
    addItem(ENTREZGENE_SYMBOL, gene.getEntrezgeneSymbols(), ValueType.TEXT_LIST, "The EntrezGene Gene Symbols");
    addItem(GENBANK_ACCESSION_NUMBERS, gene.getGenbankAccessionNumbers(), ValueType.LINK_LIST, "The GenBank Accession Numbers for the gene");
    addItem(SPECIES_NAME, gene.getSpeciesName(), ValueType.TEXT, "The species this gene is found in");
  }

  private void addItem(String name, Object value, ValueType valueType, String description)
  {
    _names.add(name);
    _values.add(value);
    _valueTypes.add(valueType);
    _descriptions.add(description);
  }
}

