#!/bin/bash

# Determine path to scripts
if [[ $(uname) == "Darwin" ]]; then
    SCRIPTPATH=`greadlink -f $0`
else
    SCRIPTPATH=`readlink -f $0`
fi
BASEDIR="`dirname $SCRIPTPATH`"

# Set paths to other code
MOFYDIR="$BASEDIR/../../../mofy/"
MOFYJARPATH="$MOFYDIR/projects/mofy/target/mofy-1.0-jar-with-dependencies.jar"
CONFIGTOOLSDIR="$BASEDIR/../../../config-tools"
BATFISHDIR="$BASEDIR/../../../batfish-faultloc"


NETWORK="colgate"
SNAPSHOT="2017-05-mod"

# Set results directory
OUTPUTDIR="$BASEDIR/../../output_end-to-end/$NETWORK/$SNAPSHOT"
mkdir -p $OUTPUTDIR
LOGDIR="$OUTPUTDIR/logs"
mkdir -p $LOGDIR
RESULTDIR="$OUTPUTDIR/result"
mkdir -p $RESULTDIR

# Configs paths
SNAPSHOTDIR="/shared/configs/colgate/bydate/$SNAPSHOT"
MODDIR="$OUTPUTDIR/mod-configs"

# Parameters for mofy
MODTYPE="Permit"
PERCENTAGE=100
SEED=500
SETUP="mod-${MODTYPE}_percent-${PERCENTAGE}_seed-${SEED}"
MODSNAPSHOTDIR="$MODDIR/$SETUP"
mkdir -p $MODSNAPSHOTDIR/configs

# Run mofy
java -jar $MOFYJARPATH -configs $SNAPSHOTDIR/configs \
    -outputDir $MODSNAPSHOTDIR/configs \
    -Modification $MODTYPE -Percentage $PERCENTAGE -seed $SEED
if [[ $? -ne 0 ]]; then
    echo "Failed to run mofy"
    exit 1
fi

#cp  $SNAPSHOTDIR/configs/* $MODSNAPSHOTDIR/configs/

# Parameters for batfish
POLICIESFILE="$SNAPSHOTDIR/policies.cmd"
CMDSFILE="/tmp/end-to-end.cmd"
echo "set-pretty-print false" > $CMDSFILE
echo "del-network $NETWORK" >> $CMDSFILE
echo "init-network -setname $NETWORK" >> $CMDSFILE
echo "init-snapshot $MODSNAPSHOTDIR $SNAPSHOT" >> $CMDSFILE
cat $POLICIESFILE >> $CMDSFILE
cat $CMDSFILE
CHECKRAWFILE="$MODSNAPSHOTDIR/check_raw.out"

# Run batfish
source $BATFISHDIR/tools/batfish_functions.sh
allinone -cmdfile $CMDSFILE | tee $CHECKRAWFILE

# Parameters for parseJSON
CHECKPARSEDFILE="$MODSNAPSHOTDIR/check_parsed.out"
$CONFIGTOOLSDIR/parseJSON.py -input $CHECKRAWFILE -generatecheck $CHECKPARSEDFILE

echo "ARE POLICIES VIOLATED?"
diff $CHECKPARSEDFILE $POLICIESFILE
