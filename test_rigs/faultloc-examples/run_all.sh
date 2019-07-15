#!/bin/bash

# Determine path to scripts
if [[ $(uname) == "Darwin" ]]; then
    SCRIPTPATH=`greadlink -f $0`
else
    SCRIPTPATH=`readlink -f $0`
fi
BASEDIR="`dirname $SCRIPTPATH`"

# Iterate over all networks in faultloc-examples
\ls $BASEDIR | while read NETWORK; do
    if [[ -d $BASEDIR/$NETWORK ]] && [[ -d $BASEDIR/$NETWORK/original ]]; then
        # Iterate over all scenarios for network
        \ls $BASEDIR/$NETWORK | grep -v "original" | while read SCENARIO; do
            # Run network and scenario
            #$BASEDIR/run_single.sh $NETWORK $SCENARIO
            echo $NETWORK $SCENARIO
        done
    fi
done
exit 1

$BASEDIR/aggregate.py -c containers

cat containers/master.csv
