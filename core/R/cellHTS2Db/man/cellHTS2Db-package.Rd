\name{cellHTS2Db-package}
\alias{cellHTS2Db-package}
\alias{cellHTS2Db}
\docType{package}
\title{
CellHTS2Db package overview.
}
\description{
CellHTS2Db provides input functions for the cellHTS2 package, not based
on files. 
}
\details{
The methods in cellHTS2 that take information from files are readPlateList, 
configure and annotate. Other methods like normalizePlates,
scoreReplicates and summarizeReplicates don't have a dependency on files
and can be used in combination with the cellHTS2Db methods. 

The cellHTSDb methods were developed to be used in combination with 
Rserve and it's Java client. Minimal dataobjects which are the parameters for 
the cellHTS2Db methods are first created afterwards these methods can be called.
}
\author{
Cor Lieftink <c.lieftink@nki.nl>
}

\keyword{ package }

