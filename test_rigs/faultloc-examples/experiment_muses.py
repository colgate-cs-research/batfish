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
    parser.add_argument('-nm', '--number of mus', dest='mus', action='store_true', help='numbers of muses to consider')
    parser.add_argument('-nf', '--number of failure', dest='failure', action='store_true', help='number of failures to consider')
    parser.add_argument('-np', '--number of policies', dest='policies', action='store_true', help='number of policies to consider')
    parser.add_argument('-pp', '--printPerPolicy', dest='print_per_policy', action='store_true', help='Print results per policy instead of per-scenario')
    args = parser.parse_args()

    identifier = "_"
    identifier += "i" if args.i_fail else "u"
    identifier += "i" if args.i_policy else "u"
    identifier += "i" if args.i_scenario else "u"
    identifier += "various_mus" if args.mus else ""
    identifier += "various_failure" if args.failure else ""
    identifier += "various_policies" if args.policies else ""

    masterfile_name = "master_mus" + identifier + ".csv"
    print(masterfile_name)
    #top_level_dir = os.path.join(args.path, "result")
    masterpath = os.path.join(args.path, masterfile_name)
    masterfile = open(masterpath, 'w')
    writer = csv.writer(masterfile)
    writer.writerow(["found_preds_count","missed_preds_count","extra_count","recall","precision","missed_preds", "extra_preds", "experiment","network", "scenario", "scenario_type", "num_mus", "num_failures", "num_policies"])

    # Process a single container directory or multiple container directories
    if os.path.exists(os.path.join(args.path, "network_ids")):
        process_experiment(args.path, writer, args)
    else:
        for experiment in sorted(os.listdir(args.path)):
            experiment_dir = os.path.join(args.path, experiment)
            if (experiment.startswith('.')
                    or not os.path.isdir(experiment_dir)):
                continue
            process_experiment(experiment, experiment_dir, writer, args)

    masterfile.close()

def process_experiment(experiment_name, experiment_dir, writer, args):
    print(experiment_name)
    ids_dir = os.path.join(experiment_dir, "network_ids")

    # Process each network
    for id_filename in sorted(os.listdir(ids_dir)):
        with open(os.path.join(ids_dir, id_filename)) as id_file:
            network_id = id_file.read().strip()
        network_name = id_filename[:-3] #stripping .id from ospf-triangle.id
        network_dir = os.path.join(experiment_dir, network_id)
        experiment_name = experiment_name.replace(network_name+'-', '')
        experiment_name = experiment_name.replace('netcomplete-', '')
        process_network(experiment_name, network_name, network_dir, writer, args)



def process_network(experiment_name, network, network_dir, writer, args):
    snapshot_ids_dir = os.path.join(network_dir, "snapshot_ids")
    if not os.path.exists(snapshot_ids_dir):
        return
    print(' ', network)

    # Process each snapshot
    for id_filename in sorted(os.listdir(snapshot_ids_dir)):
        with open(os.path.join(snapshot_ids_dir, id_filename)) as id_file:
            snapshot_id = id_file.read().strip()
        snapshot_name = id_filename[:-3]
        snapshot_dir = os.path.join(network_dir, "snapshots", snapshot_id)

        # Compute stats for all MUSes, failures, and policies
        if args.mus == args.failure == args.policies == False:
            nums = [0, 10**6]
        # Compute stats for varying numbers of MUSes, failures, or policies
        else:
            nums = [0,1,2,4,8,16,32,64]

        # Compute stats each maximum number of MUSes, failures, or policies
        for i, num in enumerate(nums):
            if i == 0:
                continue
            (faulty_preds, snapshot_candidates, num_mus, num_failures,
                    num_policies) = process_snapshot_num(experiment_name,
                        network, snapshot_name, snapshot_dir, writer, args,
                        num)
            found_count, missed_preds, extra_preds = analyze_candidates(
                    snapshot_candidates, faulty_preds)
            print('      found:', found_count, 'missed:', len(missed_preds),
                    'extra:', len(extra_preds))
            if len(faulty_preds) == 0:
                print('     extra:', '\n\t'.join(sorted(extra_preds)))
            if (not args.print_per_policy and
                    (args.mus == args.failure == args.policies == False
                        or (args.mus and num_mus >= nums[i-1])
                        or (args.failure and num_failures >= nums[i-1])
                        or (args.policies and num_policies >= nums[i-1]))):
                if (found_count == 0 and len(missed_preds) == 0):
                    recall = 'NA'
                else:
                    recall = found_count / (found_count + len(missed_preds))
                if (found_count == 0 and len(extra_preds) == 0):
                    precision = 'NA'
                else:
                    precision = found_count / (found_count + len(extra_preds))
                print('      precision:', precision, 'recall:', recall)
                writer.writerow(
                    [str(found_count),
                    str(len(missed_preds)),
                    str(len(extra_preds)),
                    str(recall),
                    str(precision),
                    str(';'.join(missed_preds)),
                    str(';'.join(extra_preds)),
                    experiment_name,
                    network,
                    snapshot_name,
                    '-'.join(snapshot_name.split('-')[:2]),
                    num_mus,
                    num_failures,
                    num_policies])


def process_snapshot_num(experiment_name, network, snapshot, snapshot_dir,
        writer, args, num=10**6):
    print('   ', snapshot)
    output_dir = os.path.join(snapshot_dir, "output")
    policy_to_candidates = dict()
    policy_to_failcount = dict()
    policy_to_muscount = dict()
    for filename in sorted(os.listdir(output_dir)):
        if filename.startswith('mus_bulk'):
            mus_path = os.path.join(output_dir, filename)
            policy, candidates, num_mus = (process_mus_intersect(mus_path, num)
                    if args.i_fail else process_mus(mus_path, num))
            print("      policy:", policy, "cands:", len(candidates),
                    "mus:", num_mus)

            # Limit number of failures considered, if requested
            if (not args.failure or policy_to_failcount.get(policy, 0) < num):
                policy_to_failcount[policy] = policy_to_failcount.get(policy, 0) + 1
            else:
                continue

            # Limit number of MUSes considered, if requested
            if (not args.policies or len(policy_to_candidates) < num):
                if policy not in policy_to_muscount:
                    policy_to_muscount[policy] = []
                policy_to_muscount[policy].append(num_mus)
                if policy not in policy_to_candidates:
                    policy_to_candidates[policy] = candidates
                else:
                    if args.i_policy:
                        policy_to_candidates[policy].intersection_update(candidates) #TODO: Check if dict updated
                    else:
                        policy_to_candidates[policy].update(candidates)

    faultloc_filepath = os.path.join(snapshot_dir, "input", "faultloc")
    faulty_preds = process_faultloc_file (faultloc_filepath)

    if (args.print_per_policy):
        for policy in policy_to_candidates.keys():
            policy_candidates = policy_to_candidates[policy]
            found_count, missed_preds, extra_preds = analyze_candidates(
                    policy_candidates, faulty_preds)
            num_mus = min(policy_to_muscount[policy])
            num_failures = policy_to_failcount[policy]
            if ((args.mus) or (args.failure and num_failures>=num)
                    or (not args.mus and not args.failure)):
                if (found_count == 0 and len(missed_preds) == 0):
                    recall = 'NA'
                else:
                    recall = found_count / (found_count + len(missed_preds))
                if (found_count == 0 and len(extra_preds) == 0):
                    precision = 'NA'
                else:
                    precision = found_count / (found_count + len(extra_preds))
                print('      precision:', precision, 'recall:', recall)
                writer.writerow(
                            [str(found_count),
                            str(len(missed_preds)),
                            str(len(extra_preds)),
                            str(recall),
                            str(precision),
                            str(missed_preds),
                            str(extra_preds),
                            experiment_name,
                            network,
                            snapshot,
                            '-'.join(snapshot_name.split('-')[:2]),
                            num_mus,
                            num_failures,
                            policy]
                        )
        return set(), set(), 0, 0, 0

    snapshot_candidates = set()
    first = True
    if not policy_to_candidates: # if no violations:
        no_faults = set()
        return no_faults, snapshot_candidates, 0, 0, 0

    for policy in policy_to_candidates:
        if first or (not args.i_scenario):
            first = False
            snapshot_candidates.update(policy_to_candidates[policy])
        else:
            snapshot_candidates.intersection_update(policy_to_candidates[policy])

        #min([min(mus_count) for mus_count in policy_to_muscount.values()])

    return (faulty_preds, snapshot_candidates,
            min([min(mus_count) for mus_count in policy_to_muscount.values()]),
            max(policy_to_failcount.values()), len(policy_to_candidates))

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

def process_mus(mus_file_path, num=10**6):
    preds= set()
    count = 0
    with open(mus_file_path, "r") as mus_file:
        for mus in mus_file:
            count = count+1
            if mus.startswith('['): #first line is policy being checked
                policy = mus.strip()
                continue
            preds_list =  mus.strip('\n,').split(',')
            for x in preds_list:
                preds.add(x)
            if count == (num+1):
                break
    return policy,preds,count-1

def analyze_candidates(candidates, faults):
    found_count = len(faults.intersection(candidates))
    missed_preds = faults.difference(candidates)
    assert(len(missed_preds) == (len(faults) - found_count))
    extra_preds = candidates.difference(faults)
    assert(len(extra_preds) == (len(candidates) - found_count))

    return found_count, missed_preds, extra_preds

def process_mus_intersect(mus_file_path, num=10**6):
    preds = set()
    first = True
    count = 0
    with open(mus_file_path, "r") as mus_file:
        for mus in mus_file:
            count = count+1
            if mus.startswith('['):
                policy = mus.strip()
                continue
            preds_set = set(mus.strip('\n,').split(','))
            if (first):
                preds = preds.union(preds_set)
                first = False
            else:
                preds = preds.intersection(preds_set)
            if count == (num+1):
                break

    return policy, preds, count-1

if __name__ == '__main__':
    main()
