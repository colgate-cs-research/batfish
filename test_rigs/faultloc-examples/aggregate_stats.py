#!/usr/bin/env python3

import os
import csv
import argparse
import json
import subprocess

def main():
    parser = argparse.ArgumentParser(description='Aggregate experiment output')
    parser.add_argument('-c','--containers', dest='path', action='store',
            required=True, help='Path to containers directory')
    args = parser.parse_args()

    #top_level_dir = os.path.join(args.path, "result")
    masterpath = os.path.join(args.path, "master_stats.csv")

    masterfile = open(masterpath, 'w')
    writer = csv.writer(masterfile)
    writer.writerow(["encodingTime","solverTime","numConstraints","configLines","routers","experiment","network","scenario"])

    if os.path.exists(os.path.join(args.path, "network_ids")):
        process_experiment(None, args.path, writer)
    else:
        for experiment in sorted(os.listdir(args.path)):
            if experiment.startswith('.'):
                continue
            process_experiment(experiment, os.path.join(args.path,
                    experiment), writer)

    masterfile.close()


def process_experiment(experiment_name, base_dir, writer):
    # Process every network
    ids_dir = os.path.join(base_dir, "network_ids")
    if os.path.exists(ids_dir):
        for id_filename in sorted(os.listdir(ids_dir)):
            with open(os.path.join(ids_dir, id_filename)) as id_file:
                network_id = id_file.read().strip()
            network_name = id_filename[:-3]
            experiment_name = experiment_name.replace(network_name+'-', '')
            experiment_name = experiment_name.replace('netcomplete-', '')
            process_network(experiment_name, network_name,
                    os.path.join(base_dir, network_id), writer)

def process_network(experiment_name, network_name, base_dir, writer):
    # Process every snapshot
    ids_dir = os.path.join(base_dir, "snapshot_ids")
    if os.path.exists(ids_dir):
        scenario_to_preds = dict()
        scenario_to_mus_time = dict()
        scenario_to_mus_count = dict()
        scenario_to_faulty_preds = dict()
        for id_filename in sorted(os.listdir(ids_dir)):
            with open(os.path.join(ids_dir, id_filename)) as id_file:
                snapshot_id = id_file.read().strip()
            snapshot_name = id_filename[:-3]
            process_snapshot(experiment_name, network_name, snapshot_name,
                    os.path.join(base_dir, "snapshots", snapshot_id), writer)

def process_snapshot(experiment_name, network_name, snapshot_name, base_dir, 
        writer):
    configs_dir = os.path.join(base_dir, "input", "configs")
    wc = 0
    routers = 0
    for config_file in sorted(os.listdir(configs_dir)):
        config_path = os.path.join(configs_dir, config_file)
        raw_wc = subprocess.check_output(["wc","-l",config_path],
                universal_newlines=True)
        wc += int(raw_wc.split(' ')[0])
        routers += 1

    output_dir = os.path.join(base_dir, "output")
    for out_filename in sorted(os.listdir(output_dir)):
        if out_filename.endswith('.json') and len(out_filename) == 41:
            with open(os.path.join(output_dir, out_filename), 'r') as out_file:
                raw_json = out_file.read()
            data = json.loads(raw_json)
            for answer in data['answerElements']:
                if 'result' in answer:
                    stats = answer['result']['statistics']
                    print(stats)
                    writer.writerow([
                        stats['avgEncodingTime'],
                        stats['avgSolverTime'],
                        stats['avgNumConstraints'],
                        wc,
                        routers,
                        experiment_name,
                        network_name,
                        snapshot_name
                        ])
    
if __name__ == "__main__":
    main()
