get smt-path-preference srcIps=['12.0.0.0/8'],dstIps=['13.0.0.0/8'],failures=1,prefs=[["d","b","c"],["d","c"]]
get smt-path-preference srcIps=['13.0.0.0/8'],dstIps=['12.0.0.0/8'],failures=1,prefs=[["c","b","d"],["c","d"]]
get smt-path-preference srcIps=['12.0.0.0/8'],dstIps=['11.0.0.0/8'],failures=1,prefs=[["d","b"],["d","c","b"]]
get smt-path-preference srcIps=['11.0.0.0/8'],dstIps=['12.0.0.0/8'],failures=1,prefs=[["b","d"],["b","c","d"]]
get smt-path-preference srcIps=['13.0.0.0/8'],dstIps=['11.0.0.0/8'],failures=1,prefs=[["c","b"],["c","d","b"]]
get smt-path-preference srcIps=['11.0.0.0/8'],dstIps=['13.0.0.0/8'],failures=1,prefs=[["b","c"],["b","d","c"]]