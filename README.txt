*******************************************************************************
*  $HeadURL$
*  $Id$
*
*  Copyright 2006 by the President and Fellows of Harvard College.
*
*  Screensaver is an open-source project developed by the ICCB-L and NSRB labs
*  at Harvard Medical School. This software is distributed under the terms of
*  the GNU General Public License.
*******************************************************************************

Screensaver 1.xx
================

This document contains general information about the Screensaver project, 
which is contained herein.


Directory Contents
------------------

.eclipse.classes/ The place for Eclipse to build its own versions of the .class
                  files.
                  
.eclipse.prefs/   Contains exported preference files from the Eclipse IDE, which
                  are intended to be shared among the project's developers.

.myeclipse/       The MyEclipse project settings directory.

.settings/        The Eclipse .settings directory.

build/			      Destination for exploded WAR, JAR, and EAR files (the project 
                  only builds a WAR file currently).

lib/  				    Java JAR (library) files.  Subdirectories reflect origins of
                  related JAR files.  JAR files at the top level of this directory 
                  are shared, and potentially satisfy dependencies of the project's
                  source code and of the third-party libraries used by the project 
                  (e.g. of Hibernate, XDoclet, etc.).  Subdirectories named
                  'unused' contain JARs that are not depended upon by the 
                  project's source code or third-party libraries.

resources/  	    Java resource files; i.e., files that can found within the 
                  project's Java classpath via Java's resource loading mechanism.

sql/				      SQL schema.

src/			 	      Java source code (only).

src/test/				  Testing-related Java source code.

web/				      Web application-specific files, used to generate a WAR file.

.classpath        The Eclipse .classpath file.

.project          The Eclipse .project file.

.springBeans      The MyEclipse .springBeans file.

build.xml         Ant build file.

LICENSE           GNU General Public License.

README.txt			  This magnificent file.