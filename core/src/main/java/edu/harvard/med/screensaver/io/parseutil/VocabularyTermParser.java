package edu.harvard.med.screensaver.io.parseutil;


public class VocabularyTermParser<T extends Enum<T>>
{
  protected T[] _values;
  
  public VocabularyTermParser(Class<T> enumClass)
  {
    _values = enumClass.getEnumConstants();
  }

  public T forValue(String value)
  {
    if(value == null ) throw new IllegalArgumentException("input value may not be null");
    for (T type : _values) {
      if (value.toUpperCase().equals(type.toString().toUpperCase())) {
        return type;
      }
    }
    return null;
  }
}
