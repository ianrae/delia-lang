package org.delia.codegen.sample;

import java.util.HashMap;
import java.util.Map;
import org.delia.type.DStructHelper;
import org.delia.type.DValue;public static class WingImmut implements Wing {
  private DValue dval;
  private DStructHelper helper;

  public WingImmut(DValue dval) {
    this.dval = dval;
    this.helper = dval.asStruct();
  }
  @Override
  public DValue internalDValue() {
    return dval;
  }

  @Override
  public Integer getWidth() {
    return helper.getField("width").asInt();
  }
  @Override
  public Integer getId() {
    return helper.getField("id").asInt();
  }
}

