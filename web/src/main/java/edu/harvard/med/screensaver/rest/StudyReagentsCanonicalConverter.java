
package edu.harvard.med.screensaver.rest;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import com.google.common.collect.Maps;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.libraries.Reagent;

public class StudyReagentsCanonicalConverter extends RestConverter
{
  public static class StudyReagentsCanonical 
  {
    public Set<Reagent> reagents;
    
    public StudyReagentsCanonical(Class clazz, Set<Reagent> reagents)
    {
      this.reagents = reagents;
    }
  }

  private static final Logger log = Logger.getLogger(StudyReagentsCanonicalConverter.class);

  public boolean canConvert(Class clazz)
  {
    return StudyReagentsCanonicalConverter.StudyReagentsCanonical.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, final HierarchicalStreamWriter writer, MarshallingContext context)
  {
    final XStreamUtil util = new XStreamUtil(writer, context, getEntityUriGenerator());

    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Set<Reagent> reagents = ((StudyReagentsCanonicalConverter.StudyReagentsCanonical) value).reagents;
        // create a list of the canonical wells - any reagent will do
        Map<String,Reagent> canonicalWellsMap = Maps.newHashMap();
        for(Reagent r:reagents)
        {
          r = getDao().reloadEntity(r);
          canonicalWellsMap.put(r.getWell().getFacilityId(), r);
        }
        for (Reagent r : canonicalWellsMap.values())
        {
          writer.startNode("reagent");
          writeReagent(util, r);
          writer.endNode();
        }
      }
    });
  }

  protected static void writeReagent(final XStreamUtil util, Reagent r)
  {
    util.writeUriAttribute(r);
    util.writeNode(r.getVendorId().getVendorName(), "vendorName");
    util.writeNode(r.getVendorId().getVendorIdentifier(), "vendorId");
  }
}