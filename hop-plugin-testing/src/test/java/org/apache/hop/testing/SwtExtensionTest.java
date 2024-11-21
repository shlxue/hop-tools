package org.apache.hop.testing;

import org.apache.hop.ui.layout.LData;
import org.apache.hop.ui.layout.Layouts;
import org.apache.hop.ui.widgets.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
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
    cTabFolder(tabFolder, SWT.NONE, 15);
  }

  @TestTemplate
  void testTabFolderParam2(CTabFolder tabFolder) {
    assertNotNull(tabFolder);
    cTabFolder(tabFolder, SWT.BORDER, 15);
  }

  private void cTabFolder(CTabFolder tabFolder, int style, int depth) {
    if (tabFolder.getParent().getLayout() == null) {
      tabFolder.getParent().setLayout(Layouts.defaultForm());
    }
    if (tabFolder.getParent().getLayout() instanceof FormLayout layout) {
      if (layout.spacing == 0) {
        layout.spacing = 8;
      }
      if (layout.marginLeft == 0) {
        layout.marginLeft = 12;
      }
      if (layout.marginTop == 0) {
        layout.marginTop = 12;
      }
      if (layout.marginRight == 0) {
        layout.marginRight = 12;
      }
      if (layout.marginBottom == 0) {
        layout.marginBottom = 12;
      }
    }
    tabFolder.setLayoutData(LData.fill(null, null, 120, false));
    Composite wPanel = Widgets.composite(style).layout(Layouts.defaultForm()).create(tabFolder);
    Widgets.C.tabItem(style | SWT.BOTTOM).text("tab" + depth).control(wPanel).create(tabFolder);
    tabFolder.setSelection(0);
    Text wName = Widgets.text(SWT.BORDER).layoutData(LData.byTop(null)).create(wPanel);
    Widgets.label(SWT.RIGHT).text("Name").layoutData(LData.on(wName)).create(wPanel);
    wName.addListener(
        SWT.RESIZE,
        event -> {
          wName.setText(
              String.format(
                  "%s -> %s -> %s",
                  wName.getBounds(),
                  wName.getParent().getBounds(),
                  wName.getParent().getParent().getBounds()));
          wName.redraw();
        });
    if (depth > 0) {
      CTabFolder sub =
          Widgets.C.tabFolder(SWT.BORDER | SWT.BOTTOM)
              .layoutData(LData.fill(wName, null, 120, false))
              .create(wPanel);
      cTabFolder(sub, style, depth - 1);
    }
  }
}
