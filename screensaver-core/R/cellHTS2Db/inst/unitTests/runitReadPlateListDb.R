testCreateIntensityFile <- function (debug=F) {
    library(RUnit)
	
	
	##1. PREPARE INPUT PARAMETERS
	
	## The workdirectory is set by the Makefile to tests/ because there is doRUnit.R
	## Paths are therefore relative to that directory
	source("../inst/unitTests/makeDummies.R")

   testSet <- makeTestSet()
   
   fileName <- "P1R1C1.txt"
   xraw  <-  testSet$xraw
   plateId <- 1
   replicateId <- 1 
   channelId <- 1
   pdim <- c(nrow=as.integer(testSet$nrRowsPlate), ncol=as.integer(testSet$nrColsPlate))
   
   ##2. RUN FUNCTION

   
   if (debug) 
      debug(createIntensityFile)
   
   intensityFile <- createIntensityFile(fileName,xraw,plateId,replicateId,channelId,pdim)
   
   ##3. PREPARE CHECK TARGET OBJECT
   
	## str(intensityFile)
	##	Class 'AsIs'  chr [1:4] "P1R1C1.txt\tA01\t1" "P1R1C1.txt\tA02\t2" "P1R1C1.txt\tB01\t3" "P1R1C1.txt\tB02\t4"
	intensityFileTarget <- I(c("P1R1C1.txt\tA01\t1","P1R1C1.txt\tA02\t2","P1R1C1.txt\tB01\t3","P1R1C1.txt\tB02\t4"))
   	
   
   ##4. CHECK EQUALS
   checkEquals(intensityFileTarget,intensityFile)
   
}

testCreatePlateListAndIntensityFiles <- function(debug=F) {

	## PLATELIST	
   ##In case of 2 plates, 2 replicates and 1 channels the result should be:
   ##1   P1R1C1.txt     1         1   1
   ##2   P1R2C1.txt     1         2   1
   ##3   P2R1C1.txt     2         1   1
   ##4   P2R2C1.txt     2         2   1
   
   ##str(pd)
   ##'data.frame':   57 obs. of  4 variables: "P2R1C1.txt" ...
   ##$ Plate    : int  1 1 
   ##$ Replicate: int  1 1 
   ##$ Channel   : int  1 2
   
   ## The workdirectory is set by the Makefile to tests/ because there is doRUnit.R
   ## Paths are therefore relative to that directory
   source("../inst/unitTests/makeDummies.R")
      
   ##1. PREPARE INPUT PARAMETERS
   testSet <- makeTestSet()
   pdim <- c(nrow=as.integer(testSet$nrRowsPlate), ncol=as.integer(testSet$nrColsPlate))

   ##2. RUN FUNCTION
   if (debug) 
      debug(testCreatePlateListAndIntensityFiles)
   
   result <- createPlateListAndIntensityFiles(xraw=testSet$xraw ,pdim=pdim)

   ##3. PREPARE CHECKEQUAL TARGET OBJECT FOR PLATELIST
   pdTarget <- testSet$pd
   ##4.CHECK EQUALS FOR  PLATELIST
   checkEquals(pdTarget,result$pd)
   
   ##5.CHECK EQUALS FOR INTENSITYFILES
   intensityFilesTarget <- testSet$intensityFiles
   checkEquals(intensityFilesTarget,result$intensityFiles)
   
}

testReadPlateListDb1 <- function(debug=FALSE) {
	
	library(cellHTS2);
	library(cellHTS2Db);
	
	## The workdirectory is set by the Makefile to tests/ because there is doRUnit.R
	## Paths are therefore relative to that directory
	source("../inst/unitTests/makeDummies.R")
	source("../inst/unitTests/readPlateListDbCheckEquals.R")
	
	## 1 PREPARE INPUT PARAMETERS
	testSet <- makeTestSet()
	xrawd <- dim(testSet$xraw)
	
	## 2 RUN FUNCTION
	if (debug)
		debug(readPlateListDb)
	
	r <- readPlateListDb(testSet$xraw, testSet$name, testSet$nrRowsPlate, testSet$nrColsPlate)
	#save(r,file="data/r.Rda")
	
	## 3 CHECKEQUALS
	## also used by runitReadPlateListCommon
	readPlateListDbCheckEquals(r)
   
}