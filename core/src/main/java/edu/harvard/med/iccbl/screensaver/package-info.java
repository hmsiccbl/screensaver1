package edu.harvard.med.iccbl.screensaver;

/**
 * Contains Screensaver classes and command-line applications that are designed
 * specifically for use by the ICCB-Longwood HTS facility. These Screensaver
 * classes may be useful to other facilities, but more often than not will
 * require adaptation or modification if used by other facilities.
 * <p>
 * Ideally, classes within this package that are used in the web application
 * should implement interfaces defined within/under the
 * edu.harvard.med.screensaver core package. The concrete classes in this
 * package should only be referenced within Spring configuration files, and
 * never directly by the Screensaver core classes. In other words, there should
 * be no direct compile-time dependencies upon classes in this package from the
 * core web application.
 */
