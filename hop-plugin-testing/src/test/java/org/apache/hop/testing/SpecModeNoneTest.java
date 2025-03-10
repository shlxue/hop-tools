package org.apache.hop.testing;

import org.apache.hop.ui.pipeline.transforms.dummy.DummyDialog;
import org.apache.hop.ui.workflow.actions.dummy.ActionDummyDialog;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(HopExtension.class)
@HopEnv(ui = SpecMode.NONE)
class SpecModeNoneTest {

  @TestTemplate
  void testActionUi(ActionDummyDialog dialog) {
    assertNotNull(dialog);
  }

  @TestTemplate
  void testTransformUi(DummyDialog dialog) {
    assertNotNull(dialog);
  }
}
