package org.batfish.symbolic.smt;

import java.util.EnumSet;
import org.batfish.datamodel.Interface;
import org.batfish.symbolic.Protocol;

public class PredicateLabel{
  
  public enum labels{
    POLICY,
    HEADER_SPACE,
    FAILURES,
    ENVIRONMENT,
    BEST_PER_PROTOCOL,
    BEST_OVERALL,
    CONTROL_FORWARDING,
    DATA_FORWARDING,
    IMPORT,
    EXPORT,
    COMMUNITY,
    ACLS_OUTBOUND,
    ACLS_INBOUND,
    PACKET,
    COUNTEREXAMPLE,
    DEFAULT_VALUE,
    VALUE_LIMIT,
    SSA
  };
  
  private final static EnumSet<labels> CONFIGURABLE_LABELS = EnumSet.of(
      labels.IMPORT, labels.EXPORT, labels.COMMUNITY, labels.ACLS_INBOUND, labels.ACLS_OUTBOUND);
  
  private labels type;
  
  private String device;
  
  private Interface intface;
  
  private String intface_String;
  
  private Protocol proto;
  
  public PredicateLabel(labels type) {
    this(type, null);
  }
  
  public PredicateLabel(labels type, String device) {
    this(type,device,"null","null");
  }
  
  public PredicateLabel(labels type, String device, Interface face) {
    this.type=type;
    this.device=device;
    this.intface=face;
    this.intface_String=face.getName();
    this.proto = null;
  }
  
  public PredicateLabel(labels type, String device, Interface face, Protocol proto) {
    this.type=type;
    this.device=device;
    this.intface=face;
    this.intface_String=face.getName();
    this.proto = proto;
  }
  
  public PredicateLabel(labels type, String device, String face, String proto) {
    this.type=type;
    this.device=device;
    if (device!=null && device.equals("null"))
      this.device=null;
    this.intface_String=face;
    if (face!=null && face.equals("null"))
      this.intface_String=null;
    this.intface=null;
    if (proto != null && proto.equals("null")) {
      this.proto = null;
    } else {
      this.proto = Protocol.fromString(proto);
    }
  }
  
  public String toString() {
    if (intface_String==null)
      return type+" "+device+" "+intface+" "+proto;
    else 
      return type+" "+device+" "+intface_String+" "+proto;
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
  
  public void setProtocol(Protocol p) {
    this.proto = p;
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
    boolean type_b=false, device_b=false, intface_b =false, proto_b = false;
    if (((PredicateLabel) o1).gettype().equals(type))
      type_b = true;
    
    if (((((PredicateLabel) o1).getdevice()==null)&&(device==null))
        ||(((PredicateLabel) o1).getdevice()!=null && ((PredicateLabel) o1).getdevice().equals(device)))
        device_b=true;
    
    if ((((PredicateLabel) o1).getStrintface()==null&&this.intface_String==null)
        ||(((PredicateLabel) o1).getStrintface()!=null&&((PredicateLabel) o1).getStrintface().equals(intface_String)))
      intface_b=true;
    
    PredicateLabel p1 = (PredicateLabel) o1;
    if ((p1.proto == null && this.proto == null)
      || (p1.proto != null && p1.proto.equals(this.proto))) {
      type_b = true;
    }
        
    return (type_b && device_b && intface_b && proto_b);
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
  
  /**
   * Checks whether the label corresponds to a predicate that is derived from configuration.
   * @return true if the label corresponds to a predicate that is derived from configuration,
   *    otherwise false
   */
  public boolean isConfigurable() {
    return CONFIGURABLE_LABELS.contains(this.type);
  }
}