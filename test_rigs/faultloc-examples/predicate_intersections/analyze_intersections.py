#!/usr/bin/python
import os, sys
from collections import Counter
import json

def analyze(dir, network, testrig):
	unsat_list = parse_core_pred_files(os.path.join(dir, "unsat.out"))
	not_unsat_list = parse_core_pred_files(os.path.join(dir, "not_unsat.out"))
	faultloc_list = parse_faultloc_file(os.path.join(dir, "faultloc"))
	unsat_count = count_occurences(unsat_list)	
	not_unsat_count = count_occurences(not_unsat_list)
	
	fault_occurences_count_unsat = dict()
	for fault in faultloc_list:
		if fault in unsat_count:
			fault_occurences_count_unsat[fault] = unsat_count[fault]
		else:
			fault_occurences_count_unsat[fault] = 0

	fault_occurences_count_not_unsat = dict()
	for fault in faultloc_list:
		if fault in not_unsat_count:
			fault_occurences_count_not_unsat[fault] = not_unsat_count[fault]
			print dir, network, testrig
		else:
			fault_occurences_count_not_unsat[fault] = 0
	print fault_occurences_count_not_unsat
	print not_unsat_list	
	print unsat_list	
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
	return fault_list

def count_occurences(pred_list):
	return  dict(Counter(pred_list))	

BASE_DIR = os.path.join(os.path.dirname(os.path.realpath(__file__)),"containers")
NETWORKS = ["bgp-triangle","enterprise-campus","fattree","ospf-line","ospf-triangle","redistribute-triangle"]
VARIATIONS =  ["add-acl", "add-routemap","rm-network", "rm-neighbor","disable-interface"]
#NETWORKS = ["ospf-triangle"]
#VARIATIONS = ["add-acl"]


for network in NETWORKS: 
	network_dir = os.path.join(BASE_DIR, network, "testrigs")
	for testrig in VARIATIONS:
		results_dir = os.path.join(network_dir, testrig, "testrig")
		if os.path.exists(results_dir):
			if "unsat.out" in os.listdir(results_dir):
				analyze(results_dir, network, testrig)
