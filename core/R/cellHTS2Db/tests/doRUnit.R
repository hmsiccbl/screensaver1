## unit tests will not be done if RUnit is not available
if(require("RUnit", quietly=TRUE)) {
	
	## --- Setup ---
	
	pkg <- "cellHTS2Db" 
	if(Sys.getenv("RCMDCHECK") == "FALSE") {
		## Path to unit tests for standalone running under Makefile (not R CMD check)
		
		# PKG/tests/../inst/unitTests
		path <- file.path(getwd(), "..", "inst", "unitTests")
	} else {
		## Path to unit tests for R CMD check
		## PKG.Rcheck/tests/../PKG/unitTests
		path <- system.file(package=pkg, "unitTests")
	}
	cat("\nRunning unit tests\n")
	print(list(pkg=pkg, getwd=getwd(), pathToUnitTests=path))
	
	library(package=pkg, character.only=TRUE)
	
	## If desired, load the name space to allow testing of private functions
	## if (is.element(pkg, loadedNamespaces()))
	##     attach(loadNamespace(pkg), name=paste("namespace", pkg, sep=":"), pos=3)
	##
	## or simply call PKG:::myPrivateFunction() in tests
	
	## --- Testing ---
	
	## Define tests
	##testSuite <- defineTestSuite(name=paste(pkg, "unit testing"), dirs=path)
	##In order to run the tests in the specific order, each test is put in a seperate suite, and combined in an overall suite.
	##.. I was not able to find a equivalent to the JUnit test of adding tests to a existing testsuite, and build up this way a specific order of tests.
	##.. One can run seperate testfiles but you don't have the total summary then.
	##.. One can ignore the warning at the end that  'testSuite' object is not of class 'RUnitTestSuite'. This is a bug as list of RUnitTestSuites 
	##.. are formally allowed and executed.
	dirs <- "../inst/unitTests/"
	## default testFuncRegexp="^test.+" 
	# DEFINE testsuites
	t1 <- defineTestSuite(name="ReadPlateListCommon",dirs=dirs, testFileRegexp="runitReadPlateListCommon.R")
	t2 <- defineTestSuite(name="ReadPlateListDb",dirs=dirs, testFileRegexp="runitReadPlateListDb.R")
	t3 <- defineTestSuite(name="ConfigureDb",dirs=dirs, testFileRegexp="runitConfigureDb.R")
	t4 <- defineTestSuite(name="AnnotateDb",dirs=dirs, testFileRegexp="runitAnnotateDb.R")
	t5 <- defineTestSuite(name="NormalizePlates",dirs=dirs, testFileRegexp="runitNormalizePlates.R")
	t6 <- defineTestSuite(name="ScoreReplicates",dirs=dirs, testFileRegexp="runitScoreReplicates.R")
	t7 <- defineTestSuite(name="SummarizeReplicates",dirs=dirs, testFileRegexp="SummarizeReplicates.R")	
	t8 <- defineTestSuite(name="WriteReport",dirs=dirs, testFileRegexp="runitWriteReport.R")

	## Run
	tests <- runTestSuite(list(t1,t2,t3,t4,t5,t6,t7,t8))
	
	## Default report name
	pathReport <- file.path("/tmp", "report")
	
	## Report to stdout and text files
	cat("------------------- UNIT TEST SUMMARY ---------------------\n\n")
	printTextProtocol(tests, showDetails=FALSE)
	printTextProtocol(tests, showDetails=FALSE,
			fileName=paste(pathReport, "Summary.txt", sep=""))
	printTextProtocol(tests, showDetails=TRUE,
			fileName=paste(pathReport, ".txt", sep=""))
	
	## Report to HTML file
	printHTMLProtocol(tests, fileName=paste(pathReport, ".html", sep=""))
	
	## Return stop() to cause R CMD check stop in case of
	##  - failures i.e. FALSE to unit tests or
	##  - errors i.e. R errors
	tmp <- getErrors(tests)
	if(tmp$nFail > 0 | tmp$nErr > 0) {
		stop(paste("\n\nunit testing failed (#test failures: ", tmp$nFail,
						", #R errors: ",  tmp$nErr, ")\n\n", sep=""))
	}
} else {
	warning("cannot run unit tests -- package RUnit is not available")
}