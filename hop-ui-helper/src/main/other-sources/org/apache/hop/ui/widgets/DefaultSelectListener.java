package org.apache.hop.ui.widgets;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import java.util.function.Consumer;

class DefaultSelectListener implements SelectionListener {
  private final Consumer<SelectionEvent> consumer;

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
