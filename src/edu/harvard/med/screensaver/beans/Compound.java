/**
 * 
 */
package edu.harvard.med.screensaver.beans;

/**
 * @author s
 * @hibernate.class
 *   table="COMPOUND"
 */
public class Compound {

	private Long _compoundId;
	
	private String _smiles;
	
	/**
	 * @return Returns the id.
	 * @hibernate.id
	 *   generator-class="native"
	 *   column="compound_id"
	 */
	public Long getCompoundId() {
		return _compoundId;
	}
	
	/**
	 * @return Returns the smiles.
	 * @hibernate.property
	 *   column="smiles"
	 */
	public String getSmiles() {
		return _smiles;
	}
	
	/**
	 * @param smiles The smiles to set.
	 */
	public void setSmiles(String smiles) {
		_smiles = smiles;
	}
}
