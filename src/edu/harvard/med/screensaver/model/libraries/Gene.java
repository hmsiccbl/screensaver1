// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OrderBy;

import com.google.common.collect.Sets;
import org.hibernate.annotations.Immutable;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.annotations.CollectionOfElements;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;


/**
 * Information about a gene (or what is or was considered to be a gene) at a
 * particular point in time, by a particular authority.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@Immutable
@org.hibernate.annotations.Proxy
@ContainedEntity(containingEntityClass=SilencingReagent.class)
public class Gene extends AbstractEntity<Integer>
{
  private static final long serialVersionUID = 0L;

  public static final PropertyPath<Gene> genbankAccessionNumbers = RelationshipPath.from(Gene.class).toCollectionOfValues("genbankAccessionNumbers");
  public static final PropertyPath<Gene> entrezgeneSymbols = RelationshipPath.from(Gene.class).toCollectionOfValues("entrezgeneSymbols");

  public static final Gene NullGene = new Gene();

  private String _geneName;
  private Integer _entrezgeneId;
  private Set<String> _entrezgeneSymbols = Sets.newHashSet();
  private Set<String> _genbankAccessionNumbers = Sets.newHashSet();
  private String _speciesName;

  /**
   * @motivation for SilencingReagent, which instantiates empty, related Gene entities 
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  public Gene() {}

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Id
  @org.hibernate.annotations.GenericGenerator(name="gene_id_seq",
                                              strategy="sequence",
                                              parameters = { @org.hibernate.annotations.Parameter(name="sequence", value="gene_id_seq")})
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="gene_id_seq")
  public Integer getGeneId()
  {
    return getEntityId();
  }

  private void setGeneId(Integer geneId)
  {
    setEntityId(geneId);
  }

  @Column
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true)
  public Integer getEntrezgeneId()
  {
    return _entrezgeneId;
  }

  private void setEntrezgeneId(Integer entrezgeneId)
  {
    _entrezgeneId = entrezgeneId;
  }
  
  /**
   * Builder method to set entrez gene ID before entity is persisted 
   * @motivation builder method
   * @return this Gene
   */
  public Gene withEntrezgeneId(Integer entrezgeneId)
  {
    if (!!!isTransient()) {
      throw new DataModelViolationException("immutable property cannot be changed after entity is persisted");
    }
    setEntrezgeneId(entrezgeneId);
    return this;
  }
  

  @Column
  @org.hibernate.annotations.Type(type="text")
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true)
  public String getGeneName()
  {
    return _geneName;
  }

  private void setGeneName(String geneName)
  {
    _geneName = geneName;
  }

  /**
   * Builder method to set gene name before entity is persisted 
   * @motivation builder method
   * @return this Gene
   */
  public Gene withGeneName(String geneName)
  {
    if (!!!isTransient()) {
      throw new DataModelViolationException("immutable property cannot be changed after entity is persisted");
    }
    setGeneName(geneName);
    return this;
  }

  @org.hibernate.annotations.CollectionOfElements
  @CollectionOfElements(hasNonconventionalMutation=true)
  @Column(name="entrezgeneSymbol", nullable=false)
  @JoinTable(name="geneSymbol", joinColumns=@JoinColumn(name="geneId"))
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_gene_symbol_to_gene")
  public Set<String> getEntrezgeneSymbols()
  {
    return _entrezgeneSymbols;
  }

  private void setEntrezgeneSymbols(Set<String> entrezgeneSymbols)
  {
    _entrezgeneSymbols = entrezgeneSymbols;
  }
  
  /**
   * Builder method to add entrezgene symbol before entity is persisted 
   * @motivation builder method
   * @return this Gene
   */
  public Gene withEntrezgeneSymbol(String entrezgeneSymbol)
  {
    _entrezgeneSymbols.add(entrezgeneSymbol);
    return this;
  }

  @org.hibernate.annotations.CollectionOfElements
  @CollectionOfElements(hasNonconventionalMutation=true)
  @Column(name="genbankAccessionNumber", nullable=false)
  @JoinTable(name="geneGenbankAccessionNumber", joinColumns=@JoinColumn(name="geneId"))
  @OrderBy("genbankAccessionNumber")
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_gene_genbank_accession_number_to_gene")
  public Set<String> getGenbankAccessionNumbers()
  {
    return _genbankAccessionNumbers;
  }

  private void setGenbankAccessionNumbers(Set<String> genbankAccessionNumbers)
  {
    _genbankAccessionNumbers = genbankAccessionNumbers;
  }

  /**
   * Builder method to add entrezgene symbol before entity is persisted 
   * @motivation builder method
   * @return this Gene
   */
  public Gene withGenbankAccessionNumber(String genbankAccessionNumber)
  {
    _genbankAccessionNumbers.add(genbankAccessionNumber);
    return this;
  }

  @Column
  @org.hibernate.annotations.Type(type="text")
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true)
  public String getSpeciesName()
  {
    return _speciesName;
  }

  private void setSpeciesName(String speciesName)
  {
    _speciesName = speciesName;
  }

  /**
   * Builder method to set species name before entity is persisted 
   * @motivation builder method
   * @return this Gene
   */
  public Gene withSpeciesName(String speciesName)
  {
    if (!!!isTransient()) {
      throw new DataModelViolationException("immutable property cannot be changed after entity is persisted");
    }
    setSpeciesName(speciesName);
    return this;
  }

}
