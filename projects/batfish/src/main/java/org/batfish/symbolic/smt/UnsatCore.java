package org.batfish.symbolic.smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper class that simplifies adding variables to the network model. mkIf debugging is enabled,
 * then it creates new variables to track the state of the other variables in the model. This allows
 * the solver to create a minimal unsat core, which is useful for debugging.
 *
 * @author Ryan Beckett
 */
class UnsatCore {

  private boolean _doTrack;

  private Map<String, BoolExpr> _trackingVars;
  private Map<String, String> _trackingLabels;

  private int _trackingNum;

  protected static final String BOUND = "bound";
  protected static final String FAILED = "failed";
  protected static final String ENVIRONMENT = "environment";
  protected static final String BEST_OVERALL = "bestOverall";
  protected static final String BEST_PER_PROTOCOL = "bestPerProtocol";
  protected static final String CHOICE_PER_PROTOCOL = "choicePerProtocol";
  protected static final String CONTROL_FORWARDING = "controlForwarding";
  protected static final String POLICY = "policy";

  UnsatCore(boolean doTrack) {
    _doTrack = doTrack;
    _trackingLabels = new HashMap<>();
    _trackingVars = new HashMap<>();
    _trackingNum = 0;
  }

  void track(Solver solver, Context ctx, BoolExpr be, String label){
    String name = "Pred" + _trackingNum;// + "(label:" +label+ ")";
    _trackingLabels.put(name, label);
    _trackingNum = _trackingNum + 1;
    _trackingVars.put(name, be);
    if (_doTrack) {
      solver.assertAndTrack(be, ctx.mkBoolConst(name));
    } else {
      solver.add(be);
    }
  }
  void track(Solver solver, Context ctx, BoolExpr be) {
    track(solver, ctx, be, "");
  }

  boolean getDoTrack() {
    return _doTrack;
  }

  Map<String, BoolExpr> getTrackingVars() {
    return _trackingVars;
  }

  Map<String, String> getTrackingLabels() {return _trackingLabels;}
}
