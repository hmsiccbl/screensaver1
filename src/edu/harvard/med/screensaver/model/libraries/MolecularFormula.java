// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import javax.persistence.Embeddable;

@Embeddable
public class MolecularFormula
{
  private String _molecularFormula;

  public MolecularFormula() {}

  public MolecularFormula(String molecularFormula)
  {
    _molecularFormula = molecularFormula;
  }

  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.db.usertypes.MolecularFormulaType")
  public String getMolecularFormula()
  {
    return _molecularFormula;
  }

  public void setMolecularFormula(String molecularFormula)
  {
    _molecularFormula = molecularFormula;
  }
  
  public String toHtml()
  {
    // TODO
    return "<div>" + _molecularFormula + "</div>";
  }
  
  public String toString()
  {
    return _molecularFormula;
  }
  
  @Override
  public boolean equals(Object other)
  {
    if (this == other) {
      return true;
    }
    if (other instanceof MolecularFormula) {
      return this._molecularFormula.equals(((MolecularFormula) other)._molecularFormula);
    }
    return false; 
  }
  
  @Override
  public int hashCode()
  {
    return _molecularFormula.hashCode();
  }
}
