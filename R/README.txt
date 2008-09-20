Screensaver & cellHTS2
======================


Some initial documentation is in the R package via .Rd file. Within a R
session this information is retrieved via help(cellHTS2Db).


Required Software Versions
--------------------------

The current cellHTS2 integration was derived from and depends upon 
cellHTS2 2.2.5, R 2.6.2, and Rserve 0.5-0.  These are the recommend 
versions to be used together, however more recent combinations of the
software may work together as well: for example, cellHTS2 2.4.1, R 2.7.2, and 
Rserve 0.5-2.  (Run the tests to verify your own environment; see
testing instructions below).   

FYI:

cellHTS2 2.2.5 is developed for the bioconductor (lite) release 2.1
Bioconductor 2.1 was developed for R 2.6.z 

cellHTS2 2.4.1 is developed for the bioconductor release 2.2
Bioconductor 2.2 was developed for R 2.7.0.


Installation Instructions for R, Rserve, cellHTS2Db
---------------------------------------------------

1. Install R 2.7.0.

  For example, using Debian GNU/Linux, as root:
  # apt-get R

  For example, using Gentoo GNU/Linux, as root:
  # emerge R 


2. Install cellHTS2:
$ R
> source("http://bioconductor.org/biocLite.R") 
> biocLite("cellHTS2")

3. Install Screensaver's cellHTS2Db2 R package:
$ R CMD INSTALL $SCREENSAVER/R/cellHTS2Db_0.2.tar.gz

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