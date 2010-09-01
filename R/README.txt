Screensaver & cellHTS2
======================


Some initial documentation is in the R package via .Rd file. Within a R
session this information is retrieved via help(cellHTS2Db).


Required Software Versions
--------------------------

The current cellHTS2 integration was originally developed and tested with: 
   * cellHTS2 2.2.5
   * R 2.6.2
   * Rserve 0.5-0  

As of 2010-09-01, the following versions appear to be compatible (thanks to Nigel Binns, University of Edinburgh): 
   * R 2.9.2
   * cellHTS2 2.8.3
   * Rserve 0.6-2
   
Run the tests to verify your own environment; see testing instructions below.   


Installation Instructions for R, Rserve, cellHTS2Db
---------------------------------------------------

1. Install R

  For example, using Debian GNU/Linux, as root:
  # apt-get R

  For example, using Gentoo GNU/Linux, as root:
  # emerge R 


2. Install cellHTS2:
$ R
> source("http://bioconductor.org/biocLite.R") 
> biocLite("cellHTS2")

3. Install Screensaver's cellHTS2Db2 R package:
$ R CMD INSTALL $SCREENSAVER/R/cellHTS2Db_0.3.tar.gz

(for building, see notes below)

Alternately, can perform custom build and install from source:
$ (cd ~/screensaver/R/cellHTS2Db/inst/unitTests && make inst)

4. Install Rserve:
$ R
> install.packages('Rserve',,'http://www.rforge.net/')

Starting Rserve
---------------

$ R
> library(cellHTS2Db)
> library(Rserve)
> Rserve()



RUnit Tests
-----------------

Install RUnit:
$ R
> install.packages("RUnit")


run tests in R:
$ R
> library(RUnit)
> setwd("$SCREENSAVER/R/cellHTS2Db/tests")
> testResult <- runTestFile("doRUnit.R")
> printTextProtocol(testResult)


#####
Building R package

To run the RUnit-tests you can use 'make' on the the Makefile script in ../R/cellHTS2Db/inst/unitTests. For example under linux: 
cor@cor-laptop:~/ws_screensaver/screensaverCollaboration/R/cellHTS2Db/inst/unitTests$ make

Running 'make' will first install the package and then run the testscripts. In setting this up, I followed the instructions as described on the wiki.r-project.org site. The url is:  http://wiki.r-project.org/rwiki/doku.php?id=developers:runit .

#build with the following statement. The filename will be automatically determine based on folder name and 
# Version number in DESCRIPTION
R CMD build cellHTS2Db/

For general information over creating R package see http://cran.r-project.org/doc/manuals/R-exts.pdf




