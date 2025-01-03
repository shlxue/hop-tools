package org.apache.hop.transforms.cdc.domain;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class RefKey {
  private final Comparable<?> raw;
  private final Comparable<?> key;
  private final Comparable<?>[] mKeys;
  private final Result[] results;
  private int counter;
  private Result cache;

  public RefKey(Comparable<?> raw, Comparable<?> key, Comparable<?>[] mKeys) {
    this.raw = raw;
    this.key = key;
    this.mKeys = mKeys;
    this.results = new Result[mKeys.length];
    this.counter = mKeys.length;
  }

  @SuppressWarnings("java:S3740")
  public Comparable getRaw() {
    return raw;
  }

  public Comparable getKey() {
    return key;
  }

  @SuppressWarnings("java:S3740")
  public Comparable[] getMKeys() {
    return mKeys;
  }

  public boolean isSuccess() {
    return Objects.requireNonNull(cache).success;
  }

  public String getSource() {
    return cache.source;
  }

  public long getTimestamp() {
    return cache.timestamp;
  }

  public String getCode() {
    return cache.code;
  }

  public String getMessage() {
    return cache.message;
  }

  public boolean isMarked() {
    return counter == 0;
  }

  public boolean allowMark() {
    return counter > 0;
  }

  public boolean hasMarked() {
    return counter < mKeys.length;
  }

  public boolean markForce() {
    if (hasMarked()) {
      for (int i = 0; i < results.length; i++) {
        if (results[i] == null) {
          mark(mKeys[i], false, null, -1, "NDF-01", "");
        }
      }
      return true;
    }
    return false;
  }

  public boolean mark(
      Comparable<?> mKey,
      boolean success,
      String source,
      long timestamp,
      String code,
      String message) {
    if (counter > 0) {
      for (int i = 0; i < mKeys.length; i++) {
        if (mKeys[i].equals(mKey)) {
          if (results[i] == null) {
            results[i] = new Result(success, source, code, message, timestamp);
            counter--;
            if (counter == 0) {
              this.cache = buildResult();
            }
            return true;
          } else {
            results[i].times++;
            break;
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RefKey refKey = (RefKey) o;
    return Objects.equals(raw, refKey.raw);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(raw);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(64).append(raw);
    if (mKeys.length == 1) {
      builder.append(": ").append(mKeys[0]);
    } else {
      builder.append(" -> ").append(mKeys.length).append(" ref-keys");
    }
    if (cache != null) {
      builder
          .append(", {success=")
          .append(cache.success)
          .append(", code=")
          .append(getCode())
          .append("}");
    }
    return builder.toString();
  }

  private Result buildResult() {
    StringBuilder builder = null;
    Set<String> codes = new LinkedHashSet<>();
    Set<String> messages = new LinkedHashSet<>();
    long timestamp = 0;
    String source = "";
    int failed = 0;
    for (int i = 0; i < results.length; i++) {
      Result rs = results[i];
      if (rs.timestamp > timestamp) {
        timestamp = rs.timestamp;
        source = rs.source;
      }
      rs.code().ifPresent(codes::add);
      if (rs.success) {
        rs.message().ifPresent(messages::add);
      } else {
        failed++;
        if (builder == null) {
          builder = new StringBuilder(256).append(mKeys.length).append(" ref-keys=");
        } else {
          builder.append(";");
        }
        builder.append(mKeys[i]).append(" > ");
        rs.message().ifPresent(builder::append);
      }
    }
    if (results.length > 1) {
      source += String.format("(%d steps)", results.length);
    }
    if (codes.isEmpty() && failed == 0) {
      return Result.build(source, timestamp);
    }
    String code = String.join(",", codes);
    String message =
        failed > 0
            ? String.format("Failed %d keys, ", failed) + Objects.requireNonNull(builder)
            : String.join(", ", messages);
    return new Result(failed == 0, source, code, message, timestamp);
  }

  private static class Result {
    private final boolean success;
    private final String code;
    private final String message;
    private final String source;
    private final long timestamp;
    private int times = 1;

    static Result build(String source, long timestamp) {
      return new Result(true, source, null, null, timestamp);
    }

    public Result(boolean success, String source, String code, String message, long timestamp) {
      this.success = success;
      this.source = source;
      this.code = safeValue(code);
      this.message = safeValue(message);
      this.timestamp = timestamp;
    }

    Optional<String> code() {
      return Optional.ofNullable(code);
    }

    Optional<String> message() {
      return Optional.ofNullable(message);
    }

    private static String safeValue(String value) {
      if (value != null) {
        value = value.trim();
        if (value.isEmpty()) {
          value = null;
        }
      }
      return value;
    }
  }
}
