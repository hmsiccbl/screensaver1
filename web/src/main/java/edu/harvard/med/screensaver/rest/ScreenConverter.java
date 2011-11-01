
package edu.harvard.med.screensaver.rest;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;

public class ScreenConverter extends RestConverter
{
  private static final Logger log = Logger.getLogger(ScreenConverter.class);

  public boolean canConvert(Class clazz)
  {
    return Screen.class.isAssignableFrom(clazz);
  }

  @Transactional(readOnly = true)
  //TODO: investigate why this is not sufficient to ensure that the same session is used throughout (and to avoid LazyInitExceptions) - ask ant4, sde
  public void marshal(final Object value, final HierarchicalStreamWriter writer,
                        MarshallingContext context)
  {
    final XStreamUtil util = new XStreamUtil(writer, context, getEntityUriGenerator());
    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Screen screen = (Screen) value;
        screen = getDao().findEntityByProperty(Screen.class, "facilityId", screen.getFacilityId());//, true, Screen.collaborators);
        if(screen.getProjectPhase() != ProjectPhase.ANNOTATION)
        {
          util.writeNode(screen.getFacilityId(), "facilityId");
          util.writeNode(screen.getTitle(), "title");
          util.writeNode(screen.getScreenType(), "screenType");
          util.writeNode(screen.getPublishableProtocol(), "publishableProtocol");
          util.writeNode(screen.getDateCreated(), "dateReceived");
          util.writeNode(screen.getDateLoaded(), "dateLoaded");
          util.writeNode(screen.getDatePubliclyAvailable(), "datePubliclyAvailable");
          util.writeNode(screen.getSummary(), "summary");
          util.writeUri(screen.getLabHead(), "labHead");
          util.writeUri(screen.getLeadScreener(), "leadScreener");  
  
          if (screen.getScreenResult() != null)
          {
            util.writeNode(screen.getScreenResult().getAssayReadoutTypes(), "assayReadoutTypes");
            util.writeNode(screen.getScreenedExperimentalWellCount(), "experimentalWellsLoaded");
           }
  
          util.writeNode(new EntityCollection<Publication>(Publication.class, screen.getPublications()), "publications");
  
          for (AttachedFile af : screen.getAttachedFiles())
          {
            writeAttachedFile(af, writer);
          }
        } else {  // Study  * note: root node is still "<screen>"
          util.writeNode(screen.getFacilityId(), "facilityId");
          util.writeNode(screen.getTitle(), "title");
          util.writeNode(screen.getLabHead().getLab().getLabName(), "labName");
          util.writeUri(screen.getLeadScreener(), "studyLead");
          if (screen.getWellStudied() != null) {
            util.writeUri(screen.getWellStudied().getLatestReleasedReagent(), "reagentStudied");
          }
          util.writeNode(screen.getStudyType(), "libraryScreenType");
          util.writeNode(screen.getSummary(), "summary");
          util.writeNode(screen.getDateCreated(), "dateDataReceived");
          util.writeNode(new EntityCollection<Publication>(Publication.class, screen.getPublications()), "publications");
          writer.startNode("attachedFiles");
          for (AttachedFile af : screen.getAttachedFiles())
          {
            writeAttachedFile(af, writer);
          }
          writer.endNode();
        }
        
      }

    });
  }

  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context)
  {
    return null;
  }

}