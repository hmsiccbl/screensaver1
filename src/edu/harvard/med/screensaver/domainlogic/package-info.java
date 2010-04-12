/**
 * Encapsulates logic for updating domain entity properties that cannot
 * effectively be implemented within the domain model entity classes themselves,
 * due to technological constraints. Ideally, we would want our
 * edu.harvard.med.screensaver.model entity classes to be as "rich" as possible
 * in their behavior, but UI and ORM technological constraints prohibit this in
 * certain non-trivial cases:
 * <ol>
 * <li>JSF and Hibernate call entity setters in non-deterministic order, and
 * thus calculating derived entity properties that depend upon more than one
 * source property is ugly or inefficient.</li>
 * <li>Hibernate calls the same POJO entity setter methods that are called by
 * application code. Therefore setters cannot blindly invoke logic that requires
 * the in-memory object model to be in a consistent state. That is properties
 * and relationships that are depended upon by the setter's logic may not yet be
 * initialized. Having the setter detect whether it's being called by Hibernate
 * is an option, but breaks the POJO assumption.
 * <li>When a derived entity property cannot be efficiently computed via the
 * in-memory entity object model, but rather requires the database to perform
 * the calculation. For example, computing an aggregate value from a large
 * collection of entities. This case implies that it is allowable for classes in
 * the package to depend upon the edu.havard.med.screensaver.db.*DAO classes.
 * Contrast this with the edu.harvard.med.screensaver.model POJO entity classes,
 * which should never have such DAO or persistence-layer dependencies.
 * </ol>
 * To overcome these problems, we implement our model's non-trivial domain logic
 * outside of the entity classes themselves, in separate {@link edu.harvard.med.screensaver.domainlogic.EntityUpdater}
 * classes. The logic in these classes will be invoked at the appropriate times
 * to keep the model consistent with its invariants. This logic is to be
 * considered part of the domain model. Contrast this with the service layer,
 * which contains business logic that is "above" the domain model and which is
 * concerned with higher-level application functions, and which may not be
 * universally applicable to all applications that depend upon this domain
 * model.
 * <p>
 * Architectural overview:
 * <ul>
 * <li>Domain logic should be implemented in classes within this package that
 * implement the {@link edu.harvard.med.screensaver.domainlogic.EntityUpdater} interface.</li>
 * <li>All EntityUpdater classes must be registered in
 * spring-context-persistence.xml by adding to them to the
 * <code>entityUpdatersList</code> Spring bean.</li>
 * <li>The registered EntityUpdater classes are injected into every Entity that
 * is managed by Hibernate. This is performed by the
 * {@link edu.harvard.med.screensaver.domainlogic.EntityUpdatersInjector}, which makes use of Hibernate's event handler
 * mechanism. In this way, every entity maintains the domain logic it must
 * invoke to keep its state consistent with respect to its invariants.</li>
 * <li>When a domain entity class performs an operation that requires one or
 * more of its derived entity properties to be recomputed, the
 * {@link edu.harvard.med.screensaver.model.Entity#invalidate} method should be
 * called. This marks the entity as needing a re-computation of its derived
 * properties. At the "appropriate time", later on, the
 * {@link edu.harvard.med.screensaver.model.Entity#update} method must be
 * invoked to perform the necessary computations, as specified by its
 * EntityUpdater classes. The appropriate time to call
 * {@link edu.harvard.med.screensaver.model.Entity#update} is usually just
 * before a transaction is to be committed.</li>
 * </ul>
 */
package edu.harvard.med.screensaver.domainlogic;