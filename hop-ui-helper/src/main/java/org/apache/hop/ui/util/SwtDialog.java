package org.apache.hop.ui.util;

import org.apache.hop.ui.core.gui.GuiResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

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

  public static boolean supportAutoLayout(Shell shell) {
    return supportAutoLayout(
        shell, ignoreBottomButtons(shell.getChildren(), shell.getDefaultButton()));
  }

  public static void preferredShellStyle(Shell shell, Button defaultButton) {
    preferredShellStyle(shell, defaultButton, true, true);
  }

  public static void preferredShellStyle(
      Shell shell, Button defaultButton, boolean cleanTabs, boolean preferred) {
    shell.setDefaultButton(defaultButton);
    if (cleanTabs) {
      cleanTabs(shell);
    }

    if ((shell.getStyle() & SWT.RESIZE) != 0 && preferred) {
      Point hintSize = hintSize(shell);
      Point preferredSize = preferredSize(shell);

      shell.setRedraw(true);
      try {
        if (supportAutoLayout(
            shell, ignoreBottomButtons(shell.getChildren(), shell.getDefaultButton()))) {
          shell.setMinimumSize(preferredSize);
          return;
        }
        shell.setMinimumSize(preferredSize.x, hintSize.y);
        Point[] templates = Monitors.preferredSizes(hintSize);
        if (templates.length > 0) {
          shell.setMaximumSize(templates[Math.min(templates.length - 1, 5)].x, hintSize.y);
        }
        shell.pack(false);
      } finally {
        shell.setRedraw(false);
      }
    }
  }

  public static void defaultShellHanding(Shell shell) {
    applyDefaultIconsOnTabs(shell);

    runEventLoop(shell);
  }

  public static void defaultShellHanding(
      Shell shell, Consumer<SelectionEvent> okConsumer, Consumer<SelectionEvent> cancelConsumer) {
    applyDefaultListeners(shell.getDefaultButton(), okConsumer);
    shell.addListener(SWT.CLOSE, e -> cancelConsumer.accept(null));

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
        .mapToInt(control -> control.getLocation().y + control.getSize().y)
        .max()
        .orElse(0);
  }

  private static boolean supportAutoLayout(Shell shell, Control... children) {
    int before = shell.getSize().y - getBottom(children);
    Point size = shell.getSize();
    shell.setSize(size.x * 2, size.y * 2);
    int after = shell.getSize().y - getBottom(children);
    return Math.abs(before - after) < 5;
  }

  private static void applyDefaultListeners(
      Button defaultButton, Consumer<SelectionEvent> okConsumer) {
    if (defaultButton != null) {
      defaultButton.addSelectionListener(new DefaultSelectListener(okConsumer));
    }
  }

  private static void applyDefaultIconsOnTabs(Composite composite) {
    for (Control control : composite.getChildren()) {
      if (control instanceof CTabFolder cTabFolder) {
        for (CTabItem item : cTabFolder.getItems()) {
          if (item.getImage() == null) {
            item.setImage(GuiResource.getInstance().getImageHop());
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

  private static class DefaultSelectListener implements SelectionListener {

    final Consumer<SelectionEvent> consumer;

    DefaultSelectListener(Consumer<SelectionEvent> consumer) {
      this.consumer = consumer;
    }

    @Override
    public void widgetSelected(SelectionEvent event) {
      consumer.accept(event);
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent event) {
      consumer.accept(event);
    }
  }
}
