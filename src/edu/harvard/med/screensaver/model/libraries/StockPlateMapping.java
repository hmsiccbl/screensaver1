// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class StockPlateMapping
{
  private Integer _plateNumber;
  private Quadrant _quadrant;

  public StockPlateMapping()
  {}

  public StockPlateMapping(Integer plateNumber, Quadrant quadrant)
  {
    _plateNumber = plateNumber;
    _quadrant = quadrant;
  }

  @Column(name = "stock_plate_number")
  public Integer getPlateNumber()
  {
    return _plateNumber;
  }

  public void setPlateNumber(Integer plateNumber)
  {
    _plateNumber = plateNumber;
  }

  public Quadrant getQuadrant()
  {
    return _quadrant;
  }

  public void setQuadrant(Quadrant quadrant)
  {
    _quadrant = quadrant;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_plateNumber == null) ? 0 : _plateNumber.hashCode());
    result = prime * result + ((_quadrant == null) ? 0 : _quadrant.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    StockPlateMapping other = (StockPlateMapping) obj;
    if (_plateNumber == null) {
      if (other._plateNumber != null) return false;
    }
    else if (!_plateNumber.equals(other._plateNumber)) return false;
    if (_quadrant != other._quadrant) return false;
    return true;
  }

  @Override
  public String toString()
  {
    return "StockPlateMapping [plateNumber=" + _plateNumber + ", quadrant=" + _quadrant + "]";
  }
}
