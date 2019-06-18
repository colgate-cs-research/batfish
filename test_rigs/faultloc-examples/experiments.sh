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
declare -A OPTIONS=(["i"]="musIntersect")
declare -a SETUPS=("" "i")
declare -a MUS_COUNTS =(1,10,100,1000)

# Clean-up from last experiment
rm -rf $BASEDIR/containers

# Run experiment for each setup
for SETUP in "${SETUPS[@]}"; do
    echo $SETUP
    for MUS_COUNT in "${MUS_COUNTS[@]}"; do

        # Prepare options file
        echo "add-batfish-option numIters 40" > $BASEDIR/custom-options
        echo "add-batfish-option saveMUS" >> $BASEDIR/custom-options
        echo "add-batfish-option mus $MUS_COUNT" >> $BASEDIR/custom-options
        for (( i=0; i<${#SETUP}; i++ )); do
            CHAR=${SETUP:$i:1}
            OPTION=${OPTIONS[$CHAR]}
            echo "add-batfish-option $OPTION" >> $BASEDIR/custom-options
        done

        if [ -z $SETUP ]; then
            SETUP="noop"
        fi
        echo $SETUP $MUS_COUNT

        # Run experiment
        $BASEDIR/run_all.sh 2>&1 | tee $LOGDIR/$SETUP-$MUS_COUNT.log

        # Save output
        mkdir -p $RESULTDIR/$SETUP
        cp -r $BASEDIR/containers/* $RESULTDIR/$SETUP || exit 1
        rm -rf $BASEDIR/containers
    done
done


# Render graphs
cd $OUTPUTDIR
Rscript $BASEDIR/generategraph.R
cd $BASEDIR

# Clean up
rm $BASEDIR/custom-options
