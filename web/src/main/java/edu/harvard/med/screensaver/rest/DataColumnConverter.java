
package edu.harvard.med.screensaver.rest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;

public class DataColumnConverter extends RestConverter
{
  private static final Logger log = Logger.getLogger(DataColumnConverter.class);

  public boolean canConvert(Class clazz)
  {
    return DataColumn.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, final HierarchicalStreamWriter writer,
                        MarshallingContext context)
  {
    final XStreamUtil util = new XStreamUtil(writer, context, getEntityUriGenerator());

    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        DataColumn dataColumn = (DataColumn) value;
        dataColumn = getDao().findEntityById(DataColumn.class, dataColumn.getEntityId());

        util.writeNode(dataColumn.getOrdinal(), "ordinal");
        util.writeNode(dataColumn.getName(), "name");
        util.writeNode(dataColumn.getDescription(), "description");
        util.writeNode(dataColumn.getDataType(),"dataType");
        util.writeNode(dataColumn.getDecimalPlaces(), "decimalPlaces");
        util.writeNode(dataColumn.getTimePoint(), "timePoint");
        util.writeNode(dataColumn.getReplicateOrdinal(), "replicateNumber");
        util.writeNode(dataColumn.getCellLine(), "cellLine");
        util.writeNode(dataColumn.getAssayReadoutType(), "assayReadoutType");
      }
    });
  }

  protected static void writeHeader(final XStreamUtil util, final DataColumn dataColumn)
  {
    util.writeNode(dataColumn.getName(), "name");
    util.writeNode(dataColumn.getEntityId(), "id");
    util.writeNode(dataColumn.getDataType(), "dataType");
  }

  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context)
  {
    return null;
  }

}