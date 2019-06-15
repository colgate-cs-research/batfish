#!/usr/bin/env bash

if [[ -z "${BATFISH_ROOT}" ]]; then
    echo "Run 'source tools/batfish_functions.sh' before running this script"
    exit 1
fi

NUM_WORKERS=1
LOG_LEVEL="WARN"

while getopts "hw:" opt; do
    case "$opt" in
    h)  echo "run.sh [-w NUM_WORKERS] [-v]"
        exit 0
        ;;
    v)  LOG_LEVEL="DEBUG"
        ;;
    w)  NUM_WORKERS=$OPTARG
        ;;
    esac
done

TMPDIR=`mktemp -d`
echo "Storing logs in $TMPDIR"

# Start batfish workers
BATFISH_PIDS=()
SERVICE_PORT=10000
for i in $(seq 1 $NUM_WORKERS); do
    BATFISH_LOG="$TMPDIR/batfish$i.log"
    BATFISH_PORT=$((i + SERVICE_PORT))
    batfish -runmode WORKSERVICE -register true -coordinatorpoolport 9998 \
        -tracingenable false -serviceport $BATFISH_PORT \
        -loglevel $LOG_LEVEL 2>&1 > "$BATFISH_LOG" &
    BATFISH_PID=$!
    echo "Started Batfish worker in background: PID=$BATFISH_PID port=$BATFISH_PORT log=$BATFISH_LOG"
    BATFISH_PIDS+=($BATFISH_PID)
done

# Start coordinator
COORDINATOR_LOG="$TMPDIR/coordinator.log"
echo "Running coordinator in foreground: log=$COORDINATOR_LOG"
echo "Press Ctrl+C to kill workers and coordinator"
coordinator -templatedirs "${BATFISH_ROOT}/questions" -tracingenable false \
    -loglevel $LOG_LEVEL -logfile "$COORDINATOR_LOG"
