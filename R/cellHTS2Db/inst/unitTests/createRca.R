createRca <- function(testSet=1,withSlog =FALSE) {
	## The workdirectory is set by the Makefile to tests/ because there is doRUnit.R
	## Paths are therefore relative to that directory
	source("../inst/unitTests/makeDummies.R")
	if (testSet==1){
		testSet <- makeTestSet(withSlog)
	}else if (testSet==2){
		testSet <- makeTestSet2()
	}else if (testSet==3) {
		testSet <- makeTestSet3()		
	}else if (testSet==4) {
		testSet <- makeTestSet4(withSlog)		
	}else if (testSet==5) {
		testSet <- makeTestSet5()		
	}		
	xrawd <- dim(testSet$xraw)
	r <- readPlateListDb(testSet$xraw, testSet$name, testSet$dimPlate[1], testSet$dimPlate[2])
	
	if (withSlog) {
		rc <- configureDb(r,testSet$conf,testSet$slog)
	}else {
		rc <- configureDb(r,testSet$conf)
	}
		
	rca <- annotateDb(rc,testSet$geneIDs)
	return(rca)
}
