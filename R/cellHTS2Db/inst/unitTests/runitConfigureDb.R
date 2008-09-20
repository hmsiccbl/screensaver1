testConfigureDb1 <- function(debug=FALSE) {
	## TODO change example dat with using wildcards into using straight numbers (as is the case with screensaver)
	## 1. PREPARE INPUT
	#the workdirectory is set by the Makefile to tests/ because there is doRUnit.R
	#paths are therefore relative to that directory
	source("../inst/unitTests/makeDummies.R")
	testSet <- makeTestSet(TRUE) #with screenlog
	xrawd <- dim(testSet$xraw)
	r <- readPlateListDb(testSet$xraw, testSet$name, testSet$nrRowsPlate, testSet$nrColsPlate)
	
	if (debug) 
		trace("configureDb", browser, exit=browser, signature = c("cellHTS"))
	
	## 2. RUN CONFIGUREDB
	rc <- configureDb(r,testSet$conf,testSet$slog)
	#save(rc,file="/tmp/rc.Rda")
	
	## 3. CHECK EQUALS
	#CHECK UPDATES OF SLOTS
	
	#1	plateConf: a 'data.frame' containing what was read from input file'confFile' (except the first two header rows).
	plateConf <- rc@plateConf
	plateConfTarget <- testSet$conf
	# For the * a space has been placed
	plateConfTarget[plateConfTarget=="*"] <- " *" 
	checkEquals(plateConfTarget,plateConf)

	#2 screenLog: a data.frame containing what was read from input file 'logFile'.
	# Channel is added in the configureDb
	targetSlog <- data.frame(Plate=c(1),Well=c("A02"),Sample=c(1),Flag=c("NA"),Channel=c(1),stringsAsFactors=FALSE)
	
	#Check slot screenLog
	checkEquals(targetSlog,rc@screenLog)
	
	#Check data adjusted, based on screenlog
	
			
	
	
	#3  screenDesc: object of class 'character' containing what was read from input file 'descripFile'.
	
	#4 state: the processing status of the 'cellHTS' object is updated in to 'state["configured"]=TRUE'.
	checkEquals(rc@state[["configured"]],TRUE)
	
	#5 featureData: the column 'controlStatus' is updated having into account the well annotation given by the plate configuration file.
	controlStatusTarget <- factor(c("pos","sample","neg","sample","pos","sample","neg","sample"),levels=c("sample","pos","neg"))
	controlStatus <- fData(rc)$controlStatus
	checkEquals(controlStatusTarget,controlStatus)
	
	#6 experimentData: an object of class 'MIAME' containing descriptions of the experiment, constructed from the screen description file.
	
	
	
}