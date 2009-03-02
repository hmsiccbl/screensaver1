// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
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

  // the vocabulary
  
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
  EPPENDORF("Eppendorf", PlateSize.WELLS_384, "CB", "PP"),
  GENETIX("Genetix", PlateSize.WELLS_384, "CB", "PP"),
  MARSH("Marsh", PlateSize.WELLS_384, "VB", "PP"),
  NUNC("Nunc", PlateSize.WELLS_96, "VB", "PS"),
  ;
  
  // TODO: consider enums for auxiliary attributes

 
  // static inner class

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


  // private instance field and constructor

  private String _value;
  private PlateSize _plateSize;
  private String _wellBottomShape;
  private String _material;

  /**
   * Constructs a <code>PlateType</code> vocabulary term.
   * @param value The value of the term.
   */
  private PlateType(String value, PlateSize plateSize, String wellBottomShape, String material)
  {
    _value = value;
    _plateSize = plateSize;
    _wellBottomShape = wellBottomShape;
    _material = material;
  }


  // public instance methods

  /**
   * Get the value of the vocabulary term.
   * @return the value of the vocabulary term
   */
  public String getValue()
  {
    return _value;
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return getValue();
  }

  // Note: we cannot return this in toString(), as it prevents String->Enum reverse lookup
  public String getFullName()
  {
    return new StringBuilder().append(_value)
    .append(' ')
    .append(getPlateSize().getWellCount())
    .append(' ')
    .append(_wellBottomShape)
    .append(' ')
    .append(_material)
    .toString();
  }
}
