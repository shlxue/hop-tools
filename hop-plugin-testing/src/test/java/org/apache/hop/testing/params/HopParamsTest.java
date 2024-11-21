package org.apache.hop.testing.params;

import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.HopExtension;
import org.apache.hop.testing.SpecMode;
import org.apache.hop.ui.pipeline.transforms.dummy.DummyDialog;
import org.apache.hop.ui.workflow.actions.dummy.ActionDummyDialog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
@HopEnv(ui = SpecMode.NONE)
class HopParamsTest {

  @TestTemplate
  void testDummyActionUi(ActionDummyDialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  void testDummyTransformUi(DummyDialog dialog) {
    Assertions.assertNotNull(dialog);
  }
}
