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

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;


/**
 * A Hibernate entity bean representing a silencing reagent.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public class SilencingReagent extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(SilencingReagent.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _silencingReagentId;
  private Integer _version;
  private Gene _gene;
  private Set<Well> _wells = new HashSet<Well>();
  private SilencingReagentType _silencingReagentType;
  private String _sequence;
  private Set<String> _nonTargettedGenbankAccessionNumbers = new HashSet<String>();


  // public constructor

  /**
   * Constructs an initialized <code>SilencingReagent</code> object.
   *
   * @param gene the gene
   * @param silencingReagentType the silencing reagent type
   * @param sequence the sequence
   */
  public SilencingReagent(
    Gene gene,
    SilencingReagentType silencingReagentType,
    String sequence)
  {
    _gene = gene;
    _silencingReagentType = silencingReagentType;
    _sequence = sequence;
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getSilencingReagentId();
  }

  /**
   * Get the id for the silencing reagent.
   *
   * @return the id for the silencing reagent
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="silencing_reagent_id_seq"
   */
  public Integer getSilencingReagentId()
  {
    return _silencingReagentId;
  }

  /**
   * Get the gene.
   *
   * @return the gene
   */
  public Gene getGene()
  {
    return _gene;
  }

  /**
   * Set the gene.
   *
   * @param gene the new gene
   */
  public void setGene(Gene gene)
  {
    temporarilyRemoveFromRelationships();
    _gene = gene;
    reinstateRelationships();
  }

  /**
   * Get an unmodifiable copy of the set of wells.
   *
   * @return the wells
   */
  public Set<Well> getWells()
  {
    return Collections.unmodifiableSet(_wells);
  }

  /**
   * Add the well.
   *
   * @param well the well to add
   * @return true iff the silencing reagent did not already have the well
   */
  public boolean addWell(Well well)
  {
    if (getHbnWells().add(well)) {
      return well.getHbnSilencingReagents().add(this);
    }
    return false;
  }

  /**
   * Remove the well.
   *
   * @param well the well to remove
   * @return true iff the silencing reagent previously had the well
   */
  public boolean removeWell(Well well)
  {
    if (getHbnWells().remove(well)) {
      return well.getHbnSilencingReagents().remove(this);
    }
    return false;
  }

  /**
   * Get the silencing reagent type.
   *
   * @return the silencing reagent type
   */
  public SilencingReagentType getSilencingReagentType()
  {
    return _silencingReagentType;
  }

  /**
   * Set the silencing reagent type.
   *
   * @param silencingReagentType the new silencing reagent type
   */
  public void setSilencingReagentType(SilencingReagentType silencingReagentType)
  {
    temporarilyRemoveFromRelationships();
    _silencingReagentType = silencingReagentType;
    reinstateRelationships();
  }

  /**
   * Get the sequence.
   *
   * @return the sequence
   */
  public String getSequence()
  {
    return _sequence;
  }

  /**
   * Set the sequence.
   *
   * @param sequence the new sequence
   */
  public void setSequence(String sequence)
  {
    temporarilyRemoveFromRelationships();
    _sequence = sequence;
    reinstateRelationships();
  }

  /**
   * Get the non-targetted GenBank accession numbers.
   *
   * @return the non-targetted GenBank accession numbers
   * @hibernate.set
   *   order-by="non_targetted_genbank_accession_number"
   *   table="silencing_reagent_non_targetted_genbank_accession_number"
   *   cascade="delete"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="silencing_reagent_id"
   *   foreign-key="fk_silencing_reagent_non_targetted_genbank_accession_number_to_silencing_reagent"
   * @hibernate.collection-element
   *   type="text"
   *   column="non_targetted_genbank_accession_number"
   *   not-null="true"
   */
  public Set<String> getNonTargettedGenbankAccessionNumbers()
  {
    return _nonTargettedGenbankAccessionNumbers;
  }

  /**
   * Add the non-targetted GenBank accession number.
   *
   * @param nonTargettedGenbankAccessionNumber the non-targetted GenBank accession number to add
   * @return true iff the silencing reagent did not already have the non-targetted GenBank accession number
   */
  public boolean addNonTargettedGenbankAccessionNumber(String nonTargettedGenbankAccessionNumber)
  {
    return _nonTargettedGenbankAccessionNumbers.add(nonTargettedGenbankAccessionNumber);
  }

  /**
   * Remove the non-targetted GenBank accession number.
   *
   * @param nonTargettedGenbankAccessionNumber the non-targetted GenBank accession number to remove
   * @return true iff the silencing reagent previously had the non-targetted GenBank accession number
   */
  public boolean removeNonTargettedGenbankAccessionNumber(Integer nonTargettedGenbankAccessionNumber)
  {
    return _nonTargettedGenbankAccessionNumbers.remove(nonTargettedGenbankAccessionNumber);
  }


  // protected methods

  /**
   * A business key class for the silencing reagent.
   */
  private class BusinessKey
  {
    
    /**
     * Get the gene.
     *
     * @return the gene
     */
    public Gene getGene()
    {
      return _gene;
    }

    /**
     * Get the silencing reagent type.
     *
     * @return the silencing reagent type
     */
    public SilencingReagentType getSilencingReagentType()
    {
      return _silencingReagentType;
    }

    /**
     * Get the sequence.
     *
     * @return the sequence
     */
    public String getSequence()
    {
      return _sequence;
    }

    @Override
    public boolean equals(Object object)
    {
      if (!(object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        getGene().equals(that.getGene()) &&
        getSilencingReagentType().equals(that.getSilencingReagentType()) &&
        getSequence().equals(that.getSequence());
    }

    @Override
    public int hashCode()
    {
      return
        getGene().hashCode() +
        17 * getSilencingReagentType().hashCode() +
        37 * getSequence().hashCode();
    }

    @Override
    public String toString()
    {
      return 
        getGene().toString() + ":" +
        getSilencingReagentType().toString() + ":" +
        getSequence();
    }
  }
  
  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }


  // package methods

  /**
   * Set the gene.
   * Throw a NullPointerException when the gene is null.
   *
   * @param gene the new gene
   * @throws NullPointerException when the gene is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setHbnGene(Gene gene)
  {
    if (gene == null) {
      throw new NullPointerException();
    }
    _gene = gene;
  }

  /**
   * Get the wells.
   *
   * @return the wells
   * @hibernate.set
   *   inverse="true"
   *   table="well_silencing_reagent_link"
   *   cascade="all"
   * @hibernate.collection-key
   *   column="silencing_reagent_id"
   * @hibernate.collection-many-to-many
   *   column="well_id"
   *   class="edu.harvard.med.screensaver.model.libraries.Well"
   *   foreign-key="fk_well_silencing_reagent_link_to_silencing_reagent"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  Set<Well> getHbnWells()
  {
    return _wells;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>SilencingReagent</code> object.
   *
   * @motivation for hibernate
   */
  private SilencingReagent() {}


  // private methods

  /**
   * Set the id for the silencing reagent.
   *
   * @param silencingReagentId the new id for the silencing reagent
   * @motivation for hibernate
   */
  private void setSilencingReagentId(Integer silencingReagentId) {
    _silencingReagentId = silencingReagentId;
  }

  /**
   * Get the version for the silencing reagent.
   *
   * @return the version for the silencing reagent
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the silencing reagent.
   *
   * @param version the new version for the silencing reagent
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the gene.
   *
   * @return the gene
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.libraries.Gene"
   *   column="gene_id"
   *   not-null="true"
   *   foreign-key="fk_silencing_reagent_to_gene"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private Gene getHbnGene()
  {
    return _gene;
  }

  /**
   * Set the wells.
   *
   * @param wells the new wells
   * @motivation for hibernate
   */
  private void setHbnWells(Set<Well> wells)
  {
    _wells = wells;
  }

  /**
   * Get the silencing reagent type.
   *
   * @return the silencing reagent type
   * @hibernate.property
   *   column="silencing_reagent_type"
   *   type="edu.harvard.med.screensaver.model.libraries.SilencingReagentType$UserType"
   *   not-null="true"
   * @motivation for hibernate
   */
  private SilencingReagentType getHbnSilencingReagentType()
  {
    return _silencingReagentType;
  }

  /**
   * Set the silencing reagent type.
   *
   * @param silencingReagentType the new silencing reagent type
   * @motivation for hibernate
   */
  private void setHbnSilencingReagentType(SilencingReagentType silencingReagentType)
  {
    _silencingReagentType = silencingReagentType;
  }

  /**
   * Get the sequence.
   *
   * @return the sequence
   * @hibernate.property
   *   column="sequence"
   *   type="text"
   *   not-null="true"
   * @motivation for hibernate
   */
  private String getHbnSequence()
  {
    return _sequence;
  }

  /**
   * Set the sequence.
   *
   * @param sequence the new sequence
   * @motivation for hibernate
   */
  private void setHbnSequence(String sequence)
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
   * Temporarily remove this entity from the other side of relationships whose
   * other side is has-many.
   * 
   * @motivation for use when the business key changes: since these changes
   * will affect {@link #equals} and {@link #hashCode}, we need to do extra
   * maintenance work for any sets this entity is in.
   */
  private void temporarilyRemoveFromRelationships()
  {
    getHbnGene().getHbnSilencingReagents().remove(this);
    for (Well well : getHbnWells()) {
      well.getSilencingReagents().remove(this);
    }
  }
  
  /**
   * Reinstate this entity to the other side of relationships whose other
   * side is has-many. This is the "inverse" operation to {@link
   * #temporarilyRemoveFromRelationships}.
   * 
   * @motivation for use when the business key changes: since these changes
   * will affect {@link #equals} and {@link #hashCode}, we need to do extra
   * maintenance work for any sets this entity is in.
   */
  private void reinstateRelationships()
  {
    getHbnGene().getHbnSilencingReagents().add(this);
    for (Well well : getHbnWells()) {
      well.getSilencingReagents().add(this);
    }    
  }
}
