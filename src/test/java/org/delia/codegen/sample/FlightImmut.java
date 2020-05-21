package org.delia.codegen.sample;
import java.util.Date;
import org.delia.codegen.sample.Wing;

import java.util.HashMap;
import java.util.Map;
import org.delia.type.DStructHelper;
import org.delia.type.DValue;public static class FlightImmut implements Flight {
  private DValue dval;
  private DStructHelper helper;

  public FlightImmut(DValue dval) {
    this.dval = dval;
    this.helper = dval.asStruct();
  }
  @Override
  public DValue internalDValue() {
    return dval;
  }

  @Override
  public Date getDd() {
    return helper.getField("dd").asDate();
  }
  @Override
  public Integer getField1() {
    return helper.getField("field1").asInt();
  }
  @Override
  public Wing getWing() {
    DValue inner = helper.getField("wing");
    Wing inner = new WingImmut(inner);
    return inner;
  }
  @Override
  public Integer getField2() {
    return helper.getField("field2").asInt();
  }
}

