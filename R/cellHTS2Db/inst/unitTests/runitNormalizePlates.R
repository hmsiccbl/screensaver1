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


testNormMean <- function(debug=FALSE,withSlog=FALSE) {
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

testNormMedian <- function(debug=FALSE,withSlog=FALSE) {
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

testNormMedianWithSlog <- function(debug=FALSE) {
	testNormMedian(debug,TRUE)
}

 testNormShorth <- function(debug=FALSE,withSlog=FALSE) {
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
 
 testNormNegatives <- function(debug=FALSE,withSlog=FALSE) {
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
 
 #Function round in R rounds towards downwards, so 0.625 becomes 0.62
 roundup <- function(x) {
	 (trunc((x + 0.005) * 100))/100
 }
	 
 
 testNormNegMultiChannels <- function(debug=FALSE,withSlog=FALSE) {
	 source("../inst/unitTests/createRca.R")
	 
	 rca <- createRca(testSet=4,withSlog)
	 
	 if (debug) 
		 debug(normalizePlates)
	 
	 #In case of multiple channels, you have to provide a value for the negControls
	 #scale multiplicative
	 rcan <- normalizePlates(rca,method="negatives",scale="multiplicative", negControls=c("NS","NS"));
	 
	 dataNorm <- Data(rcan)
	 
	 dataNormTarget <- makeNormNegativesMultipleChannelsTarget()	
	 #browser()

	 checkEquals(dataNormTarget,roundup(dataNorm))
	 
 }
 
 testNormNegMultiChannels10 <- function(debug=FALSE,withSlog=FALSE) {
	 source("../inst/unitTests/createRca.R")
	 
	 rca <- createRca(testSet=5,withSlog)
	 
	 if (debug) 
		 debug(normalizePlates)
	 
	 #In case of multiple channels, you have to provide a value for the negControls
	 #scale multiplicative
	 rcan <- normalizePlates(rca,method="negatives",scale="multiplicative", negControls=rep("N",10));
	 
	 dataNorm <- Data(rcan)
	 
	 dataNormTarget <- makeNormNegMultiCh10Target()	
	 #browser()
	 
	 checkEquals(dataNormTarget,roundup(dataNorm))
	 
 }
 
 testNormNpi <- function(debug=FALSE,withSlog=FALSE) {
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
 
 testNormLoess <- function (debug=FALSE) {
	 
	 load("../data/jUnitLoess/rca.Rda")
	 
	 rcanActual <- normalizePlates(rca,scale="additive",method="loess",negControls=rep("N",1));

	 load("../data/jUnitLoess/rcan.Rda")
	 
	 checkEquals(Data(rcanActual),Data(rcan))
 }
 
