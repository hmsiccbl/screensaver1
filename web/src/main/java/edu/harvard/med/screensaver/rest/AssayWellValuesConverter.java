
package edu.harvard.med.screensaver.rest;

import org.apache.log4j.Logger;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;

public class AssayWellValuesConverter extends RestConverter
{
  public static class AssayWellValuesRequest
  {
    public AssayWell aw;
  
    public AssayWellValuesRequest(AssayWell aw)
    {
      this.aw = aw;
    }
  }

  private static final Logger log = Logger.getLogger(AssayWellValuesConverter.class);

  public boolean canConvert(Class clazz)
  {
    return AssayWellValuesConverter.AssayWellValuesRequest.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, final HierarchicalStreamWriter writer,
                        MarshallingContext context)
  {
    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        AssayWell aw = getDao().reloadEntity(((AssayWellValuesConverter.AssayWellValuesRequest) value).aw);

        write(writer, aw);
      }
    });
  }

  protected static void write(final HierarchicalStreamWriter writer, AssayWell aw)
  {
    for (DataColumn dc : aw.getScreenResult().getDataColumns()) {
      for (ResultValue rv : dc.getResultValues()) {
        if (rv.getWell().equals(aw.getLibraryWell())) {
          writer.startNode("value");
          writer.addAttribute("ordinal", dc.getOrdinal().toString());
          writer.setValue("" + rv.getTypedValue());
          writer.endNode();
        }
      }
    }
  }

  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context)
  {
    return null;
  }

}