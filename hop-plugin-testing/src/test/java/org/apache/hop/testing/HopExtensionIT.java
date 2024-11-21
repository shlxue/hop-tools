package org.apache.hop.testing;

import org.apache.hop.pipeline.transforms.rowgenerator.RowGeneratorDialog;
import org.apache.hop.ui.pipeline.transforms.dummy.DummyDialog;
import org.apache.hop.ui.pipeline.transforms.injector.InjectorDialog;
import org.apache.hop.ui.workflow.actions.dummy.ActionDummyDialog;
import org.apache.hop.ui.workflow.actions.start.ActionStartDialog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
@HopEnv(ui = SpecMode.NONE, withH2 = true)
class HopExtensionIT {

  @TestTemplate
  void previewTransformUi(DummyDialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  void previewTransformUi2(RowGeneratorDialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  void previewTransformUi(InjectorDialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  void previewActionUi(ActionDummyDialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  void previewActionUi(ActionStartDialog dialog) {
    Assertions.assertNotNull(dialog);
  }
}
