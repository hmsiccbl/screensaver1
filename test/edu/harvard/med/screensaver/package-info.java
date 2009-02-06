package edu.harvard.med.screensaver;

/**
 * Most of the Screensaver unit tests run within the context of Spring, and so
 * inherit from {@link AbstractSpringTest}, which initializes Spring. A large
 * set of tests require the availability of a test database, and so inherit from
 * {@link AbstractSpringPersistenceTest}.
 * {@link edu.harvard.med.screensaver.model.AbstractEntityInstanceTest} is a
 * represents the "automated entity model test framework". Each model entity
 * class requires a corresponding test class that inherits from
 * AbstractEntityInstanceTest.
 * {@link edu.harvard.med.screensaver.ui.AbstractJsfUnitTest} is used to run
 * automated user interface tests against the running web application, and must
 * be run within Tomcat; it is based upon JBoss' <a
 * href="http://www.jboss.org/jsfunit/gettingstarted.html">JSFUnit</a> testing
 * framework, which in turn is based upon Cactus.
 */
