package org.batfish.minesweeper.smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Interface;
import org.batfish.minesweeper.GraphEdge;
import org.batfish.minesweeper.Protocol;
import org.batfish.minesweeper.collections.Table2;
import org.batfish.minesweeper.collections.Table3;

/**
 * Class for the symbolic variables used to represent configuration values.
 *
 * @author Aaron Gember-Jacobson
 */
class SymbolicConfiguration {

  enum Keyword{
      OSPF_ENABLED, OSPF_COST, ACTIVE
  }
  // Configuration values for each protocol
  private Table3<String, Protocol, Prefix, BoolExpr> _originatedConfiguration;

  // Configuration values for each interface (router, iface, keyword, expr)
  private Table3<String, Interface, Keyword, Expr> _interfaceConfiguration;

  // Configuration values for each layer-3 edge (router, edge, expr)
  private Table2<String, GraphEdge, Expr> _edgeConfiguration;

  // Configuration values for each BGP peer (router, iface, peer, expr)
  private Table3<String, Interface, BgpPeerConfig, Expr> _neighborConfiguration;

  SymbolicConfiguration() {
    _originatedConfiguration = new Table3<>();
    _interfaceConfiguration = new Table3<>();
    _edgeConfiguration = new Table2<>();
    _neighborConfiguration = new Table3<>();
  }

  Table3<String, Protocol, Prefix, BoolExpr> getOriginatedConfiguration() {
    return _originatedConfiguration;
  }

  Table3<String, Interface, Keyword, Expr> getInterfaceConfiguration() {
    return _interfaceConfiguration;
  }

  Table2<String, GraphEdge, Expr> getEdgeConfiguration() {
    return _edgeConfiguration;
  }

  public Table3<String, Interface, BgpPeerConfig, Expr> getNeighborConfiguration() {
    return _neighborConfiguration;
  }


}
