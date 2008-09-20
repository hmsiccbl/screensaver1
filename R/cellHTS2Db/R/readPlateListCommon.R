## Code that could be shared with the filesystem base cellHTS2 readPlateList method, is put in this seperate method 

readPlateListCommon <- function(xraw,name,dimPlate,pd,status,intensityFiles) {
	
	xrawd <- dim(xraw)
	nrWell =   xrawd[1]
	nrPlate =  xrawd[2]
	nrRep =    xrawd[3]
	nrChannel = xrawd[4]
	
	## ----  Store the data as a "cellHTS" object ----
	## arrange the assayData slot:
	
	adata <- assayDataNew(storage.mode="environment")
	chNames <- paste("ch", 1:nrChannel, sep="")
	
	for(ch in 1:nrChannel) 
		assign(chNames[ch], matrix(xraw[,,,ch, drop=TRUE], ncol=nrRep, nrow=nrWell*nrPlate), env=adata)
	
	storageMode(adata) <- "lockedEnvironment"
	
	## arrange the phenoData slot:
	pdata = new("cellHTS")@phenoData
	pData(pdata) <- data.frame(replicate=1:nrRep, assay=I(rep(name, nrRep)))
	varMetadata(pdata)[["channel"]] = factor(rep("_ALL_",2), levels=c(chNames, "_ALL_"))
	
	# pdata <- new("AnnotatedDataFrame", 
	#                   data=data.frame(replicate=1:nrRep, assay=I(rep(name, nrRep))),
	#                   varMetadata=data.frame(labelDescription=I(c("Replicate number", "Biological assay")), 
	#                                          channel=factor(rep("_ALL_",2), levels=c(chNames, "_ALL_"))))
	
	
	
	## arrange the featureData slot:
	well <- convertWellCoordinates(as.integer(1:nrWell), pdim=dimPlate)$letnum
	fdata <- new("cellHTS")@featureData
	pData(fdata) <- data.frame(plate=rep(1:nrPlate, each=nrWell), well=I(rep(well,nrPlate)), 
			controlStatus=factor(rep("unknown", nrWell*nrPlate)))
	
	# 
	# fdata <- new("AnnotatedDataFrame", 
	#            data=data.frame(plate=rep(1:nrPlate, each=nrWell), well=I(rep(well,nrPlate)), 
	#                            controlStatus=factor(rep("unknown", nrWell*nrPlate))), 
	#            varMetadata=data.frame(labelDescription=I(c("Plate number", "Well ID", "Well annotation"))))
	
	
	
	res = new("cellHTS", 
			assayData=adata,
			phenoData=pdata,
			featureData=fdata,
			plateList=cbind(pd[,1,drop=FALSE], status=I(status), pd[,-1,drop=FALSE]),
			intensityFiles=intensityFiles
			#state=c("configured"=FALSE, "normalized"=FALSE, "scored"=FALSE, "annotated" = FALSE)
			)

}