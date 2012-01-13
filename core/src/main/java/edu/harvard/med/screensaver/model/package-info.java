/**
 * Contains the entity classes that form the Screensaver domain model. The model is grouped into conceptual groups:  
 * 
 * <ul>
 * 
 * <li>{@link edu.harvard.med.screensaver.model.users}
 * <li>{@link edu.harvard.med.screensaver.model.libraries}
 * <li>{@link edu.harvard.med.screensaver.model.screens}
 * <li>{@link edu.harvard.med.screensaver.model.screenresults}
 * <li>{@link edu.harvard.med.screensaver.model.cherrypicks}
 * <li>{@link edu.harvard.med.screensaver.model.activities.Activity} (activity entities are dispersed throughout multiple model packages)
 * 
 * </ul>
 * 
 * <h3>Hibernate Annotations and the Java Persistence API</h3>
 * 
 * The domain model entity classes are mapped to a relational database schema using JPA 
 * annotations and Hibernate annotations (for extended mapping functionality). Screensaver also provides
 * its own set of domain model annotations in the {@link
 * edu.harvard.med.screensaver.model.annotations} package. These annotations
 * are mainly used to implement a comprehensive test suite of the model, which
 * tests for model correctness as thoroughly as possible.
 * 
 * <h3>Entity Identifiers</h3>
 * 
 * Domain model entity classes must implement the {@link edu.harvard.med.screensaver.model.Entity} interface and may extend 
 * {@link edu.harvard.med.screensaver.model.AbstractEntity}. Two different
 * methods are used for implementing entity ids for the entity classes:
 * 
 * <ol>
 * 
 * <li>Use a database sequence to generate entity id values. In this case, entities do not
 * get assigned an id until they are persisted to the database. Object methods
 * <code>equals(Object)</code> and <code>hashCode()</code> are not overridden by these
 * classes, so that
 * for the purpose of set containment, etc., object identity is used to compare objects.
 * For this reason, any two non-persisted entity objects are considered different. Also, any
 * two persisted entity objects are considered different, even if they have the same entity
 * id! However, note that Hibernate will assure that only one object is created for an
 * entity with a given type and entity id, <i>per session</i>. So care must be taken to
 * assure that the objects belong to the same session before comparing for identity in this
 * way. DAO methods {@link
 * edu.harvard.med.screensaver.db.GenericEntityDAO#reattachEntity(Entity)}, {@link
 * edu.harvard.med.screensaver.db.GenericEntityDAO#reloadEntity(Entity)}, and {@link
 * edu.harvard.med.screensaver.db.GenericEntityDAO#reloadEntity(Entity, boolean, edu.harvard.med.screensaver.model.meta.RelationshipPath)} may be found useful for
 * looking up a version of an entity that lives in the current session.
 * 
 * <li>Use a <i>semantic id</i>, or <i>business key</i>, as an id for the entity. In this
 * case, the entity should get assigned a stable id, as returned by {@link
 * edu.harvard.med.screensaver.model.AbstractEntity#getEntityId()}, at object creation time (ie, in the
 * constructor). These kinds of entities should inherit from {@link
 * edu.harvard.med.screensaver.model.SemanticIDAbstractEntity},
 * which has {@link
 * edu.harvard.med.screensaver.model.AbstractEntity} as a superclass.
 * <code>SemanticIDAbstractEntity</code>
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
 * First, the contained entity is annotated with {@link 
 * edu.harvard.med.screensaver.model.annotations.ContainedEntity}, which indicates
 * the containing class that is responsible for providing create*() methods for the contained entity.
 * 
 * <p>
 * 
 * For every containment relationship, there are one or more methods that match the pattern
 * <code>public Contained Container.createContained(...)</code>. For example, see
 * {@link edu.harvard.med.screensaver.model.libraries.Library#createWell(edu.harvard.med.screensaver.model.libraries.WellKey, edu.harvard.med.screensaver.model.libraries.LibraryWellType)},
 * This factory method is
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
 * <dd>In general, we use the Java type <code>Integer</code> to represent integral
 * values, and the default Hibernate type for this Java type. This converts to PostgreSQL
 * type <code>integer</code>. This type is used for version fields as well as for entity id
 * values generated by a database sequence.
 * 
 * <dt>floats
 * <dd>We use the Java type <code>BigDecimal</code> to represent rational values, and
 * explicitly set the Hibernate type to <code>big_decimal</code>. This converts to
 * PostgreSQL type <code>numeric(19,2)</code>. We always include the units of the value
 * in the property name for the sake of clarity.
 * 
 * <dt>controlled vocabularies
 * <dd>All controller vocabularies in our model are enums that implement the interface
 * {@link edu.harvard.med.screensaver.model.VocabularyTerm}. These enums all provide a public
 * static inner class that implements the abstract class {@link 
 * edu.harvard.med.screensaver.model.VocabularyUserType}; these inner classes become the
 * Hibernate type used for the controller vocabularies. More detail is provided in the class
 * documentation for {@link edu.harvard.med.screensaver.model.VocabularyUserType}. This rather
 * awkward system is forced upon us by a variety of
 * constraints: we want to use enumerations; the Java implementation of enumerations has
 * functional limitations (e.g., they can't <code>extend</code> an existing class or enum);
 * and the Hibernate requirements for user types are required to implement a somewhat
 * complex interface, <code>org.hibernate.usertype.UserType</code>. (In particular, the
 * restriction on Java enums, coupled with the Hibernate restrictions, prevent us from
 * using the same type as both the enumeration type and the Hibernate type, unless we provide
 * the boiler-plate code needed to implement <code>UserType</code> in every controlled
 * vocabulary type in our model.)
 * 
 * </dl>
 * 
 * <h3>Conventions for Relationships</h3>
 * 
 * Our model contains one-to-one, one-to-many, and many-to-many relationships. Some are
 * unidirectional and some are bidirectional. Some one-to-one and one-to-many relationships
 * are distinguished as containment relationships by the presence of a {@link
 * edu.harvard.med.screensaver.model.annotations.ContainedEntity} annotation on the child
 * class. We use proxy relationships whenever possible, and lazy relationships liberally,
 * preferring to specify which relationships should be loaded via arguments to the {@link
 * edu.harvard.med.screensaver.db.GenericEntityDAO DAO} methods for loading entities.
 * 
 * <h3>Miscellaneous Conventions</h3>
 * 
 * Generally speaking, all the conventions we have adopted are meant to serve our needs, mostly
 * by making our lives easier by having consistency throughout the model. This is helpful for
 * reading and understanding the model code, as well as for generating new model code, since
 * a copy/paste/edit routine is generally a pretty safe bet. But these are only conventions, and
 * if they are hindering us from accomplishing what we want in our model, then we will freely
 * ignore, trash, or re-convene them. An incomplete list of further miscellaneous conventions follows:
 * 
 * <dl>
 * 
 * <dt>naming of foreign keys
 * <dd>We provide names for foreign keys and other database constraints whenever Hibernate allows
 * it. We name foreign key constraints as <code>"fk_" + from_table_name + "_to_" + to_table_name</code>.
 * In situations where there are multiple properties representing relationships between the same two
 * tables, we replace the table names with the relationship names (converting from camel-case to
 * underscore-delimited).
 * 
 * </dl>
 */

package edu.harvard.med.screensaver.model;

