package org.apache.hop.testing.params;

import org.apache.hop.testing.SwtExtension;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwtExtension.class)
class SwtParamsTest {

  @TestTemplate
  void testTabFolderWidget(CTabFolder tabFolder) {
    Assertions.assertNotNull(tabFolder);
  }

  @TestTemplate
  void testFolderWidget(Group group) {
    Assertions.assertNotNull(group);
  }

  @TestTemplate
  void testDialogType(FileDialog dialog) {
    Assertions.assertNotNull(dialog);
  }
}
