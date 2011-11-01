
package edu.harvard.med.screensaver.rest;

import java.util.Collection;

import com.google.common.base.Function;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.commons.lang.StringUtils;

import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.util.DevelopmentException;

public class XStreamUtil
{
  private HierarchicalStreamWriter writer;
  private MarshallingContext context;
  private EntityUriGenerator<String> entityUriGenerator;
  
  public static Function<ScreensaverUser,Integer> SCREENSAVER_USER_TO_ID =
    new Function<ScreensaverUser,Integer>() {
      @Override
      public Integer apply(ScreensaverUser from)
      {
        return from.getEntityId();
      }
    };

  public XStreamUtil(HierarchicalStreamWriter writer,
                        MarshallingContext context,
                        EntityUriGenerator<String> entityUriGenerator)
  {
    this.writer = writer;
    this.context = context;
    this.entityUriGenerator = entityUriGenerator;
  }

  public void writeNode(Object value, String name)
  {
    this.writer.startNode(name);
    this.context.convertAnother(value==null? "" : value);
    this.writer.endNode();
  }  
  
  public void writeNodes(Collection collection, String collectionName, String name)
  {
    this.writer.startNode(collectionName);
    for(Object o:collection) 
    {
      writeNode(o,name);
    }
    this.writer.endNode();
  }
  
  public <E extends Entity<?>> void writeUri(E entity)
  {
    writeUri(entity, "uri");
  }
  
  public <E extends Entity<?>> void writeUri(E entity, String nodeName)
  {
    if(entity == null) return;
    this.writer.startNode(nodeName);
    writeUriAttribute(entity);
    this.writer.endNode();
  }

  public <E extends Entity<?>> void writeUriAttribute(E e)
  {
    String uri = e.acceptVisitor(this.entityUriGenerator);
    if (StringUtils.isEmpty(uri)) {
      throw new DevelopmentException("URI generator not implemented");
    }
    // note: In JSON output, attribute looks like "@href"
    writer.addAttribute("href", e.acceptVisitor(entityUriGenerator));
  }
}
