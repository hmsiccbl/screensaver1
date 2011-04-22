# runitReadPlateListCommon.R
# 
# TODO: Add comment
#
# Author: cor
###############################################################################
testReadPlateListCommon <- function(debug=F) {
	
	## The workdirectory is set by the Makefile to tests/ because there is doRUnit.R
	## Paths are therefore relative to that directory
	source("../inst/unitTests/makeDummies.R")	
	source("../inst/unitTests/readPlateListDbCheckEquals.R")
	
	## 1. PREPARE INPUT DATA
	testSet <- makeTestSet()
	
	## 2. EXECUTE FUNCTION
	if (debug) 
		debug(readPlateListCommon)
	r <- readPlateListCommon(testSet$xraw,testSet$name,testSet$dimPlate,testSet$pd,testSet$status,testSet$intensityFiles)
	
	## 3. CHECKEQUALS
	if (debug)
		debug(readPlateListDbCheckEquals)
	readPlateListDbCheckEquals(r)

}



