//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;

// TODO: convert to an instance-based service (not static methods)
// TODO: have main method take entity loading plan from command line
/**
 * Replicates an entity network from one database to another. Requires that any
 * entities being copied do not already exist in the destination database (i.e.,
 * does not perform merging). Non-nullable to-one relationships must be loaded
 * via the specified relationships.
 */
public class EntityReplicator
{
  // static members

  private static Set<Pair<Class<? extends AbstractEntity>,Integer>> virginized =
    new HashSet<Pair<Class<? extends AbstractEntity>,Integer>>();

  private static Logger log = Logger.getLogger(EntityReplicator.class);

  private SessionFactory _sessionFactory;

  public EntityReplicator(SessionFactory sessionFactory)
  {
    _sessionFactory = sessionFactory;
  }

  @SuppressWarnings({ "static-access", "unchecked" })
  public static void main(String[] args) throws ParseException
  {
    CommandLineApplication app = new CommandLineApplication(args);
    addDestinationDatabaseOptions(app);
    app.addCommandLineOption(OptionBuilder.isRequired().hasArg().withArgName("entity class name").withDescription("the fully qualified class name of the entity type to load").create("e"));
    app.addCommandLineOption(OptionBuilder.isRequired().hasArgs().withArgName("id(s)").withDescription("the entity identifier(s)").create("i"));
    app.addCommandLineOption(OptionBuilder.hasArgs().withArgName("relationships").withDescription("the relationships to fetch").create("r"));

    if (!app.processOptions(true, true)) {
      System.exit(1);
    }

    final SessionFactory sessionFactory = getDestinationSessionFactory(app);
    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    final AbstractEntity[] entityToReplicate = new AbstractEntity[1];
    String entityClassName = app.getCommandLineOptionValue("e");
    ClassMetadata entityMetadata = sessionFactory.getClassMetadata(entityClassName);
    if (entityMetadata == null) {
      log.error("no such entity type " + entityClassName);
      System.exit(1);
    }
    final Class<? extends AbstractEntity> entityClass = entityMetadata.getMappedClass(EntityMode.POJO);
    final List<String> ids = app.getCommandLineOptionValues("i");
    final List<String> relationships = app.getCommandLineOptionValues("r");
    EntityReplicator replicator = new EntityReplicator(sessionFactory);
    for (final String id : ids) {
      dao.doInTransaction(new DAOTransaction() {
        public void runTransaction() {
          Integer numericId;
          try {
            numericId = Integer.parseInt(id);
          }
          catch (Exception e) {
            throw new IllegalArgumentException("id " + id + " is not numeric");
          }
          AbstractEntity srcEntity = dao.findEntityById(entityClass, numericId); // TODO: not all entities have Integer IDs!
          for (String relationshipGroup : relationships) {
            String[] relationships = relationshipGroup.split(",");
            dao.needReadOnly(srcEntity, relationships);
          }
          log.info("retrieved " + srcEntity + " from source database");
          entityToReplicate[0] = srcEntity;
        }
      } );
      replicator.replicateEntity(entityToReplicate[0]);
    }
  }

  @SuppressWarnings("static-access")
  private static void addDestinationDatabaseOptions(CommandLineApplication app)
  {
    app.addCommandLineOption(OptionBuilder.
                             hasArg().
                             withArgName("dest host name").
                             withLongOpt("destdbhost").
                             withDescription("dest database host").
                             create("H2"));
    app.addCommandLineOption(OptionBuilder.
                             hasArg().
                             withArgName("port").
                             withLongOpt("destdbport").
                             withDescription("dest database port").
                             create("T2"));
    app.addCommandLineOption(OptionBuilder.
                             isRequired().
                             hasArg().
                             withArgName("user name").
                             withLongOpt("destdbuser").
                             withDescription("dest database user name").
                             create("U2"));
    app.addCommandLineOption(OptionBuilder.
                             hasArg().
                             withArgName("password").
                             withLongOpt("destdbpassword").
                             withDescription("dest database user's password").
                             create("P2"));
    app.addCommandLineOption(OptionBuilder.
                             isRequired().
                             hasArg().
                             withArgName("database").
                             withLongOpt("destdbname").
                             withDescription("dest database name").
                             create("D2"));
  }

  private static SessionFactory getDestinationSessionFactory(CommandLineApplication app)
    throws ParseException
  {
    String hostname = app.getCommandLineOptionValue("H2");
    if (hostname.length() == 0) {
      hostname = "localhost";
    }
    String database = app.getCommandLineOptionValue("D2");
    String username = app.getCommandLineOptionValue("U2");
    Integer port = app.getCommandLineOptionValue("T2", Integer.class);
    String password = null;
    if (app.isCommandLineFlagSet("P")) {
      password = app.getCommandLineOptionValue("P2");
    }
    else {
      password = new DotPgpassFileParser().getPasswordFromDotPgpassFile(hostname,
                                                                        port == null ? "" : port.toString(),
                                                                        database,
                                                                        username);
    }
    if (password == null) {
      password = "";
    }

    Configuration hibCfg = configureHibernate(hostname,
                                              port,
                                              database,
                                              username,
                                              password);
    SessionFactory sessionFactory = hibCfg.buildSessionFactory();
    return sessionFactory;
  }

  private static Configuration configureHibernate(String hostname,
                                                  Integer port,
                                                  String database,
                                                  String username,
                                                  String password)
  {
    Configuration hibCfg = new Configuration();
    hibCfg.setNamingStrategy(ImprovedNamingStrategy.INSTANCE);
    hibCfg.configure();
    hibCfg.setProperty("hibernate.connection.url",
                       "jdbc:postgresql://" +
                       hostname +
                       (port == null ? "" : ":" + port) +
                       "/" + database);
    hibCfg.setProperty("hibernate.connection.username", username);
    hibCfg.setProperty("hibernate.connection.password", password);
    if (log.isDebugEnabled()) {
      showProps(hibCfg);
    }
    return hibCfg;
  }

  /**
   * Replicates the specified entity, along with all of its <i>initialized</i>
   * entity relationships and value collections, to the database represented by
   * this EntityReplicator's SessionFactory.
   *
   * @param entity the entity to replicate
   */
  public void replicateEntity(AbstractEntity entity)
  {
    virginize(entity);
    log.info("done virginizing");

    Session session = _sessionFactory.openSession();
    Transaction txn = session.beginTransaction();
    session.saveOrUpdate(entity);
    txn.commit();
    session.close();
    log.info("copied " + entity + " to destination database");
  }

  @SuppressWarnings("unchecked")
  private void virginize(AbstractEntity entity)
  {
    Pair<Class<? extends AbstractEntity>,Integer> entityVirginizedKey =
      new Pair<Class<? extends AbstractEntity>,Integer>(entity.getEntityClass(), entity.hashCode());
    if (virginized.contains(entityVirginizedKey)) {
      return;
    }
    virginized.add(entityVirginizedKey);
    log.info("virginizing " + entity);

    Class<? extends AbstractEntity> entityClass = entity.getEntityClass();
    ClassMetadata classMetadata = _sessionFactory.getClassMetadata(entityClass);

    classMetadata.setIdentifier(entity, null, EntityMode.POJO); // make into a transient entity, so it can persisted in new session
    classMetadata.setPropertyValue(entity, "version", 0, EntityMode.POJO); // necessary?

    List<String> props = Arrays.asList(classMetadata.getPropertyNames());
    for (String propName : props) {
      Type propType = classMetadata.getPropertyType(propName);
      log.debug(classMetadata.getEntityName() + "." + propName +
                " [" + propType.getName() +
                (propType.isCollectionType() ? " (ToMany)" : propType.isEntityType() ? " (ToOne)" : "") + "]");

      // get the property value, but w/o triggering lazy initialization
      Object value = classMetadata.getPropertyValue(entity, propName, EntityMode.POJO);

      // virginize associated entities, recursively
      if (propType.isCollectionType()) {
        if (value instanceof PersistentCollection) {
          Collection coll = null;
          Map map = null;
          if (SortedSet.class.isAssignableFrom(propType.getReturnedClass())) {
            coll = new TreeSet();
          }
          else if (Set.class.isAssignableFrom(propType.getReturnedClass())) {
            coll = new HashSet();
          }
          else if (List.class.isAssignableFrom(propType.getReturnedClass())) {
            coll = new ArrayList();
          }
          else if (SortedMap.class.isAssignableFrom(propType.getReturnedClass())) {
            map = new TreeMap();
          }
          else if (Map.class.isAssignableFrom(propType.getReturnedClass())) {
            map = new HashMap();
          }
          else {
            throw new RuntimeException("unsupported collection type: " + propType.getReturnedClass() + " for property " + propName);
          }
          if (coll != null) {
            log.debug("persistent collection " + propName + " replaced with normal collection " + coll.getClass());
            if (((PersistentCollection) value).wasInitialized()) {
              log.debug("persistent collection " + propName + " copied");
              coll.addAll((Collection) value);
            }
            value = coll;
          }
          else {
            log.debug("persistent map " + propName + " replaced with normal map " + map.getClass());
            if (((PersistentCollection) value).wasInitialized()) {
              log.debug("persistent map " + propName + " copied");
              map.putAll((Map) value);
            }
            value = map;
          }
          classMetadata.setPropertyValue(entity, propName, value, EntityMode.POJO);
        }
        if (propType.isAssociationType()) { // collection of associated entities
          assert !(value instanceof PersistentCollection) : propName + " should no longer be a persistent collection: " + value.getClass();
          log.debug("virginizing entities in collection " + propName);
          Iterator iter = null;
          if (value instanceof Collection) {
            iter = ((Collection) value).iterator();
          }
          else if (value instanceof Map) {
            iter = ((Map) value).values().iterator();
          }
          else {
            throw new RuntimeException("unsupported collection type: " + value.getClass() + " for property " + propName);
          }
          while (iter.hasNext()) {
            Object element = iter.next();
            if (!(element instanceof AbstractEntity)) {
              break;
            }
            virginize((AbstractEntity) element);
          }
        }
      }
      else if (propType.isAssociationType() && value != null) { // single entity association
        if (value.getClass().getSimpleName().contains("CGLIB")) { // HACK: better way to determine if a lazy, single entity association is uninitialized? also, doesn't handle Java-styel proxies
          log.debug("associated entity " + propName + " not initialized...nullifying");
          value = null;
          classMetadata.setPropertyValue(entity, propName, value, EntityMode.POJO);
        }
        else {
          log.debug("associated entity " + propName + " initialized...virginizing");
          virginize((AbstractEntity) value);
        }
      }
      else {
        log.debug("property value=" + value);
      }
    }
  }

  private static void showProps(Configuration hibCfg)
  {
    log.debug("### Hibernate Configuration properties:");
    for (Object key : hibCfg.getProperties().keySet()) {
      if (key.toString().startsWith("hibernate.")) {
        Object value = hibCfg.getProperties().get(key);
        log.debug(key + "=" + value);
      }
    }
    Iterator<?> classMappings = hibCfg.getClassMappings();
    while (classMappings.hasNext()) {
      log.debug(classMappings.next());
    }
  }



}

