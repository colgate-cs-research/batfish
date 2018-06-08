package org.batfish.symbolic.smt;

import java.util.EnumSet;
import org.batfish.datamodel.Interface;
import org.batfish.symbolic.Protocol;

/**
 * A class contains the information of a predicate: type, name of the router and name of the interface 
 *
 * @author Owen Sun
 */

public class PredicateLabel{
  /**
  * This enum type labels are all the types of the predicate may have.
  */
  
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
    if (face != null) {
        this.intface_String=face.getName();
    } else  {
        this.intface_String = null;
    }
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
 
  @Override
  /**
  * This method override the toString method since the interface
  * may saved as String or Interface type. This methods will correctly
  * return the information.

  * @return String The type, router and the interface of this predicate
  */
  public String toString() {
      return type+(device != null ? " " + device : "")
          +(intface_String != null ? " " + intface_String : "")
          +(proto != null ? " " + proto.name() : "");
  }

  
  /**
  * This method updates the type
  * @param s String the new type
  */
  public void Settype(labels s) {
    this.type=s;
  }
  
  /**
  * This method updates the router
  * @param s String the new router name
  */
  public void Setdevice(String s) {
    this.device=s;
  }
  
  /**
  * This method updates the interface
  * @param s String the new interface
  */
  public void Setinterface(String s) {
    this.intface_String=s;
  }
  
  public void setProtocol(Protocol p) {
    this.proto = p;
  }
  
  /**
  * This method returns the type
  * @return labels the type of this PredicateLabel
  */
  
  public labels gettype() {
    return type;
  }
  
  /**
  * This method return the name of the router
  * @return String return the name of the router
  */
  
  public String getdevice() {
    return device;
  }
  
  /**
  * This method returns the interface
  * @return Interface This returns the interface
  */
  
  public Interface getintface() {
      return this.intface;
  }
  
  /**
  * This method returns interface in a String
  * @return String This returns the interface
  */
  
  public String getStrintface() {
      return this.intface_String;
  }
  
  /**
  * This method compare two predicates are the same or not
  * @param Object o1 This is the predicateLabel going to be compared
  * @return boolean Return true if they are the same, false if they are different
  */
  
  @Override
  public boolean equals (Object o1) {
    if (o1 instanceof PredicateLabel) {
    boolean type_b=false, device_b=false, intface_b =false, proto_b = false;

    PredicateLabel p1=(PredicateLabel) o1;
    if ((p1).gettype().equals(type))
      type_b = true;
    
    if ((((p1).getdevice()==null)&&(device==null))
        ||((p1).getdevice()!=null && (p1).getdevice().equals(device)))
        device_b=true;
    
    if (((p1).getStrintface()==null&&this.intface_String==null)
        ||((p1).getStrintface()!=null&&(p1).getStrintface().equals(intface_String)))
      intface_b=true;
    
    if ((p1.proto == null && this.proto == null)
      || (p1.proto != null && p1.proto.equals(this.proto))) {
      proto_b = true;
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
