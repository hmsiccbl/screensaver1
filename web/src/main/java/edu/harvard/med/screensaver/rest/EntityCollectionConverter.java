
package edu.harvard.med.screensaver.rest;

import java.util.Collection;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.Entity;

public class EntityCollectionConverter extends RestConverter
{
  
  public boolean canConvert(Class clazz)
  {
    return EntityCollection.class.isAssignableFrom(clazz);
  }

  public void marshal(Object value, final HierarchicalStreamWriter writer, MarshallingContext context)
  {
    @SuppressWarnings("rawtypes")
    final EntityCollection entityCollection = (EntityCollection) value;
    if(entityCollection.getCollection().isEmpty()) return;
    final XStreamUtil util = new XStreamUtil(writer, context, getEntityUriGenerator());
    
    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        for (Object o : entityCollection.getCollection()) {
          Entity e = getDao().reloadEntity((Entity)o, true);
          switch (entityCollection.getEntityOutputType()) {
            case ENTITY_CONTENTS: 
              util.writeNode(e, getNodeName(entityCollection.getClazz()));
              break;
            case ENTITY_URI:
              util.writeUri(e, getNodeName(entityCollection.getClazz()));
              break;
          }
        }
      }
    });
   }
}