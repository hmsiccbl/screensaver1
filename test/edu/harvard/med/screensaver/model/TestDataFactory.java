
// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Blob;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.lob.ReaderInputStream;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.SmallMoleculeCherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.MolecularFormula;
import edu.harvard.med.screensaver.model.libraries.NaturalProductReagent;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenAttachedFileType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

public class TestDataFactory
{
  private static Logger log = Logger.getLogger(TestDataFactory.class);
  
  private static String STRING_TEST_VALUE_PREFIX = "test-";
  private static int STRING_TEST_VALUE_RADIX = 36;

  private Integer _integerTestValue = 77;
  private double  _doubleTestValue = 77.1;
  private boolean _booleanTestValue = true;
  private Character _characterTestValue = 'a';
  private int     _stringTestValueIndex = Integer.parseInt("antz", STRING_TEST_VALUE_RADIX);
  private long    _dateMilliseconds = new LocalDate(2008, 3, 10).toDateMidnight().getMillis();
  private int     _vocabularyTermCounter = 0;
  private int     _wellNameTestValueIndex = 0;
  private WellKey _wellKeyTestValue = new WellKey("00001:A01");

  @SuppressWarnings("unchecked")
  public <T> T getTestValueForType(Class<T> type) throws DomainModelDefinitionException
  {
    if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
      _integerTestValue += 1;
      return (T) _integerTestValue;
    }
    if (type.equals(Double.class)) {
      _doubleTestValue *= 1.32;
      return (T) new Double(new Double(_doubleTestValue * 1000).intValue() / 1000);
    }
    if (type.equals(BigDecimal.class)) {
      BigDecimal val = new BigDecimal(((Double) getTestValueForType(Double.class)).doubleValue());
      // 2 is the default scale used in our Hibernate mapping
      val = val.setScale(2);
      return (T) val;
    }
    if (type.equals(Volume.class)) {
      Volume val = new Volume(((Integer) getTestValueForType(Integer.class)).longValue());
      return (T) val;
    }
    if (type.equals(MolarConcentration.class)) {
      MolarConcentration val = new MolarConcentration(((Integer) getTestValueForType(Integer.class)).longValue());
      return (T) val;
    }
    if (type.equals(Boolean.class)) {
      _booleanTestValue = ! _booleanTestValue;
      return (T) Boolean.valueOf(_booleanTestValue);
    }
    if (type.equals(Boolean.TYPE)) {
      _booleanTestValue = ! _booleanTestValue;
      return (T) Boolean.valueOf(_booleanTestValue);
    }
    if (type.equals(String.class)) {
      return (T) getStringTestValue();
    }
    if (type.equals(Blob.class)) {
      return (T) Hibernate.createBlob(getStringTestValue().getBytes());
    }
    if (type.equals(Character.class)) {
      _characterTestValue++;
      return (T) _characterTestValue;
    }
    if (type.equals(LocalDate.class)) {
      _dateMilliseconds += 1000 * 60 * 60 * 24 * 1.32;
      return (T) new LocalDate(_dateMilliseconds);
    }
    if (type.equals(DateTime.class)) {
      _dateMilliseconds += 1000 * 60 * 60 * 24 * 1.32;
      return (T) new DateTime(_dateMilliseconds);
    }
    if (AbstractEntity.class.isAssignableFrom(type)) {
      return (T) newInstance((Class<AbstractEntity>) type);
    }
    if (VocabularyTerm.class.isAssignableFrom(type)) {
      // TODO: move these special cases to appropriate TestDataFactory.EntityFactory classes
      if (type.equals(AssayWellControlType.class)) {
        return (T) AssayWellControlType.ASSAY_CONTROL;
      }
      if (type.equals(LibraryWellType.class)) {
        return (T) LibraryWellType.LIBRARY_CONTROL;
      }
      if (type.equals(ScreeningRoomUserClassification.class)) {
        // avoid selecting SRUC.PRINCIPAL_INVESTIGATOR, since this value cannot be used for non-lab head ScreeningRoomUsers
        return (T) ScreeningRoomUserClassification.ICCBL_NSRB_STAFF;
      }
      Method valuesMethod = null; 
      try {
        valuesMethod = type.getMethod("values");
        Object values = (Object) valuesMethod.invoke(null);
        int numValues = Array.getLength(values);
        int valuesIndex = ++ _vocabularyTermCounter % numValues;
        return (T) Array.get(values, valuesIndex);
      }
      catch (Exception e) {
        throw new DomainModelDefinitionException("could not invoke " + valuesMethod, e);
      }
    }
    if (WellKey.class.isAssignableFrom(type)) {
      return (T) nextWellKey();
    }
    if (WellName.class.isAssignableFrom(type)) {
      return (T) new WellName(nextWellKey().getWellName());
    }
    if (ReagentVendorIdentifier.class.isAssignableFrom(type)) {
      return (T) new ReagentVendorIdentifier(getStringTestValue(), getStringTestValue());
    }
    if (MolecularFormula.class.isAssignableFrom(type)) {
      return (T) new MolecularFormula("CH3M2");
    }
    if (InputStream.class.isAssignableFrom(type)) {
      return (T) new ReaderInputStream(new StringReader(getTestValueForType(String.class)));
    }
    throw new IllegalArgumentException(
      "can't create test values for type: " + type.getName());
  }

  private String getStringTestValue()
  {
    return STRING_TEST_VALUE_PREFIX + Integer.toString(++_stringTestValueIndex, STRING_TEST_VALUE_RADIX);
  }

  protected WellKey nextWellKey()
  {
    int col = _wellKeyTestValue.getColumn() + 1;
    int row = _wellKeyTestValue.getRow();
    int plateNumber = _wellKeyTestValue.getPlateNumber();
    if (col >= ScreensaverConstants.DEFAULT_PLATE_SIZE.getColumns()) {
      col = 0;
      ++row;
    }
    if (row >= ScreensaverConstants.DEFAULT_PLATE_SIZE.getRows()) {
      row = 0;
      ++plateNumber;
    }
    _wellKeyTestValue = new WellKey(plateNumber, row, col);
    return _wellKeyTestValue;
  }

  private Object getTestValueForWellName()
  {
    String wellName = String.format("%c%02d",
                                    'A' + (_wellNameTestValueIndex / 24),
                                    (_wellNameTestValueIndex % 24) + 1);
    ++_wellNameTestValueIndex;
    return wellName;
  }

  private interface EntityFactory
  {
    AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException;
  }

  private Map<Class<? extends AbstractEntity>,EntityFactory> _entityFactoryMap =
    new HashMap<Class<? extends AbstractEntity>,EntityFactory>();
  {
    _entityFactoryMap.put(ScreensaverUser.class, new EntityFactory()
    {
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        return newInstanceViaConstructor(ScreeningRoomUser.class, null);
      }
    });

    _entityFactoryMap.put(Activity.class, new EntityFactory()
    {
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        return newInstance(LibraryScreening.class);
      }
    });
    
    _entityFactoryMap.put(LabActivity.class, new EntityFactory()
    {
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        return newInstance(LibraryScreening.class);
      }
    });

    _entityFactoryMap.put(Screening.class, new EntityFactory()
    {
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        return newInstance(LibraryScreening.class);
      }
    });
    
    _entityFactoryMap.put(CherryPickRequest.class, new EntityFactory()
    {
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        return newInstance(SmallMoleculeCherryPickRequest.class);
      }
    });
    
    _entityFactoryMap.put(SmallMoleculeCherryPickRequest.class, new EntityFactory()
    {
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        Screen screen = newInstance(Screen.class);
        screen.setScreenType(ScreenType.SMALL_MOLECULE);
        CherryPickRequest cpr = screen.createCherryPickRequest((AdministratorUser) screen.getCreatedBy());
        cpr.setTransferVolumePerWellApproved(new Volume("2.0", VolumeUnit.MICROLITERS));
        return cpr;
      }
    });
    
    _entityFactoryMap.put(RNAiCherryPickRequest.class, new EntityFactory()
    {
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        Screen screen = newInstance(Screen.class);
        screen.setScreenType(ScreenType.RNAI);
        CherryPickRequest cpr = screen.createCherryPickRequest((AdministratorUser) screen.getCreatedBy());
        cpr.setTransferVolumePerWellApproved(new Volume("2.0", VolumeUnit.MICROLITERS));
        return cpr;
      }
    });
    
    _entityFactoryMap.put(Gene.class, new EntityFactory()
    {
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        SilencingReagent silencingReagent = newInstance(SilencingReagent.class);
        return silencingReagent.getFacilityGene();
      }
    });
 
    _entityFactoryMap.put(Reagent.class, new EntityFactory()
    {
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        return newInstance(SmallMoleculeReagent.class);
      }
    });

    _entityFactoryMap.put(SilencingReagent.class, new EntityFactory()
    {
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        LibraryContentsVersion contentsVersion = newInstance(LibraryContentsVersion.class);
        Library library = contentsVersion.getLibrary();
        Well well = library.createWell(new WellKey(1, 0, 0), LibraryWellType.EXPERIMENTAL);
        well.getLibrary().setScreenType(ScreenType.RNAI);
        SilencingReagent silencingReagent = well.createSilencingReagent(new ReagentVendorIdentifier("vendor1", "reagent1"), SilencingReagentType.SIRNA, "AGCT");
        return silencingReagent;
      }
    });
  
    _entityFactoryMap.put(SmallMoleculeReagent.class, new EntityFactory()
    {
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        LibraryContentsVersion contentsVersion = newInstance(LibraryContentsVersion.class);
        Library library = contentsVersion.getLibrary();
        library.setScreenType(ScreenType.SMALL_MOLECULE);
        Well well = library.createWell(new WellKey(1, 0, 0), LibraryWellType.EXPERIMENTAL);
        SmallMoleculeReagent smallMoleculeReagent = well.createSmallMoleculeReagent(new ReagentVendorIdentifier("vendor1", "reagent1"), "", "smiles", "inchi", new BigDecimal("1.001"), new BigDecimal("1.001"), new MolecularFormula("CH1"));
        return smallMoleculeReagent;
      }
    });
  
    _entityFactoryMap.put(NaturalProductReagent.class, new EntityFactory()
    {
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        LibraryContentsVersion contentsVersion = newInstance(LibraryContentsVersion.class);
        Library library = contentsVersion.getLibrary();
        library.setScreenType(ScreenType.SMALL_MOLECULE);
        library.setLibraryType(LibraryType.NATURAL_PRODUCTS);
        Well well = library.createWell(new WellKey(1, 0, 0), LibraryWellType.EXPERIMENTAL);
        NaturalProductReagent naturalProductReagent = well.createNaturalProductReagent(new ReagentVendorIdentifier("vendor1", "reagent1"));
        return naturalProductReagent;
      }
    });
 
    _entityFactoryMap.put(LabCherryPick.class, new EntityFactory()
    {
      private int testEntrezGeneId = 0;
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        CherryPickRequest cherryPickRequest = (CherryPickRequest) getTestValueForType(RNAiCherryPickRequest.class);
        Well well = (Well) getTestValueForType(Well.class);
        LibraryContentsVersion contentsVersion = newInstance(LibraryContentsVersion.class, well.getLibrary());
        well.getLibrary().setScreenType(ScreenType.RNAI);
        well.setLibraryWellType(LibraryWellType.EXPERIMENTAL);
        ReagentVendorIdentifier rvi = new ReagentVendorIdentifier("vendor", "atcg");
        SilencingReagent reagent = well.createSilencingReagent(rvi, SilencingReagentType.SIRNA, "ATCG");
        reagent.getVendorGene().withEntrezgeneId(testEntrezGeneId).withEntrezgeneSymbol("entrezSymbol" + testEntrezGeneId).withSpeciesName("Human");
        contentsVersion.release(new AdministrativeActivity(newInstance(AdministratorUser.class),
                                                           new LocalDate(),
                                                           AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE));
        ScreenerCherryPick screenerCherryPick;
        if (cherryPickRequest.getScreenerCherryPicks().isEmpty()) {
          screenerCherryPick = (ScreenerCherryPick)
          cherryPickRequest.createScreenerCherryPick(well);
        }
        else {
          screenerCherryPick = cherryPickRequest.getScreenerCherryPicks().iterator().next();
        }
        LabCherryPick labCherryPick = screenerCherryPick.createLabCherryPick(well);
        return labCherryPick;
      }
    });

    _entityFactoryMap.put(ResultValue.class, new EntityFactory() {
      public AbstractEntity createEntity(AbstractEntity relatedEntity)
        throws DomainModelDefinitionException
      {
        DataColumn col = newInstance(DataColumn.class).makeNumeric(3);
        AssayWell assayWell = col.getScreenResult().createAssayWell(newInstance(Well.class));
        return col.createResultValue(assayWell, getTestValueForType(Double.class));
      }
    });
    
    _entityFactoryMap.put(DataColumn.class, new EntityFactory() {
      public AbstractEntity createEntity(AbstractEntity relatedEntity)
        throws DomainModelDefinitionException
      {
        // force usage of numeric DataColumn, so that numeric properties can be tested (which also covers non-numeric DataColumn behaviors)
        DataColumn dataColumn = newInstance(DataColumn.class, newInstance(ScreenResult.class)).makeNumeric(3);
        return dataColumn;
      }
    });
    
    _entityFactoryMap.put(AnnotationValue.class, new EntityFactory() 
    {
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        // force usage of numeric AnnotationType, so that numeric properties can be tested
        Screen study = newInstanceViaConstructor(Screen.class, null);
        AnnotationType at = study.createAnnotationType("at", "", true);
        Reagent reagent = newInstance(Reagent.class);
        return at.createAnnotationValue(reagent, "1.01");
      }
    });
    
    _entityFactoryMap.put(AttachedFileType.class, new EntityFactory() 
    {
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        return newInstance(ScreenAttachedFileType.class);
      }
    });

    _entityFactoryMap.put(AttachedFile.class, new EntityFactory() 
    {
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        Screen screen = newInstance(Screen.class);
        try {
          return screen.createAttachedFile("filename_" + getTestValueForType(String.class),
                                           new ScreenAttachedFileType(getStringTestValue()),
                                           "filecontents_" + getTestValueForType(String.class));
        }
        catch (IOException e) {
          throw new DomainModelDefinitionException(e);
        }
      }
    });

    _entityFactoryMap.put(Plate.class, new EntityFactory() 
    {
      public AbstractEntity createEntity(AbstractEntity relatedEntity) throws DomainModelDefinitionException
      {
        Copy copy = newInstance(Copy.class);
        Plate plate = copy.findPlate(copy.getLibrary().getStartPlate());
        plate.setWellVolume(new Volume(10));
        return plate;
      }
    });
  }

  /**
   * Instantiate a new AbstractEntity of the specified type, instantiating all
   * required, related entities, as necessary.
   * 
   * @param <T> the type of the new AbstractEntity to be instantiated
   * @param entityClass the class of the new AbstractEntity to be instantiated
   * @return a new AbstractEntity of the specified type
   * @throws DomainModelDefinitionException
   */
  public <T extends AbstractEntity> T newInstance(Class<T> entityClass) throws DomainModelDefinitionException
  {
    return newInstance(entityClass, null);
  }

  /**
   * Instantiate a new AbstractEntity of the specified type, instantiating all
   * required, related entities, as necessary, and associating the new entity
   * with the <code>relatedEntity</code>, which is assumed to be required in the
   * constructor or factory method used to instantiate the new entity. If the
   * new entity is instantiated via a parent factory method, and if
   * <code>relatedEntity</code> is the same type as the parent type, then the
   * factory method is called from <code>relatedEntity</code> instance.
   * 
   * @param <T> the type of the new AbstractEntity to be instantiated
   * @param entityClass the class of the new AbstractEntity to be instantiated
   * @param relatedEntity an entity with which the newly instantiated entity
   *          should be associated, either by passing it in the constructor or
   *          factory method, or by calling this entity's factory method, if
   *          applicable; may be null
   * @return a new AbstractEntity of the specified type
   * @throws DomainModelDefinitionException
   */
  public <NE extends AbstractEntity> NE newInstance(Class<NE> entityClass, AbstractEntity relatedEntity) throws DomainModelDefinitionException
  {
    if (relatedEntity == null) {
      // use special-case EntityFactory, if defined for this entity class
      EntityFactory entityFactory = _entityFactoryMap.get(entityClass);
      if (entityFactory != null) {
        log.debug("using custom entity factory for " + entityClass.getName());
        return (NE) entityFactory.createEntity(null);
      }
    }
    
    if (entityClass.getAnnotation(ContainedEntity.class) != null) {
      return newInstanceUsingParentFactory(entityClass, relatedEntity);
    }
    else {
      return newInstanceViaConstructor(entityClass, relatedEntity);
    }
  }

  /**
   * Instantiates an AbstractEntity, using a factory method from a related,
   * parent bean. The optional relatedEntity arg will get passed into the
   * factory method, and does not have to be the parent entity; it just needs to
   * be a related entity that is set via the factory method. If it is the same
   * type as the parent entity, then it is used as the parent entity from which
   * the factory method is called.
   * 
   * @param <NE>
   * @param entityClass
   * @param relatedEntity
   * @return
   * @throws DomainModelDefinitionException
   */
  private <NE extends AbstractEntity> NE newInstanceUsingParentFactory(Class<NE> entityClass, AbstractEntity relatedEntity) 
    throws DomainModelDefinitionException
  {
    Method instantiationMethod = getFactoryMethod(entityClass);
    Class<? extends AbstractEntity> instantiatingClass = (Class<? extends AbstractEntity>) instantiationMethod.getDeclaringClass();
    AbstractEntity instantiatingBean = null;
    if (instantiatingClass.isInstance(relatedEntity)) {
      instantiatingBean = relatedEntity;
      // if related is the instantiating class, then it is *not* used as arg to
      // the factory method
      relatedEntity = null; 
    }
    else {
      instantiatingBean = newInstance(instantiatingClass);
      log.debug("using newly instantiated parent " + instantiatingBean + " for new contained entity " + entityClass);
    }
    try {
      Object[] arguments = getArgumentsForFactoryMethod(instantiationMethod, relatedEntity);
      NE newEntity = (NE) instantiationMethod.invoke(instantiatingBean, arguments);
      log.debug("constructed new entity (via parent entity's factory method) " + newEntity + " with args " + Arrays.asList(arguments));
      return newEntity;
    }
    catch (Exception e) {
      throw new DomainModelDefinitionException("could not invoke parent bean factory method " + 
                                               instantiatingClass.getName() + "." + instantiationMethod.getName(), e);
    }
  }

  /**
   * Instantiates an AbstractEntity, using a public constructor. The optional
   * relatedEntity arg will get passed into the constructor.
   * 
   * @param <NE>
   * @param entityClass
   * @param relatedEntity
   * @return
   * @throws DomainModelDefinitionException
   */
  private <NE extends AbstractEntity> NE newInstanceViaConstructor(Class<NE> entityClass, AbstractEntity relatedEntity) throws DomainModelDefinitionException
  {
    Constructor constructor = getMaxArgConstructor(entityClass);
    Object[] arguments = getArgumentsForConstructor(constructor, relatedEntity);
    try {
      NE newEntity = (NE) constructor.newInstance(arguments);
      log.debug("constructed new entity " + newEntity + " with args " + Arrays.asList(arguments));
      return newEntity;
    }
    catch (Exception e) {
      throw new DomainModelDefinitionException("could not invoke constructor " + entityClass.getName() + "." + constructor.getName(), e);
    }
  }

  private Object[] getArgumentsForConstructor(Constructor constructor, AbstractEntity parentBean) throws DomainModelDefinitionException
  {
    Class[] parameterTypes = constructor.getParameterTypes();
    Object[] arguments = getArgumentsForParameterTypes(parameterTypes, parentBean);
    return arguments;
  }

  private Object[] getArgumentsForFactoryMethod(Method method, AbstractEntity relatedEntity) throws DomainModelDefinitionException
  {
    Class[] parameterTypes = method.getParameterTypes();
    // TODO: move these special cases to a client-code-configurable map
    if (method.getName().equals("createPlatesUsed")) {
      Copy copy = newInstance(Copy.class);
      return new Object [] {
        copy.getLibrary().getStartPlate(),
        copy.getLibrary().getEndPlate(),
        copy
      };
    }
    if (method.getName().equals("createAnnotationValue") &&
      parameterTypes.length == 2 &&
      parameterTypes[0].equals(Reagent.class) &&
      parameterTypes[1].equals(String.class)) {
      parameterTypes[1] = BigDecimal.class;
      Object[] arguments = getArgumentsForParameterTypes(parameterTypes, relatedEntity);
      arguments[1] = arguments[1].toString();
      return arguments;
    }
    if (method.getName().equals("createLabCherryPick")) {
      LabCherryPick lcp = newInstance(LabCherryPick.class);
      return new Object[] {lcp.getSourceWell()};
    }
    Object[] arguments = getArgumentsForParameterTypes(parameterTypes, relatedEntity);
    return arguments;
  }

  protected Object[] getArgumentsForParameterTypes(Class[] parameterTypes, AbstractEntity relatedEntity) throws DomainModelDefinitionException
  {
    Object [] arguments = new Object[parameterTypes.length];
    boolean usedRelatedEntity = false;
    for (int i = 0; i < arguments.length; i++) {
      Class<?> parameterType = parameterTypes[i];
      if (relatedEntity != null && parameterType.isAssignableFrom(relatedEntity.getClass())) {
        if (usedRelatedEntity) {
          throw new DomainModelDefinitionException("multiple argument types for " + relatedEntity);
        }
        log.debug("related entity " + relatedEntity + 
                  " being used as argument for instantiating method parameter " + 
                  (i + 1) + " of type " + parameterType.getName());
        arguments[i] = relatedEntity;
        usedRelatedEntity = true;
      }
      else {
        arguments[i] = getTestValueForType(parameterType);
      }
    }
    if (relatedEntity != null && !usedRelatedEntity) {
      throw new DomainModelDefinitionException("expected an argument type for " + relatedEntity);
    }
    return arguments;
  }

  protected Constructor getMaxArgConstructor(Class<? extends AbstractEntity> entityClass)
  {
    int maxArgs = 0;
    Constructor maxArgConstructor = null;
    for (Constructor constructor : entityClass.getConstructors()) {
      if (Modifier.isPublic(constructor.getModifiers())) {
        int numArgs = constructor.getParameterTypes().length;
        if (numArgs > maxArgs) {
          maxArgs = numArgs;
          maxArgConstructor = constructor;
        }
      }
    }
    if (maxArgConstructor == null) {
      throw new DomainModelDefinitionException("no public constructor in entity " + entityClass.getName());
    }
    return maxArgConstructor;
  }

  protected Method getFactoryMethod(Class<? extends AbstractEntity> entityClass)
  {
    ContainedEntity containedEntity = entityClass.getAnnotation(ContainedEntity.class);
    if (containedEntity == null) {
      throw new DomainModelDefinitionException("to find factory methods, @ContainedEntity annotation must exist in class " + entityClass.getName());
    }
    Class<? extends AbstractEntity> parentClass = containedEntity.containingEntityClass();
    SortedSet<Method> candidateFactoryMethods = Sets.newTreeSet(new Comparator<Method>() {
      public int compare(Method method1, Method method2)
      {
        Class<?> class1 = method1.getReturnType();
        Class<?> class2 = method2.getReturnType();
        if (class1.isAssignableFrom(class2) && !class2.isAssignableFrom(class1)) {
          // class1 is the more specific type
          return 1;
        }
        if (!class1.isAssignableFrom(class2) && class2.isAssignableFrom(class1)) {
          // class2 is the more specific type
          return -1;
        }
        if (method1.getParameterTypes().length < method2.getParameterTypes().length) {
          return 1;
        }
        if (method1.getParameterTypes().length > method2.getParameterTypes().length) {
          return -1;
        }
        return 0;
      }
    });
    for (Method method : parentClass.getMethods()) {
      if (method.getName().startsWith("create") &&
        method.getReturnType().isAssignableFrom(entityClass)) {
        candidateFactoryMethods.add(method);
      }
    }
    log.debug("candidate factory methods for " + entityClass + " in parent " + parentClass + ": " + candidateFactoryMethods);
    if (candidateFactoryMethods.isEmpty()) {
      throw new DomainModelDefinitionException("no 'create' factory method exists for " + entityClass + " in parent " + parentClass);
    }
    
    log.debug("chose candidate factory method " + candidateFactoryMethods.first());
    return candidateFactoryMethods.first(); 
  }
  
}
