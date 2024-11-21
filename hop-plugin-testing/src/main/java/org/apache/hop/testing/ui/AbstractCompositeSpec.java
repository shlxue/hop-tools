package org.apache.hop.testing.ui;

import org.apache.hop.ui.util.AsyncUi;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

abstract class AbstractCompositeSpec<C extends Control, P extends Composite>
    extends AbstractControlSpec<P> {
  protected final NestedUi nestedUi;
  private final Class<C> childClass;

  AbstractCompositeSpec(AsyncUi asyncUi, Class<C> childClass) {
    super(asyncUi);
    this.nestedUi = NestedUi.of(asyncUi);
    this.childClass = childClass;
  }

  protected void forEach(Composite parent, boolean reverse) {
    nestedUi.forEach(false, reverse, parent, childClass, this::filterChild, this::applyChild);
  }

  protected void forEachTabList(Composite parent, boolean reverse) {
    nestedUi.forEachTabList(
        false, reverse, parent, childClass, this::filterChild, this::applyChild);
  }

  protected <W extends Control> W search(Class<W> clazz, Predicate<W> filter) {
    AtomicReference<W> reference = new AtomicReference<>();
    nestedUi.forEach(true, true, shell, clazz, filter, reference::set);
    return reference.get();
  }

  protected boolean filterChild(C control) {
    return true;
  }

  protected void applyChild(C child) {}
}
