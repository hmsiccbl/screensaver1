
/**
 * Provides classes that implement the Screensaver object model. The model is broken out
 * into the following major sections, each in its own package:
 * 
 * <dl>
 * 
 * <dt>{@link edu.harvard.med.screensaver.model.cherrypicks cherrypicks}
 * <dd>Models the cherrypicks for a screen. Screeners choose their best and most interesting
 * hits for confirmation screens and other followup studies.
 * 
 * <dt>{@link edu.harvard.med.screensaver.model.derivatives derivatives}
 * <dd>Models compound derivatives produced by medicinal chemists, and the results of trial screens run by
 * medicinal chemists on these derivatives. This portion of the object model is most likely defunct, since there is
 * a tentative new approach for storing medicinal chemistry data. These derivatives are eventually going
 * to become new small molecules libraries, and the screen results performed by the medicinal chemists will probably
 * be modelled as {@link edu.harvard.med.screensaver.model.screen.Study Studies} on those libraries.
 * 
 * <dt>{@link edu.harvard.med.screensaver.model.libraries libraries}
 * <dd>Models all the information about the screening libraries, including their contents,
 * the various copies of the library plates stored in the screening room, and activities
 * performed on those copies.
 * 
 * <dt>{@link edu.harvard.med.screensaver.model.screenresults screenresults}
 * <dd>Models the results of the screens, including raw data and values derived from the
 * raw data.
 * 
 * <dt>{@link edu.harvard.med.screensaver.model.screens screens}
 * <dd>Models the screens themselves, tracking who was involved in the screens, administrative
 * information about the screens, including billing information, publications produced from
 * the screens, etc.
 * 
 * <dt>{@link edu.harvard.med.screensaver.model.users users}
 * <dd>Models all Screensaver users, including administrative users, screening room users,
 * as well as the authors of studies that are not actually screens.
 * 
 * </dl>
 * 
 * <h3>Hibernate Annotations and the Java Persistence API</h3>
 * 
 * The object model is implemented with Hibernate Annotations. This provides an
 * implementation of, plus custom extensions to, the Java Persistence API. We also provide
 * our own set of custom extensions using annotations in the {@link
 * edu.harvard.med.screensaver.model.annotations annotations} subpackage. Our custom
 * extensions are mainly used to implement a comprehensive test suite of the model, which
 * tests for model correctness as thoroughly as possible.
 * 
 * <h3>Entity Identifiers</h3>
 * 
 * All object model classes derive from {@link AbstractEntity}. See the Javadoc comment in
 * that class to learn more about what it provides for the entity subclasses. Two different
 * methods are used for implementing entity ids for the entity classes:
 * 
 * <ol>
 * 
 * <li>Use a database sequence to generate entity id values. In this case, entities do not
 * get assigned an id until they are persisted to the database. Object methods {@link
 * #equals(Object)} and {@link #hashCode()} are not overridden by these classes, so that
 * for the purpose of set containment, etc., object identity is used to compare objects.
 * For this reason, any two non-persisted entity objects are considered different. Also, any
 * two persisted entity objects are considered different, even if they have the same entity
 * id! However, note that Hibernate will assure that only one object is created for an
 * entity with a given type and entity id, <i>per session</i>. So care must be taken to
 * assure that the objects belong to the same session before comparing for identity in this
 * way. DAO methods {@link GenericEntityDAO#reattachEntity(AbstractEntity)}, {@link
 * GenericEntityDAO#reloadEntity(AbstractEntity)}, and {@link
 * GenericEntityDAO#reloadEntity(AbstractEntity, boolean, String[])} may be found useful for
 * looking up a version of an entity that lives in the current session.
 * 
 * <li>Use a <i>semantic id</i>, or <i>business key</i>, as an id for the entity. In this
 * case, the entity should get assigned a stable id, as returned by {@link
 * AbstractEntity#getEntityId()}, at object creation time (ie, in the
 * constructor). These kinds of entities should inherit from {@link SemanticIDAbstractEntity},
 * which has {@link AbstractEntity} as a superclass. <code>SemanticIDAbstractEntity</code>
 * overrides <code>equals</code> and <code>hashCode</code> methods based on the entity id.
 * Semantic ids are particularly useful if you need to be able to look up an existing entity
 * in an unflushed Hibernate session, since the only way to look up a newly created entity in
 * an unflushed session is by the entity id. In most cases, the decision to use a semantic
 * id is motivated by this consideration.
 * 
 * </ol>
 * 
 * <h3>Containment Relationships</h3>
 * 
 * We use a set of conventions for modelling containment, or ownership, relationships.
 * First, the contained entity is annotated with {@link ContainedEntity}, which indicates
 * the containing class. There is currently a little hackery in <code>ContainedEntity</code>
 * to allow for two different containing classes, to satisfy the special case of {@link
 * WellVolumeAdjustment}. If we every need to model more than two possible containers, or
 * if we end up with multiple entities that have two different containing classes, then we
 * will probably want to move from {@link ContainedEntity#containingEntityClass()} and
 * {@link ContainedEntity#alternateContainingEntityClass()} to <code>Class&lt;? extends
 * AbstractEntity&gt; [] ContainedEntity.containingClasses()</code>. But for now, this is just
 * an annoying and ugly special case.
 * 
 * <p>
 * 
 * For every containment relationship, there are one or more methods that match the pattern
 * <code>public Contained Container.createContained(...)</code>. For example, see
 * {@link Library#createWell(Integer, String)},
 * {@link Library#createWell(WellKey, WellType)},
 * and {@link Library#createWell(Integer, String, WellType)}. These factory methods are
 * responsible for adding the newly created entity into any bidirectional relationships,
 * almost always including the classes own
 * <code>public Set<Contained> Container.getContaineds()</code>. The factory methods are
 * also responsible for testing for duplicate entities. (These are responsibilities that
 * used to fall to the contained entity's constructor.) Besides the no-arg constructor
 * needed for Hibernate, all the contained entity's constructors are intended only for
 * these factory methods. Javadoc for the constructors always indicates this, and visibility
 * of the constructors is as restrictive as possible for the contained class to be able to
 * access it. (There are cases where the container and contained are in different packages,
 * so that the constructor has to be public.)
 * 
 * <p>
 * 
 * The <code>Contained.container</code> property is always
 * <code>&#64;javax.persistence.Immutable</code>, and it is always set in the constructor.
 * 
 * <h3>Conventions with Types</h3>
 * 
 * We try to be as consistent as possible with types (both Java types and Hibernate types).
 * We use the following guidelines for choosing types:
 * 
 * <dl>
 * 
 * <dt>booleans
 * <dd>We assume booleans are non-nullable, and prefer to use primitive Java type
 * <code>boolean</code> over <code>java.lang.Boolean</code>. For boolean property
 * <code>foo</code>, we name the getter <code>isFoo</code>, the setter <code>setFoo</code>,
 * and the database column <code>is_foo</code>. We provide <code>javax.persistence</code>
 * annotation <code>&#64;Column(nullable=false, name="isFoo")</code> to make the database
 * column <code>NOT NULL</code> and named <code>is_foo</code> instead of <code>foo</code>.
 * 
 * <dt>strings
 * <dd>We use the PostgreSQL database type <code>TEXT</code> wherever possible, since there
 * is no performance penalty for doing so, and it avoids ugly problems of
 * <code>VARCHAR</code>s not being large enough. We annotate with
 * <code>&#64;org.hibernate.annotations.Type(type="text")</code> to get this database type.
 * 
 * <dt>integers
 * <dd>TODO
 * 
 * <dt>floats
 * <dd>TODO
 * 
 * <dt>controlled vocabularies
 * <dd>TODO
 * 
 * </dl>
 * 
 * <h3>Miscellaneous Conventions</h3>
 * 
 * TODO:
 * - other conventions:
 *   - ordering of fields/methods
 *   - ordering/importing of annotations
 *   - naming of foreign keys?
 */
package edu.harvard.med.screensaver.model;


