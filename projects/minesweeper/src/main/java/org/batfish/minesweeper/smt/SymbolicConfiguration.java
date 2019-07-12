package org.batfish.minesweeper.smt;

import com.microsoft.z3.Expr;
import org.batfish.minesweeper.Protocol;
import org.batfish.minesweeper.collections.Table3;

/**
 * Class for the symbolic variables used to represent configuration values.
 *
 * @author Aaron Gember-Jacobson
 */
class SymbolicConfiguration {

  // Configuration values for each protocol
  private Table3<String, Protocol, String, Expr> _protocolConfiguration;

  SymbolicConfiguration() {
    _protocolConfiguration = new Table3<>();
  }

  Table3<String, Protocol, String, Expr> getProtocolConfiguration() {
    return _protocolConfiguration;
  }
}
