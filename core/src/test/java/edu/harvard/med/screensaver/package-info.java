package edu.harvard.med.screensaver;

/**
 * Most of the Screensaver unit tests run within the context of Spring, and so
 * inherit from {@link AbstractSpringTest}, which initializes Spring. A large
 * set of tests require the availability of a test database, and so inherit from
 * {@link AbstractSpringPersistenceTest}.
 * {@link edu.harvard.med.screensaver.model.AbstractEntityInstanceTest} is our
 * automated domain model test framework, which tests that each entity's
 * properties and relationships are correctly persisted and loaded. Each domain model
 * entity class requires a corresponding test class that must inherit from
 * AbstractEntityInstanceTest.
 */
