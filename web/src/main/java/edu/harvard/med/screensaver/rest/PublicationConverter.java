
package edu.harvard.med.screensaver.rest;

import org.apache.log4j.Logger;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.screens.Publication;

/**
 * TODO: consider a class hierarchy of ScreensaverUserConverter<-ScreeningRoomUserConverter<-LabHeadConverter
 */
public class PublicationConverter extends RestConverter
{
  private static final Logger log = Logger.getLogger(PublicationConverter.class);
  
  public boolean canConvert(Class clazz)
  {
    return Publication.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, final HierarchicalStreamWriter writer,
                        MarshallingContext context)
  {
    final XStreamUtil util = new XStreamUtil(writer, context, getEntityUriGenerator());

    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Publication p = (Publication) value;
        p = getDao().reloadEntity(p);
        
        if (p.getPubmedId() != null) writer.addAttribute("pubmedId", p.getPubmedId().toString());
        if (p.getPubmedCentralId() != null) writer.addAttribute("pubmedCentralId", p.getPubmedCentralId().toString());
        if (p.getAttachedFile() != null) writeAttachedFile(p.getAttachedFile(), writer);
        
        util.writeNode(p.getYearPublished(), "yearPublished");
        util.writeNode(p.getAuthors(), "authors");
        util.writeNode(p.getTitle(), "title");
        util.writeNode(p.getJournal(), "journal");
        util.writeNode(p.getVolume(), "volume");
        util.writeNode(p.getPages(), "pages");
      }
    });
  }

  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context)
  {
    return null;
  }

}