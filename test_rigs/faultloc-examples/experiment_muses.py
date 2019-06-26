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
    writer.writerow(["found_preds_count","missed_preds_count","extra_count","missed_preds", "network", "scenario", "num_mus", "num_failures", "num_policies"])
        
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
    if os.path.exists(snapshot_ids_dir):
        for id_filename in os.listdir(snapshot_ids_dir):
            with open(os.path.join(snapshot_ids_dir, id_filename)) as id_file:
                snapshot_id = id_file.read().strip()
            snapshot_name = id_filename[:-3]
            snapshot_dir = os.path.join(network_dir, "snapshots", snapshot_id)
            if args.mus == args.failure == args.policies == False:
                faulty_preds, snapshot_candidates, num_mus ,num_failures, num_policies= process_snapshot_num(snapshot_dir, writer, args)
                found_count, missed_count, missed_preds, extra_count = analyze_candidates(snapshot_candidates, faulty_preds)
                if (not args.print_per_policy):
                    writer.writerow(
                        [str(found_count),
                        str(missed_count),
                        str(extra_count),
                        str(missed_preds),
                        network,
                        snapshot_name,
                        num_mus,
                        num_failures,
                        num_policies]
                    )
            else:
                nums = [0,1,2,4,8,16,32,64]
                for i, num in enumerate(nums):
                    if i == 0:
                        continue
                    faulty_preds, snapshot_candidates,num_mus, num_failures, num_policies = process_snapshot_num(snapshot_dir, writer, args, num)
                    found_count, missed_count, missed_preds, extra_count = analyze_candidates(snapshot_candidates, faulty_preds)
                    if ((args.mus and num_mus>=nums[i-1])or(args.failure and num_failures>=nums[i-1])or(args.policies and num_policies>=nums[i-1])):
                        if (not args.print_per_policy):
                            writer.writerow(
                                [str(found_count),
                                str(missed_count),
                                str(extra_count),
                                str(missed_preds),
                                network,
                                snapshot_name,
                                num_mus,
                                num_failures,
                                num_policies])


def process_snapshot_num(snapshot_dir, writer, args, num = 1000000):
    output_dir = os.path.join(snapshot_dir, "output")
    policy_to_candidates = dict()
    policy_to_failcount = dict()
    policy_to_muscount = dict()
    for filename in os.listdir(output_dir):
        if filename.startswith('mus_bulk'):
            mus_path = os.path.join(output_dir, filename)
            if not args.mus: 
                policy, candidates, num_mus= process_mus_intersect(mus_path) if args.i_fail else process_mus(mus_path)
            else:
                policy, candidates, num_mus= process_mus_intersect_num(mus_path, num) if args.i_fail else process_mus_num(mus_path, num)
                
            if policy not in policy_to_failcount:
                policy_to_failcount[policy] = 0
                policy_to_muscount[policy] = []
            if (not args.failure or policy_to_failcount[policy] < num):
                policy_to_failcount[policy] = policy_to_failcount[policy]+1
            else:
                continue

            if (not args.policies or len(policy_to_candidates) < num):
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
            found_count, missed_count, missed_preds, extra_count = analyze_candidates(policy_candidates, faulty_preds)
            num_mus = min(policy_to_muscount[policy])
            num_failures = policy_to_failcount[policy]
            if ((args.mus)or(args.failure and num_failures>=num)or (not args.mus and not args.failure)):
                writer.writerow(
                            [str(found_count),
                            str(missed_count),
                            str(extra_count),
                            str(missed_preds),
                            "",
                            "",
                            num_mus,
                            num_failures,
                            policy]
                        )
        return set(), set(), 0, 0, 0

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

        #min([min(mus_count) for mus_count in policy_to_muscount.values()])

    return faulty_preds, snapshot_candidates, min([min(mus_count) for mus_count in policy_to_muscount.values()]), max(policy_to_failcount.values()), len(policy_to_candidates)

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
    count = 0

    with open(mus_file_path, "r") as mus_file:
        for mus in mus_file:
            count = count+1
            if mus.startswith('['): #first line is policy being checked
                policy = mus
                continue
            preds_list =  mus.split(',')
            for x in preds_list:
                preds.add(x)
    return policy,preds,count-1

def process_mus_num(mus_file_path, num):
    preds= set()
    count = 0
    with open(mus_file_path, "r") as mus_file:
        for mus in mus_file:
            count = count+1
            if mus.startswith('['): #first line is policy being checked
                policy = mus
                continue
            preds_list =  mus.split(',')
            for x in preds_list:
                preds.add(x)
            if count == (num+1):
                break
    return policy,preds,count-1

def analyze_candidates(candidates, faults):
    found_count = len(faults.intersection(candidates))
    missed_count = len(faults) - found_count
    missed_preds = faults.difference(candidates)
    extra_count = len(candidates) - found_count

    return found_count, missed_count, missed_preds, extra_count


def process_mus_intersect(mus_file_path):
    preds = set()
    first = True
    count = 0
    with open(mus_file_path, "r") as mus_file:
        for mus in mus_file:
            count = count+1
            if mus.startswith('['):
                policy = mus
                continue
            preds_set = set(mus.split(','))
            if (first):
                preds = preds.union(preds_set)
                first = False
            else:
                preds = preds.intersection(preds_set)
    return policy, preds, count-1

def process_mus_intersect_num(mus_file_path, num):
    preds = set()
    first = True
    count = 0
    with open(mus_file_path, "r") as mus_file:
        for mus in mus_file:
            count = count+1
            if mus.startswith('['):
                policy = mus
                continue
            preds_set = set(mus.split(','))
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
