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
    if [ -d $BASEDIR/$NETWORK ]; then
        # Iterate over all scenarios for network
        \ls $BASEDIR/$NETWORK | grep -v "original" | while read SCENARIO; do
            # Run network and scenario
            $BASEDIR/run_single.sh $NETWORK $SCENARIO
        done
    fi
done

$BASEDIR/aggregate.py -c containers

cat containers/master.csv
