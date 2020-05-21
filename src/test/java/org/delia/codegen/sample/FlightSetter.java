package org.delia.codegen.sample;
import java.util.Date;

import org.delia.codegen.DeliaEntity;
import org.delia.codegen.sample.Wing;

public interface FlightSetter extends DeliaEntity {
  void setDd(Date val);
  void setField1(int val);
  void setWing(Wing val);
  void setField2(int val);
}

