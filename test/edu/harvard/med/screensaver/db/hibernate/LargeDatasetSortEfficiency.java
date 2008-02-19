// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.hibernate;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.util.DebugUtils;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class LargeDatasetSortEfficiency extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(LargeDatasetSortEfficiency.class);

  private static int REPITITIONS = 1;

  protected HibernateTemplate hibernateTemplate;

  @Override
  // override method to *avoid* truncating tables!
  protected void onSetUp() throws Exception
  {
  }

  public void testReagentSortOrderEfficiency()
  {
    final AnnotationType annotationType =
      genericEntityDao.findEntityByProperty(AnnotationType.class,
                                            "name",
                                            "# Predicted Target Genes",
                                            true,
                                            "study");
    assertNotNull(annotationType);
    final Study study = annotationType.getStudy();

    timeQuery("sort by reagent id, select by screen (from reagent)",
              "select r.id from Reagent r join r.studies s where s = ? order by r.id",
              (Screen) study);
    timeQuery("sort by reagent id, select by screen (from screen)",
              "select r.id from Screen s join s.reagents r where s = ? order by r.id",
              (Screen) study);
    timeQuery("sort by reagent id, select from annotationType",
              "select r.id from AnnotationType t join t.annotationValues v join v.reagent r where t = ? order by r.id",
              annotationType);
    timeQuery("sort by well.smiles, select by screen (from reagent)",
              "select r.id from Reagent r join r.studies s join r.wells w where s = ? order by w.smiles ",
              (Screen) study);
    timeQuery("sort by well.smiles, select by screen (from screen)",
              "select r.id from Screen s join s.reagents r join r.wells w where s = ? order by w.smiles ",
              (Screen) study);
    timeQuery("sort by well.smiles, select by annotationType",
              "select r.id from AnnotationType t join t.annotationValues v join v.reagent r join r.wells w where t = ? order by w.smiles ",
              annotationType);
    timeQuery("sort by annotation.value",
              "select r.id from Reagent r join r.annotationValues v join v.annotationType t where t = ? order by v.value",
              annotationType);
  }

  public void testResultValueSortOrderEfficiency()
  {
    final Screen screen =
      genericEntityDao.findEntityByProperty(Screen.class,
                                            "screenNumber",
                                            723,
                                            true,
                                            "screenResult.resultValueTypes");
    assertNotNull(screen);
    ScreenResult screenResult = screen.getScreenResult();
    ResultValueType resultValueType = screenResult.getResultValueTypesList().get(0);


//    timeQuery("sort by well id, select by screenResult (from well)",
//              "select w.id from Well w join w. s where s = ? order by r.id",
//              (Screen) study);
    timeQuery("sort by well id, select by screen result",
              "select w.id from ScreenResult sr join sr.wells w where sr = ? order by w.id",
              screenResult);
    timeQuery("sort by well id, select from resultValueType",
              "select w.id from ResultValueType t join t.resultValues v join v.well w where t = ? order by v.id",
              resultValueType);
    timeQuery("sort by well.smiles, select by screen result",
              "select w.id from ScreenResult sr join sr.wells w where sr = ? order by w.smiles ",
              screenResult);
//    timeQuery("sort by well.smiles, select by screen result (from well)",
//              "select r.id from Reagent r join r.studies s join r.wells w where s = ? order by w.smiles ",
//              (Screen) study);
    timeQuery("sort by well.smiles, select by resultValueType",
              "select w.id from ResultValueType t join t.resultValues v join v.well w where t = ? order by w.smiles ",
              resultValueType);
    timeQuery("sort by result.value",
              "select w.id from Well w join w.resultValues v join v.resultValueType t where t = ? order by v.value",
              resultValueType);
  }
  private void timeQuery(String description,
                         final String hql,
                         final Object arg)
  {
    //log.info("running " + description);
    DebugUtils.elapsedTime(description,
                           REPITITIONS,
                           log,
                           new Runnable() {
      public void run()
      {
        hibernateTemplate.execute(new HibernateCallback()
        {
          public Object doInHibernate(org.hibernate.Session session) throws org.hibernate.HibernateException ,java.sql.SQLException
          {
            Query query = session.createQuery(hql);
            query.setReadOnly(true);
            query.setParameter(0, arg);
            return query.list();
          }
        });
      }
    });
  }

}
