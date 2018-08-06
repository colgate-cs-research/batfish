#!/usr/bin/python

import os, sys

#analyze improvements in accuracy and precision of fault localization using intersections of unsatcores across verification of a policy and its negation. 

'''
1. Unsat (!P) 2. Unsat(P) 3. Nunsat (!P) 4. Nunsat(P)
Options : 
a. 2-1 
b. 3-4
c. 3 AND 2
d. 3-4 OR/AND 2-1

'''



def parse_core_pred_files(file_path):
	with open(file_path, 'r') as f:
		content = f.read()
		pred_list = content.split(',')
		pred_list = [i for i in pred_list if i] #Remove empty strings
	return list(set(pred_list))

BASE_DIR = os.path.dirname(os.path.realpath(__file__))
P_DIR = os.path.join(BASE_DIR, "p","containers")
NP_DIR = os.path.join(BASE_DIR, "not_p","containers")

NETWORKS = ["bgp-triangle","enterprise-campus","fattree","ospf-line","ospf-triangle","redistribute-triangle"]
VARIATIONS =  ["add-acl", "add-routemap","rm-network", "rm-neighbor","disable-interface"]
#NETWORKS = ["bgp-triangle"]
#VARIATIONS = ["add-acl"]
for network in NETWORKS:
	for testrig in VARIATIONS:
		results_dir_p = os.path.join(P_DIR, network, "testrigs", testrig, "testrig")
		results_dir_np = os.path.join(NP_DIR, network, "testrigs",testrig, "testrig")
		
		if os.path.exists(results_dir_p) and os.path.exists(results_dir_np):
			if ("unsat_no_negate.out" in os.listdir(results_dir_p)):
				#1
				list_preds_unsat_p = parse_core_pred_files(os.path.join(results_dir_p, "unsat_no_negate.out"))
				#3 
				list_preds_nunsat_p = parse_core_pred_files(os.path.join(results_dir_p, "not_unsat_no_negate.out"))
				if  ("unsat.out" in os.listdir(results_dir_np)):
					#2
					list_preds_unsat_np = parse_core_pred_files(os.path.join(results_dir_np, "unsat.out"))
					#4
					list_preds_nunsat_np = parse_core_pred_files(os.path.join(results_dir_np, "not_unsat.out"))
					
					#2-1
					listA = list(set(list_preds_unsat_np)-set(list_preds_unsat_p))
					
					#3-4
					listB = list(set(list_preds_nunsat_p)-set(list_preds_nunsat_np))
					
					#3 intersect 2
					listC = [x for x in list_preds_nunsat_p if x in list_preds_unsat_np]
					
							
					with open(os.path.join(results_dir_np, "listA.out"), 'w') as f:
						for i in listA:
							f.write("%s\n" % i)


					with open(os.path.join(results_dir_np,"listB.out"), 'w') as f:
						for i in listB:
							f.write("%s\n" % i)

					with open(os.path.join(results_dir_np, "listC.out"), 'w') as f:
						for i in listC:
							f.write("%s\n" % i)


	 
