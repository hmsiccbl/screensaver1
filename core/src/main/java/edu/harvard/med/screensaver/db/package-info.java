/**
 * Provides interfaces/classes for interacting with the Screensaver database.
 * <p>
 * This package provides Data Access Object (DAO) classes, which encapsulate
 * (non-trivial) persistence-related tasks that operate on or return domain
 * model entity objects. Due to the use of Hibernate, an ORM/persistence
 * framework, most of the common DAO tasks (Create, Read, Update, Delete) can be
 * performed via {@link edu.harvard.med.screensaver.db.GenericEntityDAO}.
 * <p>
 * This package also provides classes to manage a database schema and to
 * determine the application's connection settings.
 */
package edu.harvard.med.screensaver.db;