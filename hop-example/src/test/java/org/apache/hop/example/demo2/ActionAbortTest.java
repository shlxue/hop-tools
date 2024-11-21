package org.apache.hop.example.demo2;

import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.HopExtension;
import org.apache.hop.testing.SpecMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
@HopEnv(ui = SpecMode.PREVIEW)
class ActionAbortTest {

  @TestTemplate
  void testActionUi(ActionAbortDialog dialog) {
    Assertions.assertNotNull(dialog);
  }
}
