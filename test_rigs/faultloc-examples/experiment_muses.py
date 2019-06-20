#!/usr/bin/env python3

import os
import csv
import argparse

#all options will result  in a csv containing a distinct entry for each (network, scenario) combination.
#scenario == snapshot
def main():
    parser = argparse.ArgumentParser(description='Aggregate experiment output')
    parser.add_argument('-c','--containers', dest='path', action='store',
            required=True, help='Path to containers directory')
    parser.add_argument('-f', '--iFailSet',dest='i_fail', action='store_true', help='Only consider predicates appearing in all muses of a failset')
    parser.add_argument('-p', '--iPolicy', dest='i_policy', action='store_true', help='Only consider predicates that are candidates from each failset')
    #regardless of whether failsets are intersected or unioned
    parser.add_argument('-s', '--iScenario', dest='i_scenario', action='store_true', help='Only consider predicates that are candidates for each policy')
    #Do not aggregate results to networks, instead have resulting candidates as faults for all scenarios. (same for all scenarios)
    args = parser.parse_args()

    identifier = "_"
    identifier += "i" if args.i_fail else "u"
    identifier += "i" if args.i_policy else "u"
    identifier += "i" if args.i_scenario else "u"

    masterfile_name = "master_mus" + identifier + ".csv"
    #top_level_dir = os.path.join(args.path, "result")
    masterpath = os.path.join(args.path, masterfile_name)

    masterfile = open(masterpath, 'w')
    writer = csv.writer(masterfile)
    writer.writerow(["found_preds_count","missed_preds_count","extra_count","missed_preds", "network", "scenario"])

    ids_dir = os.path.join(args.path, "network_ids")
    for id_filename in os.listdir(ids_dir):
        with open(os.path.join(ids_dir, id_filename)) as id_file:
            network_id = id_file.read().strip()
        network_name = id_filename[:-3] #stripping .id from ospf-triangle.id
        network_dir = os.path.join(args.path, network_id)
        process_network(network_name, network_dir, writer, args)

    masterfile.close()


def process_network(network, network_dir, writer, args):
    snapshot_ids_dir = os.path.join(network_dir, "snapshot_ids")
    for id_filename in os.listdir(snapshot_ids_dir):
        with open(os.path.join(snapshot_ids_dir, id_filename)) as id_file:
            snapshot_id = id_file.read().strip()
        snapshot_name = id_filename[:-3]
        snapshot_dir = os.path.join(network_dir, "snapshots", snapshot_id)
        faulty_preds, snapshot_candidates = process_snapshot(snapshot_dir, args)
        found_count, missed_count, missed_preds, extra_count = analyze_candidates(snapshot_candidates, faulty_preds)
        if found_count == 0 == missed_count:
            continue # No use writing out where there aren't any faults
        writer.writerow(
            [str(found_count),
            str(missed_count),
            str(extra_count),
            str(missed_preds),
            network,
            snapshot_name]
        )

def process_snapshot(snapshot_dir, args):
    output_dir = os.path.join(snapshot_dir, "output")
    policy_to_candidates = dict()
    for filename in os.listdir(output_dir):
        if filename.startswith('mus_bulk'):
            mus_path = os.path.join(output_dir, filename)
            policy, candidates = process_mus_intersect(mus_path) if args.i_fail else process_mus(mus_path)
            if policy not in policy_to_candidates:
                policy_to_candidates[policy] = candidates
            else:
                if args.i_policy:
                    policy_to_candidates[policy].intersection_update(candidates) #TODO: Check if dict updated
                else:
                    policy_to_candidates[policy].update(candidates)

    snapshot_candidates = set()
    first = True
    if not policy_to_candidates: # if no violations:
        no_faults = set()
        return no_faults, snapshot_candidates

    for policy in policy_to_candidates:
        if first or (not args.i_scenario):
            first = False
            snapshot_candidates.update(policy_to_candidates[policy])
        else:
            snapshot_candidates.intersection_update(policy_to_candidates[policy])

    faultloc_filepath = os.path.join(snapshot_dir, "input", "faultloc")
    faulty_preds = process_faultloc_file (faultloc_filepath)

    return faulty_preds, snapshot_candidates

def process_faultloc_file(filepath):
    faulty_preds = set()
    try:
        with open(filepath,"r") as faultloc_file:
            for pred in faultloc_file:
                if " " in pred:
                    pred = pred.strip()
                    if pred.endswith("null"):
                        pred = ' '.join(pred.split(' ')[:-1])
                    faulty_preds.add(pred.strip())

    except OSError:
        print("NO FAULT LOC FILE ", filepath)
        pass
    return faulty_preds

def process_mus(mus_file_path):
    preds= set()

    with open(mus_file_path, "r") as mus_file:
        for mus in mus_file:
            if mus.startswith('['): #first line is policy being checked
                policy = mus
                continue
            preds_list =  mus.split(',')
            for x in preds_list:
                preds.add(x)
    return policy,preds

def analyze_candidates(candidates, faults):
    found_count = len(faults.intersection(candidates))
    missed_count = len(faults) - found_count
    missed_preds = faults.difference(candidates)
    extra_count = len(candidates) - found_count

    return found_count, missed_count, missed_preds, extra_count


def process_mus_intersect(mus_file_path):
    preds = set()
    first = True
    with open(mus_file_path, "r") as mus_file:
        for mus in mus_file:
            if mus.startswith('['):
                policy = mus
                continue
            preds_set = set(mus.split(','))
            if (first):
                preds = preds.union(preds_set)
                first = False
            else:
                preds = preds.intersection(preds_set)

    return policy, preds

if __name__ == '__main__':
    main()
