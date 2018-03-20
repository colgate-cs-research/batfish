package org.batfish.datamodel;

import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

@JsonSchemaDescription("An access-control action")
public enum LineAction {
  ACCEPT,
  REJECT;

  @Override
  public String toString() {
    switch (this){
      case ACCEPT:
        return "permit";
      case REJECT:
        return "deny";
      default:
        throw new IllegalArgumentException();
    }
  }
}
