package org.apache.hop.ui.util;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class Data {
  static final String UI_LABEL = "swt.ui.label";
  static final String UI_EDITOR = "swt.ui.editor";
  static final String UI_ACTION = "swt.ui.action";
  static final String UI_EDITOR_GETTER = "swt.ui.editor.getter";
  static final String UI_EDITOR_SETTER = "swt.ui.editor.setter";

  static final String UI_JDBC_CONNECTION = "swt.jdbc.connection";
  static final String UI_JDBC_SCHEMA = "swt.jdbc.schema";
  static final String UI_JDBC_TABLE = "swt.jdbc.table";

  static final String UI_HOP_VARIABLES = "swt.hop.variables";
  static final String UI_HOP_DATABASE_META = "swt.hop.databaseMeta";
  static final String UI_HOP_PIPELINE_META = "swt.hop.pipelineMeta";
  static final String UI_HOP_TRANSFORM_META = "swt.hop.transformMeta";
  static final String UI_HOP_LOG = "swt.hop.logObject";

  private Data() {}

  public static Optional<Label> label(Widget widget) {
    return tryGet(widget, Label.class, UI_LABEL);
  }

  public static <T extends Control> T editor(Widget widget, Class<T> type) {
    return tryGet(widget, type, UI_EDITOR)
        .orElseThrow(
            () -> new IllegalArgumentException("No editor found on control " + widget.getClass()));
  }

  public static <T> T getValue(Widget widget, Class<T> type) {
    Supplier<?> getter = tryGet(widget, Supplier.class, UI_EDITOR_GETTER).orElse(null);
    if (getter != null) {
      return type.cast(getter.get());
    }
    return null;
  }

  //  @SuppressWarnings("unchecked")
  public static <T> void setValue(Widget widget, T value) {
    Consumer<T> setter = getSetter(widget, UI_EDITOR_SETTER);
    // tryGet(widget, Consumer.class, UI_EDITOR_SETTER).orElse(null);
    if (setter != null) {
      setter.accept(value);
    }
  }

  static <T> T get(Widget widget, Class<T> clazz, String key) {
    return tryGet(widget, clazz, key)
        .orElseThrow(() -> new IllegalArgumentException("No data found for key " + key));
  }

  static <T> Optional<T> tryGet(Widget widget, Class<T> clazz, String key) {
    Object value = widget.getData(key);
    if (value != null) {
      isAssignable(clazz, value.getClass(), () -> "Wrong data type for key " + key);
      return Optional.of(clazz.cast(value));
    }
    return Optional.empty();
  }

  static <T> T getGetter(Widget widget, Class<T> clazz, String key) {
    Object value = widget.getData(key);
    if (value instanceof Supplier<?> supplier) {
      return clazz.cast(supplier.get());
    }
    return null;
  }

  @SuppressWarnings({"unchecked"})
  static <T> Consumer<T> getSetter(Widget widget, String key) {
    return get(widget, Consumer.class, key);
  }

  static void remove(Widget widget, String key) {
    widget.setData(key, null);
  }

  static void add(Widget widget, String key, Object value) {
    isNull(widget.getData(key), () -> "Already exist data for key " + key);
    widget.setData(key, value);
  }

  static void isAssignable(Class<?> superType, Class<?> subType, Supplier<String> messageSupplier) {
    notNull(superType, "Supertype to check against must not be null");
    if (subType == null || !superType.isAssignableFrom(subType)) {
      assignableCheckFailed(superType, subType, nullSafeGet(messageSupplier));
    }
  }

  static void notNull(Object object, String message) {
    if (object == null) {
      throw new IllegalArgumentException(message);
    }
  }

  static void isNull(Object object, Supplier<String> messageSupplier) {
    if (object != null) {
      throw new IllegalArgumentException(nullSafeGet(messageSupplier));
    }
  }

  static boolean hasLength(String str) {
    return str != null && !str.isEmpty();
  }

  private static boolean endsWithSeparator(String msg) {
    return (msg.endsWith(":") || msg.endsWith(";") || msg.endsWith(",") || msg.endsWith("."));
  }

  private static void assignableCheckFailed(Class<?> superType, Class<?> subType, String msg) {
    String result = "";
    boolean defaultMessage = true;
    if (hasLength(msg)) {
      if (endsWithSeparator(msg)) {
        result = msg + " ";
      } else {
        result = messageWithTypeName(msg, subType);
        defaultMessage = false;
      }
    }
    if (defaultMessage) {
      result = result + (subType + " is not assignable to " + superType);
    }
    throw new IllegalArgumentException(result);
  }

  private static String messageWithTypeName(String msg, Object typeName) {
    return msg + (msg.endsWith(" ") ? "" : ": ") + typeName;
  }

  private static String nullSafeGet(Supplier<String> messageSupplier) {
    return (messageSupplier != null ? messageSupplier.get() : null);
  }
}
