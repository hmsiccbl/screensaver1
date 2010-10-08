testWriteReport1 <- function(debug=FALSE) {
#   
	## The workdirectory is set by the Makefile to tests/ because there is doRUnit.R
	## Paths are therefore relative to that directory
	
	##1. PREPARE INPUT
	source("../inst/unitTests/createRca.R")
	rca <- createRca(3)
	rcan <- normalizePlates(rca,method="median");
	rcans <- scoreReplicates(rcan, method="zscore")
	rcanss <- summarizeReplicates(rcans,summary="mean");
	
	if (debug) 
		debug(writeReport)
	
	##2. RUN METHOD WRITEREPORT #, TODO Add "scored"=rcanss. Currently not possible. The location for some text is
	## calculated and failed in case to few samples.
	## .. 

	out <- writeReport(cellHTSlist=list("raw"=rca,"normalized"=rcan,"scored"=rcanss), plotPlateArgs = TRUE, 
			 imageScreenArgs = list(zrange=c( -4, 8), ar=1), map=TRUE,force = TRUE, outdir = "/tmp/screensaver/output/")
	
	## 3. CHECK
	## TODO check for all the expected files, starting with index.html 
	
	
	
}

testWriteReportInclScored <- function(debug=FALSE) {
	## Seperate test for including scored data, because of bug in imageScreen used by writeReport. 
	## To draw the legende, imageScreen assumes that
	## there are 7 columns at least available in the image to draw the legend. However in case
	## of two plates and two columns it is 5, including a spacer. 
	   
   
	#    setwd("/home/cor/ws_screensaver/screensaverCollaboration/R/cellHTS2Db/tests")
	#    library(cellHTS2Db)
	#  	 source("../inst/unitTests/makeDummies.R")
	
   ##1. PREPARE INPUT
   source("../inst/unitTests/createRca.R")
   rca <- createRca(testSet=2)
   rcan <- normalizePlates(rca,method="negatives",negControls="N");
   rcans <- scoreReplicates(rcan, method="zscore")
   rcanss <- summarizeReplicates(rcans,summary="mean");
   
   if (debug) 
      debug(writeReport)

   ##2. RUN METHOD WRITEREPORT #, TODO Add "scored"=rcanss
   ## debug(imageScreen)
   ##,
   out <- writeReport(cellHTSlist=list("raw"=rca,"normalized"=rcan,"scored"=rcanss ), plotPlateArgs = FALSE, imageScreenArgs = list(ar=1), map=TRUE,
		   force = TRUE, outdir = "/tmp/screensaver/output/",negControls="N")

   ## 3. CHECK
   ## TODO check for all the expected files, starting with index.html 
   
}
testWriteReportMultiChannel <- function(debug=FALSE) {
#   
	## The workdirectory is set by the Makefile to tests/ because there is doRUnit.R
	## Paths are therefore relative to that directory
	
	##1. PREPARE INPUT
	source("../inst/unitTests/createRca.R")
	rca <- createRca(4)
	rcan <- normalizePlates(rca,method="median");
	rcans <- scoreReplicates(rcan, method="zscore")
	
	if (debug) 
		debug(writeReport)
	
	##2. RUN METHOD WRITEREPORT #, TODO Add "scored"=rcanss. Currently not possible. The location for some text is
	## calculated and failed in case to few samples.
	## .. 
	
	out <- writeReport(raw=rca,normalized=rcan, plotPlateArgs = TRUE, 
			imageScreenArgs = list(zrange=c( -4, 8), ar=1), map=TRUE,force = TRUE, outdir = "/tmp/screensaver/output/")
	
	## 3. CHECK
	## TODO check for all the expected files, starting with index.html 
	
}	
	
testWriteReportMultiChannelSlog <- function(debug=FALSE) {
#   
	## The workdirectory is set by the Makefile to tests/ because there is doRUnit.R
	## Paths are therefore relative to that directory
	
	##1. PREPARE INPUT
	source("../inst/unitTests/createRca.R")
	rca <- createRca(4,TRUE) #with Slog
	rcan <- normalizePlates(rca,method="median");
	rcans <- scoreReplicates(rcan, method="zscore")
	
	if (debug) 
		debug(writeReport)
	
	##2. RUN METHOD WRITEREPORT #, TODO Add "scored"=rcanss. Currently not possible. The location for some text is
	## calculated and failed in case to few samples.
	## .. 
	
	out <- writeReport(raw=rca,normalized=rcan, plotPlateArgs = TRUE, 
			imageScreenArgs = list(zrange=c( -4, 8), ar=1), map=TRUE,force = TRUE, outdir = "/tmp/screensaver/output/")
	
	## 3. CHECK
	## TODO check for all the expected files, starting with index.html 
	
}	
