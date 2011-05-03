// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The plate type vocabulary.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum PlateType implements VocabularyTerm
{
  // TODO: consider enums for the below secondary attributes
  // Legend:
  //
  //  RB - Round Bottom
  //  CB - Conical Bottom
  //  VB - V-shaped Bottom
  //
  //  PP - Polypropylene
  //  PS - Polystyrene

  /*
   * [2007-03-15] David Fletcher writes, "At one point for a very short while we
   * were using ABgene conical bottom plates. Unfortunately when the plate type
   * was changed to V bottomed plates, the old name carried over. Therefore
   * ABgene 384 CB PP is the name we use in the software for our current
   * V-bottomed plates."
   */
  ABGENE("ABgene", PlateSize.WELLS_384, "CB", "PP"), 
  COSTAR("Costar", PlateSize.WELLS_96, "RB", "PS"),
  EPPENDORF_384("Eppendorf", PlateSize.WELLS_384, "CB", "PP"),
  EPPENDORF_96("Eppendorf", PlateSize.WELLS_96, "CB", "PP"),
  GENETIX("Genetix", PlateSize.WELLS_384, "CB", "PP"),
  MARSH("Marsh", PlateSize.WELLS_384, "VB", "PP"),
  NUNC("Nunc", PlateSize.WELLS_96, "VB", "PS"),
  ;
  
 
  /**
   * A Hibernate <code>UserType</code> to map the {@link PlateType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<PlateType>
  {
    public UserType()
    {
      super(PlateType.values());
    }
  }

  private String _value;
  private String _brand;
  private PlateSize _plateSize;
  private String _wellBottomShape;
  private String _material;

  /**
   * Constructs a <code>PlateType</code> vocabulary term.
   * 
   * @param brand The value of the term.
   */
  private PlateType(String brand, PlateSize plateSize, String wellBottomShape, String material)
  {
    _brand = brand;
    _plateSize = plateSize;
    _wellBottomShape = wellBottomShape;
    _material = material;
    _value = new StringBuilder().append(_brand)
      .append(' ')
      .append(getPlateSize().getWellCount())
      .append(' ')
      .append(_wellBottomShape)
      .append(' ')
      .append(_material)
      .toString();
  }

  /**
   * Get the value of the vocabulary term.
   * @return the value of the vocabulary term
   */
  public String getValue()
  {
    return _value;
  }

  public String getBrand()
  {
    return _brand;
  }

  public String getMaterial()
  {
    return _material;
  }

  public PlateSize getPlateSize()
  {
    return _plateSize;
  }

  public String getWellBottomShape()
  {
    return _wellBottomShape;
  }

  @Override
  public String toString()
  {
    return getValue();
  }
}
