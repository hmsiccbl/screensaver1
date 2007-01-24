// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.ui.control.LibrariesController;

public class GeneNameValueTable extends NameValueTable
{
  
  // private static final fields
  
  private static final Logger log = Logger.getLogger(GeneNameValueTable.class);
  private static final String GENBANK_ACCESSION_NUMBER_LOOKUP_URL_PREFIX =
    "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?val=";
  private static final String ENTREZGENE_ID_LOOKUP_URL_PREFIX =
    "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=Retrieve&dopt=full_report&list_uids=";
  
  private static final String GENE_NAME = "Gene Name";
  private static final String ENTREZGENE_ID = "EntrezGene ID";
  private static final String ENTREZGENE_SYMBOL = "EntrezGene Symbol";
  private static final String GENBANK_ACCESSION_NUMBERS = "GenBank Accession Numbers";
  private static final String SPECIES_NAME = "Species Name";

  
  // private instance fields
  
  private LibrariesController _librariesController;
  private Gene _gene;
  private List<String> _names = new ArrayList<String>();
  private List<Object> _values = new ArrayList<Object>();
  private List<ValueType> _valueTypes = new ArrayList<ValueType>();
  
  public GeneNameValueTable(LibrariesController librariesController, Gene gene)
  {
    _librariesController = librariesController;
    _gene = gene;
    initializeLists(gene);
    setDataModel(new ListDataModel(_values));
  }

  protected String getAction(int index, String value)
  {
    String name = getName(index);
    if (name.equals(GENE_NAME)) {
      return _librariesController.viewGene(_gene, null);
    }
    // other fields do not have actions
    return null;
  }
  
  protected String getLink(int index, String value)
  {
    String name = getName(index);
    if (name.equals(ENTREZGENE_ID)) {
      return ENTREZGENE_ID_LOOKUP_URL_PREFIX + value;
    }
    if (name.equals(GENBANK_ACCESSION_NUMBERS)) {
      return GENBANK_ACCESSION_NUMBER_LOOKUP_URL_PREFIX + value;
    }
    // other fields do not have links
    return null;
  }

  public String getName(int index)
  {
    return _names.get(index);
  }

  public int getNumRows()
  {
    return _names.size();
  }

  protected Object getValue(int index)
  {
    return _values.get(index);
  }

  protected ValueType getValueType(int index)
  {
    return _valueTypes.get(index);
  }

  private void initializeLists(Gene gene) {
    addItem(GENE_NAME, gene.getGeneName(), ValueType.COMMAND);
    addItem(ENTREZGENE_ID, Integer.toString(gene.getEntrezgeneId()), ValueType.LINK);
    addItem(ENTREZGENE_SYMBOL, gene.getEntrezgeneSymbol(), ValueType.TEXT);
    addItem(GENBANK_ACCESSION_NUMBERS, gene.getGenbankAccessionNumbers(), ValueType.LINK_LIST);
    addItem(SPECIES_NAME, gene.getSpeciesName(), ValueType.TEXT);
  }

  private void addItem(String name, Object value, ValueType valueType)
  {
    _names.add(name);
    _values.add(value);
    _valueTypes.add(valueType);
  }
}

