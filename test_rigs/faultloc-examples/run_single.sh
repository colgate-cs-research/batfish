#!/bin/bash

# Determine path to scripts
if [[ $(uname) == "Darwin" ]]; then
    SCRIPTPATH=`greadlink -f $0`
else
    SCRIPTPATH=`readlink -f $0`
fi
BASEDIR="`dirname $SCRIPTPATH`"

# Include dependencies
source $BASEDIR/../../tools/batfish_functions.sh

# Get arguments
if [ $# -ne 2 ]; then
    echo "Usage: run_single.sh NETWORK SCENARIO"
    exit 1
fi
NETWORK=$1
SCENARIO=$2

# Make sure network and scenario exists
if [ ! -d $BASEDIR/$NETWORK/$SCENARIO ]; then
    echo "$NETWORK/$SCENARIO does not exist"
    exit 1
fi

# Determine parameters
OPTIONS=$BASEDIR/options
if [ -f $BASEDIR/custom-options ]; then
    OPTIONS=$BASEDIR/custom-options
fi
POLICIES=$BASEDIR/$NETWORK/original/policies/reach.cmd
ORIG_CONFIGS=$BASEDIR/$NETWORK/original/configs
TESTRIG_DIR=$BASEDIR/$NETWORK/$SCENARIO
SCENARIO_CONFIGS=$TESTRIG_DIR/configs

# Function for running batfish commands
run_batfish_commands() {
    echo -e "$1" | allinone -runmode interactive || exit 1
}

# Create network, if necessary
if [ ! -f containers/network_ids/$NETWORK.id ]; then
    run_batfish_commands "init-network -setname $NETWORK"
fi
NETWORK_ID=`cat containers/network_ids/$NETWORK.id`

echo -e "\n\n##### SCENARIO: $NETWORK/$SCENARIO ###################################\n"

# Diff configs
echo -e "CONFIG DIFFERENCES"
echo -e "====================================================="
diff -ru $ORIG_CONFIGS $SCENARIO_CONFIGS
echo -e "=====================================================\n"

# Delete snapshot, if necessary
if [ -f containers/$NETWORK_ID/snapshot_ids/$SCENARIO.id ]; then
    run_batfish_commands "set-network $NETWORK\ndel-snapshot $SCENARIO"
fi

# Check policies
echo -e "CHECK POLICIES"
echo -e "====================================================="
run_batfish_commands "set-network $NETWORK\ninit-snapshot $TESTRIG_DIR $SCENARIO\n`cat $OPTIONS $POLICIES`"
echo -e "\n====================================================="