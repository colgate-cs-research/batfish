package org.batfish.minesweeper.smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Interface;
import org.batfish.minesweeper.Protocol;
import org.batfish.minesweeper.collections.Table3;

/**
 * Class for the symbolic variables used to represent configuration values.
 *
 * @author Aaron Gember-Jacobson
 */
class SymbolicConfiguration {

  // Configuration values for each protocol
  private Table3<String, Protocol, Prefix, BoolExpr> _originatedConfiguration;

  //Configuration values for each interface (Router, iface, keyword, expr)
  private Table3<String, Interface, String, Expr> _interfaceConfiguration;

  SymbolicConfiguration() {
    _originatedConfiguration = new Table3<>();
    _interfaceConfiguration = new Table3<>();
  }

  Table3<String, Protocol, Prefix, BoolExpr> getOriginatedConfiguration() {
    return _originatedConfiguration;
  }

  Table3<String, Interface, String, Expr> getInterfaceConfiguration() { return _interfaceConfiguration; }
}
