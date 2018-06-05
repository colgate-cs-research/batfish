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
    this(type, null);
  }
  
  public PredicateLabel(labels type, String device) {
    this(type,device,"null");
  }
  
  public PredicateLabel(labels type, String device, Interface face) {
    this.type=type;
    this.device=device;
    this.intface=face;
    this.intface_String=face.toString();
  }
  
  public PredicateLabel(labels type, String device, String face) {
    this.type=type;
    this.device=device;
    if (device!=null && device.equals("null"))
      this.device=null;
    this.intface_String=face;
    if (face!=null && face.equals("null"))
      this.intface_String=null;
    this.intface=null;
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
    return type;
  }
  
  public String getdevice() {
    if (device=="null")
      return null;
    return device;
  }
  
  public Interface getintface() {
    if (this.intface!=null)
      return this.intface;
    else
      return null;
  }
  
  public String getStrintface() {
    if (this.intface_String=="null")
      return null;
    else 
      return this.intface_String;
  }
  
  @Override
  public boolean equals (Object o1) {
    if (o1 instanceof PredicateLabel) {
    boolean type_b=false, device_b=false, intface_b =false;
    if (((PredicateLabel) o1).gettype().equals(type))
      type_b = true;
    if (((((PredicateLabel) o1).getdevice()==null)&&(device==null))
        ||(((PredicateLabel) o1).getdevice()!=null && ((PredicateLabel) o1).getdevice().equals(device)))
        device_b=true;
    
    if ((((PredicateLabel) o1).getStrintface()==null&&this.intface_String==null)
        ||(((PredicateLabel) o1).getStrintface()!=null&&((PredicateLabel) o1).getStrintface().equals(intface_String)))
      intface_b=true;
        
    return (type_b && device_b && intface_b);
//    if ((o1.gettype()==type) && (o1.getdevice()==device) && (this.intface_String==o1.getStrintface()))
//      return true;
//   
//    return false;
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return this.toString().hashCode();
  }
}