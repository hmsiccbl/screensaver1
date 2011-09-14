#!/bin/bash
# runs the batch jobs for the Screensaver ICCBL databases

DIR=/groups/screensaver/batch/screensaver/

export JAVA_HOME=/usr/lib/jvm/java-6-sun/

if [[ $# -lt 1 ]]
then
  echo "Usage: $0 [local || dev || stage || prod ] "
  exit $WRONG_ARGS
fi

SERVER=$1
shift 1

if [[ "$SERVER" == "PROD" ]] || [[ "$SERVER" == "prod" ]]
then
  export SCREENSAVER_PROPERTIES_FILE=/groups/screensaver/screensaver-configs/screensaver.properties.batch.prod
elif [[ "$SERVER" == "STAGE" ]] || [[ "$SERVER" == "stage" ]]
then
  export SCREENSAVER_PROPERTIES_FILE=/groups/screensaver/screensaver-configs/screensaver.properties.batch.stage
elif [[ "$SERVER" == "DEV" ]] || [[ "$SERVER" == "dev" ]]
then
  export SCREENSAVER_PROPERTIES_FILE=/groups/screensaver/screensaver-configs/screensaver.properties.batch.dev
elif [[ "$SERVER" == "LOCAL" ]] || [[ "$SERVER" == "local" ]]
then
  export SCREENSAVER_PROPERTIES_FILE=/home/sde4/workspace/current/screensaver.properties.batch
else
  echo "Unknown option: \"$SERVER\""
  exit 1
fi

cd $DIR
$DIR/bjobs.sh bsub -u sean.erickson.hms@gmail.com -Is -q shared_int_12h $DIR/run.sh "$@"
