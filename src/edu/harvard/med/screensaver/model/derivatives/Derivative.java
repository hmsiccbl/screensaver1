// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.derivatives;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a derivative.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
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
   * Constructs an initialized <code>Derivative</code> object.
   *
   * @param name the name
   * @param smiles the SMILES string
   */
  public Derivative(
    String name,
    String smiles)
  {
    _name = name;
    _smiles = smiles;
  }


  // public methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }
  
  @Override
  public Integer getEntityId()
  {
    return getDerivativeId();
  }

  /**
   * Get the id for the derivative.
   *
   * @return the id for the derivative
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="derivative_id_seq"
   */
  public Integer getDerivativeId()
  {
    return _derivativeId;
  }

  /**
   * Get an unmodifiable copy of the set of derivative screen results.
   *
   * @return the derivative screen results
   */
  public Set<DerivativeScreenResult> getDerivativeScreenResults()
  {
    return Collections.unmodifiableSet(_derivativeScreenResults);
  }

  /**
   * Add the derivative screen result.
   *
   * @param derivativeScreenResult the derivative screen result to add
   * @return true iff the derivative did not already have the derivative screen result
   */
  public boolean addDerivativeScreenResult(DerivativeScreenResult derivativeScreenResult)
  {
    if (getHbnDerivativeScreenResults().add(derivativeScreenResult)) {
      derivativeScreenResult.setHbnDerivative(this);
      return true;
    }
    return false;
  }

  /**
   * Get the name.
   *
   * @return the name
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getName()
  {
    return _name;
  }

  /**
   * Set the name.
   *
   * @param name the new name
   */
  public void setName(String name)
  {
    _name = name;
  }

  /**
   * Get the synonyms.
   *
   * @return the synonyms
   * @hibernate.set
   *   order-by="synonym"
   *   table="derivative_synonym"
   *   cascade="delete"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="derivative_id"
   *   foreign-key="fk_derivative_synonym_to_derivative"
   * @hibernate.collection-element
   *   type="text"
   *   column="synonym"
   *   not-null="true"
   */
  public Set<String> getSynonyms()
  {
    return _synonyms;
  }

  /**
   * Add the synonym.
   *
   * @param synonym the synonym to add
   * @return true iff the derivative did not already have the synonym
   */
  public boolean addSynonym(String synonym)
  {
    return _synonyms.add(synonym);
  }

  /**
   * Remove the synonym.
   *
   * @param synonym the synonym to remove
   * @return true iff the derivative previously had the synonym
   */
  public boolean removeSynonym(String synonym)
  {
    return _synonyms.remove(synonym);
  }

  /**
   * Get the SMILES string.
   *
   * @return the SMILES string
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   *   unique="true"
   */
  public String getSmiles()
  {
    return _smiles;
  }

  /**
   * Set the SMILES string.
   *
   * @param smiles the new SMILES string
   */
  public void setSmiles(String smiles)
  {
    _smiles = smiles;
  }

  /**
   * Get the SD file.
   *
   * @return the SD file
   * @hibernate.property
   *   type="text"
   */
  public String getSdfile()
  {
    return _sdfile;
  }

  /**
   * Set the SD file.
   *
   * @param sdfile the new SD file
   */
  public void setSdfile(String sdfile)
  {
    _sdfile = sdfile;
  }


  // protected methods

  @Override
  protected Object getBusinessKey()
  {
    return getSmiles();
  }


  // package methods

  /**
   * Get the derivative screen results.
   *
   * @return the derivative screen results
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="derivative_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.derivatives.DerivativeScreenResult"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  Set<DerivativeScreenResult> getHbnDerivativeScreenResults()
  {
    return _derivativeScreenResults;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>Derivative</code> object.
   *
   * @motivation for hibernate
   */
  private Derivative() {}


  // private methods

  /**
   * Set the id for the derivative.
   *
   * @param derivativeId the new id for the derivative
   * @motivation for hibernate
   */
  private void setDerivativeId(Integer derivativeId) {
    _derivativeId = derivativeId;
  }

  /**
   * Get the version for the derivative.
   *
   * @return the version for the derivative
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the derivative.
   *
   * @param version the new version for the derivative
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Set the derivative screen results.
   *
   * @param derivativeScreenResults the new derivative screen results
   * @motivation for hibernate
   */
  private void setHbnDerivativeScreenResults(Set<DerivativeScreenResult> derivativeScreenResults)
  {
    _derivativeScreenResults = derivativeScreenResults;
  }

  /**
   * Set the synonyms.
   *
   * @param synonyms the new synonyms
   * @motivation for hibernate
   */
  private void setSynonyms(Set<String> synonyms)
  {
    _synonyms = synonyms;
  }
}
