package com.opennews.domain;

import java.util.function.Supplier;

public interface Headers {

  void addHeader(String key, Object value);

  Object removeHeader(String key);

  default <T> T getHeader(String key, Class<T> type) {
    return getHeader(key, type, null);
  }

  <T> T getHeader(String key, Class<T> type, Supplier<T> defaultValue);
}
