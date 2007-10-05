// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.derivatives;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;


/**
 * A Hibernate entity bean representing a derivative.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
public class Derivative extends AbstractEntity
{

  // static fields

  private static final Logger log = Logger.getLogger(Derivative.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _derivativeId;
  private Integer _version;
  private Set<DerivativeScreenResult> _derivativeScreenResults = new HashSet<DerivativeScreenResult>();
  private String _name;
  private Set<String> _synonyms = new HashSet<String>();
  private String _smiles;
  private String _sdfile;


  // public constructor

  /**
   * Construct an initialized <code>Derivative</code>.
   * @param name the name
   * @param smiles the SMILES string
   */
  public Derivative(String name, String smiles)
  {
    _name = name;
    _smiles = smiles;
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
    return getDerivativeId();
  }

  /**
   * Get the id for the derivative.
   * @return the id for the derivative
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="derivative_id_seq",
    strategy="sequence",
    parameters = { @org.hibernate.annotations.Parameter(name="sequence", value="derivative_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="derivative_id_seq")
  public Integer getDerivativeId()
  {
    return _derivativeId;
  }

  /**
   * Get the set of derivative screen results.
   * @return the derivative screen results
   */
  @OneToMany(
    mappedBy="derivative",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE
  })
  public Set<DerivativeScreenResult> getDerivativeScreenResults()
  {
    return _derivativeScreenResults;
  }

  /**
   * Create and return a new derivative screen result for the derivative.
   * @param activityLevel the activity level
   * @param activityType the activity type
   * @return the new derivative screen result
   */
  public DerivativeScreenResult createDerivativeScreenResult(String activityLevel, String activityType)
  {
    DerivativeScreenResult derivativeScreenResult = new DerivativeScreenResult(
      this,
      activityLevel,
      activityType);
    _derivativeScreenResults.add(derivativeScreenResult);
    return derivativeScreenResult;
  }

  /**
   * Get the name.
   * @return the name
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getName()
  {
    return _name;
  }

  /**
   * Set the name.
   * @param name the new name
   */
  public void setName(String name)
  {
    _name = name;
  }

  /**
   * Get the synonyms.
   * @return the synonyms
   */
  @org.hibernate.annotations.CollectionOfElements
  @Column(name="synonym", nullable=false)
  @JoinTable(
    name="derivativeSynonym",
    joinColumns=@JoinColumn(name="derivativeId")
  )
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_derivative_synonym_to_derivative")
  @OrderBy("synonym")
  public Set<String> getSynonyms()
  {
    return _synonyms;
  }

  /**
   * Add the synonym.
   * @param synonym the synonym to add
   * @return true iff the derivative did not already have the synonym
   */
  public boolean addSynonym(String synonym)
  {
    return _synonyms.add(synonym);
  }

  /**
   * Remove the synonym.
   * @param synonym the synonym to remove
   * @return true iff the derivative previously had the synonym
   */
  public boolean removeSynonym(String synonym)
  {
    return _synonyms.remove(synonym);
  }

  /**
   * Get the SMILES string.
   * @return the SMILES string
   */
  @Column(nullable=false, unique=true)
  @org.hibernate.annotations.Type(type="text")
  public String getSmiles()
  {
    return _smiles;
  }

  /**
   * Set the SMILES string.
   * @param smiles the new SMILES string
   */
  public void setSmiles(String smiles)
  {
    _smiles = smiles;
  }

  /**
   * Get the SD file.
   * @return the SD file
   */
  @org.hibernate.annotations.Type(type="text")
  public String getSdfile()
  {
    return _sdfile;
  }

  /**
   * Set the SD file.
   * @param sdfile the new SD file
   */
  public void setSdfile(String sdfile)
  {
    _sdfile = sdfile;
  }


  // private constructor and instance methods

  /**
   * Construct an uninitialized <code>Derivative</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected Derivative() {}

  /**
   * Set the id for the derivative.
   *
   * @param derivativeId the new id for the derivative
   * @motivation for hibernate
   */
  private void setDerivativeId(Integer derivativeId)
  {
    _derivativeId = derivativeId;
  }

  /**
   * Get the version for the derivative.
   * @return the version for the derivative
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the derivative.
   * @param version the new version for the derivative
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the derivative screen results.
   * @param derivativeScreenResults the new derivative screen results
   * @motivation for hibernate
   */
  private void setDerivativeScreenResults(Set<DerivativeScreenResult> derivativeScreenResults)
  {
    _derivativeScreenResults = derivativeScreenResults;
  }

  /**
   * Set the synonyms.
   * @param synonyms the new synonyms
   * @motivation for hibernate
   */
  private void setSynonyms(Set<String> synonyms)
  {
    _synonyms = synonyms;
  }
}
