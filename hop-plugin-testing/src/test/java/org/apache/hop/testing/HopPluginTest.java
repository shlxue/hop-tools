package org.apache.hop.testing;

import org.apache.hop.ui.pipeline.transforms.dummy.DummyDialog;
import org.apache.hop.ui.workflow.actions.dummy.ActionDummyDialog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
@HopEnv(ui = SpecMode.STRICT)
class HopPluginTest {

  @TestTemplate
  void uiSpec(ActionDummyDialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  void uiSpec(DummyDialog dialog) {
    Assertions.assertNotNull(dialog);
  }
}
