#!/bin/bash

# This script will recreate the LINCS database

DIR=/groups/pharmacoresponse/
DATA_DIRECTORY=${DIR}/data/current

if [[ $# -lt 1 ]]
then
  echo "Usage: $0 [local || dev || stage || prod ] "
  exit $WRONG_ARGS
fi

SERVER=$1

if [[ "$SERVER" == "PROD" ]] || [[ "$SERVER" == "prod" ]] 
then
  SERVER=""
  DB=${SERVER}pharmacoresponse
  DB_USER=${SERVER}pharmacoresponseweb
  export SCREENSAVER_PROPERTIES_FILE=/groups/pharmacoresponse/screensaver/cfg/pharmacoresponse.properties.util.prod
elif [[ "$SERVER" == "STAGE" ]] || [[ "$SERVER" == "stage" ]] 
then
  SERVER="stage"
  DB=${SERVER}pharmacoresponse
  DB_USER=${SERVER}pharmacoresponseweb
  export SCREENSAVER_PROPERTIES_FILE=/groups/pharmacoresponse/screensaver/cfg/pharmacoresponse.properties.util.stage
elif [[ "$SERVER" == "DEV" ]] || [[ "$SERVER" == "dev" ]] 
then
  SERVER="dev"
  DB=${SERVER}pharmacoresponse
  DB_USER=${SERVER}pharmacoresponseweb
  PGHOST=dev.pgsql.orchestra
  export SCREENSAVER_PROPERTIES_FILE=/groups/pharmacoresponse/screensaver/cfg/pharmacoresponse.properties.util.dev
elif [[ "$SERVER" == "LOCAL" ]] || [[ "$SERVER" == "local" ]] 
then
  DIR=.
  DATA_DIRECTORY=/home/sde4/sean/docs/work/LINCS/data/current
  DB=screensavergo
  DB_USER=screensavergo
  export SCREENSAVER_PROPERTIES_FILE=/home/sde4/workspace/current/screensaver.properties.LINCS
else
  echo "Unknown option: \"$SERVER\""
  exit 1
fi

# TODO: parameterize
ECOMMONS_ADMIN=djw11
#ECOMMONS_ADMIN=sde_admin

check_errs()
{
  # Function. Parameter 1 is the return code
  # Para. 2 is text to display on failure.
  if [ "${1}" -ne "0" ]; then
    echo "ERROR: ${1} : ${2}"
    exit ${1}
  fi
}

#set -x 

psql -q -U $DB_USER $DB -f scripts/drop_all.sql -v ON_ERROR_STOP=1
check_errs $? "drop_all.sh fails"

psql -q -U $DB_USER $DB -f scripts/create_lincs_schema.sql -v ON_ERROR_STOP=1
check_errs $? "create schema fails"

psql -q -U $DB_USER $DB -f $DATA_DIRECTORY/misc.sql -v ON_ERROR_STOP=1
check_errs $? "misc.sql fails"

psql -q -U $DB_USER $DB -f $DATA_DIRECTORY/lincs-users.sql -v ON_ERROR_STOP=1
check_errs $? "lincs-users.sql fails"

## Create the library
LIBRARY_1_SHORTNAME="LINCS-1"
LIBRARY_2_SHORTNAME="LINCS-2"
LIBRARY_3_SHORTNAME="LINCS-3"
LIBRARY_4_SHORTNAME="LINCS-4"
LIBRARY_5_SHORTNAME="LINCS-5"

set -x 
./run.sh edu.harvard.med.screensaver.io.libraries.LibraryCreator \
-n "HMS LINCS BATCH 001" -s $LIBRARY_1_SHORTNAME \
-lt "Commercial" -st SMALL_MOLECULE -sp 1 -ep 1 -AE $ECOMMONS_ADMIN

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryContentsLoader \
--release-library-contents-version \
-l $LIBRARY_1_SHORTNAME \
-f $DATA_DIRECTORY/HMS_LINCS-1.sdf -AE $ECOMMONS_ADMIN

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryCreator \
-n "HMS LINCS Anti-Mitotics" -s $LIBRARY_2_SHORTNAME \
-lt "Commercial" -st SMALL_MOLECULE -sp 2 -ep 2 -AE $ECOMMONS_ADMIN

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryContentsLoader \
--release-library-contents-version \
-l $LIBRARY_2_SHORTNAME \
-f $DATA_DIRECTORY/HMS_LINCS-2.sdf -AE $ECOMMONS_ADMIN

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryCreator \
-n "HMS LINCS Batch 001 Stock Plates" -s $LIBRARY_3_SHORTNAME -ps WELLS_96 \
-lt "Commercial" -st SMALL_MOLECULE -sp 3 -ep 4 -AE $ECOMMONS_ADMIN 

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryContentsLoader \
--release-library-contents-version \
-l $LIBRARY_3_SHORTNAME \
-f $DATA_DIRECTORY/HMS_LINCS-3.sdf -AE $ECOMMONS_ADMIN

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryCreator \
-n "HMS LINCS Plated 5 Anti-Mitotics" -s $LIBRARY_4_SHORTNAME \
-lt "Commercial" -st SMALL_MOLECULE -sp 5 -ep 5 -AE $ECOMMONS_ADMIN

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryContentsLoader \
--release-library-contents-version \
-l $LIBRARY_4_SHORTNAME \
-f $DATA_DIRECTORY/HMS_LINCS-4.sdf -AE $ECOMMONS_ADMIN

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryCreator \
-n "HMS LINCS Plated 6 Anti-Mitotics" -s $LIBRARY_5_SHORTNAME \
-lt "Commercial" -st SMALL_MOLECULE -sp 6 -ep 6 -AE $ECOMMONS_ADMIN

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryContentsLoader \
--release-library-contents-version \
-l $LIBRARY_5_SHORTNAME \
-f $DATA_DIRECTORY/HMS_LINCS-5.sdf -AE $ECOMMONS_ADMIN

## Create the screens

LEAD_SCREENER_FIRST="Nathan"
LEAD_SCREENER_LAST="Moerke"
LEAD_SCREENER_EMAIL="nathanmoerke@gmail.com"
LAB_HEAD_FIRST="Nathanael"
LAB_HEAD_LAST="Gray"
LAB_HEAD_EMAIL="nathanael_gray@dfci.harvard.edu"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Moerke 2 Color Apoptosis: IA-LM cells.'  \
-i 10001 \
--summary "Moerke 2 Color Apoptosis: IA-LM cells. Dose response of anti-mitotic compounds in human cancer cell line IA-LM at 24, 48 and 72 hours to determine effect on apoptosis and cell death." \
-p 'Day 1: Seed cells in 384-well assay plates, with 3 plates per cell line (for 24, 48, 72 hr time points).  Add 30 uL cell suspension per well
Day 2: Add compounds to plates by pin transfer.
Day 2: Prepare the 4X apoptosis reagent mixture fresh in PBS
4X apoptosis reagent mixture composition:
(1) DEVD-NucView488 caspase substrate: 1 uM
(2) Hoechst 33342:  2 ug/mL
For 4 384-well plates, 25 mL of this mixture will be sufficient and allow plenty of dead volume.
For 25 mL of mixture add 25 uL of DEVD-NucView488 substrate (in small refrigerator) and 50 uL of Hoechst 33342 (in freezer).
Use WellMate (hood or bench is fine) to add the mixture, using the designated manifold (labeled �NucView�).  Add 10 uL per well, skipping the 1st 2 and last 2 columns.  Spin plates briefly at 1000 rpm in plate centrifuge and put in incubator for 90 minutes.
Prepare 2X fixative solution: 2 % formaldehyde in PBS.  Dilute a 36-36.5% formaldehyde stock bottle 1:20 in PBS.  100 mL fixative total is sufficient for 4 plates; add 5 mL formaldehyde stock to 95 mL PBS.
After 90 minutes, use benchtop (not hood) WellMate to add fixative to plates.  Use the manifold labeled �Fixative�  Add 40 uL per well (again skipping 1st 2 and last 2 columns).  Spin plates briefly as before.  Let fix 20-30 minutes at RT, then seal with metal foil, and image right away or store in the cold room until you are ready to image.

Reagent stocks:
Hoechst 33342 (Invitrogen ) � 1 mg/mL stock in H2O, store at -20 degrees C
DEVD-NucView488 (Biotium) �  1 mM in DMSO, store at 4 degrees C protected from light' \


./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/moerke_IA-LM_24hr.xls \
-s 10000 -i 

## Create the studies

LAB_HEAD_FIRST="Nathanael"
LAB_HEAD_LAST="Gray"
LAB_HEAD_EMAIL="nathanael_gray@dfci.harvard.edu"
#LAB_HEAD_FIRST="Qingsong"
#LAB_HEAD_LAST="Liu"
#LAB_HEAD_EMAIL="nkliuqs97@gmail.com"
LEAD_SCREENER_FIRST="Qingsong"
LEAD_SCREENER_LAST="Liu"
LEAD_SCREENER_EMAIL="qingsong_liu@hms.harvard.edu"

# NOTE: (2011-02-08) qingsong-compound-study-data.xls fails -sde4
./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/LINCS-001-compounds_selection_master_study_fields.xls \
-t 'LINCS Compound Targets and Concentrations'  \
-i 300001 \
--summary "Provides uncurated data for compounds' kinase targets, screening concentrations, and relevant publication citations"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10008_sorafenib_ambit.xls \
-t 'Sorafenib Ambit'  \
-i 300002 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`" 

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10017_HG-6-64-1_ambit.xls \
-t 'HG-6-64-1 Ambit'  \
-i 300003 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10029_GW-5074_ambit.xls \
-t 'GW-5074 Ambit'  \
-i 300004 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10046_SB590885_ambit.xls \
-t 'SB590885 Ambit'  \
-i 300005 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10049_PLX-4720_ambit.xls \
-t 'PLX-4720 Ambit'  \
-i 300006 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10050_AZ-628_ambit.xls \
-t 'AZ-628 Ambit'  \
-i 300007 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10068_PLX-4032_ambit.xls \
-t 'PLX-4032 Ambit'  \
-i 300008 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10006_AZD7762_ambit.xls \
-t 'AZD7762 Ambit'  \
-i 300009 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10009_CP466722_ambit.xls \
-t 'CP466722 Ambit'  \
-i 300010 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10010_CP724714_ambit.xls \
-t 'CP724714 Ambit'  \
-i 300011 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10012_GSK429286A_ambit.xls \
-t 'GSK429286A Ambit'  \
-i 300012 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10013_GSK461364_ambit.xls \
-t 'GSK461364 Ambit'  \
-i 300013 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10014_GW843682_ambit.xls \
-t 'GW843682 Ambit'  \
-i 300014 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10027_PF02341066_ambit.xls \
-t 'PF02341066 Ambit'  \
-i 300015 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10028_BMS345541_ambit.xls \
-t 'BMS345541 Ambit'  \
-i 300016 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10034_AS601245_ambit.xls \
-t 'AS601245 Ambit'  \
-i 300017 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10038_WH-4-023_ambit.xls \
-t 'WH-4-023 Ambit'  \
-i 300018 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10055_BX-912_ambit.xls \
-t 'BX-912 Ambit'  \
-i 300019 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10059_AZD-6482_ambit.xls \
-t 'AZD-6482 Ambit'  \
-i 300020 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10060_TAK-715_ambit.xls \
-t 'TAK-715 Ambit'  \
-i 300021 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10061_NU7441_ambit.xls \
-t 'NU7441 Ambit'  \
-i 300022 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10065_KIN001-220_ambit.xls \
-t 'KIN001-220 Ambit'  \
-i 300023 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10066_MLN8054_ambit.xls \
-t 'MLN8054 Ambit'  \
-i 300024 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10067_AZD1152-HQPA_ambit.xls \
-t 'AZD1152-HQPA Ambit'  \
-i 300025 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10071_PD0332991_ambit.xls \
-t 'PD0332991 Ambit'  \
-i 300026 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10078_THZ-2-98-01_ambit.xls \
-t 'THZ-2-98-01 Ambit'  \
-i 300027 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10092_JWE-035_ambit.xls \
-t 'JWE-035 Ambit'  \
-i 300028 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10096_ZM-447439_ambit.xls \
-t 'ZM-447439 Ambit'  \
-i 300029 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10100_JNK-9L_ambit.xls \
-t 'JNK-9L Ambit'  \
-i 300030 \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"

## Reagent QC Attachments
./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10001.101.01.pdf -i HMSL10001 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10001.101.01.pdf -i HMSL10001 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10004.101.01.pdf -i HMSL10004 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10004.101.01.pdf -i HMSL10004 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10005.101.01.pdf -i HMSL10005 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10005.101.01.pdf -i HMSL10005 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10006.101.01.pdf -i HMSL10006 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10006.101.01.pdf -i HMSL10006 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10010.101.01.pdf -i HMSL10010 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10010.101.01.pdf -i HMSL10010 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10011.101.01.pdf -i HMSL10011 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10011.101.01.pdf -i HMSL10011 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10012.101.01.pdf -i HMSL10012 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10012.101.01.pdf -i HMSL10012 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10013.101.01.pdf -i HMSL10013 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10013.101.01.pdf -i HMSL10013 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10014.101.01.pdf -i HMSL10014 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10014.101.01.pdf -i HMSL10014 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10018.101.01.pdf -i HMSL10018 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10018.101.01.pdf -i HMSL10018 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10020.101.01.pdf -i HMSL10020 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10020.101.01.pdf -i HMSL10020 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10021.101.01.pdf -i HMSL10021 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10021.101.01.pdf -i HMSL10021 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10023.103.01.pdf -i HMSL10023 -sid 103 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10023.103.01.pdf -i HMSL10023 -sid 103 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10029.101.01.pdf -i HMSL10029 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10029.101.01.pdf -i HMSL10029 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10032.101.01.pdf -i HMSL10032 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10032.101.01.pdf -i HMSL10032 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10035.101.01.pdf -i HMSL10035 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10035.101.01.pdf -i HMSL10035 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10036.101.01.pdf -i HMSL10036 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10036.101.01.pdf -i HMSL10036 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10041.101.01.pdf -i HMSL10041 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10041.101.01.pdf -i HMSL10041 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10042.101.01.pdf -i HMSL10042 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10042.101.01.pdf -i HMSL10042 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10046.101.01.pdf -i HMSL10046 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10046.101.01.pdf -i HMSL10046 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10048.101.01.pdf -i HMSL10048 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10048.101.01.pdf -i HMSL10048 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10050.101.01.pdf -i HMSL10050 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10050.101.01.pdf -i HMSL10050 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10051.104.01.pdf -i HMSL10051 -sid 104 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10051.104.01.pdf -i HMSL10051 -sid 104 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10051.104.01.pdf -i HMSL10051 -sid 104 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10052.101.01.pdf -i HMSL10052 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10053.101.01.pdf -i HMSL10053 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10053.101.01.pdf -i HMSL10053 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10053.101.01.pdf -i HMSL10053 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10054.101.01.pdf -i HMSL10054 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10054.101.01.pdf -i HMSL10054 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10054.101.01.pdf -i HMSL10054 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10055.101.01.pdf -i HMSL10055 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10055.101.01.pdf -i HMSL10055 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10056.101.01.pdf -i HMSL10056 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10056.101.01.pdf -i HMSL10056 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10056.101.01.pdf -i HMSL10056 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10057.102.01.pdf -i HMSL10057 -sid 102 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10057.102.01.pdf -i HMSL10057 -sid 102 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10060.101.01.pdf -i HMSL10060 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10060.101.01.pdf -i HMSL10060 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10061.101.01.pdf -i HMSL10061 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10061.101.01.pdf -i HMSL10061 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10062.101.01.pdf -i HMSL10062 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10062.101.01.pdf -i HMSL10062 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10062.101.01.pdf -i HMSL10062 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10069.101.01.pdf -i HMSL10069 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10069.101.01.pdf -i HMSL10069 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10071.101.01.pdf -i HMSL10071 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10071.101.01.pdf -i HMSL10071 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10072.101.01.pdf -i HMSL10072 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10072.101.01.pdf -i HMSL10072 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10073.101.01.pdf -i HMSL10073 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10073.101.01.pdf -i HMSL10073 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10096.101.01.pdf -i HMSL10096 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10096.101.01.pdf -i HMSL10096 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10096.101.01.pdf -i HMSL10096 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10097.101.01.pdf -i HMSL10097 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10097.101.01.pdf -i HMSL10097 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10098.101.01.pdf -i HMSL10098 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10098.101.01.pdf -i HMSL10098 -sid 101 -bid 1 -type QC-HPLC

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10101.101.01.pdf -i HMSL10101 -sid 101 -bid 1 -type QC-NMR

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10101.101.01.pdf -i HMSL10101 -sid 101 -bid 1 -type QC-LCMS

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentQCAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10101.101.01.pdf -i HMSL10101 -sid 101 -bid 1 -type QC-HPLC
