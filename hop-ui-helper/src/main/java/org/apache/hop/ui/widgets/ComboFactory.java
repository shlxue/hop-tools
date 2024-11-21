package org.apache.hop.ui.widgets;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Combo;

import java.util.function.Consumer;

public final class ComboFactory extends AbstractCompositeFactory<ComboFactory, Combo> {
  private ComboFactory(int style) {
    super(ComboFactory.class, parent -> new Combo(parent, style));
  }

  private ComboFactory(Combo control) {
    super(ComboFactory.class, parent -> control);
  }

  public static ComboFactory of(Combo control) {
    return new ComboFactory(control);
  }

  public static ComboFactory newCombo(int style) {
    return new ComboFactory(style);
  }

  public ComboFactory listVisible(boolean visible) {
    return add(combo -> combo.setListVisible(visible));
  }

  public ComboFactory items(String... items) {
    return add(combo -> combo.setItems(items));
  }

  public ComboFactory text(String text) {
    return add(combo -> combo.setText(text));
  }

  public ComboFactory textLimit(int limit) {
    return add(combo -> combo.setTextLimit(limit));
  }

  public ComboFactory visibleItemCount(int count) {
    return add(combo -> combo.setVisibleItemCount(count));
  }

  public ComboFactory onModify(Consumer<ModifyEvent> consumer) {
    return add((combo -> combo.addModifyListener(consumer::accept)));
  }

  public ComboFactory onSelection(Consumer<SelectionEvent> consumer) {
    return add((combo -> combo.addSelectionListener(Adapter.widgetSelected(consumer))));
  }

  public ComboFactory onVerify(Consumer<VerifyEvent> consumer) {
    return add((combo -> combo.addVerifyListener(consumer::accept)));
  }

  private ComboFactory add(Property<Combo> property) {
    addProperty(property);
    return this;
  }
}
