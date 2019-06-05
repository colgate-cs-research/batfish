get smt-reachability ingressNodeRegex="d", finalNodeRegex="c", srcIps=['12.0.0.0/8'], dstIps=['13.0.0.0/8'], failures=1
get smt-reachability ingressNodeRegex="c", finalNodeRegex="d", srcIps=['13.0.0.0/8'], dstIps=['12.0.0.0/8'], failures=1
get smt-reachability ingressNodeRegex="d", finalNodeRegex="b", srcIps=['12.0.0.0/8'], dstIps=['11.0.0.0/8'], failures=1
get smt-reachability ingressNodeRegex="b", finalNodeRegex="d", srcIps=['11.0.0.0/8'], dstIps=['12.0.0.0/8'], failures=1
get smt-reachability ingressNodeRegex="c", finalNodeRegex="b", srcIps=['13.0.0.0/8'], dstIps=['11.0.0.0/8'], failures=1
get smt-reachability ingressNodeRegex="b", finalNodeRegex="c", srcIps=['11.0.0.0/8'], dstIps=['13.0.0.0/8'], failures=1
