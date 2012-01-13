*******************************************************************************
*  $HeadURL$
*  $Id$
*
*  Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
*
*  Screensaver is an open-source project developed by the ICCB-L and NSRB labs
*  at Harvard Medical School. This software is distributed under the terms of
*  the GNU General Public License.
*******************************************************************************

Screensaver README.txt
======================

This document contains general information about the Screensaver project,
which is contained herein.

The Screensaver project use Maven to manage configuration, testing, and
building of the application. The application is comprised of the following
Maven modules, which are found in subdirectories of the same name:


Directory Contents
------------------

As Screensaver follows the Maven conventions for its project directory
structure, the following list describes only the directories and files that
are not common to standard Maven projects:


licenses/           Software licenses for redistributed software.

INSTALL.txt         Link to installation instructions for the Screensaver
                    web application.

LICENSE.txt         GNU General Public License, under which the Screensaver
                    application is distributed.

NOTICE.txt          Legal mumbo jumbo.

README.txt          This magnificent file. 

core/               Maven module containing the core source code of the
                    Screensaver projects, including the domain model layer, 
                    the services layer, and the persistence layer.
web/                Maven module containing the web application page 
                    definitions, resources, and (eventually) the source code
                    specific to the web application, such as JSF backing beans.

batch/              Maven module Containing the source code for the batch 
                    applications for that perform data I/O or data updates 
                    via command-line invocation.

core/src/main/sql/
           
                    Files related to database maintenance.  
                    schema_updates/ contains scripts for upgrading an 
                    existing database's schema to match changes made to the Java 
                    entity model.

core/src/main/config/example-database/ 

                    contains an example database schema, with 
                    some limited data for assessing Screensaver's capabilities.
                    
core/src/main/scripts/dev/

                    Executable scripts for development, deployment, etc.

                    
core/contrib/       Code that has been contributed to the project that is not 
                    directly needed or used by the Screensaver application, but 
                    that may be of use to developers at specific facilities.
                   
core/local_libs/             
   
                    Libraries need to be installed in the local maven
                    repository in order for the build/runtime to work.  
                    See local_libs/readme.txt. 

core/R/cellHTS2Db   cellHTS2 code for R (statistical package) 
                    [will become an independent Maven project or module]


core/.fbprefs       FindBugs user preferences, for FindBugs Eclipse plugin

core/findbugs-filter.xml 

                    Settings for FindBugs, to configure the code and bug 
                    categories that should be ignored by FindBugs  

web/src/main/webapp/main/changelog.html      

                    Description of major changes made to Screensaver for each
                    released version.

web/src/main/tomcat/conf

                    Configuration files for deploying Screensaver.
