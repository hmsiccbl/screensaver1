createRca <- function(testSet=1,withSlog =FALSE) {
	## The workdirectory is set by the Makefile to tests/ because there is doRUnit.R
	## Paths are therefore relative to that directory
	source("../inst/unitTests/makeDummies.R")
	if (testSet==1){
		testSet <- makeTestSet(withSlog)
	} else {
		testSet <- makeTestSet2()
	}
	xrawd <- dim(testSet$xraw)
	r <- readPlateListDb(testSet$xraw, testSet$name, testSet$nrRowsPlate, testSet$nrColsPlate)
	
	if (withSlog) {
		rc <- configureDb(r,testSet$conf,testSet$slog)
	}else {
		rc <- configureDb(r,testSet$conf)
	}
		
	rca <- annotateDb(rc,testSet$geneIDs)
	return(rca)
}
