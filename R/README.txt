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




