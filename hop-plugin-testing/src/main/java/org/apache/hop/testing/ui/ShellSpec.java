package org.apache.hop.testing.ui;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hop.testing.SpecMode;
import org.apache.hop.ui.core.widget.ComboVar;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.util.AsyncUi;
import org.apache.hop.ui.util.SwtDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.Assertions;
import org.junit.platform.commons.util.ExceptionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

final class ShellSpec {
  private static final Method getMaximumSizeMethod;

  private ShellSpec() {}

  static {
    Method method = null;
    try {
      method = Shell.class.getMethod("getMaximumSize");
    } catch (NoSuchMethodException e) {
      // ignore
    }
    getMaximumSizeMethod = method;
  }

  static class AutoClose extends AbstractShellSpec<Control> {

    AutoClose(AsyncUi asyncUi) {
      super(asyncUi);
    }

    @Override
    public void invoke(Shell target, SpecMode mode, Shell dispatcher) {
      super.invoke(target, mode, dispatcher);
      if (Boolean.FALSE.equals(asyncUi.get(shell::isDisposed))) {
        asyncUi.runInUiThread(shell::dispose);
      }
    }
  }

  static class DelayClose extends AbstractShellSpec<Control> {

    DelayClose(AsyncUi asyncUi) {
      super(asyncUi);
    }

    @Override
    public void invoke(Shell target, SpecMode mode, Shell dispatcher) {
      super.invoke(target, mode, dispatcher);
      if (Boolean.FALSE.equals(asyncUi.get(shell::isDisposed))) {
        delay(mode.getWaitTimeMs() * (mode.isColorized() ? 2 : 1));
        asyncUi.runInUiThread(shell::dispose);
      }
    }
  }

  static class OkListener extends AbstractShellSpec<Button> {
    OkListener(AsyncUi asyncUi) {
      super(asyncUi, Button.class);
    }

    @Override
    public void invoke(Shell target, SpecMode mode, Shell dispatcher) {
      super.invoke(target, mode, dispatcher);
      Button wOk = search(Button.class, this::isOkButton);
      Assertions.assertNotNull(wOk, "Not found ok button on the dialog: " + uiName());
      AtomicReference<Throwable> err = new AtomicReference<>();
      delay(100);
      asyncUi.runInUiThread(() -> notifyOkListener(wOk, err));
      if (err.get() != null) {
        Assertions.assertDoesNotThrow(
            err::get, "Received an error:" + ExceptionUtils.readStackTrace(err.get()));
      }
      Assertions.assertDoesNotThrow(
          () -> waitShellDispose(wOk), "Ok listener don't work after trigger the listener");
    }

    private void notifyOkListener(Button wOk, AtomicReference<Throwable> err) {
      try {
        wOk.notifyListeners(SWT.Selection, new Event());
      } catch (Throwable e) {
        err.set(e);
      }
    }

    private Throwable waitShellDispose(Button wOk) {
      long waiting = System.currentTimeMillis() + 1000;
      try {
        while (!wOk.isDisposed()) {
          delay(5);
          if (System.currentTimeMillis() < waiting) {
            throw new IllegalStateException(
                "Ok listener don't work after waiting for " + waiting + " ms");
          }
        }
      } catch (Throwable e) {
        return e;
      }
      return null;
    }
  }

  static class CancelListener extends AbstractShellSpec<Button> {
    CancelListener(AsyncUi asyncUi) {
      super(asyncUi, Button.class);
    }

    @Override
    public void invoke(Shell target, SpecMode mode, Shell dispatcher) {
      super.invoke(target, mode, dispatcher);
      Button wCancel = search(Button.class, this::isCancelButton);
      Assertions.assertNotNull(wCancel, "Not found cancel button on the dialog: " + uiName());
      AtomicReference<Throwable> err = new AtomicReference<>();
      delay(10);
      asyncUi.runInUiThread(() -> notifyCancelListener(wCancel, err));
      if (err.get() != null) {
        Assertions.assertDoesNotThrow(
            err::get, "Received an error:" + ExceptionUtils.readStackTrace(err.get()));
      }
      Assertions.assertDoesNotThrow(
          () -> waitShellDispose(wCancel), "Ok listener don't work after trigger the listener");
    }

    private void notifyCancelListener(Button wCancel, AtomicReference<Throwable> err) {
      try {
        Event event = new Event();
        event.widget = wCancel;
        wCancel.notifyListeners(SWT.Selection, event);
      } catch (Throwable e) {
        err.set(e);
      }
    }

    private Throwable waitShellDispose(Button wCancel) {
      long waiting = System.currentTimeMillis() + 1000;
      try {
        while (!wCancel.isDisposed()) {
          delay(5);
          if (System.currentTimeMillis() < waiting) {
            throw new IllegalStateException(
                "Ok listener don't work after waiting for " + waiting + " ms");
          }
        }
      } catch (IllegalStateException e) {
        return e;
      }
      return null;
    }
  }

  static class PreviewAutoLayout extends AbstractShellSpec<Control> {
    PreviewAutoLayout(AsyncUi asyncUi) {
      super(asyncUi);
    }

    @Override
    public void invoke(Shell target, SpecMode mode, Shell dispatcher) {
      super.invoke(target, mode, dispatcher);
      int style = getStyle();
      Point hintSize = asyncUi.get(() -> SwtDialog.hintSize(shell));
      Point screenSize = screenSize();
      Point screenFix = screenFix();
      String title = asyncUi.get(shell::getText);
      try {
        asyncUi.runInUiThread(() -> adjustSize(hintSize, title, screenSize, screenFix));
        if ((style & SWT.RESIZE) != 0) {
          Point[] preferredSizes = asyncUi.get(() -> SwtDialog.preferredSizes(shell));
          Point max = asyncUi.get(() -> maximumSize(shell));
          AtomicBoolean skipDefault = new AtomicBoolean(true);
          Predicate<Point> filter = size -> size.x <= max.x && !skipDefault.get();
          AtomicInteger tabCount = new AtomicInteger();
          CTabFolder tabFolder =
              asyncUi.tryGet(() -> searchTabFolder(shell, tabCount)).orElse(null);
          for (Point size : preferredSizes) {
            if (tabCount.get() > 0 && tabFolder != null) {
              asyncUi.runInUiThread(() -> tabFolder.setSelection(tabCount.decrementAndGet()));
            }
            if (filter.test(size)) {
              size.y = Math.min(max.y, size.y);
              delay();
              asyncUi.runInUiThread(() -> adjustSize(size, title, screenSize, screenFix));
            }
            skipDefault.set(false);
          }
        }
      } finally {
        asyncUi.runInUiThread(() -> shell.setText(title));
      }
      assertLayout(asyncUi.get(() -> shell.getLayout()));
    }

    private CTabFolder searchTabFolder(Shell shell, AtomicInteger count) {
      int style = shell.getStyle();
      if ((style & SWT.MIN) != 0 && (style & SWT.MAX) != 0) {
        CTabFolder wCTabFolder =
            (CTabFolder)
                Arrays.stream(shell.getChildren())
                    .filter(CTabFolder.class::isInstance)
                    .findFirst()
                    .orElse(null);
        if (wCTabFolder != null) {
          count.set(wCTabFolder.getItemCount());
          return wCTabFolder;
        }
      }
      return null;
    }

    private Point screenFix() {
      Rectangle clientArea = asyncUi.get(() -> shell.getMonitor().getClientArea());
      return new Point(clientArea.x, clientArea.y);
    }

    private void adjustSize(Point size, String title, Point screenArea, Point screenFix) {
      shell.setText(String.format("%s size: %s", title, size));
      Point central = new Point(screenArea.x / 2, screenArea.y / 2);
      Point half = new Point(size.x / 2, size.y / 2);
      int y = central.y - half.y + screenFix.y;
      int style = shell.getStyle();
      if ((style & SWT.MIN) != 0 && (style & SWT.MAX) != 0) {
        shell.setBounds(screenFix.x, screenFix.y, size.x, size.y);
      } else {
        shell.setBounds(central.x - half.x + screenFix.x, y, size.x, size.y);
      }
    }

    private void assertLayout(Layout layout) {
      if (layout instanceof FormLayout fl) {
        Assertions.assertArrayEquals(
            new boolean[] {true, true, true, true, true},
            new boolean[] {
              fl.spacing > 0,
              fl.marginLeft > 0,
              fl.marginTop > 0,
              fl.marginRight > 0,
              fl.marginBottom > 0
            },
            "These spacings of the dialog be greater than zero: [left, top, right, bottom] & spacing");
      }
    }
  }

  static class DefaultButton extends AbstractShellSpec<Button> {

    DefaultButton(AsyncUi asyncUi) {
      super(asyncUi);
    }

    @Override
    public void invoke(Shell target, SpecMode mode, Shell dispatcher) {
      super.invoke(target, mode, dispatcher);
      Button wOk = search(Button.class, this::isOkButton);
      if (wOk != null) {
        Assertions.assertTrue(
            getDefaultButton().isPresent(), "The default button of the dialog should be specified");
      }
    }
  }

  static class LazyApplyTheme extends AbstractShellSpec<Control> {

    LazyApplyTheme(AsyncUi asyncUi) {
      super(asyncUi);
    }

    @Override
    public void invoke(Shell target, SpecMode mode, Shell dispatcher) {
      super.invoke(target, mode, dispatcher);
      int childShellCount = (int) asyncUi.get(() -> dispatcher.getData("CHILD_SHELL_COUNT"));
      if (childShellCount > 0) {
        Color c1 = asyncUi.get(dispatcher::getBackground);
        Color c2 = asyncUi.get(() -> shell.getBackground());
        Assertions.assertArrayEquals(
            new int[] {c1.getBlue(), c1.getRed(), c1.getGreen()},
            new int[] {c2.getBlue(), c2.getRed(), c2.getGreen()},
            "Apply the theme on open method");
      }
    }
  }

  static class Minimum extends AbstractShellSpec<Control> {

    Minimum(AsyncUi asyncUi) {
      super(asyncUi);
    }

    @Override
    public void invoke(Shell target, SpecMode mode, Shell dispatcher) {
      super.invoke(target, mode, dispatcher);
      int style = asyncUi.get(() -> shell.getStyle());
      if ((style & SWT.RESIZE) != 0) {
        asyncUi.runInUiThread(() -> shell.setSize(100, 90));
        Point size = asyncUi.get(shell::getSize);
        Assertions.assertFalse(
            size.x <= 100 || size.y <= 90, "The minimum size of dialog should be specified");
      }
    }
  }

  static class CheckAutoLayout extends AbstractShellSpec<Button> {

    CheckAutoLayout(AsyncUi asyncUi) {
      super(asyncUi, Button.class);
    }

    @Override
    public void invoke(Shell target, SpecMode mode, Shell dispatcher) {
      super.invoke(target, mode, dispatcher);
      int style = asyncUi.get(shell::getStyle);
      if ((style & SWT.RESIZE) != 0) {
        Button wOk = getDefaultButton().orElseGet(() -> search(Button.class, this::isOkButton));
        Assertions.assertNotNull(wOk, "The default button of the dialog should be specified");
        Assertions.assertTrue(
            asyncUi.get(() -> SwtDialog.supportAutoLayout(shell, true)) || (style & SWT.MIN) == 0,
            "SWt.MIN should be removed if the dialog don't support auto layout");
        Control[] children = asyncUi.get(() -> shell.getChildren());
        CTabFolder cTabFolder = findTabFolder(children, CTabFolder.class);
        if (cTabFolder != null) {
          Assertions.assertTrue(
              asyncUi.get(
                  () -> supportAutoLayout(cTabFolder, CTabFolder::getItems, CTabItem::getControl)),
              "SWt.MIN should be removed if the dialog don't support auto layout");
        }
        TabFolder tabFolder = findTabFolder(children, TabFolder.class);
        if (tabFolder != null) {
          Assertions.assertTrue(
              asyncUi.get(
                  () -> supportAutoLayout(tabFolder, TabFolder::getItems, TabItem::getControl)),
              "SWt.MIN should be removed if the dialog don't support auto layout");
        }
      }
    }

    private <TF extends Composite, TI> boolean supportAutoLayout(
        TF tabFolder, Function<TF, TI[]> tabItemsGetter, Function<TI, Control> to) {
      return Arrays.stream(tabItemsGetter.apply(tabFolder))
          .map(to)
          .map(Composite.class::cast)
          .anyMatch(p -> SwtDialog.supportAutoLayout(p, p.getChildren()));
    }

    private <T> T findTabFolder(Control[] children, Class<T> type) {
      return Arrays.stream(children)
          .filter(c -> type.equals(c.getClass()))
          .map(control -> type.cast(control))
          .findFirst()
          .orElse(null);
    }
  }

  static class SwitchFocus extends AbstractShellSpec<Control> {
    private String title;
    private final AtomicInteger position = new AtomicInteger();

    SwitchFocus(AsyncUi asyncUi) {
      super(asyncUi);
    }

    @Override
    public void invoke(Shell target, SpecMode mode, Shell dispatcher) {
      super.invoke(target, mode, dispatcher);
      position.set(0);
      title = asyncUi.get(() -> shell.getText());
      try {
        forEachTabList(shell, false);
      } finally {
        asyncUi.runInUiThread(() -> shell.setText(title));
      }
    }

    @Override
    protected boolean filterChild(Control control) {
      position.incrementAndGet();
      return !(control instanceof Label);
    }

    @Override
    protected void applyChild(Control child) {
      if (child.isEnabled() && child.isVisible() && !child.isFocusControl()) {
        child.forceFocus();
        shell.setText(
            String.format(
                "%s: Focus: %d. %s(%s)",
                title, position.get(), child.getClass().getSimpleName(), child.getLocation()));
        changeValue(child);
        delay(Math.min(1000, mode.getWaitTimeMs()));
      }
    }

    private void changeValue(Control child) {
      if (child instanceof Label || child instanceof Group) {
        return;
      }
      if (child instanceof Button wButton) {
        if ((child.getStyle() & SWT.CHECK) != 0) {
          wButton.setSelection(!wButton.getSelection());
        }
        return;
      }
      if (child instanceof Combo wCombo && wCombo.getItemCount() > 0) {
        wCombo.select(wCombo.getSelectionIndex() == -1 ? 0 : wCombo.getItemCount() - 1);
      } else if (child instanceof CCombo wCombo && wCombo.getItemCount() > 0) {
        wCombo.select(wCombo.getSelectionIndex() == -1 ? 0 : wCombo.getItemCount() - 1);
      } else if (child instanceof ComboVar wCombo && wCombo.getItemCount() > 0) {
        wCombo.select(wCombo.getSelectionIndex() == -1 ? 0 : wCombo.getItemCount() - 1);
      } else {
        fillRandomValue(child);
      }
    }

    private void fillRandomValue(Control control) {
      try {
        if (control instanceof Text
            || control instanceof CCombo
            || control instanceof Combo
            || control instanceof TextVar
            || control instanceof ComboVar) {
          control
              .getClass()
              .getMethod("setText", String.class)
              .invoke(control, RandomStringUtils.randomAlphabetic(7));
        }
      } catch (Exception ignored) {
      }
    }
  }

  static class Colorized extends AbstractShellSpec<Control> {
    private final int[] colors;
    private final AtomicInteger times;
    private final AtomicInteger ctlTimes;

    Colorized(AsyncUi asyncUi, int... colors) {
      super(asyncUi);
      this.colors = colors;
      this.times = new AtomicInteger();
      this.ctlTimes = new AtomicInteger();
    }

    @Override
    public void invoke(Shell target, SpecMode mode, Shell dispatcher) {
      super.invoke(target, mode, dispatcher);
      forEach(shell, false);
    }

    @Override
    protected void applyChild(Control child) {
      boolean containerView = child instanceof Composite;
      int index = (containerView ? times : ctlTimes).incrementAndGet() % colors.length;
      RGB rgb = child.getDisplay().getSystemColor(colors[index]).getRGB();
      Display display = child.getDisplay();
      child.setBackground(
          containerView ? new Color(display, rgb, 0x30) : new Color(display, rgb, 0x70));
    }
  }

  static class TabOrder extends AbstractShellSpec<Composite> {
    private final Map<Composite, SortedSet<TabEntry>> panelTabListMap = new HashMap<>();
    private final Queue<TabEntry> errorOrder = new ConcurrentLinkedQueue<>();

    TabOrder(AsyncUi asyncUi) {
      super(asyncUi, Composite.class);
    }

    @Override
    public void invoke(Shell target, SpecMode mode, Shell dispatcher) {
      super.invoke(target, mode, dispatcher);
      panelTabListMap.put(shell, asyncUi.get(() -> wrap(shell.getTabList())));
      forEachTabList(shell, false);
      for (SortedSet<TabEntry> item : panelTabListMap.values()) {
        if (item.size() > 1) {
          assertTabOrder(item.first(), item);
        }
      }
      panelTabListMap.clear();
      Assertions.assertTrue(errorOrder.isEmpty(), () -> dumpErrorOrder(errorOrder));
    }

    @Override
    protected void applyChild(Composite child) {
      panelTabListMap.putIfAbsent(child, wrap(child.getTabList()));
    }

    @Override
    protected boolean filterChild(Composite control) {
      return super.filterChild(control) && !isHopWidget(control) && !isHelpButton(control);
    }

    private String dumpErrorOrder(Queue<TabEntry> errorOrder) {
      return new StringBuilder()
          .append(String.format("Found %d invalid tab orders:", errorOrder.size()))
          .append("\n  ")
          .append(errorOrder.stream().map(Object::toString).collect(Collectors.joining("\n  ")))
          .toString();
    }

    private SortedSet<TabEntry> wrap(Control[] controls) {
      SortedSet<TabEntry> result = new TreeSet<>();
      for (int i = 0; i < controls.length; i++) {
        if (controls[i].isVisible() && !isHelpButton(controls[i])) {
          result.add(new TabEntry(controls[i], i + 1));
        }
      }
      return result;
    }

    private void assertTabOrder(TabEntry prev, SortedSet<TabEntry> children) {
      int activeTab = 0;

      for (TabEntry item : children) {
        if (prev.equals(item)) {
          continue;
        }
        if (item.getIndex() > activeTab) {
          prev = item;
          activeTab = item.getIndex();
        } else {
          item.setPrev(prev);
          errorOrder.add(item);
        }
      }
    }
  }

  static class Tags extends AbstractShellSpec<Control> {
    private final AtomicInteger position = new AtomicInteger();
    private final Map<Control, List<Control>> panelTabLists = new HashMap<>();

    Tags(AsyncUi asyncUi) {
      super(asyncUi);
    }

    @Override
    public void invoke(Shell target, SpecMode mode, Shell dispatcher) {
      super.invoke(target, mode, dispatcher);
      panelTabLists.put(shell, Arrays.asList(asyncUi.get(() -> shell.getTabList())));
      forEachTabList(shell, false);
      panelTabLists.clear();
    }

    @Override
    protected void applyChild(Control child) {
      List<Control> tabList;
      if (child instanceof Composite panel) {
        panelTabLists.putIfAbsent(panel, Arrays.asList(panel.getTabList()));
      }
      tabList = panelTabLists.get(child.getParent());
      if (tabList != null) {
        int tab = tabList.indexOf(child);
        int pos = position.incrementAndGet();
        child.setData(DEBUG_POS_KEY, Integer.toString(pos));
        if (tab >= 0) {
          child.setData(DEBUG_TAB_KEY, Integer.toString(tab + 1));
        }
        child.addListener(SWT.Paint, this::onCustomPaint);
        //        child.addPaintListener(this::onCustomPaint);
        child.redraw();
      }
    }

    @Override
    protected boolean filterChild(Control control) {
      return super.filterChild(control) && !isHelpButton(control);
    }
  }

  static class UiBuilder extends AbstractShellSpec<Control> {

    UiBuilder(AsyncUi asyncUi) {
      super(asyncUi);
    }

    @Override
    public void invoke(Shell target, SpecMode mode, Shell dispatcher) {
      super.invoke(target, mode, dispatcher);
      int childShellCount = (int) asyncUi.get(() -> dispatcher.getData("CHILD_SHELL_COUNT"));
      Assertions.assertTrue(childShellCount > 0, "The ui should be initialized in constructor");
    }
  }

  private static class TabEntry implements Comparable<TabEntry> {
    private final Class<? extends Control> type;
    private final int index;
    private final Point center;
    private TabEntry prev;
    private String label;

    TabEntry(Control control, int index) {
      this.type = control.getClass();
      this.index = index;
      Rectangle bounds = control.getBounds();
      Point size = control.getSize();
      this.center = new Point(bounds.x + size.x / 2, bounds.y + size.y / 2);
      if (control instanceof Label wLabel) {
        this.label = wLabel.getText();
      }
    }

    public int getIndex() {
      return index;
    }

    public void setPrev(TabEntry prev) {
      this.prev = prev;
    }

    @Override
    public int compareTo(TabEntry o) {
      int fixVal = 7;
      int hValue = center.x - o.center.x;
      int vValue = center.y - o.center.y;
      if (Math.abs(vValue) < fixVal) {
        if (isOnEditorButton(o.type, vValue)) {
          return 0;
        }
        return Math.abs(hValue) < fixVal ? 0 : hValue;
      }
      return vValue > 0 ? 1 : -1;
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof TabEntry ref
          && ref.index == index
          && ref.center.x == center.x
          && ref.center.y == center.y;
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(new int[] {index, center.x, center.y});
    }

    @Override
    public String toString() {
      String value = toString(this);
      if (prev != null) {
        if (label != null) {
          value += ", tooltip: " + label;
        }
        return String.format("%s --> %s", toString(prev), value);
      }
      return value;
    }

    private boolean isOnEditorButton(Class<?> clazz, int vValue) {
      return Button.class.isAssignableFrom(clazz) && Math.abs(vValue) <= 1;
    }

    private String toString(TabEntry entry) {
      return String.format(
          "%d. %s({%d, %d})",
          entry.index, entry.type.getSimpleName(), entry.center.x, entry.center.y);
    }
  }

  private static Point maximumSize(Shell shell) {
    if (getMaximumSizeMethod != null) {
      try {
        return (Point) getMaximumSizeMethod.invoke(shell);
      } catch (ReflectiveOperationException e) {
        // ignore
      }
    }
    return new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }
}
