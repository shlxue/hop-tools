package org.apache.hop.ui.widgets;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import java.util.function.Consumer;
import java.util.function.Function;

public final class MixedVarPanelFactory<P extends Composite, C extends Control>
    extends AbstractCompositeFactory<MixedVarPanelFactory<P, C>, P> {
  private final Function<P, C> getter;
  private C cache;

  @SuppressWarnings("unchecked")
  private MixedVarPanelFactory(P panel, Function<P, C> getter) {
    super((Class) MixedVarPanelFactory.class, parent -> panel);
    this.getter = getter;
  }

  public static <P extends Composite, C extends Control> MixedVarPanelFactory<P, C> of(
      P panel, Function<P, C> getter) {
    return new MixedVarPanelFactory<>(panel, getter);
  }

  public C get() {
    if (cache == null) {
      synchronized (this) {
        cache = getter.apply(super.widgetCreator.create(null));
      }
    }
    return cache;
  }

  public MixedVarPanelFactory<P, C> tooltip(String tooltipText) {
    addProperty(c -> get().setToolTipText(tooltipText));
    return cast(this);
  }

  public MixedVarPanelFactory<P, C> message(String message) {
    addProperty(
        c -> {
          if (get() instanceof Text wText) {
            wText.setMessage(message);
          }
        });
    return cast(this);
  }

  public MixedVarPanelFactory<P, C> widget(Consumer<C> consumer) {
    consumer.accept(get());
    return this;
  }

  public MixedVarPanelFactory<P, C> onModify(Consumer<ModifyEvent> consumer) {
    addProperty(widget -> addModifyListener(get(), consumer));
    return this;
  }

  private void addModifyListener(C control, Consumer<ModifyEvent> consumer) {
    if (control instanceof Text wText) {
      wText.addModifyListener(consumer::accept);
    }
  }
}
