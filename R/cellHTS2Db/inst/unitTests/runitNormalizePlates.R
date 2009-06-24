## TODO . Write RUnits for 	
## POC
## negatives
## NPI
## mean
## median
## shorth
## Bscore
## locfit
## loess


testNormalizeMean <- function(debug=FALSE,withSlog=FALSE) {
	source("../inst/unitTests/createRca.R")
	rca <- createRca(testSet=1,withSlog=withSlog)
	
	if (debug) 
		debug(normalizePlates)
	
	rcan <- normalizePlates(rca,method="mean");
	
	dataNorm <- Data(rcan)
	
	dataNormTarget <- makeNormMeanTarget(withSlog)	
	#browser()
	checkEquals(dataNormTarget,dataNorm)
	
}

testNormalizeMedian <- function(debug=FALSE,withSlog=FALSE) {
	source("../inst/unitTests/createRca.R")
	rca <- createRca(testSet=1,withSlog=withSlog)
	
	if (debug) 
		debug(normalizePlates)
	
	#see if annotation is required for normalizePlates
	rcan <- normalizePlates(rca,method="median");
	
	dataNorm <- Data(rcan)

	dataNormTarget <- makeNormMedianTarget(withSlog)	
	#browser()
	checkEquals(dataNormTarget,dataNorm)
	
}

testNormalizeMedianWithSlog <- function(debug=FALSE) {
	testNormalizeMedian(debug,TRUE)
}

 testNormalizeShorth <- function(debug=FALSE,withSlog=FALSE) {
     source("../inst/unitTests/createRca.R")
#	 shorth calculates descriptive statistics based on the shortest half of the distribution of each variable or group specified: 
#			 the shorth, the mean of values in that shortest half; 
#	 the midpoint of that half, which is the least median of squares estimate of location; and the length of the shortest half.
#	According to the vignette the midpoint is calculated, which is confusing because with shorth is normally the mean. 	 
	 
     rca <- createRca(testSet=2,withSlog)
 
     if (debug) 
         debug(normalizePlates)
 
     #see if annotation is required for normalizePlates
     rcan <- normalizePlates(rca,method="shorth");
 
     dataNorm <- Data(rcan)
 
     dataNormTarget <- makeNormShorthTarget(withSlog)	
     #browser()
     checkEquals(dataNormTarget,dataNorm)
 
 }
 
 testNormalizeNegatives <- function(debug=FALSE,withSlog=FALSE) {
	 source("../inst/unitTests/createRca.R")
	 
	 rca <- createRca(testSet=2,withSlog)
	 
	 if (debug) 
		 debug(normalizePlates)
	 
	 #see if annotation is required for normalizePlates
	 # default additive, than the median of thet negative controls will be subtracted from the measure value 
	 rcan <- normalizePlates(rca,method="negatives",negControls=c("N"));
	 
	 dataNorm <- Data(rcan)
	 
	 dataNormTarget <- makeNormNegativesTarget()	
	 #browser()
	 checkEquals(dataNormTarget,dataNorm)
	 
 }
 
 testNormalizeLoess <- function(debug=FALSE,withSlog=FALSE) {
	source("../inst/unitTests/createRca.R")
	 
	## *  'method="loess"' (loess regression): for each plate and
	## replicate, spatial effects are removed by fitting a loess
	## curve (see 'spatial normalization function').
	## help(spatialNormalization) 
	Examples:
			
	data(KcViabSmall)
	x <- KcViabSmall
	xs <- spatialNormalization(x, model="loess", save.model = TRUE)
	## Calling spatialNormalization function from "normalizePlates":
	xopt <- normalizePlates(x, method="loess", varianceAdjust="none", save.model = TRUE)
	all(xs@rowcol.effects == xopt@rowcol.effects, na.rm=TRUE)
	
		rca <- createRca(testSet=2,withSlog)
	 
	 if (debug) 
		 debug(normalizePlates)
	 
	 #see if annotation is required for normalizePlates
	 rcan <- normalizePlates(rca,method="negatives");
	 
	 dataNorm <- Data(rcan)
	 
	 dataNormTarget <- makeNormNegativesTarget()	
	 #browser()
	 checkEquals(dataNormTarget,dataNorm)
	 
 }
 
 
 
 testNormalizeNpi <- function(debug=FALSE,withSlog=FALSE) {
	 source("../inst/unitTests/createRca.R")
	 
	 rca <- createRca(testSet=1,withSlog)
	 
	 if (debug) 
		 debug(normalizePlates)
	 
	 #see if annotation is required for normalizePlates
	 rcan <- normalizePlates(rca,method="NPI");
	 
	 dataNorm <- Data(rcan)
	 
	 dataNormTarget <- makeNormNpiTarget()	
	 #browser()
	 checkEquals(dataNormTarget,dataNorm)
	 
 }
 
