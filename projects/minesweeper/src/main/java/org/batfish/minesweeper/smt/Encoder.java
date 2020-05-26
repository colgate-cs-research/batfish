package org.batfish.minesweeper.smt;

import com.microsoft.z3.enumerations.Z3_ast_print_mode;
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Tactic;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nullable;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.Graph;
import org.batfish.minesweeper.GraphEdge;
import org.batfish.minesweeper.OspfType;
import org.batfish.minesweeper.Protocol;
import org.batfish.minesweeper.question.HeaderQuestion;
import org.batfish.minesweeper.smt.PredicateLabel.LabelType;
import org.batfish.minesweeper.utils.MsPair;
import org.batfish.minesweeper.utils.Tuple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * A class responsible for building a symbolic encoding of the entire network. The encoder does this
 * by maintaining a collection of encoding slices, where each slice encodes the forwarding behavior
 * for a particular packet.
 *
 * <p>The encoder object is architected this way to allow for modeling of features such as iBGP or
 * non-local next-hop ip addresses in static routes, where the forwarding behavior of one packet
 * depends on that of other packets.
 *
 * <p>Symbolic variables that are common to all slices are maintained in this class. That includes,
 * for example, the collection of variables representing topology failures.
 *
 * @author Ryan Beckett
 */
public class Encoder {

  static final Boolean ENABLE_DEBUGGING = false;
  static final String MAIN_SLICE_NAME = "SLICE-MAIN_";

  static final String UNSATCORE_OUT_FILE = "unsat.out";
  static final String UNSATCORE_OUT_FILE_NO_NEGATE = "unsat_no_negate.out";

  static final String NOT_UNSATCORE_OUT_FILE = "not_unsat.out";
  static final String NOT_UNSATCORE_OUT_FILE_NO_NEGATE = "not_unsat_no_negate.out";

  static final String REFERENCE_FILE_NAME = "predicate_config_refs";

  public static final String ARG_NO_FAULTLOC = "noFaultloc";
  public static final String ARG_NUM_ITERS_FAULTLOC = "numIters";
  public static final String ARG_NO_NEGATE_PROPERTY = "noNegateProperty";
  public static final String ARG_PRINT_GRAPH = "printGraph";
  public static final String ARG_PRINT_SMT = "printSmt";
  public static final String ARG_PRINT_COUNTER_EXAMPLE_CHANGES = "printCeDiff";
  public static final String ARG_PRINT_UNSAT_EXPR = "printUnsatExpr";
  public static final String ARG_ENABLE_SLICING = "enableSlicing";
  public static final String ARG_INCLUDE_COMPUTABLE = "includeComputable";
  public static final String ARG_NO_FILTER_UNSAT_CORE = "noFilterUnsatCore";
  public static final String ARG_MINIMIZE_UNSAT_CORE = "minimizeUnsatCore";
  public static final String ARG_SPLIT_ITE = "splitITE";
  public static final String ARG_USE_MARCO = "useMarco";
  public static final String ARG_MARCO_VERBOSE = "marcoVerbose";
  public static final String ARG_MAX_MUS_COUNT = "mus";
  public static final String ARG_MAX_MSS_COUNT = "mss";
  public static final String ARG_MAX_MARCO_TIME = "marcoTime";
  public static final String ARG_MUS_INTERSECT = "musIntersect";
  public static final String ARG_MUS_UNION = "musUnion";
  public static final String ARG_BULK_SAVE_MUS = "saveMUS";
  public static final String ARG_MUS_THRESHOLD = "musThreshold";
  public static final String ARG_SAVE_ALL_REFERENCES = "saveAllRefs";
  public static final String ARG_SKIP_DUPLICATE_FAILURES = "skipDupFailures";

  private static final boolean ENABLE_UNSAT_CORE = true;

  private int _encodingId;

  private boolean _modelIgp;

  private HeaderQuestion _question;

  private Map<String, EncoderSlice> _slices;

  private Map<String, Map<String, BoolExpr>> _sliceReachability;

  private Encoder _previousEncoder;

  private SymbolicFailures _symbolicFailures;

  private Map<String, Expr> _allVariables;

  private Graph _graph;

  private Context _ctx;

  private Solver _solver;

  private UnsatCore _unsatCore;

  private Solver _faultlocSolver;

  private UnsatCore _faultlocUnsatCore;

  private FaultlocStats _faultlocStats;

  private ImmutableConfiguration _settings;

  private IBatfish _batfish;

  /**
   * Create an encoder object that will consider all packets in the provided headerspace.
   *
   * @param graph The network graph
   */
  Encoder(Graph graph, HeaderQuestion q) {
    this(null, graph, q, null, null, null, 0);
  }

  /**
   * Create an encoder object from an existing encoder.
   *
   * @param e An existing encoder object
   * @param g An existing network graph
   */
  Encoder(Encoder e, Graph g) {
    this(
            e,
            g,
            e._question,
            e.getCtx(),
            e.getSolver(),
            e.getAllVariables(),
            e.getId() + 1);
  }

  /**
   * Create an encoder object from an existing encoder.
   *
   * @param e An existing encoder object
   * @param g An existing network graph
   * @param q A header question
   */
  Encoder(Encoder e, Graph g, HeaderQuestion q) {
    this(e, g, q, e.getCtx(), e.getSolver(), e.getAllVariables(), e.getId() + 1);
  }

  /**
   * Create an encoder object while possibly reusing the partial encoding of another encoder. If the
   * context and solver are null, then a new encoder is created. Otherwise the old encoder is used.
   */
  private Encoder(
          @Nullable Encoder enc,
          Graph graph,
          HeaderQuestion q,
          @Nullable Context ctx,
          @Nullable Solver solver,
          @Nullable Map<String, Expr> vars,
          int id) {
    _batfish = graph.getBatfish();
    _settings = _batfish.getSettingsConfiguration();
    _graph = graph;
    _previousEncoder = enc;
    _modelIgp = true;
    _encodingId = id;
    _question = q;
    _slices = new HashMap<>();
    _sliceReachability = new HashMap<>();

    HashMap<String, String> cfg = new HashMap<>();

    // allows for unsat core when debugging
    if (ENABLE_UNSAT_CORE) {
      cfg.put("proof", "true");
      cfg.put("auto-config", "false");
    }

    _ctx = (ctx == null ? new Context(cfg) : ctx);

    if (solver == null) {
      if (ENABLE_UNSAT_CORE) {
        _solver = _ctx.mkSolver();
      } else {
        Tactic t1 = _ctx.mkTactic("simplify");
        Tactic t2 = _ctx.mkTactic("propagate-values");
        Tactic t3 = _ctx.mkTactic("solve-eqs");
        Tactic t4 = _ctx.mkTactic("bit-blast");
        Tactic t5 = _ctx.mkTactic("smt");
        Tactic t = _ctx.then(t1, t2, t3, t4, t5);
        _solver = _ctx.mkSolver(t);
      }
    } else {
      _solver = solver;
    }

    _symbolicFailures = new SymbolicFailures(this._ctx);

    if (vars == null) {
      _allVariables = new HashMap<>();
    } else {
      _allVariables = vars;
    }

    if (ENABLE_DEBUGGING) {
      System.out.println(graph);
    }

    _unsatCore = new UnsatCore(ENABLE_UNSAT_CORE,
        _settings.getBoolean(ARG_NO_FILTER_UNSAT_CORE), 
        _settings.getBoolean(ARG_INCLUDE_COMPUTABLE));

    _faultlocSolver = _ctx.mkSolver();
    _faultlocUnsatCore = new UnsatCore(true,
        _settings.getBoolean(ARG_NO_FILTER_UNSAT_CORE), 
        _settings.getBoolean(ARG_INCLUDE_COMPUTABLE));

    initFailedLinkVariables();
    initFailedNodeVariables();
    initSlices(_question.getHeaderSpace(), graph);

    _faultlocStats = new FaultlocStats(_batfish, _question.toJsonString());
  }

  /*
   * Initialize symbolic variables to represent link failures.
   */
  private void initFailedLinkVariables() {
    for (List<GraphEdge> edges : _graph.getEdgeMap().values()) {
      for (GraphEdge ge : edges) {
        if (ge.getPeer() == null) {
          Interface i = ge.getStart();
          String name = getId() + "_FAILED-EDGE_" + ge.getRouter() + "_" + i.getName();
          ArithExpr var = getCtx().mkIntConst(name);
          _symbolicFailures.getFailedEdgeLinks().put(ge, var);
          _allVariables.put(var.toString(), var);
        }
      }
    }

    for (Entry<String, Set<String>> entry : _graph.getNeighbors().entrySet()) {
      String router = entry.getKey();
      Set<String> peers = new HashSet<String>(entry.getValue());
      peers.addAll(_graph.getPossibleNeighbors().get(router));
      for (String peer : peers) {
        // sort names for unique
        String pair = (router.compareTo(peer) < 0 ? router + "_" + peer : peer + "_" + router);
        String name = getId() + "_FAILED-EDGE_" + pair;
        ArithExpr var = _ctx.mkIntConst(name);
        _symbolicFailures.getFailedInternalLinks().put(router, peer, var);
        _allVariables.put(var.toString(), var);
      }
    }
  }

  /*
   * Initialize symbolic variables to represent node failures.
   */
  private void initFailedNodeVariables() {
    for (String router : _graph.getRouters()) {
      String name = getId() + "_FAILED-NODE_" + router;
      ArithExpr var = _ctx.mkIntConst(name);
      _symbolicFailures.getFailedNodes().put(router, var);
      _allVariables.put(var.toString(), var);
    }
  }

  /*
   * Initialize each encoding slice.
   * For iBGP, we also add reachability information for each pair of neighbors,
   * to determine if messages sent to/from a neighbor will arrive.
   */
  private void initSlices(HeaderSpace h, Graph g) {
    if (g.getIbgpNeighbors().isEmpty() || !_modelIgp) {
      _slices.put(MAIN_SLICE_NAME, new EncoderSlice(this, h, g, ""));
    } else {
      _slices.put(MAIN_SLICE_NAME, new EncoderSlice(this, h, g, MAIN_SLICE_NAME));
    }

    if (_modelIgp) {
      SortedSet<MsPair<String, Ip>> ibgpRouters = new TreeSet<>();

      for (Entry<GraphEdge, BgpActivePeerConfig> entry : g.getIbgpNeighbors().entrySet()) {
        GraphEdge ge = entry.getKey();
        BgpPeerConfig n = entry.getValue();
        String router = ge.getRouter();
        Ip ip = n.getLocalIp();
        MsPair<String, Ip> pair = new MsPair<>(router, ip);

        // Add one slice per (router, source ip) pair
        if (!ibgpRouters.contains(pair)) {

          ibgpRouters.add(pair);

          // Create a control plane slice only for this ip
          HeaderSpace hs = new HeaderSpace();

          // Make sure messages are sent to this destination IP
          SortedSet<IpWildcard> ips = new TreeSet<>();
          ips.add(IpWildcard.create(n.getLocalIp()));
          hs.setDstIps(ips);

          // Make sure messages use TCP port 179
          SortedSet<SubRange> dstPorts = new TreeSet<>();
          dstPorts.add(new SubRange(179, 179));
          hs.setDstPorts(dstPorts);

          // Make sure messages use the TCP protocol
          SortedSet<IpProtocol> protocols = new TreeSet<>();
          protocols.add(IpProtocol.TCP);
          hs.setIpProtocols(protocols);

          // TODO: create domains once
          Graph gNew = new Graph(g.getBatfish(), null, g.getDomain(router));
          String sliceName = "SLICE-" + router + "_";
          EncoderSlice slice = new EncoderSlice(this, hs, gNew, sliceName);
          _slices.put(sliceName, slice);

          PropertyAdder pa = new PropertyAdder(slice);
          Map<String, BoolExpr> reachVars = pa.instrumentReachability(router);
          _sliceReachability.put(router, reachVars);
        }
      }
    }
  }

  // Create a symbolic boolean
  BoolExpr mkBool(boolean val) {
    return getCtx().mkBool(val);
  }

  // Symbolic boolean negation
  BoolExpr mkNot(BoolExpr e) {
    return getCtx().mkNot(e);
  }

  // Symbolic boolean disjunction
  BoolExpr mkOr(BoolExpr... vals) {
    return getCtx().mkOr(Arrays.stream(vals).filter(Objects::nonNull).toArray(BoolExpr[]::new));
  }

  // Symbolic boolean implication
  BoolExpr mkImplies(BoolExpr e1, BoolExpr e2) {
    return getCtx().mkImplies(e1, e2);
  }

  // Symbolic boolean conjunction
  BoolExpr mkAnd(BoolExpr... vals) {
    return getCtx().mkAnd(Arrays.stream(vals).filter(Objects::nonNull).toArray(BoolExpr[]::new));
  }

  // Symbolic true value
  BoolExpr mkTrue() {
    return getCtx().mkBool(true);
  }

  // Symbolic false value
  BoolExpr mkFalse() {
    return getCtx().mkBool(false);
  }

  // Symbolic arithmetic less than
  BoolExpr mkLt(Expr e1, Expr e2) {
    if (e1 instanceof BoolExpr && e2 instanceof BoolExpr) {
      return mkAnd((BoolExpr) e2, mkNot((BoolExpr) e1));
    }
    if (e1 instanceof ArithExpr && e2 instanceof ArithExpr) {
      return getCtx().mkLt((ArithExpr) e1, (ArithExpr) e2);
    }
    if (e1 instanceof BitVecExpr && e2 instanceof BitVecExpr) {
      return getCtx().mkBVULT((BitVecExpr) e1, (BitVecExpr) e2);
    }
    throw new BatfishException("Invalid call to mkLt while encoding control plane");
  }

  // Symbolic greater than
  BoolExpr mkGt(Expr e1, Expr e2) {
    if (e1 instanceof BoolExpr && e2 instanceof BoolExpr) {
      return mkAnd((BoolExpr) e1, mkNot((BoolExpr) e2));
    }
    if (e1 instanceof ArithExpr && e2 instanceof ArithExpr) {
      return getCtx().mkGt((ArithExpr) e1, (ArithExpr) e2);
    }
    if (e1 instanceof BitVecExpr && e2 instanceof BitVecExpr) {
      return getCtx().mkBVUGT((BitVecExpr) e1, (BitVecExpr) e2);
    }
    throw new BatfishException("Invalid call the mkLe while encoding control plane");
  }

  // Symbolic arithmetic subtraction
  ArithExpr mkSub(ArithExpr e1, ArithExpr e2) {
    return getCtx().mkSub(e1, e2);
  }

  // Symbolic if-then-else for booleans
  BoolExpr mkIf(BoolExpr cond, BoolExpr case1, BoolExpr case2) {
    return (BoolExpr) getCtx().mkITE(cond, case1, case2);
  }

  // Symbolic if-then-else for arithmetic
  ArithExpr mkIf(BoolExpr cond, ArithExpr case1, ArithExpr case2) {
    return (ArithExpr) getCtx().mkITE(cond, case1, case2);
  }

  // Create a symbolic integer
  ArithExpr mkInt(long l) {
    return getCtx().mkInt(l);
  }

  // Symbolic arithmetic addition
  ArithExpr mkSum(ArithExpr e1, ArithExpr e2) {
    return getCtx().mkAdd(e1, e2);
  }

  // Symbolic greater than or equal to
  BoolExpr mkGe(Expr e1, Expr e2) {
    if (e1 instanceof ArithExpr && e2 instanceof ArithExpr) {
      return getCtx().mkGe((ArithExpr) e1, (ArithExpr) e2);
    }
    if (e1 instanceof BitVecExpr && e2 instanceof BitVecExpr) {
      return getCtx().mkBVUGE((BitVecExpr) e1, (BitVecExpr) e2);
    }
    throw new BatfishException("Invalid call to mkGe while encoding control plane");
  }

  // Symbolic less than or equal to
  BoolExpr mkLe(Expr e1, Expr e2) {
    if (e1 instanceof ArithExpr && e2 instanceof ArithExpr) {
      return getCtx().mkLe((ArithExpr) e1, (ArithExpr) e2);
    }
    if (e1 instanceof BitVecExpr && e2 instanceof BitVecExpr) {
      return getCtx().mkBVULE((BitVecExpr) e1, (BitVecExpr) e2);
    }
    throw new BatfishException("Invalid call to mkLe while encoding control plane");
  }

  // Symblic equality of expressions
  BoolExpr mkEq(Expr e1, Expr e2) {
    //System.out.println("Making expressions");
    String exp1 = e1.toString();
    //System.out.println(exp1);
    //System.out.println(e1.toString()); THIS DOES NOTHING
    //System.out.println(e2);
    return getCtx().mkEq(e1, e2);
  }

  void add(BoolExpr e, PredicateLabel caller) {
    _unsatCore.track(_solver, _ctx, e, caller);
    // Invert policy for faultloc, if requested

//    System.out.println("[Pred] " + caller + " : " + e);

    if (caller.getLabelType() == LabelType.POLICY) {
      _ctx.setPrintMode(Z3_ast_print_mode.Z3_PRINT_SMTLIB_FULL);
      if  (_settings.getBoolean(ARG_NO_NEGATE_PROPERTY)) {
        e = _ctx.mkNot(e);
        System.out.println("Assert P: " + e);
      } else {
        System.out.println("Assert !P: " + e);
      }
    }
    _faultlocUnsatCore.track(_faultlocSolver, _ctx, e, caller);
  }

  /*
   * Adds the constraint that at most k links/nodes have failed.
   * This is done in two steps. First we ensure that each
   * variable that represents a failure is constrained to
   * take on a value between 0 and 1:
   *
   * 0 <= failVar_i <= 1
   *
   * Then we ensure that the sum of all fail variables is never more than k:
   *
   * failVar_1 + failVar_2 + ... + failVar_n <= k
   */
  private void addFailedConstraints(int k, Set<ArithExpr> vars) {
    PredicateLabel label=new PredicateLabel(PredicateLabel.LabelType.VALUE_LIMIT);
    ArithExpr sum = mkInt(0);
    for (ArithExpr var : vars) {
      sum = mkSum(sum, var);

      add(mkGe(var, mkInt(0)), label);
      add(mkLe(var, mkInt(1)), label);
    }
    label=new PredicateLabel(PredicateLabel.LabelType.FAILURE_LIMIT);
    if (k == 0) {
      for (ArithExpr var : vars) {
        add(mkEq(var, mkInt(0)), label);
      }
    } else {
      add(mkLe(sum, mkInt(k)), label);
    }
  }

  /* Generate constraints for link failures */
  private void addFailedLinkConstraints(int k) {
    Set<ArithExpr> vars = new HashSet<>();
    getSymbolicFailures().getFailedInternalLinks().forEach((router, peer, var) -> vars.add(var));
    getSymbolicFailures().getFailedEdgeLinks().forEach((ge, var) -> vars.add(var));
    addFailedConstraints(k, vars);
  }

  /* Generate constraints for node failures */
  private void addFailedNodeConstraints(int k) {
    Set<ArithExpr> vars = new HashSet<>();
    getSymbolicFailures().getFailedNodes().forEach((router, var) -> vars.add(var));
    addFailedConstraints(k, vars);
  }

  /*
   * Check if a community value should be displayed to the human
   */
  private boolean displayCommunity(CommunityVar cvar) {
    if (cvar.getType() == CommunityVar.Type.OTHER) {
      return false;
    }
    if (cvar.getType() == CommunityVar.Type.EXACT) {
      return true;
    }
    return true;
  }



  /*
   * Add the relevant variables in the counterexample to
   * display to the user in a human-readable fashion
   */
  private void buildCounterExample(
          Encoder enc,
          Model m,
          SortedMap<String, String> model,
          SortedMap<String, String> packetModel,
          SortedSet<String> fwdModel,
          SortedMap<String, SortedMap<String, String>> envModel,
          SortedMap<String, ArithExpr> failures,
          SortedMap<String, ArithExpr> nonfailures,
          SortedMap<Expr, Expr> variableAssignments) {
    SortedMap<Expr, String> valuation = new TreeMap<>();
    // If user asks for the full model
    for (Entry<String, Expr> entry : _allVariables.entrySet()) {

      String name = entry.getKey();
      Expr e = entry.getValue();
      Expr val = m.evaluate(e, true);
      if (!val.equals(e)) { // excluding constants in the model.
        String s = val.toString();
        model.put(name, s);

        valuation.put(e, s);
        // counterExampleState.put(name, s);
        if (name.contains("reachable-id")){ //this fixes issue with bgp-acls
        }else{
          variableAssignments.put(e, val);
        }
      }
    }

    ArrayList<String> sortedKeys = new ArrayList<String>(model.keySet());
    Collections.sort(sortedKeys);

    // Packet model
    SymbolicPacket p = enc.getMainSlice().getSymbolicPacket();
    String dstIp = valuation.get(p.getDstIp());
    String srcIp = valuation.get(p.getSrcIp());
    String dstPt = valuation.get(p.getDstPort());
    String srcPt = valuation.get(p.getSrcPort());
    String icmpCode = valuation.get(p.getIcmpCode());
    String icmpType = valuation.get(p.getIcmpType());
    String ipProtocol = valuation.get(p.getIpProtocol());
    String tcpAck = valuation.get(p.getTcpAck());
    String tcpCwr = valuation.get(p.getTcpCwr());
    String tcpEce = valuation.get(p.getTcpEce());
    String tcpFin = valuation.get(p.getTcpFin());
    String tcpPsh = valuation.get(p.getTcpPsh());
    String tcpRst = valuation.get(p.getTcpRst());
    String tcpSyn = valuation.get(p.getTcpSyn());
    String tcpUrg = valuation.get(p.getTcpUrg());

    Ip dip = Ip.create(Long.parseLong(dstIp));
    Ip sip = Ip.create(Long.parseLong(srcIp));

    packetModel.put("dstIp", dip.toString());

    if (sip.asLong() != 0) {
      packetModel.put("srcIp", sip.toString());
    }
    if (dstPt != null && !dstPt.equals("0")) {
      packetModel.put("dstPort", dstPt);
    }
    if (srcPt != null && !srcPt.equals("0")) {
      packetModel.put("srcPort", srcPt);
    }
    if (icmpCode != null && !icmpCode.equals("0")) {
      packetModel.put("icmpCode", icmpCode);
    }
    if (icmpType != null && !icmpType.equals("0")) {
      packetModel.put("icmpType", icmpType);
    }
    if (ipProtocol != null && !ipProtocol.equals("0")) {
      Integer number = Integer.parseInt(ipProtocol);
      IpProtocol proto = IpProtocol.fromNumber(number);
      packetModel.put("protocol", proto.toString());
    }
    if ("true".equals(tcpAck)) {
      packetModel.put("tcpAck", "set");
    }
    if ("true".equals(tcpCwr)) {
      packetModel.put("tcpCwr", "set");
    }
    if ("true".equals(tcpEce)) {
      packetModel.put("tcpEce", "set");
    }
    if ("true".equals(tcpFin)) {
      packetModel.put("tcpFin", "set");
    }
    if ("true".equals(tcpPsh)) {
      packetModel.put("tcpPsh", "set");
    }
    if ("true".equals(tcpRst)) {
      packetModel.put("tcpRst", "set");
    }
    if ("true".equals(tcpSyn)) {
      packetModel.put("tcpSyn", "set");
    }
    if ("true".equals(tcpUrg)) {
      packetModel.put("tcpUrg", "set");
    }

    for (EncoderSlice slice : enc.getSlices().values()) {
      for (Entry<LogicalEdge, SymbolicRoute> entry2 :
              slice.getLogicalGraph().getEnvironmentVars().entrySet()) {
        LogicalEdge lge = entry2.getKey();
        SymbolicRoute r = entry2.getValue();
        if ("true".equals(valuation.get(r.getPermitted()))) {
          SortedMap<String, String> recordMap = new TreeMap<>();
          GraphEdge ge = lge.getEdge();
          String nodeIface = ge.getRouter() + "," + ge.getStart().getName() + " (BGP)";
          envModel.put(nodeIface, recordMap);
          if (r.getPrefixLength() != null) {
            String x = valuation.get(r.getPrefixLength());
            if (x != null) {
              int len = Integer.parseInt(x);
              Prefix p1 = Prefix.create(dip, len);
              recordMap.put("prefix", p1.toString());
            }
          }
          if (r.getAdminDist() != null) {
            String x = valuation.get(r.getAdminDist());
            if (x != null) {
              recordMap.put("admin distance", x);
            }
          }
          if (r.getLocalPref() != null) {
            String x = valuation.get(r.getLocalPref());
            if (x != null) {
              recordMap.put("local preference", x);
            }
          }
          if (r.getMetric() != null) {
            String x = valuation.get(r.getMetric());
            if (x != null) {
              recordMap.put("protocol metric", x);
            }
          }
          if (r.getMed() != null) {
            String x = valuation.get(r.getMed());
            if (x != null) {
              recordMap.put("multi-exit disc.", valuation.get(r.getMed()));
            }
          }
          if (r.getOspfArea() != null && r.getOspfArea().getBitVec() != null) {
            String x = valuation.get(r.getOspfArea().getBitVec());
            if (x != null) {
              Integer i = Integer.parseInt(x);
              Long area = r.getOspfArea().value(i);
              recordMap.put("OSPF Area", area.toString());
            }
          }
          if (r.getOspfType() != null && r.getOspfType().getBitVec() != null) {
            String x = valuation.get(r.getOspfType().getBitVec());
            if (x != null) {
              Integer i = Integer.parseInt(x);
              OspfType type = r.getOspfType().value(i);
              recordMap.put("OSPF Type", type.toString());
            }
          }

          for (Entry<CommunityVar, BoolExpr> entry3 : r.getCommunities().entrySet()) {
            CommunityVar cvar = entry3.getKey();
            BoolExpr e = entry3.getValue();
            String c = valuation.get(e);
            // TODO: what about OTHER type?
            if ("true".equals(c) && displayCommunity(cvar)) {
              String s = cvar.getRegex();
              String t = slice.getNamedCommunities().get(cvar.getRegex());
              s = (t == null ? s : t);
              recordMap.put("community " + s, "");
            }
          }
        }
      }
    }

    // Forwarding Model
    enc.getMainSlice()
            .getSymbolicDecisions()
            .getDataForwarding()
            .forEach(
                    (router, edge, e) -> {
                      String s = valuation.get(e);
                      if ("true".equals(s)) {
                        SymbolicRoute r =
                                enc.getMainSlice().getSymbolicDecisions().getBestNeighbor().get(router);
                        if (r.getProtocolHistory() != null) {
                          Protocol proto;
                          List<Protocol> allProtocols = enc.getMainSlice().getProtocols().get(router);
                          if (allProtocols.size() == 1) {
                            proto = allProtocols.get(0);
                          } else {
                            s = valuation.get(r.getProtocolHistory().getBitVec());
                            int i = Integer.parseInt(s);
                            proto = r.getProtocolHistory().value(i);
                          }
                          fwdModel.add(edge + " (" + proto.name() + ")");
                        } else {
                          fwdModel.add(edge.toString());
                        }
                      }
                    });

    _symbolicFailures
            .getFailedInternalLinks()
            .forEach(
                    (x, y, e) -> {
                      String s = valuation.get(e);
                      String pair = (x.compareTo(y) < 0 ? x + "," + y : y + "," + x);
                      if ("1".equals(s)) {
                        failures.put("link(" + pair + ")", e);
                      }else{
                        nonfailures.put("link(" + pair + ")", e);
                      }

                    });

    _symbolicFailures
            .getFailedEdgeLinks()
            .forEach(
                    (ge, e) -> {
                      String s = valuation.get(e);
                      if ("1".equals(s)) {
                        failures.put("link(" + ge.getRouter() + "," + ge.getStart().getName() + ")", e);
                      }else{
                        nonfailures.put("link(" +  ge.getRouter() + "," +  ge.getStart().getName()+ ")", e);
                      }
                    });

    _symbolicFailures
            .getFailedNodes()
            .forEach(
                    (x,e) -> {
                      String s = valuation.get(e);
                      if ("1".equals(s)){
                        failures.put("node(" + x + ")", e);
                      }
                    });
  }

  /*
   * Generate a blocking clause for the encoding that says that one
   * of the environments that was true before must now be false.
   */
  private BoolExpr environmentBlockingClause(Model m) {
    BoolExpr acc1 = mkFalse();
    BoolExpr acc2 = mkTrue();

    // Disable an environment edge if possible
    Map<LogicalEdge, SymbolicRoute> map = getMainSlice().getLogicalGraph().getEnvironmentVars();
    for (Map.Entry<LogicalEdge, SymbolicRoute> entry : map.entrySet()) {
      SymbolicRoute record = entry.getValue();
      BoolExpr per = record.getPermitted();
      Expr x = m.evaluate(per, false);
      if (x.toString().equals("true")) {
        acc1 = mkOr(acc1, mkNot(per));
      } else {
        acc2 = mkAnd(acc2, mkNot(per));
      }
    }

    // Disable a community value if possible
    for (Map.Entry<LogicalEdge, SymbolicRoute> entry : map.entrySet()) {
      SymbolicRoute record = entry.getValue();
      for (Map.Entry<CommunityVar, BoolExpr> centry : record.getCommunities().entrySet()) {
        BoolExpr comm = centry.getValue();
        Expr x = m.evaluate(comm, false);
        if (x.toString().equals("true")) {
          acc1 = mkOr(acc1, mkNot(comm));
        } else {
          acc2 = mkAnd(acc2, mkNot(comm));
        }
      }
    }

    return mkAnd(acc1, acc2);
  }

  //gives a list where first index is assinged to and second index is referenced to etc....
  //CONVERT TO BOOL EXPRESSIONS!
  public List<Expr> findPredicateAssignment(Expr expr){
    List<Expr> result = new ArrayList<Expr>();
    Expr[] args = expr.getArgs();
    if (expr.isITE() == true){
      result.addAll(findPredicateAssignment(args[1]));
      result.addAll(findPredicateAssignment(args[2]));
    }
    else if (expr.isAnd() == true || expr.isOr() == true){
      for (Expr exp: args){
        result.addAll(findPredicateAssignment(exp));
      }
    }
    else if (expr.isEq() == true){
      if (args[0].getArgs().length > 1) {
        // this should work but just gives an empty when recursing back
        result.addAll(findPredicateAssignment(args[0]));
      }
      else{
        result.add(args[0]);
        if (args[1].getArgs().length > 1){
          result.add(mkTrue());
        }
        else{
          result.add(args[1]);
        }
        return result;
      }
    }
    else if (expr.isImplies() == true) {
      result.addAll(findPredicateAssignment(args[0]));
      result.addAll(findPredicateAssignment(args[1]));
    }
    return result;
  }

  /**
   * Compute variable to predicate mappings required for computing slices.
   * @param assignedTo
   * @param referencedTo
   * @param predicatesToExprMap
   */
  public void computeVarAssignmentsReferences(Map<Expr, List<String>> assignedTo,
      Map<String, List<Expr>> referencedTo, Map<String, BoolExpr> predicatesToExprMap){
    for (Map.Entry<String,BoolExpr> entry : predicatesToExprMap.entrySet()) {
      String key = entry.getKey();
      BoolExpr value = entry.getValue();

      List<Expr> assignments = findPredicateAssignment(value);
      for (int i =0; i<assignments.size(); i+=2){
        Expr assigned = assignments.get(i);
        Expr referenced = assignments.get(i+1);
        if (!assignedTo.containsKey(assigned)){
          List<String> output = new ArrayList<>();
          output.add(key);
          assignedTo.put(assigned,output);
        }
        else{
          List<String> temp = assignedTo.get(assigned);
          temp.add(key);
          assignedTo.put(assigned,temp);
        }
        if (!referencedTo.containsKey(key)){
          List<Expr> output =
                  new ArrayList<>();
          output.add(referenced);
          referencedTo.put(key,output);
        }
        else{
          List<Expr> temp = referencedTo.get(key);
          temp.add(referenced);
          referencedTo.put(key,temp);
        }
      }
    }
  }


  /**
   * Print Slices Map ...
   */
  private void printSlicesMap(){
    System.out.println("Printing slices Map");
    List<String> encoderSliceStr = new ArrayList<>();
    List<String> symbolicRouteStr = new ArrayList<>();
    for (EncoderSlice slice: _slices.values()){
      for (String key: slice.slicesMap.keySet()){
        encoderSliceStr.add(key);
      }
      for (SymbolicRoute route: slice._allSymbolicRoutes){
        for (String key: route.slicesMap.keySet()){
          symbolicRouteStr.add(key);
        }
      }
    }
    System.out.println(encoderSliceStr);
    System.out.println(symbolicRouteStr);
  }

  /**
   * Adds solver constraints that need not be varied for fault localization,
   * namely, packet variables.
   * @param counterExampleVariableAssignments values for all variables in model
   */
  private void addPacketConstraints(
      Map<Expr, Expr> counterExampleVariableAssignments){
    SortedSet<BoolExpr> newEqs = new TreeSet<>();

    PredicateLabel label=new PredicateLabel(PredicateLabel.LabelType.PACKET);
    SortedSet<Expr> packetVars = getMainSlice().getSymbolicPacket().getSymbolicPacketVars();
    for (Expr var : packetVars){
      newEqs.add(_ctx.mkEq(var, counterExampleVariableAssignments.get(var)));
    }

    BoolExpr andPacketConstraints = _ctx.mkAnd(newEqs.toArray(new BoolExpr[newEqs.size()]));
    _faultlocUnsatCore.track(_faultlocSolver, _ctx, andPacketConstraints, label);

    if (_settings.getBoolean(ARG_PRINT_SMT)) {
      _ctx.setPrintMode(Z3_ast_print_mode.Z3_PRINT_SMTLIB_FULL);
      System.out.println("PACKET");
      System.out.println(andPacketConstraints.getSExpr());
    }
  }


  /**
   * Adds constraints from a counter-example (satisfying solution) to the solver
   * after each call to solver.check() that returns SATISFIABLE.
   * @param packetModel variables pertaining to packet
   * @param counterExampleVariableAssignments values for all variables in model
   */
  private void addCounterExampleConstraints(Map<String,String> packetModel,
      Map<Expr, Expr> counterExampleVariableAssignments){
    SortedSet<BoolExpr> newEqs = new TreeSet<>();
    PredicateLabel label=new PredicateLabel(LabelType.COUNTEREXAMPLE);
    for (Expr var : counterExampleVariableAssignments.keySet()) {
      if (packetModel.get(var.toString()) == null) {
        newEqs.add(_ctx.mkEq(var, counterExampleVariableAssignments.get(var)));
      }
    }
    BoolExpr andAllEq = _ctx.mkAnd(newEqs.toArray(new BoolExpr[newEqs.size()]));
    _faultlocUnsatCore.track(_faultlocSolver,_ctx, _ctx.mkNot(andAllEq),label);
  }

  /**
   * Gets the names of the predicates in the unsat core
   * @return names of the predicates in the unsat core
   */
  private List<String> getFaultlocUnsatCorePredNames() {
    // Get unsat core
    BoolExpr[] unsatCore = _faultlocSolver.getUnsatCore();
    System.out.printf("Unsat Core Size: %d\n", unsatCore.length);
    // Each predicate in the unsat core is a label, e.g. Pred22
    List<String> predNames = new ArrayList<>();
    for (int i = 0; i < unsatCore.length; i++) {
      predNames.add(unsatCore[i].toString());
    }
    return predNames;
  }

  /**
   * Removes predicates from the solver that do not determine satisfiability
   * to produce a `minimal` UnsatCore. Here, we check if removing a predicate
   * from an unsatisfiable boolean formula makes it satisfiable and append it
   * to a growing UnsatCore if this change makes the formula satisfiable.
   *
   */
  private List<String> minimizeUnsatCore(List<String> unsatCoreNames, Map<String, BoolExpr> predNameToExpr){

    // Add all expressions in unsat core to minimization solver
    long start_mini=System.currentTimeMillis();
    Solver minSolver = _ctx.mkSolver();
    BoolExpr[] unsatCoreExprs = new BoolExpr[unsatCoreNames.size()];
    for (int i = 0; i < unsatCoreNames.size(); i++) {
      unsatCoreExprs[i] = mkNot(predNameToExpr.get(unsatCoreNames.get(i)));
    }
    minSolver.add(unsatCoreExprs);

    // Remove one expression at a time to determine the minimal unsat core
    List<String> minimalCore = new ArrayList<>();
    for (int j = 0; j < unsatCoreExprs.length; j++){
      BoolExpr currentPred =  unsatCoreExprs[j];
      unsatCoreExprs[j] = mkTrue(); //Equivalent of removing the assertion
      minSolver.reset();
      minSolver.add(unsatCoreExprs);
      if (minSolver.check() == Status.SATISFIABLE) {
        unsatCoreExprs[j] = currentPred;
        minimalCore.add(unsatCoreNames.get(j));
      }
    }
    return minimalCore;
  }


  public Set<String> computeBackwardSlice(Collection<String> unsatCore,
      Map<Expr, List<String>> assignedTo, Map<String, List<Expr>> referencedTo){
    long start_slice=System.currentTimeMillis();
    List<String> worklist = new ArrayList<String>(unsatCore);


    Set<String> processed = new HashSet<String>();
    while (worklist.size() > 0){
      String currentPred = worklist.remove(0);
      if (!processed.contains(currentPred)){
        try{
          List<Expr> refVars = referencedTo.get(currentPred);
          for (Expr exp: refVars){
            for (String pred: assignedTo.get(exp)){
              if (!processed.contains(pred)){
                worklist.add(pred);
              }
            }
          }
        }
        catch (Exception e){} // for assigned values that aren't expressions
        processed.add(currentPred);
      }
    }
    long time_slice=System.currentTimeMillis()-start_slice;
    _faultlocStats.setFirstCEGenTime(time_slice);
    return processed;
  }

  /**
   * Load the faultloc file and throw exception if faultloc file is not in the directory
   *
   * @return HashMap<String,ArrayList<PredicateLabel>> A hashmap with questions as keys and
   * coresponding list of PredicateLabels as value
   */
  public HashMap<String,ArrayList<PredicateLabel>> loadFaultloc(){
    // find the path of the faultloc file
    Path filepath = _batfish.getTestrigPath()
            .resolve(BfConsts.RELPATH_INPUT).resolve("faultloc");
    File file = filepath.toFile();
    // check whether faultloc file exists, if not, throw an exception
    HashMap<String,ArrayList<PredicateLabel>> result= new HashMap<>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(file));
      String s=null;
      String question=null;
      String[] arr = null;
      ArrayList<PredicateLabel> label_list=new ArrayList<>();
      //go through the faultloc file and turns each line into a PredicateLabel and associated with the corespoding question
      while ((s=br.readLine())!=null) {
        if (!s.contains(" ")) {
          if (question!=null) {
            result.put(question, label_list);
          }
          question=s;
          label_list=new ArrayList<>();
        }
        if (s.contains(" ")) {
          arr = s.split(" ");
          try {
            PredicateLabel.LabelType type = 
                PredicateLabel.LabelType.valueOf(arr[0]);
            String router = arr[1];
            Configuration conf = _graph.getConfigurations().get(router);
            // Determine whether an interface of filter is specified
            Interface iface = null;
            String filter = null;
            if (!arr[2].equals("null")) {
                if (conf.getAllInterfaces().containsKey(arr[2])) {
                    iface = conf.getAllInterfaces().get(arr[2]);
                } else {
                    filter = arr[2];
                }
            }
            Protocol proto = Protocol.fromString(arr[3]);
            Boolean omission = null;
            if (arr.length == 5) {
                if (arr[4].equals("OMISSION")) {
                    omission = true;
                }
                else if (arr[4].equals("COMISSION")) {
                    omission = false;
                }
            }
            PredicateLabel label = null;
            if (iface != null || proto != null) {
                label = new PredicateLabel(type, router, iface, proto, omission);
            } else {
                label = new PredicateLabel(type, router, filter, omission);
            }
            label_list.add(label);
          }
          catch (Exception e) {
            System.out.println("WARNING: Invalid faultloc line: " + s);
          }
        }
        result.put(question, label_list);
      }
    br.close();
    }catch (IOException e) {
      System.out.println ("WARNING: Faultloc file not found");
    }
    return result;
  }
  /**
   * Load the possible bgp edges and turn them into a list of predicateLabels
   *
   * @return List<PredicateLabel> represents the possilbe LabelType generated from the possible bgp edges
   * but not actually exist
   */
  public List<PredicateLabel> edgeToLabel(){
    List<GraphEdge> possible=_graph.getPossible();
    List<PredicateLabel> result=new ArrayList<>();
    for (GraphEdge e: possible) {
      PredicateLabel label1=new PredicateLabel(LabelType.EXPORT,e.getRouter(),e.getStart(),Protocol.BGP);
      PredicateLabel label2=new PredicateLabel(LabelType.IMPORT,e.getPeer(),e.getEnd(),Protocol.BGP);
      result.add(label1);
      result.add(label2);
    }
    return result;

  }


  /**
   * Checks that a property is always true by seeing if the encoding is unsatisfiable. If the model
   * is satisfiable, then there is a counter example to the property.
   *
   * @return A VerificationResult indicating the status of the check.
   */
  public Tuple<VerificationResult, Model> verify() {

    int numVariables = _allVariables.size();
    int numConstraints = _solver.getAssertions().length;
    EncoderSlice mainSlice = _slices.get(MAIN_SLICE_NAME);
    int numNodes = mainSlice.getGraph().getConfigurations().size();
    int numEdges = 0;
    for (Map.Entry<String, Set<String>> e : mainSlice.getGraph().getNeighbors().entrySet()) {
      numEdges += e.getValue().size();
    }

    if (_settings.getBoolean(ARG_PRINT_GRAPH)) {
      System.out.println("\nGRAPH");
      System.out.println("-------------------------------------------");
      System.out.println(mainSlice.getGraph());
      System.out.println("-------------------------------------------");
    }

    if (_settings.getBoolean(ARG_PRINT_SMT)) {
        System.out.printf("\nSMT (%d constraints, %d tracked)\n",
                numConstraints, _unsatCore.getTrackingVars().size());
        System.out.println("-------------------------------------------");
        _ctx.setPrintMode(Z3_ast_print_mode.Z3_PRINT_SMTLIB_FULL);
        for (String predName : _unsatCore.getTrackingVars().keySet()) {
            System.out.println(_unsatCore.getTrackingLabels().get(predName));
            System.out.println(
                    _unsatCore.getTrackingVars().get(predName).getSExpr());
        }
      System.out.println("-------------------------------------------");
    }


    long start = System.currentTimeMillis();
    Status status = _solver.check();
    long time = System.currentTimeMillis() - start;

    VerificationStats stats = null;
    if (_question.getBenchmark()) {
      stats = new VerificationStats();
      stats.setAvgNumNodes(numNodes);
      stats.setMaxNumNodes(numNodes);
      stats.setMinNumNodes(numNodes);
      stats.setAvgNumEdges(numEdges);
      stats.setMaxNumEdges(numEdges);
      stats.setMinNumEdges(numEdges);
      stats.setAvgNumVariables(numVariables);
      stats.setMaxNumVariables(numVariables);
      stats.setMinNumVariables(numVariables);
      stats.setAvgNumConstraints(numConstraints);
      stats.setMaxNumConstraints(numConstraints);
      stats.setMinNumConstraints(numConstraints);
      stats.setAvgSolverTime(time);
      stats.setMaxSolverTime(time);
      stats.setMinSolverTime(time);
    }

    if (status == Status.UNSATISFIABLE) {
      VerificationResult res = new VerificationResult(true, null, null, null, null, null, stats);
      return new Tuple<>(res, null);
    } else if (status == Status.UNKNOWN) {
      throw new BatfishException("ERROR: satisfiability unknown");
    } else {
      VerificationResult result;
      Model m;
      while (true) {
        m = _solver.getModel();
        SortedMap<String, String> model = new TreeMap<>();
        SortedMap<String, String> packetModel = new TreeMap<>();
        SortedSet<String> fwdModel = new TreeSet<>();
        SortedMap<String, SortedMap<String, String>> envModel = new TreeMap<>();
        SortedMap<String, ArithExpr> failures = new TreeMap<>();
        SortedMap<String, ArithExpr> nonfailures = new TreeMap<>();
        buildCounterExample(this, m, model, packetModel, fwdModel, envModel, failures, nonfailures, new TreeMap<>());

        if (_previousEncoder != null) {
          buildCounterExample(
              _previousEncoder, m, model, packetModel, fwdModel, envModel, failures, nonfailures, new TreeMap<>());
        }
        SortedSet<String> failuresStringSet = new TreeSet<>(failures.keySet());
        result =
            new VerificationResult(false, null, packetModel, envModel, fwdModel, failuresStringSet, stats);

        if (!_settings.getBoolean(ARG_NO_FAULTLOC)) {
            // Localize faults
            localizeFaults();
            _faultlocStats.writeOut();
        }
        if (_settings.getBoolean(ARG_SAVE_ALL_REFERENCES)){
          //for each predicateLabel in the problem
          Set<PredicateLabel> predicates =  new HashSet<>(_unsatCore.getTrackingLabels().values());
          saveReferences(predicates);
        }

        if (!_question.getMinimize()) {
          break;
        }

        BoolExpr blocking = environmentBlockingClause(m);
        add(blocking, new PredicateLabel(LabelType.ENVIRONMENT));

        Status s = _solver.check();
        if (s == Status.UNSATISFIABLE) {
          break;
        }
        if (s == Status.UNKNOWN) {
          throw new BatfishException("ERROR: satisfiability unknown");
        }
      }

      return new Tuple<>(result, m);
    }
  }

  void saveReferences (Set<PredicateLabel> predicates){
    Path filePath = _batfish.getTestrigPath()
            .resolve(BfConsts.RELPATH_OUTPUT)
            .resolve(REFERENCE_FILE_NAME);

    JSONObject root = new JSONObject();
    for (PredicateLabel pred : predicates){
      List<PredicateLabel.ConfigurationReference> refs = pred.getConfigurationRefs();
      if (refs.size()!=0){
        JSONArray refsArray = new JSONArray();
        for (PredicateLabel.ConfigurationReference ref : refs){
          refsArray.put(ref.toString());
        }
        try{
          root.put(pred.toString(), refsArray);
        }catch (JSONException jse){
          System.err.println("JSON saving failed");
          System.exit(1);
        }
      }
    }
    try {
      System.out.printf("Saving configuration references to %s\n", filePath.toString());
      FileWriter fileWriter = new FileWriter(filePath.toFile());
      fileWriter.write(root.toString());
      fileWriter.close();
    }catch (IOException ioe){
      System.err.println("FAILED to write json file");
      System.exit(1);
    }


  }

  boolean checkForRepeatedfailedEdges(Map<Integer, Map<String, String>> failedEdgesCE,
                                      int numCounterExamples){
    for (int i =2; i<=numCounterExamples;i++){
      Map<String, String> pivotSet = failedEdgesCE.get(i);
      for (int j = 1;j<i;j++){
        Map<String, String> checkAgainstSet = failedEdgesCE.get(j);
        boolean repeated = true;
        for (String varName: pivotSet.keySet()){
          if (checkAgainstSet.containsKey(varName)){
            if (!checkAgainstSet.get(varName).equals(pivotSet.get(varName))){
              repeated= false;
              break;
            }
          }else{
            repeated= false;
            break;
          }
        }
        if (repeated){
          return true;
        }
      }
    }

    return false;
  }


  /**
   * For each failure set check if (N && P && failureSet) is satisfiable.
   * Expected behavior : Since all failureSets passed into the method are obtained from
   * counter examples on (not P) assertion, each check should return unsatifiable.
   * @param failureSets Set of failureSets.
   */
  void localizeForAllFailures(HashMap<SortedMap<String, ArithExpr>,SortedMap<String, ArithExpr>> failureSets){

    int totalNumMUSesGenerated = 0;
    int totalNumMCSesGenerated = 0;
    long time_start = System.currentTimeMillis();
    Set<PredicateLabel> candidatePredicateLabels = new HashSet<>();

    int maxMUScount = _settings.getInt(ARG_MAX_MUS_COUNT);
    int maxMSScount = _settings.getInt(ARG_MAX_MSS_COUNT);
    int maxMarcoTime = _settings.getInt(ARG_MAX_MARCO_TIME);
    boolean marcoVerbose = _settings.getBoolean(ARG_MARCO_VERBOSE);

    Map<String, BoolExpr> predicates = new HashMap<>();

    Map<String, PredicateLabel> predNameToLabelsMap = _faultlocUnsatCore.getTrackingLabels();
    Map<String, BoolExpr> predNameToExprMap = _faultlocUnsatCore.getTrackingVars();

    for (String key : predNameToLabelsMap.keySet()){
      PredicateLabel predLabel = predNameToLabelsMap.get(key);
      if (!predLabel.getLabelType().equals(LabelType.COUNTEREXAMPLE)) {
        if (predLabel.getLabelType() == PredicateLabel.LabelType.POLICY){
          //re-negate (no negate)
          predicates.put(key,_ctx.mkNot(predNameToExprMap.get(key)));
        }else{
          predicates.put(key,predNameToExprMap.get(key));
        }
      }
    }

    List<BoolExpr> allConstraints;
    List<PredicateLabel> labels;
    List<String> predNames;

    Solver solver;
    long time_end = time_start;
    int failureSetNum = 0;
    boolean shouldReturnMUSes = false;
    for (SortedMap<String, ArithExpr> failureSet : failureSets.keySet()){
      failureSetNum++;

      allConstraints = new ArrayList<>();
      labels = new ArrayList<>();
      predNames = new ArrayList<>();

      System.out.printf("\n********FAILURE SET (%d of %d)***********\n",
              failureSetNum, failureSets.keySet().size());
      for(String key: failureSet.keySet()){
        System.out.println(key + " : " + failureSet.get(key));
      }
      if (failureSet.keySet().size()==0){
        System.out.println("Scenario : No links failing");
      }
      System.out.println("******************************\n");


      solver = _ctx.mkSolver();
      for (String key:  predicates.keySet()){
        // Adding Network and policy (unnegated)
        solver.assertAndTrack(predicates.get(key), _ctx.mkBoolConst(key));
        allConstraints.add(predicates.get(key));
        labels.add(predNameToLabelsMap.get(key));
        predNames.add(key);
      }
      for (String key : failureSet.keySet()){
        BoolExpr failureExpr = mkEq(failureSet.get(key), mkInt(1));
        //Don't assert and track failure constraint
        solver.add(failureExpr);
        allConstraints.add(failureExpr);
        PredicateLabel label = new PredicateLabel(
            PredicateLabel.LabelType.FAILURES, failureExpr.toString());
        labels.add(label);
      }
      for (String key : failureSets.get(failureSet).keySet()){ //Links that do not fail
        BoolExpr failureExpr = mkEq(failureSets.get(failureSet).get(key), mkInt(0));
        //Don't assert and track failure constraint
        solver.add(failureExpr);
        allConstraints.add(failureExpr);
        PredicateLabel label = new PredicateLabel(
            PredicateLabel.LabelType.FAILURES, failureExpr.toString());
        labels.add(label);
      }

      Status result = solver.check();

      if (result==Status.SATISFIABLE){
        System.out.println("Satisfiable with fixed failure set. THIS SHOULD NOT HAPPEN!");
      }else {

        System.out.printf("# of predicates in solver : %d | " +
                        "# of predicates in initial unsat core : %d\n",
                solver.getNumAssertions(),
                solver.getUnsatCore().length);

        System.out.printf( "Running Marco (upto %d MUSes, %d MSSes/MCSes will be produced)\n", maxMUScount, maxMSScount);
        BoolExpr[] constraints = allConstraints.toArray(new BoolExpr[allConstraints.size()]);
        PredicateLabel[] constraintLabels = labels.toArray(new PredicateLabel[labels.size()]);
        List<List<PredicateLabel>> muses;


        muses = MarcoMUS.enumerate(constraints, constraintLabels, _ctx,
                maxMUScount, maxMSScount, maxMarcoTime, marcoVerbose, 
                shouldReturnMUSes, _faultlocStats);
        time_end = System.currentTimeMillis();
        if (1 == failureSetNum) {
            if (shouldReturnMUSes) {
                _faultlocStats.setFailSetMUSGenTime(time_end - time_start);
            } else {
                _faultlocStats.setFailSetMCSGenTime(time_end - time_start);
            }
        }

        Map<PredicateLabel, Integer> predicateFrequency = new HashMap<>();

        Set<Set<PredicateLabel>> musesLabels = new HashSet<>();
        System.out.println("****************");
        for (List<PredicateLabel> mus : muses) {
          for (PredicateLabel label : mus) {
            predicateFrequency.putIfAbsent(label, 0);
            predicateFrequency.replace(label, 
                predicateFrequency.get(label) + 1);
            System.out.println(label);
          }
          Set<PredicateLabel> musLabels = new HashSet<>(mus);
          System.out.println("****************");

        /*  if (marcoVerbose){
            System.out.println("Satsifying solution for MCS");
            Solver satisfier = _ctx.mkSolver();
            for(int i =0;i<allConstraints.size();i++){
              if (!mus.contains(i)){
              satisfier.add(allConstraints.get(i));
              }else{
                System.out.println("MCS CONSTRAINT: "+ allConstraints.get(i));
              }
            }
            satisfier.check();
            Model m = satisfier.getModel();
            List<Expr> allVars = new ArrayList<>(_allVariables.values());
            Collections.sort(allVars);
            for (Expr var: allVars){
              System.out.printf("%s | %s\n", var, m.eval(var, false));
            }
          }*/

          musesLabels.add(musLabels);
        }

        if (_settings.getBoolean(ARG_BULK_SAVE_MUS)){
          saveMUSes(musesLabels);
        }

        if (shouldReturnMUSes) {
            totalNumMUSesGenerated += muses.size();
        } else {
            totalNumMCSesGenerated += muses.size();
        }

        Map<Integer,List<PredicateLabel>> sortedPredicateFrequencies = 
            new TreeMap<>();
        for (Entry<PredicateLabel,Integer> entry : 
                predicateFrequency.entrySet()) {
            candidatePredicateLabels.add(entry.getKey()); //Union of MUSes...TODO : Need to factor rank in.
            if (!sortedPredicateFrequencies.containsKey(entry.getValue())) {
                sortedPredicateFrequencies.put(entry.getValue(), 
                        new ArrayList<PredicateLabel>());
            }
            sortedPredicateFrequencies.get(entry.getValue()).add(
                    entry.getKey());
        }

        for (Entry<Integer,List<PredicateLabel>> entry : 
                sortedPredicateFrequencies.entrySet()) {
            System.out.printf("%d MSSes\n", entry.getKey());
            for (PredicateLabel label : entry.getValue()) {
                System.out.printf("\t%s\n", label.toString());
            }
        }

//        for (PredicateLabel label : predicateFrequency.keySet()) {
//            candidatePredicateLabels.add(label); //Union of MUSes...TODO : Need to factor rank in.
//            //TODO : Add thresholding back (currently removed)
//            System.out.printf("%s appeared in %d MUSes\n", label.toString(), 
//                    predicateFrequency.get(label));
//        }
      }
    }
    System.out.printf("Considering %d candidate predicates\n", candidatePredicateLabels.size());

    HashMap<String, ArrayList<PredicateLabel>> unfound= loadFaultloc(); //Predicates that are at fault
    HashMap<String, ArrayList<PredicateLabel>> faultyPreds= loadFaultloc(); //This is the OG list of faulty preds

    computeExtrapredicates(predNameToLabelsMap, unfound, faultyPreds, candidatePredicateLabels);
    outUnfound(unfound, faultyPreds);
    /* Printing references for faults in configuration*/
    System.out.println("***** Diagnosis Report ******");
    //TODO : Write report (understandable localization to a file for each error).
    for (PredicateLabel faultyPredicate : candidatePredicateLabels) {
        List<PredicateLabel.ConfigurationReference> refs = faultyPredicate.getConfigurationRefs();
        for (PredicateLabel.ConfigurationReference ref : refs) {
          System.out.println(ref.toString());
        }

    }
    _faultlocStats.setTimeElapsedDuringMUSGeneration(time_end - time_start);
    if (shouldReturnMUSes) {
        _faultlocStats.setNumMUSesGenerated(totalNumMUSesGenerated);
    } else {
        _faultlocStats.setNumMCSesGenerated(totalNumMCSesGenerated);
    }
  }


  /**
  * Iteratively produce counterexamples until problem becomes unsat, then employ
  * fault localization algorithm.
  */
  void localizeFaults() {

    long time_check = 0;
    int numCounterexamples = 0;
    // History of values assigned to variables in different (counter)examples
    Map<String, Set<String>> variableHistoryMap = new HashMap<>();
    // Counter Example Number to set of failed edges
    Map<Integer, Map<String, String>> failedEdgesCEMap = new HashMap<>();

    HashMap<SortedMap<String, ArithExpr>,SortedMap<String, ArithExpr>> failureSets = new HashMap<>();

    do {
      // Solve
      long check_start = System.currentTimeMillis();
      Status s = _faultlocSolver.check();
      time_check += System.currentTimeMillis() - check_start;

      if (s == Status.UNKNOWN) {
        throw new BatfishException("ERROR: satisfiability unknown");
      }
      else if (s == Status.UNSATISFIABLE) {
        _faultlocStats.setTimeToUNSAT(time_check);

        System.out.println("\nLOCALIZE FAULTS");
        System.out.println("=====================================================");
        if(_settings.getString(ARG_USE_MARCO)!=null) {
          localizeFaultsUsingMarco();
//        }else if (_settings.getBoolean(ARG_BULK_SAVE_MUS)){
////          saveMUSes(produceMUSes());
//
//        }
        }else {


          //FORALL LOCALIZATION
//          localizeForAllFailures(failureSets); // Pass in list of failedLinkSets

          //TODO : Add option to use the old option of
          // just using a single unsatcore without

          // localizeFaultsUsingUnsat();
        }
        break;
      }
      else if (s == Status.SATISFIABLE) {
        numCounterexamples++;
        SortedMap<String, String> ce = new TreeMap<>();
        SortedMap<String, String> packetModel = new TreeMap<>();
        SortedMap<String, ArithExpr> failures = new TreeMap<>();
        SortedMap<String, ArithExpr> nonfailures = new TreeMap<>();

        SortedMap<Expr, Expr> counterExampleVariableAssignments = new TreeMap<>();
        buildCounterExample(this, _faultlocSolver.getModel(), ce, packetModel, new TreeSet<>(),
            new TreeMap<>(), failures, nonfailures,counterExampleVariableAssignments);

        boolean foundEquivalent = false;
        if (_settings.getBoolean(ARG_SKIP_DUPLICATE_FAILURES)) {
            for (SortedMap<String, ArithExpr> nonfailuresOther :
                    failureSets.values()) {
                boolean exactMatch = true;
                for (String varName : nonfailures.keySet()) {
                if (!(varName.contains("DATA-FORWARDING_")
                    || varName.contains("CONTROL-FORWARDING_"))) {
                    continue;
                }
                if (nonfailures.get(varName) != nonfailuresOther.get(varName)) {
                    exactMatch = false;
                    break;
                }
                }
                if (exactMatch) {
                foundEquivalent = true;
                break;
                }
            }
        }

        if (!foundEquivalent) {
          failureSets.put(failures,nonfailures);
        }

        /* Store variable assignments over multiple counter-examples (satisfying assignments.)*/
        if (numCounterexamples == 1) {
          _faultlocStats.setFirstCEGenTime(System.currentTimeMillis()- check_start);
          addPacketConstraints(counterExampleVariableAssignments);

          //// ***
          //Check if predicates at fault are in the model. //TODO : Check this.UPDATE: This works.
          //Map<String, ArrayList<PredicateLabel>> faultsMap = loadFaultloc();
          //Make sure atleast one of the predicates in fault is present in the model.
          // TODO : Count of num predicates at fault and num predicates found in the model to FaultLocStats.
        }
        updateFailedEdgeMap(numCounterexamples, failedEdgesCEMap, variableHistoryMap, ce);
        addCounterExampleConstraints(packetModel, counterExampleVariableAssignments);
      }
    } while (numCounterexamples < _settings.getInt(ARG_NUM_ITERS_FAULTLOC));
    _faultlocStats.setNumCounterExamples(numCounterexamples);
    System.out.println("Number of Counter Examples generated: " + numCounterexamples);

    if (_settings.getBoolean(ARG_PRINT_COUNTER_EXAMPLE_CHANGES)) {
      ArrayList<String> variableNames = new ArrayList<>(variableHistoryMap.keySet());
      Collections.sort(variableNames);
      System.out.println("Changes in Model Variables");
      for (String variableName : variableNames) {
        System.out.println(
              variableName + " { " + String.join(";", variableHistoryMap.get(variableName)) + " }");
      }
    }

    if (numCounterexamples == _settings.getInt(ARG_NUM_ITERS_FAULTLOC)){
      _faultlocStats.setTimeToUNSAT(time_check); //Never reaches UNSAT
    }
    //FORALL LOCALIZATION
    localizeForAllFailures(failureSets); // Pass in list of failedLinkSets

  }

  void updateFailedEdgeMap(int numCounterexamples,
                           Map<Integer, Map<String, String>> failedEdgesCEMap,
                           Map<String,
                            Set<String>> variableHistoryMap,
                           SortedMap<String, String> ce){
    for (String varName : ce.keySet()) {
      if (numCounterexamples != 1){
      variableHistoryMap.get(varName).add(ce.get(varName));
      if (varName.contains("FAILED-EDGE")){
        if (failedEdgesCEMap.containsKey(numCounterexamples)){
          failedEdgesCEMap.get(numCounterexamples).put(varName, ce.get(varName));
        }else{
          failedEdgesCEMap.put(numCounterexamples, new HashMap<>());
          failedEdgesCEMap.get(numCounterexamples).put(varName, ce.get(varName));
        }
      }
     }
     if (numCounterexamples == 1){
       variableHistoryMap.put(varName, new HashSet<>(Arrays.asList(ce.get(varName))));
     }
    }
  }

  /**
  *compute predicates that not in the unsatCore
  @param predicatesNameToLabelMap
  **/
  List<String> predicatesNotInUnsatCore(Map<String,PredicateLabel> predicatesNameToLabelMap, List<String> unsatCore) {
    List<String> notUnsatCore = new ArrayList<>();
    for (String predicate : predicatesNameToLabelMap.keySet()) {
      PredicateLabel label = predicatesNameToLabelMap.get(predicate);
      if (!unsatCore.contains(predicate)
            && (_settings.getBoolean(ARG_NO_FILTER_UNSAT_CORE) || label.isConfigurable()
                || (_settings.getBoolean(ARG_INCLUDE_COMPUTABLE) && label.isComputable()))) {
          notUnsatCore.add(predicate);
       }
    }
    return notUnsatCore;
  }

  /**
  printout the slice results
  */
  void outSlice(Map<String, BoolExpr> predicatesNameToExprMap,
                Map<String, PredicateLabel> predicatesNameToLabelMap,
                Set<String> backwardSlice){
    System.out.println("\nBackward slice:");
    System.out.println("-------------------------------------------");
    for (String predicate : backwardSlice) {
      // Only output constraints we can change
      PredicateLabel label = predicatesNameToLabelMap.get(predicate);
      if (_settings.getBoolean(ARG_NO_FILTER_UNSAT_CORE)
              || label.isConfigurable() || label.isComputable()) {
          if (_settings.getBoolean(ARG_PRINT_UNSAT_EXPR)) {
            System.out.println(label + ": "
                    + predicatesNameToExprMap.get(predicate));
          } else {
            System.out.println(label);
          }
      }
    }
    System.out.println("-------------------------------------------");
  }

  /**
   * Use a single unsat core to localize faults
   */
  void localizeFaultsUsingUnsat() {
    Map<String, BoolExpr> predicatesNameToExprMap = _unsatCore.getTrackingVars();
    Map<String, PredicateLabel> predicatesNameToLabelMap = _unsatCore.getTrackingLabels();

    HashMap<String, ArrayList<PredicateLabel>> unfound= loadFaultloc();
    HashMap<String, ArrayList<PredicateLabel>> Faultloc= loadFaultloc();


    // Get unsat core
    List<String> unsatCore = this.getFaultlocUnsatCorePredNames();

    // Minimize unsat core, if requested
    if (_settings.getBoolean(ARG_MINIMIZE_UNSAT_CORE)) {
      //long start_mini=System.currentTimeMillis();
      unsatCore = minimizeUnsatCore(unsatCore, predicatesNameToExprMap);
      //long time_minimization=System.currentTimeMillis()-start_mini;
      //_faultlocStats.setTimeToUNSAT(time_minimization);
    }

    // Compute predicates in not unsat core
    List<String> notUnsatCore = predicatesNotInUnsatCore(predicatesNameToLabelMap,unsatCore);

    outputCores(unsatCore, notUnsatCore, predicatesNameToExprMap, predicatesNameToLabelMap);
    // Determine whether to use unsat core or not unsat core for fault localization
    // A solution to Minesweeeper's constraints is a counterexample, and the
    // unsat core is the reason a counterexample cannot be found---i.e.,
    // the unsat core contains "good" predicates. Hence, the not unsat
    // core should be used to localize faults.
    Set<String> faultCandidates = new HashSet<>(notUnsatCore);
    // If normal Minesweeper behavior is inverted, then a solution to the constraints
    // is a satisfying example, and the unsat core is the reason no satisfying example
    // can found---i.e., the unsat core contains "bad" predicates.
    if (_settings.getBoolean(ARG_NO_NEGATE_PROPERTY)) {
      faultCandidates = new HashSet<>(unsatCore);
    }


    // If requested, compute a backward slice from all predicates in the (not) unsat
    // core and add everything in the slice to the list of predicates for fault localization.
    if (_settings.getBoolean(ARG_ENABLE_SLICING)) {
      //long start_slice=System.currentTimeMillis();

      // Mapping from variable names to predicates in which a value is assigned to the variable
      Map<Expr, List<String>> assignedTo = new HashMap<>();
      // Mapping from predicates to the variables that are referenced in the predicate
      Map<String, List<Expr>> referencedTo = new HashMap<>();
      // Compute variable/predicate mappings required for computing slices
      computeVarAssignmentsReferences(assignedTo, referencedTo, predicatesNameToExprMap);

      // Compute Slice
      Set<String> backwardSlice = computeBackwardSlice(faultCandidates, assignedTo, referencedTo);
      // long time_slice=System.currentTimeMillis()-start_slice;
      // _faultlocStats.setFirstCEGenTime(time_slice);
      faultCandidates = backwardSlice;
      //print out slice results
      outSlice(predicatesNameToExprMap,predicatesNameToLabelMap,backwardSlice);

    }

    // Determine which LabelType are not found
//    computeExtrapredicates(predicatesNameToLabelMap, unfound, Faultloc, faultCandidates);
//    outUnfound(unfound, Faultloc);

  }

  // Determine which LabelType are not found
  void computeExtrapredicates(Map<String, PredicateLabel> predicatesNameToLabelMap,
                              HashMap<String, ArrayList<PredicateLabel>> unfound,
                              HashMap<String, ArrayList<PredicateLabel>> faultyPredicates,
                              Set<PredicateLabel> candidatePredLabels){
    int extraComputable = 0;
    int extraConfigurable = 0;

//    candidatePredLabels.addAll(edgeToLabel()); //Adds all possible BGP edges? TODO: WHY? THIS WAS A HACK. Added predicates after instead of before

    for (PredicateLabel label:candidatePredLabels) {
      for (String q : faultyPredicates.keySet()) {
        unfound.get(q).remove(label);
        if (!faultyPredicates.get(q).contains(label)) {
          if (label.isComputable())
            extraComputable += 1;
          if (label.isConfigurable())
            extraConfigurable += 1;
        }
      }
    }
    _faultlocStats.setNumExtraConfigPreds(extraConfigurable);
    _faultlocStats.setNumExtraComputePreds(extraComputable);
  }

/*
print out unfound items in Faultloc
*/
  void outUnfound(HashMap<String, ArrayList<PredicateLabel>> missedPredicatesMap,
                  HashMap<String, ArrayList<PredicateLabel>> faultyPredicatesMap){
    int missedCount = 0;
    int foundCount = 0;
    for (String q:faultyPredicatesMap.keySet()) {
      missedCount = missedPredicatesMap.get(q).size();
      foundCount = faultyPredicatesMap.get(q).size()-missedCount;
      System.out.println("\nFaulty predicates for " + q + ": "
              + foundCount + " found, " + missedCount + " missed");
      if (missedCount > 0) {
        System.out.println("Missed faulty predicates for " + q + ":");
        System.out.println("-------------------------------------------");
        for (PredicateLabel label : missedPredicatesMap.get(q)) {
            System.out.println(label);
        }
        System.out.println("-------------------------------------------");
      }

      if (foundCount>0){
        System.out.println("Found faulty predicates for " + q + ":");
        System.out.println("-------------------------------------------");
        for (PredicateLabel label: faultyPredicatesMap.get(q)){
          if (!missedPredicatesMap.get(q).contains(label)){
            System.out.println(label);
          }
        }
        System.out.println("-------------------------------------------");
      }

    }
    _faultlocStats.setNumFoundPreds(foundCount);
    _faultlocStats.setNumUnfoundPreds(missedCount);
    String unfoundpred="";
    for (String q:faultyPredicatesMap.keySet()) {
      for (PredicateLabel label:missedPredicatesMap.get(q))
      unfoundpred+=label.toString()+";";
    }
    _faultlocStats.setUnfoundPreds(unfoundpred);
    System.out.println("=====================================================");
  }


  /**
   * Produce a set of MUSes using Marco
   * @return Set of Sets of PredicateLabels where each set is an MUS.
   */
  Set<Set<PredicateLabel>> produceMUSes(){
    Map<String, BoolExpr> assertionsMap = _faultlocUnsatCore.getTrackingVars();
    Map<String, PredicateLabel> predicateLabelMap = _faultlocUnsatCore.getTrackingLabels();
    BoolExpr[] constraints = new BoolExpr[assertionsMap.size()];
    PredicateLabel[] constraintLabels =
        new PredicateLabel[assertionsMap.size()];
    String[] trackingNames = new String[assertionsMap.size()];

    int i=0;
    for (String key: assertionsMap.keySet()){
      constraints[i] = assertionsMap.get(key);
      constraintLabels[i] = predicateLabelMap.get(key);
      trackingNames[i] = key;
      i++;
    }

    //Use MUSes unless specified to use MSS
    boolean shouldUseMUSes = !_settings.getString(ARG_USE_MARCO).equals("mss");

    long start_time = System.currentTimeMillis();
    List<List<PredicateLabel>> listMUSes = MarcoMUS.enumerate(constraints,
            constraintLabels,
            _ctx,
            _settings.getInt(ARG_MAX_MUS_COUNT),
            _settings.getInt(ARG_MAX_MSS_COUNT),
            _settings.getInt(ARG_MAX_MARCO_TIME),
            _settings.getBoolean(ARG_MARCO_VERBOSE),
            shouldUseMUSes, _faultlocStats);
    long time_elapsed = System.currentTimeMillis() - start_time;
    _faultlocStats.setTimeElapsedDuringMUSGeneration(time_elapsed);

    Set<Set<PredicateLabel>> setMUSes = new HashSet<>();
    for (List<PredicateLabel> mus : listMUSes){
      setMUSes.add(new HashSet<>(mus));
    }

    return setMUSes;

  }

  void saveMUSes(Set<Set<PredicateLabel>> setOfMUSes){
    String timeStamp = new SimpleDateFormat("yyMMddHHmmssSS").format(new Date());
    Path filepath = _batfish.getTestrigPath()
            .resolve(BfConsts.RELPATH_OUTPUT).resolve("mus_bulk_"+timeStamp);
    System.out.printf("Saving MUSes to %s\n", filepath);
    File file = filepath.toFile();
    try {
      FileWriter musWriter = new FileWriter(file, true);
      String policyIdentifier = _question.getSrcIps() + "  ->  " + _question.getDstIps() + "\n";
      musWriter.append(policyIdentifier);
      for (Set<PredicateLabel> mus : setOfMUSes){
        for (PredicateLabel pred : mus){
          musWriter.append(pred.toString());
          musWriter.append(",");
        }
        musWriter.append("\n");
      }
      musWriter.flush();
      musWriter.close();
    }catch(IOException ioe){
      ioe.printStackTrace();
    }

  }

  void localizeFaultsUsingMarco(){
    //Map<String, BoolExpr> assertionsMap = _faultlocUnsatCore.getTrackingVars();
    Map<String, PredicateLabel> predicateLabelMap = _faultlocUnsatCore.getTrackingLabels();
    /*BoolExpr[] constraints = new BoolExpr[assertionsMap.size()];
    String[] trackingNames = new String[assertionsMap.size()];

    int i=0;
    for (String key: assertionsMap.keySet()){
      constraints[i] = assertionsMap.get(key);
      trackingNames[i] = key;
      i++;
    }*/

    //Use MUSes unless specified to use MSS
    boolean shouldUseMUSes = !_settings.getString(ARG_USE_MARCO).equals("mss");

    /*//TODO: Not the actual time elapsed.. But close.
    long start_time = System.currentTimeMillis();
    List<Set<Integer>> candidateSets = MarcoMUS.enumerate(constraints,
            _ctx,
            _settings.getMaxMUSCount(),
            _settings.getMaxMSSCount(),shouldUseMUSes);
    long time_elapsed = System.currentTimeMillis() - start_time;
    _faultlocStats.setTimeElapsedDuringMUSGeneration(time_elapsed);*/

    Set<Set<PredicateLabel>> candidateSets = produceMUSes();

    Set<PredicateLabel> intersection = new HashSet<>();
    Set<PredicateLabel> union = new HashSet<>();
    Map<PredicateLabel, Integer> predicateFrequencies = new HashMap<>();

    int musCount = 1;

    int unionGrowth = 0;
    int intersectionShrink = 0;

    for (Set<PredicateLabel> candidateSet: candidateSets){
      int currentIntersectionSize;
      int currentUnionSize;
      if (musCount==1){
        //Initialize intersection and union with contents of the MUS on the first iteration.
        intersection.addAll(candidateSet);
        union.addAll(candidateSet);

      }else{
        currentIntersectionSize = intersection.size();
        buildSetIntersect(candidateSet, intersection);
        intersectionShrink+=currentIntersectionSize - intersection.size();

        currentUnionSize = union.size();
        buildSetUnion(candidateSet, union);
        unionGrowth += union.size() - currentUnionSize;
      }

      //Computes frequency of each predicate in set of MUSes
      for(PredicateLabel id: candidateSet){
        if (predicateFrequencies.containsKey(id)){
          predicateFrequencies.put(id, predicateFrequencies.get(id)+1);
        }else{
          predicateFrequencies.put(id, 1);
        }
      }
      musCount++;
    }

    System.out.printf("Total Union Growth : %d  | Total Intersection Shrink : %d\n", unionGrowth, intersectionShrink);


	System.out.printf("Number of %s generated : %d\n", shouldUseMUSes?"MUSes":"MSSes", candidateSets.size());
	_faultlocStats.setNumMUSesGenerated(candidateSets.size());
    if (_settings.getBoolean(ARG_MUS_INTERSECT)) {
      //Idea 1 : Compute Not (Intersection of MUSes)

      //Don't compute complement of intersection if noNegate option is used xor msses is being generated.
      if (_settings.getBoolean(ARG_NO_NEGATE_PROPERTY) ^ shouldUseMUSes) {
        Set<PredicateLabel> complementOfIntersection = new HashSet<>();
        complementOfIntersection.addAll(predicateLabelMap.values());

        //Computing complement of Intersection:
        /*for (int intersectionPredId : intersection) {
          complementOfIntersection.remove(predicateLabelMap.get(trackingNames[intersectionPredId]));
        }*/
        complementOfIntersection.removeAll(intersection);


        produceAnalysisForCandidates(complementOfIntersection);
      }else{

        /*Set<PredicateLabel> intersectionPredicates = new HashSet<>();

        for (int intersectionPredId: intersection){
          intersectionPredicates.add(predicateLabelMap.get(trackingNames[intersectionPredId]));
        }*/

        produceAnalysisForCandidates(intersection);
      }

    }else if (_settings.getBoolean(ARG_MUS_UNION)){
      //Idea 2 : Compute Not (union of MUSes)

      //Only compute complement if noNegate option is not used.
      if (_settings.getBoolean(ARG_NO_NEGATE_PROPERTY) ^ shouldUseMUSes) {
        Set<PredicateLabel> complementOfUnion = new HashSet<>();
        complementOfUnion.addAll(predicateLabelMap.values());

        //Computing complement of Union:
        /*for (int unionPredId : union) {
          complementOfUnion.remove(predicateLabelMap.get(trackingNames[unionPredId]));
        }*/
        complementOfUnion.removeAll(union);

        produceAnalysisForCandidates(complementOfUnion);
      }else{
        /*Set<PredicateLabel> unionPredicates = new HashSet<>();

        for (int unionPredId : union){
          unionPredicates.add(predicateLabelMap.get(trackingNames[unionPredId]));
        }*/

        produceAnalysisForCandidates(union);
      }

    }

  }

  /**
   * Given a set of fault candidates, produce analysis of true/false positives and true/false negatives using loadFaultloc().
   * This updates global FaultlocStats object.
   * @param faultCandidates Set of predicate LabelType for predicates at fault
   */
  void produceAnalysisForCandidates(Set<PredicateLabel> faultCandidates){
    HashMap<String, ArrayList<PredicateLabel>> questionToFaultyPredicateLabelsMap = loadFaultloc();

    Set<PredicateLabel> truePositives = new HashSet<>();
    Set<PredicateLabel> falsePositives = new HashSet<>();
    Set<PredicateLabel> falseNegatives = new HashSet<>();

    Set<PredicateLabel> faultyPredicates = new HashSet<>();

    for (String key : questionToFaultyPredicateLabelsMap.keySet()){
      faultyPredicates.addAll(questionToFaultyPredicateLabelsMap.get(key));
      for (PredicateLabel candidate : faultCandidates){
        if(faultyPredicates.contains(candidate)){
          truePositives.add(candidate);
          faultyPredicates.remove(candidate);
        }else{
          falsePositives.add(candidate);
        }
      }
      falseNegatives.addAll(faultyPredicates);
    }

    _faultlocStats.setNumFoundPreds(truePositives.size());
    _faultlocStats.setNumUnfoundPreds(falseNegatives.size());
    _faultlocStats.setNumExtraComputePreds(falsePositives.size()); //TODO:  This is both COMPUTABLE and CONFIGURABLE right now.

    Set<String> falseNegativeString = new HashSet<>();
    for (PredicateLabel predLabel : falseNegatives){
      falseNegativeString.add(predLabel.toString());
    }

    _faultlocStats.setUnfoundPreds(String.join(";",falseNegativeString));

    System.out.printf("\t*Analysis Result*\n True Positives : %d |" +
            "False Negatives : %d | False Positives : %d |\n", truePositives.size(),
            falseNegatives.size(),falsePositives.size());
  }


  /**
   * Compute intersection of two sets.
   * @param mus new Mus to intersect with existing intersection.
   * @param currentIntersection Current Intersection
   */
  void buildSetIntersect(Set<PredicateLabel> mus, Set<PredicateLabel> currentIntersection){
    //TODO: Search for a more efficient implementation of set-intersect.
    Iterator<PredicateLabel> iter = currentIntersection.iterator();
    while (iter.hasNext()){
      if (!mus.contains(iter.next())){
        iter.remove();
      }
    }
  }

  /**
   * Compute union of two sets of MUSes.
   * @param mus new Mus to compute union with existing intersection.
   * @param currentUnion Current MUS Union
   */
  void buildSetUnion(Set<PredicateLabel> mus, Set<PredicateLabel> currentUnion){
    for(PredicateLabel newElement: mus){
      currentUnion.add(newElement);
    }
  }

  /**
   * Print and process the produced UnsatCore and the complement for storage.
   * Predicate Labels are written out seperated by commas.
   */
  void outputCores(List<String> unsatCore, List<String> notUnsatCore,
                    Map<String, BoolExpr> predicatesNameToExprMap,
                    Map<String, PredicateLabel> predicatesNameToLabelMap){
    Set<String> notUnsatLabels = new HashSet<>();
    Set<String> unsatLabels = new HashSet<>();

    // Display not unsat core
    System.out.println("\nNot Unsat Core:");
    System.out.println("-------------------------------------------");
    for (String predicate : notUnsatCore) {
      PredicateLabel label = predicatesNameToLabelMap.get(predicate);
      notUnsatLabels.add(label.toString());
      if (_settings.getBoolean(ARG_PRINT_UNSAT_EXPR)) {
        System.out.println(label + ": "
                + predicatesNameToExprMap.get(predicate));
      } else {
        System.out.println(label);
      }
    }
    System.out.println("-------------------------------------------");

    // Display unsat core
    System.out.println("\nUnsat Core:");
    System.out.println("-------------------------------------------");
    for (String predicate : unsatCore) {
      PredicateLabel label = predicatesNameToLabelMap.get(predicate);
      unsatLabels.add(label.toString());
      if (_settings.getBoolean(ARG_PRINT_UNSAT_EXPR)) {
        System.out.println(label + ": "
                + predicatesNameToExprMap.get(predicate));
      } else {
        System.out.println(label);
      }
    }
    System.out.println("-------------------------------------------");

    // Write Out UnsatCore to unsat.out && NotUnsatCore to not_unsat.out
    Path unsatCorePath = _batfish.getTestrigPath()
            .resolve(BfConsts.RELPATH_OUTPUT)
            .resolve(_settings.getBoolean(ARG_NO_NEGATE_PROPERTY)?
                    UNSATCORE_OUT_FILE_NO_NEGATE:UNSATCORE_OUT_FILE);

    Path notUnsatCorePath = _batfish.getTestrigPath()
            .resolve(BfConsts.RELPATH_OUTPUT)
            .resolve(_settings.getBoolean(ARG_NO_NEGATE_PROPERTY)?
                    NOT_UNSATCORE_OUT_FILE_NO_NEGATE:NOT_UNSATCORE_OUT_FILE);
    try{
      FileWriter coreWriter = new FileWriter(unsatCorePath.toFile(), true);
      coreWriter.append(String.join(",", unsatLabels));
      coreWriter.append(",");
      coreWriter.flush();
      coreWriter.close();
      coreWriter = new FileWriter(notUnsatCorePath.toFile(), true);
      coreWriter.append(String.join(",", notUnsatLabels));
      coreWriter.append(",");
      coreWriter.flush();
      coreWriter.close();
    }catch(IOException ioe){
      ioe.printStackTrace();
    }
  }

  /**
   * Adds all the constraints to capture the interactions of messages among all protocols in the
   * network. This should be called prior to calling the <b>verify method</b>
   */
  void computeEncoding() {

    if (_graph.hasStaticRouteWithDynamicNextHop()) {
      throw new BatfishException(
              "Cannot encode a network that has a static route with a dynamic next hop");
    }
    addFailedLinkConstraints(_question.getFailures());
    addFailedNodeConstraints(_question.getNodeFailures());
    getMainSlice().computeEncoding();
    for (Entry<String, EncoderSlice> entry : _slices.entrySet()) {
      String name = entry.getKey();
      EncoderSlice slice = entry.getValue();
      if (!name.equals(MAIN_SLICE_NAME)) {
        slice.computeEncoding();
      }
    }


  }

  /*
   * Getters and setters
   */

  SymbolicFailures getSymbolicFailures() {
    return _symbolicFailures;
  }

  EncoderSlice getSlice(String router) {
    String s = "SLICE-" + router + "_";
    return _slices.get(s);
  }

  public Context getCtx() {
    return _ctx;
  }

  EncoderSlice getMainSlice() {
    return _slices.get(MAIN_SLICE_NAME);
  }

  Solver getSolver() {
    return _solver;
  }

  Map<String, Expr> getAllVariables() {
    return _allVariables;
  }

  int getId() {
    return _encodingId;
  }

  boolean getModelIgp() {
    return _modelIgp;
  }

  Map<String, Map<String, BoolExpr>> getSliceReachability() {
    return _sliceReachability;
  }

  UnsatCore getUnsatCore() {
    return _unsatCore;
  }

  int getFailures() {
    return _question.getFailures();
  }

  public boolean getFullModel() {
    return _question.getFullModel();
  }

  private Map<String, EncoderSlice> getSlices() {
    return _slices;
  }

  HeaderQuestion getQuestion() {
    return _question;
  }

  public void setQuestion(HeaderQuestion question) {
    this._question = question;
  }

  public BitVecExpr mkBV(long val, int size) {
    return _ctx.mkBV(val, size);
  }

  public BitVecExpr mkBVAND(BitVecExpr expr, BitVecExpr mask) {
    return _ctx.mkBVAND(expr, mask);
  }

  public boolean shouldSplitITEs() {
      return _settings.getBoolean(ARG_SPLIT_ITE);
  }
}
