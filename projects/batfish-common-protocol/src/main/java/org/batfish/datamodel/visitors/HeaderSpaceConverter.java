package org.batfish.datamodel.visitors;

import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;

/** Converts an {@link AclLineMatchExpr} to the {@link HeaderSpace} matching that expr. */
public class HeaderSpaceConverter implements GenericAclLineMatchExprVisitor<HeaderSpace> {

  private static final HeaderSpaceConverter INSTANCE = new HeaderSpaceConverter();

  public static HeaderSpace convert(AclLineMatchExpr expr) {
    return expr.accept(INSTANCE);
  }

  private HeaderSpaceConverter() {}

  @Override
  public HeaderSpace visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public HeaderSpace visitDeniedByAcl(DeniedByAcl deniedByAcl) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public HeaderSpace visitFalseExpr(FalseExpr falseExpr) {
    return HeaderSpace.builder().setNegate(true).build();
  }

  @Override
  public HeaderSpace visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    return matchHeaderSpace.getHeaderspace();
  }

  @Override
  public HeaderSpace visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public HeaderSpace visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public HeaderSpace visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public HeaderSpace visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public HeaderSpace visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public HeaderSpace visitTrueExpr(TrueExpr trueExpr) {
    return HeaderSpace.builder().build();
  }
}
