get smt-reachability ingressNodeRegex="A", finalNodeRegex="C", srcIps=['101.0.0.0/8'], dstIps=['103.0.0.0/8'], failures=1
get smt-reachability ingressNodeRegex="C", finalNodeRegex="A", srcIps=['103.0.0.0/8'], dstIps=['101.0.0.0/8'], failures=1
get smt-reachability ingressNodeRegex="A", finalNodeRegex="B", srcIps=['101.0.0.0/8'], dstIps=['102.0.0.0/8'], failures=1
get smt-reachability ingressNodeRegex="B", finalNodeRegex="A", srcIps=['102.0.0.0/8'], dstIps=['101.0.0.0/8'], failures=1
get smt-reachability ingressNodeRegex="C", finalNodeRegex="B", srcIps=['103.0.0.0/8'], dstIps=['102.0.0.0/8'], failures=1
get smt-reachability ingressNodeRegex="B", finalNodeRegex="C", srcIps=['102.0.0.0/8'], dstIps=['103.0.0.0/8'], failures=1
