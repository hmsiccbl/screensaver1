/**
 * 
 */
package edu.harvard.med.screensaver.beans;

/**
 * @author s
 *
 */
public class Compound {

	private Long id;
	
	private String smiles;
	
	/**
	 * @return Returns the id.
	 */
	public Long getId() {
		return id;
	}
	
	/**
	 * @return Returns the smiles.
	 */
	public String getSmiles() {
		return smiles;
	}
	
	/**
	 * @param smiles The smiles to set.
	 */
	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}
}
