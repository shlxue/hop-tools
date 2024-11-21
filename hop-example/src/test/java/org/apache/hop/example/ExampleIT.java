package org.apache.hop.example;

import org.apache.hop.example.demo1.Demo1Dialog;
import org.apache.hop.example.demo2.ActionAbortDialog;
import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.HopExtension;
import org.apache.hop.testing.SpecMode;
import org.apache.hop.transforms.cdc.OpEventInputDialog;
import org.apache.hop.transforms.cdc.SubOpEventDialog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
@HopEnv(withH2 = true, ui = SpecMode.PREVIEW)
class ExampleIT {

  @TestTemplate
  void testActionAbortUi(ActionAbortDialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  void testDemo1Ui(Demo1Dialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  void testOpEventInputUi(OpEventInputDialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  void testSubOpEventUi(SubOpEventDialog dialog) {
    Assertions.assertNotNull(dialog);
  }
}
