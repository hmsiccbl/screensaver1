makeTestSet <- function(withSlog =FALSE) {

	#1. PARAMETERS FOR THE READPLATELIST
	## A. GENERAL
	nrWells = 4 #per plate
	nrPlates = 2
	nrReps = 2
	nrChannels = 1
	
	xraw <- array(as.numeric(NA), dim=c(nrWells,nrPlates,nrReps,nrChannels))
	#plate 1, replicate 1
	p1r1 <- c(1,2,3,4)
	xraw[,1,1,] <- p1r1
	
	#plate 2, replicate 1
	p2r1 <- c(5,6,7,9)
	xraw[,2,1,] <- p2r1
	
	#plate 1, replicate 2
	p1r2 <- c(9,10,11,14)
	xraw[,1,2,] <- p1r2
	
	#plate 2, replicate 2
	p2r2 <- c(13,14,15,19)
	xraw[,2,2,] <- p2r2
	
	## 1.1 READPLATELISTCOMMON
	## dimPlate
	dimPlate <-  c(nrow=as.integer(2), ncol=as.integer(2))
	
	## pd
	vFileNames <- c("P1R1C1.txt","P1R2C1.txt","P2R1C1.txt","P2R2C1.txt")
	vPlates <- c(1,1,2,2)
	vRep <- c(1,2,1,2)
	vChannel <- c(1,1,1,1)
	
	pd <- data.frame(Filename=vFileNames, Plate=as.integer(vPlates) , Replicate=as.integer(vRep) ,Channel=as.integer(vChannel),stringsAsFactors=FALSE)
	
	## status		
	status =  rep("OK",nrow(pd))
	
	## intensityFile
	intensityFiles <- makeIntensityFiles()

	
	#2. CONF PARAMETER FOR THE CONFIGURATION
	#building conf object
	##  confFile : Same content als de config file (without the first two lines), f.e.
	#	Plate	Well	Content
	#	1	A01	pos
	#	1	A02	sample	
	#	1	B01	neg
	#	1	B02	sample
	
	#	but now in a 'data.frame', f.e.    3 obs. of  3 variables: 
	#		$ Plate  : chr  "*" "*" "*"  
	#		$ Well   : chr  "*" "A01" "B01"
	#		$ Content: chr  "sample" "pos" "neg"	
	conf <- data.frame(Plate=c("*","*","*"), Well=c("*","A01","B01"), Content=c("sample","pos","neg"),stringsAsFactors=FALSE)
	
	
	#3. ANNOTATION
	##  geneIDs : Same content als de geneID file  f.e.
	##		Plate	Well	HFAID		GeneID
	##		1		A01					MOCK
	##		1		A02		NM_018243	SEPT11
	##		1		B01					MOCK		
	##		1		B02		NM_001777	CD47		
	##		2		A01					MOCK
	##		2		A02		NM_001087	AAMP
	##		2		B01					MOCK		
	##		2		B02		NM_001778	CD48		
	
	##	but now in a 'data.frame', with NA for empty values:
	##$ Plate : int  1 1 1 ...
	##$ Well  : chr  "A01" "A02" "B01" "B02" (one character and two digits)
	##$ HFAID : chr  NA "NM_018243"	NA NM_001777"	
	##$ GeneID: chr  "MOCK" "SEPT11" "MOCK ""CD47"
	##----------------------------------------	
	
	plate <- c(1,1,1,1,2,2,2,2) 
	well <- rep(c("A01","A02","B01","B02"),2)
	hfaid <- c(NA,"NM_018243",NA,"NM_001777",NA,"NM_001087",NA,"NM_001778")
	geneid <- c("MOCK","SEPT11","MOCK","CD47","MOCK","AAMP","MOCK","CD48")
	geneIDs <- data.frame(Plate=plate, Well=well, HFAID=hfaid, GeneID=geneid,stringsAsFactors=FALSE)
	
	testSet <- list(xraw=xraw, dimPlate=dimPlate, pd=pd, status=status, intensityFiles=intensityFiles,
			nrRowsPlate=2, nrColsPlate=2, name="Dummy_experiment", conf=conf, geneIDs=geneIDs)

	if (withSlog) {
		slog <- data.frame(Plate=c(1),Well=c("A02"),Sample=c(1),Flag=c("NA"),stringsAsFactors=FALSE)
		listSlog <- list(slog=slog)
		testSet <- c(testSet, listSlog)
	}
	
	return(testSet)
}

makeIntensityFiles <- function() {
	nrPlates = 2
	nrReps=2
	nrChannels = 1
	intensityFilesTarget = vector(mode="list", length=nrPlates * nrReps * nrChannels)

	names(intensityFilesTarget) <- c("P1R1C1.txt","P1R2C1.txt","P2R1C1.txt","P2R2C1.txt")
	intensityFilesTarget[[1]] <- I(c("P1R1C1.txt\tA01\t1","P1R1C1.txt\tA02\t2","P1R1C1.txt\tB01\t3","P1R1C1.txt\tB02\t4"))
	intensityFilesTarget[[2]] <- I(c("P1R2C1.txt\tA01\t9","P1R2C1.txt\tA02\t10","P1R2C1.txt\tB01\t11","P1R2C1.txt\tB02\t14"))
	intensityFilesTarget[[3]] <- I(c("P2R1C1.txt\tA01\t5","P2R1C1.txt\tA02\t6","P2R1C1.txt\tB01\t7","P2R1C1.txt\tB02\t9"))
	intensityFilesTarget[[4]] <- I(c("P2R2C1.txt\tA01\t13","P2R2C1.txt\tA02\t14","P2R2C1.txt\tB01\t15","P2R2C1.txt\tB02\t19"))
	
	return(intensityFilesTarget)
	
}

makeTestSet2 <- function() {
	
	#1. PARAMETERS FOR THE READPLATELIST
	## A. GENERAL
	nrWells = 6
	nrPlates = 2
	nrReps = 2
	nrChannels = 1
	
	xraw <- array(as.numeric(NA), dim=c(nrWells,nrPlates,nrReps,nrChannels))
	#plate 1, replicate 1
	p1r1 <- c(1,2,9,3,4,7)
	xraw[,1,1,] <- p1r1
	
	#plate 2, replicate 1
	p2r1 <- c(5,6,8,7,9,4)
	xraw[,2,1,] <- p2r1
	
	#plate 1, replicate 2
	p1r2 <- c(9,10,3,11,14,6)
	xraw[,1,2,] <- p1r2
	
	#plate 2, replicate 2
	p2r2 <- c(13,14,10,15,19,5)
	xraw[,2,2,] <- p2r2
	
	## 1.1 READPLATELISTCOMMON
	## dimPlate
	dimPlate <-  c(nrow=as.integer(2), ncol=as.integer(3))
	
	## pd
	vFileNames <- c("P1R1C1.txt","P1R2C1.txt","P2R1C1.txt","P2R2C1.txt")
	vPlates <- c(1,1,2,2)
	vRep <- c(1,2,1,2)
	vChannel <- c(1,1,1,1)
	
	pd <- data.frame(Filename=vFileNames, Plate=as.integer(vPlates) , Replicate=as.integer(vRep) ,Channel=as.integer(vChannel),stringsAsFactors=FALSE)
	
	## status		
	status =  rep("OK",nrow(pd))
	
	## intensityFile
	intensityFiles <- makeIntensityFiles2()
	
	
	#2. CONF PARAMETER FOR THE CONFIGURATION
	#building conf object
	##  confFile : Same content als de config file (without the first two lines), f.e.
	#	Plate	Well	Content
	#	1	A01	pos
	#	1	A02	sample	
	#	1	A03	sample	
	#	1	B01	neg
	#	1	B02	sample
	#	1	B03	sample
	#
	#	but now in a 'data.frame', f.e.   
	#		$ Plate  : chr  "*" "*" "*"  
	#		$ Well   : chr  "*" "A01" "B01"
	#		$ Content: chr  "sample" "pos" "neg"	
	conf <- data.frame(Plate=c("*","*","*"), Well=c("*","A01","B01"), Content=c("sample","pos","neg"),stringsAsFactors=FALSE)
	
	
	#3. ANNOTATION
	##  geneIDs : Same content als de geneID file  f.e.
	##		Plate	Well	HFAID		GeneID
	##		1		A01					MOCK
	##		1		A02		NM_018243	SEPT11
	##		1		A03		NM_000001	AAP1
	##		1		B01					MOCK		
	##		1		B02		NM_001777	CD47
	##		1		B03		NM_000002   AAP2
	##		2		A01					MOCK
	##		2		A02		NM_001087	AAMP
	##		2		A03		NM_000003	AAP3
	##		2		B01					MOCK		
	##		2		B02		NM_001778	CD48
	##		2		B03		NM_000004	AAP4
			
	
	##	but now in a 'data.frame', with NA for empty values:
	##$ Plate : int  1 1 1 ...
	##$ Well  : chr  "A01" "A02" "B01" "B02" (one character and two digits)
	##$ HFAID : chr  NA "NM_018243"	NA NM_001777"	
	##$ GeneID: chr  "MOCK" "SEPT11" "MOCK ""CD47"
	##----------------------------------------	
	
	plate <- c(1,1,1,1,1,1,2,2,2,2,2,2) 
	well <- rep(c("A01","A02","A03","B01","B02","B03"),2)
	hfaid <- c(NA,"NM_018243","NM_000001",NA,"NM_001777","NM_000002",NA,"NM_001087","NM_000003",NA,"NM_001778","NM_000004")
	geneid <- c("MOCK","SEPT11","AAP1","MOCK","CD47","AAP2","MOCK","AAMP","AAP3","MOCK","CD48","AAP4")
	
	geneIDs <- data.frame(Plate=plate, Well=well, HFAID=hfaid, GeneID=geneid,stringsAsFactors=FALSE)
	
	testSet <- list(xraw=xraw, dimPlate=dimPlate, pd=pd, status=status, intensityFiles=intensityFiles,
			nrRowsPlate=2, nrColsPlate=3, name="Dummy_experiment", conf=conf, geneIDs=geneIDs)
	
}

makeIntensityFiles2 <- function() {
	nrPlates = 2
	nrReps=2
	nrChannels = 1
	intensityFilesTarget = vector(mode="list", length=nrPlates * nrReps * nrChannels)
	
#	p1r1 <- c(1,2,9,3,4,7)
#	xraw[,1,1,] <- p1r1
#	
#	#plate 2, replicate 1
#	p2r1 <- c(5,6,8,7,9,4)
#	xraw[,2,1,] <- p2r1
#	
#	#plate 1, replicate 2
#	p1r2 <- c(9,10,3,11,14,6)
#	xraw[,1,2,] <- p1r2
#	
#	#plate 2, replicate 2
#	p2r2 <- c(13,14,10,15,19,2)
#	xraw[,2,2,] <- p2r2
	
	names(intensityFilesTarget) <- c("P1R1C1.txt","P1R2C1.txt","P2R1C1.txt","P2R2C1.txt")
	intensityFilesTarget[[1]] <- I(c("P1R1C1.txt\tA01\t1","P1R1C1.txt\tA02\t2","P1R1C1.txt\tA03\t9","P1R1C1.txt\tB01\t3","P1R1C1.txt\tB02\t4","P1R1C1.txt\tB03\t7"))
	intensityFilesTarget[[2]] <- I(c("P1R2C1.txt\tA01\t9","P1R2C1.txt\tA02\t10","P1R2C1.txt\tA03\t3","P1R2C1.txt\tB01\t11","P1R2C1.txt\tB02\t14","P1R2C1.txt\tB03\t6"))
	intensityFilesTarget[[3]] <- I(c("P2R1C1.txt\tA01\t5","P2R1C1.txt\tA02\t6","P2R1C1.txt\tA03\t8","P2R1C1.txt\tB01\t7","P2R1C1.txt\tB02\t9","P2R1C1.txt\tB03\t4"))
	intensityFilesTarget[[4]] <- I(c("P2R2C1.txt\tA01\t13","P2R2C1.txt\tA02\t14","P2R2C1.txt\tA03\t10","P2R2C1.txt\tB01\t15","P2R2C1.txt\tB02\t19","P2R2C1.txt\tB03\t2"))
	
	return(intensityFilesTarget)
	
}

makeReadPlateListTarget <- function() {
	
	## CREATE PLATELIST TARGET
	vFileNames <- c("P1R1C1.txt","P1R2C1.txt","P2R1C1.txt","P2R2C1.txt")
	##Status will be later on updated in configure
	##nrChannels here: 1
	vStatus <- rep("OK",4)
	vPlates <- c(1,1,2,2)
	vRep <- c(1,2,1,2)
	vChannel <- c(1,1,1,1)
	plateListTarget <- data.frame(Filename=vFileNames, status=I(vStatus), Plate=as.integer(vPlates) , Replicate=as.integer(vRep) ,Channel=as.integer(vChannel),stringsAsFactors=FALSE)

	## CREATE DATA TARGET
	## In the assaydata slot, the data is now three in stead of 4 dimensional as the plates are now below each other
	## De dimensions are now:  "Features","Sample", "Channels" . Sample can also be read as "Replicate". See fData below for the details 
	
	nrWell =   4
	nrPlate =  2
	nrRep =    2
	nrChannel = 1
	dimNames <- list(Features=c(1:8),Sample=c(1:2),Channels="ch1")
	dataTarget <- array(as.numeric(NA), dim=c(nrWell * nrPlate,nrRep,nrChannel), dimnames=dimNames)
	dataTarget[,1,1] <- c(1,2,3,4,5,6,7,9)
	dataTarget[,2,1] <- c(9,10,11,14,13,14,15,19)
	
	## CREATE FD TARGET
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

	fdTarget <- data.frame(plate=as.integer(1),well= I("B01"),controlStatus=factor("unknown"),stringsAsFactors =FALSE)
	rownames(fdTarget) <- as.integer(3)
	
	
	
	## CREATE PD TARGET
	##   str pdata(object)
	##   'data.frame':   .. obs. of  2 variables:
	##         $ replicate: int  1 2 3
	##   $ assay    :Class 'AsIs'  chr [1:..] "Dummy_experiment" 
	
	##   replicate            assay
	##   1         1 Dummy_experiment
	##   2         2 Dummy_experiment
	##   check the second row
	## TODO create dummy data for whole PD not only second row
	pdTarget <- data.frame(replicate=as.integer(2),assay= I("Dummy_experiment"),stringsAsFactors =FALSE)   
	rownames(pdTarget) <- as.character(2)
	
	intensityFilesTarget <- makeIntensityFiles()
	
	target <- list(class="cellHTS", plateList=plateListTarget,data=dataTarget,fd=fdTarget,pd=pdTarget,intensityFiles=intensityFilesTarget)
	
	return(target)
	
	
	
}

makeNormMeanTarget <- function (withSlog=FALSE){
	## with the current dataset with just two sample wells the median and mean has the same result
	dataNormTarget <- makeNormMedianTarget()
	return(dataNormTarget)
}

makeNormMedianTarget <- function (withSlog=FALSE) {
	
	nrWells = 4
	nrPlates = 2
	nrReps = 2
	nrChannels = 1
	
	dimNames <- list(Features=c(1:8),Sample=c(1:2),Channels="ch1")
	dataNormTarget <- array(as.numeric(NA), dim=c(nrWells * nrPlates,nrReps,nrChannels), dimnames=dimNames)

	if (withSlog) {
		## samples in A02 and B02
		## p1r1 <- c(1,NA,3,4).  median(4) : 4 (value 2 -> NA because of log file)
		## p2r1 <- c(5,6,7,9).  median(6,9) : 7.5
		## norm = x - median (in case of additive data . In case of multiplicative data, norm = x/mean )
		dataNormTarget[,1,1] <- c(-3,NA,-1,0,-2.5,-1.5,-0.5, 1.5)
				
	} else {
		## samples in A02 and B02
		## p1r1 <- c(1,2,3,4).  median(2,4) : 3
		## p2r1 <- c(5,6,7,9).  media(6,9) : 7.5
		## norm = x - median
		dataNormTarget[,1,1] <- c(-2,-1,0,1,-2.5,-1.5,-0.5, 1.5)
	}	

	## p1r2 <- c(9,10,11,14). median(10,14) : 12
	## p2r2 <- c(13,14,15,19). median(14,19): 16.5
	dataNormTarget[,2,1] <- c(-3,-2,-1,2,-3.5,-2.5,-1.5,2.5)
	return(dataNormTarget)
	
}

makeNormShorthTarget <- function (withSlog=FALSE) {
	
	nrWells = 6
	nrPlates = 2
	nrReps = 2
	nrChannels = 1
	
	dimNames <- list(Features=c(1:12),Sample=c(1:2),Channels="ch1")
	dataNormTarget <- array(as.numeric(NA), dim=c(nrWells * nrPlates,nrReps,nrChannels), dimnames=dimNames)
	
	##  method="shorth" (scaling by the midpoint of the shorth), for each plate and replicate, the midpoint of the
	## 'shorth' of the distribution of values in the wells annotated  as 'sample' is calculated. Then, every measurement is divided by this value.
	
#	if (withSlog) {
#		## samples in A02,A03,B02,B03
#		## p1r1 <- c(). 
#		## p2r1 <- c().  
#		dataNormTarget[,1,1] <- 
#		
#	} else {
		## samples in A02 and B02
		## p1r1 <- c(1,2,3,4).  shorth(2,4) : 
		## p2r1 <- c(5,6,7,9).  media(6,9) : 
		## norm = x - median
		dataNormTarget[,1,1] <- c(-2,-1,6,0,1,4,-2,-1,1,0,2,-3)
		
	## }	
	
	## p1r2 <- c(9,10,11,14). shorth c(10,14) : 12
	## p2r2 <- c(13,14,15,19). shorth(14,19): 16.5
	dataNormTarget[,2,1] <- c(4.5,5.5,-1.5,6.5,9.5,1.5,5.5,6.5,2.5,7.5,11.5,-2.5)
	return(dataNormTarget)
	
}

makeNormNegativesTarget <- function () {
	
	nrWells = 6
	nrPlates = 2
	nrReps = 2
	nrChannels = 1
	
	dimNames <- list(Features=c(1:12),Sample=c(1:2),Channels="ch1")
	dataNormTarget <- array(as.numeric(NA), dim=c(nrWells * nrPlates,nrReps,nrChannels), dimnames=dimNames)

	## *  If 'method="negatives"' (scaling by the negative controls), for each plate and replicate, each measurement is divided by
	## the median of the measurements on the plate negative controls.

	## p1r1 <- c(1,2,9,3,4,7) BO1: 3
	## p2r1 <- c(5,6,8,7,9,4) BO1: 7
	
	dataNormTarget[,1,1] <- c(-2,-1,6,0,1,4,-2,-1,1,0,2,-3)

	
	## p1r2 <- c(9,10,3,11,14,6) : B01: 11
	## p2r2 <- c(13,14,10,15,19,5) BO1: 15
	dataNormTarget[,2,1] <- c(-2,-1,-8,0,3,-5,-2,-1,-5,0,4,-10)
	return(dataNormTarget)
	
}

