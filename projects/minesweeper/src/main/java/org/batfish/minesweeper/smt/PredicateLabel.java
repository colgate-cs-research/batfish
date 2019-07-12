package org.batfish.minesweeper.smt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.batfish.datamodel.Interface;
import org.batfish.minesweeper.Protocol;

/**
 * A class contains the information of a predicate: type, name of the router and name of the interface 
 *
 * @author Owen Sun
 */

public class PredicateLabel{
  /**
  * This enum type LabelType are all the types of the predicate may have.
  */
  
  public enum LabelType {
    // Predicates for policy and environment
    POLICY,
    HEADER_SPACE,
    FAILURES,
    ENVIRONMENT,
    // Predicates computed based on predicates derived from configuration
    BEST_PER_PROTOCOL,
    BEST_OVERALL,
    CONTROL_FORWARDING,
    DATA_FORWARDING,
    REACHABILITY,
    PATH_LENGTH,
    LOOP,
    LOAD,
    // Predicates derived from configuration
    IMPORT,
    EXPORT,
    EXPORT_REDISTRIBUTED,
    COMMUNITY,
    ACLS_OUTBOUND,
    ACLS_INBOUND,
    PROTOCOL,
    // Predicates derived from counterexamples
    PACKET,
    COUNTEREXAMPLE,
    // Predicates for guaranteeing validity
    DEFAULT_VALUE,
    VALUE_LIMIT,
    SSA
  };
  
  /** Labels for predicates derived from configuration */
  private final static EnumSet<LabelType> CONFIGURABLE_LABELS = EnumSet.of(
      LabelType.IMPORT, LabelType.EXPORT, LabelType.EXPORT_REDISTRIBUTED,
      LabelType.PROTOCOL, LabelType.COMMUNITY,
      LabelType.ACLS_INBOUND, LabelType.ACLS_OUTBOUND);
  
  /** Labels for predicates that are computable based on predicates derived
   * from configuration */
  private final static EnumSet<LabelType> COMPUTABLE_LABELS = EnumSet.of(
      LabelType.BEST_PER_PROTOCOL, LabelType.BEST_OVERALL,
      LabelType.CONTROL_FORWARDING, LabelType.DATA_FORWARDING);

  private final static EnumSet<LabelType> TRACK_LABELS =
      EnumSet.complementOf(EnumSet.of(LabelType.COUNTEREXAMPLE));


  public class ConfigurationReference{
    private String router;
    private Interface iface;
    public String detail;


    public ConfigurationReference(String router, Interface iface, String detail) {
      this.router = router;
      this.iface = iface;
      this.detail = detail;
    }

    @Override
    public String toString(){
      return String.format("Router : %s | Interface : %s | Message : %s", router, iface.getName(), detail);
    }
  }

  private List<ConfigurationReference> references;

  private LabelType type;
  
  private String device;
  
  private Interface iface;
  
  private String intface_String;
  
  private Protocol proto;

  public PredicateLabel(LabelType type) {
    this(type, null);
  }
  
  public PredicateLabel(LabelType type, String device) {
    this(type,device,"null","null");
  }
  
  public PredicateLabel(LabelType type, String device, Interface iface) {
    this.type=type;
    this.device=device;
    this.iface =iface;
    this.intface_String=iface.getName();
    this.proto = null;
    this.references = new ArrayList<>();
  }
  
  public PredicateLabel(LabelType type, String device, Interface iface, Protocol proto) {
    this.type=type;
    this.device=device;
    this.iface =iface;
    if (iface != null) {
        this.intface_String=iface.getName();
    } else  {
        this.intface_String = null;
    }
    this.proto = proto;
    this.references = new ArrayList<>();
  }
  
  public PredicateLabel(LabelType type, String device, String iface, String proto) {
    this.type=type;
    this.device=device;
    if (device!=null && device.equals("null"))
      this.device=null;
    this.intface_String=iface;
    if (iface!=null && iface.equals("null"))
      this.intface_String=null;
    this.iface =null;
    if (proto != null && proto.equals("null")) {
      this.proto = null;
    } else {
      this.proto = Protocol.fromString(proto);
    }
    this.references = new ArrayList<>();
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
   * Add reference (currently just a string) to a list of references
   * Refers to one or more configuration statements used in the construction
   * of the predicate.
   * TODO: Create a Reference class to contain more than just a string.
   */
  public void addConfigurationRef(String router, Interface iface, String detail){
    references.add(new ConfigurationReference(router,iface, detail));
  }


  /**
   * This method returns the list of configuration references to this predicate label.
   * @return
   */
  public List<ConfigurationReference> getConfigurationRefs(){
    return this.references;
  }

  /**
  * This method returns the type
  * @return LabelType the type of this PredicateLabel
  */
  
  public LabelType getLabelType() {
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
  
  public Interface getInterface() {
      return this.iface;
  }
  
  /**
  * This method returns interface in a String
  * @return String This returns the interface
  */
  
  public String getInterfaceString() {
      return this.intface_String;
  }
  
  /**
  * This method compare two predicates are the same or not
  * @param o1 This is the predicateLabel going to be compared
  * @return boolean Return true if they are the same, false if they are different
  */
  
  @Override
  public boolean equals (Object o1) {
    if (o1 instanceof PredicateLabel) {
    boolean type_b=false, device_b=false, intface_b =false, proto_b = false;

    PredicateLabel p1=(PredicateLabel) o1;
    if ((p1).getLabelType().equals(type))
      type_b = true;
    
    if ((((p1).getdevice()==null)&&(device==null))
        ||((p1).getdevice()!=null && (p1).getdevice().equals(device)))
        device_b=true;
    
    if (((p1).getInterfaceString()==null&&this.intface_String==null)
        ||((p1).getInterfaceString()!=null&&(p1).getInterfaceString().equals(intface_String)))
      intface_b=true;
    
    if ((p1.proto == null && this.proto == null)
      || (p1.proto != null && p1.proto.equals(this.proto))) {
      proto_b = true;
    }
        
    return (type_b && device_b && intface_b && proto_b);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return this.toString().hashCode();
  }
  
  /**
   * Checks whether the label corresponds to a predicate that is derived from 
   *    configuration.
   * @return true if the label corresponds to a predicate that is derived from 
   *    configuration, otherwise false
   */
  public boolean isConfigurable() {
    return CONFIGURABLE_LABELS.contains(this.type);
  }

  /**
   * Checks whether the label corresponds to a predicate that is computed
   *    based on predicates derived from the configuration.
   * @return true if the label corresponds to a predicate that is computed
   *    based on predicates derived from configuration, otherwise false
   */
  public boolean isComputable() {
    return COMPUTABLE_LABELS.contains(this.type);
  }
  
  /**
   * Checks whether the label corresponds to a predicate that should be tracked (to allow it
   * to be part of the unsat core).
   * @return true if the label corresponds to a predicate that should be tracked,
   *    otherwise false
   */
  public boolean shouldTrack() {
    //return isConfigurable();
    return TRACK_LABELS.contains(this.type);
  }
}
