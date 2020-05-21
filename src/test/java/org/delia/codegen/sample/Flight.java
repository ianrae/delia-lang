package org.delia.codegen.sample;
import java.util.Date;

import org.delia.codegen.DeliaImmutable;
import org.delia.codegen.sample.Wing;

public interface Flight extends DeliaImmutable {
  Date getDd();
  int getField1();
  Wing getWing();
  int getField2();
}

