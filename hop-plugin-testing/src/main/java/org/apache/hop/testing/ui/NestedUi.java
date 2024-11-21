package org.apache.hop.testing.ui;

import org.apache.hop.ui.util.AsyncUi;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

class NestedUi {
  private final AsyncUi asyncUi;

  static NestedUi of(AsyncUi asyncUi) {
    return new NestedUi(asyncUi);
  }

  private NestedUi(AsyncUi asyncUi) {
    this.asyncUi = asyncUi;
  }

  public <W extends Widget> void forEach(
      boolean fastShift,
      boolean reverse,
      Composite parent,
      Class<W> clazz,
      Predicate<W> filter,
      Consumer<W> action) {
    Function<Composite, Control[]> getter =
        reverse ? p -> reversed(p.getChildren()) : Composite::getChildren;
    doForEach(fastShift, parent, p -> asyncUi.get(() -> getter.apply(p)), clazz, filter, action);
  }

  public <W extends Widget> void forEachTabList(
      boolean fastShift,
      boolean reverse,
      Composite parent,
      Class<W> clazz,
      Predicate<W> filter,
      Consumer<W> action) {
    Function<Composite, Control[]> getter =
        reverse ? p -> reversed(p.getTabList()) : Composite::getTabList;
    doForEach(fastShift, parent, p -> asyncUi.get(() -> getter.apply(p)), clazz, filter, action);
  }

  private <W extends Widget> void doForEach(
      boolean fastShift,
      Composite parent,
      Function<Composite, Control[]> childrenGetter,
      Class<W> clazz,
      Predicate<W> filter,
      Consumer<W> action) {
    for (Control item : childrenGetter.apply(parent)) {
      if (clazz.isAssignableFrom(item.getClass())) {
        W child = clazz.cast(item);
        if (Boolean.TRUE.equals(asyncUi.get(() -> filter.test(child)))) {
          asyncUi.runInUiThread(() -> action.accept(child));
          if (fastShift) {
            return;
          }
        }
      }
      if (item instanceof Composite subPanel) {
        if (Composite.class.equals(item.getClass()) || Group.class.equals(item.getClass())) {
          doForEach(fastShift, subPanel, childrenGetter, clazz, filter, action);
        } else if (subPanel instanceof TabFolder tabFolder) {
          for (TabItem tabItem : tabFolder.getItems()) {
            asyncUi.runInUiThread(() -> tabFolder.setSelection(tabItem));
            Composite panel = (Composite) asyncUi.get(tabItem::getControl);
            doForEach(fastShift, panel, childrenGetter, clazz, filter, action);
          }
        } else if (subPanel instanceof CTabFolder cTabFolder) {
          for (CTabItem tabItem : cTabFolder.getItems()) {
            asyncUi.runInUiThread(() -> cTabFolder.setSelection(tabItem));
            Composite panel = (Composite) asyncUi.get(tabItem::getControl);
            doForEach(fastShift, panel, childrenGetter, clazz, filter, action);
          }
        }
      }
    }
  }

  private Control[] reversed(Control[] children) {
    List<Control> list = Arrays.asList(children);
    Collections.reverse(Arrays.asList(children));
    return list.toArray(Control[]::new);
  }
}
