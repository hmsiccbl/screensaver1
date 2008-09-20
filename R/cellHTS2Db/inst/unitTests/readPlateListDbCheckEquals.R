# r3ead.R
# Function used by both readPlateListDb and readPlateListDbCommon.  
# TODO: Add comment
#
# Author: cor
###############################################################################

readPlateListDbCheckEquals <- function(r) {
	source("../inst/unitTests/makeDummies.R")	
	
	target <- makeReadPlateListTarget()

	## ! cannot use checkEquals for S4 objects, so do it on different SLOTS
	
	##check if the result is of class cellHTS
	checkEquals( target$class,"cellHTS" )

	## 1. SLOT PLATELIST
	checkEquals(target$plateList, plateList(r))
	
	## 2. SLOT ASSAYDATA
	## In the assaydata slot, the data is now three in stead of 4 dimensional as the plates are now below each other
	## De dimensions are now:  "Features","Sample", "Channels" . Sample can also be read as "Replicate". See fData below for the details 
	checkEquals(target$data,Data(r))

	## 3. SLOT FEATUREDATA
	##fdata must be initialized with well annotation and controlStatus "unknown"
	##   'data.frame':   .. obs. of  3 variables:
	##         $ plate        : int  ...
	##   $ well         :Class 'AsIs'  chr [1:7296] "A01"  ...
	##   $ controlStatus: Factor w/ 1 level "unknown": 1  ...
	
	## controlStatus will be later on updated in configure()
	##      plate well controlStatus
	##   1     1  A01       unknown
	##   2     1  A02       unknown
	##   3     1  B01       unknown  
	
	##Check the third row
	## TODO create target object for all rows
	fd <- fData(r)[3,]
	checkEquals(target$fd,fd)
	
	##   4. SLOT PHENODATA
	##   str pdata(object)
	##   'data.frame':   .. obs. of  2 variables:
	##         $ replicate: int  1 2 3
	##   $ assay    :Class 'AsIs'  chr [1:..] "Dummy_experiment" 
	
	##   replicate            assay
	##   1         1 Dummy_experiment
	##   2         2 Dummy_experiment
	##   check the second row
	pd <- pData(r)[2,]
	checkEquals(target$pd,pd)
	
	##5. SLOT INTENSITYFILES
	checkEquals(target$intensityFiles,intensityFiles(r))
}






