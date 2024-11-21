package org.apache.hop.example.demo1;

import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.HopExtension;
import org.apache.hop.testing.SpecMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
@HopEnv(ui = SpecMode.PREVIEW, withH2 = true)
class Demo1Test {

  @TestTemplate
  void testTransformUi(Demo1Dialog dialog) {
    Assertions.assertNotNull(dialog);
  }
}
