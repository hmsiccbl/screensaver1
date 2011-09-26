// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.annotations.Derived;

/**
 * @motivation Stores min/max/primary, mg_ml/molar concentration values
 */
@Embeddable
public class ConcentrationStatistics implements Serializable
{
  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(ConcentrationStatistics.class);

  public static final ConcentrationStatistics NULL = new ConcentrationStatistics();
  
  private MolarConcentration _minMolarConcentration;
  private MolarConcentration _maxMolarConcentration;
  private BigDecimal _minMgMlConcentration;
  private BigDecimal _maxMgMlConcentration;
  private BigDecimal _primaryWellMgMlConcentration;
  private MolarConcentration _primaryWellMolarConcentration;
//  private BigDecimal _wellConcentrationDilutionFactor = new BigDecimal("1.00"); 

  public ConcentrationStatistics() 
  {
  }
    
  public ConcentrationStatistics(MolarConcentration min1, MolarConcentration max1, MolarConcentration prim1, BigDecimal min2, BigDecimal max2, BigDecimal prim2)
  {
    _minMolarConcentration = min1;
    _maxMolarConcentration = max1; 
    _primaryWellMolarConcentration = prim1;
    _minMgMlConcentration = min2;
    _maxMgMlConcentration = max2;
    _primaryWellMgMlConcentration = prim2;
  }

  public void setMinMolarConcentration(MolarConcentration value)
  {
    _minMolarConcentration = value;
  }

  @Column(precision = ScreensaverConstants.MOLAR_CONCENTRATION_PRECISION, scale = ScreensaverConstants.MOLAR_CONCENTRATION_SCALE)
  @org.hibernate.annotations.Type(type = "edu.harvard.med.screensaver.db.usertypes.MolarConcentrationType")
  public MolarConcentration getMinMolarConcentration()
  {
    return _minMolarConcentration;
  }

  public void setMaxMolarConcentration(MolarConcentration value)
  {
    _maxMolarConcentration = value;
  }

  @Column(precision = ScreensaverConstants.MOLAR_CONCENTRATION_PRECISION, scale = ScreensaverConstants.MOLAR_CONCENTRATION_SCALE)
  @org.hibernate.annotations.Type(type = "edu.harvard.med.screensaver.db.usertypes.MolarConcentrationType")
  public MolarConcentration getMaxMolarConcentration()
  {
    return _maxMolarConcentration;
  }

  public void setMinMgMlConcentration(BigDecimal value)
  {
    _minMgMlConcentration = value;
  }

  @Column(precision = ScreensaverConstants.MG_ML_CONCENTRATION_PRECISION, scale = ScreensaverConstants.MG_ML_CONCENTRATION_SCALE)
  public BigDecimal getMinMgMlConcentration()
  {
    return _minMgMlConcentration;
  }

  public void setMaxMgMlConcentration(BigDecimal value)
  {
    _maxMgMlConcentration = value;
  }

  @Column(precision = ScreensaverConstants.MG_ML_CONCENTRATION_PRECISION, scale = ScreensaverConstants.MG_ML_CONCENTRATION_SCALE)
  public BigDecimal getMaxMgMlConcentration()
  {
    return _maxMgMlConcentration;
  }
  
  @Column(precision = ScreensaverConstants.MG_ML_CONCENTRATION_PRECISION, scale = ScreensaverConstants.MG_ML_CONCENTRATION_SCALE)
  @Derived
  public BigDecimal getPrimaryWellMgMlConcentration()
  {
    return _primaryWellMgMlConcentration;
  }

  public void setPrimaryWellMgMlConcentration(BigDecimal value)
  {
    _primaryWellMgMlConcentration = value;
  }
  
  @Column(precision = ScreensaverConstants.MOLAR_CONCENTRATION_PRECISION, scale = ScreensaverConstants.MOLAR_CONCENTRATION_SCALE)
  @org.hibernate.annotations.Type(type = "edu.harvard.med.screensaver.db.usertypes.MolarConcentrationType")
  @Derived
  public MolarConcentration getPrimaryWellMolarConcentration()
  {
    return _primaryWellMolarConcentration;
  }

  public void setPrimaryWellMolarConcentration(MolarConcentration value)
  {
    _primaryWellMolarConcentration = value;
  }

//  public void setWellConcentrationDilutionFactor(BigDecimal _plateDilutionFactor)
//  {
//    this._wellConcentrationDilutionFactor = _plateDilutionFactor;
//  }
//
//  @Column(precision = ScreensaverConstants.PLATE_DILUTION_FACTOR_PRECISION, scale = ScreensaverConstants.PLATE_DILUTION_FACTOR_SCALE )
//  public BigDecimal getWellConcentrationDilutionFactor()
//  {
//    if(_wellConcentrationDilutionFactor == null ) return BigDecimal.ONE; // Todo: this should not happen
//    return _wellConcentrationDilutionFactor;
//  }  
  
  public MolarConcentration getDilutedPrimaryWellMolarConcentration(BigDecimal df)
  {
    if(getPrimaryWellMolarConcentration() == null ) return null;
    // NOTE: a null value for the well concentration dilution factor should be considered a db error
    BigDecimal value = getPrimaryWellMolarConcentration().getValue().divide(df,ScreensaverConstants.PLATE_DILUTION_FACTOR_SCALE, RoundingMode.HALF_UP);
    return MolarConcentration.makeConcentration(value.toString(), getPrimaryWellMolarConcentration().getUnits()); 
  }  
  
  @Transient
  public MolarConcentration getDilutedMinMolarConcentration(BigDecimal df)
  {
    if(getMinMolarConcentration() == null ) return null; 
    BigDecimal value = getMinMolarConcentration().getValue().divide(df,ScreensaverConstants.PLATE_DILUTION_FACTOR_SCALE, RoundingMode.HALF_UP);
    return MolarConcentration.makeConcentration(value.toString(), getMinMolarConcentration().getUnits()); 
  }  
  
  @Transient
  public MolarConcentration getDilutedMaxMolarConcentration(BigDecimal df)
  {
    if(getMaxMolarConcentration() == null ) return null;
    BigDecimal value = getMaxMolarConcentration().getValue().divide(df,ScreensaverConstants.PLATE_DILUTION_FACTOR_SCALE, RoundingMode.HALF_UP);
    return MolarConcentration.makeConcentration(value.toString(), getMaxMolarConcentration().getUnits()); 
  }

  @Transient
  public BigDecimal getDilutedMinMgMlConcentration(BigDecimal df)
  {
    if(getMinMgMlConcentration()== null) return null;
    BigDecimal value = getMinMgMlConcentration().divide(df,ScreensaverConstants.PLATE_DILUTION_FACTOR_SCALE, RoundingMode.HALF_UP);
    return value;
  } 
  
  @Transient
  public BigDecimal getDilutedMaxMgMlConcentration(BigDecimal df)
  {
    if(getMaxMgMlConcentration()== null) return null;
    BigDecimal value = getMaxMgMlConcentration().divide(df,ScreensaverConstants.PLATE_DILUTION_FACTOR_SCALE, RoundingMode.HALF_UP);
    return value;
  } 
  
  @Transient
  public BigDecimal getDilutedPrimaryWellMgMlConcentration(BigDecimal df)
  {
    if(getPrimaryWellMgMlConcentration()== null) return null;
    BigDecimal value = getPrimaryWellMgMlConcentration().divide(df,ScreensaverConstants.PLATE_DILUTION_FACTOR_SCALE, RoundingMode.HALF_UP);
    return value;
  }  
  
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    Object v = getMinMgMlConcentration();
    result = prime * result + ((v == null) ? 0 : v.hashCode());
    v = getMaxMgMlConcentration();
    result = prime * result + ((v == null) ? 0 : v.hashCode());
    v = getMinMolarConcentration();
    result = prime * result + ((v == null) ? 0 : v.hashCode());
    v = getMaxMolarConcentration();
    result = prime * result + ((v == null) ? 0 : v.hashCode());
    v = getPrimaryWellMgMlConcentration();
    result = prime * result + ((v == null) ? 0 : v.hashCode());
    v = getPrimaryWellMolarConcentration();
    result = prime * result + ((v == null) ? 0 : v.hashCode());    
    //    v = getWellConcentrationDilutionFactor();
    result = prime * result + ((v == null) ? 0 : v.hashCode());
    
    return result;
  }
  
  @Override
  public boolean equals(Object o)
  {
    if(this==o) return true;
    if(o==null) return false;
    if(this.getClass() != o.getClass()) return false;
    ConcentrationStatistics cs = (ConcentrationStatistics)o;

    Object v = getMinMgMlConcentration();
    Object v2 = cs.getMinMgMlConcentration();
    if(v!=null && !v.equals(v2)) return false;
    v = getMaxMgMlConcentration();
    v2 = cs.getMaxMgMlConcentration();
    if(v!=null && !v.equals(v2)) return false;
    v = getPrimaryWellMgMlConcentration();
    v2 = cs.getPrimaryWellMgMlConcentration();
    if(v!=null && !v.equals(v2)) return false;
    
    v = getMaxMolarConcentration();
    v2 = cs.getMaxMolarConcentration();
    if(v!=null && !v.equals(v2)) return false;
    v = getMinMolarConcentration();
    v2 = cs.getMinMolarConcentration();
    if(v!=null && !v.equals(v2)) return false;
    v = getPrimaryWellMolarConcentration();
    v2 = cs.getPrimaryWellMolarConcentration();
    if(v!=null && !v.equals(v2)) return false;

//    v = getWellConcentrationDilutionFactor();
//    v2 = cs.getWellConcentrationDilutionFactor();
//    if(v!=null && !v.equals(v2)) return false;

    return true;
  }
  
  @Override
  public String toString()
  {
    Object v1 = getMinMgMlConcentration();
    Object v2 = getMaxMgMlConcentration();
    Object v3 = getPrimaryWellMgMlConcentration();
//    Object v4 = getWellConcentrationDilutionFactor();
    Object v1a = getMinMolarConcentration();
    Object v2a = getMaxMolarConcentration();
    Object v3a = getPrimaryWellMolarConcentration();
    
    String s = "[min=" + v1 + ", max=" + v2 + ", primary=" + v3  + "]";
    s += "[min=" + v1a + ", max=" + v2a + ", primary=" + v3a +  "]";
    
    return s;
  }
}

