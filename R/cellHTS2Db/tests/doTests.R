setwd("~/ws_scr_nki_trunk/screensaver/R/cellHTS2Db/tests")

#source("doRUnit.R")
#debug <- TRUE
##
library(cellHTS2Db)
 library(RUnit)

###
## ## #######	I RUNNING THE TESTSUITE
testSuite <- defineTestSuite(name="CellHTS2 integration",dirs="../inst/unitTests/", testFileRegexp="^runit.+[rR]$",
          testFuncRegexp="^test.+")
isValidTestSuite(testSuite)
testResult <- runTestSuite(testSuite, useOwnErrorHandler=TRUE)
 printTextProtocol(testResult)

###II RUNNING INDIVIDUAL TESTS IN NORMAL MODE
#
#testResult <- runTestFile("../inst/unitTests/runitReadPlateListDb.R")
# testResult <- runTestFile("../inst/unitTests/runitReadPlateListCommon.R")
#testResult <- runTestFile("../inst/unitTests/runitConfigureDb.R")
#testResult <- runTestFile("../inst/unitTests/runitAnnotateDb.R")
#testResult <- runTestFile("../inst/unitTests/runitNormalizePlates.R")
#testResult <- runTestFile("../inst/unitTests/runitScoreReplicates.R")
## source("../inst/unitTests/makeDummies.R")
## testResult <- runTestFile("../inst/unitTests/runitWriteReport.R")

#printTextProtocol(testResult)

##III RUNNING TESTS IN DEBUG MODE

#source("../R/readPlateListDb.R")
#source("../inst/unitTests/runitReadPlateListDb.R")
#debug(testCreateIntensityFile)
#testCreateIntensityFile(T)
#
#source("../inst/unitTests/runitReadPlateListDb.R")
#debug(testCreatePlateListAndIntensityFiles)
#testCreatePlateListAndIntensityFiles()
#
#source("../inst/unitTests/runitReadPlateListDb.R")
#debug(testReadPlateListDb1)
#testReadPlateListDb1()

#source("../inst/unitTests/runitReadPlateListCommon.R")
##debug(testReadPlateListCommon)
#testReadPlateListCommon()

#source("../inst/unitTests/runitConfigureDb.R")
#testConfigureDb1(T)

## source("../inst/unitTests/runitConfigureDb.R")
## debug(testConfigureDbMultichannelSlog)
## testConfigureDbMultichannelSlog()

#source("../inst/unitTests/runitConfigureDb.R")


## source("../inst/unitTests/runitAnnotateDb.R")
## testAnnotateDb1()
## #
#source("../inst/unitTests/runitNormalizePlates.R")
#debug(testNormalizeMean)
#testNormalizeMean()
#testNormalizeNegatives(T)

source("../inst/unitTests/runitNormalizePlates.R")
## #debug(testNormalizeNegativesMultiChannels)
## testNormalizeNegMultiChannels()
##testNormNegMultiChannels10()
testNormLoess(debug)


## debug(testNormalizeMeanWithSlog)
## testNormalizeMeanWithSlog(T)

#source("../inst/unitTests/runitScoreReplicates.R")
#testScoreReplicates1(debug)
## 
## source("../inst/unitTests/runitScoreReplicates.R")
## testScoresReplicatesMultiChannels(T)


#source("../inst/unitTests/runitSummarizeReplicates.R")
#debug(summarizeReplicates)
#testSummarizeReplicates1(debug)

## source("../inst/unitTests/runitWriteReport.R")
## testWriteReportInclScored()

## source("../inst/unitTests/runitWriteReport.R")
## testWriteReport1()

## 
## source("../inst/unitTests/runitWriteReport.R")
## testWriteReportMultiChannel()

source("../inst/unitTests/runitWriteReport.R")
#debug(testWriteReportMultiChannelSlog)
testWriteReportMultiChannelSlog()


#source("../inst/unitTests/runitIntegrationTest.R")
#testIntegrationTest1()

#source("../inst/unitTests/runitReadPlateListUsingCommon.R")
#debug(testReadPlateListUsingCommon1)
#testReadPlateListUsingCommon1()







