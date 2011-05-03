testAnnotateDb1 <- function(debug=FALSE) {
	
	#1. PREPARE INPUT
	#the workdirectory is set by the Makefile to tests/ because there is doRUnit.R
	#paths are therefore relative to that directory
	source("../inst/unitTests/makeDummies.R")
	testSet <- makeTestSet()
	xrawd <- dim(testSet$xraw)
	r <- readPlateListDb(testSet$xraw, testSet$name, testSet$nrRowsPlate, testSet$nrColsPlate)
	rc <- configureDb(r,testSet$conf)	
	
	if (debug) 
		trace("annotateDb", browser, exit=browser, signature = c("cellHTS"))
	
	# 2. RUN METHOD ANNOTATEDB
	rca <- annotateDb(rc,testSet$geneIDs)
	
	#check the adding into featureData
	testSet$geneIDs <- testSet$geneIDs[, !c(names(testSet$geneIDs) %in% c("Plate", "Well")), drop=FALSE]
	
	# After geneIDs[apply(geneIDs, 2, function(i) i %in% "NA")] <- NA de row.names are
	# .. changed from numeric to character. Can be checked via attributes(geneIDs)
	rownames(testSet$geneIDs) <- c("1", "2", "3", "4", "5", "6", "7", "8")
	
	
	# 3. CHECK EQUALS

	checkEquals(testSet$geneIDs,fData(rca)[names(testSet$geneIDs)])
	
	# check the adding of the names in varMetaData
	#fvarMetadata(object)[names(testSet$geneIDs),]=names(testSet$geneIDs)
	
	# Check the changed status
	#object@state[["annotated"]] = TRUE
}