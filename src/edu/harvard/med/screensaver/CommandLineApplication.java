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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Convenience class for instantiating a Screensaver-based command-line
 * application.  The main purpose of this class is to house the Spring framework
 * bootstrapping code, so that developers can forget the details of how to do
 * this (and don't have to cut and paste code between various main() methods).
 * Also provides a some help with
 * <ul>
 * <li>command-line argument parsing
 * <li>obtaining Spring-managed beans
 * <ul>.
 * 
 * @author ant
 */
public class CommandLineApplication
{
  public static final String[] SPRING_CONFIG_FILES = {
    "spring-context-logging.xml",
    "spring-context.persistence.xml",
    "spring-context-services.xml",
  };
  private static final String DEFAULT_DB_NAME = "devscreensaver";
  private static final String DEFAULT_DB_PASSWORD = "devscreensaver";
  private static final String DEFAULT_DB_USER = "devscreensaver";
  private static final String DEFAULT_DB_HOST = "localhost";
  private static final String DEFAULT_DB_PORT = "5432";
  
  private ApplicationContext _appCtx;
  private Options _options;
  private CommandLine _cmdLine;
  private String[] _cmdLineArgs;
  private Map<String,Object> _option2DefaultValue = new HashMap<String,Object>();
  
    
  @SuppressWarnings("static-access")
  public CommandLineApplication(String[] cmdLineArgs)
  {
    _appCtx = new ClassPathXmlApplicationContext(new String[] { 
      "spring-context-logging.xml",
      "spring-context-services.xml", 
      "spring-context-persistence.xml",
    });
    _cmdLineArgs = cmdLineArgs;
    _options = new Options();
    _options.addOption(OptionBuilder.
                       withArgName("help").
                       withLongOpt("help").
                       withDescription("print this help").
                       create());
  }
  
  public Object getSpringBean(String springBeanName)
  {
    return _appCtx.getBean(springBeanName);
  }
  
  @SuppressWarnings("unchecked")
  public <T> T getSpringBean(String springBeanName, Class<T> ofType)
  {
    return (T) _appCtx.getBean(springBeanName);
  }
  
  public ApplicationContext getSpringApplicationContext()
  {
    return _appCtx;
  }
  
  public void addCommandLineOption(Option option)
  {
    _options.addOption(option);
  }
  
  public void addCommandLineOption(Option option, Object defaultValue)
  {
    _options.addOption(option);
    _option2DefaultValue.put(option.getArgName(), defaultValue);
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
  
  private void verifyOptionsProcessed()
  {
    if (_cmdLine == null) {
      throw new IllegalStateException("processOptions() not yet called or error occurred parsing command line options");
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getCommandLineOptionValue(String optionName, Class<T> ofType) throws ParseException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
  {
    verifyOptionsProcessed();
    if (!_cmdLine.hasOption(optionName) &&
      _option2DefaultValue.containsKey(optionName)) {
      return (T) _option2DefaultValue.get(optionName);
    }
    Constructor cstr = ofType.getConstructor(String.class);
    return (T) cstr.newInstance(getCommandLineOptionValue(optionName));
  }
  
  public boolean isCommandLineFlagSet(String optionName) throws ParseException
  {
    verifyOptionsProcessed();
    return _cmdLine.hasOption(optionName);
  }
  
  public void showHelp()
  {
    new HelpFormatter().printHelp("<command>", _options);
  }
  
  
  // private methods

  /**
   * @throws ParseException
   */
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
      BasicDataSource dataSource = getSpringBean("screensaverDataSource",
                                                 BasicDataSource.class);
      dataSource.setUsername(getCommandLineOptionValue("dbuser"));
      dataSource.setPassword(getCommandLineOptionValue("dbpassword"));
      dataSource.setUrl("jdbc:postgresql://" + getCommandLineOptionValue("dbhost") + (getCommandLineOptionValue("dbport").length() > 0
        ? (":" + getCommandLineOptionValue("dbport")) : "") + "/" + getCommandLineOptionValue("dbname"));
    }
      
    return true;
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
                         withArgName("dbhost").
                         withLongOpt("dbhost").
                         hasArg().
                         withDescription("database host").
                         create(), 
                         DEFAULT_DB_HOST);
    addCommandLineOption(OptionBuilder.
                         withArgName("dbport").
                         withLongOpt("dbport").
                         hasArg().
                         withDescription("database port").
                         create(), 
                         DEFAULT_DB_PORT);
    addCommandLineOption(OptionBuilder.
                         withArgName("dbuser").
                         withLongOpt("dbuser").
                         hasArg().
                         withDescription("database user name").
                         create(), 
                         DEFAULT_DB_USER);
    addCommandLineOption(OptionBuilder.withArgName("dbpassword").
                         withLongOpt("dbpassword").
                         hasArg().
                         withDescription("database user's password").
                         create(), 
                         DEFAULT_DB_PASSWORD);
    addCommandLineOption(OptionBuilder.
                         withArgName("dbname").
                         withLongOpt("dbname").
                         hasArg().
                         withDescription("database name").
                         create(), 
                         DEFAULT_DB_NAME);
  }
  
}
