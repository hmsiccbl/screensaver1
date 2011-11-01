
package edu.harvard.med.screensaver.rest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.users.LabHead;

/**
 * TODO: consider a class hierarchy of ScreensaverUserConverter<-ScreeningRoomUserConverter<-LabHeadConverter
 */
public class LabHeadConverter extends RestConverter
{
  private static final Logger log = Logger.getLogger(LabHeadConverter.class);
  
  @Autowired 
  private ScreeningRoomUserConverter screeningRoomUserConverter;
  
  public boolean canConvert(Class clazz)
  {
    return LabHead.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, final HierarchicalStreamWriter writer,
                        MarshallingContext context)
  {
    final XStreamUtil util = new XStreamUtil(writer, context, getEntityUriGenerator());

    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        LabHead labHead = (LabHead) value;
        labHead = getDao().reloadEntity(labHead);
        screeningRoomUserConverter.write(util, labHead);
        util.writeNode(labHead.getLab().getLabAffiliationName(), "labAffiliationName");
        util.writeNode(labHead.getLab().getLabAffiliation().getAffiliationCategory(), "labAffiliationCategory");
      }
    });
  }

  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context)
  {
    return null;
  }

}