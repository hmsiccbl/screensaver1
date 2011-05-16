// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.harvard.med.screensaver.DatabaseConnectionSettings;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

/**
 * Convenience class for instantiating a Screensaver-based command-line
 * application. The main purpose of this class is to house the Spring framework
 * bootstrapping code, so that developers can forget the details of how to do
 * this (and don't have to cut and paste code between various main() methods).
 * Also provides help with:
 * <ul>
 * <li>command-line argument parsing (including special-case handling of database connection options)
 * <li>obtaining Spring-managed beans.
 * <ul>
 * .
 * <p>
 * Normally, a screensaver distribution will use the database connection settings specified in
 * "classpath:screensaver.properties". However, if {@link #processOptions(boolean, boolean, boolean)} is called with
 * acceptDatabaseOptions=true, the command line options 'dbhost', 'dbport', 'dbname', 'dbuser', and 'dbpassword' will be
 * used to configure the database connection settings.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class CommandLineApplication
{
  private static final Logger log = Logger.getLogger(CommandLineApplication.class);

  public static final String DEFAULT_SPRING_CONFIG = "spring-context-cmdline.xml";

  public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

  public static final String CMD_LINE_ARGS_DATABASE_CONNECTION_SETTINGS = "cmdline.args.database.connection.settings";

  // instance data

  private String _springConfigurationResource = DEFAULT_SPRING_CONFIG;
  private ApplicationContext _appCtx;
  private Options _options;
  private CommandLine _cmdLine;
  private String[] _cmdLineArgs;
  private Map<String,Object> _option2DefaultValue = new HashMap<String,Object>();

  private Option _lastAccessOption;


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
      _appCtx = new ClassPathXmlApplicationContext(getSpringConfigurationResource());
    }
    return _appCtx;
  }

  /**
   * Override this method to specify a different spring configuration resource
   * file.
   *
   * @return the name of the spring configuration resource file (resource name
   *         relative to the classpath root).
   */
  protected String getSpringConfigurationResource()
  {
    return _springConfigurationResource;
  }

  public void addCommandLineOption(Option option)
  {
    _options.addOption(option);
  }

  public void addCommandLineOption(Option option, Object defaultValue)
  {
    _options.addOption(option);
    if (option.hasArgs()) {
      if (defaultValue instanceof List) {
        throw new IllegalArgumentException("when option takes multiple args, defaultValue must be a List");
      }
    }
    _option2DefaultValue.put(option.getOpt(), defaultValue);
  }

  public String getCommandLineOptionValue(String optionName)
  {
    verifyOptionsProcessed();
    _lastAccessOption = _options.getOption(optionName);
    if (!_cmdLine.hasOption(optionName) &&
      _option2DefaultValue.containsKey(optionName)) {
      return _option2DefaultValue.get(optionName).toString();
    }
    Object optionValue = _cmdLine.getOptionValue(optionName);
    return optionValue == null ? "" : optionValue.toString();
  }

  public List<String> getCommandLineOptionValues(String optionName)
  {
    verifyOptionsProcessed();
    _lastAccessOption = _options.getOption(optionName);
    List<String> optionValues = new ArrayList<String>();
    if (!_cmdLine.hasOption(optionName) &&
      _option2DefaultValue.containsKey(optionName)) {
      for (Object defaultValue : (List<?>) _option2DefaultValue.get(optionName)) {
        optionValues.add(defaultValue.toString());
      }
    }
    else {
      String[] optionValuesArray = _cmdLine.getOptionValues(optionName);
      if (optionValuesArray != null) {
        optionValues.addAll(Arrays.asList(optionValuesArray));
      }
    }
    return optionValues;
  }

  public <T> T getCommandLineOptionValue(String optionName, Class<T> ofType)
  {
    verifyOptionsProcessed();
    _lastAccessOption = _options.getOption(optionName);
    if (!_cmdLine.hasOption(optionName) &&
      _option2DefaultValue.containsKey(optionName)) {
      return (T) _option2DefaultValue.get(optionName);
    }
    if (_cmdLine.hasOption(optionName)) {
      Object value = getCommandLineOptionValue(optionName);
      try {
        Constructor cstr = ofType.getConstructor(String.class);
        return (T) cstr.newInstance(value);
      }
      catch (Exception e) {
        showHelpAndExit("could not parse option " + optionName + " with arg " + value + " as type " + ofType.getSimpleName() + ": " +
          e);
      }
    }
    return null;
  }

  public <T extends Enum<T>> T getCommandLineOptionEnumValue(String optionName, Class<T> ofEnum)
  {
    verifyOptionsProcessed();
    _lastAccessOption = _options.getOption(optionName);
    if (!_cmdLine.hasOption(optionName) &&
      _option2DefaultValue.containsKey(optionName)) {
      return (T) _option2DefaultValue.get(optionName);
    }
    if (_cmdLine.hasOption(optionName)) {
      Object value = getCommandLineOptionValue(optionName);
      try {
        Enum<T> valueOf = Enum.valueOf(ofEnum, value.toString().toUpperCase());
        return (T) valueOf;
      }
      catch (Exception e) {
        showHelpAndExit("could not parse option " + optionName + " with arg " + value + " as enum " +
          ofEnum.getClass().getSimpleName());
      }
    }
    return null;
  }

  public DateTime getCommandLineOptionValue(String optionName,
                                            DateTimeFormatter formatter)
    throws ParseException
  {
    verifyOptionsProcessed();
    _lastAccessOption = _options.getOption(optionName);
    if (!_cmdLine.hasOption(optionName) &&
      _option2DefaultValue.containsKey(optionName)) {
      return (DateTime) _option2DefaultValue.get(optionName);
    }
    if (_cmdLine.hasOption(optionName)) {
      try {
        String value = getCommandLineOptionValue(optionName);
        return formatter.parseDateTime(value);
      }
      catch (Exception e) {
        throw new ParseException("could not parse date option " + optionName);
      }
    }
    return null;
  }

  public boolean isCommandLineFlagSet(String optionName)
  {
    verifyOptionsProcessed();
    return _cmdLine.hasOption(optionName);
  }

  public void showHelpAndExit(String errorMessage)
  {
    if (errorMessage != null) {
      System.out.println(errorMessage);
    }
    new HelpFormatter().printHelp("command", _options, true);
    System.exit(errorMessage != null ? 1 : 0);
  }

  /**
   * Parses command line arguments. On error, shows help and exits JVM process with status 1.
   * 
   * @param acceptDatabaseOptions
   * @param acceptAdminUserOptions
   */
  @SuppressWarnings("unchecked")
  public void processOptions(boolean acceptDatabaseOptions,
                             boolean acceptAdminUserOptions)
  {
    if (acceptDatabaseOptions) {
      addDatabaseOptions();
    }
    if (acceptAdminUserOptions) {
      addAdministratorUserOptions();
    }

    try {
      _cmdLine = new GnuParser().parse(_options, _cmdLineArgs);
      if (acceptDatabaseOptions) {
        if (isCommandLineFlagSet("D")) {
          DatabaseConnectionSettings settings =
            // note: we want non-specified options to be recorded as nulls, not as empty strings, as these have 
            // different meanings (null meaning that the option has not been provided by the user at all)
            new DatabaseConnectionSettings(isCommandLineFlagSet("H") ? getCommandLineOptionValue("H") : null,
                                           getCommandLineOptionValue("T", Integer.class),
                                           getCommandLineOptionValue("D"),
                                           isCommandLineFlagSet("U") ? getCommandLineOptionValue("U") : null,
                                           isCommandLineFlagSet("P") ? getCommandLineOptionValue("P") : null);
          System.getProperties().put(CMD_LINE_ARGS_DATABASE_CONNECTION_SETTINGS, settings);
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
    }
    catch (ParseException e) {
      showHelpAndExit(e.getMessage());
    }

    if (_cmdLine.hasOption("help")) {
      showHelpAndExit(null);
    }

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

  @SuppressWarnings("static-access")
  private void addAdministratorUserOptions()
  {
    addCommandLineOption(OptionBuilder
                         .hasArg()
                         .withArgName("user ID #")
                         .withDescription("the User ID of the administrative user performing the load")
                         .withLongOpt("admin-id")
                         .create("A"));

    addCommandLineOption(OptionBuilder
                         .hasArg()
                         .withArgName("login ID")
                         .withDescription("the Login ID of the administrative user performing the load")
                         .withLongOpt("admin-login-id")
                         .create("AL"));

    addCommandLineOption(OptionBuilder
                         .hasArg()
                         .withArgName("eCommons ID")
                         .withDescription("the eCommons ID of the administrative user performing the load")
                         .withLongOpt("admin-ecommons-id")
                         .create("AE"));
  }

  public AdministratorUser findAdministratorUser()
  {
    AdministratorUser admin;
    String criterionMessage;
    GenericEntityDAO dao = (GenericEntityDAO) getSpringBean("genericEntityDao");
    if (isCommandLineFlagSet("-A")) {
      Integer userId = getCommandLineOptionValue("-A", Integer.class);
      admin = dao.findEntityById(AdministratorUser.class, userId);
      criterionMessage = "User ID=" + userId;
    }
    else if (isCommandLineFlagSet("-AL")) {
      String loginId = getCommandLineOptionValue("-AL");
      admin = dao.findEntityByProperty(AdministratorUser.class, "loginId", loginId);
      criterionMessage = "Login ID=" + loginId;
    }
    else if (isCommandLineFlagSet("-AE")) {
      String ecommonsId = getCommandLineOptionValue("-AE");
      admin = dao.findEntityByProperty(AdministratorUser.class, "ECommonsId", ecommonsId);
      criterionMessage = "eCommons ID=" + ecommonsId;
    }
    else {
      throw new IllegalArgumentException("option --admin-id, --admin-login-id, or --user-ecommons-id (admin) must be speciifed");
    }
    if (admin == null) {
      throw new IllegalArgumentException("no administrator user found with " + criterionMessage);
    }
    return admin;
  }

  public void setSpringConfigurationResource(String springConfigurationResource)
  {
    if (_appCtx != null) {
      throw new IllegalStateException("spring application context has already been instantiated; it is too late to set spring configuration resource");
    }
    _springConfigurationResource = springConfigurationResource;
  }

  public Option getLastAccessOption()
  {
    return _lastAccessOption;
  }
}
