#!/bin/bash

#Determine path to scripts
if [[ $(uname) == "Darwin" ]]; then
    SCRIPTPATH=`greadlink -f $0`
else
    SCRIPTPATH=`readlink -f $0`
fi
BASEDIR="`dirname $SCRIPTPATH`"

# Set results directory
OUTPUTDIR="$BASEDIR/../../marco_output"
mkdir -p $OUTPUTDIR
LOGDIR="$OUTPUTDIR/logs"
mkdir -p $LOGDIR
RESULTDIR="$OUTPUTDIR/result"
mkdir -p $RESULTDIR

# Configure experimental setups
declare -A OPTIONS=(["i"]="musIntersect" \
                    ["u"]="musUnion" \
                    ["n"]="noNegateProperty")
declare -a SETUPS=("i" "in" "u" "un")

#MAX_MUS_COUNTS=(25 50 75 100)
#MAX_MUS_COUNTS=(1 2 4 8)
MAX_MUS_COUNTS=(1)
# Clean-up from last experiment
rm -rf $BASEDIR/containers

#Running all setups
for i in "${MAX_MUS_COUNTS[@]}"; do
    for SETUP in "${SETUPS[@]}"; do

        # Prepare options file
		echo "add-batfish-option useMarco mss" > $BASEDIR/custom-options
        echo "add-batfish-option numIters 20" >> $BASEDIR/custom-options
        #echo "add-batfish-option mus" $i >>$BASEDIR/custom-options
        for (( j=0; j<${#SETUP}; j++ )); do
            CHAR=${SETUP:$j:1}
            OPTION=${OPTIONS[$CHAR]}
            echo "add-batfish-option $OPTION" >> $BASEDIR/custom-options
        done
        #TODO: Find out what purpose this serves.
        if [ -z $SETUP ]; then
            SETUP="noop"
        fi
		SETUP="$SETUP$i"
        # Run experiment
        $BASEDIR/run_all.sh 2>&1 | tee $LOGDIR/$SETUP.log
	    
		# Save output
    	mkdir -p $RESULTDIR/$SETUP
    	cp -r $BASEDIR/containers/* $RESULTDIR/$SETUP || exit 1
    	rm -rf $BASEDIR/containers

    done
done

#Aggregate results
python3 $BASEDIR/aggregate.py -path $OUTPUTDIR

#Render graphs
cd $OUTPUTDIR
Rscript $BASEDIR/generategraph.R
cd $BASEDIR

#Clean up

rm $BASEDIR/custom-options
