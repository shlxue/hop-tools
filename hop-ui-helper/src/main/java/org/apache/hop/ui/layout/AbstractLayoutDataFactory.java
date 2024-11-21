package org.apache.hop.ui.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

abstract class AbstractLayoutDataFactory<F extends AbstractLayoutDataFactory<?, ?>, D> {

  private final Class<F> factoryClass;
  protected final D data;
  private final List<Consumer<D>> properties = new ArrayList<>();

  AbstractLayoutDataFactory(Class<F> factoryClass, D data) {
    this.factoryClass = factoryClass;
    this.data = data;
  }

  protected final F cast(AbstractLayoutDataFactory<F, D> factory) {
    return this.factoryClass.cast(factory);
  }

  public final D get() {
    this.properties.forEach(p -> p.accept(data));
    return data;
  }

  protected final F set(Consumer<D> property) {
    properties.add(property);
    return cast(this);
  }
}
