package org.apache.hop.testing.params;

import org.apache.hop.testing.SwtExtension;
import org.apache.hop.ui.layout.LData;
import org.apache.hop.ui.layout.Layouts;
import org.apache.hop.ui.widgets.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwtExtension.class)
class SwtParamsTest {

  @TestTemplate
  void testCTabFolderWidget(CTabFolder tabFolder) {
    Assertions.assertNotNull(tabFolder);
    Composite parent = tabFolder.getParent();
    tabFolder.dispose();
    parent.setLayout(Layouts.defaultForm());
    tabFolder =
        Widgets.C.tabFolder(SWT.BORDER)
            .layoutData(LData.fill(null, parent, 120, false))
            .create(parent);
    Widgets.C.tabItem(SWT.NONE).text("ti1").create(tabFolder);
    Widgets.C.tabItem(SWT.NONE).text("ti2").create(tabFolder);
    //    SwtDialog.defaultShellHanding(parent.getShell());
  }

  @TestTemplate
  void testFolderWidget(Group group) {
    Assertions.assertNotNull(group);
  }
}
