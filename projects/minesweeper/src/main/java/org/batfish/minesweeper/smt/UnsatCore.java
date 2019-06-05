package org.batfish.minesweeper.smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import java.util.HashMap;
import java.util.Map;
import org.batfish.minesweeper.smt.PredicateLabel.labels;

/**
 * A wrapper class that simplifies adding variables to the network model. If debugging is enabled,
 * then it creates new variables to track the state of the other variables in the model. This allows
 * the solver to create a minimal unsat core, which is useful for debugging.
 *
 * @author Ryan Beckett
 */
class UnsatCore {

  private boolean _doTrack;

  private Map<String, BoolExpr> _trackingVars;
  private Map<String, PredicateLabel> _trackingLabels;
  private boolean _noFilter;
  private boolean _includeComputable;

  private int _trackingNum;

  protected static final String BOUND = "bound";
  protected static final String FAILED = "failed";
  protected static final String ENVIRONMENT = "environment";
  protected static final String BEST_OVERALL = "bestOverall";
  protected static final String BEST_PER_PROTOCOL = "bestPerProtocol";
  protected static final String CHOICE_PER_PROTOCOL = "choicePerProtocol";
  protected static final String CONTROL_FORWARDING = "controlForwarding";
  protected static final String POLICY = "policy";
  protected static final String UNUSED_DEFAULT_VALUE = "addUnusedDefaultValueConstraints";
  protected static final String HISTORY_CONSTRAINTS = "addHistoryConstraints";

  UnsatCore(boolean doTrack, boolean noFilter, boolean includeComputable) {
    _doTrack = doTrack;
    _trackingLabels = new HashMap<>();
    _trackingVars = new HashMap<>();
    _trackingNum = 0;
    _noFilter = noFilter;
    _includeComputable = includeComputable;
  }

  void track(Solver solver, Context ctx, BoolExpr be, PredicateLabel label) {
    String name = "Pred" + _trackingNum; // + "(label:" +label+ ")";
    _trackingLabels.put(name, label);
    _trackingNum = _trackingNum + 1;
    _trackingVars.put(name, be);
    if (_doTrack && (_noFilter 
          || label.isConfigurable() 
          || (_includeComputable && label.isComputable()))) {
        solver.assertAndTrack(be, ctx.mkBoolConst(name));
      } else {
        solver.add(be);
    }
  }

  boolean getDoTrack() {
    return _doTrack;
  }

  Map<String, BoolExpr> getTrackingVars() {
    return _trackingVars;
  }

  Map<String, PredicateLabel> getTrackingLabels() {
    return _trackingLabels;
  }
}
