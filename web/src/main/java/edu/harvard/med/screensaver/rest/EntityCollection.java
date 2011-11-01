package edu.harvard.med.screensaver.rest;

import java.util.Collection;

import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;

/**
 * A light weight wrapper for a collection of Entities 
 * TODO: this class is created so that the org.springframework.oxm.xstream.XStreamMarshaller can be made to have a proper alias for a screen collection.  
 * If a marshaller with namespace and mapping capabilities is swapped in for the XStreamMarshaller, the need for this should go away.  -sde4
 * Also, consider doing away with the EntityToRestNodeNameMap and using these classes.
 */
public class EntityCollection<E extends Entity>
{
  private Collection<E> collection;
  private Class<E> clazz;
  private EntityOutputType entityOutputType;
  
  public EntityCollection(Class<E> clazz, Collection<E> collection)
  {
    this(clazz, collection, EntityOutputType.ENTITY_URI);
  }

  public EntityCollection(Class<E> clazz, Collection<E> collection, EntityOutputType entityOutputType)
  {
    this.collection = collection;
    this.clazz = clazz;
    this.entityOutputType = entityOutputType;
  }

  public Collection<E> getCollection()
  {
    return this.collection;
  }

  public Class<E> getClazz()
  {
    return this.clazz;
  }

  public EntityOutputType getEntityOutputType()
  {
    return this.entityOutputType;
  }
  
  // Thin wrapper classes to aid Xstream marshaller aliasing of the collection root node, and to direct the marshaller to the CollectionConverter
  
  public static class Screens extends EntityCollection
  {
    public Screens(Class clazz, Collection collection)
    {
      super(clazz, collection);
    }
  };
  public static class Studies extends EntityCollection
  {
    public Studies(Class clazz, Collection collection)
    {
      super(clazz, collection);
    }
  };
  public static class Libraries extends EntityCollection
  {

    public Libraries(Class clazz, Collection collection)
    {
      super(clazz, collection);
    }
  };
  public static class AssayWells extends EntityCollection
  {

    public AssayWells(Class clazz, Collection collection)
    {
      super(clazz, collection);
    }
  };
  public static class StudyColumns extends EntityCollection
  {
    public StudyColumns(Class clazz, Collection collection)
    {
      super(clazz, collection, EntityOutputType.ENTITY_CONTENTS);
    }
  };

  public static class ScreenColumns extends EntityCollection
  {
    public ScreenColumns(Collection collection)
    {
      super(DataColumn.class, collection, EntityOutputType.ENTITY_CONTENTS);
    }
  };

  public static class Reagents extends EntityCollection
  {
    public Reagents(Collection collection)
    {
      super(Reagent.class, collection);
    }
  };
}
