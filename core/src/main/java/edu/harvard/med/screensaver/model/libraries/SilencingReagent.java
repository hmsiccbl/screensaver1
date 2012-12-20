// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.annotations.ToOne;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;


/**
 * Silencing reagent of an RNAi library.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@Immutable
@ContainedEntity(containingEntityClass=Well.class)
public class SilencingReagent extends Reagent
{
  private static final long serialVersionUID = 0L;

  public static final RelationshipPath<SilencingReagent> vendorGenes = RelationshipPath.from(SilencingReagent.class).to("vendorGenes", Cardinality.TO_MANY);
  public static final RelationshipPath<SilencingReagent> facilityGenes = RelationshipPath.from(SilencingReagent.class).to("facilityGenes", Cardinality.TO_MANY);
  public static final RelationshipPath<SilencingReagent> duplexWells = RelationshipPath.from(SilencingReagent.class).to("duplexWells");
  
  private static final Function<Well,SilencingReagent> wellToReagentTransformer = 
    new Function<Well, SilencingReagent>() { public SilencingReagent apply(Well well) { return well.<SilencingReagent>getLatestReleasedReagent(); } };
    
  private SilencingReagentType _silencingReagentType;
  private String _sequence;
  private List<Gene> _vendorGenes;
  private List<Gene> _facilityGenes;
  private Set<Well> _duplexWells = Sets.newHashSet();
  private boolean _isRestrictedSequence;

  /**
   * Construct an uninitialized <code>SilencingReagent</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected SilencingReagent() {}

  /**
   * Construct a <code>SilencingReagent</code>.
   *
   * @motivation for use of {@link Well#createSilencingReagent} methods only
   */
  SilencingReagent(ReagentVendorIdentifier rvi,
                   Well well,
                   LibraryContentsVersion libraryContentsVersion,
                   SilencingReagentType silencingReagentType,
                   String sequence)
  {
    super(rvi, well, libraryContentsVersion);
    _silencingReagentType = silencingReagentType;
    _sequence = sequence;
    _vendorGenes = new ArrayList<Gene>();
    _facilityGenes = new ArrayList<Gene>();
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Column(nullable=true)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.libraries.SilencingReagentType$UserType")
  public SilencingReagentType getSilencingReagentType()
  {
    return _silencingReagentType;
  }

  private void setSilencingReagentType(SilencingReagentType silencingReagentType)
  {
    _silencingReagentType = silencingReagentType;
  }

  /**
   * The genetic sequence of the silencing reagent. For pool wells, this may be
   * null (or empty), or can be a delimited list of the sequences of the
   * constituent duplexes. If left null/empty, it is still possible to find the
   * duplex sequences via {@link #getDuplexWells}.{@link Well#getLatestReleasedReagent}.
   */
  @Column(nullable=true)
  @org.hibernate.annotations.Type(type="text")
  public String getSequence()
  {
    return _sequence;
  }

  private void setSequence(String sequence)
  {
    _sequence = sequence;
  }

  @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.LAZY)
  @JoinTable(name = "reagentVendorGenes", joinColumns = @JoinColumn(name = "reagentId"), inverseJoinColumns = @JoinColumn(name = "geneId"))
  @IndexColumn(name = "ordinal")
  @org.hibernate.annotations.Cascade(value = { org.hibernate.annotations.CascadeType.SAVE_UPDATE,
                  org.hibernate.annotations.CascadeType.DELETE })
  @org.hibernate.annotations.ForeignKey(name = "fk_vendor_genes_to_reagent")
  @ToMany(hasNonconventionalMutation = true)
  public List<Gene> getVendorGenes()
  {
    return _vendorGenes;
  }

  private void setVendorGenes(List<Gene> vendorGenes)
  {
    _vendorGenes = vendorGenes;
  }

  @Transient
  public Gene getVendorGene() {
	  if(_vendorGenes == null) {
		  _vendorGenes = new ArrayList<Gene>();
	  }
	  
	  if(_vendorGenes.size() == 0 && getEntityId() == null) {
		  _vendorGenes.add(new Gene());
	  }

	  return _vendorGenes.size() == 0 ? null : _vendorGenes.get(0);
  }
  
  /**
   * Optional gene information provided by the screening facility, which can
   * differ from the vendor-provided gene information ({@link #getVendorGene()}).
   * In particular if updated gene information is available (names, symbols,
   * etc.), this can be reflected here, without affecting the original gene
   * information provided by the vendor. Also, if it is determined that this
   * silencing reagent in fact targets a different gene than expected, the
   * facility gene information can be used reflect this fact.
   */
  @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.LAZY)
  @JoinTable(name = "reagentFacilityGenes", joinColumns = @JoinColumn(name = "reagentId"), inverseJoinColumns = @JoinColumn(name = "geneId"))
  @IndexColumn(name = "ordinal")
  @org.hibernate.annotations.Cascade(value = { org.hibernate.annotations.CascadeType.SAVE_UPDATE,
                  org.hibernate.annotations.CascadeType.DELETE })
  @org.hibernate.annotations.ForeignKey(name = "fk_facility_genes_to_reagent")
  @ToMany(hasNonconventionalMutation = true)
  public List<Gene> getFacilityGenes()
  {
    return _facilityGenes;
  }

  private void setFacilityGenes(List<Gene> facilityGenes)
  {
   _facilityGenes = facilityGenes;
  }

  @Transient
  public Gene getFacilityGene() {
	  if(_facilityGenes == null) {
		  _facilityGenes = new ArrayList<Gene>();
	  }
	  
	  if(_facilityGenes.size() == 0 && getEntityId() == null) {
		  _facilityGenes.add(new Gene());
	  }

	  return _facilityGenes.size() == 0 ? null : _facilityGenes.get(0);
  }
  
  @ManyToMany(cascade={}, fetch=FetchType.LAZY)
  @JoinTable(
    joinColumns=@JoinColumn(name="silencing_reagent_id"),
    inverseJoinColumns=@JoinColumn(name="wellId")
  )
  @Cascade({})
  @LazyCollection(LazyCollectionOption.TRUE)
  @ToMany(unidirectional=true, hasNonconventionalMutation=true)
  public Set<Well> getDuplexWells()
  {
    return _duplexWells;
  }
  
  private void setDuplexWells(Set<Well> duplexWells)
  {
    _duplexWells = duplexWells;
  }

  /**
   * Builder method when creating a new SilencingReagent, prior to being
   * persisted. Note: it is up to the client code to validate that the duplex
   * well targets the same gene as the pool (e.g., entrezgene IDs match, or
   * whatever "similarity" criteria is deemed appropriate for determining that
   * targeted gene is the same); the model allows for real-world errors, where a
   * pool well is erroneously contains silencing reagents that target different
   * genes.
   */
  public SilencingReagent withDuplexWell(Well duplexWell)
  {
    validateImmutablePropertyInitialization();
    _duplexWells.add(duplexWell);
    return this;
  }

  @Transient
  public Set<SilencingReagent> getDuplexSilencingReagents()
  {
    Iterable<SilencingReagent> reagents = Iterables.transform(getDuplexWells(), wellToReagentTransformer);
    reagents = Iterables.filter(reagents, Predicates.notNull());
    return ImmutableSet.copyOf(reagents);
  }

  @Column(name = "is_restricted_sequence", nullable = false)
  public boolean isRestrictedSequence()
  {
    return _isRestrictedSequence;
  }

  private void setRestrictedSequence(boolean isRestrictedSequence)
  {
    _isRestrictedSequence = isRestrictedSequence;
  }

  public SilencingReagent withRestrictedSequence(boolean isRestrictedSequence)
  {
    validateImmutablePropertyInitialization();
    setRestrictedSequence(isRestrictedSequence);
    return this;
  }
}
