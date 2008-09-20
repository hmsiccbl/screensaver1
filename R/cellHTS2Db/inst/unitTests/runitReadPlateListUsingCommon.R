testReadPlateListUsingCommon1 <- function(debug=FALSE) {

	library(cellHTS2);
	
	## The workdirectory is set by the Makefile to tests/ because there is doRUnit.R
	## Paths are therefore relative to that directory
	source("../inst/unitTests/readPlateListDbCheckEquals.R")
	
	## 1 PREPARE INPUT PARAMETERS
	filename <- "Platelist.txt"
	name <- "Dummy_experiment"
	path <- "../data/originals"
	
	## 2 RUN FUNCTION
	if (debug)
		debug(readPlateListUsingCommon)
	
	
	r  <-   readPlateListUsingCommon(filename=filename, name=name, path=path,verbose=TRUE);
	
	## 3 CHECKEQUALS
	
	## also used by runitReadPlateListCommon, runitReadPlateListDb.
	readPlateListDbCheckEquals(r)
	

}