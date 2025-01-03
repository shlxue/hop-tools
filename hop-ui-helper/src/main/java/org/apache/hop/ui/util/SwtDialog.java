package org.apache.hop.ui.util;

import org.apache.hop.ui.widgets.Adapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class SwtDialog {
  private SwtDialog() {}

  public static Point hintSize(Shell shell) {
    return shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
  }

  public static Point preferredSize(Shell shell) {
    return Monitors.preferredSize(hintSize(shell));
  }

  public static Point[] preferredSizes(Shell shell) {
    return Monitors.preferredSizes(hintSize(shell));
  }

  public static void cleanTabs(Composite composite) {
    cleanTabs(composite, Label.class::isInstance);
  }

  public static Point screenSize(Shell shell) {
    return Monitors.getClientAreaSize(shell.getMonitor());
  }

  private static int nestedOffset(Composite composite) {
    if (composite instanceof CTabFolder || composite instanceof TabFolder) {
      return 0;
    }
    boolean border = (composite.getStyle() & SWT.BORDER) != 0;
    int offset = 0;
    int depth = 0;
    while (composite != null && !(composite instanceof Shell)) {
      offset++;
      if ((composite.getStyle() & SWT.BORDER) != 0) {
        offset++;
      }
      if (composite.getLayout() instanceof FormLayout layout && layout.spacing > 0) {
        offset++;
      }
      depth++;
      composite = composite.getParent();
    }
    return -offset - (border ? (-((depth - 1) / 5)) : ((depth - 1) / 4));
  }

  public static void fixNestedFormOffset(Composite parent) {
    int offset = nestedOffset(parent);
    if (offset != 0 && parent.getLayout() instanceof FormLayout) {
      for (Control control : parent.getChildren()) {
        fixLayoutOffsetIfNeed(control, offset);
      }
    }
    for (Control control : parent.getChildren()) {
      if (control instanceof Composite) {
        fixNestedFormOffset((Composite) control);
      }
    }
  }

  private static void fixLayoutOffsetIfNeed(Control control, int offset) {
    if (control.getLayoutData() instanceof FormData formData) {
      FormAttachment attachment = formData.left;
      if (attachment != null) {
        if (attachment.numerator > 0 && attachment.denominator > 0 && attachment.offset == 0) {
          attachment.offset = offset;
        }
      }
      attachment = formData.top;
      if (attachment != null
          && attachment.offset == 0
          && attachment.control instanceof Button wButton
          && (wButton.getStyle() & SWT.CHECK) != 0) {
        attachment.offset = -4;
      }
    }
  }

  public static Control lastButton(Composite parent) {
    Control[] children = parent.getChildren();
    return children[children.length - 1];
  }

  public static void cleanTabs(Composite composite, Predicate<Control> filter) {
    List<Control> tabList = new ArrayList<>(Arrays.asList(composite.getTabList()));
    tabList.removeIf(filter);
    for (Control control : composite.getChildren()) {
      if (control instanceof Composite) {
        cleanTabs(Composite.class.cast(control), filter);
      }
    }
    composite.setTabList(tabList.toArray(Control[]::new));
  }

  public static boolean supportAutoLayout(Shell shell, boolean excludeBottomButtons) {
    Control[] children = shell.getChildren();
    if (excludeBottomButtons) {
      children = ignoreBottomButtons(shell.getChildren(), shell.getDefaultButton());
    }
    return supportAutoLayout(shell, children);
  }

  //  public static boolean supportAutoLayout(Composite parent) {
  //    return supportAutoLayout(parent, parent.getChildren());
  //  }

  public static void preferredShellStyle(Shell shell, Button defaultButton) {
    preferredShellStyle(shell, defaultButton, true, true);
  }

  public static void preferredShellStyle(
      Shell shell, Button defaultButton, boolean cleanTabs, boolean preferred) {
    shell.setDefaultButton(defaultButton);
    shell.pack(true);
    if (cleanTabs) {
      cleanTabs(shell);
    }
    fixNestedFormOffset(shell);

    shell.setRedraw(false);
    try {
      if ((shell.getStyle() & SWT.RESIZE) != 0 && preferred) {
        Point hintSize = hintSize(shell);
        Point preferredSize = preferredSize(shell);
        shell.setMinimumSize(hintSize);
        if (supportAutoLayout(shell, true)) {
          //        if (supportAutoLayout(
          //            shell, ignoreBottomButtons(shell.getChildren(), shell.getDefaultButton())))
          // {
          shell.setSize(preferredSize);
          //          shell.setMinimumSize(hintSize);
        } else {
          //          shell.setMinimumSize(preferredSize.x, hintSize.y);
          shell.setSize(preferredSize.x, hintSize.y);
          Point[] templates = Monitors.preferredSizes(hintSize);
          if (templates.length > 0) {
            shellMaximize(shell, templates[Math.min(templates.length - 1, 3)].x, hintSize.y);
          }
        }
      }
    } finally {
      //      shell.pack(false);
      screenCenter(shell);
      shell.setRedraw(true);
    }
  }

  private static void shellMaximize(Shell shell, int x, int y) {
    try {
      shell.getClass().getMethod("setMaximumSize", Point.class).invoke(shell, new Point(x, y));
    } catch (ReflectiveOperationException ignore) {
      // ignore
    }
  }

  public static void defaultShellHanding(Shell shell) {
    applyDefaultIconsOnTabs(shell);

    runEventLoop(shell);
  }

  public static void defaultShellHanding(
      Shell shell, Consumer<SelectionEvent> okConsumer, Consumer<SelectionEvent> cancelConsumer) {
    if (shell.getDefaultButton() != null) {
      shell.getDefaultButton().addSelectionListener(Adapter.widgetSelected(okConsumer));
    }
    shell.addShellListener(Adapter.shellClosed(e -> cancelConsumer.accept(null)));

    defaultShellHanding(shell);
  }

  public static void runEventLoop(Shell loopShell) {
    loopShell.open();
    Display display = loopShell.getDisplay();
    while (!loopShell.isDisposed()) {
      try {
        if (!display.readAndDispatch()) {
          display.sleep();
        }
      } catch (Exception e) {
        handleException(e);
      }
    }
  }

  private static void screenCenter(Shell shell) {
    Point size = shell.getSize();
    Point areaSize = Monitors.getClientAreaSize(shell.getMonitor());
    Point location = Monitors.getClientArea(shell.getMonitor());
    shell.setLocation(
        new Point((areaSize.x - size.x) / 2 + location.x, (areaSize.y - size.y) / 2 + location.y));
  }

  private static Control[] ignoreBottomButtons(Control[] children, Button defaultButton) {
    List<Control> list = new ArrayList<>(Arrays.asList(children));
    if (defaultButton != null) {
      int y = defaultButton.getLocation().y;
      list.removeIf(control -> isBottomButton(control, y));
    }
    return list.toArray(Control[]::new);
  }

  private static boolean isBottomButton(Control control, int y) {
    int currentY = control.getLocation().y;
    return control instanceof Button && (currentY >= y || Math.abs(currentY - y) < 3);
  }

  private static int getBottom(Control[] children) {
    return Arrays.stream(children)
        .mapToInt(control -> toLocation(control) + control.getSize().y)
        .max()
        .orElse(0);
  }

  private static int toLocation(Control control) {
    int y = 0;
    while (!(control instanceof Shell)) {
      y += control.getLocation().y;
      control = control.getParent();
    }
    return y;
  }

  //  private static boolean supportAutoLayout(Control... children) {
  //    return Stream.of(children).anyMatch(SwtDialog::isAutoLayout);
  //  }

  //  private static boolean supportAutoLayout(Shell shell, Control... children) {
  //    shell.layout(true, true);
  //    int before = shell.getSize().y - getBottom(children);
  //    Point size = shell.getSize();
  //    shell.setSize(size.x * 2, size.y * 2);
  //    shell.layout(true, true);
  //    //    shell.setRedraw(true);
  //    //    shell.layout();
  //    //    shell.redraw();
  //    int after = shell.getSize().y - getBottom(children);
  //    return Math.abs(before - after) < 5;
  //  }

  public static boolean supportAutoLayout(Composite parent, Control[] children) {
    return parent.getLayout() instanceof FormLayout
        && Arrays.stream(children).anyMatch(SwtDialog::isAutoLayout);
  }

  private static boolean isAutoLayout(Control control) {
    if (control.getLayoutData() instanceof FormData formData && formData.bottom != null) {
      FormAttachment attachment = formData.bottom;
      return attachment.control != null || attachment.numerator > 10;
    }
    return false;
  }

  private static void applyDefaultIconsOnTabs(Composite composite) {
    for (Control control : composite.getChildren()) {
      if (control instanceof CTabFolder cTabFolder) {
        for (CTabItem item : cTabFolder.getItems()) {
          if (item.getImage() == null) {
            // item.setImage(GuiResource.getInstance().getImageHop());
          }
        }
      }
    }
  }

  private static void handleException(Throwable throwable) {
    if (throwable instanceof ThreadDeath) {
      throw ThreadDeath.class.cast(throwable);
    }
    if (throwable instanceof SWTException swtErr && swtErr.code == SWT.ERROR_WIDGET_DISPOSED) {
      return;
    }
    if (throwable instanceof RuntimeException) {
      throw RuntimeException.class.cast(throwable);
    }
    if (throwable instanceof Error) {
      throw Error.class.cast(throwable);
    }
    SWT.error(SWT.ERROR_UNSPECIFIED, throwable);
  }
}
