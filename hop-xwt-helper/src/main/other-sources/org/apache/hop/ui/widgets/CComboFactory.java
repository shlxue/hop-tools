package org.apache.hop.ui.widgets;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;

import java.util.function.Consumer;

public final class CComboFactory extends AbstractCompositeFactory<CComboFactory, CCombo> {
  private CComboFactory(int style) {
    super(CComboFactory.class, parent -> new CCombo(parent, style));
  }

  public static CComboFactory newCombo(int style) {
    return new CComboFactory(style);
  }

  public CComboFactory listVisible(boolean visible) {
    return add(combo -> combo.setListVisible(visible));
  }

  public CComboFactory items(String... items) {
    return add(combo -> combo.setItems(items));
  }

  public CComboFactory text(String text) {
    return add(combo -> combo.setText(text));
  }

  public CComboFactory textLimit(int limit) {
    return add(combo -> combo.setTextLimit(limit));
  }

  public CComboFactory visibleItemCount(int count) {
    return add(combo -> combo.setVisibleItemCount(count));
  }

  public CComboFactory onModify(Consumer<ModifyEvent> consumer) {
    return add((combo -> combo.addModifyListener(consumer::accept)));
  }

  public CComboFactory onSelection(Consumer<SelectionEvent> consumer) {
    return add((combo -> combo.addSelectionListener(Adapter.widgetSelected(consumer))));
  }

  public CComboFactory onVerify(Consumer<VerifyEvent> consumer) {
    return add((combo -> combo.addVerifyListener(consumer::accept)));
  }

  private CComboFactory add(Property<CCombo> property) {
    addProperty(property);
    return this;
  }
}
