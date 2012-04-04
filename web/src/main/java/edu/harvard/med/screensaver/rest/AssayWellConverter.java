
package edu.harvard.med.screensaver.rest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

/**
 */
@Component
public class AssayWellConverter extends RestConverter
{
  private static final Logger log = Logger.getLogger(AssayWellConverter.class);
  
  public boolean canConvert(Class clazz)
  {
    return AssayWell.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, final HierarchicalStreamWriter writer,
                        MarshallingContext context)
  {
    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        AssayWell aw = (AssayWell) value;
        aw = getDao().reloadEntity(aw);
        write(writer,aw);
      }
    });
  }

  protected final void write( final HierarchicalStreamWriter writer, final AssayWell aw)
  {
    writer.addAttribute("libraryWellUrl", (String) aw.getLibraryWell().acceptVisitor(getEntityUriGenerator()));
    if (aw.getAssayWellControlType() != null) {
      writer.startNode("controlType");
      writer.setValue(aw.getAssayWellControlType().getValue());
      writer.endNode();
    }
  }

  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context)
  {
    return null;
  }

}