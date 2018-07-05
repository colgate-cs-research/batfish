import os
import csv
import argparse

parser = argparse.ArgumentParser(description='Aggregate experiment output')
parser.add_argument('-path', dest='path', action='store', required=True,
                   help='path containing experiment output')
args = parser.parse_args()

top_level_dir = os.path.join(args.path, "result")
masterpath = os.path.join(args.path, "master.csv")

# Open master file

f = open(masterpath,'w')
writer=csv.writer(f)
writer.writerow(["examples","foundpreds","unfoundpreds","extraconfigpred","extracomputepred","includecomputable","notnegating","minimize","slice","experiment","network","scenario"])
# List every experiment
experiments = os.listdir(top_level_dir)
for experiment in experiments:
    # Ignore hidden directories
    if experiment.startswith('.'):
        next
    experiment_dir = os.path.join(top_level_dir, experiment)


    # List every network
    networks = os.listdir(experiment_dir)
    for network in networks:
        # Ignore hidden directories
        if network.startswith('.'):
            next
        network_dir = os.path.join(experiment_dir, network)
    
        # List every scenario
        scenarios = os.listdir(os.path.join(network_dir, 'testrigs'))
        for scenario in scenarios:
            # Ignore hidden directories
            if scenario.startswith('.'):
                next
            try:
                scenario_file = os.path.join(network_dir, 'testrigs', scenario, 'testrig', 'experiment.csv')
                test = open(scenario_file,'r')
                readFile= csv.reader(test)
                for row in readFile:
                    if row[0].isdigit():
                        row.append(experiment)
                        row.append(network)
                        row.append(scenario)
                        writer.writerow(row)
  
                test.close()
            except OSError:
                pass
            # TODO: Open scenario file
            # TODO: For each line in scenario file
            # TODO: Add experiment, network, and scenario to line
            # TODO: Add line to master file

# Close master file
f.close()
