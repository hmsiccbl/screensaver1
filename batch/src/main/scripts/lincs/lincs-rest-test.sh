#!/bin/sh

HOST=${1:-https://stage.pharmacoresponse.hms.harvard.edu}


curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/libraries \
  | tidy -xml -i 2> /dev/null  > libraries.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/libraries/R-LINCS-1 \
  | tidy -xml -i 2> /dev/null  > libraries.R-LINCS-1.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/plates/1/wells/A01 \
  | tidy -xml -i 2> /dev/null  > plates.1.wells.A01.xml  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/plates/5/wells/C03 \
  | tidy -xml -i 2> /dev/null  > plates.5.wells.C03.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/reagents \
  | tidy -xml -i 2> /dev/null  > reagents.xml 
   
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/reagents/HMSL10001 \
  | tidy -xml -i 2> /dev/null  > reagents.HMSL10001.xml

curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/screens \
  | tidy -xml -i 2> /dev/null  > screens.xml

curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/screens/10001 \
  | tidy -xml -i 2> /dev/null  > screens.10001.xml

curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/screens/10001/results \
  | tidy -xml -i 2> /dev/null  > screens.10001.results.xml

curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/screens/10001/results/columns/0 \
  | tidy -xml -i 2> /dev/null  > screens.10001.results.columns.0.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/screens/10001/results/columns/0/values \
  | tidy -xml -i 2> /dev/null  > screens.10001.results.columns.0.values.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/screens/10001/results/assaywells \
  | tidy -xml -i 2> /dev/null  > screens.10001.results.assaywells.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/screens/10001/results/assaywells/5/J06 \
  | tidy -xml -i 2> /dev/null  > screens.10001.results.assaywells.5.J06.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/screens/10001/results/assaywells/5/J06/values \
  | tidy -xml -i 2> /dev/null  > screens.10001.results.assaywells.5.J06.values.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/studies \
  | tidy -xml -i 2> /dev/null  > studies.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/studies/300001 \
  | tidy -xml -i 2> /dev/null  > studies.300001.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/studies/300001/results \
  | tidy -xml -i 2> /dev/null  > studies.300001.results.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/studies/300001/results/columns \
  | tidy -xml -i 2> /dev/null  > studies.300001.results.columns.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/studies/300001/results/columns/0 \
  | tidy -xml -i 2> /dev/null  > studies.300001.results.columns.0.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/studies/300001/results/columns/0/values \
  | tidy -xml -i 2> /dev/null  > studies.300001.results.columns.0.values.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/studies/300001/results/reagents \
  | tidy -xml -i 2> /dev/null  > studies.300001.results.reagents.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/studies/300001/results/reagents/HMSL10008/values \
  | tidy -xml -i 2> /dev/null  > studies.300001.results.reagents.HMSL10008.values.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/persons/6  \
  | tidy -xml -i 2> /dev/null  > persons.6.xml
  
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/publications/1  \
  | tidy -xml -i 2> /dev/null  > publications.1.xml
  
  /** note, only "STUDY-FILE" attached file types can be downloaded at this time
curl --insecure -H 'accept:application/xml'  ${HOST}/screensaver/data/attachedfiles/775  > attachedfile.775
  