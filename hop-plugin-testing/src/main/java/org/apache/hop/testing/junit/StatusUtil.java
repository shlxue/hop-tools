package org.apache.hop.testing.junit;

import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.HopEnv.Type;
import org.apache.hop.testing.SpecMode;
import org.apache.hop.testing.SqlMode;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.StringUtils;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.isNull;

public final class StatusUtil {
  private StatusUtil() {}

  public static final String HOP_JUNIT_TYPE = "HOP_JUNIT_TYPE";
  public static final String HOP_JUNIT_UI = "HOP_JUNIT_UI";
  public static final String HOP_JUNIT_WITH_H2 = "HOP_JUNIT_WITH_H2";
  public static final String HOP_SQL_MODE = "HOP_JUNIT_SQL_MODE";

  public static final String HOP_UI_DELAY = "HOP_JUNIT_UI_DELAY";

  public static Type envType(HopEnv hopEnv) {
    return get(HOP_JUNIT_TYPE, Type::valueOf, isNull(hopEnv) ? null : hopEnv::type, Type.MOCK);
  }

  public static SpecMode uiSpec(HopEnv hopEnv) {
    return get(
        HOP_JUNIT_UI, SpecMode::valueOf, isNull(hopEnv) ? null : hopEnv::ui, SpecMode.STRICT);
  }

  public static boolean withH2(HopEnv hopEnv) {
    return get(
        HOP_JUNIT_WITH_H2, Boolean::parseBoolean, isNull(hopEnv) ? null : hopEnv::withH2, false);
  }

  public static SqlMode sqlMode(HopEnv hopEnv) {
    return get(HOP_SQL_MODE, SqlMode::valueOf, isNull(hopEnv) ? null : hopEnv::sqlMode, SqlMode.H2);
  }

  public static long delayTime(long timeMs) {
    return get(HOP_UI_DELAY, Long::parseLong, null, timeMs);
  }

  private static <T> T get(
      String key, Function<String, T> getter, Supplier<T> creator, T defaultValue) {
    String val = System.getProperty(key, System.getenv(key));
    if (StringUtils.isNotBlank(val)) {
      return getter.apply(val);
    }
    return creator != null ? creator.get() : defaultValue;
  }

  public static <K> Object remove(ExtensionContext context, K key) {
    return context.getStore(HopJunit.HOP_NS).remove(key);
  }

  public static <K, V> V remove(ExtensionContext context, K key, Class<V> type) {
    return context.getStore(HopJunit.HOP_NS).remove(key, type);
  }

  public static <K> void set(ExtensionContext context, K key, Object value) {
    context.getStore(HopJunit.HOP_NS).put(key, value);
  }

  public static <K, V> V get(
      ExtensionContext context, K key, Class<V> clazz, Function<K, V> creator) {
    return context.getStore(HopJunit.HOP_NS).getOrComputeIfAbsent(key, creator, clazz);
  }

  public static <K, V> V get(ExtensionContext context, K key, Class<V> clazz) {
    return context.getStore(HopJunit.HOP_NS).get(key, clazz);
  }
}
