#!/bin/bash

BASEDIR=test_rigs/faultloc-examples
OPTIONS=$BASEDIR/options

\ls $BASEDIR | while read NETWORK; do
    if [ -d $BASEDIR/$NETWORK ]; then
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
            cat $OPTIONS $POLICIES | allinone -runmode interactive -testrigdir $TESTRIG_DIR || exit 1
            echo -e "\n====================================================="
        done
    fi
done
