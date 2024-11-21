package org.apache.hop.maven.extensions;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

class Xpp3DomAdapter {
  private final Object value;
  private final Method getChildMethod;
  private final Method getValueMethod;
  private final Method setValueMethod;
  private final Method addChildMethod;

  static Xpp3DomAdapter of(Object value) throws ReflectiveOperationException {
    return new Xpp3DomAdapter(value);
  }

  static Xpp3DomAdapter of(Class<?> type, String key) throws ReflectiveOperationException {
    return new Xpp3DomAdapter(type.getConstructor(String.class).newInstance(key));
  }

  Xpp3DomAdapter(Object value) throws ReflectiveOperationException {
    this.value = value;
    Class<?> type = value.getClass();
    this.getChildMethod = type.getMethod("getChild", String.class);
    this.getValueMethod = type.getMethod("getValue");
    this.setValueMethod = type.getMethod("setValue", String.class);
    this.addChildMethod = type.getMethod("addChild", type);
  }

  Object get() {
    return value;
  }

  Object getChild(String name) {
    return call(() -> getChildMethod.invoke(value, name));
  }

  String getValue() {
    return call(() -> (String) getValueMethod.invoke(value));
  }

  void setValue(String newValue) {
    call(() -> setValueMethod.invoke(value, newValue));
  }

  void addChild(Object child) {
    call(() -> addChildMethod.invoke(value, child));
  }

  private <T> T call(Callable<T> getter) {
    try {
      return getter.call();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
