
package edu.harvard.med.screensaver.rest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

/**
 * TODO: consider a class hierarchy of ScreensaverUserConverter<-ScreeningRoomUserConverter<-LabHeadConverter
 */
@Component
public class AnnotationTypeConverter extends RestConverter
{
  private static final Logger log = Logger.getLogger(AnnotationTypeConverter.class);
  
  public boolean canConvert(Class clazz)
  {
    return AnnotationType.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, final HierarchicalStreamWriter writer,
                        MarshallingContext context)
  {
    final XStreamUtil util = new XStreamUtil(writer, context, getEntityUriGenerator());
    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        AnnotationType at = (AnnotationType) value;
        at = getDao().reloadEntity(at);
        write(util, at);
      }
    });
  }

  protected static void write(final XStreamUtil util, AnnotationType at)
  {
    util.writeNode(at.getOrdinal(), "ordinal");
    util.writeNode(at.getName(), "name");
    util.writeNode(at.getDescription(), "description");
    util.writeNode(at.isNumeric(), "isNumeric");
  }
  
  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context)
  {
    return null;
  }

}