
package edu.harvard.med.screensaver.rest;

import java.util.Map;

import org.apache.log4j.Logger;
import com.google.common.collect.Maps;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Study;

public class StudyValuesConverter extends RestConverter
{
  public static class StudyValues
  {
    public Study s;
    public StudyValues(Study s)
    {
      this.s = s;
    }
  }

  private static final Logger log = Logger.getLogger(StudyValuesConverter.class);

  public boolean canConvert(Class clazz)
  {
    return StudyValuesConverter.StudyValues.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, final HierarchicalStreamWriter writer, MarshallingContext context)
  {
    final XStreamUtil util = new XStreamUtil(writer, context, getEntityUriGenerator());

    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Study s = getDao().reloadEntity(((StudyValuesConverter.StudyValues) value).s);
        // Write out the columns
        util.writeNodes(s.getAnnotationTypes(), "columns", "column");
        
        // Write out the <data><reagent><values>....
        writer.startNode("data");
        
        // create a list of the canonical wells - any reagent will do
        Map<String,Reagent> canonicalWellsMap = Maps.newHashMap();
        for(Reagent r:s.getReagents())
        {
          canonicalWellsMap.put(r.getWell().getFacilityId(), r);
        }
        for(Reagent r:canonicalWellsMap.values())
        {
          writer.startNode("reagent");
          StudyReagentsCanonicalConverter.writeReagent(util, r);
          writer.startNode("values");
          StudyRowConverter.writeRow(writer, s, r);
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