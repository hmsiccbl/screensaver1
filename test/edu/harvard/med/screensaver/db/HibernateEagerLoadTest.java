// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/test/edu/harvard/med/screensaver/db/SimpleDAOTest.java $
// $Id: SimpleDAOTest.java 1364 2007-05-24 16:44:49Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.lang.reflect.Field;
import java.util.List;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;
import org.hibernate.collection.AbstractPersistentCollection;


/**
 * JUnit-drive code for investigate Hibernate behavior w.r.t eager loading (HQL
 * left join fetch, etc).  Not a real test class.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class HibernateEagerLoadTest extends AbstractSpringTest
{
  
  private static final String GENE_NAME = "ANT1";

  private static final String LIBRARY_NAME = "ln1";

  private static final String WELL_ID = "00001:A01";

  private static final int PLATE_NUMBER = 1;

  private static final String WELL_NAME = "A01";

  private static final Integer GENE_ID = 1;

  private static final String GENE_SYMBOL = "GS1";
  
  private static final String GENBANK_ACC_NO = "GBAN1";

  private static final String SIRNA_SEQ = "AAAA";

  private static final String SIRNA_ID = "Gene(" + GENE_ID + "):siRNA:" + SIRNA_SEQ;

  

  private static final Logger log = Logger.getLogger(HibernateEagerLoadTest.class);


  // public static methods
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(HibernateEagerLoadTest.class);
  }

  
  // protected instance fields
  
  /**
   * Bean property, for database access via Spring and Hibernate.
   */
  protected GenericEntityDAO genericEntityDao;

  /**
   * For schema-related test setup tasks.
   */
  protected SchemaUtil schemaUtil;

  
  // protected instance methods

  @Override
  protected void onSetUp() throws Exception
  {
    schemaUtil.truncateTablesOrCreateSchema();
    
    // create a library, well, silencing reagent, gene hierarchy
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library library = new Library(
          LIBRARY_NAME,
          "sn1",
          ScreenType.SMALL_MOLECULE,
          LibraryType.NATURAL_PRODUCTS,
          1,
          50);
        Well well1 = library.createWell(new WellKey(PLATE_NUMBER, WELL_NAME), WellType.EXPERIMENTAL);
        Gene gene1 = new Gene(GENE_NAME, GENE_ID, GENE_SYMBOL, "Human");
        gene1.addGenbankAccessionNumber(GENBANK_ACC_NO);
        SilencingReagent siReagent1 = gene1.createSilencingReagent(SilencingReagentType.SIRNA, SIRNA_SEQ);
        well1.addSilencingReagent(siReagent1);
        genericEntityDao.persistEntity(library);
      }
    });

  }

  
  // public instance methods

  /**
   * Intent is to inspect the SQL produced for left-join versus left-join-fetch
   * queries. LESSON: not unexpectedly, the left-join-fetch generates an SQL
   * projection including fields for the Library, Well, and SilencingReagent
   * entities, whereas the left-join only generates an SQL projection for the
   * Library entity. In both cases, however, the generated SQL statements,
   * perform joins between all entities (which is not strictly necessary in our
   * left-join example, since we're inspecting any of the value of the child
   * entities, and being a left join, the query result is not affected by the
   * absence or existence of the child entities (as it would be if inner joins
   * were used).
   * @throws Exception 
   */
  public void testLoadVersusFetch() throws Exception
  {
    log.debug("'left join' query:");
    Library library1 = ((List<Library>)
    genericEntityDao.findEntitiesByHql(Library.class,
                                       "select distinct l from Library l left join       l.hbnWells w left join       w.hbnSilencingReagents where l.libraryName = ?",
                                       LIBRARY_NAME)).iterator().next();
    assertFalse("'left join' library.wells is non-initialized persistent collection",
                isPersistentCollectionInitialized(library1, "_wells"));
    
    log.debug("'left join fetch' query:");
    Library library2 = ((List<Library>)
    genericEntityDao.findEntitiesByHql(Library.class,
                                       "select distinct l from Library l left join fetch l.hbnWells w left join fetch w.hbnSilencingReagents where l.libraryName = ?",
                                       LIBRARY_NAME)).iterator().next();
    assertTrue("'left join' library.wells is non-initialized persistent collection",
               isPersistentCollectionInitialized(library2, "_wells"));
   }
  
  private boolean isPersistentCollectionInitialized(AbstractEntity entity, String property) throws Exception
  { 
    Field field = entity.getClass().getDeclaredField(property);
    field.setAccessible(true);
    AbstractPersistentCollection pColl = (AbstractPersistentCollection) field.get(entity);
    boolean isInitialized = pColl.wasInitialized();
    return isInitialized;
  }
  
  public void testLeftJoinFetchBug()
  {
    List<SilencingReagent> siRNAs = null;
    
    log.debug("executing query:");
    siRNAs = 
      genericEntityDao.findEntitiesByHql(SilencingReagent.class,
                                         "select distinct sr from SilencingReagent sr left join fetch sr.gene g left join fetch g.genbankAccessionNumbers where sr.silencingReagentId = ?",
                                         SIRNA_ID);
    assertEquals("query, as desired", 
                 GENBANK_ACC_NO, siRNAs.iterator().next().getGene().getGenbankAccessionNumbers().iterator().next());
  }
  
}
