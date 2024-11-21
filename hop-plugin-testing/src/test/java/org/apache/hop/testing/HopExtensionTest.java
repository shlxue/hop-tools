package org.apache.hop.testing;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.FileDialog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(HopExtension.class)
class HopExtensionTest {

  @TestTemplate
  void testFileDialog(FileDialog dialog) {
    assertNotNull(dialog);
  }

  @TestTemplate
  void testSwtControl(Button button) {
    assertNotNull(button);
  }

  @Test
  void testTransform() {
    //    assertNotNull(dummy);
  }

  @Test
  void testAction() {
    //    assertNotNull(dummy);
  }
}
