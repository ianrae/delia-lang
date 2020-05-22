package org.delia.codegen.sample;


import org.delia.type.DStructHelper;
import org.delia.type.DValue;
public class WingImmut implements Wing {
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
  public int getWidth() {
    return helper.getField("width").asInt();
  }
  @Override
  public int getId() {
    return helper.getField("id").asInt();
  }
}

