#!/bin/bash

# Determine path to scripts
if [[ $(uname) == "Darwin" ]]; then
    SCRIPTPATH=`greadlink -f $0`
else
    SCRIPTPATH=`readlink -f $0`
fi
BASEDIR="`dirname $SCRIPTPATH`"

# Set results directory
OUTPUTDIR="$BASEDIR/../../output"
mkdir -p $OUTPUTDIR
LOGDIR="$OUTPUTDIR/logs"
mkdir -p $LOGDIR
RESULTDIR="$OUTPUTDIR/result"
mkdir -p $RESULTDIR

# Configure experimental setups
declare -A OPTIONS=(["c"]="includeComputable" \
                    ["m"]="minimizeUnsatCore" \
                    ["s"]="enableSlicing")
declare -a SETUPS=("" "c" "m" "s" "cm" "cs" "sm" "csm")

# Run experiment for each setup
for SETUP in "${SETUPS[@]}"; do 
    echo $SETUP

    # Prepare options file
    echo "add-batfish-options numIters 20" > $BASEDIR/custom-options
    for (( i=0; i<${#SETUP}; i++ )); do
        CHAR=${SETUP:$i:1}
        OPTION=${OPTIONS[$CHAR]}
        echo "add-batfish-options $OPTION" >> $BASEDIR/custom-options
    done

    if [ $SETUP=="" ]; then
        SETUP="noop"
    fi

    # Run experiment
    $BASEDIR/run.sh | tee $LOGDIR/$SETUP.log

    # Save output
    mv $BASEDIR/containers $RESULTDIR/$SETUP
done

# Aggregate results
python3 $BASEDIR/aggregate.py $OUTPUTDIR

# Clean up
rm $BASEDIR/custom-options
