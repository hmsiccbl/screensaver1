createIntensityFile <- function(fileName,xraw,plateId,replicateId,channelId,pdim) {
	## An IntensityFile contains data for one specific combination of plate, replicate and channel
	## Structure of an intensity file dataobject:
	##	Class 'AsIs'  chr [1:4] "P1R1C1.txt\tA01\t1" "P1R1C1.txt\tA02\t2" "P1R1C1.txt\tB01\t3" "P1R1C1.txt\tB02\t4"
	
	## Get the number of elements in the intentsity file array. This is equal to the number of wells
	xrawd <- dim(xraw)
	nrWells =   xrawd[1]
	
	## Get seperate arrays with the values for fileNames, wells and intensity measurements 
	## The elements in the intensity file char array can afterwards created at once using the paste function 

	## All elements start with the name of the (same) intensity file			
	fileNames <- rep(fileName, nrWells)

	## Get the wells in the letnum notation
	wells <- convertWellCoordinates(c(1:nrWells),pdim)$letnum
	
	## Get the intensity data for the given plateId, replicateId and channelId
	intensities <- xraw[,plateId,replicateId,channelId]

	## Create all the array elements at once, using the paste function				
	intensityFile <- I(paste(fileNames,wells,intensities,sep="\t"))
	
	return (intensityFile)
}

createPlateListAndIntensityFiles <- function(xraw , pdim) {
	## PURPOSE: BUILDING THE DATAFRAMES FOR THE SLOTS OF THE CELLHTS-OBJECT 'PLATELIST' AND 'THE INTENSITY FILES'
	## Is needed in f.e. by writeReport to create the separate intensity files on the filesystem.
	##  
	## Although seperate dataframes, the most efficient way to create them is using the same loop. The function returns them 
	## as seperate parts of a list. 
	##
	## In a file the content the platelist looks like:
	## Filename Plate Replicate Channel
	## DRUG-P1R1C1.txt     1         1	1
	## DRUG-P1R2C1.txt     1         1	1
	##
	## This is transferred into a dataobject with the structure:
	## 'data.frame':   .. obs. of  .. variables:
	## $ Filename : chr  "DRUG-P1R1.txt" "DRUG-P1R2.txt" "DRUG-P1R3.txt" "DRUG-P2R1.txt" ...
	## $ Plate    : int  1 1 1 2 2 2 3 3 3 4 ...
	## $ Replicate: int  1 2 3 1 2 3 1 2 3 1 ...
	## $ Channel  : int  1 1 1 1 1 1 1 1 1 1 ..
	## 
	## WriteReport (and probably normalizePlates, scoreReplicates and summarizeReplicates) assumes that Plates start with 1, compare it's following code:	
	## with(plateList(xr), which(Plate==p & status=="OK")) 
	##	for(p in 1:nrPlate){
	##		wh = with(plateList(xr), which(Plate==p & status=="OK"))
	## 
	## When starting platenumbering with one, there is no need to add plate names to the xraw1 variable.
	
	xrawd <- dim(xraw)
	
	nrWells =   xrawd[1]
	nrPlates =  xrawd[2]
	nrReps =    xrawd[3]
	nrChannels = xrawd[4]
	
	vChannel <- 1:nrChannels
	
	## This will repeat for every replicate
	vChannel <- rep(vChannel,nrReps)
	
	## And this will repeat for every plate
	vChannel <- rep(vChannel,nrPlates)
	
	##per plate, the pattern of the replicates is: each value  is presented equal to the number of channels  (see example above)
	vRepPattern <- c()
	for (rep in 1 : nrReps)
		for (ch in 1 : nrChannels)
			vRepPattern <- c(vRepPattern,rep)		
	
	## This will repeat for every plate
	vRep <- rep(vRepPattern,nrPlates)
	
	##For the plates: each value is presented: for the length of the vRepPattern (see example above)
	lRep <- length(vRepPattern)
	vPlates <- c()
	for (pl in 1: nrPlates)
		vPlates <- c(vPlates, rep(pl,lRep))
	
	vFileNames <- c()
	intensityFiles = vector(mode="list", length=nrPlates * nrReps * nrChannels)

	i <- 0
	for (pl in 1:nrPlates)
		for (rep in 1: nrReps)
			for (ch in 1: nrChannels) {
				##TODO: perhaps start the name of the file with the short name of the screen result
				## perhaps leave the channel part out if just one channel
				fileName <- paste("P",pl,"R",rep,"C",ch,".txt",sep="")
				vFileNames <- c(vFileNames, fileName)
				i <- i + 1
				names(intensityFiles)[i] <- fileName
				intensityFiles[[i]] <- createIntensityFile(fileName,xraw,pl,rep,ch,pdim)
			}
	
	pd <- data.frame(Filename=vFileNames, Plate=as.integer(vPlates) , Replicate=as.integer(vRep) ,Channel=as.integer(vChannel),stringsAsFactors=FALSE)
	result <- list(pd=pd, intensityFiles=intensityFiles)
	return(result)
}


readPlateListDb <- function(xraw ,name, nrRowsPerPlate,nrColsPerPlate) { 
		
	dimPlate = c(nrow=as.integer(nrRowsPerPlate), ncol=as.integer(nrColsPerPlate))
	nrWell = prod(dimPlate)
	
	result <- createPlateListAndIntensityFiles(xraw=xraw ,pdim=dimPlate) 
			
	pd <- result$pd
	intensityFiles <- result$intensityFiles
	
	combo = paste(pd$Plate, pd$Replicate,pd$Channel)
	
	##channel <-  pd$Channel

	## Further on in in the platelist is inserted a column 'status'. Status other than "OK" indicates problems with
	## reading the files from the filesystem into the xraw array.
	## The column 'status'' is used in f.e. writeReport step 3 QC per plate per channel to subselect only the plates with status "OK".
	## Checks are there for the following possible problems:  the plateList.txt file is missing, one or more intensity files are missing, 
	## problems with using the userdefined importFun, converting the Wellids to a number or building up the xraw-array. 	
	
	## TODO check if reading data from the database into the xraw-array can have plates specific or general problems. If so, 
	## the status per plate should be set. For now 
	status =  rep("OK",nrow(pd))
	
	res <- readPlateListCommon(xraw,name,dimPlate,pd,status,intensityFiles)
#	
#	
#	
#	## output the possible errors that were encountered along the way:
#	whHadProbs = which(status!="OK")
#	if(length(whHadProbs)>0 & verbose) {
#		idx = whHadProbs[1:min(5, length(whHadProbs))]
#		msg = paste("Please check the following problems encountered while reading the data:\n",
#				"\tFilename \t Error\n", "\t",
#				paste(plateList(res)$Filename[idx], status[idx], sep="\t", collapse="\n\t"),
#				if(length(whHadProbs)>5) sprintf("\n\t...and %d more.\n", length(whHadProbs)-5), "\n", sep="")
#		stop(msg)
#	}
#	
	return(res)
}
