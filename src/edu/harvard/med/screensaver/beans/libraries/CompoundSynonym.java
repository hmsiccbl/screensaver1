// CompoundSynonym.java
// by john sullivan 2006.05

package edu.harvard.med.screensaver.beans.libraries;


/**
 * @author john sullivan
 * @hibernate.class
 *   table="compound_synonym"
 */
public class CompoundSynonym {

  
  // instance fields
  
  private Integer  _synonymId;
  private String   _synonym;
  
  
  // getters and setters
  
  /**
   * @return Returns the synonymId.
   * @hibernate.id
   *   generator-class="native"
   *   column="synonym_id"
   */
  public Integer getSynonymId() {
    return _synonymId;
  }

  /**
   * @return Returns the synonym.
   * @hibernate.property
   *   type="text"
   */
  public String getSynonym() {
    return _synonym;
  }
  
  /**
   * @param synonym The synonym to set.
   */
  public void setSynonym(String synonym) {
    _synonym = synonym;
  }
}
