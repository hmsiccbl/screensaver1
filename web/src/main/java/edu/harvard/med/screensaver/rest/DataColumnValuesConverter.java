
package edu.harvard.med.screensaver.rest;

import org.apache.log4j.Logger;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;

public class DataColumnValuesConverter extends RestConverter
{
  public static class DataColumnValuesRequest
  {
    public DataColumn dc;
  
    public DataColumnValuesRequest(DataColumn dc)
    {
      this.dc = dc;
    }
  }


  private static final Logger log = Logger.getLogger(DataColumnValuesConverter.class);

  public boolean canConvert(Class clazz)
  {
    return DataColumnValuesConverter.DataColumnValuesRequest.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, final HierarchicalStreamWriter writer,
                        MarshallingContext context)
  {
    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        DataColumn dataColumn = ((DataColumnValuesConverter.DataColumnValuesRequest) value).dc;
        dataColumn = getDao().findEntityById(DataColumn.class, dataColumn.getEntityId());
        
        if(dataColumn.getResultValues().isEmpty())
        {
          writer.setValue("empty"); // XStream doesn't like empty nodes
          return;
        }
        int i =0;
        long startTime =  System.currentTimeMillis();
        long loopTime = startTime;
        //TODO: not memory performant for larg datasets (>100k)  possibly run this out in batches, getting RV's by Plate range, or the like -sde4
        for(ResultValue rv:dataColumn.getResultValues())
        {
          writer.startNode("value");
          // Note: may want to just display the platenumber/wellname
          // writer.addAttribute("plate", "" + rv.getWell().getPlateNumber());
          // writer.addAttribute("well", rv.getWell().getWellName());
          writer.addAttribute("href",  (String)rv.getWell().acceptVisitor(getEntityUriGenerator()));
          writer.setValue("" + rv.getTypedValue());
          writer.endNode();
          if (i++ % 1000 == 0) {
            log.debug("i: " + i + ", elapsed: " + (System.currentTimeMillis() - startTime) + " (ms), loop: " +
                      (System.currentTimeMillis() - loopTime) + " (ms)");
            loopTime = System.currentTimeMillis();
          }
        }
        log.debug("DataColumnValuesRequest: done: " + i + " values, elapsed: " + (System.currentTimeMillis() - startTime) +
          " (ms)");
      }
    });
  }


  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context)
  {
    return null;
  }

}