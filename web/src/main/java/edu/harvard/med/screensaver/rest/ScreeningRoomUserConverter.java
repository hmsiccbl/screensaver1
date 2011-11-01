
package edu.harvard.med.screensaver.rest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

/**
 * TODO: consider a class hierarchy of ScreensaverUserConverter<-ScreeningRoomUserConverter<-LabHeadConverter
 */
@Component
public class ScreeningRoomUserConverter extends RestConverter
{
  private static final Logger log = Logger.getLogger(ScreeningRoomUserConverter.class);

  @Autowired 
  private ScreensaverUserConverter screensaverUserConverter;
  
  public boolean canConvert(Class clazz)
  {
    return ScreeningRoomUser.class.isAssignableFrom(clazz) && !LabHead.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, HierarchicalStreamWriter writer,
                        final MarshallingContext context)
  
  {
    final XStreamUtil util = new XStreamUtil(writer, context, getEntityUriGenerator());
    
    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        ScreeningRoomUser user = (ScreeningRoomUser) value;
        user = getDao().findEntityById(ScreeningRoomUser.class, user.getEntityId());
        write(util,user);
        }
    });
  }
  
  protected final void write(final XStreamUtil util, final ScreeningRoomUser user)
  {
    screensaverUserConverter.write(util, user);
    util.writeUri(user.getLab().getLabHead(), "labHead");
    util.writeNode(new EntityCollection.Screens(Screen.class, user.getScreensLed()), "screensLed");
  }

  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context)
  {
    return null;
  }

}