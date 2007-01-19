// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Convenience class for instantiating a Screensaver-based command-line
 * application. The main purpose of this class is to house the Spring framework
 * bootstrapping code, so that developers can forget the details of how to do
 * this (and don't have to cut and paste code between various main() methods).
 * Also provides a some help with:
 * <ul>
 * <li>command-line argument parsing (including special-case handling of
 * database connection options)
 * <li>obtaining Spring-managed beans.
 * <ul>.
 * <p>
 * Normally, a screensaver distribution will use the database connection
 * settings specified in "classpath:screensaver.properties". However, if
 * {@link #processOptions(boolean, boolean)} is called with
 * acceptDatabaseOptions=true, the command line options 'dbhost', 'dbport',
 * 'dbname', 'dbuser', and 'dbpassword' will be used to configure the database
 * connection settings.
 * 
 * @author ant
 */
public class CommandLineApplication
{
  private static final Logger log = Logger.getLogger(CommandLineApplication.class);
  
  private static final String SPRING_CONFIG = "spring-context.xml";
  private static final String SPRING_CONFIG_SANS_DB = "spring-context-sans-db.xml";
  
  
  // instanc data
  
  private ApplicationContext _appCtx;
  private Options _options;
  private CommandLine _cmdLine;
  private String[] _cmdLineArgs;
  private Map<String,Object> _option2DefaultValue = new HashMap<String,Object>();
  private boolean _isDatabaseRequired;
  
  
  // public methods
  
  @SuppressWarnings("static-access")
  public CommandLineApplication(String[] cmdLineArgs)
  {
    _cmdLineArgs = cmdLineArgs;
    _options = new Options();
    _options.addOption(OptionBuilder.
                       withLongOpt("help").
                       withDescription("print this help").
                       create("h"));
  }
  

  public Object getSpringBean(String springBeanName)
  {
    return getSpringApplicationContext().getBean(springBeanName);
  }
  
  @SuppressWarnings("unchecked")
  public <T> T getSpringBean(String springBeanName, Class<T> ofType)
  {
    return (T) getSpringApplicationContext().getBean(springBeanName);
  }
  
  public ApplicationContext getSpringApplicationContext()
  {
    if (_appCtx == null) {
      if (isDatabaseRequired()) {
        _appCtx = new ClassPathXmlApplicationContext(SPRING_CONFIG);
      }
      else {
        _appCtx = new ClassPathXmlApplicationContext(SPRING_CONFIG_SANS_DB);
      }
    }
    return _appCtx;
  }
  
  public boolean isDatabaseRequired()
  {
    return _isDatabaseRequired;
  }

  /**
   * Configures whether the application will be configured with database access.
   * Affects which top-level Spring context configuration file is used to
   * initialize the application, "spring-context.xml" or
   * "spring-context-sans-db.xml".
   * 
   * @param isDatabaseRequired
   */
  public void setDatabaseRequired(boolean isDatabaseRequired)
  {
    if (_appCtx != null) {
      throw new IllegalStateException("setDatabaseRequired() must be called before " +
      "first call to getSpringBean() or getSpringApplicationContext()");
    }
    _isDatabaseRequired = isDatabaseRequired;
  }

  public void addCommandLineOption(Option option)
  {
    _options.addOption(option);
  }
  
  public void addCommandLineOption(Option option, Object defaultValue)
  {
    _options.addOption(option);
    _option2DefaultValue.put(option.getOpt(), defaultValue);
  }
  
  public String getCommandLineOptionValue(String optionName) throws ParseException
  {
    verifyOptionsProcessed();
    if (!_cmdLine.hasOption(optionName) &&
      _option2DefaultValue.containsKey(optionName)) {
      return _option2DefaultValue.get(optionName).toString();
    }
    Object optionValue = _cmdLine.getOptionValue(optionName);
    return optionValue == null ? "" : optionValue.toString();
  }
  
  @SuppressWarnings("unchecked")
  public <T> T getCommandLineOptionValue(String optionName, Class<T> ofType) 
    throws ParseException, IllegalArgumentException, SecurityException, 
           InstantiationException, IllegalAccessException, InvocationTargetException, 
           NoSuchMethodException
  {
    verifyOptionsProcessed();
    if (!_cmdLine.hasOption(optionName) &&
      _option2DefaultValue.containsKey(optionName)) {
      return (T) _option2DefaultValue.get(optionName);
    }
    if (_cmdLine.hasOption(optionName)) {
      Constructor cstr = ofType.getConstructor(String.class);
      return (T) cstr.newInstance(getCommandLineOptionValue(optionName));
    }
    return null;
  }
  
  public boolean isCommandLineFlagSet(String optionName) throws ParseException
  {
    verifyOptionsProcessed();
    return _cmdLine.hasOption(optionName);
  }
  
  public void showHelp()
  {
    new HelpFormatter().printHelp("command", _options, true);
  }
  
  /**
   * @param acceptDatabaseOptions
   * @param showHelpOnError if codetrue/code and method returns
   *          codefalse/code, calling code should not call
   *          {@link #getCommandLineOptionValue} or
   *          {@link #isCommandLineFlagSet(String)}.
   * @return
   * @throws ParseException
   */
  @SuppressWarnings("unchecked")
  public boolean processOptions(
    boolean acceptDatabaseOptions,
    boolean showHelpOnError) throws ParseException
  {
    if (acceptDatabaseOptions) {
      addDatabaseOptions();
    }
    
    try {
      _cmdLine = new GnuParser().parse(_options, _cmdLineArgs);
    }
    catch (ParseException e) {
      if (showHelpOnError) {
        System.out.println(e.getMessage());
        showHelp();
        return false;
      }
      else {
        throw e;
      }
    }

    if (_cmdLine.hasOption("help")) {
      showHelp();
    }

    if (acceptDatabaseOptions) {
      // TODO: make constants for these property names
      if (isCommandLineFlagSet("H")) {
        System.setProperty("SCREENSAVER_PGSQL_SERVER", getCommandLineOptionValue("H"));
      }
      if (isCommandLineFlagSet("D") || isCommandLineFlagSet("T")) {
        System.setProperty("SCREENSAVER_PGSQL_DB", 
                           (isCommandLineFlagSet("D") ? getCommandLineOptionValue("D") : "localhost") +
                           (isCommandLineFlagSet("T") ? ": " + getCommandLineOptionValue("T") : ""));
      }
      if (isCommandLineFlagSet("U")) {
        System.setProperty("SCREENSAVER_PGSQL_USER", getCommandLineOptionValue("U"));
      }
      if (isCommandLineFlagSet("P")) {
        System.setProperty("SCREENSAVER_PGSQL_PASSWORD", getCommandLineOptionValue("P"));
      }
    }
    
    StringBuilder s = new StringBuilder();
    for (Option option : (Collection<Option>) _options.getOptions()) {
      if (_cmdLine.hasOption(option.getOpt())) {
        if (s.length() > 0) {
          s.append(", ");
        }
        s.append(option.getLongOpt());
        if (option.hasArg()) {
          s.append("=").append(_cmdLine.getOptionValue(option.getOpt()));
        }
      }
    }
    log.info("command line options: " + s.toString());

    return true;
  }
  
  
  // private methods

  private void verifyOptionsProcessed()
  {
    if (_cmdLine == null) {
      throw new IllegalStateException("processOptions() not yet called or error occurred parsing command line options");
    }
  }

  /**
   * Adds "dbhost", "dbport", "dbuser", "dbpassword", and "dbname" options to
   * the command line parser, enabling this CommandLineApplication to access a
   * database.
   */
  @SuppressWarnings("static-access")
  private void addDatabaseOptions()
  {
    addCommandLineOption(OptionBuilder.
                         hasArg().
                         withArgName("host name").
                         withLongOpt("dbhost").
                         withDescription("database host").
                         create("H"));
    addCommandLineOption(OptionBuilder.
                         hasArg().
                         withArgName("port").
                         withLongOpt("dbport").
                         withDescription("database port").
                         create("T"));
    addCommandLineOption(OptionBuilder.
                         hasArg().
                         withArgName("user name").
                         withLongOpt("dbuser").
                         withDescription("database user name").
                         create("U"));
    addCommandLineOption(OptionBuilder.
                         hasArg().
                         withArgName("password").
                         withLongOpt("dbpassword").
                         withDescription("database user's password").
                         create("P"));
    addCommandLineOption(OptionBuilder.
                         hasArg().
                         withArgName("database").
                         withLongOpt("dbname").
                         withDescription("database name").
                         create("D"));
  }
  
}
