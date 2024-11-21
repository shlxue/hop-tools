package org.apache.hop.ui.layout;

import org.eclipse.swt.widgets.Layout;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

abstract class AbstractLayoutFactory<F extends AbstractLayoutFactory<?, ?>, L extends Layout> {
  private final Class<F> factoryClass;
  private final Supplier<L> creator;
  private final List<Consumer<L>> properties = new ArrayList<>();

  AbstractLayoutFactory(Class<F> factoryClass, Supplier<L> creator) {
    this.factoryClass = factoryClass;
    this.creator = creator;
  }

  protected final F cast(AbstractLayoutFactory<F, L> factory) {
    return this.factoryClass.cast(factory);
  }

  public final L get() {
    L layout = creator.get();
    this.properties.forEach(p -> p.accept(layout));
    return layout;
  }

  final F set(Consumer<L> property) {
    properties.add(property);
    return cast(this);
  }
}
