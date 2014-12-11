// $HeadURL:
// http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/2920-rev2/core/src/main/java/edu/harvard/med/screensaver/test/TestDataFactory.java
// $
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.regex.Pattern;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.ManagedType;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.MolarUnit;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.activities.ServiceActivityType;
import edu.harvard.med.screensaver.model.activities.TypedActivity;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.cells.Cell;
import edu.harvard.med.screensaver.model.cells.CellLineage;
import edu.harvard.med.screensaver.model.cells.PrimaryCell;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayProtocolsFollowed;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickFollowupResultsStatus;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransferStatus;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.SmallMoleculeCherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.ConcentrationStatistics;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryScreeningStatus;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.MolecularFormula;
import edu.harvard.med.screensaver.model.libraries.NaturalProductReagent;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Quadrant;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Solvent;
import edu.harvard.med.screensaver.model.libraries.StockPlateMapping;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.ConfirmedPositiveValue;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayProtocolType;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.AssayType;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenAttachedFileType;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenStatus;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.screens.Species;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.AffiliationCategory;
import edu.harvard.med.screensaver.model.users.ChecklistItemGroup;
import edu.harvard.med.screensaver.model.users.FacilityUsageRole;
import edu.harvard.med.screensaver.model.users.Gender;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.UserAttachedFileType;
import edu.harvard.med.screensaver.test.model.meta.DomainModelDefinitionException;
import edu.harvard.med.screensaver.test.model.meta.ModelIntrospectionUtil;
import edu.harvard.med.screensaver.util.DevelopmentException;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * Generates <i>persisted</i> instances of domain model entities. Use {@link #newInstance(Class)} to generate an entity
 * of the
 * specified type. If the entity has required relationships with other entities, these other entities will be generated
 * as well (e.g., if the requested entity type has a parent, the parent will be generated and associated).
 * <p/>
 * All entity property values will be set to arbitrary values, unless a custom {@link Builder}, {@link PreCreateHook},
 * or {@link PostCreateHook} has been configured to modify the default creation behavior. See
 * {@link #addBuilder(Builder)}, {@link #addPreCreateHook(Class, PreCreateHook)}, and
 * {@link #addPostCreateHook(Class, PostCreateHook)}.
 * <p/>
 * TODO:
 * <ul>
 * <li>describe "call stack"</li>
 * <li>describe patterns for entity instantiation customization, using Builders and {Pre,Post}CreateHooks.</li>
 * 
 * @author atolopko
 */
public class TestDataFactory
{
  private static Logger log = Logger.getLogger(TestDataFactory.class);

  private static String STRING_TEST_VALUE_PREFIX = "test-";
  private static int STRING_TEST_VALUE_RADIX = 36;

  private GenericEntityDAO genericEntityDao;
  private EntityManagerFactory entityManagerFactory;

  /**
   * A hook into a Builder's object instantiation process that allows customization of instantiation arguments prior to
   * instantiating the object.
   */
  public interface PreCreateHook<T>
  {
    void preCreate(String callStack, Object[] args);
  }

  /**
   * A hook into a Builder's object instantiation process that allows customization of the instantiated object, after it
   * has been instantiated.
   */
  public interface PostCreateHook<T>
  {
    void postCreate(String callStack, T o);
  }

  /**
   * Instantiates an instance of a specific entity type, if the Builder is {@link #isApplicable(String) applicable} to
   * the current "call stack".
   */
  public interface Builder<T>
  {
    Class<T> getTargetClass();

    boolean isApplicable(String callStack);

    Builder<T> addPreCreateHook(PreCreateHook<T> preCreateHook);

    Builder<T> addPostCreateHook(PostCreateHook<T> postCreateHook);

    T newInstance(String callStack);
  }

  public abstract static class AbstractBuilder<T> implements Builder<T>
  {
    private Class<T> type;
    private Pattern pattern;
    protected List<PreCreateHook<T>> preCreateHooks = Lists.newLinkedList();
    protected List<PostCreateHook<T>> postCreateHooks = Lists.newLinkedList();

    public AbstractBuilder(Class<T> type)
    {
      this(type, "");
    }

    public AbstractBuilder(Class<T> type,
                           String patternSuffix)
    {
      this.type = type;
      if (StringUtils.isEmpty(patternSuffix)) {
        this.pattern = Pattern.compile(Pattern.quote(type.getName()) + "(\\|.*)?");
      }
      else {
        this.pattern = Pattern.compile(Pattern.quote(type.getName() + "|") + patternSuffix);
      }
    }

    public boolean isApplicable(String callStack)
    {
      return pattern.matcher(callStack).matches();
    }

    @Override
    public Class<T> getTargetClass()
    {
      return type;
    }

    public Builder<T> addPreCreateHook(PreCreateHook<T> preCreateHook)
    {
      preCreateHooks.add(preCreateHook);
      return this;
    }

    public Builder<T> addPostCreateHook(PostCreateHook<T> postCreateHook)
    {
      postCreateHooks.add(postCreateHook);
      return this;
    }

    protected void applyPreCreateHooks(String callStack, Object[] args)
    {
      for (PreCreateHook<T> preCreateHook : preCreateHooks) {
        preCreateHook.preCreate(callStack, args);
      }
    }

    protected void applyPostCreateHooks(String callStack, T newEntity)
    {
      for (PostCreateHook<T> postCreateHook : postCreateHooks) {
        postCreateHook.postCreate(callStack, newEntity);
      }
    }

    @Override
    public String toString()
    {
      return type.getName() + ":" + pattern;
    }
  }

  public static abstract class AbstractEntityBuilder<T extends AbstractEntity> extends AbstractBuilder<T>
  {
    protected GenericEntityDAO dao;
    protected TestDataFactory dataFactory;

    public AbstractEntityBuilder(Class<T> type,
                                 GenericEntityDAO dao,
                                 TestDataFactory dataFactory)
    {
      this(type, "", dao, dataFactory);
    }

    public AbstractEntityBuilder(Class<T> type,
                                 String patternSuffix,
                                 GenericEntityDAO dao,
                                 TestDataFactory dataFactory)
    {
      super(type, patternSuffix);
      this.dao = dao;
      this.dataFactory = dataFactory;
    }

    protected Object[] getArgumentsForParameterTypes(String callStack, Class[] parameterTypes, String methodName) throws DomainModelDefinitionException
    {
      Object[] arguments = new Object[parameterTypes.length];
      for (int i = 0; i < arguments.length; i++) {
        Class<?> parameterType = parameterTypes[i];
        Object arg = arguments[i] = dataFactory.newInstance(parameterType, newCallStack(methodName, i, callStack));
        if (arg instanceof AbstractEntity) {
          dao.persistEntity((AbstractEntity) arg);
          log.debug("persisted " + arg);
        }
      }
      return arguments;
    }

  }

  public static class EntityBuilder<T extends AbstractEntity> extends AbstractEntityBuilder<T>
  {
    private Constructor cstr;

    public EntityBuilder(Class<T> entityClass,
                         GenericEntityDAO dao,
                         TestDataFactory dataFactory)
    {
      this(entityClass, findMaxArgConstructor(entityClass), "", dao, dataFactory);
    }

    public EntityBuilder(Class<T> entityClass,
                         Constructor cstr,
                         String patternSuffix,
                         GenericEntityDAO dao,
                         TestDataFactory dataFactory)
    {
      super(entityClass, patternSuffix, dao, dataFactory);
      this.cstr = cstr;
    }

    @Override
    public T newInstance(String callStack)
    {
      try {
        Object[] args = getArgumentsForParameterTypes(callStack, cstr.getParameterTypes(), cstr.getName());
        applyPreCreateHooks(callStack, args);
        T newEntity = (T) cstr.newInstance(args);
        log.debug("constructed new entity " + newEntity + " with args " + Arrays.asList(args));
        applyPostCreateHooks(callStack, newEntity);
        dao.persistEntity(newEntity);
        return newEntity;
      }
      catch (Exception e) {
        throw new DomainModelDefinitionException("could not invoke constructor " + cstr.getName(), e);
      }
    }
  }

  /**
   * PreCreateHook that for parented entities that provides access to the instantiated parent, allowing the parent to be
   * modified before the child is instantiated and/or to access the parent's properties if they are needed to generate
   * arguments for child instantiation.
   */
  public static abstract class ParentedPreCreateHook<T,P extends AbstractEntity> implements PreCreateHook<T>
  {
    protected P parent;

    public void setParent(P parent)
    {
      this.parent = parent;
    }

    @Override
    public void preCreate(String callStack, Object[] args)
    {}
  }

  public static class ParentedEntityBuilder<C extends AbstractEntity,P extends AbstractEntity> extends AbstractEntityBuilder<C>
  {
    private Class<P> parentClass;
    private Method instantiationMethod;

    public ParentedEntityBuilder(Class<C> entityClass,
                                 GenericEntityDAO dao,
                                 TestDataFactory dataFactory)
    {
      this(entityClass, findParentFactoryMethod(entityClass), "", dao, dataFactory);
    }

    public ParentedEntityBuilder(Class<C> entityClass,
                                 Method instantiationMethod,
                                 String patternSuffix,
                                 GenericEntityDAO dao, TestDataFactory dataFactory)
    {
      super(entityClass, patternSuffix, dao, dataFactory);
      parentClass = (Class<P>) ModelIntrospectionUtil.getParent(entityClass);
      this.instantiationMethod = instantiationMethod;
    }

    @Override
    public C newInstance(String callStack)
    {
      try {
        P parent = newParent(callStack);
        Object[] args = getArgumentsForParameterTypes(callStack, instantiationMethod.getParameterTypes(), instantiationMethod.getName());
        applyPreCreateHooks(callStack, args, parent);
        C newEntity = (C) instantiationMethod.invoke(parent, args);
        log.debug("constructed new entity (via parent entity's factory method) " + newEntity + " with args " +
          Arrays.asList(args));
        applyPostCreateHooks(callStack, newEntity);
        dao.persistEntity(parent);
        assert !newEntity.isTransient() : "persisting of parent should have cascaded to child";
        return newEntity;
      }
      catch (Exception e) {
        throw new DomainModelDefinitionException("could not invoke parent bean factory method " +
                                                 parentClass.getName() + "." + instantiationMethod.getName(), e);
      }
    }

    protected P newParent(String callStack)
    {
      P parent = dataFactory.newInstance(parentClass, callStack);
      return parent;
    }

    protected void applyPreCreateHooks(String callStack, Object[] args, P parent)
    {
      for (PreCreateHook<C> preCreateHook : preCreateHooks) {
        if (preCreateHook instanceof ParentedPreCreateHook) {
          ((ParentedPreCreateHook<C,P>) preCreateHook).setParent(parent);
        }
        preCreateHook.preCreate(callStack, args);
      }
    }
  }

  private Integer _integerTestValue = 77;
  private double _doubleTestValue = 77.1;
  private boolean _booleanTestValue = true;
  private Character _characterTestValue = 'a';
  private int _stringTestValueIndex = Integer.parseInt("antz", STRING_TEST_VALUE_RADIX);
  private long _dateMilliseconds = new LocalDate(2008, 3, 10).toDateMidnight().getMillis();
  private int _vocabularyTermCounter = 0;
  private int _wellNameTestValueIndex = 0;
  private WellKey _wellKeyTestValue = new WellKey("00001:A01");

  private Multimap<Class,Builder> builders = ArrayListMultimap.create();

  protected TestDataFactory()
  {}

  public TestDataFactory(GenericEntityDAO dao,
                         EntityManagerFactory entityManagerFactory) throws SecurityException, NoSuchMethodException
  {
    this.genericEntityDao = dao;
    this.entityManagerFactory = entityManagerFactory;
    initializeBuilders();
  }

  private void initializeBuilders() throws NoSuchMethodException
  {
    // add builders for abstract entity types, which map to concrete entity types
    addBuilder(new AbstractBuilder<ScreensaverUser>(ScreensaverUser.class) {
      @Override
      public ScreensaverUser newInstance(String callStack)
      {
        return TestDataFactory.this.newInstance(AdministratorUser.class, callStack);
      }
    });
    addBuilder(new AbstractBuilder<CherryPickRequest>(CherryPickRequest.class) {
      @Override
      public CherryPickRequest newInstance(String callStack)
      {
        return TestDataFactory.this.newInstance(SmallMoleculeCherryPickRequest.class, callStack);
      }
    });
    addBuilder(new AbstractBuilder<TypedActivity>(TypedActivity.class) {
      @Override
      public TypedActivity newInstance(String callStack)
      {
        return TestDataFactory.this.newInstance(AdministrativeActivity.class, callStack);
      }
    });
//    addBuilder(new AbstractBuilder<Cell>(Cell.class) {
//      @Override
//      public Cell newInstance(String callStack)
//      {
//        return TestDataFactory.this.newInstance(PrimaryCell.class, callStack);
//      }
//    });
    addBuilder(new AbstractBuilder<LabActivity>(LabActivity.class) {
      @Override
      public LabActivity newInstance(String callStack)
      {
        return TestDataFactory.this.newInstance(LibraryScreening.class, callStack);
      }
    });
    addBuilder(new AbstractBuilder<Screening>(Screening.class) {
      @Override
      public Screening newInstance(String callStack)
      {
        return TestDataFactory.this.newInstance(LibraryScreening.class, callStack);
      }
    });
    addBuilder(new AbstractBuilder<Reagent>(Reagent.class) {
      @Override
      public Reagent newInstance(String callStack)
      {
        return TestDataFactory.this.newInstance(SmallMoleculeReagent.class, callStack);
      }
    });
    addBuilder(new AbstractEntityBuilder<AttachedFileType>(AttachedFileType.class, genericEntityDao, this) {
      @Override
      public AttachedFileType newInstance(String callStack)
      {
        if (callStack.contains(ScreeningRoomUser.class.getName())) {
          return new UserAttachedFileType("x");
        }
        return new ScreenAttachedFileType("y");
      }
    });

    addBuilder(new AbstractBuilder<Integer>(Integer.class) {
      @Override
      public Integer newInstance(String callStack)
      {
        _integerTestValue += 1;
        return _integerTestValue;
      }
    });
    addBuilder(new AbstractBuilder<Integer>(Integer.TYPE) {
      @Override
      public Integer newInstance(String callStack)
      {
        _integerTestValue += 1;
        return _integerTestValue;
      }
    });
    addBuilder(new AbstractBuilder<Double>(Double.class) {
      @Override
      public Double newInstance(String callStack)
      {
        _doubleTestValue *= 1.32;
        return new Double(new Double(_doubleTestValue * 1000).intValue() / 1000);
      }
    });
    addBuilder(new AbstractBuilder<Double>(Double.TYPE) {
      @Override
      public Double newInstance(String callStack)
      {
        _doubleTestValue *= 1.32;
        return new Double(new Double(_doubleTestValue * 1000).intValue() / 1000);
      }
    });
    addBuilder(new AbstractBuilder<Boolean>(Boolean.class) {
      @Override
      public Boolean newInstance(String callStack)
      {
        _booleanTestValue = !_booleanTestValue;
        return Boolean.valueOf(_booleanTestValue);
      }
    });
    addBuilder(new AbstractBuilder<Boolean>(Boolean.TYPE) {
      @Override
      public Boolean newInstance(String callStack)
      {
        _booleanTestValue = !_booleanTestValue;
        return Boolean.valueOf(_booleanTestValue);
      }
    });
    addBuilder(new AbstractBuilder<BigDecimal>(BigDecimal.class) {
      @Override
      public BigDecimal newInstance(String callStack)
      {
        BigDecimal val = null;
        // TODO: we should be able to generalize these special case BigDecimal builders by inspecting the model annotations for precision and scale and then construct a BigDecimal that is appropriate for the test
        if (callStack.contains("MlConcentration")) {
          // mgMlConcentration has a max precision 3, scale 5
          val = new BigDecimal((TestDataFactory.this.newInstance(Double.class)).doubleValue() / _doubleTestValue);
          val = val.setScale(3, RoundingMode.HALF_UP);
        }
        else if (callStack.contains("Dilution")) {
          // dilution factor has a max precision 8, scale 2
          val = new BigDecimal((TestDataFactory.this.newInstance(Double.class)).doubleValue() / (_doubleTestValue / 10));
          val = val.setScale(2, RoundingMode.HALF_UP);
        }
        else {
          // 2 is the default scale used in our Hibernate mapping
          val = new BigDecimal((TestDataFactory.this.newInstance(Double.class)).doubleValue());
          val = val.setScale(2);
        }
        return val;
      }
    });
    addBuilder(new AbstractBuilder<Volume>(Volume.class) {
      @Override
      public Volume newInstance(String callStack)
      {
        return new Volume(TestDataFactory.this.newInstance(Integer.class), VolumeUnit.DEFAULT);
      }
    });
    addBuilder(new AbstractBuilder<MolarConcentration>(MolarConcentration.class) {
      @Override
      public MolarConcentration newInstance(String callStack)
      {
        return new MolarConcentration(TestDataFactory.this.newInstance(Integer.class));
      }
    });    
    addBuilder(new AbstractBuilder<String>(String.class) {
      @Override
      public String newInstance(String callStack)
      {
        return STRING_TEST_VALUE_PREFIX + Integer.toString(++_stringTestValueIndex, STRING_TEST_VALUE_RADIX);
      }
    });
    addBuilder(new AbstractBuilder<Character>(Character.class) {
      @Override
      public Character newInstance(String callStack)
      {
        return _characterTestValue++;
      }
    });
    addBuilder(new AbstractBuilder<Character>(Character.TYPE) {
      @Override
      public Character newInstance(String callStack)
      {
        return _characterTestValue++;
      }
    });
    addBuilder(new AbstractBuilder<LocalDate>(LocalDate.class) {
      @Override
      public LocalDate newInstance(String callStack)
      {
        _dateMilliseconds += 1000 * 60 * 60 * 24 * 1.32;
        return new LocalDate(_dateMilliseconds);
      }
    });
    addBuilder(new AbstractBuilder<DateTime>(DateTime.class) {
      @Override
      public DateTime newInstance(String callStack)
      {
        _dateMilliseconds += 1000 * 60 * 60 * 24 * 1.32;
        return new DateTime(_dateMilliseconds);
      }
    });

    addBuilder(new AbstractBuilder<WellKey>(WellKey.class) {
      @Override
      public WellKey newInstance(String callStack)
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
    });
    addBuilder(new AbstractBuilder<WellName>(WellName.class) {
      @Override
      public WellName newInstance(String callStack)
      {
        return new WellName(TestDataFactory.this.newInstance(WellKey.class).getWellName());
      }
    });
    addBuilder(new AbstractBuilder<ReagentVendorIdentifier>(ReagentVendorIdentifier.class) {
      @Override
      public ReagentVendorIdentifier newInstance(String callStack)
      {
        return new ReagentVendorIdentifier(TestDataFactory.this.newInstance(String.class),
                                           TestDataFactory.this.newInstance(String.class));
      }
    });
    addBuilder(new AbstractBuilder<MolecularFormula>(MolecularFormula.class) {
      @Override
      public MolecularFormula newInstance(String callStack)
      {
        return new MolecularFormula("CH3M2");
      }
    });
    addBuilder(new AbstractBuilder<StockPlateMapping>(StockPlateMapping.class) {
      @Override
      public StockPlateMapping newInstance(String callStack)
      {
        return new StockPlateMapping(TestDataFactory.this.newInstance(Plate.class, callStack).getPlateNumber(),
                                     TestDataFactory.this.newInstance(Quadrant.class, callStack));
      }
    });
    addBuilder(new AbstractBuilder<ConcentrationStatistics>(ConcentrationStatistics.class) {
      @Override
      public ConcentrationStatistics newInstance(String callStack)
      {
        return new ConcentrationStatistics(MolarConcentration.makeConcentration("1", MolarUnit.MILLIMOLAR),MolarConcentration.makeConcentration("10", MolarUnit.MILLIMOLAR), MolarConcentration.makeConcentration("1", MolarUnit.MILLIMOLAR), 
                                           new BigDecimal("1.000"), new BigDecimal("10.000"), new BigDecimal("1.000"));
      }
    });
    addBuilder(new AbstractBuilder<Quadrant>(Quadrant.class) {
      @Override
      public Quadrant newInstance(String callStack)
      {
        return Quadrant.values()[TestDataFactory.this.newInstance(Integer.class, callStack) % Quadrant.values().length];
      }
    });
    addBuilder(new AbstractBuilder<ScreenType>(ScreenType.class) {
      @Override
      public ScreenType newInstance(String callStack)
      {
        return ScreenType.SMALL_MOLECULE;
      }
    });
    addBuilder(new AbstractBuilder<LibraryType>(LibraryType.class) {
      @Override
      public LibraryType newInstance(String callStack)
      {
        return LibraryType.COMMERCIAL;
      }
    });
    addBuilder(new AbstractBuilder<Species>(Species.class) {
      @Override
      public Species newInstance(String callStack)
      {
        return Species.OTHER;
      }
    });
    addBuilder(new AbstractBuilder<AssayType>(AssayType.class) {
      @Override
      public AssayType newInstance(String callStack)
      {
        return AssayType.BIOCHEMICAL;
      }
    });
    addBuilder(new AbstractBuilder<PlateSize>(PlateSize.class) {
      @Override
      public PlateSize newInstance(String callStack)
      {
        return PlateSize.WELLS_384;
      }
    });
    addBuilder(new AbstractBuilder<ScreeningRoomUserClassification>(ScreeningRoomUserClassification.class) {
      @Override
      public ScreeningRoomUserClassification newInstance(String callStack)
      {
        return ScreeningRoomUserClassification.UNASSIGNED;
      }
    });
    addBuilder(new AbstractBuilder<AffiliationCategory>(AffiliationCategory.class) {
      @Override
      public AffiliationCategory newInstance(String callStack)
      {
        return AffiliationCategory.HMS;
      }
    });
    addBuilder(new AbstractBuilder<LibraryWellType>(LibraryWellType.class) {
      @Override
      public LibraryWellType newInstance(String callStack)
      {
        return LibraryWellType.UNDEFINED;
      }
    });
    addBuilder(new AbstractBuilder<StudyType>(StudyType.class) {
      @Override
      public StudyType newInstance(String callStack)
      {
        return StudyType.IN_SILICO;
      }
    });
    addBuilder(new AbstractBuilder<ProjectPhase>(ProjectPhase.class) {
      @Override
      public ProjectPhase newInstance(String callStack)
      {
        return ProjectPhase.PRIMARY_SCREEN;
      }
    });
    addBuilder(new AbstractBuilder<StatusItem>(StatusItem.class) {
      @Override
      public StatusItem newInstance(String callStack)
      {
        return new StatusItem(new LocalDate(), ScreenStatus.ONGOING);
      }
    });
    addBuilder(new AbstractBuilder<PlateType>(PlateType.class) {
      @Override
      public PlateType newInstance(String callStack)
      {
        return PlateType.ABGENE;
      }
    });
    addBuilder(new AbstractBuilder<PlateStatus>(PlateStatus.class) {
      @Override
      public PlateStatus newInstance(String callStack)
      {
        return PlateStatus.AVAILABLE;
      }
    });
    addBuilder(new AbstractBuilder<CherryPickLiquidTransferStatus>(CherryPickLiquidTransferStatus.class) {
      @Override
      public CherryPickLiquidTransferStatus newInstance(String callStack)
      {
        return CherryPickLiquidTransferStatus.SUCCESSFUL;
      }
    });
    addBuilder(new AbstractBuilder<AssayProtocolType>(AssayProtocolType.class) {
      @Override
      public AssayProtocolType newInstance(String callStack)
      {
        return AssayProtocolType.ESTABLISHED;
      }
    });
    addBuilder(new AbstractBuilder<FacilityUsageRole>(FacilityUsageRole.class) {
      @Override
      public FacilityUsageRole newInstance(String callStack)
      {
        return FacilityUsageRole.SMALL_MOLECULE_SCREENER;
      }
    });
    addBuilder(new AbstractBuilder<AssayReadoutType>(AssayReadoutType.class) {
      @Override
      public AssayReadoutType newInstance(String callStack)
      {
        return AssayReadoutType.FP;
      }
    });
    addBuilder(new AbstractBuilder<CopyUsageType>(CopyUsageType.class) {
      @Override
      public CopyUsageType newInstance(String callStack)
      {
        return CopyUsageType.LIBRARY_SCREENING_PLATES;
      }
    });
    addBuilder(new AbstractBuilder<CherryPickAssayProtocolsFollowed>(CherryPickAssayProtocolsFollowed.class) {
      @Override
      public CherryPickAssayProtocolsFollowed newInstance(String callStack)
      {
        return CherryPickAssayProtocolsFollowed.SAME_PROTOCOL_AS_PRIMARY_ASSAY;
      }
    });
    addBuilder(new AbstractBuilder<CherryPickFollowupResultsStatus>(CherryPickFollowupResultsStatus.class) {
      @Override
      public CherryPickFollowupResultsStatus newInstance(String callStack)
      {
        return CherryPickFollowupResultsStatus.NOT_RECEIVED;
      }
    });
    addBuilder(new AbstractBuilder<ScreenDataSharingLevel>(ScreenDataSharingLevel.class) {
      @Override
      public ScreenDataSharingLevel newInstance(String callStack)
      {
        return ScreenDataSharingLevel.SHARED;
      }
    });
    addBuilder(new AbstractBuilder<AdministrativeActivityType>(AdministrativeActivityType.class) {
      @Override
      public AdministrativeActivityType newInstance(String callStack)
      {
        return AdministrativeActivityType.ENTITY_UPDATE;
      }
    });
    addBuilder(new AbstractBuilder<SilencingReagentType>(SilencingReagentType.class) {
      @Override
      public SilencingReagentType newInstance(String callStack)
      {
        return SilencingReagentType.SIRNA;
      }
    });
    addBuilder(new AbstractBuilder<ScreenStatus>(ScreenStatus.class) {
      @Override
      public ScreenStatus newInstance(String callStack)
      {
        return ScreenStatus.ACCEPTED;
      }
    });
    addBuilder(new AbstractBuilder<ChecklistItemGroup>(ChecklistItemGroup.class) {
      @Override
      public ChecklistItemGroup newInstance(String callStack)
      {
        return ChecklistItemGroup.FORMS;
      }
    });
    addBuilder(new AbstractBuilder<LibraryScreeningStatus>(LibraryScreeningStatus.class) {
      @Override
      public LibraryScreeningStatus newInstance(String callStack)
      {
        return LibraryScreeningStatus.ALLOWED;
      }
    });
    addBuilder(new AbstractBuilder<Solvent>(Solvent.class) {
      @Override
      public Solvent newInstance(String callStack)
      {
        if (callStack.matches("Library\\|.*SilencingReagent")) {
          return Solvent.RNAI_BUFFER;
        }
        return Solvent.DMSO;
      }
    });
    addBuilder(new AbstractBuilder<ConfirmedPositiveValue>(ConfirmedPositiveValue.class) {
      @Override
      public ConfirmedPositiveValue newInstance(String callStack)
      {
        return ConfirmedPositiveValue.CONFIRMED_POSITIVE;
      }
    });
    addBuilder(new AbstractBuilder<PartitionedValue>(PartitionedValue.class) {
      @Override
      public PartitionedValue newInstance(String callStack)
      {
        return PartitionedValue.STRONG;
      }
    });
    addBuilder(new AbstractBuilder<ServiceActivityType>(ServiceActivityType.class) {
      @Override
      public ServiceActivityType newInstance(String callStack)
      {
        return ServiceActivityType.INFORMATICS;
      }
    });
    addBuilder(new AbstractBuilder<Gender>(Gender.class) {
      @Override
      public Gender newInstance(String callStack)
      {
        return Gender.FEMALE;
      }
    });

    // special-case builder for Plate, which is auto-created by parent Copy
    addBuilder(new AbstractEntityBuilder<Plate>(Plate.class, genericEntityDao, this) {
      @Override
      public Plate newInstance(String callStack)
      {
        Copy copy = TestDataFactory.this.newInstance(Copy.class, callStack);
        Plate plate = copy.findPlate(copy.getLibrary().getStartPlate());
        applyPostCreateHooks(callStack, plate);
        return plate;
      }
    });

    // special-case builder for Gene, which is auto-created by parent SilencingReagent
    addBuilder(new AbstractEntityBuilder<Gene>(Gene.class, genericEntityDao, this) {
      @Override
      public Gene newInstance(String callStack)
      {
        SilencingReagent silencingReagent = TestDataFactory.this.newInstance(SilencingReagent.class, callStack);
        return silencingReagent.getVendorGene();
      }
    });
    
    addBuilder(new AbstractBuilder<Cell>(Cell.class) {
      @Override
      public Cell newInstance(String callStack)
      {
        Cell cell =  TestDataFactory.this.newInstance(CellLineage.class, callStack);
        return cell;
      }
    });
    
    
    // create special-case builders for Reagent types, in order to choose the correct parent factory method (the one w/o the updateReagentsRelationship param)
    // TODO: add these as spring beans which are then injected into TestDataFactory, thereby moving these special-case, domain model-specific builders into a deploy-time configurable location
    addBuilder(new ParentedEntityBuilder<SilencingReagent,Well>(SilencingReagent.class,
                                                                Well.class.getMethod("createSilencingReagent", new Class[] {
                                                                  ReagentVendorIdentifier.class, SilencingReagentType.class,
                                                                  String.class }), "", genericEntityDao, this)
                                                                  .addPostCreateHook(new PostCreateHook<SilencingReagent>() {

                                                                    @Override
                                                                    public void postCreate(String callStack, SilencingReagent sr)
                                                                {
                                                                  // instantiate both gene types since this must be done before the SMR is persisted.
                                                                    Gene gene = sr.getFacilityGene()
                                                                      .withEntrezgeneId(1) // values used by GeneTest
                                                                      .withGeneName("genename")
                                                                      .withSpeciesName("species")
                                                                      .withEntrezgeneSymbol("symbol1")
                                                                      .withEntrezgeneSymbol("symbol2")
                                                                      .withGenbankAccessionNumber("gbn1")
                                                                      .withGenbankAccessionNumber("gbn2");
                                                                    sr.getVendorGene();
                                                                  }
                                                                  }));
    addBuilder(new ParentedEntityBuilder<SmallMoleculeReagent,Well>(SmallMoleculeReagent.class,
                                                                    Well.class.getMethod("createSmallMoleculeReagent", new Class[] {
                                                                      ReagentVendorIdentifier.class,
                                                                      String.class,
                                                                      String.class,
                                                                      String.class,
                                                                      BigDecimal.class,
                                                                      BigDecimal.class,
                                                                      MolecularFormula.class }), "", genericEntityDao, this));
    addBuilder(new ParentedEntityBuilder<NaturalProductReagent,Well>(NaturalProductReagent.class,
                                                                     Well.class.getMethod("createNaturalProductReagent", new Class[] {
                                                                                          ReagentVendorIdentifier.class }), "", genericEntityDao, this));

    addBuilder(new ParentedEntityBuilder<Well,Library>(Well.class,
                                                                     Library.class.getMethod("createWell", new Class[] {
                                                                       WellKey.class, LibraryWellType.class }), "", genericEntityDao, this)
                                                                                          .addPostCreateHook(new PostCreateHook<Well>() {

                                                                                            @Override
                                                                                            public void postCreate(String callStack,
                                                                                                                   Well o)
                                                                                            {
                                                                                              o.setMgMlConcentration(new BigDecimal("99.9"));
                                                                                               
                                                                                            }
                                                                                          }));
    Builder<ResultValue> numericResultValueBuilder =
      new ParentedEntityBuilder<ResultValue,DataColumn>(ResultValue.class,
                                                        DataColumn.class.getMethod("createResultValue",
                                                                                   new Class[] { AssayWell.class, Double.class }),
                                                        "",
                                                        genericEntityDao, this) {
        protected DataColumn newParent(String callStack)
      {
        // unfortunately, we can't just call super.newParent(), as this causes the new DataColumn to be persisted before its data type is set, which in turn causes Hibernate to set the (immutable) data type to an arbitrary value
        return dataFactory.newInstance(ScreenResult.class, callStack).createDataColumn(dataFactory.newInstance(String.class, callStack)).makeNumeric(3);
      }
      };
    addBuilder(numericResultValueBuilder);

    addDefaultEntityBuilders();

    addPostCreateHook(CellLineage.class,new PostCreateHook<CellLineage>() {

		@Override
			public void postCreate(String callStack, CellLineage o) {
				o.setFacilityId("1"); // TODO: hack - sde4
			}
    });

    addPostCreateHook(Well.class, new PostCreateHook<Well>() {
      /** ensure that a well has a reagent if we're ultimately instantiating a LabCherryPick */
      public void postCreate(String callStack, Well well)
      {
        if (callStack.contains(LabCherryPick.class.getName())) {
          Library library = well.getLibrary();
          assert !library.getContentsVersions().isEmpty();
          Class<? extends Reagent> reagentType = library.getReagentType();
          ReagentVendorIdentifier rvi = newInstance(ReagentVendorIdentifier.class);
          if (reagentType.equals(SilencingReagent.class)) {
            well.createSilencingReagent(newInstance(ReagentVendorIdentifier.class),
                                        SilencingReagentType.SIRNA, "ACTG");
          }
          else if (reagentType.equals(SmallMoleculeReagent.class)) {
            well.createSmallMoleculeReagent(rvi,
                                            "molfile",
                                            "smiles",
                                            "inchi",
                                            BigDecimal.ONE,
                                            BigDecimal.ONE,
                                            new MolecularFormula());
          }
          else if (reagentType.equals(NaturalProductReagent.class)) {
            well.createNaturalProductReagent(rvi);
          }
          else {
            throw new DevelopmentException("unhandled reagent type " + reagentType);
          }
          AdministrativeActivity releaseActivity =
            new AdministrativeActivity(newInstance(AdministratorUser.class),
                                       new LocalDate(),
                                       AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE);
          library.getLatestContentsVersion().release(releaseActivity);
        }
      }
    }).addPreCreateHook(new ParentedPreCreateHook<Well,Library>() {
      /** ensures well plate numbers agree with library start/end plate */
      public void preCreate(String callStack, Object[] args)
      {
        args[0] = new WellKey(parent.getStartPlate(), 0, 0);
        args[1] = LibraryWellType.EXPERIMENTAL;
      }
    });
    addPostCreateHook(Library.class, new PostCreateHook<Library>() {
      /** ensure that a LibraryContentsVersion is created for Reagents that are to be instantiated */
      public void postCreate(String callStack, Library o)
      {
        if (callStack.contains("Reagent") || callStack.contains("LabCherryPick")) {
          o.createContentsVersion(newInstance(AdministratorUser.class));
        }
      }
    }).addPreCreateHook(new PreCreateHook<Library>() {
      @Override
      public void preCreate(String callStack, Object[] args)
    {
      if (callStack.contains("SmallMoleculeReagent")) {
        args[3] = ScreenType.SMALL_MOLECULE;
        args[4] = LibraryType.COMMERCIAL;
      }
      else if (callStack.contains("SilencingReagent")) {
        args[3] = ScreenType.RNAI;
        args[4] = LibraryType.COMMERCIAL;
      }
      else if (callStack.contains("NaturalProductReagent")) {
        args[3] = ScreenType.SMALL_MOLECULE;
        args[4] = LibraryType.NATURAL_PRODUCTS;
      }
    }
    });

    addPreCreateHook(RNAiCherryPickRequest.class,
                     new ParentedPreCreateHook<RNAiCherryPickRequest,Screen>()
                     {
                       @Override
                       public void setParent(Screen parent)
      {
        super.setParent(parent);
        parent.setScreenType(ScreenType.RNAI);
      }
                     });
    addPreCreateHook(SmallMoleculeCherryPickRequest.class,
                     new ParentedPreCreateHook<SmallMoleculeCherryPickRequest,Screen>()
                     {
                       @Override
                       public void setParent(Screen parent)
      {
        super.setParent(parent);
        parent.setScreenType(ScreenType.SMALL_MOLECULE);
      }
                     });

    addPreCreateHook(AnnotationValue.class,
                     new ParentedPreCreateHook<AnnotationValue,AnnotationType>()
                     {
                       @Override
                       public void preCreate(String callStack, Object[] args)
      {
        if (parent.isNumeric()) {
          args[1] = newInstance(Double.class, callStack).toString();
        }
        else {
          args[1] = newInstance(String.class, callStack);
        }
      }
                     });
    addPreCreateHook(SmallMoleculeReagent.class,
                     new PreCreateHook<SmallMoleculeReagent>()
                     {

                       @Override
                       public void preCreate(String callStack, Object[] args)
                      {
                        args[4] = new BigDecimal("1000.001");
                        args[5] = new BigDecimal("1.001");
                      }
                     });
  }

  /**
   * Adds an entity builder for every entity type that does not already have a builder (in other words, a builder that
   * is added for a entity type before this method is called will be the entity type's default builder).
   */
  private void addDefaultEntityBuilders()
  {
    for (ManagedType<?> managedType : entityManagerFactory.getMetamodel().getManagedTypes()) {
      Class<?> managedClass = managedType.getJavaType();
      if (AbstractEntity.class.isAssignableFrom(managedClass)) {
        if (Modifier.isAbstract(managedClass.getModifiers())) {
          continue;
        }
        Class<? extends AbstractEntity> entityClass = (Class<? extends AbstractEntity>) managedClass;
        Class<? extends AbstractEntity> parentClass = ModelIntrospectionUtil.getParent(entityClass);
        if (!!!builders.containsKey(entityClass)) {
          if (parentClass != null) {
            addBuilder(new ParentedEntityBuilder(entityClass, genericEntityDao, this));
          }
          else {
            addBuilder(new EntityBuilder(entityClass, genericEntityDao, this));
          }
        }
      }
    }
  }

  public void addBuilder(Builder<?> builder)
  {
    builders.put(builder.getTargetClass(), builder);
    log.debug("added builder " + builder);
  }

  public <T> Builder<T> addPreCreateHook(Class<T> type, PreCreateHook<T> preCreateHook)
  {
    return ((Builder<T>) Iterables.getLast(builders.get(type))).addPreCreateHook(preCreateHook);
  }

  public <T> Builder<T> addPostCreateHook(Class<T> type, PostCreateHook<T> postCreateHook)
  {
    return ((Builder<T>) Iterables.getLast(builders.get(type))).addPostCreateHook(postCreateHook);
  }

  /**
   * Instantiate a new, persisted AbstractEntity of the specified type, instantiating all
   * required, related entities, as necessary.
   * 
   * @param <T> the type of the new AbstractEntity to be instantiated
   * @param type the class of the new AbstractEntity to be instantiated
   * @return a new, persisted AbstractEntity of the specified type
   * @throws DomainModelDefinitionException
   */
  @Transactional
  public <T> T newInstance(Class<T> type) throws DomainModelDefinitionException
  {
    return newInstance(type, "");
  }

  /**
   * Instantiate a new, persisted AbstractEntity of the specified type, instantiating all
   * required, related entities, as necessary. The <code>callStack</code> argument allows a custom frame to be specified
   * by TestDataFactory client code, so that custom {@link Builder}s (that are added by the client code) can be detect
   * whether they should be applied to this newInstance request.
   * 
   * @param <T> the type of the new AbstractEntity to be instantiated
   * @param type the class of the new AbstractEntity to be instantiated
   * @param callStack the initial (bottom) frame (or frames) of the callStack
   * @return a new, persisted AbstractEntity of the specified type
   * @throws DomainModelDefinitionException
   */
  @Transactional
  public <T> T newInstance(Class<T> type, String callStack) throws DevelopmentException
  {
    log.debug("newInstance(" + type.getName() + ", " + callStack + ")");
    callStack = newCallStack(type, callStack);
    if (!!!builders.containsKey(type)) {
      throw new DevelopmentException("no builder for type " + type);
    }
    for (Builder<T> builder : Iterables.reverse(Lists.newArrayList(builders.get(type)))) {
      if (builder.isApplicable(callStack)) {
        return builder.newInstance(callStack);
      }
    }
    throw new DevelopmentException("none of the builders for type " + type + " were applicable");
  }

  public static String newCallStack(Class<?> type)
  {
    return newCallStack(type, "");
  }

  public static String newCallStack(Class<?> type, String callStack)
  {
    return newCallStack(type.getName(), callStack);
  }

  public static String newCallStack(String methodName, int argIndex, String callStack)
  {
    return newCallStack(methodName + ":" + argIndex, callStack);
  }

  public static String newCallStack(String newFrame, String callStack)
  {
    if (callStack.isEmpty()) {
      return newFrame;
    }
    return newFrame + "|" + callStack;
  }

  protected static Constructor findMaxArgConstructor(Class<? extends AbstractEntity> entityClass)
  {
    int maxArgs = -1;
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

  protected static Method findParentFactoryMethod(Class<? extends AbstractEntity> entityClass)
  {
    ContainedEntity containedEntity = entityClass.getAnnotation(ContainedEntity.class);
    if (containedEntity == null) {
      throw new DomainModelDefinitionException("to find factory methods, @ContainedEntity annotation must exist in class " +
        entityClass.getName());
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
      throw new DomainModelDefinitionException("no 'create' factory method exists for " + entityClass + " in parent " +
        parentClass);
    }

    log.debug("chose candidate factory method " + candidateFactoryMethods.first());
    return candidateFactoryMethods.first();
  }

  /**
   * Reset the TestDataFactory, clearing all custom {@link Builder}s, {@link PreCreateHook}s
   * and {@link PostCreateHook}s.
   * 
   * @throws DevelopmentException
   */
  public void resetToDefaults() throws DevelopmentException
  {
    builders.clear();
    try {
      initializeBuilders();
    }
    catch (NoSuchMethodException e) {
      throw new DevelopmentException(e.getMessage());
    }
  }
}
