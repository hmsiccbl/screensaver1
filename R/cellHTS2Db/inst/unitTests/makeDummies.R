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
	negAnno <- "N"
	conf <- data.frame(Plate=c("*","*","*"), Well=c("*","A01","B01"), Content=c("sample","pos",negAnno),stringsAsFactors=FALSE)
	
	
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


#Test set especially created for writeReport. In the report the "Screen-wide image plot of the scored values"
# must have a minimal width, because otherwise there will be an error due to the plotting of the colorbar below the image
# An image of one plate with 384 wells will do. I took from the KcViab example of cellHTS2 the file FT01-GO1.txt

makeTestSet3 <- function() {
	#1. PARAMETERS FOR THE READPLATELIST
	## A. GENERAL
	nrWells = 384
	nrPlates = 1
	nrReps = 1
	nrChannels = 1
	
	xraw <- array(as.numeric(NA), dim=c(nrWells,nrPlates,nrReps,nrChannels))
	
	d <- read.table(file="../inst/unitTests/FT01-G01.txt", sep="\t", header=TRUE, stringsAsFactors=FALSE, na.string="", quote="\"", fill=FALSE)
	
	#plate 1, replicate 1
	xraw[,1,1,] <- d$value
	
	## 1.1 READPLATELISTCOMMON
	## dimPlate
	dimPlate <-  c(nrow=as.integer(16), ncol=as.integer(24))
	
	## pd
	vFileNames <- "FT01-G01.txt"
	vPlates <- c(1)
	vRep <- c(1)
	vChannel <- c(1)
	
	pd <- data.frame(Filename=vFileNames, Plate=as.integer(vPlates) , Replicate=as.integer(vRep) ,Channel=as.integer(vChannel),stringsAsFactors=FALSE)
	
	## status		
	status =  rep("OK",nrow(pd))
	intensityFiles = vector(mode="list", length=nrPlates * nrReps * nrChannels)
	names(intensityFiles) <- "FT01-G01.txt"
	intensityFiles[[1]] <- I(paste(d$filename,d$well,d$value,sep="\t"))
	
	#2. CONF PARAMETER FOR THE CONFIGURATION
	## Wells: 384
	## Plates: 57
	## Plate	Well	Content
	## *	*	sample
	## *	A0[1-2]	other
	## *	B01	neg
	## *	B02	pos
	conf <- data.frame(Plate=c("*","*","*","*"), Well=c("*","A0[1-2]","B01","B02"), Content=c("sample","other","neg","pos"),stringsAsFactors=FALSE)
	
	
	#3. ANNOTATION
	##  geneIDs : Same content als de geneID file  f.e.
	##		Plate	Well	HFAID		GeneID
	##		1		A01					MOCK
	##		1		A02		NM_018243	SEPT11
	##	but now in a 'data.frame', with NA for empty values:
	##$ Plate : int  1 1 1 ...
	##$ Well  : chr  "A01" "A02" "B01" "B02" (one character and two digits)
	##$ HFAID : chr  NA "NM_018243"	NA NM_001777"	
	##$ GeneID: chr  "MOCK" "SEPT11" "MOCK ""CD47"
	##----------------------------------------	
	geneIDs <- read.table(file="../inst/unitTests/KcViab_geneIds.txt", sep="\t", header=TRUE, stringsAsFactors=FALSE, na.string="", quote="\"", fill=FALSE)
	
	testSet <- list(xraw=xraw, dimPlate=dimPlate, pd=pd, status=status, intensityFiles=intensityFiles,
			nrRowsPlate=16, nrColsPlate=24, name="Dummy_experiment 3", conf=conf, geneIDs=geneIDs)
	
}

# Testset for multiple channels
makeTestSet4 <- function(withSlog=FALSE) {
	## plate	cell	r1c1	r1c2	r2c1	r2c2
	## 1	A01	1	4	9	6
	## 1	A02	2	6	10	8
	## 1	B01	3	5	11	7
	## 1	B02	4	6	14	8
	## 1	C01	2	8	6	10
	## 1	C02		7		9
	## 2	A01	5	14	13	16
	## 2	A02	6	16	14	18
	## 2	B01	7	15	15	17
	## 2	B02	9	16	19	18
	## 2	C01	4	18	8	20
	## 2	C02		17		19
	
	#1. PARAMETERS FOR THE READPLATELIST
	## A. GENERAL
	nrWells = 6
	nrPlates = 2
	nrReps = 2
	nrChannels = 2
	
	xraw <- array(as.numeric(NA), dim=c(nrWells,nrPlates,nrReps,nrChannels))
	#p1r1c1
	xraw[,1,1,1] <- c(1,2,3,4,2,NA)
	
	#p1r1c2
	xraw[,1,1,2] <-  c(4,6,5,6,8,7)
	
	#p1r2c1
	xraw[,1,2,1] <- c(9,10,11,14,6,NA)
	
	#p1r2c2
	xraw[,1,2,2] <- c(6,8,7,8,10,9)

	xraw[,2,1,1] <- c(5,6,7,9,4,NA)
	
	#p2r1c2
	xraw[,2,1,2] <-  c(14,16,15,16,18,17)
	
	#p2r2c1
	xraw[,2,2,1] <- c(13,14,15,19,8,NA)
	
	#p2r2c2
	xraw[,2,2,2] <- c(16,18,17,18,20,19)
	
	## 1.1 READPLATELISTCOMMON
	## dimPlate
	dimPlate <-  c(nrow=as.integer(3), ncol=as.integer(2))
	
	## pd
	vFileNames <- c("P1R1C1.txt","P1R1C2.txt","P1R2C1.txt","P1R2C2.txt","P2R1C1.txt","P2R1C2.txt","P2R2C1.txt","P2R2C2.txt")
	vPlates <- c(1,1,1,1,2,2,2,2)
	vRep <- rep(c(1,1,2,2),2)
	vChannel <- rep(c(1,2,1,2),2)
	
	pd <- data.frame(Filename=vFileNames, Plate=as.integer(vPlates) , Replicate=as.integer(vRep) ,Channel=as.integer(vChannel),stringsAsFactors=FALSE)
	
	## status		
	status =  rep("OK",nrow(pd))
	
	## intensityFile
	intensityFiles <- makeIntensityFiles4()
		
	#2. CONF PARAMETER FOR THE CONFIGURATION
	#building conf object
	##  confFile : Same content als de config file (without the first two lines), f.e.
	#	Plate	Well	Content
	#	1	A01	pos
	#	1	A02	sample	
	#	1	BO1 N	
	#	1	B02	sample
	#	1	C01	NS
	#	1	C02	sample
	#
	#	but now in a 'data.frame', f.e.   
	conf <- data.frame(Plate=c("*"), Well=c("A01","A02","B01","B02","C01","C02"), 
				Content=c("pos","sample","N","sample","NS","sample"),stringsAsFactors=FALSE)
	
	
	plate <- c(rep(1,6),rep(2,6)); 
	well <- rep(c("A01","A02","B01","B02","C01","C02"),2)
	hfaid <- c(NA,"NM_000001",NA,"NM_000002",NA,"NM_000003",NA,"NM_000004",NA,"NM_000005",NA,"NM_000006")
	geneid <- c("","AAP1","","AAP2","","AAP3","","AAP4","","AAP5","","AAP6")
	
	geneIDs <- data.frame(Plate=plate, Well=well, HFAID=hfaid, GeneID=geneid,stringsAsFactors=FALSE)
	
	testSet <- list(xraw=xraw, dimPlate=dimPlate, pd=pd, status=status, intensityFiles=intensityFiles,
			name="Dummy_experiment", conf=conf, geneIDs=geneIDs)
	
	if (withSlog) { 
	slog <- data.frame(Plate=c(1),Well=c("A02"),Sample=c(1),Flag=c("NA"),Channel=c(2), stringsAsFactors=FALSE)
		listSlog <- list(slog=slog)
		testSet <- c(testSet, listSlog)
	}
	
	return(testSet)
}	

makeIntensityFiles4 <- function() {
	nrPlates = 2
	nrReps=2
	nrChannels = 2
	intensityFilesTarget = vector(mode="list", length=nrPlates * nrReps * nrChannels)
	
	names(intensityFilesTarget) <- c("P1R1C1.txt","P1R1C2.txt","P1R2C1.txt","P1R2C2.txt","P2R1C1.txt","P2R1C2.txt","P2R2C1.txt","P2R2C2.txt")
	intensityFilesTarget[[1]] <- I(c("P1R1C1.txt\tA01\t1","P1R1C1.txt\tA02\t2","P1R1C1.txt\tB01\t3","P1R1C1.txt\tB02\t4","P1R1C1.txt\tC01\t2","P1R1C1.txt\tC02\tNA"))
	intensityFilesTarget[[2]] <- I(c("P1R1C2.txt\tA01\t4","P1R1C2.txt\tA02\t6","P1R1C2.txt\tB01\t5","P1R1C2.txt\tB02\t6","P1R1C2.txt\tC01\t8","P1R1C2.txt\tC02\t7"))
	intensityFilesTarget[[3]] <- I(c("P1R2C1.txt\tA01\t9","P1R2C1.txt\tA02\t10","P1R2C1.txt\tB01\t11","P1R2C1.txt\tB02\t14","P1R2C1.txt\tC01\t6","P1R2C1.txt\tC02\tNA"))
	intensityFilesTarget[[4]] <- I(c("P1R2C2.txt\tA01\t6","P1R2C2.txt\tA02\t8","P1R2C2.txt\tB01\t7","P1R2C2.txt\tB02\t8","P1R2C2.txt\tC01\t10","P1R2C2.txt\tC02\t9"))
	intensityFilesTarget[[5]] <- I(c("P2R1C1.txt\tA01\t5","P2R1C1.txt\tA02\t6","P2R1C1.txt\tB01\t7","P2R1C1.txt\tB02\t9","P2R1C1.txt\tC01\t4","P2R1C1.txt\tC02\tNA"))
	intensityFilesTarget[[6]] <- I(c("P2R1C2.txt\tA01\t14","P2R1C2.txt\tA02\t16","P2R1C2.txt\tB01\t15","P2R1C2.txt\tB02\t16","P2R1C2.txt\tC01\t18","P2R1C2.txt\tC02\t17"))
	intensityFilesTarget[[7]] <- I(c("P2R2C1.txt\tA01\t13","P2R2C1.txt\tA02\t14","P2R2C1.txt\tB01\t15","P2R2C1.txt\tB02\t19","P2R2C1.txt\tC01\t8","P2R2C1.txt\tC02\tNA"))
	intensityFilesTarget[[8]] <- I(c("P2R2C2.txt\tA01\t16","P2R2C2.txt\tA02\t18","P2R2C2.txt\tB01\t17","P2R2C2.txt\tB02\t18","P2R2C2.txt\tC01\t20","P2R2C2.txt\tC02\t19"))
	
	return(intensityFilesTarget)
	
}

# Testset for 10 channels as this had a bug
makeTestSet5 <- function() {
	
	## plate cell	c1	c2	c3	c4	c5	c6	c7	c8	c9	c10
	## 1	A01	1	3	9	6	5	7	9	11	13	15
	## 1	A02	2	6	10	8	3	5	7	9	11	13
	
	#1. PARAMETERS FOR THE READPLATELIST
	## A. GENERAL
	nrWells = 2
	nrPlates = 1
	nrReps = 1
	nrChannels = 10
	
	xraw <- array(as.numeric(NA), dim=c(nrWells,nrPlates,nrReps,nrChannels))
	#p1 A01
	xraw[1,1,1,] <- c(1,3,9,6,5,7,9,11,13,15)
	
	#p1 A02
	xraw[2,1,1,] <-  c(2,6,10,8,3,5,7,9,11,13)
	
	
	## 1.1 READPLATELISTCOMMON
	## dimPlate
	dimPlate <-  c(nrow=as.integer(1), ncol=as.integer(2))
	
	## pd
	vFileNames <- c("P1R1C1.txt","P1R1C2.txt","P1R1C3.txt","P1R1C4.txt","P1R1C5.txt","P1R1C6.txt","P1R1C7.txt","P1R1C8.txt","P1R1C9.txt","P1R1C10.txt")
	vPlates <- rep(1,10)
	vRep <- rep(1,10)
	vChannel <- c(1:10)
	
	pd <- data.frame(Filename=vFileNames, Plate=as.integer(vPlates) , Replicate=as.integer(vRep) ,Channel=as.integer(vChannel),stringsAsFactors=FALSE)
	
	## status		
	status =  rep("OK",nrow(pd))
	
	## intensityFile
	intensityFiles <- makeIntensityFiles5()
	
	#2. CONF PARAMETER FOR THE CONFIGURATION
	#building conf object
	##  confFile : Same content als de config file (without the first two lines), f.e.
	#	Plate	Well	Content
	#	1	A01	N
	#	1	A02	sample	
	#
	#	but now in a 'data.frame', f.e.   
	conf <- data.frame(Plate=c("*"), Well=c("A01","A02"), 
			Content=c("N","sample"),stringsAsFactors=FALSE)
	
	plate <- rep(1,2); 
	well <- c("A01","A02")
	hfaid <- c(NA,"NM_000001")
	geneid <- c("","AAP1")
	
	geneIDs <- data.frame(Plate=plate, Well=well, HFAID=hfaid, GeneID=geneid,stringsAsFactors=FALSE)
	
	testSet <- list(xraw=xraw, dimPlate=dimPlate, pd=pd, status=status, intensityFiles=intensityFiles,
			name="Dummy_experiment", conf=conf, geneIDs=geneIDs)

}	

makeIntensityFiles5 <- function() {
	nrPlates = 1
	nrReps=1
	nrChannels = 10
	intensityFilesTarget = vector(mode="list", length=nrPlates * nrReps * nrChannels)
	
	names(intensityFilesTarget) <- c("P1R1C1.txt","P1R1C2.txt","P1R1C3.txt","P1R1C4.txt","P1R1C5.txt","P1R1C6.txt","P1R1C7.txt","P1R1C8.txt","P1R1C9.txt","P1R1C10.txt")
	## plate cell	c1	c2	c3	c4	c5	c6	c7	c8	c9	c10
	## 1	A01	1	3	9	6	5	7	9	11	13	15
	## 1	A02	2	6	10	8	3	5	7	9	11	13
	intensityFilesTarget[[1]] <- I(c("P1R1C1.txt\tA01\t1","P1R1C1.txt\tA02\t2"))
	intensityFilesTarget[[2]] <- I(c("P1R1C2.txt\tA01\t3","P1R1C2.txt\tA02\t6"))
	intensityFilesTarget[[3]] <- I(c("P1R1C3.txt\tA01\t9","P1R1C3.txt\tA02\t10"))
	intensityFilesTarget[[4]] <- I(c("P1R1C4.txt\tA01\t6","P1R1C4.txt\tA02\t8"))
	intensityFilesTarget[[5]] <- I(c("P1R1C5.txt\tA01\t5","P1R1C5.txt\tA02\t3"))
	intensityFilesTarget[[6]] <- I(c("P1R1C6.txt\tA01\t7","P1R1C6.txt\tA02\t5"))
	intensityFilesTarget[[7]] <- I(c("P1R1C7.txt\tA01\t9","P1R1C7.txt\tA02\t7"))
	intensityFilesTarget[[8]] <- I(c("P1R1C8.txt\tA01\t11","P1R1C8.txt\tA02\t9"))
	intensityFilesTarget[[9]] <- I(c("P1R1C9.txt\tA01\t13","P1R1C9.txt\tA02\t11"))
	intensityFilesTarget[[10]] <- I(c("P1R1C10.txt\tA01\t15","P1R1C10.txt\tA02\t13"))
	
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

makeNormNegativesMultipleChannelsTarget <- function () {
	
	nrWells = 6
	nrPlates = 2
	nrReps = 2
	nrChannels = 2
	
	dimNames <- list(Features=c(1:(nrWells * nrPlates)),Sample=c(1,2),Channels=c("ch1","ch2"))
	dataNormTarget <- array(as.numeric(NA), dim=c(nrWells * nrPlates,nrReps,nrChannels), dimnames=dimNames)
	
	#r1_c1 
	dataNormTarget[,1,1] <- c(0.5,1,1.5,2,1,NA,1.25,1.5,1.75,2.25,1,NA)
	
	#r2_c1 
	dataNormTarget[,2,1] <-  c(1.5,1.67,1.83,2.33,1,NA,1.63,1.75,1.88,2.38,1,NA)
	
	#r1_c2 
	dataNormTarget[,1,2] <- c(0.5,0.75,0.63,0.75,1,0.88,0.78,0.89,0.83,0.89,1,0.94)

	#r2_c2 
	dataNormTarget[,2,2] <- c(0.6,0.8,0.7,0.8,1,0.9,0.8,0.9,0.85,0.9,1,0.95)
	
	return(dataNormTarget)
	
}

makeNormNegMultiCh10Target <- function () {
	
	nrWells = 2
	nrPlates = 1
	nrReps = 1
	nrChannels = 10
	
	dimNames <- list(Features=c(1:(nrWells * nrPlates)),Sample="1",Channels=c("ch01","ch02","ch03","ch04","ch05","ch06","ch07","ch08","ch09","ch10"))
	dataNormTarget <- array(as.numeric(NA), dim=c(nrWells * nrPlates,nrReps,nrChannels), dimnames=dimNames)
	
	## Original values
	## plate cell	c1	c2	c3	c4	c5	c6	c7	c8	c9	c10
	## 1	A01	1	3	9	6	5	7	9	11	13	15
	## 1	A02	2	6	10	8	3	5	7	9	11	13
	## Result values (value second row, divided by value first raw (the N well)
	
	
	#A01. Divided by itself always 1
	dataNormTarget[1,1,] <- rep(1,10)
	
	#A02 . 
	dataNormTarget[2,1,] <-  c(2,2,1.11,1.33,0.6, 0.71,0.78,0.82, 0.85, 0.87)
	
	return(dataNormTarget)
	
}

makeNormNpiTarget <- function () {
	
	nrWells = 4
	nrPlates = 2
	nrReps = 2
	nrChannels = 1
	
	dimNames <- list(Features=c(1:8),Sample=c(1:2),Channels="ch1")
	dataNormTarget <- array(as.numeric(NA), dim=c(nrWells * nrPlates,nrReps,nrChannels), dimnames=dimNames)
	## Fun in cellHTS2 code
	## (mean(a[pos], na.rm = TRUE) - a)/(mean(a[pos], na.rm = TRUE) - 
	##             mean(a[neg], na.rm = TRUE))
	
	##     		(mean(P) -x)  / (mean(P) - mean(N)
	## P1R1
	## A01 P 1  (1-1)/ (1-3) = 0.0   
	## A02 X 2  (1-2)/ (1-3) = 0.5
	## B01 N 3 	(1-3)/ (1-3) = 1.0
	## B02 X 4  (1-4)/ (1-3) = 1.5 
	
	## P2R1
	## A01 P 5  (5-5)/ (5-7) = 0.0   
	## A02 X 6  (5-6)/ (5-7) = 0.5
	## B01 N 7 	(5-7)/ (5-7) = 1.0
	## B02 X 9  (5-9)/ (5-7) = 2.0  

	##P1R2 
	## A01 P 9  (9-9)/ (9-11)= 0.0
	## A02 X 10 (9-10)/ (9-11)= 0.5
	## B01 N 11 (9-11)/ (9-11)= 1
	## B02 X 14 (9-14)/ (9-11)= 2.5
	
	##P2R2 
	## A01 P 13 (13-13)/ (13-15)= 0.0
	## A02 X 14 (13-14)/ (13-15)= 0.5
	## B01 N 15 (13-15)/ (13-15)= 1.0
	## B02 X 19 (13-19)/ (13-15)= 3.0
	
	#values replicate 1 (plate 1 + 2) 
	dataNormTarget[,1,1] <- c(0.0, 0.5, 1.0,1.5,0.0, 0.5, 1.0, 2.0)
	
	#values replicate 2 (plate 1 + 2) 	
	dataNormTarget[,2,1] <- c(0.0, 0.5, 1, 2.5, 0.0, 0.5, 1.0, 3.0)
	return(dataNormTarget)
	
}


makeScoresReplicatesultipleChannelsTarget <- function (){
	nrWells = 6
	nrPlates = 2
	nrReps = 2
	nrChannels = 2
	
	dimNames <- list(Features=c(1:12),Sample=c(1:2),Channels=c("ch1","ch2"))
	dataScoresTarget <- array(as.numeric(NA), dim=c(nrWells * nrPlates,nrReps,nrChannels), dimnames=dimNames)
	
	dataScoresTarget[,1,1] <- c(-2.2483025,-1.3489815,-0.4496605,0.4496605,-1.3489815,NA,-0.8993210,-0.4496605,0.0000000,0.8993210,-1.3489815,NA)
	dataScoresTarget[,1,2] <- c(-7.4193984,-2.5630649,-4.9912316,-2.5630649,2.2932686,-0.1348982,-2.0234723,0.1348982,-0.9442871,0.1348982,2.2932686,1.2140834)
	dataScoresTarget[,2,1] <- c(-1.1691173,-0.8093889,-0.4496605,0.6295247,-2.2483025,NA,-0.8993210,-0.6295247,-0.3597284,0.7194568,-2.2483025,NA)
	dataScoresTarget[,2,2] <- c(-8.0938891,-2.6979630,-5.3959261,-2.6979630,2.6979630,0.0000000,-2.6979630,0.0000000,-1.3489815,0.0000000,2.6979630,1.3489815)
		
	return(dataScoresTarget)
	
}