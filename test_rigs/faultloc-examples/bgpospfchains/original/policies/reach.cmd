get smt-reachability ingressNodeRegex="D", finalNodeRegex="C", srcIps=['12.0.0.0/8'], dstIps=['13.0.0.0/8'], failures=1
get smt-reachability ingressNodeRegex="C", finalNodeRegex="D", srcIps=['13.0.0.0/8'], dstIps=['12.0.0.0/8'], failures=1
get smt-reachability ingressNodeRegex="D", finalNodeRegex="B", srcIps=['12.0.0.0/8'], dstIps=['11.0.0.0/8'], failures=1
get smt-reachability ingressNodeRegex="B", finalNodeRegex="D", srcIps=['11.0.0.0/8'], dstIps=['12.0.0.0/8'], failures=1
get smt-reachability ingressNodeRegex="C", finalNodeRegex="B", srcIps=['13.0.0.0/8'], dstIps=['11.0.0.0/8'], failures=1
get smt-reachability ingressNodeRegex="B", finalNodeRegex="C", srcIps=['11.0.0.0/8'], dstIps=['13.0.0.0/8'], failures=1
