Screensaver & cellHTS2
======================


Some initial documentation is in the R package via .Rd file. Within a R
session this information is retrieved via help(cellHTS2Db).


Required Software Versions
--------------------------

As of 2010-10-06, the following versions appear to be compatible: 
   * R 2.10.1
   * Rserve 0.6-2
   * Bioconductor 2.5
   * Biobase 2.6.1
   * cellHTS2 2.10.5
   * cellHTS2Db 0.41
  
   
Run the tests to verify your own environment; see testing instructions below.   


Installation Instructions for R, Rserve, cellHTS2Db
---------------------------------------------------

1. Install R

  For example, using Ubuntu GNU/Linux:
  $ sudo apt-get install r-base-core

  For example, using Debian GNU/Linux, as root:
  # apt-get install R

  For example, using Gentoo GNU/Linux, as root:
  # emerge R 


1a. sudo apt-get install libxml2-dev [ubuntu 10.04]

2. Install cellHTS2:
$ R
> source("http://bioconductor.org/biocLite.R") 
> biocLite("XML")
> biocLite("GSEABase")
> biocLite("cellHTS2")

3. Install Screensaver's cellHTS2Db2 R package:
$ R CMD INSTALL $SCREENSAVER/R/cellHTS2Db_0.41.tar.gz

(To building, see notes below)


4. Install Rserve:
$ R
> install.packages('Rserve',,'http://www.rforge.net/')

Starting Rserve
---------------

$ R
> library(cellHTS2Db)
> library(Rserve)
> Rserve()




FOR DEVELOPERS
==============

Running R-based Unit Tests
--------------------------

Install RUnit:
$ R
> install.packages("RUnit")


run tests in R:
$ R
> library(RUnit)
> setwd("$SCREENSAVER/R/cellHTS2Db/tests")
> testResult <- runTestFile("doRUnit.R")
> printTextProtocol(testResult)



Building R package
------------------

To run the RUnit-tests you can use 'make' on the the Makefile script in
../R/cellHTS2Db/inst/unitTests. For example under linux:
$ $SCREENSAVER/R/cellHTS2Db/inst/unitTests$ make

Running 'make' will first install the package and then run the testscripts. In
setting this up, I followed the instructions at
http://wiki.r-project.org/rwiki/doku.php?id=developers:runit

Build with the following statement. The filename will be automatically
determined based on folder name and Version number in DESCRIPTION:

$ R CMD build cellHTS2Db/

For general information about creating an R package see http://cran.r-project.org/doc/manuals/R-exts.pdf




