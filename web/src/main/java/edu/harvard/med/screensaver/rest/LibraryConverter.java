
package edu.harvard.med.screensaver.rest;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.libraries.Library;

public class LibraryConverter extends RestConverter
{
  private static final Logger log = Logger.getLogger(LibraryConverter.class);

  public boolean canConvert(Class clazz)
  {
    return Library.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, HierarchicalStreamWriter writer,
                        MarshallingContext context)
  {
    final XStreamUtil util = new XStreamUtil(writer, context, getEntityUriGenerator());

    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Library library = (Library) value;
        library = getDao().findEntityById(Library.class, library.getEntityId());
        
        util.writeNode(library.getShortName(), "shortName");
        util.writeNode(library.getLibraryName(), "name");
        util.writeNode(library.getScreenType(), "screenType");
        util.writeNode(library.getSolvent(), "solvent");
        util.writeNode(library.getDateCreated(), "dateReceived");
        util.writeNode(library.getDateLoaded(), "dateLoaded");
        util.writeNode(library.getDatePubliclyAvailable(), "datePubliclyAvailable");
        util.writeNode(library.getStartPlate(), "startPlate");
        util.writeNode(library.getEndPlate(), "endPlate");
      }
    });
  }

  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context)
  {
    return null;
  }

}