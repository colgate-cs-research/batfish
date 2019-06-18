#!/usr/bin/env python3

import os
import csv
import argparse

def main():
    parser = argparse.ArgumentParser(description='Aggregate experiment output')
    parser.add_argument('-c','--containers', dest='path', action='store',
            required=True, help='Path to containers directory')
    parser.add_argument('-i', '--intersect',dest='intersect', action='store_true', help='Only consider predicates appearing in all muses.')
    args = parser.parse_args()

    #top_level_dir = os.path.join(args.path, "result")
    masterpath = os.path.join(args.path, "master_mus.csv")

    masterfile = open(masterpath, 'w')
    writer = csv.writer(masterfile)
    writer.writerow(["found_preds_count","missed_preds_count","extra_count","missed_preds", "allMUSes_genTime","numMUSGenerated","network", "scenario"])

    if os.path.exists(os.path.join(args.path, "network_ids")):
        process_experiment(None, args.path, writer, args.intersect)
    else:
        for experiment in os.listdir(args.path):
            if experiment.startswith('.'):
                continue
            process_experiment(experiment, os.path.join(args.path,
                    experiment), writer, args.intersect)

    masterfile.close()


def process_experiment(experiment_name, base_dir, writer, should_intersect = False):
    # Process every network
    ids_dir = os.path.join(base_dir, "network_ids")
    if os.path.exists(ids_dir):
        for id_filename in os.listdir(ids_dir):
            with open(os.path.join(ids_dir, id_filename)) as id_file:
                network_id = id_file.read().strip()
            network_name = id_filename[:-3]
            process_network(experiment_name, network_name,
                    os.path.join(base_dir, network_id), writer, should_intersect)

def process_network(experiment_name, network_name, base_dir, writer, should_intersect):
    # Process every snapshot
    ids_dir = os.path.join(base_dir, "snapshot_ids")
    if os.path.exists(ids_dir):
        scenario_to_preds = dict()
        scenario_to_mus_time = dict()
        scenario_to_mus_count = dict()
        scenario_to_faulty_preds = dict()
        for id_filename in os.listdir(ids_dir):
            with open(os.path.join(ids_dir, id_filename)) as id_file:
                snapshot_id = id_file.read().strip()
            snapshot_name = id_filename[:-3]
            preds, gen_time, mus_count, faulty_preds = process_snapshot(experiment_name, network_name, snapshot_name,
                    os.path.join(base_dir, "snapshots", snapshot_id), should_intersect)
            if None == preds:
                continue
            if snapshot_name not in scenario_to_preds: #sufficient of all dicts
                scenario_to_preds[snapshot_name] = set()
                scenario_to_mus_time[snapshot_name] = 0
                scenario_to_mus_count[snapshot_name] = 0
            scenario_to_faulty_preds[snapshot_name] = faulty_preds
            for pred in preds:
                scenario_to_preds[snapshot_name].add(pred)
            scenario_to_mus_time[snapshot_name] += gen_time
            scenario_to_mus_count[snapshot_name] += mus_count

        for scenario in scenario_to_preds:
            #write one row for each scenario
            candidate_preds = scenario_to_preds[scenario]
            faulty_preds = scenario_to_faulty_preds[scenario]
            missed_preds = faulty_preds.difference(candidate_preds)
            missed_preds_count = len(missed_preds)
            found_preds_count = len(faulty_preds)- missed_preds_count
            extra_preds = candidate_preds.difference(faulty_preds)
            extra_count = len(extra_preds)

            writer.writerow([str(found_preds_count),
                        str(missed_preds_count),
                        str(extra_count),
                        str(missed_preds),
                        str(scenario_to_mus_time[scenario]),
                        str(scenario_to_mus_count[scenario]),
                        network_name,scenario])

def process_snapshot(experiment_name, network_name, snapshot_name, base_dir, should_intersect):
    exp_filepath = os.path.join(base_dir, "output", "experiment.csv")

    if os.path.exists(exp_filepath):
        all_preds = set()
        output_dir = os.path.join(base_dir, "output")
        for filename in os.listdir(output_dir):
            if filename.startswith('mus_bulk'):
                filepath = os.path.join(output_dir, filename)
                preds = process_mus_intersect(filepath) if should_intersect else process_mus(filepath)
                for pred in preds:
                    all_preds.add(pred)
        gentime = 0
        mus_count = 0
        with open(exp_filepath, "r") as experiment_file:
            reader = csv.reader(experiment_file)

            for row in reader:
                if row[0].isdigit():
                    gentime+=int(row[14])
                    mus_count+=int(row[15])
        faultloc_filepath = os.path.join(base_dir,"input","faultloc")
        faulty_preds = set()
        try:
            with open(faultloc_filepath,"r") as faultloc_file:
                for pred in faultloc_file:
                    if " " in pred:
                        pred = pred.strip()
                        if pred.endswith("null"):
                            pred = ' '.join(pred.split(' ')[:-1])
                        faulty_preds.add(pred.strip())

        except OSError:
            pass
        return all_preds, gentime, mus_count, faulty_preds
    return None, None, None, None
def process_mus(mus_file_path):
    preds= set()
    with open(mus_file_path, "r") as mus_file:
        for mus in mus_file:
            preds_list =  mus.split(',')
            for x in preds_list:
                preds.add(x)
    return preds

def process_mus_intersect(mus_file_path):
    preds = set()
    first = True
    with open(mus_file_path, "r") as mus_file:
        for mus in mus_file:
            preds_set = set(mus.split(','))
            if (first):
                preds = preds.union(preds_set)
                first = False
            else:
                preds = preds.intersection(preds_set)

    return preds
if __name__ == "__main__":
    main()
