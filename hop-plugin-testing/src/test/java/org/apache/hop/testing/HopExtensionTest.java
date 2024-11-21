package org.apache.hop.testing;

import org.eclipse.swt.widgets.Button;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(HopExtension.class)
class HopExtensionTest {

  @TestTemplate
  void testSwtControl(Button button) {
    assertNotNull(button);
  }
}
