
package edu.harvard.med.screensaver.rest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screens.Screen;

public class ScreenResultValuesConverter extends RestConverter
{
  public static class ScreenResult
  {
    public Screen s;
    public ScreenResult(Screen s)
    {
      this.s = s;
    }
  }


  private static final Logger log = Logger.getLogger(ScreenResultValuesConverter.class);
  
  @Autowired
  private AssayWellConverter assayWellConverter;
  
  public boolean canConvert(Class clazz)
  {
    return ScreenResultValuesConverter.ScreenResult.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, final HierarchicalStreamWriter writer,
                        MarshallingContext context)
  {
    final XStreamUtil util = new XStreamUtil(writer, context, getEntityUriGenerator());

    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Screen s = getDao().reloadEntity(((ScreenResultValuesConverter.ScreenResult) value).s);
        
        if(s.getScreenResult() == null ) 
        {
          writer.setValue("empty"); // xstream doesn't like empty nodes.
          return;
        }
        
        util.writeNodes(s.getScreenResult().getDataColumns(), "columns", getNodeName(DataColumn.class));
        
        writer.startNode("data");
        for(AssayWell aw:s.getScreenResult().getAssayWells())
        {
          writer.startNode(getNodeName(AssayWell.class));
          assayWellConverter.write(writer,aw);
          writer.startNode("values");
          AssayWellValuesConverter.write(writer, aw);
          writer.endNode();
          writer.endNode();
        }
        writer.endNode();
      }
    });
  }


  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context)
  {
    return null;
  }

}