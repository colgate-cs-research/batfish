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
  
  private String intface_String;
  
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
  
  public PredicateLabel(labels type, String device, String face) {
    this.type=type;
    this.device=device;
    this.intface_String=face;
  }
  
  public String toString() {
    if (intface_String==null)
      return type+" "+device+" "+intface;
    else 
      return type+" "+device+" "+intface_String;
  }
  public void Settype(labels s) {
    this.type=s;
  }
  
  public void Setdevice(String s) {
    this.device=s;
  }
  
  public void Setinterface(String s) {
    this.intface_String=s;
  }
  
  public labels gettype() {
    return this.type;
  }
}