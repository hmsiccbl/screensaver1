// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Study;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

public class ScreenDAOImpl extends AbstractDAO implements ScreenDAO
{
  private static final Integer FIRST_SCREEN_NUMBER = 1;

  private static Logger log = Logger.getLogger(ScreenDAOImpl.class);

  private GenericEntityDAO _dao;


  /**
   * @motivation for CGLIB dynamic proxy creation
   */
  public ScreenDAOImpl()
  {
  }

  public ScreenDAOImpl(GenericEntityDAO dao)
  {
    _dao = dao;
  }
  
  public Integer findNextScreenNumber()
  {
    _dao.flush(); // allow us to create multiple screens within the same Hibernate session
    class NextScreenNumberQuery implements edu.harvard.med.screensaver.db.Query 
    {
     public List execute(Session session)
      {
         Query hqlQuery = session.createQuery("select max(screenNumber) + 1 from Screen where screenNumber < " + Study.MIN_STUDY_NUMBER);
         return (List) hqlQuery.list();
      } 
    }
    List<Integer> result = _dao.runQuery(new NextScreenNumberQuery());
    Integer nextScreenNumber = result.get(0);
    if (nextScreenNumber == null) {
      nextScreenNumber = FIRST_SCREEN_NUMBER;
    }
    return  nextScreenNumber;
  }
    
  public void deleteStudy(Screen study)
  {
    study = _dao.reloadEntity(study,
                              false,
                              "annotationTypes.annotationValues.reagent");
    if (study != null) {
      for (AnnotationType at : study.getAnnotationTypes()) {
        for (AnnotationValue av : new ArrayList<AnnotationValue>(at.getAnnotationValues().values())) {
          if (av.getReagent().getAnnotationValues().containsKey(at)) {
            av.getReagent().getAnnotationValues().remove(at);
            if (av.getReagent().getAnnotationValues().containsKey(at)) {
              throw new RuntimeException("could not remove annotationValue from reagent");
            }
          }
          if (at.getAnnotationValues().containsKey(av.getReagent())) {
            at.getAnnotationValues().remove(av.getReagent());
            if (at.getAnnotationValues().containsKey(av.getReagent())) {
              throw new RuntimeException("could not remove annotationValue from annotationType");
            }
          }
          _dao.deleteEntity(av);
        }
        _dao.deleteEntity(at);
      }
      _dao.deleteEntity((Screen) study);
      _dao.flush();
      log.info("deleted existing study");
    }
  }
}
