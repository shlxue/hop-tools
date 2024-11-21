package org.apache.hop.ui.layout;

import org.eclipse.swt.widgets.Layout;

import java.util.function.Supplier;

abstract class BaseLayoutFactory<F extends AbstractLayoutFactory<?, ?>, L extends Layout>
    extends AbstractLayoutFactory<F, L> {

  BaseLayoutFactory(Class<F> factoryClass, Supplier<L> creator) {
    super(factoryClass, creator);
  }

  public F margin(int value) {
    return margin(value, value, value, value);
  }

  public abstract F spacing(int spacing);

  public abstract F margin(int top, int right, int bottom, int left);
}
