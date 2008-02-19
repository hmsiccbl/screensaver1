// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.hibernate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.DistinctRootEntityResultTransformer;

/**
 * A suite of pedagogical tests to determine whether we can have Hibernate eager
 * fetch an entity and its collections, but restricting the collections at query
 * time. Hibernate documentation tells us to use "Filters", but these cannot be
 * dynamically created at run-time (AFAIK), and are kludgey relative to using
 * just HQL to perform our eager fetching of restricted collections. Alas, it
 * works.
 *
 * @author drew
 */
public class HibernateCapabilities extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(HibernateCapabilities.class);


  // instance data members

  private ScreenResult _screenResult;

  // public constructors and methods


  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    genericEntityDao.doInTransaction(new DAOTransaction() {

      public void runTransaction()
      {
        Screen rnaiScreen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
        Library rnaiLibrary = MakeDummyEntities.makeDummyLibrary(rnaiScreen.getScreenNumber(),
                                                                 rnaiScreen.getScreenType(),
                                                                 1);
        genericEntityDao.saveOrUpdateEntity(rnaiLibrary);
        MakeDummyEntities.makeDummyScreenResult(rnaiScreen, rnaiLibrary);
        genericEntityDao.saveOrUpdateEntity(rnaiScreen);
      }
    });
    // note: we reload the screen result, even though it was just created
    // (above), since we want entity-ID-based hashCodes, not Object hashCodes
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen _rnaiScreen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
        _screenResult = _rnaiScreen.getScreenResult();
        genericEntityDao.needReadOnly(_screenResult, "resultValueTypes.resultValues");
        genericEntityDao.needReadOnly(_screenResult, "wells");
      }
    });
  }

  /**
   * Tests whether we can have Hibernate materialize an entity network, having
   * an entity's related collection be populated with a <i>restricted</i>
   * subset of values. Also tests the critical requirement that this works
   * across multiple joins of the same table type, retrieving disjoint rows for
   * each of the repeated table joins (in this case, result_value). Note that
   * this will *not* work if one of the repeated joins contains a null value
   * (the whole collection is restricted!)
   */
  public void testHQLForEagerFetchRestrictedCollectionWithMultiJoin()
  {
    Well wellWithAllResultValues = genericEntityDao.findEntityById(Well.class,
                                                                   "01000:A01",
                                                                   true,
                                                                   "resultValues");
    assertEquals("well.resultValues unrestricted size",
                 8 /* all RVTs in screen result*/,
                 wellWithAllResultValues.getResultValues().size());

    final ResultValueType rvt1 =  _screenResult.getResultValueTypesList().get(0);
    final ResultValueType rvt2 =  _screenResult.getResultValueTypesList().get(1);
    List<Well> result = genericEntityDao.runQuery(new edu.harvard.med.screensaver.db.Query() {
      public List execute(Session session)
      {
        Query query = session.createQuery("from Well w " +
            "left join fetch w.resultValues rv1 " +
            "left join fetch w.resultValues rv2 " +
            "where w.id = :wellKey " +
            "and rv1.resultValueType = :rvt1 " +
            "and rv2.resultValueType = :rvt2");
        query.setParameter("wellKey", "01000:A01");
        query.setParameter("rvt1", rvt1);
        query.setParameter("rvt2", rvt2);
        return query.list();
      }
    });
    Well well = result.get(0);
    assertEquals("well" , well.getWellKey(), new WellKey("01000:A01"));
    assertEquals("well.resultValues restricted size" ,
                 2,
                 well.getResultValues().size());
    Set<ResultValueType> actualRVTs = new HashSet<ResultValueType>();
    for (ResultValue rv : well.getResultValues().values()) {
      actualRVTs.add(rv.getResultValueType());
    }
    assertEquals("well.resultValues.rvt is correct",
                   new HashSet<ResultValueType>(Arrays.asList(rvt1, rvt2)),
                   actualRVTs);
  }

  /**
   * Tests whether we can have Hibernate materialize an entity network, having
   * an entity's related collection be populated with a <i>restricted</i> subset
   * of values. In particular, we explicitly test for the case that failed for
   * the multi-join strategy (above), where a RVT has no RV for the requested
   * Well(s).
   */
  public void testHQLForEagerFetchRestrictedCollectionWithSingleJoin()
  {
    // test a Well that has RVs for all RVTs in the database (i.e., across all ScreenResults)
    Well wellWithAllResultValuesForAllScreenResults =
      genericEntityDao.findEntityById(Well.class,
                                      "01000:A01",
                                      true,
                                      "resultValues");
    assertEquals("well.resultValues unrestricted size",
                 8 /*all RVTs in screen result*/,
                 wellWithAllResultValuesForAllScreenResults.getResultValues().size());

    // test a Well that does not have RVs for all *requested* RVTs
    Well wellWithAMissingResultValueType =
      genericEntityDao.findEntityById(Well.class,
                                      "01000:A04",
                                      true,
                                      "resultValues");
    assertEquals("well.resultValues unrestricted size for well this is missing a RV for a RVT",
                 7 /*8-1 (missing a RV for the "Comments" RVT)*/,
                 wellWithAMissingResultValueType.getResultValues().size());

    final ResultValueType rvt1 =  _screenResult.getResultValueTypesList().get(0);
    final ResultValueType rvt2 =  _screenResult.getResultValueTypesList().get(7); // "Comments" RVT, not defined for every well
    List<Well> result = genericEntityDao.runQuery(new edu.harvard.med.screensaver.db.Query() {
      public List execute(Session session)
      {
        Query query = session.createQuery("from Well w " +
            "left join fetch w.resultValues rv " +
            "where w.id in (:wellKeys) " +
            "and rv.resultValueType in (:rvts) " +
            "order by w.id");
        query.setParameterList("wellKeys", Arrays.asList("01000:A01", "01000:A04"));
        query.setParameterList("rvts", Arrays.asList(rvt1, rvt2));
        query.setResultTransformer(new DistinctRootEntityResultTransformer());
        return query.list();
      }
    });

    // test that Well.resultValues is restricted by specific RVTs
    Well well = result.get(0);
    assertEquals("well", new WellKey("01000:A01"), well.getWellKey());
    assertEquals("well.resultValues restricted size" ,
                 2,
                 well.getResultValues().size());
    Set<ResultValueType> actualRVTs = new HashSet<ResultValueType>();
    for (ResultValue rv : well.getResultValues().values()) {
      actualRVTs.add(rv.getResultValueType());
    }
    assertEquals("well.resultValues.rvt is correct",
                   new HashSet<ResultValueType>(Arrays.asList(rvt1, rvt2)),
                   actualRVTs);

    // test that Well.resultValues is restricted by specific RVTs, even when one of those RVTs has no RVs for the Well
    // this is a common case when generating cross-ScreenResult WellSearchResults, or amending (Study) AnnotationType columns
    well = result.get(1);
    assertEquals("well", new WellKey("01000:A04"), well.getWellKey());
    assertEquals("well.resultValues restricted size" ,
                 1,
                 well.getResultValues().size());
    assertEquals("well.resultValues.rvt is correct",
                 well.getResultValues().values().iterator().next().getResultValueType(),
                 rvt1);

  }

  public void testDuplicateRelationshipJoin()
  {
    final ResultValueType rvt1Entity = _screenResult.getResultValueTypesList().get(0);
    final ResultValueType rvt2Entity = _screenResult.getResultValueTypesList().get(2);

    List<Object> hqlResult = genericEntityDao.runQuery(new edu.harvard.med.screensaver.db.Query() {
      public List execute(Session session) {
        String hql = "from Well w " +
        "join w.resultValues rv1 " +
        "join w.resultValues rv2 " +
        "join rv1.resultValueType rvt1 " +
        "join rv2.resultValueType rvt2 " +
        "where rvt1.name = :name1 and rvt2.name = :name2";
        org.hibernate.Query query = session.createQuery(hql);
        query.setParameter("name1", rvt1Entity.getName());
        query.setParameter("name2", rvt2Entity.getName());
        return query.list();
      }
    });
    log.info(hqlResult);

    // this is expected to fail due to Hibernate bug
    // http://opensource.atlassian.com/projects/hibernate/browse/HHH-879
//    List<Object> criteriaResult = genericEntityDao.runQuery(new Query() {
//      public List execute(Session session) {
//        Criteria well = session.createCriteria(Well.class);
////        Criteria rvs1 = well.createCriteria("resultValues");
////        Criteria rvs2 = well.createCriteria("resultValues");
////        Criteria rvt1 = rvs1.createCriteria("resultValueType");
////        Criteria rvt2 = rvs2.createCriteria("resultValueType");
//        Criteria rvs1 = well.createAlias("resultValues", "rvs1");
//        Criteria rvs2 = well.createAlias("resultValues", "rvs2");
//        Criteria rvt1 = well.createCriteria("rvs1.resultValueType");
//        Criteria rvt2 = well.createCriteria("rvs2.resultValueType");
//        rvt1.add(Restrictions.eq("name", rvt1Entity.getName()));
//        rvt2.add(Restrictions.eq("name", rvt2Entity.getName()));
//        return well.list();
//      }
//    });
//    log.info(criteriaResult);
  }

  // private methods

}
