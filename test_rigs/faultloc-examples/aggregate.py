#!/usr/bin/env python3

import os
import csv
import argparse

def main():
    parser = argparse.ArgumentParser(description='Aggregate experiment output')
    parser.add_argument('-c','--containers', dest='path', action='store', 
            required=True, help='Path to containers directory')
    args = parser.parse_args()

    #top_level_dir = os.path.join(args.path, "result")
    masterpath = os.path.join(args.path, "master.csv")

    # Open master file
    masterfile = open(masterpath,'w')
    writer = csv.writer(masterfile)
    writer.writerow(["examples","foundpreds","unfoundpreds","extraconfigpred",
            "extracomputepred","includecomputable","notnegating","minimize",
            "slice","unfoundpred","slicetime","minimizationtime","checktime",
            "numMUSGenerated","musGenTimeElapsed","experiment","network",
            "scenario"])

    if os.path.exists(os.path.join(args.path, "network_ids")):
        process_experiment(None, args.path, writer)
    else:
        for experiment in os.listdir(args.path):
            if experiment.startswith('.'):
                continue
            process_experiment(experiment, os.path.join(args.path, 
                    experiment), writer)
    masterfile.close()

def process_experiment(experiment_name, base_dir, writer):
    # Process every network
    ids_dir = os.path.join(base_dir, "network_ids")
    for id_filename in os.listdir(ids_dir):
        with open(os.path.join(ids_dir, id_filename)) as id_file:
            network_id = id_file.read().strip()
        network_name = id_filename[:-3]
        process_network(experiment_name, network_name, 
                os.path.join(base_dir, network_id), writer)

def process_network(experiment_name, network_name, base_dir, writer):
    # Process every snapshot
    ids_dir = os.path.join(base_dir, "snapshot_ids")
    for id_filename in os.listdir(ids_dir):
        with open(os.path.join(ids_dir, id_filename)) as id_file:
            snapshot_id = id_file.read().strip()
        snapshot_name = id_filename[:-3]
        process_snapshot(experiment_name, network_name, snapshot_name,
                os.path.join(base_dir, "snapshots", snapshot_id), writer)

def process_snapshot(experiment_name, network_name, snapshot_name, base_dir,
        writer):
    filepath = os.path.join(base_dir, "output", "experiment.csv")
    try:
        with open(filepath, "r") as experiment_file:
            reader = csv.reader(experiment_file)
            for row in reader:
                if row[0].isdigit():
                    row.append(experiment_name)
                    row.append(network_name)
                    row.append(snapshot_name)
                    writer.writerow(row)
    except OSError:
        pass

if __name__ == "__main__":
    main()
