If Screensaver utility programs are executed from within your IDE or from a
shell, and this resources/ directory is part of your configured Java
classpath, it is recommended that you create or symlink a
screensaver.properties file and a log4j.properties file here.  See ../cfg/ for
examples.  Note the screensaver.properties file, which can contain both
build-time settings (used by Ant) and run-time settings, will only be used for 
its run-time settings.
