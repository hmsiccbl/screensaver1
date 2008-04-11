// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.SemanticIDAbstractEntity;


/**
 * A Hibernate entity bean representing a gene.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
public class Gene extends SemanticIDAbstractEntity
{

  // static fields

  private static final Logger log = Logger.getLogger(Gene.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _geneId;
  private Integer _version;
  private Set<SilencingReagent> _silencingReagents = new HashSet<SilencingReagent>();
  private String _geneName;
  private Integer _entrezgeneId;
  private String _entrezgeneSymbol;
  private Set<Integer> _oldEntrezgeneIds = new HashSet<Integer>();
  private Set<String> _oldEntrezgeneSymbols = new HashSet<String>();
  private Set<String> _genbankAccessionNumbers = new HashSet<String>();
  private String _speciesName;


  // public constructors

  /**
   * Construct a <code>Gene</code>.
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
   * Construct a <code>Gene</code>.
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
    _geneId = entrezgeneId;
    _geneName = geneName;
    _entrezgeneId = entrezgeneId;
    _entrezgeneSymbol = entrezgeneSymbol;
    _speciesName = speciesName;
  }


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getGeneId();
  }

  /**
   * Get the id for the gene.
   * @return the id for the gene
   */
  @Id
  public Integer getGeneId()
  {
    return _geneId;
  }

  /**
   * Get the set of silencing reagents.
   * @return the set of silencing reagents
   */
  @OneToMany(
    mappedBy="gene",
    fetch=FetchType.LAZY
  )
  public Set<SilencingReagent> getSilencingReagents()
  {
    return _silencingReagents;
  }

  /**
   * Create a new silencing reagent for the gene.
   * @param silencingReagentType the silencing reagent type
   * @param sequence the sequence
   * @return the new silencing reagent
   */
  public SilencingReagent createSilencingReagent(
    SilencingReagentType silencingReagentType,
    String sequence)
  {
    return createSilencingReagent(silencingReagentType, sequence, false);
  }

  /**
   * Create a new silencing reagent for the gene.
   * @param silencingReagentType the silencing reagent type
   * @param sequence the sequence
   * @param isPoolOfUnknownSequences
   * @return the new silencing reagent
   */
  public SilencingReagent createSilencingReagent(
    SilencingReagentType silencingReagentType,
    String sequence,
    boolean isPoolOfUnknownSequences)
  {
    SilencingReagent silencingReagent = new SilencingReagent(
      this,
      silencingReagentType,
      sequence,
      isPoolOfUnknownSequences);
    if (! _silencingReagents.add(silencingReagent)) {
      throw new DuplicateEntityException(this, silencingReagent);
    }
    return silencingReagent;
  }

  /**
   * Get the set of wells that contain silencing reagents for this gene.
   * @return the set of wells that contain silencing reagents for this gene
   */
  @Transient
  public Set<Well> getWells()
  {
    Set<Well> wells = new HashSet<Well>();
    for (SilencingReagent silencingReagent : getSilencingReagents()) {
      wells.addAll(silencingReagent.getWells());
    }
    return wells;
  }

  /**
   * Get the gene name.
   * @return the gene name
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getGeneName()
  {
    return _geneName;
  }

  /**
   * Set the gene name.
   * @param geneName the new gene name
   */
  public void setGeneName(String geneName)
  {
    _geneName = geneName;
  }

  /**
   * Get the EntrezGene ID.
   * @return the EntrezGene ID
   */
  @org.hibernate.annotations.Immutable
  @Column(nullable=false, unique=true)
  public Integer getEntrezgeneId()
  {
    return _entrezgeneId;
  }

  /**
   * Get the EntrezGene symbol.
   * @return the EntrezGene symbol
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getEntrezgeneSymbol()
  {
    return _entrezgeneSymbol;
  }

  /**
   * Set the EntrezGene symbol.
   * @param entrezgeneSymbol the new EntrezGene symbol
   */
  public void setEntrezgeneSymbol(String entrezgeneSymbol)
  {
    _entrezgeneSymbol = entrezgeneSymbol;
  }

  /**
   * Get the old EntrezGene IDs.
   * @return the old EntrezGene IDs
   */
  @org.hibernate.annotations.CollectionOfElements
  @Column(name="oldEntrezgeneId", nullable=false)
  @JoinTable(
    name="geneOldEntrezgeneId",
    joinColumns=@JoinColumn(name="geneId")
  )
  @org.hibernate.annotations.ForeignKey(name="fk_gene_old_entrezgene_id_to_gene")
  @OrderBy("oldEntrezgeneId") // TODO: test this (somehow)
  public Set<Integer> getOldEntrezgeneIds()
  {
    return _oldEntrezgeneIds;
  }

  /**
   * Add the old EntrezGene ID.
   * @param oldEntrezgeneId the old EntrezGene ID to add
   * @return true iff the gene did not already have the old EntrezGene ID
   */
  public boolean addOldEntrezgeneId(Integer oldEntrezgeneId)
  {
    return _oldEntrezgeneIds.add(oldEntrezgeneId);
  }

  /**
   * Remove the old EntrezGene ID.
   * @param oldEntrezgeneId the old EntrezGene ID to remove
   * @return true iff the gene previously had the old EntrezGene ID
   */
  public boolean removeOldEntrezgeneId(Integer oldEntrezgeneId)
  {
    return _oldEntrezgeneIds.remove(oldEntrezgeneId);
  }

  /**
   * Get the old EntrezGene symbols.
   * @return the old EntrezGene symbols
   */
  @org.hibernate.annotations.CollectionOfElements
  @Column(name="oldEntrezgeneSymbol", nullable=false)
  @JoinTable(
    name="geneOldEntrezgeneSymbol",
    joinColumns=@JoinColumn(name="geneId")
  )
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_gene_old_entrezgene_symbol_to_gene")
  @OrderBy("oldEntrezgeneSymbol") // TODO: test this (somehow)
  public Set<String> getOldEntrezgeneSymbols()
  {
    return _oldEntrezgeneSymbols;
  }

  /**
   * Add the old EntrezGene symbol.
   * @param oldEntrezgeneSymbol the old EntrezGene symbol to add
   * @return true iff the gene did not already have the old EntrezGene symbol
   */
  public boolean addOldEntrezgeneSymbol(String oldEntrezgeneSymbol)
  {
    return _oldEntrezgeneSymbols.add(oldEntrezgeneSymbol);
  }

  /**
   * Remove the old EntrezGene symbol.
   * @param oldEntrezgeneSymbol the old EntrezGene symbol to remove
   * @return true iff the gene previously had the old EntrezGene symbol
   */
  public boolean removeOldEntrezgeneSymbol(String oldEntrezgeneSymbol)
  {
    return _oldEntrezgeneSymbols.remove(oldEntrezgeneSymbol);
  }

  /**
   * Get the GenBank accession numbers.
   * @return the GenBank accession numbers
   */
  @Column(name="genbankAccessionNumber", nullable=false)
  @JoinTable(
    name="geneGenbankAccessionNumber",
    joinColumns=@JoinColumn(name="geneId")
  )
  @OrderBy("genbankAccessionNumber")
  @org.hibernate.annotations.CollectionOfElements
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_gene_genbank_accession_number_to_gene")
  @edu.harvard.med.screensaver.model.annotations.CollectionOfElements(initialCardinality=1)
  public Set<String> getGenbankAccessionNumbers()
  {
    return _genbankAccessionNumbers;
  }

  /**
   * Add the GenBank accession number.
   * @param genbankAccessionNumber the GenBank accession number to add
   * @return true iff the gene did not already have the GenBank accession number
   */
  public boolean addGenbankAccessionNumber(String genbankAccessionNumber)
  {
    return _genbankAccessionNumbers.add(genbankAccessionNumber);
  }

  /**
   * Remove the GenBank accession number.
   * @param genbankAccessionNumber the GenBank accession number to remove
   * @return true iff the gene previously had the GenBank accession number
   */
  public boolean removeGenbankAccessionNumber(String genbankAccessionNumber)
  {
    return _genbankAccessionNumbers.remove(genbankAccessionNumber);
  }

  /**
   * Get the species name.
   * @return the species name
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getSpeciesName()
  {
    return _speciesName;
  }

  /**
   * Set the species name.
   * @param speciesName the new species name
   */
  public void setSpeciesName(String speciesName)
  {
    _speciesName = speciesName;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>Gene</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected Gene() {}


  // private constructor and instance methods

  /**
   * Set the id for the gene.
   * @param geneId the new id for the gene
   * @motivation for hibernate
   */
  private void setGeneId(Integer geneId)
  {
    _geneId = geneId;
  }

  /**
   * Get the version for the gene.
   * @return the version for the gene
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the gene.
   * @param version the new version for the gene
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the set of silencing reagents.
   * @param silencingReagents the new set of silencing reagents
   * @motivation for hibernate
   */
  private void setSilencingReagents(Set<SilencingReagent> silencingReagents)
  {
    _silencingReagents = silencingReagents;
  }

  /**
   * Set the EntrezGene ID.
   * @param entrezgeneId the new EntrezGene ID
   * @motivation for hibernate
   */
  private void setEntrezgeneId(Integer entrezgeneId)
  {
    _entrezgeneId = entrezgeneId;
  }

  /**
   * Set the old EntrezGene IDs.
   * @param oldEntrezgeneIds the new old EntrezGene IDs
   * @motivation for hibernate
   */
  private void setOldEntrezgeneIds(Set<Integer> oldEntrezgeneIds)
  {
    _oldEntrezgeneIds = oldEntrezgeneIds;
  }

  /**
   * Set the old EntrezGene symbols.
   * @param oldEntrezgeneSymbols the new old EntrezGene symbols
   * @motivation for hibernate
   */
  private void setOldEntrezgeneSymbols(Set<String> oldEntrezgeneSymbols)
  {
    _oldEntrezgeneSymbols = oldEntrezgeneSymbols;
  }

  /**
   * Set the GenBank accession numbers.
   * @param genbankAccessionNumbers the new GenBank accession numbers
   * @motivation for hibernate
   */
  private void setGenbankAccessionNumbers(Set<String> genbankAccessionNumbers)
  {
    _genbankAccessionNumbers = genbankAccessionNumbers;
  }
}
