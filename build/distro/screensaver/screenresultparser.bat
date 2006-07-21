REM usage: screenresultparser.bat <metadata file> [<wells to print #>] [--ignorefilepaths]
IF "%2"=="" (SET WELLSTOPRINT=10 ) ELSE (SET WELLSTOPRINT=%2)
./run.bat edu.harvard.med.screensaver.io.screenresult.ScreenResultParser --metadatafile %1 --wellstoprint %WELLSTOPRINT% %3