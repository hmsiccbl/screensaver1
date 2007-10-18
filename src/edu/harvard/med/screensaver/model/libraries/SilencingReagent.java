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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.SemanticIDAbstractEntity;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;


/**
 * A Hibernate entity bean representing a silencing reagent.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@Table(uniqueConstraints={
  @UniqueConstraint(columnNames={ "geneId", "silencingReagentType", "sequence" })
})
@org.hibernate.annotations.Proxy
@ContainedEntity(containingEntityClass=Gene.class)
public class SilencingReagent extends SemanticIDAbstractEntity
{

  // static fields

  private static final Logger log = Logger.getLogger(SilencingReagent.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private String _silencingReagentId;
  private Integer _version;
  private Gene _gene;
  private Set<Well> _wells = new HashSet<Well>();
  private SilencingReagentType _silencingReagentType;
  private String _sequence;
  private Set<String> _nonTargettedGenbankAccessionNumbers = new HashSet<String>();
  private boolean _isPoolOfUnknownSequences;


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public String getEntityId()
  {
    return getSilencingReagentId();
  }

  /**
   * Get the id for the silencing reagent.
   * @return the id for the silencing reagent
   */
  @Id
  @org.hibernate.annotations.Type(type="text")
  public String getSilencingReagentId()
  {
    return _silencingReagentId;
  }

  /**
   * Get the gene.
   * @return the gene
   */
  @ManyToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
             fetch=FetchType.LAZY)
  @JoinColumn(name="geneId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_silencing_reagent_to_gene")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE
  })
  public Gene getGene()
  {
    return _gene;
  }

  /**
   * Get the set of wells.
   * @return the set of wells
   */
  @ManyToMany(
    cascade={ CascadeType.PERSIST, CascadeType.MERGE },
    mappedBy="silencingReagents",
    targetEntity=Well.class,
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.ForeignKey(name="fk_well_silencing_reagent_link_to_silencing_reagent")
  @org.hibernate.annotations.LazyCollection(value=org.hibernate.annotations.LazyCollectionOption.TRUE)
  @org.hibernate.annotations.Cascade(value=org.hibernate.annotations.CascadeType.SAVE_UPDATE)
  public Set<Well> getWells()
  {
    return _wells;
  }

  /**
   * Add the well.
   * @param well the well to add
   * @return true iff the silencing reagent did not already have the well
   */
  public boolean addWell(Well well)
  {
    well.getSilencingReagents().add(this);
    return _wells.add(well);
  }

  /**
   * Remove the well.
   * @param well the well to remove
   * @return true iff the silencing reagent previously had the well
   */
  public boolean removeWell(Well well)
  {
    well.getSilencingReagents().remove(this);
    return _wells.remove(well);
  }

  /**
   * Get the silencing reagent type.
   * @return the silencing reagent type
   */
  @Immutable
  @Column(nullable=false)
  @org.hibernate.annotations.Type(
    type="edu.harvard.med.screensaver.model.libraries.SilencingReagentType$UserType"
  )
  public SilencingReagentType getSilencingReagentType()
  {
    return _silencingReagentType;
  }

  /**
   * Get the sequence.
   * @return the sequence
   */
  @org.hibernate.annotations.Immutable
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getSequence()
  {
    return _sequence;
  }

  /**
   * Get the non-targetted GenBank accession numbers.
   * @return the non-targetted GenBank accession numbers
   */
  @org.hibernate.annotations.CollectionOfElements
  @Column(name="nonTargettedGenbankAccessionNumber", nullable=false)
  @JoinTable(
    name="silencingReagentNonTargettedGenbankAccessionNumber",
    joinColumns=@JoinColumn(name="silencingReagentId")
  )
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_silencing_reagent_non_targetted_genbank_accession_number_to_silencing_reagent")
  @OrderBy("nonTargettedGenbankAccessionNumber")
  public Set<String> getNonTargettedGenbankAccessionNumbers()
  {
    return _nonTargettedGenbankAccessionNumbers;
  }

  /**
   * Add the non-targetted GenBank accession number.
   * @param nonTargettedGenbankAccessionNumber the non-targetted GenBank accession number to add
   * @return true iff the silencing reagent did not already have the non-targetted GenBank accession number
   */
  public boolean addNonTargettedGenbankAccessionNumber(String nonTargettedGenbankAccessionNumber)
  {
    return _nonTargettedGenbankAccessionNumbers.add(nonTargettedGenbankAccessionNumber);
  }

  /**
   * Remove the non-targetted GenBank accession number.
   * @param nonTargettedGenbankAccessionNumber the non-targetted GenBank accession number to remove
   * @return true iff the silencing reagent previously had the non-targetted GenBank accession number
   */
  public boolean removeNonTargettedGenbankAccessionNumber(String nonTargettedGenbankAccessionNumber)
  {
    return _nonTargettedGenbankAccessionNumbers.remove(nonTargettedGenbankAccessionNumber);
  }

  /**
   * Get the isPoolOfUnknownSequences.
   * @return the isPoolOfUnknownSequences
   */
  @org.hibernate.annotations.Immutable
  @Column(nullable=false, name="isPoolOfUnknownSequences")
  public boolean isPoolOfUnknownSequences()
  {
    return _isPoolOfUnknownSequences;
  }


  // package constructor

  /**
   * Construct a <code>SilencingReagent</code>.
   *
   * @param gene the gene
   * @param silencingReagentType the silencing reagent type
   * @param sequence the sequence
   * @param isPoolOfUnknownSequences
   * @motivation for use of {@link Gene#createSilencingReagent} methods only
   */
  SilencingReagent(
    Gene gene,
    SilencingReagentType silencingReagentType,
    String sequence,
    boolean isPoolOfUnknownSequences)
  {
    _gene = gene;
    _silencingReagentType = silencingReagentType;
    _sequence = sequence;
    _isPoolOfUnknownSequences = isPoolOfUnknownSequences;
    _silencingReagentId =
      getGene().toString() + ":" +
      getSilencingReagentType().toString() + ":" +
      getSequence();
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>SilencingReagent</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected SilencingReagent() {}


  // private constructor and instance methods

  /**
   * Set the id for the silencing reagent.
   * @param silencingReagentId the new id for the silencing reagent
   * @motivation for hibernate
   */
  private void setSilencingReagentId(String silencingReagentId)
  {
    _silencingReagentId = silencingReagentId;
  }

  /**
   * Get the version for the silencing reagent.
   * @return the version for the silencing reagent
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the silencing reagent.
   * @param version the new version for the silencing reagent
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the gene.
   * Throw a NullPointerException when the gene is null.
   *
   * @param gene the new gene
   * @throws NullPointerException when the gene is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  private void setGene(Gene gene)
  {
    if (gene == null) {
      throw new NullPointerException();
    }
    _gene = gene;
  }

  /**
   * Set the set of wells.
   * @param wells the new set of wells
   * @motivation for hibernate
   */
  private void setWells(Set<Well> wells)
  {
    _wells = wells;
  }

  /**
   * Set the silencing reagent type.
   * @param silencingReagentType the new silencing reagent type
   * @motivation for hibernate
   */
  private void setSilencingReagentType(SilencingReagentType silencingReagentType)
  {
    _silencingReagentType = silencingReagentType;
  }

  /**
   * Set the sequence.
   * @param sequence the new sequence
   * @motivation for hibernate
   */
  private void setSequence(String sequence)
  {
    _sequence = sequence;
  }

  /**
   * Set the non-targetted GenBank accession numbers.
   *
   * @param nonTargettedGenbankAccessionNumbers the new non-targetted GenBank accession numbers
   * @motivation for hibernate
   */
  private void setNonTargettedGenbankAccessionNumbers(Set<String> nonTargettedGenbankAccessionNumbers)
  {
    _nonTargettedGenbankAccessionNumbers = nonTargettedGenbankAccessionNumbers;
  }

  /**
   * Set the isPoolOfUnknownSequences.
   * @param isPoolOfUnknownSequences the isPoolOfUnknownSequences
   * @motivatin for hibernate
   */
  private void setPoolOfUnknownSequences(boolean isPoolOfUnknownSequences)
  {
    _isPoolOfUnknownSequences = isPoolOfUnknownSequences;
  }
}
