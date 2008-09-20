##----------------------------------------
## Parameters
## 	object: cellHTS class object
##  conf : Same content als de config file (without the first two lines) but now in a 'data.frame':   .. obs. of  3 variables: f.e.
#		$ Plate  : chr  "*" "*" "*" "*" ...
#		$ Well   : chr  "*" "A01" "A02" "A03" ...
#		$ Content: chr  "sample" "mock" "mock" "mock" ...
#		Although data from the database would not have wild cards.
#	descr :  
#	log	:
##----------------------------------------

# create own generic function
# IN case of overriding, kan only add arguments to the generic only if '...' is an argument.
setGeneric("configureDb", def=function(object, conf,slog = "missing") standardGeneric("configureDb")) # ... allows adding other parameters ,when overriding method

setMethod("configureDb", signature("cellHTS"),
		function(object, conf,slog = "missing") { #slog default NULL 
			#print(paste("configureDb start",proc.time()[3]))
			
## If 'path' is given, we assume that all the files are in this directory.
#			if (!missing(path)) 
#				if(!(is.character(path)&&length(path)==1))
#					stop("'path' must be character of length 1")
#			
## get dimensions:
			pdimResult =pdim(object)		
			nrWpP   = prod(pdimResult)
			nrPlate = max(plate(object))
			nrSample <- ncol(object)
#			chNames <- channelNames(object)
#			nrChannel <- length(chNames)
#			
			xraw <- Data(object)
#			
#			tt = readLines(file.path(ppath, confFile), n=2)
#			hinfo = list(Wells = grep("^Wells:", tt),
#					Plates  =   grep("^Plates:", tt))
#			
#			if(any(listLen(hinfo)==0))
#				stop(sprintf("Could not find all expected header rows ('Wells' and 'Plates') in plate configuration file '%s'.\n", confFile))
#			
#			tt <- sapply(tt, strsplit, split=":")
#			tt <- as.integer(sapply(tt, "[", 2L))
#			
#			if(tt[hinfo$"Plates"] != nrPlate) stop(sprintf("in plate screen log file '%s', the number of plates \n specified in the row header 'Plates:' should be %d instead of %d!", confFile, nrPlate, tt[hinfo$"Plates"]))
#			
#			if(tt[hinfo$"Wells"] != nrWpP) stop(sprintf("in plate screen log file '%s', the number of wells per plate \n specified in the row header 'Wells:' should be %d instead of %d!", confFile, nrWpP, tt[hinfo$"Wells"]))
#			
#			
			## Check if the screen log file is empty
			if (!missing(slog) && !is.null(slog) ){
				if (nrow(slog) > 0 ) {
					## TODO check if the checks below are obsolete, given the data from the screensaver database
					## .. or maybe keep as part of a more generic solution
					
					# check consistency of columns 'Plate', 'Channel' and 'Sample'
					for(i in c("Sample", "Channel")) {
						# in case no column Sample and/or Channel than add one and set the values to 1
						if(!(i %in% names(slog))) 
							slog[[i]] <- rep(1L, nrow(slog)) 
						else 
						if(!all(slog[[i]] %in% 1:get(paste("nr", i, sep="")))) stop(sprintf("Column '%s' of the screen log file 'contains invalid entries.", i))
					}
					
					# cellHTS2 prefix is necessary because checkColumns is not a exported function (via the NAMESPACE file)
					cellHTS2:::checkColumns(slog, NULL, mandatory=c("Plate", "Well", "Flag", "Sample", "Channel"), numeric=c("Plate", "Sample", "Channel"))
					
					invalidPlateID <- !(slog$Plate %in% 1:nrPlate)
					
					if(sum(invalidPlateID)) stop(sprintf("Column 'Plate' of the screen log file contains invalid entries."))
					
					object@screenLog = slog
					
				}
			}
#			
#			
#			## Process the description file
#			ppath = ifelse(missing(path), dirname(descripFile), path)
#			descripFile = basename(descripFile)
#			descript = readLines(file.path(ppath, descripFile))
#			
#			
#			## Store the contents of the description file in the 'experimentData' slot which is accessed via description(object):
#			miameList = list(sepLab=grep("Lab description", descript),
#					name = grep("^Experimenter name:", descript),
#					lab  =   grep("^Laboratory:", descript),
#					contact = grep("^Contact information:", descript),
#					title=grep( "^Title:", descript),
#					pubMedIds=grep( "^PMIDs:",descript),
#					url=grep("^URL:",descript),
#					abstract=grep("^Abstract:",descript)
#					)
#			
#			miameInfo = lapply(miameList, function(i) unlist(strsplit(descript[i], split=": "))[2L]) 
#			miameInfo = lapply(miameInfo, function(i) if(is.null(i)) "" else { if(is.na(i)) "" else i })
#			miameInfo = with(miameInfo, new("MIAME", 
#							name=name,
#							lab = lab,
#							contact=contact,
#							title=title,
#							pubMedIds=pubMedIds,
#							url=url,
#							abstract=abstract))
#			
#			#store the rest of the description information into slot "other":
#			otherInfo <- descript[-unlist(miameList)]
#			#otherInfo <- otherInfo[nchar(otherInfo)>0] #remove empty lines
#			#notes(miameInfo) <- unlist(lapply(otherInfo, function(i) append(append("\t",i), "\n")))
#			notes(miameInfo) <- lapply(otherInfo, function(i) paste("\t", i, "\n", collapse="")) 
#			
#			checkColumns(conf, confFile, mandatory=c("Plate", "Well", "Content"),
#					numeric=integer(0))  #column 'Plate' is no longer numeric
#			
			pWells <- well(object)[1:nrWpP] 
#			
#			## Process the configuration file into wellAnno slot
#			## and set all 'empty' wells to NA in object
			## all plates are below each other
			pcontent = tolower(conf$Content)  ## ignore case!
			wAnno = factor(rep(NA, nrWpP*nrPlate), levels=unique(pcontent))
#			
#			
#			# In the current plate configuration file, the concept of 'batch' is separated from the plate configuration issue.
			# Lieftink: otherwise invalid regular expression '*'. It needs the space for some obscure reason. 
			# TODO remove processing of * as this was only in the RUnit. For data from screensaver database there is no use of * .
			conf[conf=="*"] <- " *" 
#			
			for (i in 1:nrow(conf)) {
				iconf <- conf[i,]
				
				# get plate IDs
				# Lieftink: 1. processing potential regular expressions like plate: * 
				wp <- if(is.numeric(iconf$Plate)) iconf$Plate  else  c(1:nrPlate)[regexpr(iconf$Plate, 1:nrPlate)>0]
				
				# get well IDs.
				# Lieftink: 1. processing potential regular expressions like well: * 
				# Lieftink: 2. Convert the alphanumeric representation as "A01" into an integer, ic. 1
				ww <- convertWellCoordinates(pWells[regexpr(iconf$Well, pWells)>0], pdimResult)$num
				
				#Lieftink: !0 produces a true, so probably meant here is if ()length(wp)==0)
				if(!length(wp)) stop(sprintf("In the plate configuration file '%s', no plate matches were found for rule specified by line %d:\n\t %s \n\t %s", conf, i, paste(names(conf), collapse="\t"), paste(iconf, collapse="\t")))
				if(!length(ww)) stop(sprintf("In the plate configuration file '%s', no well matches were found for rule specified by line %d:\n\t %s \n\t %s", conf, i, paste(names(conf), collapse="\t"), paste(iconf, collapse="\t")))
				
				#Fill the wAnno variable with the specified controlStatus
				# ww: well numbers (the same for each plate), fe. ww: 1 2 3 4
				# wp: plate numbers: fe.  wp: 1 2 ?! TODO Check what happens if plate numbers are fe. 50006??
				# nrWpP: product of number of wells and number of plates
				# nrWpP*(wp-1): produces a factor to determine the position of a well on consecutive plates, as the 
				# plates are now below each other. , fe. 0 4 
				# rep(nrWpP*(wp-1), each=length(ww)):  0 0 0 0 4 4 4 4
				wAnno[ww + rep(nrWpP*(wp-1), each=length(ww))] = pcontent[i] 
			}
#			
#			## Each well and plate should be covered at leat once.
#			## Allow duplication and consider the latter occurence.
#			missAnno <- is.na(wAnno)
#			if(sum(missAnno)) {
#				ind <- which(missAnno)[1:min(5, sum(missAnno))]
#				msg = paste("The following plates and wells were not covered in the plate configuration file\n",
#						"'", confFile, "':\n",
#						"\tPlate Well\n", "\t",
#						paste((ind-1) %/% nrWpP + 1,  1+(ind-1)%%nrWpP, sep="\t", collapse="\n\t"),
#						if(sum(missAnno)>5) sprintf("\n\t...and %d more.\n", sum(missAnno)-5), "\n", sep="")
#				stop(msg)
#			}
#			
#			
#			
			#TODO check in screensaver all wells are represented, also the empty ones. And that the empty ones in xraw have the value 'NA'
			#   In that case the following lines can probably removed 
#			#get empty positions from the final well anno and flag them in each replicate and channel
#			empty = which(wAnno=="empty")
#			xraw[] = apply(xraw, 2:3, replace, list=empty, NA) 
#			
#			
#			## store the conf data.frame into the 'plateConf' slot of 'object' and
#			## slog into the 'slog' slot
#			## descript into the screenDesc slot

			object@plateConf = conf
			
			#object@screenDesc = descript
#			
#			## Process the configuration file into 'controlStatus' column of featureData slot
#			## and set all 'empty' wells to NA in assayData slot
#			
			## Process screenlog
			if (!missing(slog) && !is.null(slog) ){
				ipl  = slog$Plate
				irep = slog$Sample
				ich  = slog$Channel
				ipos = convertWellCoordinates(slog$Well, pdim(object))$num
				stopifnot(!any(is.na(ipl)), !any(is.na(irep)), !any(is.na(ich)))
				
				xraw[cbind(ipos + nrWpP*(ipl-1), irep, ich)] = NA 
			} 
			
			## update object (measurements and well anno) 
			Data(object) <- xraw
			
			## update well anno information: 

			fData(object)$controlStatus=wAnno
			stopifnot(all(fData(object)$controlStatus!="unknown"))
#			
#			## add the 'miame'-like description:
#			description(object) <- miameInfo
			object@state[["configured"]] <- TRUE
			validObject(object)
			
			return(object)
		} )