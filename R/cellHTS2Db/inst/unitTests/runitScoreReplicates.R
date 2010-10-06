testScoreReplicates1 <- function(debug=FALSE) {
	
	# 1. PREPARE INPUT
	#the workdirectory is set by the Makefile to tests/ because there is doRUnit.R
	#paths are therefore relative to that directory
	#
	source("../inst/unitTests/createRca.R")
	source("../inst/unitTests/makeDummies.R")
	 
	rca <- createRca();
	rcan <- normalizePlates(rca,method="median");
		
	if (debug)
		debug(scoreReplicates) 

	# 2. RUN METHOD
	#	ScoreReplicates		
	rcans <- scoreReplicates(rcan, method="zscore")
	
	#3. CHECK UPDATES OF SLOTS
	dataScores <- Data(rcans)

	nrWells = 4
	nrPlates = 2
	nrReps = 2
	nrChannels = 1
	
	dimNames <- list(Features=c(1:8),Sample=c(1:2),Channels="ch1")
	dataScoresTarget <- array(as.numeric(NA), dim=c(nrWells * nrPlates,nrReps,nrChannels), dimnames=dimNames)
	
#	*  'method="zscore"' (robust _z_-scores), for each replicate,
#	this is calculated by subtracting the overall median from
#	each measurement and dividing the result by the overall
#	'mad'. These are estimated for each replicate by considering
#	the distribution of intensities (over all plates) in the
#	wells whose content is annotated as 'sample'.
	
#	# per plate genormaliseerde data 
#	replicate1:  c(-2, -1,  0,1 ,-2.5, -1.5, -0.5, 1.5)
#	replicate2:  c(-3, -2, -1,2 ,-3.5, -2.5, -1.5, 2.5)	
	
	## calculates the zscore per second (=replicate) and third(=channel) dimension.	
	#  samples A02,A02,B02,B04
	
	## calculation of mean and mad is done over all replicates
	## median(c(-1, 1, -1.5, 1.5)) = 0  #average  of -1 and 1
	## median(c(-2, 2, -2.5, 2.5)) = 0  # average of -2 and 2
	## mad(c(-1, 1, -1.5, 1.5)) =  1.85325
	## mad(c(-2, 2, -2.5, 2.5)) = 3.33585
	##  
	## f.e. rep 1 A01 : value -2
	## (-2 - 0) /1.85325 =-1.079185
	## f.e. rep 1 A02 : value -1
	## (-1 -0 )/1.85325 = -0.5395926
		
	dataScoresTarget[,1,1] <- c(-1.0791852,-0.5395926,0,0.5395926,-1.3489815,-0.8093889,-0.2697963,0.8093889)
	dataScoresTarget[,2,1] <- c(-0.899321,-0.5995473,-0.2997737,0.5995473,-1.0492078,-0.7494342,-0.4496605,0.7494342)
		
	checkEquals(dataScoresTarget,round(dataScores,7))

}

roundup3 <- function(x) {
	(trunc((x + 0.0005) * 1000))/1000
}

testScoresReplicatesMultiChannels <- function(debug=FALSE) {
	source("../inst/unitTests/createRca.R")
	source("../inst/unitTests/makeDummies.R")	
	rca <- createRca(testSet=4)
	
	if (debug) 
		debug(normalizePlates)
	
	#In case of multiple channels, you have to provide a value for the negControls
	#scale multiplicative
	rcan <- normalizePlates(rca,method="negatives",scale="multiplicative", negControls=c("NS","NS"));
	
	if (debug)
		debug(scoreReplicates) 
	
	# 2. RUN METHOD
	#	ScoreReplicates		
	rcans <- scoreReplicates(rcan, method="zscore")
	
	dataScores <- Data(rcans)
	
	dataScoresTarget <- makeScoresReplicatesultipleChannelsTarget()	
	
	browser()
	checkEquals(dataScoresTarget,round(dataScores,7)) #R rounds downwards	

	
	
	
}
