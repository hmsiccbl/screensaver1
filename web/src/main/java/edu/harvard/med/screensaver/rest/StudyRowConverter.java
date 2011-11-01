
package edu.harvard.med.screensaver.rest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.Study;

@Component
public class StudyRowConverter extends RestConverter
{
  public static class StudyRow 
  {
    public Study s;
    public Reagent r;
    
    public StudyRow(Study s, Reagent r)
    {
      this.s = s;
      this.r = r;
    }
  }

  private static final Logger log = Logger.getLogger(StudyRowConverter.class);

  public boolean canConvert(Class clazz)
  {
    return StudyRowConverter.StudyRow.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, final HierarchicalStreamWriter writer, MarshallingContext context)
  {
    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Study s = getDao().reloadEntity(((StudyRowConverter.StudyRow) value).s);
        Reagent r = getDao().reloadEntity(((StudyRowConverter.StudyRow) value).r);
        writeRow(writer, s, r);
      }
    });
  }

  protected static void writeRow(final HierarchicalStreamWriter writer, Study s, Reagent r)
  {
    int colsWritten = 0;
    for (AnnotationType at : s.getAnnotationTypes()) {
      AnnotationValue av = at.getAnnotationValues().get(r);
      writer.startNode("value");
      writer.addAttribute("ordinal", at.getOrdinal().toString());
      writer.setValue(av.getFormattedValue());
      writer.endNode();
      colsWritten++;
      // NOTE: since we are doing this for one row, do not print out *all* of the wells, just the first one, as this will serve as the "canonical" instance (any will do) - LINCS only
      continue;
    }
  }

  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context)
  {
    return null;
  }

}