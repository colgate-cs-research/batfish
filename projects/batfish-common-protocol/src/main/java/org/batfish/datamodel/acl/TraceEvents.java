package org.batfish.datamodel.acl;

import static com.google.common.base.MoreObjects.firstNonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceMetadata;

/** Utility methods for creating {@link TraceEvent TraceEvents}. */
@ParametersAreNonnullByDefault
public final class TraceEvents {
  private TraceEvents() {}

  public static TraceEvent permittedByAclLine(IpAccessList acl, int index) {
    String type = firstNonNull(acl.getSourceType(), "filter");
    String name = firstNonNull(acl.getSourceName(), acl.getName());
    AclLine line = acl.getLines().get(index);
    String lineDescription = firstNonNull(line.getName(), line.toString());
    String description =
        String.format(
            "Flow permitted by %s named %s, index %d: %s", type, name, index, lineDescription);
    return new TraceEvent(description);
  }

  static TraceEvent deniedByAclLine(IpAccessList acl, int index) {
    String type = firstNonNull(acl.getSourceType(), "filter");
    String name = firstNonNull(acl.getSourceName(), acl.getName());
    AclLine line = acl.getLines().get(index);
    String lineDescription = firstNonNull(line.getName(), line.toString());
    String description =
        String.format(
            "Flow denied by %s named %s, index %d: %s", type, name, index, lineDescription);
    return new TraceEvent(description);
  }

  public static TraceEvent defaultDeniedByIpAccessList(IpAccessList ipAccessList) {
    String name = ipAccessList.getName();
    @Nullable String sourceName = ipAccessList.getSourceName();
    @Nullable String sourceType = ipAccessList.getSourceType();
    String description =
        sourceName != null
            ? String.format("Flow did not match '%s' named '%s'", sourceType, sourceName)
            : String.format("Flow did not match ACL named '%s'", name);
    return new TraceEvent(description);
  }

  public static TraceEvent permittedByNamedIpSpace(
      Ip ip,
      String ipDescription,
      @Nullable IpSpaceMetadata ipSpaceMetadata,
      @Nonnull String name) {
    String type;
    String displayName;
    if (ipSpaceMetadata != null) {
      type = ipSpaceMetadata.getSourceType();
      displayName = ipSpaceMetadata.getSourceName();
    } else {
      type = IpSpace.class.getSimpleName();
      displayName = name;
    }
    return new TraceEvent(
        String.format("%s %s permitted by '%s' named '%s'", ipDescription, ip, type, displayName));
  }
}
