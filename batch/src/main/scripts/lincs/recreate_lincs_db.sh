#!/bin/bash

# This script will recreate the HMS LINCS database 

DIR=/groups/pharmacoresponse/
DATA_DIRECTORY=${DIR}/data/current

if [[ $# -lt 1 ]]
then 
  echo "Usage: $0 { local <local data dir> <properties file> [<db name> [<db user>]] | dev | stage | prod }"
  exit $WRONG_ARGS
fi

SERVER=$1

if [[ "$SERVER" == "PROD" ]] || [[ "$SERVER" == "prod" ]] 
then
  SERVER=""
  DATA_DIRECTORY=${DIR}/data/prod
  DB=${SERVER}pharmacoresponse
  DB_USER=${SERVER}pharmacoresponseweb
  export SCREENSAVER_PROPERTIES_FILE=/groups/pharmacoresponse/screensaver/cfg/pharmacoresponse.properties.util.prod
elif [[ "$SERVER" == "STAGE" ]] || [[ "$SERVER" == "stage" ]] 
then
  SERVER="stage"
  DATA_DIRECTORY=${DIR}/data/stage
  DB=${SERVER}pharmacoresponse
  DB_USER=${SERVER}pharmacoresponseweb
  export SCREENSAVER_PROPERTIES_FILE=/groups/pharmacoresponse/screensaver/cfg/pharmacoresponse.properties.util.stage
elif [[ "$SERVER" == "DEV" ]] || [[ "$SERVER" == "dev" ]] 
then
  SERVER="dev"
  DATA_DIRECTORY=${DIR}/data/dev
  DB=${SERVER}pharmacoresponse
  DB_USER=${SERVER}pharmacoresponseweb
  PGHOST=dev.pgsql.orchestra
  export SCREENSAVER_PROPERTIES_FILE=/groups/pharmacoresponse/screensaver/cfg/pharmacoresponse.properties.util.dev
elif [[ "$SERVER" == "LOCAL" ]] || [[ "$SERVER" == "local" ]] 
then
  DIR=.
  DATA_DIRECTORY=${2:-/home/sde4/sean/docs/work/LINCS/data/current}
  export SCREENSAVER_PROPERTIES_FILE=${3:-/home/sde4/workspace/current/screensaver.properties.LINCS}
  DB=${4:-screensavergo}
  DB_USER=${5:-screensavergo}
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

echo DIR=$DIR
echo DATA_DIRECTORY=$DATA_DIRECTORY 
echo DB=$DB
echo DB_USER=$DB_USER
echo SCREENSAVER_PROPERTIES_FILE=$SCREENSAVER_PROPERTIES_FILE

if [[ `psql -U $DB_USER $DB -c '\dt'` != 'No relations found.' ]]; then
  echo dropping existing database
  psql -q -U $DB_USER $DB -c "`scripts/generate_drop_all.sh $DB_USER $DB`" -v ON_ERROR_STOP=1
#  psql -q -U $DB_USER $DB -f scripts/drop_all.sql -v ON_ERROR_STOP=1
  check_errs $? "drop_all.sh fails"
fi

psql -q -U $DB_USER $DB -f scripts/create_lincs_schema.sql -v ON_ERROR_STOP=1
check_errs $? "create schema fails"

psql -q -U $DB_USER $DB -f scripts/misc.sql -v ON_ERROR_STOP=1
check_errs $? "misc.sql fails"

psql -q -U $DB_USER $DB -f $DATA_DIRECTORY/lincs-users.sql -v ON_ERROR_STOP=1
check_errs $? "lincs-users.sql fails"

## Create the library
LIBRARY_1_SHORTNAME="R-HMS-LINCS"
LIBRARY_2_SHORTNAME="R-Anti-Mitotics"
LIBRARY_3_SHORTNAME="P-LINCS-1"
LIBRARY_4_SHORTNAME="P-Anti-mitotics5"
LIBRARY_5_SHORTNAME="P-Anti-mitotics6"
LIBRARY_6_SHORTNAME="P-Mario-1"
LIBRARY_7_SHORTNAME="R-CMT-1"
LIBRARY_8_SHORTNAME="P-LINCS-2"
LIBRARY_9_SHORTNAME="P-LINCS-3"
LIBRARY_10_SHORTNAME="P-Mario-2"

set -x 
./run.sh edu.harvard.med.screensaver.io.libraries.LibraryCreator \
-n "HMS-LINCS-1 BATCH 001" -s $LIBRARY_1_SHORTNAME -lt "commercial" \
-lp "Qingsong Liu" -st SMALL_MOLECULE -sp 1 -ep 1 -AE $ECOMMONS_ADMIN 
check_errs $? "create library fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryContentsLoader \
--release-library-contents-version \
-l $LIBRARY_1_SHORTNAME \
-f $DATA_DIRECTORY/HMS_LINCS-1.sdf -AE $ECOMMONS_ADMIN
check_errs $? "library contents loading fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryCreator \
-n "HMS-LINCS Anti-Mitotics" -s $LIBRARY_2_SHORTNAME -lt "commercial" \
-lp "Nate Moerke" -st SMALL_MOLECULE -sp 2 -ep 2 -AE $ECOMMONS_ADMIN 
check_errs $? "create library fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryContentsLoader \
--release-library-contents-version \
-l $LIBRARY_2_SHORTNAME \
-f $DATA_DIRECTORY/HMS_LINCS-2.sdf -AE $ECOMMONS_ADMIN
check_errs $? "library contents loading fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryCreator \
-n "HMS-LINCS-1 Stock Plates" -s $LIBRARY_3_SHORTNAME -ps WELLS_96 -lt "commercial" \
-lp "Qingsong Liu" -st SMALL_MOLECULE -sp 3 -ep 4 -AE $ECOMMONS_ADMIN -ds 2011-05-20
check_errs $? "create library fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryContentsLoader \
--release-library-contents-version \
-l $LIBRARY_3_SHORTNAME \
-f $DATA_DIRECTORY/HMS_LINCS-3.sdf -AE $ECOMMONS_ADMIN
check_errs $? "library contents loading fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryCreator \
-n "HMS-LINCS Anti-Mitotics Plate 5" -s $LIBRARY_4_SHORTNAME -lt "commercial" \
-lp "Nate Moerke" -st SMALL_MOLECULE -sp 5 -ep 5 -AE $ECOMMONS_ADMIN -ds 2010-06-01
check_errs $? "create library fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryContentsLoader \
--release-library-contents-version \
-l $LIBRARY_4_SHORTNAME \
-f $DATA_DIRECTORY/HMS_LINCS-4.sdf -AE $ECOMMONS_ADMIN
check_errs $? "library contents loading fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryCreator \
-n "HMS-LINCS Anti-Mitotics Plate 6" -s $LIBRARY_5_SHORTNAME -lt "commercial" \
-lp "Nate Moerke" -st SMALL_MOLECULE -sp 6 -ep 6 -AE $ECOMMONS_ADMIN -ds 2010-11-01
check_errs $? "create library fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryContentsLoader \
--release-library-contents-version \
-l $LIBRARY_5_SHORTNAME \
-f $DATA_DIRECTORY/HMS_LINCS-5.sdf -AE $ECOMMONS_ADMIN
check_errs $? "library contents loading fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryCreator \
-n "P-Mario-1 (ICCB-L 3265)" -s $LIBRARY_6_SHORTNAME -lt "commercial" \
-lp "Mario Niepel/Qingsong Liu" -st SMALL_MOLECULE -sp 7 -ep 7 -AE $ECOMMONS_ADMIN -ds 2011-05-27
check_errs $? "create library fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryContentsLoader \
--release-library-contents-version \
-l $LIBRARY_6_SHORTNAME \
-f $DATA_DIRECTORY/HMS_LINCS-6.sdf -AE $ECOMMONS_ADMIN
check_errs $? "library contents loading fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryCreator \
-n "R-CMT-1" -s $LIBRARY_7_SHORTNAME -lt "commercial" \
-lp "Cyril Benes" -st SMALL_MOLECULE -sp 8 -ep 8 -AE $ECOMMONS_ADMIN 
check_errs $? "create library fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryContentsLoader \
--release-library-contents-version \
-l $LIBRARY_7_SHORTNAME \
-f $DATA_DIRECTORY/HMS_LINCS-7.sdf -AE $ECOMMONS_ADMIN
check_errs $? "library contents loading fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryCreator \
-n "HMS-LINCS-2 Stock Plates" -s $LIBRARY_8_SHORTNAME -ps WELLS_96 -lt "commercial" \
-lp "Qingsong Liu" -st SMALL_MOLECULE -sp 9 -ep 10 -AE $ECOMMONS_ADMIN -ds 2011-12-15
check_errs $? "create library fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryContentsLoader \
--release-library-contents-version \
-l $LIBRARY_8_SHORTNAME \
-f $DATA_DIRECTORY/HMS_LINCS-8.sdf -AE $ECOMMONS_ADMIN
check_errs $? "library contents loading fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryCreator \
-n "HMS-LINCS-3 Stock Plates" -s $LIBRARY_9_SHORTNAME -ps WELLS_96 -lt "commercial" \
-lp "Qingsong Liu" -st SMALL_MOLECULE -sp 11 -ep 13 -AE $ECOMMONS_ADMIN -ds 2011-12-15
check_errs $? "create library fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryContentsLoader \
--release-library-contents-version \
-l $LIBRARY_9_SHORTNAME \
-f $DATA_DIRECTORY/HMS_LINCS-9.sdf -AE $ECOMMONS_ADMIN
check_errs $? "library contents loading fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryCreator \
-n "P-Mario-2 (ICCB-L 3295)" -s $LIBRARY_10_SHORTNAME -lt "commercial" \
-lp "Mario Niepel/Qingsong Liu" -st SMALL_MOLECULE -sp 14 -ep 14 -AE $ECOMMONS_ADMIN -ds 2012-01-05
check_errs $? "create library fails"

./run.sh edu.harvard.med.screensaver.io.libraries.LibraryContentsLoader \
--release-library-contents-version \
-l $LIBRARY_10_SHORTNAME \
-f $DATA_DIRECTORY/HMS_LINCS-10.sdf -AE $ECOMMONS_ADMIN
check_errs $? "library contents loading fails"

## Restrict reagents

psql -q -U $DB_USER $DB -f $DATA_DIRECTORY/restrict_reagents.sql -v ON_ERROR_STOP=1
check_errs $? "lincs-users.sql fails"

## Create the screens

LEAD_SCREENER_FIRST="Nathan"
LEAD_SCREENER_LAST="Moerke"
LEAD_SCREENER_EMAIL="nathanmoerke@gmail.com"
LAB_HEAD_FIRST="Tim"
LAB_HEAD_LAST="Mitchison"
LAB_HEAD_EMAIL="timothy_mitchison@hms.harvard.edu"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Moerke 2 Color Apoptosis: IA-LM cells.'  \
-i 10001 \
--summary "Moerke 2 Color Apoptosis: IA-LM cells. Dose response of anti-mitotic compounds in human cancer cell lines at 24, 48, and 72 hours to determine their effects on apoptosis.  In this assay, the cell-permeable DNA dye Hoechst 33342 is used to stain the nuclei of all cells.  The fluorescent caspase 3 reporter NucView488 stains the nuclei of cells undergoing apoptosis (in which caspase 3 is active)." \
-p 'Day 1: Seed cells in 384-well assay plates, at approximately 2000 cells/well (the exact density varies by cell line), with 3 plates per cell line (one each for a 24 hr, 48 hr and 72 hr time point).  Add 30 uL cell suspension per well.
Day 2: Add compounds to plates by pin transfer.
Day 3: Prepare cells for 24 hr timepoint.
Prepare the 4X apoptosis reagent mixture fresh in PBS
4X apoptosis reagent mixture composition:
(1) DEVD-NucView488 caspase substrate: 1 uM
(2) Hoechst 33342:  2 ug/mL
For 4 384-well plates, 25 mL of this mixture will be sufficient and allow plenty of dead volume.
For 25 mL of mixture add 25 uL of DEVD-NucView488 substrate (from refrigerator) and 50 uL of Hoechst 33342 (from freezer).
Use WellMate (hood or bench is fine) to add the mixture, using the designated manifold (labeled â€œNucViewâ€?).  Add 10 uL per well, skipping the 1st 2 and last 2 columns.  Spin plates briefly at 1000 rpm in plate centrifuge and put in incubator for 90 minutes.
Prepare 2X fixative solution: 2 % formaldehyde in PBS.  Dilute a 36-36.5% formaldehyde stock bottle 1:20 in PBS.  100 mL fixative total is sufficient for 4 plates; add 5 mL formaldehyde stock to 95 mL PBS.
After 90 minutes, use benchtop (not hood) WellMate to add fixative to plates.  Use the manifold labeled â€œFixativeâ€?  Add 40 uL per well (again skipping first 2 and last 2 columns).  Spin plates briefly as before.  Let fix 20-30 minutes at RT, then seal with metal foil, and image right away or store in the cold room until you are ready to image.
Day 4: Repeat for 48 hr time point
Day 5: Repeat for 72 hr time point

Plates are imaged on the ImageXpress Micro screening microscope (Molecular Devices).  4 images are collected per well of the plate at 10X magnification, using the DAPI and FITC filter sets of this instrument.
Images are analyzed using MetaXpress software.  The multiwavelength cell scoring module of the software is used to detect all cells using the DAPI channel and score cells as apoptotic cells (NucView 488 positive) using the FITC channel.  The analysis produces for each well the total cell count and the % of cells in the well that are apoptotic.

Reagent stocks:
Hoechst 33342 (Invitrogen ) â€“ 1 mg/mL stock in H2O, store at -20 degrees C
DEVD-NucView488 (Biotium) â€“  1 mM in DMSO, store at 4 degrees C protected from light

References:

Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.

http://www.biotium.com/product/applications/Cell_Biology/price_and_info.asp?item=30029&layer1=A;&layer2=A02;&layer3=A0203;'
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/moerke_2color_IA-LM.xls \
-s 10001 -i 
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Moerke 2 Color Apoptosis: IST-MEL1 cells.'  \
-i 10002 \
--summary "Moerke 2 Color Apoptosis: IST-MEL1 cells. Dose response of anti-mitotic compounds in human cancer cell lines at 24, 48, and 72 hours to determine their effects on apoptosis.  In this assay, the cell-permeable DNA dye Hoechst 33342 is used to stain the nuclei of all cells.  The fluorescent caspase 3 reporter NucView488 stains the nuclei of cells undergoing apoptosis (in which caspase 3 is active)." \
-p 'Day 1: Seed cells in 384-well assay plates, at approximately 2000 cells/well (the exact density varies by cell line), with 3 plates per cell line (one each for a 24 hr, 48 hr and 72 hr time point).  Add 30 uL cell suspension per well.
Day 2: Add compounds to plates by pin transfer.
Day 3: Prepare cells for 24 hr timepoint.
Prepare the 4X apoptosis reagent mixture fresh in PBS
4X apoptosis reagent mixture composition:
(1) DEVD-NucView488 caspase substrate: 1 uM
(2) Hoechst 33342:  2 ug/mL
For 4 384-well plates, 25 mL of this mixture will be sufficient and allow plenty of dead volume.
For 25 mL of mixture add 25 uL of DEVD-NucView488 substrate (from refrigerator) and 50 uL of Hoechst 33342 (from freezer).
Use WellMate (hood or bench is fine) to add the mixture, using the designated manifold (labeled â€œNucViewâ€?).  Add 10 uL per well, skipping the 1st 2 and last 2 columns.  Spin plates briefly at 1000 rpm in plate centrifuge and put in incubator for 90 minutes.
Prepare 2X fixative solution: 2 % formaldehyde in PBS.  Dilute a 36-36.5% formaldehyde stock bottle 1:20 in PBS.  100 mL fixative total is sufficient for 4 plates; add 5 mL formaldehyde stock to 95 mL PBS.
After 90 minutes, use benchtop (not hood) WellMate to add fixative to plates.  Use the manifold labeled â€œFixativeâ€?  Add 40 uL per well (again skipping first 2 and last 2 columns).  Spin plates briefly as before.  Let fix 20-30 minutes at RT, then seal with metal foil, and image right away or store in the cold room until you are ready to image.
Day 4: Repeat for 48 hr time point
Day 5: Repeat for 72 hr time point

Plates are imaged on the ImageXpress Micro screening microscope (Molecular Devices).  4 images are collected per well of the plate at 10X magnification, using the DAPI and FITC filter sets of this instrument.
Images are analyzed using MetaXpress software.  The multiwavelength cell scoring module of the software is used to detect all cells using the DAPI channel and score cells as apoptotic cells (NucView 488 positive) using the FITC channel.  The analysis produces for each well the total cell count and the % of cells in the well that are apoptotic.

Reagent stocks:
Hoechst 33342 (Invitrogen ) â€“ 1 mg/mL stock in H2O, store at -20 degrees C
DEVD-NucView488 (Biotium) â€“  1 mM in DMSO, store at 4 degrees C protected from light

References:

Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.

http://www.biotium.com/product/applications/Cell_Biology/price_and_info.asp?item=30029&layer1=A;&layer2=A02;&layer3=A0203;' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/moerke_2color_IS-MELI.xls \
-s 10002 -i 
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Moerke 2 Color Apoptosis: NCI-H1648 cells.'  \
-i 10003 \
--summary "Moerke 2 Color Apoptosis: NCI-H1648 cells. Dose response of anti-mitotic compounds in human cancer cell lines at 24 hours to determine their effects on apoptosis.  In this assay, the cell-permeable DNA dye Hoechst 33342 is used to stain the nuclei of all cells.  The fluorescent caspase 3 reporter NucView488 stains the nuclei of cells undergoing apoptosis (in which caspase 3 is active)." \
-p 'Day 1: Seed cells in 384-well assay plates, at approximately 2000 cells/well (the exact density varies by cell line), with 3 plates per cell line (one each for 24 hr time point).  Add 30 uL cell suspension per well.
Day 2: Add compounds to plates by pin transfer.
Day 3: Prepare cells for 24 hr timepoint.
Prepare the 4X apoptosis reagent mixture fresh in PBS
4X apoptosis reagent mixture composition:
(1) DEVD-NucView488 caspase substrate: 1 uM
(2) Hoechst 33342:  2 ug/mL
For 4 384-well plates, 25 mL of this mixture will be sufficient and allow plenty of dead volume.
For 25 mL of mixture add 25 uL of DEVD-NucView488 substrate (from refrigerator) and 50 uL of Hoechst 33342 (from freezer).
Use WellMate (hood or bench is fine) to add the mixture, using the designated manifold (labeled â€œNucViewâ€?).  Add 10 uL per well, skipping the 1st 2 and last 2 columns.  Spin plates briefly at 1000 rpm in plate centrifuge and put in incubator for 90 minutes.
Prepare 2X fixative solution: 2 % formaldehyde in PBS.  Dilute a 36-36.5% formaldehyde stock bottle 1:20 in PBS.  100 mL fixative total is sufficient for 4 plates; add 5 mL formaldehyde stock to 95 mL PBS.
After 90 minutes, use benchtop (not hood) WellMate to add fixative to plates.  Use the manifold labeled â€œFixativeâ€?  Add 40 uL per well (again skipping first 2 and last 2 columns).  Spin plates briefly as before.  Let fix 20-30 minutes at RT, then seal with metal foil, and image right away or store in the cold room until you are ready to image.

Plates are imaged on the ImageXpress Micro screening microscope (Molecular Devices).  4 images are collected per well of the plate at 10X magnification, using the DAPI and FITC filter sets of this instrument.
Images are analyzed using MetaXpress software.  The multiwavelength cell scoring module of the software is used to detect all cells using the DAPI channel and score cells as apoptotic cells (NucView 488 positive) using the FITC channel.  The analysis produces for each well the total cell count and the % of cells in the well that are apoptotic.

Reagent stocks:
Hoechst 33342 (Invitrogen ) â€“ 1 mg/mL stock in H2O, store at -20 degrees C
DEVD-NucView488 (Biotium) â€“  1 mM in DMSO, store at 4 degrees C protected from light

References:

Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.

http://www.biotium.com/product/applications/Cell_Biology/price_and_info.asp?item=30029&layer1=A;&layer2=A02;&layer3=A0203;' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/moerke_2color_NCI-1648.xls \
-s 10003 -i 
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Moerke 2 Color Apoptosis: PC-9 cells.'  \
-i 10004 \
--summary "Moerke 2 Color Apoptosis: PC-9 cells. Dose response of anti-mitotic compounds in human cancer cell lines at 24, 48, and 72 hours to determine their effects on apoptosis.  In this assay, the cell-permeable DNA dye Hoechst 33342 is used to stain the nuclei of all cells.  The fluorescent caspase 3 reporter NucView488 stains the nuclei of cells undergoing apoptosis (in which caspase 3 is active)." \
-p 'Day 1: Seed cells in 384-well assay plates, at approximately 2000 cells/well (the exact density varies by cell line), with 3 plates per cell line (one each for a 24 hr, 48 hr and 72 hr time point).  Add 30 uL cell suspension per well.
Day 2: Add compounds to plates by pin transfer.
Day 3: Prepare cells for 24 hr timepoint.
Prepare the 4X apoptosis reagent mixture fresh in PBS
4X apoptosis reagent mixture composition:
(1) DEVD-NucView488 caspase substrate: 1 uM
(2) Hoechst 33342:  2 ug/mL
For 4 384-well plates, 25 mL of this mixture will be sufficient and allow plenty of dead volume.
For 25 mL of mixture add 25 uL of DEVD-NucView488 substrate (from refrigerator) and 50 uL of Hoechst 33342 (from freezer).
Use WellMate (hood or bench is fine) to add the mixture, using the designated manifold (labeled â€œNucViewâ€?).  Add 10 uL per well, skipping the 1st 2 and last 2 columns.  Spin plates briefly at 1000 rpm in plate centrifuge and put in incubator for 90 minutes.
Prepare 2X fixative solution: 2 % formaldehyde in PBS.  Dilute a 36-36.5% formaldehyde stock bottle 1:20 in PBS.  100 mL fixative total is sufficient for 4 plates; add 5 mL formaldehyde stock to 95 mL PBS.
After 90 minutes, use benchtop (not hood) WellMate to add fixative to plates.  Use the manifold labeled â€œFixativeâ€?  Add 40 uL per well (again skipping first 2 and last 2 columns).  Spin plates briefly as before.  Let fix 20-30 minutes at RT, then seal with metal foil, and image right away or store in the cold room until you are ready to image.
Day 4: Repeat for 48 hr time point
Day 5: Repeat for 72 hr time point

Plates are imaged on the ImageXpress Micro screening microscope (Molecular Devices).  4 images are collected per well of the plate at 10X magnification, using the DAPI and FITC filter sets of this instrument.
Images are analyzed using MetaXpress software.  The multiwavelength cell scoring module of the software is used to detect all cells using the DAPI channel and score cells as apoptotic cells (NucView 488 positive) using the FITC channel.  The analysis produces for each well the total cell count and the % of cells in the well that are apoptotic.

Reagent stocks:
Hoechst 33342 (Invitrogen ) â€“ 1 mg/mL stock in H2O, store at -20 degrees C
DEVD-NucView488 (Biotium) â€“  1 mM in DMSO, store at 4 degrees C protected from light

References:

Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.

http://www.biotium.com/product/applications/Cell_Biology/price_and_info.asp?item=30029&layer1=A;&layer2=A02;&layer3=A0203;' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/moerke_2color_PC-9.xls \
-s 10004 -i 
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Moerke 2 Color Apoptosis: SK-LMS-1 cells.'  \
-i 10005 \
--summary "Moerke 2 Color Apoptosis: SK-LMS-1 cells. Dose response of anti-mitotic compounds in human cancer cell lines at 24 and 48 hours to determine their effects on apoptosis.  In this assay, the cell-permeable DNA dye Hoechst 33342 is used to stain the nuclei of all cells.  The fluorescent caspase 3 reporter NucView488 stains the nuclei of cells undergoing apoptosis (in which caspase 3 is active)." \
-p 'Day 1: Seed cells in 384-well assay plates, at approximately 2000 cells/well (the exact density varies by cell line), with 3 plates per cell line (one each for a 24 hr and 48 hr time point).  Add 30 uL cell suspension per well.
Day 2: Add compounds to plates by pin transfer.
Day 3: Prepare cells for 24 hr timepoint.
Prepare the 4X apoptosis reagent mixture fresh in PBS
4X apoptosis reagent mixture composition:
(1) DEVD-NucView488 caspase substrate: 1 uM
(2) Hoechst 33342:  2 ug/mL
For 4 384-well plates, 25 mL of this mixture will be sufficient and allow plenty of dead volume.
For 25 mL of mixture add 25 uL of DEVD-NucView488 substrate (from refrigerator) and 50 uL of Hoechst 33342 (from freezer).
Use WellMate (hood or bench is fine) to add the mixture, using the designated manifold (labeled â€œNucViewâ€?).  Add 10 uL per well, skipping the 1st 2 and last 2 columns.  Spin plates briefly at 1000 rpm in plate centrifuge and put in incubator for 90 minutes.
Prepare 2X fixative solution: 2 % formaldehyde in PBS.  Dilute a 36-36.5% formaldehyde stock bottle 1:20 in PBS.  100 mL fixative total is sufficient for 4 plates; add 5 mL formaldehyde stock to 95 mL PBS.
After 90 minutes, use benchtop (not hood) WellMate to add fixative to plates.  Use the manifold labeled â€œFixativeâ€?  Add 40 uL per well (again skipping first 2 and last 2 columns).  Spin plates briefly as before.  Let fix 20-30 minutes at RT, then seal with metal foil, and image right away or store in the cold room until you are ready to image.
Day 4: Repeat for 48 hr time point

Plates are imaged on the ImageXpress Micro screening microscope (Molecular Devices).  4 images are collected per well of the plate at 10X magnification, using the DAPI and FITC filter sets of this instrument.
Images are analyzed using MetaXpress software.  The multiwavelength cell scoring module of the software is used to detect all cells using the DAPI channel and score cells as apoptotic cells (NucView 488 positive) using the FITC channel.  The analysis produces for each well the total cell count and the % of cells in the well that are apoptotic.

Reagent stocks:
Hoechst 33342 (Invitrogen ) â€“ 1 mg/mL stock in H2O, store at -20 degrees C
DEVD-NucView488 (Biotium) â€“  1 mM in DMSO, store at 4 degrees C protected from light

References:

Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.

http://www.biotium.com/product/applications/Cell_Biology/price_and_info.asp?item=30029&layer1=A;&layer2=A02;&layer3=A0203;' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/moerke_2color_SK-LM31.xls \
-s 10005 -i 
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Moerke 3 Color Apoptosis: 5637 cells.'  \
-i 10006 \
--summary "Moerke 3 Color Apoptosis: 5637 cells. Dose response of anti-mitotic compounds in human cancer cell lines at 24 and 48 hours to determine their effects on apoptosis and cell death.  In this assay, the cell-permeable DNA dye Hoechst 33342 is used to stain the nuclei of all cells.  The fluorescent caspase 3 reporter NucView488 stains the nuclei of cells undergoing apoptosis (in which caspase 3 is active), and the cell-impermeable DNA dye TO-PRO3 stains only the nuclei of dead or dying cells in which membrane integrity is compromised." \
-p 'Day 1: Seed cells in 384-well assay plates at approximately 2000 cells/well (the exact density varies by cell line), with 3 plates per cell line (one each for a 24 hr and 48 hr time point).  Add 30 uL cell suspension per well.
Day 2: Add compounds to plates by pin transfer.
Day 3: Process cells for 24 hr timepoint. 
Prepare the 4X apoptosis reagent mixture fresh in PBS or media from stocks.
4X apoptosis reagent mixture composition:
(1) DEVD-NucView488 caspase substrate: 1 uM
(2) TO-PRO-3: 2 uM
(3) Hoechst 33342:  2 ug/mL
Add 10 uL mixture per well using WellMate plate filler or multichannel pipette, and leave in tissue culture incubator for 2 hrs.
Remove plate from incubator, seal, and image using IX Micro â€“ should take 45-60 min to read an entire plate (assuming 10X magnification and 4 sites per well) depending on the exact settings.
If reading multiple plates, stagger reagent addition times by the time required for imaging so that the incubation times are equal.
Day 4: Repeat for 48 hr time point

Plates are imaged on the ImageXpress Micro screening microscope.  4 images are collected per well of the plate at 10X magnification, using the DAPI, FITC, and Cy5 filter sets of this instrument.
Images are analyzed using MetaXpress software.  The multiwavelength cell scoring module of the software is used to detect all cells using the DAPI channel, score cells as apoptotic cells (NucView 488 positive) using the FITC channel, and score cells as dead or dying (TO-PRO-3 positive) using the Cy5 channel.  The analysis produces for each well the total cell count, the % of cells in the well that are apoptotic, and the % of cells in the well that are dead or dying.

Reagent stocks:
Hoechst 33342 (Invitrogen ) â€“ 1 mg/mL stock in H2O, store at -20 degrees C
DEVD-NucView488 (Biotium) â€“  1 mM in DMSO, store at 4 degrees C and protect from light
TO-PRO-3 (Invitrogen) â€“ 1 mM in DMSO, store at -20 degrees C

References:

Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.

http://www.biotium.com/product/applications/Cell_Biology/price_and_info.asp?item=30029&layer1=A;&layer2=A02;&layer3=A0203;' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/moerke_3color_5637.xls \
-s 10006 -i 
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Moerke 3 Color Apoptosis: BPH-1 cells.'  \
-i 10007 \
--summary "Moerke 3 Color Apoptosis: BPH-1 cells. Dose response of anti-mitotic compounds in human cancer cell lines at 24 and 48 hours to determine their effects on apoptosis and cell death.  In this assay, the cell-permeable DNA dye Hoechst 33342 is used to stain the nuclei of all cells.  The fluorescent caspase 3 reporter NucView488 stains the nuclei of cells undergoing apoptosis (in which caspase 3 is active), and the cell-impermeable DNA dye TO-PRO3 stains only the nuclei of dead or dying cells in which membrane integrity is compromised." \
-p 'Day 1: Seed cells in 384-well assay plates at approximately 2000 cells/well (the exact density varies by cell line), with 3 plates per cell line (one each for a 24 hr and 48 hr time point).  Add 30 uL cell suspension per well.
Day 2: Add compounds to plates by pin transfer.
Day 3: Process cells for 24 hr timepoint. 
Prepare the 4X apoptosis reagent mixture fresh in PBS or media from stocks.
4X apoptosis reagent mixture composition:
(1) DEVD-NucView488 caspase substrate: 1 uM
(2) TO-PRO-3: 2 uM
(3) Hoechst 33342:  2 ug/mL
Add 10 uL mixture per well using WellMate plate filler or multichannel pipette, and leave in tissue culture incubator for 2 hrs.
Remove plate from incubator, seal, and image using IX Micro â€“ should take 45-60 min to read an entire plate (assuming 10X magnification and 4 sites per well) depending on the exact settings.
If reading multiple plates, stagger reagent addition times by the time required for imaging so that the incubation times are equal.
Day 4: Repeat for 48 hr time point

Plates are imaged on the ImageXpress Micro screening microscope.  4 images are collected per well of the plate at 10X magnification, using the DAPI, FITC, and Cy5 filter sets of this instrument.
Images are analyzed using MetaXpress software.  The multiwavelength cell scoring module of the software is used to detect all cells using the DAPI channel, score cells as apoptotic cells (NucView 488 positive) using the FITC channel, and score cells as dead or dying (TO-PRO-3 positive) using the Cy5 channel.  The analysis produces for each well the total cell count, the % of cells in the well that are apoptotic, and the % of cells in the well that are dead or dying.

Reagent stocks:
Hoechst 33342 (Invitrogen ) â€“ 1 mg/mL stock in H2O, store at -20 degrees C
DEVD-NucView488 (Biotium) â€“  1 mM in DMSO, store at 4 degrees C and protect from light
TO-PRO-3 (Invitrogen) â€“ 1 mM in DMSO, store at -20 degrees C

References:

Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.

http://www.biotium.com/product/applications/Cell_Biology/price_and_info.asp?item=30029&layer1=A;&layer2=A02;&layer3=A0203;' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/moerke_3color_BPH-1.xls \
-s 10007 -i 
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Moerke 3 Color Apoptosis: NCI-H810 cells.'  \
-i 10008 \
--summary "Moerke 3 Color Apoptosis: NCI-H810 cells. Dose response of anti-mitotic compounds in human cancer cell lines at 24 hours to determine their effects on apoptosis and cell death.  In this assay, the cell-permeable DNA dye Hoechst 33342 is used to stain the nuclei of all cells.  The fluorescent caspase 3 reporter NucView488 stains the nuclei of cells undergoing apoptosis (in which caspase 3 is active), and the cell-impermeable DNA dye TO-PRO3 stains only the nuclei of dead or dying cells in which membrane integrity is compromised." \
-p 'Day 1: Seed cells in 384-well assay plates at approximately 2000 cells/well (the exact density varies by cell line), with 3 plates per cell line (one each for a 24 hr time point).  Add 30 uL cell suspension per well.
Day 2: Add compounds to plates by pin transfer.
Day 3: Process cells for 24 hr timepoint. 
Prepare the 4X apoptosis reagent mixture fresh in PBS or media from stocks.
4X apoptosis reagent mixture composition:
(1) DEVD-NucView488 caspase substrate: 1 uM
(2) TO-PRO-3: 2 uM
(3) Hoechst 33342:  2 ug/mL
Add 10 uL mixture per well using WellMate plate filler or multichannel pipette, and leave in tissue culture incubator for 2 hrs.
Remove plate from incubator, seal, and image using IX Micro â€“ should take 45-60 min to read an entire plate (assuming 10X magnification and 4 sites per well) depending on the exact settings.
If reading multiple plates, stagger reagent addition times by the time required for imaging so that the incubation times are equal.

Plates are imaged on the ImageXpress Micro screening microscope.  4 images are collected per well of the plate at 10X magnification, using the DAPI, FITC, and Cy5 filter sets of this instrument.
Images are analyzed using MetaXpress software.  The multiwavelength cell scoring module of the software is used to detect all cells using the DAPI channel, score cells as apoptotic cells (NucView 488 positive) using the FITC channel, and score cells as dead or dying (TO-PRO-3 positive) using the Cy5 channel.  The analysis produces for each well the total cell count, the % of cells in the well that are apoptotic, and the % of cells in the well that are dead or dying.

Reagent stocks:
Hoechst 33342 (Invitrogen ) â€“ 1 mg/mL stock in H2O, store at -20 degrees C
DEVD-NucView488 (Biotium) â€“  1 mM in DMSO, store at 4 degrees C and protect from light
TO-PRO-3 (Invitrogen) â€“ 1 mM in DMSO, store at -20 degrees C

References:

Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.

http://www.biotium.com/product/applications/Cell_Biology/price_and_info.asp?item=30029&layer1=A;&layer2=A02;&layer3=A0203;' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/moerke_3color_H810.xls \
-s 10008 -i 
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Moerke 3 Color Apoptosis: KYSE-140 cells.'  \
-i 10009 \
--summary "Moerke 3 Color Apoptosis: KYSE-140 cells. Dose response of anti-mitotic compounds in human cancer cell lines at 24 and 48 hours to determine their effects on apoptosis and cell death.  In this assay, the cell-permeable DNA dye Hoechst 33342 is used to stain the nuclei of all cells.  The fluorescent caspase 3 reporter NucView488 stains the nuclei of cells undergoing apoptosis (in which caspase 3 is active), and the cell-impermeable DNA dye TO-PRO3 stains only the nuclei of dead or dying cells in which membrane integrity is compromised." \
-p 'Day 1: Seed cells in 384-well assay plates at approximately 2000 cells/well (the exact density varies by cell line), with 3 plates per cell line (one each for a 24 hr and 48 hr time point).  Add 30 uL cell suspension per well.
Day 2: Add compounds to plates by pin transfer.
Day 3: Process cells for 24 hr timepoint. 
Prepare the 4X apoptosis reagent mixture fresh in PBS or media from stocks.
4X apoptosis reagent mixture composition:
(1) DEVD-NucView488 caspase substrate: 1 uM
(2) TO-PRO-3: 2 uM
(3) Hoechst 33342:  2 ug/mL
Add 10 uL mixture per well using WellMate plate filler or multichannel pipette, and leave in tissue culture incubator for 2 hrs.
Remove plate from incubator, seal, and image using IX Micro â€“ should take 45-60 min to read an entire plate (assuming 10X magnification and 4 sites per well) depending on the exact settings.
If reading multiple plates, stagger reagent addition times by the time required for imaging so that the incubation times are equal.
Day 4: Repeat for 48 hr time point

Plates are imaged on the ImageXpress Micro screening microscope.  4 images are collected per well of the plate at 10X magnification, using the DAPI, FITC, and Cy5 filter sets of this instrument.
Images are analyzed using MetaXpress software.  The multiwavelength cell scoring module of the software is used to detect all cells using the DAPI channel, score cells as apoptotic cells (NucView 488 positive) using the FITC channel, and score cells as dead or dying (TO-PRO-3 positive) using the Cy5 channel.  The analysis produces for each well the total cell count, the % of cells in the well that are apoptotic, and the % of cells in the well that are dead or dying.

Reagent stocks:
Hoechst 33342 (Invitrogen ) â€“ 1 mg/mL stock in H2O, store at -20 degrees C
DEVD-NucView488 (Biotium) â€“  1 mM in DMSO, store at 4 degrees C and protect from light
TO-PRO-3 (Invitrogen) â€“ 1 mM in DMSO, store at -20 degrees C

References:

Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.

http://www.biotium.com/product/applications/Cell_Biology/price_and_info.asp?item=30029&layer1=A;&layer2=A02;&layer3=A0203;' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/moerke_3color_KYSE-140.xls \
-s 10009 -i 
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Moerke 3 Color Apoptosis: KYSE-180 cells.'  \
-i 10010 \
--summary "Moerke 3 Color Apoptosis: KYSE-180 cells. Dose response of anti-mitotic compounds in human cancer cell lines at 24 and 48 hours to determine their effects on apoptosis and cell death.  In this assay, the cell-permeable DNA dye Hoechst 33342 is used to stain the nuclei of all cells.  The fluorescent caspase 3 reporter NucView488 stains the nuclei of cells undergoing apoptosis (in which caspase 3 is active), and the cell-impermeable DNA dye TO-PRO3 stains only the nuclei of dead or dying cells in which membrane integrity is compromised." \
-p 'Day 1: Seed cells in 384-well assay plates at approximately 2000 cells/well (the exact density varies by cell line), with 3 plates per cell line (one each for a 24 hr and 48 hr time point).  Add 30 uL cell suspension per well.
Day 2: Add compounds to plates by pin transfer.
Day 3: Process cells for 24 hr timepoint. 
Prepare the 4X apoptosis reagent mixture fresh in PBS or media from stocks.
4X apoptosis reagent mixture composition:
(1) DEVD-NucView488 caspase substrate: 1 uM
(2) TO-PRO-3: 2 uM
(3) Hoechst 33342:  2 ug/mL
Add 10 uL mixture per well using WellMate plate filler or multichannel pipette, and leave in tissue culture incubator for 2 hrs.
Remove plate from incubator, seal, and image using IX Micro â€“ should take 45-60 min to read an entire plate (assuming 10X magnification and 4 sites per well) depending on the exact settings.
If reading multiple plates, stagger reagent addition times by the time required for imaging so that the incubation times are equal.
Day 4: Repeat for 48 hr time point

Plates are imaged on the ImageXpress Micro screening microscope.  4 images are collected per well of the plate at 10X magnification, using the DAPI, FITC, and Cy5 filter sets of this instrument.
Images are analyzed using MetaXpress software.  The multiwavelength cell scoring module of the software is used to detect all cells using the DAPI channel, score cells as apoptotic cells (NucView 488 positive) using the FITC channel, and score cells as dead or dying (TO-PRO-3 positive) using the Cy5 channel.  The analysis produces for each well the total cell count, the % of cells in the well that are apoptotic, and the % of cells in the well that are dead or dying.

Reagent stocks:
Hoechst 33342 (Invitrogen ) â€“ 1 mg/mL stock in H2O, store at -20 degrees C
DEVD-NucView488 (Biotium) â€“  1 mM in DMSO, store at 4 degrees C and protect from light
TO-PRO-3 (Invitrogen) â€“ 1 mM in DMSO, store at -20 degrees C

References:

Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.

http://www.biotium.com/product/applications/Cell_Biology/price_and_info.asp?item=30029&layer1=A;&layer2=A02;&layer3=A0203;' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/moerke_3color_KYSE-180.xls \
-s 10010 -i 
check_errs $? "create screen result import fails"

LEAD_SCREENER_FIRST="Yangzhong"
LEAD_SCREENER_LAST="Tang"
LEAD_SCREENER_EMAIL="yangzhong_tang@hms.harvard.edu"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: A375.S2 cells.'  \
-i 10011 \
--summary "Tang Mitosis/Apoptosis ver.II: A375.S2 cells. Dose response of anti-mitotic compounds in human cancer cell line A375.S2 at 24 and 48 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_A375S2.xls \
-s 10011 -i 
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: AGS cells.'  \
-i 10012 \
--summary "Tang Mitosis/Apoptosis ver.II: AGS cells. Dose response of anti-mitotic compounds in human cancer cell line AGS at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_AGS.xls \
-s 10012 -i 
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: Calu-3 cells.'  \
-i 10013 \
--summary "Tang Mitosis/Apoptosis ver.II: Calu-3 cells. Dose response of anti-mitotic compounds in human cancer cell line Calu-3 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_Calu-3.xls \
-s 10013 -i 
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: Ca Ski cells.'  \
-i 10014 \
--summary "Tang Mitosis/Apoptosis ver.II: Ca Ski cells. Dose response of anti-mitotic compounds in human cancer cell line Ca Ski at 24 and 48 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_Caski.xls \
-s 10014 -i 
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: Colo-679 cells.'  \
-i 10015 \
--summary "Tang Mitosis/Apoptosis ver.II: Colo-679 cells. Dose response of anti-mitotic compounds in human cancer cell line Colo-679 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_Colo-679.xls \
-s 10015 -i 
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: HEC-1 cells.'  \
-i 10016 \
--summary "Tang Mitosis/Apoptosis ver.II: HEC-1 cells. Dose response of anti-mitotic compounds in human cancer cell line HEC-1 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_HEC-1.xls \
-s 10016 -i 
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: Ishikawa (Heraklio) 02 ER- cells.'  \
-i 10017 \
--summary "Tang Mitosis/Apoptosis ver.II: Ishikawa (Heraklio) 02 ER- cells. Dose response of anti-mitotic compounds in human cancer cell line Ishikawa (Heraklio) 02 ER- at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_Ishikawa-02-ER.xls \
-s 10017 -i 
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: JHH-6 cells.'  \
-i 10018 \
--summary "Tang Mitosis/Apoptosis ver.II: JHH-6 cells. Dose response of anti-mitotic compounds in human cancer cell line JHH-6 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_JHH-6.xls \
-s 10018 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: Kyse-150 cells.'  \
-i 10019 \
--summary "Tang Mitosis/Apoptosis ver.II: Kyse-150 cells. Dose response of anti-mitotic compounds in human cancer cell line Kyse-150 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_Kyse-150.xls \
-s 10019 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: Kyse-450 cells.'  \
-i 10020 \
--summary "Tang Mitosis/Apoptosis ver.II: Kyse-450 cells. Dose response of anti-mitotic compounds in human cancer cell line Kyse-450 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_Kyse-450.xls \
-s 10020 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: LNZTA3WT4 cells.'  \
-i 10021 \
--summary "Tang Mitosis/Apoptosis ver.II: LNZTA3WT4 cells. Dose response of anti-mitotic compounds in human cancer cell line LNZTA3WT4 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_LNZTA3WT4.xls \
-s 10021 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: MDA-MB-435S cells.'  \
-i 10022 \
--summary "Tang Mitosis/Apoptosis ver.II: MDA-MB-435S cells. Dose response of anti-mitotic compounds in human cancer cell line MDA-MB-435S at 24  hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_MDA-MB-435S.xls \
-s 10022 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: MT-3 cells.'  \
-i 10023 \
--summary "Tang Mitosis/Apoptosis ver.II: MT-3 cells. Dose response of anti-mitotic compounds in human cancer cell line MT-3 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_MT-3.xls \
-s 10023 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: NCI-H1651 cells.'  \
-i 10024 \
--summary "Tang Mitosis/Apoptosis ver.II: NCI-H1651 cells. Dose response of anti-mitotic compounds in human cancer cell line NCI-H1651 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_NCI-1651.xls \
-s 10024 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: NCI-H1915 cells.'  \
-i 10025 \
--summary "Tang Mitosis/Apoptosis ver.II: NCI-H1915 cells. Dose response of anti-mitotic compounds in human cancer cell line NCI-H1915 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_NCI-H1915.xls \
-s 10025 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: NCI-H2023 cells.'  \
-i 10026 \
--summary "Tang Mitosis/Apoptosis ver.II: NCI-H2023 cells. Dose response of anti-mitotic compounds in human cancer cell line NCI-H2023 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_NCI-H2023.xls \
-s 10026 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: PE/CA-PJ15 cells.'  \
-i 10027 \
--summary "Tang Mitosis/Apoptosis ver.II: PE/CA-PJ15 cells. Dose response of anti-mitotic compounds in human cancer cell line PE/CA-PJ15 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_PE-CA-PJ15.xls \
-s 10027 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: PL4 cells.'  \
-i 10028 \
--summary "Tang Mitosis/Apoptosis ver.II: PL4 cells. Dose response of anti-mitotic compounds in human cancer cell line PL4 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4Î¼g/mL Hoechst33342 (Sigma B2261), 4ÂµM LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_PL-4.xls \
-s 10028 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: SK-OV-3 cells.'  \
-i 10029 \
--summary "Tang Mitosis/Apoptosis ver.II: SK-OV-3 cells. Dose response of anti-mitotic compounds in human cancer cell line SK-OV-3 at 24 and 48 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_SKOV3.xls \
-s 10029 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: T24 cells.'  \
-i 10030 \
--summary "Tang Mitosis/Apoptosis ver.II: T24 cells. Dose response of anti-mitotic compounds in human cancer cell line T24 at 24 and 48 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_T24.xls \
-s 10030 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: WiDr cells.'  \
-i 10031 \
--summary "Tang Mitosis/Apoptosis ver.II: WiDr cells. Dose response of anti-mitotic compounds in human cancer cell line WiDr at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ï?­g/mL Hoechst33342 (Sigma B2261), 4ï?­M LysoTracker-Red (Invitrogen L7528), and 2ï?­M DEVD-NucView488 (Biotium 10403).
(B) Add 10ï?­L of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ï?­g/mL, LysoTracker-Red is 1ï?­M, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40ï?­L of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_WiDr.xls \
-s 10031 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: 5637 cells.'  \
-i 10032 \
--summary "Tang Proliferation/Mitosis: 5637 cells. Dose response of anti-mitotic compounds in human cancer cell line 5637 at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_5637.xls \
-s 10032 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: BPH-1 cells.'  \
-i 10033 \
--summary "Tang Proliferation/Mitosis: BPH-1 cells. Dose response of anti-mitotic compounds in human cancer cell line BPH-1 at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_BTH-1.xls \
-s 10033 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: COLO-800 cells.'  \
-i 10034 \
--summary "Tang Proliferation/Mitosis: COLO-800 cells. Dose response of anti-mitotic compounds in human cancer cell line COLO-800 at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_Colo-800.xls \
-s 10034 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: HUTU-80 cells.'  \
-i 10035 \
--summary "Tang Proliferation/Mitosis: HUTU-80 cells. Dose response of anti-mitotic compounds in human cancer cell line HUTU-80 at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_HuTu-80.xls \
-s 10035 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: IA-LM cells.'  \
-i 10036 \
--summary "Tang Proliferation/Mitosis: IA-LM cells. Dose response of anti-mitotic compounds in human cancer cell line IA-LM at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_IA-LM.xls \
-s 10036 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: IST-MEL1 cells.'  \
-i 10037 \
--summary "Tang Proliferation/Mitosis: IST-MEL1 cells. Dose response of anti-mitotic compounds in human cancer cell line IST-MEL1 at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_IS-MEL1.xls \
-s 10037 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: KATO III cells.'  \
-i 10038 \
--summary "Tang Proliferation/Mitosis: KATO III cells. Dose response of anti-mitotic compounds in human cancer cell line KATO III at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_KATO-III.xls \
-s 10038 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: KYSE-140 cells.'  \
-i 10039 \
--summary "Tang Proliferation/Mitosis: KYSE-140 cells. Dose response of anti-mitotic compounds in human cancer cell line KYSE-140 at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_KYSE-140.xls \
-s 10039 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: KYSE-180 cells.'  \
-i 10040 \
--summary "Tang Proliferation/Mitosis: KYSE-180 cells. Dose response of anti-mitotic compounds in human cancer cell line KYSE-180 at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_KYSE-180.xls \
-s 10040 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: NCI-H1648 cells.'  \
-i 10041 \
--summary "Tang Proliferation/Mitosis: NCI-H1648 cells. Dose response of anti-mitotic compounds in human cancer cell line NCI-H1648 at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_NCI-H1648.xls \
-s 10041 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: NCI-H1703 cells.'  \
-i 10042 \
--summary "Tang Proliferation/Mitosis: NCI-H1703 cells. Dose response of anti-mitotic compounds in human cancer cell line NCI-H1703 at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_NCI-H1703.xls \
-s 10042 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: PC-9 cells.'  \
-i 10043 \
--summary "Tang Proliferation/Mitosis: PC-9 cells. Dose response of anti-mitotic compounds in human cancer cell line PC-9 at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_PC-9.xls \
-s 10043 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: SJCRH30 cells.'  \
-i 10044 \
--summary "Tang Proliferation/Mitosis: SJCRH30 cells. Dose response of anti-mitotic compounds in human cancer cell line SJCRH30 at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_SJCRH30.xls \
-s 10044 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: SK-LMS-1 cells.'  \
-i 10045 \
--summary "Tang Proliferation/Mitosis: SK-LMS-1 cells. Dose response of anti-mitotic compounds in human cancer cell line SK-LMS-1 at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_SK-LMS1.xls \
-s 10045 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: SK-MES cells.'  \
-i 10046 \
--summary "Tang Proliferation/Mitosis: SK-MES cells. Dose response of anti-mitotic compounds in human cancer cell line SK-MES at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_SK-MES.xls \
-s 10046 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: SNB75 cells.'  \
-i 10047 \
--summary "Tang Proliferation/Mitosis: SNB75 cells. Dose response of anti-mitotic compounds in human cancer cell line SNB75 at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_SNB75.xls \
-s 10047 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Proliferation/Mitosis: SNB75 cells.'  \
-i 10048 \
--summary "Tang Proliferation/Mitosis: SNB75 cells. Dose response of anti-mitotic compounds in human cancer cell line SNB75 at 24, 48 and 72 hours to determine effect on cell proliferation and mitosis." \
-p '1. On Day 1, seed ~2000-3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On Day 2, 3, and 4 (0, 24, and 48 hrs after pin transfer), add 3 uL of 100 uM EdU (diluted from 10 mM stock to growth medium) to each well.

4. On Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following to the plate to which EdU was added 24 hrs ago:
(A) Prepare 2ï‚´ fixation/permeabilization solution in PBS and pre-warm it in 37C water bath.
(B) Add 30uL of the pre-warmed fixation/permeabilization solution to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 3.7% and the final concentration of Triton-100 is 0.2%. Immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed and permeabilized. 
(C) Prepare an EdU reaction cocktail in H2O that has:
100mM TBS,
2mM CuSO4,
2uM Alexa488-azide, and
2mM ascorbic acid.
(Add individual components to the cocktail in the above order.)
(D) Add 50 uL of the above freshly prepared EdU reaction cocktail to each well.
(E) Incubate the plate at RT for 40-50 mins.
(F) Wash the plate three times with PBS. After washing, the residual volume in each well should be ~20uL.
(G) Add 20uL of anti-MPM2 primary antibody (Millipore #05-368, final dilution 1:500) in blocking buffer (4% BSA and 0.2% Triton-100 in PBS) to each well. Incubate for 1hr at RT.
(H) Wash the plate three times with PBS.
(I) Add 20uL of Goat-anti-mouse Alexa fluor 568 (Invitrogen A11004, final dilution 1:500) in blocking buffer to each well. Incubate for 1hr at RT.
(J) Wash the plate three times with PBS.
(K) Add 20uL of Hoechst (final concentration 1 ug/mL) in PBS to each well. Incubate for 30min at RT.
(L) Wash the plate. Seal with a plate seal. 
(M) Image the plates using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, and then it identifies bright spots in the Texas Red (MPM2) channel to score for mitotic cells. In a separate step, the program measures the average FITC (EdU) intensity for all the nuclei, and scores for actively proliferating cells (cells that are incorporating EdU) by applying an EdU threshold.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. EdU positive cells: The percentage of actively proliferating cells that have average FITC (EdU) intensity above the EdU threshold.
c. Mitotic cells: The percentage of cells that are MPM2-positive.
d. Non-mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the non-mitotic population.
e. Mitotic cells that are EdU-positive: The percentage of EdU-positive cells within the mitotic population. 

Reference for EdU labeling:
Salic A, Mitchison TJ. A chemical method for fast and sensitive detection of DNA synthesis in vivo. Proc Natl Acad Sci USA. 2008 Feb 19;105(7):2415-20.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_ProMitosis_SNB75.xls \
-s 10048 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: 647-V cells.'  \
-i 10049 \
--summary "Tang Mitosis/Apoptosis ver.II: 647-V cells. Dose response of anti-mitotic compounds in human cancer cell line 647-V at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ug/mL Hoechst33342 (Sigma B2261), 4uM LysoTracker-Red (Invitrogen L7528), and 2uM DEVD-NucView488 (Biotium 10403).
(B) Add 10uL of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ug/mL, LysoTracker-Red is 1uM, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40uL of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_647-V.xls \
-s 10049 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: 5637 cells.'  \
-i 10050 \
--summary "Tang Mitosis/Apoptosis ver.II: 5637 cells. Dose response of anti-mitotic compounds in human cancer cell line 5637 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ug/mL Hoechst33342 (Sigma B2261), 4uM LysoTracker-Red (Invitrogen L7528), and 2uM DEVD-NucView488 (Biotium 10403).
(B) Add 10uL of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ug/mL, LysoTracker-Red is 1uM, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40uL of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_5637.xls \
-s 10050 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: Ca9-22 cells.'  \
-i 10051 \
--summary "Tang Mitosis/Apoptosis ver.II: Ca9-22 cells. Dose response of anti-mitotic compounds in human cancer cell line Ca9-22 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ug/mL Hoechst33342 (Sigma B2261), 4uM LysoTracker-Red (Invitrogen L7528), and 2uM DEVD-NucView488 (Biotium 10403).
(B) Add 10uL of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ug/mL, LysoTracker-Red is 1uM, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40uL of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_Ca9-22.xls \
-s 10051 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: CAL-51 cells.'  \
-i 10052 \
--summary "Tang Mitosis/Apoptosis ver.II: CAL-51 cells. Dose response of anti-mitotic compounds in human cancer cell line CAL-51 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ug/mL Hoechst33342 (Sigma B2261), 4uM LysoTracker-Red (Invitrogen L7528), and 2uM DEVD-NucView488 (Biotium 10403).
(B) Add 10uL of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ug/mL, LysoTracker-Red is 1uM, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40uL of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_Cal51.xls \
-s 10052 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: Calu-1 cells.'  \
-i 10053 \
--summary "Tang Mitosis/Apoptosis ver.II: Calu-1 cells. Dose response of anti-mitotic compounds in human cancer cell line Calu-1 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ug/mL Hoechst33342 (Sigma B2261), 4uM LysoTracker-Red (Invitrogen L7528), and 2uM DEVD-NucView488 (Biotium 10403).
(B) Add 10uL of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ug/mL, LysoTracker-Red is 1uM, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40uL of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_Calu-1.xls \
-s 10053 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: FU97 cells.'  \
-i 10054 \
--summary "Tang Mitosis/Apoptosis ver.II: FU97 cells. Dose response of anti-mitotic compounds in human cancer cell line FU97 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ug/mL Hoechst33342 (Sigma B2261), 4uM LysoTracker-Red (Invitrogen L7528), and 2uM DEVD-NucView488 (Biotium 10403).
(B) Add 10uL of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ug/mL, LysoTracker-Red is 1uM, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40uL of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_FU97.xls \
-s 10054 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: HLF cells.'  \
-i 10055 \
--summary "Tang Mitosis/Apoptosis ver.II: HLF cells. Dose response of anti-mitotic compounds in human cancer cell line HLF at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ug/mL Hoechst33342 (Sigma B2261), 4uM LysoTracker-Red (Invitrogen L7528), and 2uM DEVD-NucView488 (Biotium 10403).
(B) Add 10uL of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ug/mL, LysoTracker-Red is 1uM, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40uL of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_HLF.xls \
-s 10055 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: Ishikawa cells.'  \
-i 10056 \
--summary "Tang Mitosis/Apoptosis ver.II: Ishikawa cells. Dose response of anti-mitotic compounds in human cancer cell line Ishikawa at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ug/mL Hoechst33342 (Sigma B2261), 4uM LysoTracker-Red (Invitrogen L7528), and 2uM DEVD-NucView488 (Biotium 10403).
(B) Add 10uL of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ug/mL, LysoTracker-Red is 1uM, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40uL of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_Ishikawa.xls \
-s 10056 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: KMRC-20 cells.'  \
-i 10057 \
--summary "Tang Mitosis/Apoptosis ver.II: KMRC-20 cells. Dose response of anti-mitotic compounds in human cancer cell line KMRC-20 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ug/mL Hoechst33342 (Sigma B2261), 4uM LysoTracker-Red (Invitrogen L7528), and 2uM DEVD-NucView488 (Biotium 10403).
(B) Add 10uL of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ug/mL, LysoTracker-Red is 1uM, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40uL of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_KMRC-20.xls \
-s 10057 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: Kyse-140 cells.'  \
-i 10058 \
--summary "Tang Mitosis/Apoptosis ver.II: Kyse-140 cells. Dose response of anti-mitotic compounds in human cancer cell line Kyse-140 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ug/mL Hoechst33342 (Sigma B2261), 4uM LysoTracker-Red (Invitrogen L7528), and 2uM DEVD-NucView488 (Biotium 10403).
(B) Add 10uL of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ug/mL, LysoTracker-Red is 1uM, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40uL of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_Kyse-140.xls \
-s 10058 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: SW527 cells.'  \
-i 10059 \
--summary "Tang Mitosis/Apoptosis ver.II: SW527 cells. Dose response of anti-mitotic compounds in human cancer cell line SW527 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ug/mL Hoechst33342 (Sigma B2261), 4uM LysoTracker-Red (Invitrogen L7528), and 2uM DEVD-NucView488 (Biotium 10403).
(B) Add 10uL of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ug/mL, LysoTracker-Red is 1uM, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40uL of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_SW527.xls \
-s 10059 -i
check_errs $? "create screen result import fails"

./run.sh edu.harvard.med.screensaver.io.screens.ScreenCreator \
-AE $ECOMMONS_ADMIN  \
-st SMALL_MOLECULE  \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-t 'Tang Mitosis/Apoptosis ver.II: SW620 cells.'  \
-i 10060 \
--summary "Tang Mitosis/Apoptosis ver.II: SW620 cells. Dose response of anti-mitotic compounds in human cancer cell line SW620 at 24, 48 and 72 hours to determine effect on apoptosis, mitosis and cell death. 

In screening for small-molecule compounds that are effective at killing cancer cells, one-dimensional readout GI50, which is the EC50 value of growth inhibition, is usually used as the only criterion. A major problem with this one-readout approach is that other useful information is discarded, which could be critical for understanding the action of the compounds. In this screen, we use a single-cell-based imaging assay that can report multi-dimensional physiological responses in cells treated with small molecule kinase inhibitors." \
-p 'Protocol:
1. On Day 1, seed ~3000 cells in 30 uL of growth medium into each well of a 384-well clear-bottom black assay plate (Corning 3712), using a WellMate plate filler in a cell culture hood. 

2. On Day 2, pin transfer performed by an ICCB-Longwood Screening Facility staff member using an Epson robot system. The pin transfer adds 100nL of each diluted compound from the 384-well compound library plate to each well of the assay plate.

3. On each day of Day 3, 4, and 5 (24, 48, and 72 hrs after pin transfer), perform the following:
(A) Prepare a cocktail of reagents in PBS that has 4ug/mL Hoechst33342 (Sigma B2261), 4uM LysoTracker-Red (Invitrogen L7528), and 2uM DEVD-NucView488 (Biotium 10403).
(B) Add 10uL of the reagent cocktail to each well of the assay plate using benchtop WellMate plate filler, so that the final concentration of Hoechst33342 is 1ug/mL, LysoTracker-Red is 1uM, and DEVD-NucView488 is 500nM.
(C) Incubate cells in a tissue culture incubator at 37C, 5% CO2 for 1.5 hrs.
(D) Prepare 2% formaldehyde in PBS and pre-warm it in 37C water bath. 
(E) Add 40uL of the pre-warmed formaldehyde to each well of cells using benchtop WellMate, so that the final concentration of formaldehyde is 1%. Then immediately centrifuge the plates at 1000rpm at room temperature for 20 minutes, in a plate centrifuge, while the cells are being fixed. 
(F) After 20 mins of fixation and centrifugation, seal the plates with adhesive plate seals. 
(G) Image the plates, ideally within the same day, using the ImageXpress Micro screening microscope (Molecular Devices) and the 10x objective lens. Image 4 sites/per well.

Filter information:
DAPI [Excitation 377/50; Emission 447/60]
FITC [Excitation 482/35; Emission 536/40]
Texas Red [Excitation 562/40; Emission 624/40]

4. After image acquisition, image analysis is done using a customized Matlab program developed by Dr. Tiao Xie (Harvard Medical School). The program does segmentation on the DAPI channel to identify all nuclei, then it counts the bright, rounded cells in the Texas Red channel (LysoTracker-Red) to score mitotic cells. Finally it detects bright spots in the FITC channel (NucView) to score apoptotic cells. We also identify a population of late-stage dead cells with a â€œblurryâ€? DAPI morphology, and no NucView Signal or LysoTracker Red signal.

5. When reporting data, 5 parameters are reported for each replicate for each cell line and compound condition:
a. Cell Count: The total number of cells (nuclei) stained with Hoechst 33342 and detected in the DAPI channel.
b. Interphase cells: The total number of cells less the number of Apoptotic cells, Dead cells, and Mitotic cells.
c. Apoptotic cells: The cells stained with NucView.
d. Dead cells: The â€œlate-stageâ€? dead cells with blurry DAPI morphology that do not stain with either NucView or LysoTracker Red.
e. Mitotic cells: The cells that stain brightly with LysoTracker Red and that have a rounded morphology.

Reference for NucView:
Cen H, Mao F, Aronchik I, Fuentes RJ, Firestone GL. DEVD-NucView488: a novel class of enzyme substrates for real-time detection of caspase-3 activity in live cells. FASEB J. 2008 Jul;22(7):2243-52.' 
check_errs $? "create screen fails"

./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultImporter \
-AE $ECOMMONS_ADMIN \
-f $DATA_DIRECTORY/screen/tang_MitoApop2_SW620.xls \
-s 10060 -i
check_errs $? "create screen result import fails"

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

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/LINCS-001-compounds_selection_master_study_fields.xls \
-t 'LINCS Compound Targets and Concentrations'  \
-i 300001 \
--parseLincsSpecificFacilityID \
--summary "Information about kinase targets, bioactive concentrations, and relevant publications for the indicated kinase inhibitors.  This information was  provided by the lab of Nathanael Gray."
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10008_sorafenib_ambit.xls \
-t 'Sorafenib KINOMEscan'  \
-i 300002 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10017_HG-6-64-1_ambit.xls \
-t 'HG-6-64-1 KINOMEscan'  \
-i 300003 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10029_GW-5074_ambit.xls \
-t 'GW-5074 KINOMEscan'  \
-i 300004 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10046_SB590885_ambit.xls \
-t 'SB590885 KINOMEscan'  \
-i 300005 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10049_PLX-4720_ambit.xls \
-t 'PLX-4720 KINOMEscan'  \
-i 300006 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10050_AZ-628_ambit.xls \
-t 'AZ-628 KINOMEscan'  \
-i 300007 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10068_PLX-4032_ambit.xls \
-t 'PLX-4032 KINOMEscan'  \
-i 300008 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10006_AZD7762_ambit.xls \
-t 'AZD7762 KINOMEscan'  \
-i 300009 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10009_CP466722_ambit.xls \
-t 'CP466722 KINOMEscan'  \
-i 300010 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10010_CP724714_ambit.xls \
-t 'CP724714 KINOMEscan'  \
-i 300011 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10012_GSK429286A_ambit.xls \
-t 'GSK429286A KINOMEscan'  \
-i 300012 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10013_GSK461364_ambit.xls \
-t 'GSK461364 KINOMEscan'  \
-i 300013 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10014_GW843682_ambit.xls \
-t 'GW843682 KINOMEscan'  \
-i 300014 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10027_PF02341066_ambit.xls \
-t 'PF02341066 KINOMEscan'  \
-i 300015 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10028_BMS345541_ambit.xls \
-t 'BMS345541 KINOMEscan'  \
-i 300016 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10034_AS601245_ambit.xls \
-t 'AS601245 KINOMEscan'  \
-i 300017 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10038_WH-4-023_ambit.xls \
-t 'WH-4-023 KINOMEscan'  \
-i 300018 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10055_BX-912_ambit.xls \
-t 'BX-912 KINOMEscan'  \
-i 300019 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10059_AZD-6482_ambit.xls \
-t 'AZD-6482 KINOMEscan'  \
-i 300020 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10060_TAK-715_ambit.xls \
-t 'TAK-715 KINOMEscan'  \
-i 300021 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10061_NU7441_ambit.xls \
-t 'NU7441 KINOMEscan'  \
-i 300022 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10065_KIN001-220_ambit.xls \
-t 'KIN001-220 KINOMEscan'  \
-i 300023 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10066_MLN8054_ambit.xls \
-t 'MLN8054 KINOMEscan'  \
-i 300024 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10067_AZD1152-HQPA_ambit.xls \
-t 'AZD1152-HQPA KINOMEscan'  \
-i 300025 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10071_PD0332991_ambit.xls \
-t 'PD0332991 KINOMEscan'  \
-i 300026 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10078_THZ-2-98-01_ambit.xls \
-t 'THZ-2-98-01 KINOMEscan'  \
-i 300027 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10092_JWE-035_ambit.xls \
-t 'JWE-035 KINOMEscan'  \
-i 300028 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10096_ZM-447439_ambit.xls \
-t 'ZM-447439 KINOMEscan'  \
-i 300029 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10100_JNK-9L_ambit.xls \
-t 'JNK-9L KINOMEscan'  \
-i 300030 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10008_sorafenib_kinativ.xls \
-t 'Sorafenib KiNativ'  \
-i 300031 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/kinativ_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10017_HG-6-64-01_kinativ.xls \
-t 'HG-6-64-01 KiNativ'  \
-i 300032 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/kinativ_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10029_GW-5074_kinativ.xls \
-t 'GW-5074 KiNativ'  \
-i 300033 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/kinativ_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10046_SB590885_kinativ.xls \
-t 'SB590885 KiNativ'  \
-i 300034 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/kinativ_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10049_PLX-4720_kinativ.xls \
-t 'PLX-4720 KiNativ'  \
-i 300035 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/kinativ_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10050_AZ-628_kinativ.xls \
-t 'AZ-628 KiNativ'  \
-i 300036 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/kinativ_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10068_PLX4032_kinativ.xls \
-t 'PLX4032 KiNativ'  \
-i 300037 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/kinativ_protocol.txt`"
check_errs $? "create study fails"

LAB_HEAD_FIRST="Cyril"
LAB_HEAD_LAST="Benes"
LAB_HEAD_EMAIL="cbenes@partners.org"
LEAD_SCREENER_FIRST="Cyril"
LEAD_SCREENER_LAST="Benes"
LEAD_SCREENER_EMAIL="cbenes@partners.org"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10013_template.xls \
-t 'GSK461364: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300038 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10020_template.xls \
-t 'Dasatinib: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300039 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10021_template.xls \
-t 'VX680: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300040 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10022_template.xls \
-t 'GNF2: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300041 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10023_template.xls \
-t 'Imatinib: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300042 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10024_template.xls \
-t 'NVP-TAE684: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300043 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10025_template.xls \
-t 'CGP60474: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300044 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10026_template.xls \
-t 'PD173074: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300045 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10027_template.xls \
-t 'PF02341066: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300046 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10032_template.xls \
-t 'AZD0530: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300047 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10038_template.xls \
-t 'WH-4-023: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300048 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10039_template.xls \
-t 'WH-4-025: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300049 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10041_template.xls \
-t 'BI-2536: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300050 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10043_template.xls \
-t 'KIN001-127: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300051 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10045_template.xls \
-t 'A443644: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300052 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10050_template.xls \
-t 'AZ-628: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300053 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10051_template.xls \
-t 'GW-572016: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300054 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10079_template.xls \
-t 'Torin1: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300055 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10107_template.xls \
-t 'MG-132: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300056 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10108_template.xls \
-t 'Geldanamycin: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300057 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10052_template.xls \
-t 'Rapamycin: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300058 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10001_template.xls \
-t 'Roscovitine: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300059 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10008_template.xls \
-t 'BAY-439006: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300060 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10097_template.xls \
-t 'OSI-774: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300061 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10093_template.xls \
-t 'XMD8-85: MGH/Sanger Institute growth inhibition data (3 dose)'  \
-i 300062 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/cmt_protocol.txt`"
check_errs $? "create study fails"

LAB_HEAD_FIRST="Nathanael"
LAB_HEAD_LAST="Gray"
LAB_HEAD_EMAIL="nathanael_gray@dfci.harvard.edu"
LEAD_SCREENER_FIRST="Qingsong"
LEAD_SCREENER_LAST="Liu"
LEAD_SCREENER_EMAIL="qingsong_liu@hms.harvard.edu"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10002_ALW-II-38-3_kinomescan.xls \
-t 'ALW-II-38-3 KINOMEscan'  \
-i 300063 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10003_ALW-II-49-7_kinomescan.xls \
-t 'ALW-II-49-7 KINOMEscan'  \
-i 300064 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10015_HG-5-113-01_kinomescan.xls \
-t 'HG-5-113-01 KINOMEscan'  \
-i 300065 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10016_HG-5-88-01_kinomescan.xls \
-t 'HG-5-88-01 KINOMEscan'  \
-i 300066 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10018_HKI-272_kinomescan.xls \
-t 'HKI-272 KINOMEscan'  \
-i 300067 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10022_GNF2_kinomescan.xls \
-t 'GNF2 KINOMEscan'  \
-i 300068 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10025_CGP60474_kinomescan.xls \
-t 'CGP60474 KINOMEscan'  \
-i 300069 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10026_PD173074_kinomescan.xls \
-t 'PD173074 KINOMEscan'  \
-i 300070 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10039_WH-4-025_kinomescan.xls \
-t 'WH-4-025 KINOMEscan'  \
-i 300071 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10041_BI-2536_kinomescan.xls \
-t 'BI-2536 KINOMEscan'  \
-i 300072 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10043_KIN001-127_kinomescan.xls \
-t 'KIN001-127 KINOMEscan'  \
-i 300073 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10045_A443644_kinomescan.xls \
-t 'A443644 KINOMEscan'  \
-i 300074 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10047_GDC-0941_kinomescan.xls \
-t 'GDC-0941 KINOMEscan'  \
-i 300075 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10070_NPK76-II-72-1_kinomescan.xls \
-t 'NPK76-II-72-1 KINOMEscan'  \
-i 300076 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10075_QL-X-138_kinomescan.xls \
-t 'QL-X-138 KINOMEscan'  \
-i 300077 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10076_QL-XI-92_kinomescan.xls \
-t 'QL-XI-92 KINOMEscan'  \
-i 300078 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10079_Torin1_kinomescan.xls \
-t 'Torin1 KINOMEscan'  \
-i 300079 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10080_Torin2_kinomescan.xls \
-t 'Torin2 KINOMEscan'  \
-i 300080 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10082_WZ-4-145_kinomescan.xls \
-t 'WZ-4-145 KINOMEscan'  \
-i 300081 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10083_WZ-7043_kinomescan.xls \
-t 'WZ-7043 KINOMEscan'  \
-i 300082 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10084_WZ3105_kinomescan.xls \
-t 'WZ3105 KINOMEscan'  \
-i 300083 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10085_WZ4002_kinomescan.xls \
-t 'WZ4002 KINOMEscan'  \
-i 300084 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10086_XMD11-50_kinomescan.xls \
-t 'XMD11-50 KINOMEscan'  \
-i 300085 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10087_XMD11-85h_kinomescan.xls \
-t 'XMD11-85h KINOMEscan'  \
-i 300086 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10088_XMD13-2_kinomescan.xls \
-t 'XMD13-2 KINOMEscan'  \
-i 300087 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10089_XMD14-99_kinomescan.xls \
-t 'XMD14-99 KINOMEscan'  \
-i 300088 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10090_XMD15-27_kinomescan.xls \
-t 'XMD15-27 KINOMEscan'  \
-i 300089 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10091_XMD16-144_kinomescan.xls \
-t 'XMD16-144 KINOMEscan'  \
-i 300090 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10093_XMD8-85_kinomescan.xls \
-t 'XMD8-85 KINOMEscan'  \
-i 300091 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10094_XMD8-92_kinomescan.xls \
-t 'XMD8-92 KINOMEscan'  \
-i 300092 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

./run.sh edu.harvard.med.screensaver.io.screens.StudyCreator \
-AE $ECOMMONS_ADMIN -annotationNamesInCol1  \
-y SMALL_MOLECULE -yy IN_VITRO \
-hf $LAB_HEAD_FIRST -hl $LAB_HEAD_LAST -he $LAB_HEAD_EMAIL -lf $LEAD_SCREENER_FIRST -ll $LEAD_SCREENER_LAST -le $LEAD_SCREENER_EMAIL \
-keyByFacilityId \
--replace -f $DATA_DIRECTORY/study/HMSL10095_ZG-10_kinomescan.xls \
-t 'ZG-10 KINOMEscan'  \
-i 300093 \
--parseLincsSpecificFacilityID \
--summary "`cat $DATA_DIRECTORY/study/ambit_protocol.txt`"
check_errs $? "create study fails"

## [#3110] Track data received date, data publicized date for compounds, studies, screens

psql -q -U $DB_USER $DB -f $DATA_DIRECTORY/adjust_dates_received.sql -v ON_ERROR_STOP=1
check_errs $? "drop_all.sh fails"

## Reagent QC Attachments
./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10001.101.01.pdf -i HMSL10001 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10001.101.01.pdf -i HMSL10001 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10004.101.01.pdf -i HMSL10004 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10004.101.01.pdf -i HMSL10004 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10005.101.01.pdf -i HMSL10005 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10005.101.01.pdf -i HMSL10005 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10006.101.01.pdf -i HMSL10006 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10006.101.01.pdf -i HMSL10006 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10010.101.01.pdf -i HMSL10010 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10010.101.01.pdf -i HMSL10010 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10011.101.01.pdf -i HMSL10011 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10011.101.01.pdf -i HMSL10011 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10012.101.01.pdf -i HMSL10012 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10012.101.01.pdf -i HMSL10012 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10013.101.01.pdf -i HMSL10013 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10013.101.01.pdf -i HMSL10013 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10014.101.01.pdf -i HMSL10014 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10014.101.01.pdf -i HMSL10014 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10015.101.01.pdf -i HMSL10015 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10015.101.01.pdf -i HMSL10015 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10016.101.01.pdf -i HMSL10016 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10016.101.01.pdf -i HMSL10016 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10018.101.01.pdf -i HMSL10018 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10018.101.01.pdf -i HMSL10018 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10020.101.01.pdf -i HMSL10020 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10020.101.01.pdf -i HMSL10020 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10021.101.01.pdf -i HMSL10021 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10021.101.01.pdf -i HMSL10021 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10023.103.01.pdf -i HMSL10023 -sid 103 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10023.103.01.pdf -i HMSL10023 -sid 103 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10024.101.01.pdf -i HMSL10024 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10024.101.01.pdf -i HMSL10024 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10024.101.01.pdf -i HMSL10024 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10029.101.01.pdf -i HMSL10029 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10029.101.01.pdf -i HMSL10029 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10032.101.01.pdf -i HMSL10032 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10032.101.01.pdf -i HMSL10032 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10034.101.01.pdf -i HMSL10034 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10034.101.01.pdf -i HMSL10034 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10035.101.01.pdf -i HMSL10035 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10035.101.01.pdf -i HMSL10035 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10036.101.01.pdf -i HMSL10036 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10036.101.01.pdf -i HMSL10036 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10037.101.01.pdf -i HMSL10037 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10037.101.01.pdf -i HMSL10037 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10038.101.01.pdf -i HMSL10038 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10038.101.01.pdf -i HMSL10038 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10039.101.01.pdf -i HMSL10039 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10039.101.01.pdf -i HMSL10039 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10040.101.01.pdf -i HMSL10040 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10041.101.01.pdf -i HMSL10041 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10041.101.01.pdf -i HMSL10041 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10042.101.01.pdf -i HMSL10042 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10042.101.01.pdf -i HMSL10042 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10043.101.01.pdf -i HMSL10043 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10043.101.01.pdf -i HMSL10043 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10043.101.01.pdf -i HMSL10043 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10046.101.01.pdf -i HMSL10046 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10046.101.01.pdf -i HMSL10046 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10048.101.01.pdf -i HMSL10048 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10048.101.01.pdf -i HMSL10048 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10050.101.01.pdf -i HMSL10050 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10050.101.01.pdf -i HMSL10050 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10051.104.01.pdf -i HMSL10051 -sid 104 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10051.104.01.pdf -i HMSL10051 -sid 104 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10051.104.01.pdf -i HMSL10051 -sid 104 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10052.101.01.pdf -i HMSL10052 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10053.101.01.pdf -i HMSL10053 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10053.101.01.pdf -i HMSL10053 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10053.101.01.pdf -i HMSL10053 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10054.101.01.pdf -i HMSL10054 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10054.101.01.pdf -i HMSL10054 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10054.101.01.pdf -i HMSL10054 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10055.101.01.pdf -i HMSL10055 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10055.101.01.pdf -i HMSL10055 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10056.101.01.pdf -i HMSL10056 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10056.101.01.pdf -i HMSL10056 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10056.101.01.pdf -i HMSL10056 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10057.102.01.pdf -i HMSL10057 -sid 102 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10057.102.01.pdf -i HMSL10057 -sid 102 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10059.101.01.pdf -i HMSL10059 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10059.101.01.pdf -i HMSL10059 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10059.101.01.pdf -i HMSL10059 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10060.101.01.pdf -i HMSL10060 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10060.101.01.pdf -i HMSL10060 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10061.101.01.pdf -i HMSL10061 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10061.101.01.pdf -i HMSL10061 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10062.101.01.pdf -i HMSL10062 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10062.101.01.pdf -i HMSL10062 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10062.101.01.pdf -i HMSL10062 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10064.101.01.pdf -i HMSL10064 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10064.101.01.pdf -i HMSL10064 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10064.101.01.pdf -i HMSL10064 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10066.101.01.pdf -i HMSL10066 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10066.101.01.pdf -i HMSL10066 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10066.101.01.pdf -i HMSL10066 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10068.101.01.pdf -i HMSL10068 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10068.101.01.pdf -i HMSL10068 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10069.101.01.pdf -i HMSL10069 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10069.101.01.pdf -i HMSL10069 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10070.101.01.pdf -i HMSL10070 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10070.101.01.pdf -i HMSL10070 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10071.101.01.pdf -i HMSL10071 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10071.101.01.pdf -i HMSL10071 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10072.101.01.pdf -i HMSL10072 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10072.101.01.pdf -i HMSL10072 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10073.101.01.pdf -i HMSL10073 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10073.101.01.pdf -i HMSL10073 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10074.101.01.pdf -i HMSL10074 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10074.101.01.pdf -i HMSL10074 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10074.101.01.pdf -i HMSL10074 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10078.101.01.pdf -i HMSL10078 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10078.101.01.pdf -i HMSL10078 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10082.101.01.pdf -i HMSL10082 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10082.101.01.pdf -i HMSL10082 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10083.101.01.pdf -i HMSL10083 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10083.101.01.pdf -i HMSL10083 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10086.101.01.pdf -i HMSL10086 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10086.101.01.pdf -i HMSL10086 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10088.101.01.pdf -i HMSL10088 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10088.101.01.pdf -i HMSL10088 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10089.101.01.pdf -i HMSL10089 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10089.101.01.pdf -i HMSL10089 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10090.101.01.pdf -i HMSL10090 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10090.101.01.pdf -i HMSL10090 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10091.101.01.pdf -i HMSL10091 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10091.101.01.pdf -i HMSL10091 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10092.101.01.pdf -i HMSL10092 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10092.101.01.pdf -i HMSL10092 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10093.101.01.pdf -i HMSL10093 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10093.101.01.pdf -i HMSL10093 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10094.101.01.pdf -i HMSL10094 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10094.101.01.pdf -i HMSL10094 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10095.101.01.pdf -i HMSL10095 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10095.101.01.pdf -i HMSL10095 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10096.101.01.pdf -i HMSL10096 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10096.101.01.pdf -i HMSL10096 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10096.101.01.pdf -i HMSL10096 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10097.101.01.pdf -i HMSL10097 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10097.101.01.pdf -i HMSL10097 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10098.101.01.pdf -i HMSL10098 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10098.101.01.pdf -i HMSL10098 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10101.101.01.pdf -i HMSL10101 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10101.101.01.pdf -i HMSL10101 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10101.101.01.pdf -i HMSL10101 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10116.101.01.pdf -i HMSL10116 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10116.101.01.pdf -i HMSL10116 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10117.101.01.pdf -i HMSL10117 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10117.101.01.pdf -i HMSL10117 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10117.101.01.pdf -i HMSL10117 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10118.101.01.pdf -i HMSL10118 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10118.101.01.pdf -i HMSL10118 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10119.101.01.pdf -i HMSL10119 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10119.101.01.pdf -i HMSL10119 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10120.102.01.pdf -i HMSL10120 -sid 102 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10120.102.01.pdf -i HMSL10120 -sid 102 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10121.101.01.pdf -i HMSL10121 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10121.101.01.pdf -i HMSL10121 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10122.101.01.pdf -i HMSL10122 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10122.101.01.pdf -i HMSL10122 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10123.101.01.pdf -i HMSL10123 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10123.101.01.pdf -i HMSL10123 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10124.101.01.pdf -i HMSL10124 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10124.101.01.pdf -i HMSL10124 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10125.101.01.pdf -i HMSL10125 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10125.101.01.pdf -i HMSL10125 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10126.101.01.pdf -i HMSL10126 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10126.101.01.pdf -i HMSL10126 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10127.101.01.pdf -i HMSL10127 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10127.101.01.pdf -i HMSL10127 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10128.101.01.pdf -i HMSL10128 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10128.101.01.pdf -i HMSL10128 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10129.101.01.pdf -i HMSL10129 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10129.101.01.pdf -i HMSL10129 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10130.101.01.pdf -i HMSL10130 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10130.101.01.pdf -i HMSL10130 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10130.101.01.pdf -i HMSL10130 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10131.101.01.pdf -i HMSL10131 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10131.101.01.pdf -i HMSL10131 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10131.101.01.pdf -i HMSL10131 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10132.101.01.pdf -i HMSL10132 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10132.101.01.pdf -i HMSL10132 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10133.101.01.pdf -i HMSL10133 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10133.101.01.pdf -i HMSL10133 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10134.101.01.pdf -i HMSL10134 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10134.101.01.pdf -i HMSL10134 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10134.101.01.pdf -i HMSL10134 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10135.101.01.pdf -i HMSL10135 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10135.101.01.pdf -i HMSL10135 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10135.101.01.pdf -i HMSL10135 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10136.101.01.pdf -i HMSL10136 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10136.101.01.pdf -i HMSL10136 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10136.101.01.pdf -i HMSL10136 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10137.101.01.pdf -i HMSL10137 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10137.101.01.pdf -i HMSL10137 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10138.105.01.pdf -i HMSL10138 -sid 105 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10138.105.01.pdf -i HMSL10138 -sid 105 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10138.105.01.pdf -i HMSL10138 -sid 105 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10139.101.01.pdf -i HMSL10139 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10139.101.01.pdf -i HMSL10139 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10139.101.01.pdf -i HMSL10139 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10140.101.01.pdf -i HMSL10140 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10140.101.01.pdf -i HMSL10140 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10140.101.01.pdf -i HMSL10140 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10141.101.01.pdf -i HMSL10141 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10141.101.01.pdf -i HMSL10141 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10142.101.01.pdf -i HMSL10142 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10142.101.01.pdf -i HMSL10142 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10142.101.01.pdf -i HMSL10142 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10143.101.01.pdf -i HMSL10143 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10143.101.01.pdf -i HMSL10143 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10144.101.01.pdf -i HMSL10144 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10144.101.01.pdf -i HMSL10144 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10145.102.01.pdf -i HMSL10145 -sid 102 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10145.102.01.pdf -i HMSL10145 -sid 102 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10145.102.01.pdf -i HMSL10145 -sid 102 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10146.101.01.pdf -i HMSL10146 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10146.101.01.pdf -i HMSL10146 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10146.101.01.pdf -i HMSL10146 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10147.101.01.pdf -i HMSL10147 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10147.101.01.pdf -i HMSL10147 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10147.101.01.pdf -i HMSL10147 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10148.101.01.pdf -i HMSL10148 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10148.101.01.pdf -i HMSL10148 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10148.101.01.pdf -i HMSL10148 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10149.102.01.pdf -i HMSL10149 -sid 102 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10149.102.01.pdf -i HMSL10149 -sid 102 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10149.102.01.pdf -i HMSL10149 -sid 102 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10150.101.01.pdf -i HMSL10150 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10150.101.01.pdf -i HMSL10150 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10150.101.01.pdf -i HMSL10150 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10151.101.01.pdf -i HMSL10151 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10151.101.01.pdf -i HMSL10151 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10151.101.01.pdf -i HMSL10151 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10152.101.01.pdf -i HMSL10152 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10152.101.01.pdf -i HMSL10152 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10152.101.01.pdf -i HMSL10152 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10153.101.01.pdf -i HMSL10153 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10153.101.01.pdf -i HMSL10153 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10153.101.01.pdf -i HMSL10153 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10154.101.01.pdf -i HMSL10154 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10154.101.01.pdf -i HMSL10154 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10155.101.01.pdf -i HMSL10155 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10155.101.01.pdf -i HMSL10155 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10156.101.01.pdf -i HMSL10156 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10156.101.01.pdf -i HMSL10156 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10157.101.01.pdf -i HMSL10157 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10157.101.01.pdf -i HMSL10157 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10158.101.01.pdf -i HMSL10158 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10158.101.01.pdf -i HMSL10158 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10159.101.01.pdf -i HMSL10159 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10159.101.01.pdf -i HMSL10159 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10160.101.01.pdf -i HMSL10160 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10160.101.01.pdf -i HMSL10160 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10161.101.01.pdf -i HMSL10161 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10161.101.01.pdf -i HMSL10161 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10162.101.01.pdf -i HMSL10162 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10162.101.01.pdf -i HMSL10162 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10163.101.01.pdf -i HMSL10163 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10163.101.01.pdf -i HMSL10163 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10164.101.01.pdf -i HMSL10164 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10164.101.01.pdf -i HMSL10164 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10165.101.01.pdf -i HMSL10165 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10165.101.01.pdf -i HMSL10165 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10166.101.01.pdf -i HMSL10166 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/LCMS_HMSL10166.101.01.pdf -i HMSL10166 -sid 101 -bid 1 -type QC-LCMS
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10166.101.01.pdf -i HMSL10166 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10167.101.01.pdf -i HMSL10167 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10167.101.01.pdf -i HMSL10167 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10168.101.01.pdf -i HMSL10168 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10168.101.01.pdf -i HMSL10168 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10169.101.01.pdf -i HMSL10169 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10169.101.01.pdf -i HMSL10169 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10170.101.01.pdf -i HMSL10170 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10170.101.01.pdf -i HMSL10170 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10171.101.01.pdf -i HMSL10171 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10171.101.01.pdf -i HMSL10171 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10172.101.01.pdf -i HMSL10172 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10172.101.01.pdf -i HMSL10172 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10173.101.01.pdf -i HMSL10173 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10173.101.01.pdf -i HMSL10173 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/NMR_HMSL10174.101.01.pdf -i HMSL10174 -sid 101 -bid 1 -type QC-NMR
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.libraries.ReagentAttachmentImporter \
-f $DATA_DIRECTORY/qc/HPLC_HMSL10174.101.01.pdf -i HMSL10174 -sid 101 -bid 1 -type QC-HPLC
check_errs $? "attachment import fails"

# "Study-File" Attached files, for viewing in the study viewer

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10013_GSK461364_CMT_Study300038.xls -i 300038
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10020_Dasatinib_CMT_Study300039.xls -i 300039
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10021_VX680_CMT_Study300040.xls -i 300040
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10022_GNF2_CMT_Study300041.xls -i 300041
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10023_Imatinib_CMT_Study300042.xls -i 300042
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10024_NVP-TAE684_CMT_Study300043.xls -i 300043
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10025_CGP60474_CMT_Study300044.xls -i 300044
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10026_PD173074_CMT_Study300045.xls -i 300045
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10027_PF02341066_CMT_Study300046.xls -i 300046
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10032_AZD0530_CMT_Study300047.xls -i 300047
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10038_WH-4-023_CMT_Study300048.xls -i 300048
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10039_WH-4-025_CMT_Study300049.xls -i 300049
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10041_BI-2536_CMT_Study300050.xls -i 300050
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10043_KIN001-127_CMT_Study300051.xls -i 300051
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10045_A443644_CMT_Study300052.xls -i 300052
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10050_AZ-628_CMT_Study300053.xls -i 300053
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10051_GW-572016_CMT_Study300054.xls -i 300054
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10079_Torin1_CMT_Study300055.xls -i 300055
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10107_MG-132_CMT_Study300056.xls -i 300056
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10108_Geldanamycin_CMT_Study300057.xls -i 300057
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10052_Rapamycin_CMT_Study300058.xls -i 300058
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10001_Roscovitine_CMT_Study300059.xls -i 300059
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10008_BAY-439006_CMT_Study300060.xls -i 300060
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10097_OSI-774_CMT_Study300061.xls -i 300061
check_errs $? "attachment import fails"

./run.sh edu.harvard.med.lincs.screensaver.io.screens.ScreenAttachmentImporter \
-f $DATA_DIRECTORY/study/HMSL10093_XMD8-85_CMT_Study300062.xls -i 300062
check_errs $? "attachment import fails"

# THIS SHOULD ALWAYS BE THE LAST COMMAND!
psql -q -U $DB_USER $DB -c analyze
