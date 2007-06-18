// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.EntityIdProperty;
import edu.harvard.med.screensaver.model.ToManyRelationship;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a gene.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public class Gene extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(Gene.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _version;
  private Set<SilencingReagent> _silencingReagents = new HashSet<SilencingReagent>();
  private String _geneName;
  private Integer _entrezgeneId;
  private String _entrezgeneSymbol;
  private Set<Integer> _oldEntrezgeneIds = new HashSet<Integer>();
  private Set<String> _oldEntrezgeneSymbols = new HashSet<String>();
  private Set<String> _genbankAccessionNumbers = new HashSet<String>();
  private String _speciesName;


  // public constructor

  // public constructor
  
  /**
   * Constructs an initialized <code>Gene</code> object.
   *
   * @param geneName the gene name
   * @param entrezgeneId the EntrezGene ID
   * @param entrezgeneSymbol the EntrezGene symbol
   * @param speciesName the species name
   */
  public Gene(
    String geneName,
    Integer entrezgeneId,
    String entrezgeneSymbol,
    String genbankAccessionNumber,
    String speciesName)
  {
    this(geneName, entrezgeneId, entrezgeneSymbol, speciesName);
    _genbankAccessionNumbers.add(genbankAccessionNumber);
  }


  /**
   * Constructs an initialized <code>Gene</code> object.
   *
   * @param geneName the gene name
   * @param entrezgeneId the EntrezGene ID
   * @param entrezgeneSymbol the EntrezGene symbol
   * @param speciesName the species name
   */
  public Gene(
    String geneName,
    Integer entrezgeneId,
    String entrezgeneSymbol,
    String speciesName)
  {
    _geneName = geneName;
    _entrezgeneId = entrezgeneId;
    _entrezgeneSymbol = entrezgeneSymbol;
    _speciesName = speciesName;
  }


  // public methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }
  
  @Override
  @EntityIdProperty
  public Integer getEntityId()
  {
    return getBusinessKey();
  }

  /**
   * Get the id for the gene.
   *
   * @return the id for the gene
   * @hibernate.id
   *   generator-class="assigned"
   */
  @EntityIdProperty
  public Integer getGeneId()
  {
    return getBusinessKey();
  }

  /**
   * Get an unmodifiable copy of the set of silencing reagents.
   *
   * @return the silencing reagents
   */
  public Set<SilencingReagent> getSilencingReagents()
  {
    return Collections.unmodifiableSet(_silencingReagents);
  }


  /**
   * Get the set of wells that contain silencing reagents for this gene.
   * @return the set of wells that contain silencing reagents for this gene
   */
  @DerivedEntityProperty
  public Set<Well> getWells()
  {
    Set<Well> wells = new HashSet<Well>();
    for (SilencingReagent silencingReagent : getSilencingReagents()) {
      for (Well well : silencingReagent.getWells()) {
        wells.add(well);
      }
    }
    return wells;
  }
  
  /**
   * Get the gene name.
   *
   * @return the gene name
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getGeneName()
  {
    return _geneName;
  }

  /**
   * Set the gene name.
   *
   * @param geneName the new gene name
   */
  public void setGeneName(String geneName)
  {
    _geneName = geneName;
  }

  /**
   * Get the EntrezGene ID.
   *
   * @return the EntrezGene ID
   * @hibernate.property
   *   not-null="true"
   *   unique="true"
   */
  @EntityIdProperty
  public Integer getEntrezgeneId()
  {
    return _entrezgeneId;
  }

  /**
   * Get the EntrezGene symbol.
   *
   * @return the EntrezGene symbol
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getEntrezgeneSymbol()
  {
    return _entrezgeneSymbol;
  }

  /**
   * Set the EntrezGene symbol.
   *
   * @param entrezgeneSymbol the new EntrezGene symbol
   */
  public void setEntrezgeneSymbol(String entrezgeneSymbol)
  {
    _entrezgeneSymbol = entrezgeneSymbol;
  }

  /**
   * Get the old EntrezGene IDs.
   *
   * @return the old EntrezGene IDs
   * @hibernate.set
   *   order-by="old_entrezgene_id"
   *   table="gene_old_entrezgene_id"
   *   cascade="delete"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="gene_id"
   *   foreign-key="fk_gene_old_entrezgene_id_to_gene"
   * @hibernate.collection-element
   *   type="int"
   *   column="old_entrezgene_id"
   *   not-null="true"
   */
  public Set<Integer> getOldEntrezgeneIds()
  {
    return _oldEntrezgeneIds;
  }

  /**
   * Add the old EntrezGene ID.
   *
   * @param oldEntrezgeneId the old EntrezGene ID to add
   * @return true iff the gene did not already have the old EntrezGene ID
   */
  public boolean addOldEntrezgeneId(Integer oldEntrezgeneId)
  {
    return _oldEntrezgeneIds.add(oldEntrezgeneId);
  }

  /**
   * Remove the old EntrezGene ID.
   *
   * @param oldEntrezgeneId the old EntrezGene ID to remove
   * @return true iff the gene previously had the old EntrezGene ID
   */
  public boolean removeOldEntrezgeneId(Integer oldEntrezgeneId)
  {
    return _oldEntrezgeneIds.remove(oldEntrezgeneId);
  }

  /**
   * Get the old EntrezGene symbols.
   *
   * @return the old EntrezGene symbols
   * @hibernate.set
   *   order-by="old_entrezgene_symbol"
   *   table="gene_old_entrezgene_symbol"
   *   cascade="delete"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="gene_id"
   *   foreign-key="fk_gene_old_entrezgene_symbol_to_gene"
   * @hibernate.collection-element
   *   type="text"
   *   column="old_entrezgene_symbol"
   *   not-null="true"
   */
  public Set<String> getOldEntrezgeneSymbols()
  {
    return _oldEntrezgeneSymbols;
  }

  /**
   * Add the old EntrezGene symbol.
   *
   * @param oldEntrezgeneSymbol the old EntrezGene symbol to add
   * @return true iff the gene did not already have the old EntrezGene symbol
   */
  public boolean addOldEntrezgeneSymbol(String oldEntrezgeneSymbol)
  {
    return _oldEntrezgeneSymbols.add(oldEntrezgeneSymbol);
  }

  /**
   * Remove the old EntrezGene symbol.
   *
   * @param oldEntrezgeneSymbol the old EntrezGene symbol to remove
   * @return true iff the gene previously had the old EntrezGene symbol
   */
  public boolean removeOldEntrezgeneSymbol(String oldEntrezgeneSymbol)
  {
    return _oldEntrezgeneSymbols.remove(oldEntrezgeneSymbol);
  }

  /**
   * Get the GenBank accession numbers.
   *
   * @return the GenBank accession numbers
   * @hibernate.set
   *   order-by="genbank_accession_number"
   *   table="gene_genbank_accession_number"
   *   cascade="delete"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="gene_id"
   * @hibernate.collection-element
   *   type="text"
   *   column="genbank_accession_number"
   *   not-null="true"
   */
  @ToManyRelationship(minCardinality=1)
  public Set<String> getGenbankAccessionNumbers()
  {
    return _genbankAccessionNumbers;
  }

  /**
   * Add the GenBank accession number.
   *
   * @param genbankAccessionNumber the GenBank accession number to add
   * @return true iff the gene did not already have the GenBank accession number
   */
  public boolean addGenbankAccessionNumber(String genbankAccessionNumber)
  {
    return _genbankAccessionNumbers.add(genbankAccessionNumber);
  }

  /**
   * Remove the GenBank accession number.
   *
   * @param genbankAccessionNumber the GenBank accession number to remove
   * @return true iff the gene previously had the GenBank accession number
   */
  public boolean removeGenbankAccessionNumber(String genbankAccessionNumber)
  {
    return _genbankAccessionNumbers.remove(genbankAccessionNumber);
  }

  /**
   * Get the species name.
   *
   * @return the species name
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getSpeciesName()
  {
    return _speciesName;
  }

  /**
   * Set the species name.
   *
   * @param speciesName the new species name
   */
  public void setSpeciesName(String speciesName)
  {
    _speciesName = speciesName;
  }


  // protected methods

  @Override
  protected Integer getBusinessKey()
  {
    return getEntrezgeneId();
  }


  // package methods

  /**
   * Get the silencing reagents.
   *
   * @return the silencing reagents
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="gene_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.libraries.SilencingReagent"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  Set<SilencingReagent> getHbnSilencingReagents()
  {
    return _silencingReagents;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>Gene</code> object.
   *
   * @motivation for hibernate
   */
  private Gene() {}


  // private methods

  /**
   * Set the id for the gene.
   *
   * @param geneId the new id for the gene
   * @motivation for hibernate
   */
  private void setGeneId(Integer geneId)
  {
  }

  /**
   * Set the EntrezGene ID.
   *
   * @param entrezgeneId the new EntrezGene ID
   */
  private void setEntrezgeneId(Integer entrezgeneId)
  {
    _entrezgeneId = entrezgeneId;
  }  
  
  /**
   * Get the version for the gene.
   *
   * @return the version for the gene
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the gene.
   *
   * @param version the new version for the gene
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the silencing reagents.
   *
   * @param silencingReagents the new silencing reagents
   * @motivation for hibernate
   */
  private void setHbnSilencingReagents(Set<SilencingReagent> silencingReagents)
  {
    _silencingReagents = silencingReagents;
  }

  /**
   * Set the old EntrezGene IDs.
   *
   * @param oldEntrezgeneIds the new old EntrezGene IDs
   * @motivation for hibernate
   */
  private void setOldEntrezgeneIds(Set<Integer> oldEntrezgeneIds)
  {
    _oldEntrezgeneIds = oldEntrezgeneIds;
  }

  /**
   * Set the old EntrezGene symbols.
   *
   * @param oldEntrezgeneSymbols the new old EntrezGene symbols
   * @motivation for hibernate
   */
  private void setOldEntrezgeneSymbols(Set<String> oldEntrezgeneSymbols)
  {
    _oldEntrezgeneSymbols = oldEntrezgeneSymbols;
  }

  /**
   * Set the GenBank accession numbers.
   *
   * @param genbankAccessionNumbers the new GenBank accession numbers
   * @motivation for hibernate
   */
  private void setGenbankAccessionNumbers(Set<String> genbankAccessionNumbers)
  {
    _genbankAccessionNumbers = genbankAccessionNumbers;
  }
}
