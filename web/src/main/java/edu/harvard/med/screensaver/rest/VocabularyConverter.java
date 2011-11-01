package edu.harvard.med.screensaver.rest;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.model.VocabularyTerm;

public class VocabularyConverter implements Converter
{
  @Override
  public boolean canConvert(Class type)
  {
    return VocabularyTerm.class.isAssignableFrom(type);
  }
  
  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    writer.setValue(((VocabularyTerm) source).getValue());
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
  {
    return null;
  }
}
