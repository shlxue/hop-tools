package org.apache.hop.transforms.cdc.domain;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class MixedKey implements Comparable<MixedKey> {
  static final Map<Class<?>, Method> getValuesMethodMap = new HashMap<>();

  private final Comparable<?>[] values;

  private MixedKey(Comparable<?>[] values) {
    this.values = values;
  }

  private MixedKey(Object[] values) {
    this.values = new Comparable[values.length];
    for (int i = 0; i < values.length; i++) {
      if (values[i] instanceof Comparable) {
        this.values[i] = (Comparable<?>) values[i];
      } else {
        throw new IllegalArgumentException("Invalid constructor param: " + values[i]);
      }
    }
  }

  public static MixedKey of(Comparable<?>[] values) {
    return new MixedKey(values);
  }

  public static MixedKey of(Object[] values) {
    return new MixedKey(values);
  }

  public Comparable[] getValues() {
    return values;
  }

  @Override
  @SuppressWarnings("unchecked")
  public int compareTo(MixedKey o) {
    int len = Math.min(values.length, o.values.length);
    for (int i = 0; i < len; i++) {
      int result = ((Comparable) values[i]).compareTo(o.values[i]);
      if (result != 0) {
        return result;
      }
    }
    return values.length > o.values.length ? 1 : -1;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (getClass() != o.getClass()) {
      if (o.getClass().isArray()) {
        return Arrays.equals(values, (Object[]) o);
      }
      Method method = getValuesMethodMap.get(o.getClass());
      try {
        if (method == null) {
          method = o.getClass().getMethod("getValues");
          getValuesMethodMap.put(o.getClass(), method);
        }
        Object[] val = (Object[]) method.invoke(o);
        return Arrays.equals(values, val);
      } catch (Exception ignore) {
        return false;
      }
    }
    MixedKey mixedKey = (MixedKey) o;
    return Arrays.equals(values, mixedKey.values);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    if (values != null) {
      for (Comparable<?> value : values) {
        if (value != null) {
          if (value instanceof String) {
            builder.append('"').append(value).append('"');
          } else {
            builder.append(value);
          }
        }
        builder.append(",");
      }
    }
    builder.deleteCharAt(builder.length() - 1);
    builder.append("]");
    return builder.toString();
  }
}
