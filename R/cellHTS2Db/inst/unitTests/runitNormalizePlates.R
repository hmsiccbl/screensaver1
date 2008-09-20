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
#	 shorth calculates descriptive statistics based on the shortest half of the distribution of each variable or group specified: 
#			 the shorth, the mean of values in that shortest half; 
#	 the midpoint of that half, which is the least median of squares estimate of location; and the length of the shortest half.
#	According to the vignette the midpoint is calculated, which is confusing because with shorth is normally the mean. 	 
	 
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
 
 
