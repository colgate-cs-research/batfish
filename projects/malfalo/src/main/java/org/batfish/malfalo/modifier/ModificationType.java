package org.batfish.malfalo.modifier;

/** Types of modifications that maybe made on a configuration file. **/
public enum ModificationType {
    ADD_STANDARD_ACL("add-standard-acl"),
    ADD_EXTENDED_ACL("add-extended-acl"),
    REMOVE_NETWORK("rm-network");

    private String type;

    ModificationType(String type){
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
