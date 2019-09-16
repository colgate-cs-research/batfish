#!/usr/bin/env bash

if [[ -z "${BATFISH_ROOT}" ]]; then
    echo "Run 'source tools/batfish_functions.sh' before running this script"
    exit 1
fi

NUM_WORKERS=1
LOG_LEVEL="WARN"
BATFISH_ARGS=""
START_PORT=9990

while getopts "hw:b:p:l:v" opt; do
    case "$opt" in
    h)  echo "run.sh [-w NUM_WORKERS] [-b BATFISH_ARGS] [-p START_PORT] [-l LOG_DIR] [-v]"
        exit 0
        ;;
    v)  LOG_LEVEL="DEBUG"
        ;;
    b)  BATFISH_ARGS="$OPTARG"
        ;;
    w)  NUM_WORKERS=$OPTARG
        ;;
    p)  START_PORT=$OPTARG
        ;;
    l) LOG_DIR=$OPTARG
        ;;
    esac
done

POOL_PORT=$((START_PORT+8))
WORK_PORT=$((START_PORT+7))
WORKV2_PORT=$((START_PORT+6))
SERVICE_PORT=$((START_PORT+10))

if [ -z $LOG_DIR ]; then
    TMPDIR=`mktemp -d`
else
    TMPDIR=$LOG_DIR
fi
echo "Storing logs in $TMPDIR"

echo "$BATFISH_ARGS" > "$TMPDIR/batfish_args"

# Start batfish workers
BATFISH_PIDS=()
for i in $(seq 1 $NUM_WORKERS); do
    BATFISH_LOG="$TMPDIR/batfish$i.log"
    BATFISH_PORT=$((i + SERVICE_PORT))
    batfish -runmode WORKSERVICE -register true -tracingenable false \
        -coordinatorpoolport $POOL_PORT -serviceport $BATFISH_PORT \
        -loglevel $LOG_LEVEL $BATFISH_ARGS 2>&1 > "$BATFISH_LOG" &
    BATFISH_PID=$!
    echo "Started Batfish worker in background: PID=$BATFISH_PID port=$BATFISH_PORT log=$BATFISH_LOG"
    BATFISH_PIDS+=($BATFISH_PID)
done

# Start coordinator
COORDINATOR_LOG="$TMPDIR/coordinator.log"
echo "Running coordinator in foreground: log=$COORDINATOR_LOG"
echo "Press Ctrl+C to kill workers and coordinator"
coordinator -templatedirs "${BATFISH_ROOT}/questions" -tracingenable false \
    -poolport $POOL_PORT -workport $WORK_PORT -workv2port $WORKV2_PORT \
    -loglevel $LOG_LEVEL -logfile "$COORDINATOR_LOG"
