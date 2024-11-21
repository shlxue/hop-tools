package com.opennews.domain;

import java.util.UUID;

final class KeyUtil {
  public static final Long LONG_ZERO = 0L;
  public static final Long LONG_ONE = 1L;
  public static final Long LONG_MINUS_ONE = -1L;

  public static final UUID UUID_ZERO = new UUID(0, 0);

  public static int compare(Number x, Number y) {
    return 0;
  }

  public static int compare(Comparable<?> x, Comparable<?> y) {
    return 0;
  }

  public static boolean isNumber(Comparable<?> k) {
    return k instanceof Number;
  }

  public static boolean isUUID(Comparable<?> k) {
    //        return UUID.fromString(k.toString());
    return false;
  }

  public static <K extends Comparable<K>> K increment(K k) {
    return k;
  }

  public static <K extends Comparable<K>> K decrement(K k) {
    return k;
  }
}
