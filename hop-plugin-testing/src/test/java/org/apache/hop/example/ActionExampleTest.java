package org.apache.hop.example;

import org.apache.hop.testing.HopExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
class ActionExampleTest {

  @TestTemplate
  void previewActionExample(ActionExampleDialog dialog) {
    Assertions.assertNotNull(dialog);
  }
}
