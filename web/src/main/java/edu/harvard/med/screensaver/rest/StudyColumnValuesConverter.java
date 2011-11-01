
package edu.harvard.med.screensaver.rest;

import java.util.Map;

import com.google.common.collect.Maps;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;

public class StudyColumnValuesConverter extends RestConverter
{
  public static class StudyColumnValuesRequest
  {
    public AnnotationType at;
  
    public StudyColumnValuesRequest(AnnotationType at)
    {
      this.at = at;
    }
  }


  private static final Logger log = Logger.getLogger(StudyColumnValuesConverter.class);
  
  @Autowired
  private LibrariesDAO librariesDao;

  public boolean canConvert(Class clazz)
  {
    return StudyColumnValuesConverter.StudyColumnValuesRequest.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, final HierarchicalStreamWriter writer,
                        MarshallingContext context)
  {
    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        AnnotationType at = ((StudyColumnValuesConverter.StudyColumnValuesRequest) value).at;
        at = getDao().reloadEntity(at);
        
        if(at.getAnnotationValues().isEmpty())
        {
          writer.setValue("empty"); // XStream doesn't like empty nodes
          return;
        }
        
        int i =0;
        long startTime =  System.currentTimeMillis();
        long loopTime = startTime;
        // transform the annotation values into a canonical well list, since they are the same annotations for all the wells (*for LINCS*)
        Map<String,AnnotationValue> canonicalWellToAnnotationValueMap = Maps.newHashMap();
        for(AnnotationValue av :at.getAnnotationValues().values())
        {
          canonicalWellToAnnotationValueMap.put(av.getReagent().getWell().getFacilityId(), av);
        }
        for(AnnotationValue av:canonicalWellToAnnotationValueMap.values())
        {
          writer.startNode("value");
          writer.addAttribute("reagentUrl", av.getReagent().acceptVisitor(getEntityUriGenerator()));
          writer.setValue("" + av.getFormattedValue());
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