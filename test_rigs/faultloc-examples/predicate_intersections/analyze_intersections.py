#!/usr/bin/python
import os, sys
from collections import Counter
import json

def analyze(dir, network, testrig):
	global avg_percentage_modal, total_preds, modal_preds
	unsat_list = parse_core_pred_files(os.path.join(dir, "unsat.out"))
	not_unsat_list = parse_core_pred_files(os.path.join(dir, "not_unsat.out"))
	faultloc_list = parse_faultloc_file(os.path.join(dir, "faultloc"))
	unsat_count = count_occurences(unsat_list)	
	not_unsat_count = count_occurences(not_unsat_list)
	
	modal_frequency, modal_percentage = find_highest_rank_percentage(not_unsat_count)
	avg_percentage_modal +=  modal_percentage 
	fault_occurences_count_unsat = dict()
	for fault in faultloc_list:
		if fault in unsat_count:
			fault_occurences_count_unsat[fault] = unsat_count[fault]
			print 'found in unsat'
		else:
			fault_occurences_count_unsat[fault] = 0

	fault_occurences_count_not_unsat = dict()
	for fault in faultloc_list:
		total_preds += 1
		if fault in not_unsat_count:
			fault_occurences_count_not_unsat[fault] = not_unsat_count[fault]
			if not_unsat_count[fault] == modal_frequency:
				modal_preds += 1
			else:
				print not_unsat_count[fault], modal_frequency	
		else:
			fault_occurences_count_not_unsat[fault] = 0
	with open(os.path.join(dir,'fault_count_unsat.json'), 'w') as f:
		json.dump(fault_occurences_count_unsat, f)
	
	with open(os.path.join(dir, 'fault_count_not_unsat.json'), 'w') as f:
		json.dump(fault_occurences_count_not_unsat, f)


def parse_core_pred_files(file_path):
	with open(file_path, 'r') as f:
		content = f.read()
		pred_list = content.split(',')
		pred_list = [i for i in pred_list if i] #Remove empty strings
	return pred_list

def parse_faultloc_file(faultloc_path):
	with open(faultloc_path, 'r') as f:
		content = f.readlines()
		fault_list = [i.rstrip('\n') for i in content if ' ' in i] #Remove 'q1' strings.
	for i in range(len(fault_list)):
		#Getting rid of nulls.
		fault = fault_list[i]
		tokens = fault.split()
		tokens = [j for j in tokens if j!='null']
		fault_list[i] = ' '.join(tokens)
	return fault_list

def find_highest_rank_percentage(count_map):
	highest_rank = max(count_map.values())
	highest_preds = [i for i in count_map.keys() if count_map[i]==highest_rank]
	print highest_preds
	percentPredsWithHighestRank = (100.0*float(len(highest_preds)))/float(len(count_map.keys()))
	return highest_rank, percentPredsWithHighestRank

def count_occurences(pred_list):
	count_dic =  dict(Counter(pred_list))	
	return count_dic

BASE_DIR = os.path.join(os.path.dirname(os.path.realpath(__file__)),"containers")
NETWORKS = ["bgp-triangle","enterprise-campus","fattree","ospf-line","ospf-triangle","redistribute-triangle"]
VARIATIONS =  ["add-acl", "add-routemap","rm-network", "rm-neighbor","disable-interface"]
#NETWORKS = ["bgp-triangle"]
#VARIATIONS = ["add-acl"]

modal_preds = 0
total_preds = 0
avg_percentage_modal = 0.0
count_cases = 0.0
for network in NETWORKS: 
	network_dir = os.path.join(BASE_DIR, network, "testrigs")
	for testrig in VARIATIONS:
		results_dir = os.path.join(network_dir, testrig, "testrig")
		if os.path.exists(results_dir):
			if "unsat.out" in os.listdir(results_dir):
				count_cases += 1.0
				analyze(results_dir, network, testrig)


avg_percentage_modal /= count_cases
print 'num_cases, num_modal_preds_faulty, total_faulty_preds, avg_modal_percent'
print count_cases,modal_preds, total_preds, avg_percentage_modal
