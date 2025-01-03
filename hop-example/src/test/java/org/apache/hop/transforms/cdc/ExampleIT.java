package org.apache.hop.transforms.cdc;

import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.HopExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
@HopEnv(withH2 = true)
class ExampleIT {

  @TestTemplate
  void testOpEventInputUi(OpEventInputDialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  void testSubOpEventUi(SubOpEventDialog dialog) {
    Assertions.assertNotNull(dialog);
  }
}
