#!/bin/bash

# Determine path to scripts
if [[ $(uname) == "Darwin" ]]; then
    SCRIPTPATH=`greadlink -f $0`
else
    SCRIPTPATH=`readlink -f $0`
fi
BASEDIR="`dirname $SCRIPTPATH`"
source $BASEDIR/../../tools/batfish_functions.sh
#BASEDIR=test_rigs/faultloc-examples
OPTIONS=$BASEDIR/options

\ls $BASEDIR | while read NETWORK; do
    if [ -d $BASEDIR/$NETWORK ]; then
        echo -e "init-container -setname $NETWORK" | allinone -runmode interactive || exit 1
        POLICIES=$BASEDIR/$NETWORK/original/policies/reach.cmd
        ORIG_CONFIGS=$BASEDIR/$NETWORK/original/configs
        \ls $BASEDIR/$NETWORK | while read SCENARIO; do
            echo -e "\n\n##### SCENARIO: $NETWORK/$SCENARIO ###################################\n"
            TESTRIG_DIR=$BASEDIR/$NETWORK/$SCENARIO

            SCENARIO_CONFIGS=$TESTRIG_DIR/configs
            echo -e "CONFIG DIFFERENCES"
            echo -e "====================================================="
            diff -ru $ORIG_CONFIGS $SCENARIO_CONFIGS
            echo -e "=====================================================\n"

            echo -e "CHECK POLICIES"
            echo -e "====================================================="
            echo -e "set-container $NETWORK\ninit-testrig $TESTRIG_DIR $SCENARIO\n`cat $OPTIONS $POLICIES`" | allinone -runmode interactive || exit 1
            echo -e "\n====================================================="
        done
    fi
done
