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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Immutable;
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

  public static final RelationshipPath<SilencingReagent> vendorGene = RelationshipPath.from(SilencingReagent.class).to("vendorGene", Cardinality.TO_ONE);
  public static final RelationshipPath<SilencingReagent> facilityGene = RelationshipPath.from(SilencingReagent.class).to("facilityGene", Cardinality.TO_ONE);
  public static final RelationshipPath<SilencingReagent> duplexWells = RelationshipPath.from(SilencingReagent.class).to("duplexWells");
  
  private static final Function<Well,SilencingReagent> wellToReagentTransformer = 
    new Function<Well, SilencingReagent>() { public SilencingReagent apply(Well well) { return well.<SilencingReagent>getLatestReleasedReagent(); } };
    
  private SilencingReagentType _silencingReagentType;
  private String _sequence;
  private Gene _vendorGene;
  private Gene _facilityGene;
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

  @OneToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch=FetchType.LAZY)
  @JoinColumn(name="vendorGeneId", nullable=true, unique=true)
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE})
  @ToOne(hasNonconventionalSetterMethod=true) /* lazy-created in getter */
  public Gene getVendorGene()
  {
    // lazy instantiate the Gene iff SilencingReagent has not yet been persisted
    // (since SilencingReagent and Gene are immutable)
    if (_vendorGene == null && getReagentId() == null) {
      _vendorGene = new Gene();
    }
    return _vendorGene;
  }

  private void setVendorGene(Gene vendorGene)
  {
    _vendorGene = vendorGene;
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
  @OneToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch=FetchType.LAZY)
  @JoinColumn(name="facilityGeneId", nullable=true, unique=true)
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE})
  @ToOne(hasNonconventionalSetterMethod=true) /* lazy-created in getter */
  public Gene getFacilityGene()
  {
    // lazy instantiate the Gene iff SilencingReagent has not yet been persisted
    // (since SilencingReagent and Gene are immutable)
    if (_facilityGene == null && getReagentId() == null) {
      _facilityGene = new Gene();
    }
    return _facilityGene;
  }

  private void setFacilityGene(Gene facilityGene)
  {
   _facilityGene = facilityGene;
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
