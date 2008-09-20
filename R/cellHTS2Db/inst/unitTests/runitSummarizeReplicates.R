# runitSummarizeReplicates.R
# 
# TODO: Add comment
#
# Author: cor
###############################################################################
## TODO build RUnit tests for other values of parameter 'summary'
testSummarizeReplicates1 <- function(debug=FALSE) {
	
	##1. PREPARE INPUT
	source("../inst/unitTests/createRca.R")
	rca <- createRca()
	rcan <- normalizePlates(rca,method="median");
	rcans <- scoreReplicates(rcan, method="zscore")
	
	##2. RUN METHOD SUMMARIZEREPLICATES
	rcanss <- summarizeReplicates(rcans,summary="mean");
	
	##3. CHECKEQUALS
	dataSummarized <- Data(rcanss)
	
	nrWells = 4
	nrPlates = 2
	nrChannels = 1
	
	dimNames <- list(Features=c(1:8),Sample=c(1),Channels="score") ##Why this is called score in stead of ch1??
	dataSummarizedTarget <- array(as.numeric(NA), dim=c(nrWells * nrPlates,1,nrChannels), dimnames=dimNames)

	dataSummarizedTarget[,1,1] <- c(-0.98925310,-0.56957000,-0.14988680,0.56957000,-1.19909470,-0.77941150,-0.35972840,0.77941150)
	
	checkEquals(dataSummarizedTarget,round(dataSummarized,7))
	
}