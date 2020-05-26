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
    FAILURE_LIMIT,
    CANNOT_FAIL,
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
    // Control logic
    IMPORT,
    EXPORT,
    EXPORT_REDISTRIBUTED,
    // Predicates derived from configuration
    COMMUNITY,
    ACLS_OUTBOUND,
    ACLS_INBOUND,
    ORIGINATED,
    INTERFACE_ACTIVE,
    INTERFACE_PROTOCOL_ENABLED,
    INTERFACE_OSPF_COST,
    NEIGHBOR,
    LAYER3_ADJACENCY,
    ROUTE_FILTER_LIST,
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
      LabelType.ORIGINATED, LabelType.NEIGHBOR, LabelType.INTERFACE_ACTIVE,
      LabelType.INTERFACE_PROTOCOL_ENABLED, LabelType.INTERFACE_OSPF_COST,
      LabelType.LAYER3_ADJACENCY, LabelType.COMMUNITY,
      LabelType.ACLS_INBOUND, LabelType.ACLS_OUTBOUND,
      LabelType.ROUTE_FILTER_LIST);
  
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
    private Protocol proto;
    public String detail;

    public ConfigurationReference(String router, String detail) {
      this.router = router;
      this.detail = detail;
    }

    public ConfigurationReference(String router, Interface iface, String detail) {
      this(router, detail);
      this.iface = iface;
    }

    public ConfigurationReference(String router, Protocol proto, String detail) {
      this(router, detail);
      this.proto = proto;
    }


    @Override
    public String toString(){
      List<String> details = new ArrayList<>();
      details.add("Router: " + router);
      if (iface != null) {
          details.add("Interface: " + iface.getName());
      }
      if (proto != null) {
          details.add("Protocol: " + proto.name());
      }
      details.add("Message: " + detail);
      return String.join(" | ", details);
    }
  }

  private List<ConfigurationReference> references;

  private LabelType type;
  
  private String device;
  
  private Interface iface;
  
  private Protocol proto;

  private String filter;

  private Boolean omission;

  public PredicateLabel(LabelType type) {
    this(type, null);
  }

  public PredicateLabel(LabelType type, String device) {
    this(type, device, null, null, null);
  }

  public PredicateLabel(LabelType type, String device, Interface iface) {
    this(type, device, iface, null, null);
  }
  
  public PredicateLabel(LabelType type, String device, Interface iface, 
      Protocol proto) {
    this(type, device, iface, proto, null);
  }

  public PredicateLabel(LabelType type, String device, Interface iface, 
      Protocol proto, Boolean omission) {
    this.type = type;
    this.device = device;
    this.iface = iface;
    this.proto = proto;
    this.filter = null;
    this.omission = omission;
    this.references = new ArrayList<>();
  }

  public PredicateLabel(LabelType type, String device, String filter) {
    this(type, device, filter, null);
  }

  public PredicateLabel(LabelType type, String device, String filter,
      Boolean omission) {
    this.type = type;
    this.device = device;
    this.iface = null;
    this.proto = null;
    this.filter = filter;
    this.omission = omission;
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
      if (filter != null) {
        return type + (device != null ? " " + device : " null")
            + " " + filter + " null " + (omission == null ? " null" :
            (omission ? " OMISSION" : " COMISSION"));
      } else {
        return type + (device != null ? " " + device : " null")
            + (iface != null ? " " + iface.getName() : " null")
            + (proto != null ? " " + proto.name() : " null") 
            + (omission == null ? " null" : 
              (omission ? " OMISSION" : " COMISSION"));
      }
  }

  /**
   * Add reference (currently just a string) to a list of references
   * Refers to one or more configuration statements used in the construction
   * of the predicate.
   * TODO: Create a Reference class to contain more than just a string.
   */
  public void addConfigurationRef(String router, Interface iface, String detail){
    references.add(new ConfigurationReference(router, iface, detail));
  }

  /**
   * Add reference (currently just a string) to a list of references
   * Refers to one or more configuration statements used in the construction
   * of the predicate.
   * TODO: Create a Reference class to contain more than just a string.
   */
  public void addConfigurationRef(String router, Protocol proto, String detail){
    references.add(new ConfigurationReference(router, proto, detail));
  }

  /**
   * Add reference (currently just a string) to a list of references
   * Refers to one or more configuration statements used in the construction
   * of the predicate.
   * TODO: Create a Reference class to contain more than just a string.
   */
  public void addConfigurationRef(String router, String detail){
    references.add(new ConfigurationReference(router, detail));
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
  * This method returns the interface
  * @return Interface This returns the interface
  */
  
  public Interface getInterface() {
      return this.iface;
  }

  /**
   * This method sets whether it is an omission
   * @param omission
   */
  public void setOmission(Boolean omission) {
    this.omission = omission;
  }

  /**
   * This method returns true if it is omission
   * @return true if it is an omission, otherwise false
   */
  public boolean isOmission() {
    return (this.omission == true);
  }

  
  /**
  * This method compare two predicates are the same or not
  * @param o1 This is the predicateLabel going to be compared
  * @return boolean Return true if they are the same, false if they are different
  */
  
  @Override
  public boolean equals (Object o1) {
    if (o1 instanceof PredicateLabel) {
      boolean type_b=false, device_b=false, iface_b =false, proto_b = false,
          filter_b=false, omission_b = false;

      PredicateLabel p1=(PredicateLabel) o1;
      if ((p1).getLabelType().equals(type))
        type_b = true;
    
      if ((p1.device == null && this.device == null)
          || (p1.device !=null && p1.device.equals(this.device)))
        device_b = true;
    
      if ((p1.iface==null && this.iface==null)
          || (p1.iface != null && p1.iface.equals(this.iface)))
        iface_b = true;
    
      if ((p1.proto == null && this.proto == null)
          || (p1.proto != null && p1.proto.equals(this.proto))) {
        proto_b = true;
      }

      if ((p1.filter == null && this.filter == null)
          || (p1.filter != null && p1.filter.equals(this.filter))) {
        filter_b = true;
      }

      if (p1.omission == this.omission) {
        omission_b = true;
      }
        
      return (type_b && device_b && iface_b && proto_b && filter_b 
          && omission_b);
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
