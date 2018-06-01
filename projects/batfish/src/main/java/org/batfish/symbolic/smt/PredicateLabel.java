package org.batfish.symbolic.smt;

import org.batfish.datamodel.Interface;

public class PredicateLabel{
  
  public enum labels{
    unlabeled,
    failed,
    environment,
    policy,
    bestOverall,
    bound,
    bestperprotocal,
    addCommunityConstraints,
    controlForwarding,
    addDataForwardingConstraints,
    counterexampleconstraint,
    packetvariables,
    unuseddefaultvalue,
    historyconstraints,
    addImportConstraint,
    addExportConstraint_connected,
    addExportConstraint_static,
    addExportConstraint,
    addTransferFunction,
    addHistoryConstraint,
    addUnusedDefaultValueConstraints,
    addHeaderSpaceConstraint,
    addEnvironmentConstraints,
    addChoicePerProtocolConstraint,
    initAclFunctions_outbound,
    initAclFunctions_inbound,
    choiceperprotocal,
    
  };
  private labels type;
  
  private String device;
  
  private Interface intface;
  
  public PredicateLabel(labels type) {
    this.type=type;
    this.device=null;
    this.intface=null;
  }
  
  public PredicateLabel(labels type, String device) {
    this.type=type;
    this.device=device;
    this.intface=null;
  }
  
  public PredicateLabel(labels type, String device, Interface face) {
    this.type=type;
    this.device=device;
    this.intface=face;
  }
  
  public String toString() {
    return type+" "+device+" "+intface;
  }
  
  public labels gettype() {
    return this.type;
  }
}