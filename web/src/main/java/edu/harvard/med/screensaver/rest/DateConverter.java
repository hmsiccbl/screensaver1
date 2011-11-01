package edu.harvard.med.screensaver.rest;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.joda.time.base.AbstractDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateConverter implements Converter
{
  //private static final DateTimeFormatter formatter = DateTimeFormat.mediumDate();

  @Override
  public boolean canConvert(Class type)
  {
    return AbstractDateTime.class.isAssignableFrom(type);
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    writer.setValue(source.toString());
    //writer.setValue(((AbstractDateTime) source).toString());
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
  {
    return null;
  }
}
