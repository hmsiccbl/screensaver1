##----------------------------------------
## annotateDb
##
## parameters
##    object: cellHTS class object
##  geneIDs: a 
##'data.frame':   .. obs. of  4 variables:
##$ Plate : int  1 1 1 ...
##$ Well  : chr  "A01" "A02" "A03" ... (one character and two digits)
##$ HFAID : chr  NA "NM_018243"   NM_001777"   
##$ GeneID: chr  "MOCK" "SEPT11" "CD47"
##----------------------------------------

# create own generic function
# IN case of overriding, kan only add arguments to the generic only if '...' is an argument.
setGeneric("annotateDb", def=function(object, geneIDs) standardGeneric("annotateDb")) # ... allows adding other parameters ,when overriding method

setMethod("annotateDb", signature("cellHTS"),
function(object, geneIDs) {

#   checkColumns(geneIDs, file, mandatory=c("Plate", "Well", "GeneID"),
#         numeric=c("Plate"))
#   
   ## sort the data by Plate and then by well
   ## ordering by well might be problematic when we have "A2" instead of "A02"...
   ## so all well IDs are given as alphanumeric characters with 3 characters.
   
   geneIDs = geneIDs[order(geneIDs$Plate, geneIDs$Well),]
   
   ## Some checkings for dimension of "Plate" and "Well"
   ## expect prod(x@pdim) * x@nrPlate rows
   nrWpP   = prod(pdim(object))
   nrPlate = max(plate(object))
   
   if (!((nrow(geneIDs)==nrWpP*nrPlate) && all(convertWellCoordinates(geneIDs$Well, pdim(object))$num==rep(1:nrWpP, times=nrPlate)) &&
            all(geneIDs$Plate == rep(1:nrPlate, each=nrWpP))))
      stop(paste("Invalid input '", "': expecting ", nrWpP*nrPlate,
                  " rows, one for each well and for each plate. Please see the vignette for",
                  " an example.\n", sep=""))
   
   
   ## store the geneIDs data.frame into the featureData slot
   ## 'annotation(object)' returns a character vector indicating the annotation package

   geneIDs <- geneIDs[, !c(names(geneIDs) %in% c("Plate", "Well")), drop=FALSE]

   ## flag 'NA' values in the "GeneID" column:
   geneIDs[apply(geneIDs, 2, function(i) i %in% "NA")] <- NA 
   
   ## add the columns into featureData:
   fData(object)[names(geneIDs)] <- geneIDs
   fvarMetadata(object)[names(geneIDs),]=names(geneIDs)
   
   object@state[["annotated"]] = TRUE
   validObject(object)
   return(object)
})         