package edu.harvard.med.screensaver.rest;

import org.apache.log4j.Logger;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ErrorConverter extends RestConverter
{
  private static final Logger log = Logger.getLogger(ErrorConverter.class);

  public static class ErrorContainer
  {
    
    public Exception e;
    public String message;
    
    public ErrorContainer(Exception e)
    {
      this.e = e;
    }
    
    public ErrorContainer(String message)
    {
      this.message = message;
    }
    
    public ErrorContainer(Exception e, String message)
    {
      this.e = e;
      this.message = message;
    }
  }
  
  @Override
  public boolean canConvert(Class type)
  {
    return ErrorContainer.class.isAssignableFrom(type);
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    final XStreamUtil util = new XStreamUtil(writer, context, getEntityUriGenerator());
    Exception e = ((ErrorContainer)source).e;
    String errMsg = ((ErrorContainer)source).message;

    if(e != null)
    {
      log.warn("Exception in controller", e);
      util.writeNode(e.getClass(), "exception");
      util.writeNode(e.getMessage(), "exceptionMessage");
    }
    if(errMsg != null)
    {
      log.error("Error in controller: " + errMsg);
      util.writeNode(errMsg, "errorMessage");
    }
  }
}
