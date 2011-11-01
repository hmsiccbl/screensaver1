package edu.harvard.med.screensaver.rest;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AttachedFile;

public abstract class RestConverter implements Converter
{
  @Autowired
  private GenericEntityDAO genericEntityDao;

  @Autowired
  private EntityUriGenerator<String> entityUriGenerator;
  
  @Autowired
  private EntityToRestNodeNameMap entityToRestNodeNameMap;
  
  public GenericEntityDAO getDao() { return genericEntityDao; }
  public EntityUriGenerator<String> getEntityUriGenerator() { return entityUriGenerator; }
  public String getNodeName(Class clazz)
  {
    return entityToRestNodeNameMap.getNodeName(clazz);
  }
  
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
  {
    return null;
  }

  // TODO: move to an intermediate abstract subclass: HasAttachedFilesConverter 
  protected void writeAttachedFile(AttachedFile attachedFile, HierarchicalStreamWriter writer)
  {
    AttachedFile af = (AttachedFile)attachedFile.restrict();
    if(af == null ) return;
    writer.startNode(getNodeName(AttachedFile.class));
    
    writer.addAttribute("name", af.getFilename());
    writer.addAttribute("dateCreated", af.getDateCreated().toString());
    writer.addAttribute("href", (String) af.acceptVisitor(getEntityUriGenerator()));
    writer.endNode();
  }  

}
