#!/usr/bin/python
import os

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

BASE_DIR = os.path.dirname(os.path.realpath(__file__))
P_DIR = os.path.join(BASE_DIR, "p","containers")
NP_DIR = os.path.join(BASE_DIR, "not_p","containers")

NETWORKS = ["bgp-triangle","enterprise-campus","fattree","ospf-line","ospf-triangle","redistribute-triangle"]
VARIATIONS =  ["add-acl", "add-routemap","rm-network", "rm-neighbor","disable-interface"]

for network in NETWORKS: 
	for testrig in VARIATIONS:
		results_dir_p = os.path.join(P_DIR, network, "testrigs", testrig, "testrig")
		results_dir_np = os.path.join(NP_DIR, network, "testrigs",testrig, "testrig")
		
		if os.path.exists(results_dir_p) and os.path.exists(results_dir_np):
			if os.path.exists(os.path.join(results_dir_np, "listA.out")):
				faultloc_file_path = os.path.join(results_dir_np, "faultloc")
				content = parse_faultloc_file(faultloc_file_path)	
				listA = os.path.join(results_dir_np, "listA.out")
				listB = os.path.join(results_dir_np, "listB.out")
				listC = os.path.join(results_dir_np, "listC.out")

				with open(listA,'r') as f:
					contentListA = f.readlines()
				contentListA = [x.strip() for x in contentListA]

				with open(listB, 'r') as f:
					contentListB = f.readlines()
				contentListB = [x.strip() for x in contentListB]
				with open(listC, 'r') as f:
					contentListC = f.readlines()
				contentListC = [x.strip() for x in contentListC]
				print contentListA, content
				numFoundA = len([x for x in contentListA if x in content])
				numTotalA = len(contentListA)
				
				numFoundB = len([x for x in contentListB if x in content])
				numTotalB = len(contentListB)

				numFoundC = len([x for x in contentListC if x in content])
				numTotalC = len(contentListC)

				print '%d : %d | %d : %d | %d : %d' % (numFoundA, numTotalA, numFoundB, numTotalB, numFoundC, numTotalC)

