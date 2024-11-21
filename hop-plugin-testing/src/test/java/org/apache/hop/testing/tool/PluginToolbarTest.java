package org.apache.hop.testing.tool;

import org.apache.hop.testing.SwtExtension;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwtExtension.class)
class PluginToolbarTest {
  static Shell shell;

  @TestTemplate
  void show(EventViewer viewer) {
    Assertions.assertNotNull(viewer);
  }
}
