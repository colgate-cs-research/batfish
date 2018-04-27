package org.batfish.symbolic.smt;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Tactic;
import com.microsoft.z3.AST;

import java.util.*;
import java.util.Map.Entry;
import javax.annotation.Nullable;

import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.config.Settings;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.questions.smt.HeaderQuestion;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.OspfType;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.utils.Tuple;

/**
 * A class responsible for building a symbolic encoding of the entire network. The encoder does this
 * by maintaining a collection of encoding slices, where each slice encodes the forwarding behavior
 * for a particular packet. >
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
  private static final boolean ENABLE_UNSAT_CORE = true;
  private static final Boolean ENABLE_BENCHMARKING = false;
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

  private Settings _settings;

  private Integer _numIters;

  private boolean _shouldInvertFormula;

  private boolean _shouldPrintUnsatCore;

  private boolean _shouldPrintCounterExampleChanges;

  /**
   * Create an encoder object that will consider all packets in the provided headerspace.
   *
   * @param settings The Batfish configuration settings object
   * @param graph The network graph
   */
  Encoder(Settings settings, Graph graph, HeaderQuestion q) {
    this(settings, null, graph, q, null, null, null, 0);
  }

  /**
   * Create an encoder object from an existing encoder.
   *
   * @param e An existing encoder object
   * @param g An existing network graph
   */
  Encoder(Encoder e, Graph g) {
    this(
        e._settings,
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
    this(e._settings, e, g, q, e.getCtx(), e.getSolver(), e.getAllVariables(), e.getId() + 1);
  }

  /**
   * Create an encoder object while possibly reusing the partial encoding of another encoder. mkIf
   * the context and solver are null, then a new encoder is created. Otherwise the old encoder is
   * used.
   */
  private Encoder(
      Settings settings,
      @Nullable Encoder enc,
      Graph graph,
      HeaderQuestion q,
      @Nullable Context ctx,
      @Nullable Solver solver,
      @Nullable Map<String, Expr> vars,
      int id) {
    _settings = settings;
    _graph = graph;
    _previousEncoder = enc;
    _modelIgp = true;
    _encodingId = id;
    _question = q;
    _slices = new HashMap<>();
    _sliceReachability = new HashMap<>();
    _numIters = _settings.getNumIters();
    _shouldInvertFormula =_settings.shouldInvertSatFormula();
    _shouldPrintUnsatCore = _settings.shouldPrintUnsatCore();
    _shouldPrintCounterExampleChanges = _settings.shouldPrintCounterExampleDiffs();


    HashMap<String, String> cfg = new HashMap<>();

    // allows for unsat core when debugging
    if (ENABLE_UNSAT_CORE) {
      cfg.put("proof", "true");
      cfg.put("auto-config", "false");
    }

    cfg.put("timeout", String.valueOf(_settings.getZ3timeout()));

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
        // System.out.println("Help: \n" + _solver.getHelp());
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

    _unsatCore = new UnsatCore(ENABLE_UNSAT_CORE);

    initFailedLinkVariables();
    initSlices(_question.getHeaderSpace(), graph);
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
      Set<String> peers = entry.getValue();
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
      SortedSet<Pair<String, Ip>> ibgpRouters = new TreeSet<>();

      for (Entry<GraphEdge, BgpNeighbor> entry : g.getIbgpNeighbors().entrySet()) {
        GraphEdge ge = entry.getKey();
        BgpNeighbor n = entry.getValue();
        String router = ge.getRouter();
        Ip ip = n.getLocalIp();
        Pair<String, Ip> pair = new Pair<>(router, ip);

        // Add one slice per (router, source ip) pair
        if (!ibgpRouters.contains(pair)) {

          ibgpRouters.add(pair);

          // Create a control plane slice only for this ip
          HeaderSpace hs = new HeaderSpace();

          // Make sure messages are sent to this destination IP
          SortedSet<IpWildcard> ips = new TreeSet<>();
          ips.add(new IpWildcard(n.getLocalIp()));
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
          javafx.util.Pair<Map<String, ArithExpr>, Map<String, BoolExpr>> reachVars = pa.instrumentReachability(router);
          _sliceReachability.put(router, reachVars.getValue());
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
    return getCtx().mkOr(vals);
  }

  // Symbolic boolean implication
  BoolExpr mkImplies(BoolExpr e1, BoolExpr e2) {
    return getCtx().mkImplies(e1, e2);
  }

  // Symbolic boolean conjunction
  BoolExpr mkAnd(BoolExpr... vals) {
    return getCtx().mkAnd(vals);
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

  // Add a boolean variable to the model
  void add(BoolExpr e) {
    _unsatCore.track(_solver, _ctx, e);
  }

  void add(BoolExpr e, String caller) {
    _unsatCore.track(_solver, _ctx, e, caller);
  }

  /*
   * Adds the constraint that at most k links have failed.
   * This is done in two steps. First we ensure that each link
   * variable is constrained to take on a value between 0 and 1:
   *
   * 0 <= link_i <= 1
   *
   * Then we ensure that the sum of all links is never more than k:
   *
   * link_1 + link_2 + ... + link_n <= k
   */
  private void addFailedConstraints(int k) {
    Set<ArithExpr> vars = new HashSet<>();
    getSymbolicFailures().getFailedInternalLinks().forEach((router, peer, var) -> vars.add(var));
    getSymbolicFailures().getFailedEdgeLinks().forEach((ge, var) -> vars.add(var));

    ArithExpr sum = mkInt(0);
    for (ArithExpr var : vars) {
      sum = mkSum(sum, var);
      add(mkGe(var, mkInt(0)), UnsatCore.FAILED);
      add(mkLe(var, mkInt(1)), UnsatCore.FAILED);
    }
    if (k == 0) {
      for (ArithExpr var : vars) {
        add(mkEq(var, mkInt(0)), UnsatCore.FAILED);
      }
    } else {
      add(mkLe(sum, mkInt(k)), UnsatCore.FAILED);
    }
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
  private HashMap<String, String> buildCounterExample(
      Encoder enc,
      Model m,
      SortedMap<String, String> model,
      SortedMap<String, String> packetModel,
      SortedSet<String> fwdModel,
      SortedMap<String, SortedMap<String, String>> envModel,
      SortedSet<String> failures,
      SortedMap<Expr, Expr> additionalConstraints,
      SortedMap<Expr, Expr> staticConstraints) {

    SortedMap<Expr, String> valuation = new TreeMap<>();
    HashMap<String, String> counterExampleState = new HashMap<>();
    // If user asks for the full model
    for (Entry<String, Expr> entry : _allVariables.entrySet()) {

      String name = entry.getKey();
      Expr e = entry.getValue();
      Expr val = m.evaluate(e, true);
      if (!val.equals(e)) { // excluding constants in the model.
        String s = val.toString();
        if (_question.getFullModel()) {
          model.put(name, s);
        }
        valuation.put(e, s);
        counterExampleState.put(name, s);
        if (name.contains("reachable-id")){ //this fixes issue with bgp-acls
          staticConstraints.put(e, val);
        }else{
          additionalConstraints.put(e, val);
        }
      }
    }



    ArrayList<String> sortedKeys = new ArrayList<String>(counterExampleState.keySet());
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

    Ip dip = new Ip(Long.parseLong(dstIp));
    Ip sip = new Ip(Long.parseLong(srcIp));

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
              Prefix p1 = new Prefix(dip, len);
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
              String s = cvar.getValue();
              String t = slice.getNamedCommunities().get(cvar.getValue());
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
              if ("1".equals(s)) {
                String pair = (x.compareTo(y) < 0 ? x + "," + y : y + "," + x);
                failures.add("link(" + pair + ")");
              }
            });

    _symbolicFailures
        .getFailedEdgeLinks()
        .forEach(
            (ge, e) -> {
              String s = valuation.get(e);
              if ("1".equals(s)) {
                failures.add("link(" + ge.getRouter() + "," + ge.getStart().getName() + ")");
              }
            });

    return counterExampleState;
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

  public void buildPredicateMaps(Map<Expr, List<String>> assignedTo, Map<String, List<Expr>> referencedTo, Map<String, BoolExpr> unsatVarsMap){
    for (Map.Entry<String,BoolExpr> entry : unsatVarsMap.entrySet()) {
      String key = entry.getKey();
      BoolExpr value = entry.getValue();
      //System.out.println("finding predicate assignment");
      //if (key.equals("Pred117")){
      List<Expr> assignments = findPredicateAssignment(value);
      //Arrays.toString(assignments.toArray());
      for (int i =0; i< assignments.size(); i+=2){
        Expr assigned = assignments.get(i);
        Expr referenced = assignments.get(i+1);
        if (!assignedTo.containsKey(assigned)){
          List<String> output = new ArrayList<String>();
          output.add(key);
          assignedTo.put(assigned,output);
        }
        else{
          List<String> temp = assignedTo.get(assigned);
          temp.add(key);
          assignedTo.put(assigned,temp);
        }
        if (!referencedTo.containsKey(key)){
          List<Expr> output = new ArrayList<Expr>();
          output.add(referenced);
          referencedTo.put(key,output);
        }
        else{
          List<Expr> temp = referencedTo.get(key);
          temp.add(referenced);
          referencedTo.put(key,temp);
        }
      }
      //} 
    }
  }
  public void checkPreds(Expr[] unsatCore, Map<Expr, List<String>> assignedTo, Map<String, List<Expr>> referencedTo){
    List<String> worklist = new ArrayList<String>();
    for (Expr exp: unsatCore){
      worklist.add(exp.toString());
    }
    // System.out.println(" Printing worklist");
    // for (String str: worklist){
    //   System.out.print(str + ", ");
    // }
    // System.out.println("");

    List<String> processed = new ArrayList<String>();
    while (worklist.size() > 0){
      String currentPred = worklist.remove(0);
      //System.out.println(currentPred);
      if (!processed.contains(currentPred)){
        //System.out.println("in if conditional");
        try{
          List<Expr> refVars = referencedTo.get(currentPred);
          // for (Expr exp: refVars){
          //   System.out.print(exp.toString() + ", ");
          // }
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
    System.out.println(" Printing processed"); //in one of these processed expressions, this is where something went wrong, FAULT LOCALISATION
    for (String str: processed){
      System.out.print(str + ", ");
    }
    System.out.println("");
  }
  /**
   * Checks that a property is always true by seeing if the encoding is unsatisfiable. mkIf the
   * model is satisfiable, then there is a counter example to the property.
   *
   * @return A VerificationResult indicating the status of the check.
   */
  public Tuple<VerificationResult, Model> verify() {
    //Map<String, BoolExpr> slicesMap = 

    Map<String, BoolExpr> unsatVarsMap = _unsatCore.getTrackingVars();
    System.out.println("printing unsatVars Map");
    Arrays.toString(unsatVarsMap.entrySet().toArray());
    Map<String, String> unsatcoreLabelsMap = _unsatCore.getTrackingLabels();
    EncoderSlice mainSlice = _slices.get(MAIN_SLICE_NAME);
    System.out.println("printing slices Map");
    List<String>EncoderSliceStr = new ArrayList<String>();
    List<String>SymbolicRouteStr = new ArrayList<String>();
    for (EncoderSlice slice: _slices.values()){
      for (String key: slice.slicesMap.keySet()){
        EncoderSliceStr.add(key);
      }
      for (SymbolicRoute route: slice._allSymbolicRoutes){
        for (String key: route.slicesMap.keySet()){
          SymbolicRouteStr.add(key);
        }
      }
    }
    System.out.println(EncoderSliceStr);
    System.out.println(SymbolicRouteStr);
    
    //from list of B see if it gets assignedTo -- B: pred220
    Map<Expr, List<String>>   assignedTo = new HashMap<Expr, List<String>>();

    // this should be predname : list of referenced to, pred221 : List of B
    Map<String, List<Expr>> referencedTo = new HashMap<String, List<Expr>>();

    buildPredicateMaps(assignedTo, referencedTo, unsatVarsMap);
    
    // System.out.println("printing assignedTo map"); //odd entry are assigned to and even entry are referenced to
    // for (Map.Entry<Expr, List<String>> entry : assignedTo.entrySet()) {
    //   Expr k = entry.getKey();
    //   List<String> val = entry.getValue();
    //   System.out.println(k);
    //   for (String str: val){
    //     System.out.print(str + ", ");
    //   }
    //   System.out.println();
    // } 
    // System.out.println("printing referencedTo map"); //odd entry are assigned to and even entry are referenced to
    // for (Map.Entry<String, List<Expr>> entry : referencedTo.entrySet()) {
    //   String k = entry.getKey();
    //   List<Expr> val = entry.getValue();
    //   if (!val.get(0).equals("numerical value")){
    //     System.out.println(k);
    //     for (Expr exp: val){
    //       System.out.print(exp + ", ");
    //     }
    //     System.out.println();
    //   }
    // } 

    int numVariables = _allVariables.size();
    int numConstraints = _solver.getAssertions().length;
    int numNodes = mainSlice.getGraph().getConfigurations().size();
    int numEdges = 0;
    for (Map.Entry<String, Set<String>> e : mainSlice.getGraph().getNeighbors().entrySet()) {
      numEdges += e.getValue().size();
    }

    HashMap<String, Set<String>> variableHistoryMap = new HashMap<>();

    long start = System.currentTimeMillis();

    if (_shouldInvertFormula) { //create a new solver with negated formula
      System.out.println("Inverting boolean formula");
      BoolExpr[] assertions = _solver.getAssertions();
      BoolExpr negFormula = _ctx.mkAnd(assertions);
      negFormula = _ctx.mkNot(negFormula);
      _solver.reset();
      _solver.add(negFormula);
    }
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

      Model m =null;
      int numCounterexamples = 0;

      SortedSet<Expr> staticVars =
              this.getMainSlice()
                      .getSymbolicPacket()
                      .getSymbolicPacketVars(); // should be added to solver during the first iteration.


      SortedSet<BoolExpr> newEqs = new TreeSet<BoolExpr>();


      BoolExpr andPcktVars = mkTrue();
      do {
        m = _solver.getModel();
        SortedMap<String, String> model = new TreeMap<>();
        SortedMap<String, String> packetModel = new TreeMap<>();
        SortedSet<String> fwdModel = new TreeSet<>();
        SortedMap<String, SortedMap<String, String>> envModel = new TreeMap<>();
        SortedSet<String> failures = new TreeSet<>();
        SortedMap<Expr, Expr> additionalConstraints = new TreeMap<>();
        SortedMap<Expr, Expr> moreStaticConstraints = new TreeMap<>();
        HashMap<String, String> ce =
            buildCounterExample(
                this, m, model, packetModel, fwdModel, envModel, failures, additionalConstraints,moreStaticConstraints);
        if (numCounterexamples == 0) {
          for (String key : ce.keySet()) {
            // first values assigned to counter-example variables...
            variableHistoryMap.put(key, new HashSet<>(Arrays.asList(ce.get(key))));
          }
        } else {
          for (String varName : ce.keySet()) {
            variableHistoryMap.get(varName).add(ce.get(varName));
          }
        }
        if (_previousEncoder != null) {
          ce =
              buildCounterExample(
                  _previousEncoder,
                  m,
                  model,
                  packetModel,
                  fwdModel,
                  envModel,
                  failures,
                  additionalConstraints,
                  moreStaticConstraints);
          for (String varName : ce.keySet()) {
            variableHistoryMap.get(varName).add(ce.get(varName));
          }
        }

        result =
            new VerificationResult(false, model, packetModel, envModel, fwdModel, failures, stats);
        // TODO: need to print each result information.. Need to go from a VerificationResult object
        // to an AnswerElement object in order get the summary
        numCounterexamples++;

        // Generate multiple counter examples
        if (_numIters > 1) {


          // this is the counterexample. the 15 data plane packet variables need to be set
          if (numCounterexamples == 1) {
            staticVars.addAll(moreStaticConstraints.keySet());
            for (Expr e : staticVars) {
              if (additionalConstraints.containsKey(e)) {
                newEqs.add(_ctx.mkEq(e, additionalConstraints.get(e)));
              }else{
                newEqs.add(_ctx.mkEq(e, moreStaticConstraints.get(e)));
              }
            }

            andPcktVars = _ctx.mkAnd(newEqs.toArray(new BoolExpr[newEqs.size()]));
            _unsatCore.track(_solver, _ctx,andPcktVars, "Packet Variables");
          }

          newEqs.clear();
          for (Expr var : additionalConstraints.keySet()) {
            if (!staticVars.contains(var))
              newEqs.add(_ctx.mkEq(var, additionalConstraints.get(var)));
          }
          BoolExpr andAllEq = _ctx.mkAnd(newEqs.toArray(new BoolExpr[newEqs.size()]));
          _unsatCore.track(_solver,_ctx, _ctx.mkNot(andAllEq),"counterexample constraint");
        }

        // Find the smallest possible counterexample
        if (_question.getMinimize()) {
          BoolExpr blocking = environmentBlockingClause(m);
          add(blocking, UnsatCore.ENVIRONMENT);
        }

        if (_shouldInvertFormula) { //create a new solver with negated formula
          System.out.println("Inverting boolean formula (in loop)");
          BoolExpr[] assertions = _solver.getAssertions();
          BoolExpr negFormula = _ctx.mkAnd(assertions);
          negFormula = _ctx.mkNot(negFormula);
          _solver.reset();
          _solver.add(negFormula);
        }

        Set<BoolExpr> coll = new HashSet<BoolExpr>();
        coll.addAll(_unsatCore.getTrackingVars().values());

        Status s = _solver.check();
        if (s == Status.UNSATISFIABLE) {
          Solver nSolver = _ctx.mkSolver();


          BoolExpr[] solverAssertions = _solver.getUnsatCore();
          for (int i =0;i<solverAssertions.length;i++){
            solverAssertions[i] = mkNot(unsatVarsMap.get(solverAssertions[i].toString()));
          }
          nSolver.add(solverAssertions);
          /* Replacing implications with consequents */
//          BoolExpr[] solverAssertions = _solver.getAssertions();
//
//          for (int i =0;i<solverAssertions.length;i++){
//            BoolExpr be = solverAssertions[i];
//            if (be.isImplies()){
//              solverAssertions[i] =  (BoolExpr) be.getArgs()[1];
//            }
//          }
//          nSolver.add(solverAssertions); //Modified assertions added to solver.


          Set<Integer> extra = new HashSet<>(); //tracking noncore assertions
          Set<BoolExpr> extraPredicates = new HashSet<>();

          BoolExpr currentPred;
          for (int j = 0; j < solverAssertions.length; j++){
            currentPred =  solverAssertions[j];
            solverAssertions[j] = mkTrue(); //Equivalent of removing the assertion
            nSolver.reset();
            nSolver.add(solverAssertions);
            if (nSolver.check()==Status.UNSATISFIABLE) {
              extra.add(j);
              extraPredicates.add(currentPred);
            }else {
              solverAssertions[j] = currentPred;
            }
          }

          int predId = 0;
          int minCoreSize = solverAssertions.length - extra.size();
          BoolExpr[] minimalCore = new BoolExpr[minCoreSize];
          for (int k=0;k<solverAssertions.length;k++){
            if (!extra.contains(k)){
              minimalCore[predId] = solverAssertions[k];
              predId++;
            }
          }

          System.out.println("=========================================");

          nSolver.reset();
          nSolver.add(minimalCore);
          System.out.println("First " + nSolver.check());


          System.out.println("\nPredicates not in the unsatCore:");
          System.out.println("==========================================");
          Expr[] unsatCore = _solver.getUnsatCore();

          HashSet<String> unsatCoreStrings = new HashSet<>();
          for (int i = 0; i < unsatCore.length; i++) {
            unsatCoreStrings.add(unsatCore[i].toString());
          }

          for (String e : unsatcoreLabelsMap.keySet()) {
            if (!unsatCoreStrings.contains(e)) {
              String label = unsatcoreLabelsMap.get(e.toString());
              // Only consider constraints we can change
              if (!(label.equals(UnsatCore.FAILED)
                      || label.equals(UnsatCore.ENVIRONMENT)
                      || label.equals(UnsatCore.BEST_OVERALL)
                      || label.equals(UnsatCore.BEST_PER_PROTOCOL)
                      || label.equals(UnsatCore.CHOICE_PER_PROTOCOL)
                      || label.equals(UnsatCore.CONTROL_FORWARDING)
                      || label.equals(UnsatCore.POLICY)
                      || label.equals(UnsatCore.BOUND)
                      || label.equals(UnsatCore.UNUSED_DEFAULT_VALUE)
                      || label.equals(UnsatCore.HISTORY_CONSTRAINTS))) {
                System.out.println(e.toString() + ": " + label + ": "
                        + unsatVarsMap.get(e));
              }
            }
          }

          for (BoolExpr extraPred : extraPredicates){
            System.out.println(extraPred);
          }



          System.out.println("==========================================");
          if (_shouldPrintUnsatCore) {
            System.out.println("Size of UnsatCore : " + _solver.getUnsatCore().length);
            System.out.println("\nPredicates in the unsatCore:");
            System.out.println("==========================================");
            for (Expr e : _solver.getUnsatCore()) {
              String label = unsatcoreLabelsMap.get(e.toString());
              // Only consider constraints we can change
              if (!(label.equals(UnsatCore.FAILED)
                      || label.equals(UnsatCore.ENVIRONMENT)
                      || label.equals(UnsatCore.BEST_OVERALL)
                      || label.equals(UnsatCore.BEST_PER_PROTOCOL)
                      || label.equals(UnsatCore.CHOICE_PER_PROTOCOL)
                      || label.equals(UnsatCore.CONTROL_FORWARDING)
                      || label.equals(UnsatCore.POLICY)
                      || label.equals(UnsatCore.BOUND))) {
                System.out.println(e.toString() + ": " + label);// + ": " + unsatVarsMap.get(e.toString()));

              }
            }
            System.out.println("==========================================");
            System.out.println("\nSolver Assertions");
            System.out.println("Count " + _solver.getNumAssertions());
            BoolExpr[] unsatCorePredicates = _solver.getUnsatCore();
            System.out.println("==========================================");
            System.out.println("Size of Original Unsatcore" + unsatCorePredicates.length);
            System.out.println("Minimizing Unsat Core...\n");

          //build not unsatCor!!!! USE COMMENTS!! PUT A FOR LINE
          // Expr[] notUnsatCore = new ArrayList<Expr>();
          // for unsatVarsMap.entrySet(){
          //   // in unsatvarsmap but not in unsatcore
          // }
          //do above down below
          for (String e : unsatcoreLabelsMap.keySet()) {
            String label = unsatcoreLabelsMap.get(e.toString());
              // Only consider constraints we can change
            if (!(label.equals(UnsatCore.FAILED)
                    || label.equals(UnsatCore.ENVIRONMENT)
                    || label.equals(UnsatCore.BEST_OVERALL)
                    || label.equals(UnsatCore.BEST_PER_PROTOCOL)
                    || label.equals(UnsatCore.CHOICE_PER_PROTOCOL)
                    || label.equals(UnsatCore.CONTROL_FORWARDING)
                    || label.equals(UnsatCore.POLICY)
                    || label.equals(UnsatCore.BOUND)
                    || label.equals(UnsatCore.UNUSED_DEFAULT_VALUE)
                    || label.equals(UnsatCore.HISTORY_CONSTRAINTS))) {
              System.out.println(e.toString() + ": " + label + ": "
                      + unsatVarsMap.get(e));
            }
          }
        
            //making labels for unsat core tracking :
            BoolExpr[] ps = new BoolExpr[unsatCorePredicates.length];
            for (int i = 0; i < unsatCorePredicates.length; i++) {
              ps[i] = _ctx.mkBoolConst("UCorePred" + i);
            }


            System.out.println("==========================================");
          }
          //use notUnsatCore
          checkPreds(unsatCore, assignedTo, referencedTo);
          break;
        }
        if (s == Status.UNKNOWN) {
          throw new BatfishException("ERROR: satisfiability unknown");
        }
      } while (_question.getMinimize() || numCounterexamples < _numIters);
      ArrayList<String> variableNames = new ArrayList<>(variableHistoryMap.keySet());
      Collections.sort(variableNames);

      System.out.println("Generated " + numCounterexamples + " counterexamples");

      if (_shouldPrintCounterExampleChanges) {
        System.out.println("Changes in Model Variables");
        for (String variableName : variableNames) {
//          if (variableHistoryMap.get(variableName).size() <= 1) {
            System.out.println(
                    variableName + " { " + String.join(";", variableHistoryMap.get(variableName)) + " }");
//          }
        }
      }

      return new Tuple<>(result, m);
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
    addFailedConstraints(_question.getFailures());
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

  // add constraints to the encoder.
  public void addVariables(Map<String, Expr> addedConstraints) {
    _allVariables.putAll(addedConstraints);
  }
}
