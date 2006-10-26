REM usage: parse-legacy-screen-result.bat <input file> <screen number> [<wells to print #>]
IF "%3"=="" (SET WELLSTOPRINT=10 ) ELSE (SET WELLSTOPRINT=%3)
./run.bat edu.harvard.med.screensaver.io.screenresults.ScreenResultParser --input-file %1 --screen %2 --wells %WELLSTOPRINT% %3 --ignore-file-paths --legacy-format
