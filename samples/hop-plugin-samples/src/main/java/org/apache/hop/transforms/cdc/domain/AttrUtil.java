package org.apache.hop.transforms.cdc.domain;

import java.util.function.UnaryOperator;

final class AttrUtil {
  public static StringBuilder append(final StringBuilder builder, String fieldName, Object value) {
    return append(builder, fieldName, value, value != null);
  }

  public static StringBuilder append(
      final StringBuilder builder, String fieldName, Object value, boolean check) {
    UnaryOperator<StringBuilder> call = buf -> buf.append(value);
    UnaryOperator<StringBuilder> strCall = buf -> buf.append('"').append(value).append('"');
    return append(builder, fieldName, check, value instanceof String ? strCall : call);
  }

  public static StringBuilder append(
      final StringBuilder builder,
      String fieldName,
      boolean check,
      UnaryOperator<StringBuilder> call) {
    if (!check) {
      return builder;
    }
    if (builder.charAt(builder.length() - 1) != '{') {
      builder.append(",");
    }
    builder.append('"').append(fieldName).append("\":");
    return call.apply(builder);
  }

  private AttrUtil() {}
}
