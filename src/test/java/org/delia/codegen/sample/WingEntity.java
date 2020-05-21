package org.delia.codegen.sample;

import java.util.HashMap;
import java.util.Map;
import org.delia.type.DStructHelper;
import org.delia.type.DValue;  public class WingEntity implements Wing,WingSetter {
    private DValue dval;
    private DStructHelper helper;
    private Map<String,Object> setMap = new HashMap<>();

    public WingEntity(DValue dval) {
      this.dval = dval;
      this.helper = dval.asStruct();
    }
    public WingEntity(Wing immut) {
      WingImmut x = (WingImmut) immut;
      this.dval = x.dval;
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
    public Integer getWidth() {
      String fieldName = "width";
      if (setMap.containsKey(fieldName)) {
        return (Integer)setMap.get(fieldName); //can return null
      }
      return helper.getField(fieldName).asInt();
    }
    @Override
    public void setWidth(Integer val) {
      setMap.put("width", val);
    }
    
    @Override
    public Integer getId() {
      String fieldName = "id";
      if (setMap.containsKey(fieldName)) {
        return (Integer)setMap.get(fieldName); //can return null
      }
      return helper.getField(fieldName).asInt();
    }
    @Override
    public void setId(Integer val) {
      setMap.put("id", val);
    }
    
}

