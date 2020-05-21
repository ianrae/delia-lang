package org.delia.codegen.sample;
import java.util.Date;
import org.delia.codegen.sample.Wing;


import java.util.HashMap;
import java.util.Map;
import org.delia.type.DStructHelper;
import org.delia.type.DValue;
  public class FlightEntity implements Flight,FlightSetter {
    private DValue dval;
    private DStructHelper helper;
    private Map<String,Object> setMap = new HashMap<>();

    public FlightEntity(DValue dval) {
      this.dval = dval;
      this.helper = dval.asStruct();
    }
    public FlightEntity(Flight immut) {
      FlightImmut x = (FlightImmut) immut;
      this.dval = x.internalDValue();
      this.helper = dval.asStruct();
    }
    
    @Override
    public DValue internalDValue() {
      return dval; //can be null, if disconnected entity
    }
    @Override
    public Map<String, Object> internalSetValueMap() {
      return setMap;
    }

    @Override
    public Date getDd() {
      String fieldName = "dd";
      if (setMap.containsKey(fieldName)) {
        return (Date)setMap.get(fieldName); //can return null
      }
      return helper.getField(fieldName).asDate();
    }
    @Override
    public void setDd(Date val) {
      setMap.put("dd", val);
    }
    
    @Override
    public int getField1() {
      String fieldName = "field1";
      if (setMap.containsKey(fieldName)) {
        return (Integer)setMap.get(fieldName); //can return null
      }
      return helper.getField(fieldName).asInt();
    }
    @Override
    public void setField1(int val) {
      setMap.put("field1", val);
    }
    
    @Override
    public Wing getWing() {
      String fieldName = "wing";
      if (setMap.containsKey(fieldName)) {
        return (Wing)setMap.get(fieldName); //can return null
      }

      DValue inner = helper.getField("wing");
      Wing immut = new WingImmut(inner);
      return immut;
    }
    @Override
    public void setWing(Wing val) {
      setMap.put("wing", val);
    }
    
    @Override
    public int getField2() {
      String fieldName = "field2";
      if (setMap.containsKey(fieldName)) {
        return (Integer)setMap.get(fieldName); //can return null
      }
      return helper.getField(fieldName).asInt();
    }
    @Override
    public void setField2(int val) {
      setMap.put("field2", val);
    }
    
}

