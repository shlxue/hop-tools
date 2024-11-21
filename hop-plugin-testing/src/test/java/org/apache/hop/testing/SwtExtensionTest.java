package org.apache.hop.testing;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SwtExtension.class)
class SwtExtensionTest {
  static Shell parent;

  @Test
  void shouldInitShell() {
    assertNotNull(parent);
  }

  @TestTemplate
  void testShellParam(Shell shell) {
    assertNotNull(shell);
  }

  @TestTemplate
  void testShellParam(Composite composite) {
    assertNotNull(composite);
  }

  @TestTemplate
  void testTabFolderParam(CTabFolder tabFolder) {
    assertNotNull(tabFolder);
  }

  @TestTemplate
  void testDialog(FileDialog dialog) {
    assertNotNull(dialog);
  }
}
