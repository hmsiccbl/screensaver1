
package edu.harvard.med.screensaver.rest;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

/**
 * TODO: consider a class hierarchy of ScreensaverUserConverter<-ScreeningRoomUserConverter<-LabHeadConverter
 */
@Component
public class ScreensaverUserConverter extends RestConverter
{
  private static final Logger log = Logger.getLogger(ScreensaverUserConverter.class);

  public boolean canConvert(Class clazz)
  {
    return ScreensaverUser.class.isAssignableFrom(clazz) && !ScreeningRoomUser.class.isAssignableFrom(clazz) && ! LabHead.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    throw new NotImplementedException();
  }

  protected final void write(final XStreamUtil util, final ScreensaverUser user)
  {
    util.writeNode(user.getEntityId(), "id");
    util.writeNode(user.getFirstName(), "firstName");
    util.writeNode(user.getLastName(), "lastName");
  }
  
  
  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context)
  {
    return null;
  }

}