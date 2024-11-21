package org.apache.hop.ui.layout;

import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Control;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FormDataFactory extends AbstractLayoutDataFactory<FormDataFactory, FormData> {
  FormDataFactory(Supplier<FormData> creator) {
    super(FormDataFactory.class, creator.get());
  }

  public FormDataFactory left(int numerator) {
    return init(new FormAttachment(numerator), v -> data.left = v);
  }

  public FormDataFactory left(int numerator, int offset) {
    return init(new FormAttachment(numerator, offset), v -> data.left = v);
  }

  public FormDataFactory left(int numerator, int denominator, int offset) {
    return init(new FormAttachment(numerator, denominator, offset), v -> data.left = v);
  }

  public FormDataFactory left(Control control) {
    return init(new FormAttachment(control), v -> data.left = v);
  }

  public FormDataFactory left(Control control, int offset) {
    return init(new FormAttachment(control, offset), v -> data.left = v);
  }

  public FormDataFactory left(Control control, int offset, int alignment) {
    return init(new FormAttachment(control, offset, alignment), v -> data.left = v);
  }

  public FormDataFactory top(int numerator) {
    return init(new FormAttachment(numerator), v -> data.top = v);
  }

  public FormDataFactory top(int numerator, int offset) {
    return init(new FormAttachment(numerator, offset), v -> data.top = v);
  }

  public FormDataFactory top(int numerator, int denominator, int offset) {
    return init(new FormAttachment(numerator, denominator, offset), v -> data.top = v);
  }

  public FormDataFactory top(Control control) {
    return init(new FormAttachment(control), v -> data.top = v);
  }

  public FormDataFactory top(Control control, int offset) {
    return init(new FormAttachment(control, offset), v -> data.top = v);
  }

  public FormDataFactory top(Control control, int offset, int alignment) {
    return init(new FormAttachment(control, offset, alignment), v -> data.top = v);
  }

  public FormDataFactory right(int numerator) {
    return init(new FormAttachment(numerator), v -> data.right = v);
  }

  public FormDataFactory right(int numerator, int offset) {
    return init(new FormAttachment(numerator, offset), v -> data.right = v);
  }

  public FormDataFactory right(int numerator, int denominator, int offset) {
    return init(new FormAttachment(numerator, denominator, offset), v -> data.right = v);
  }

  public FormDataFactory right(Control control) {
    return init(new FormAttachment(control), v -> data.right = v);
  }

  public FormDataFactory right(Control control, int offset) {
    return init(new FormAttachment(control, offset), v -> data.right = v);
  }

  public FormDataFactory right(Control control, int offset, int alignment) {
    return init(new FormAttachment(control, offset, alignment), v -> data.right = v);
  }

  public FormDataFactory bottom(int numerator) {
    return init(new FormAttachment(numerator), v -> data.bottom = v);
  }

  public FormDataFactory bottom(int numerator, int offset) {
    return init(new FormAttachment(numerator, offset), v -> data.bottom = v);
  }

  public FormDataFactory bottom(int numerator, int denominator, int offset) {
    return init(new FormAttachment(numerator, denominator, offset), v -> data.bottom = v);
  }

  public FormDataFactory bottom(Control control) {
    return init(new FormAttachment(control), v -> data.bottom = v);
  }

  public FormDataFactory bottom(Control control, int offset) {
    return init(new FormAttachment(control, offset), v -> data.bottom = v);
  }

  public FormDataFactory bottom(Control control, int offset, int alignment) {
    return init(new FormAttachment(control, offset, alignment), v -> data.bottom = v);
  }

  private FormDataFactory init(FormAttachment attachment, Consumer<FormAttachment> setter) {
    setter.accept(attachment);
    return this;
  }
}
