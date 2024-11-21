package com.opennews.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 操作日志
 *
 * @param <K> 逻辑主键对应的数据类型(一个支持排序操作的数据类型, 默认所有)
 */
@Getter
@Setter
public final class OpLog<K> implements Comparable<OpLog<K>> {

  static Pattern UUID_PATTERN =
      Pattern.compile(
          String.format("^[%s]{8}-([%1$s]{4}-){3}[%1$s]{12}$", "0-9a-f"), Pattern.CASE_INSENSITIVE);

  /** long, BigDecimal, string, guid, array */
  private K rowKey;

  private Op op;
  private long opAt;

  public OpLog() {
    op = Op.NONE;
  }

  OpLog(Op op, K rowKey) {
    this.op = op;
    this.rowKey = rowKey;
  }

  public static <T extends Comparable<T>> OpLog<T> of(Op op, T rowKey) {
    return new OpLog<>(op, rowKey);
  }

  public static OpLog<Comparable<?>> build(Op op, Comparable<?> value) {
    return new OpLog<>(op, value);
  }

  public Comparable<?> getRowKey() {
    return (Comparable<?>) rowKey;
  }

  public boolean isMixedKey() {
    return rowKey instanceof MixedKey;
  }

  public Long getKey() {
    return (Long) rowKey;
  }

  @SuppressWarnings("unchecked")
  public void setKey(Long value) {
    rowKey = (K) value;
  }

  public String getStr() {
    return (String) rowKey;
  }

  @SuppressWarnings("unchecked")
  public void setStr(String value) {
    rowKey = (K) value;
  }

  public String getGuid() {
    return (String) rowKey;
  }

  @SuppressWarnings("unchecked")
  public void setGuid(String uuid) {
    rowKey = (K) uuid;
  }

  @SuppressWarnings("unchecked")
  public void setGuid(UUID uuid) {
    rowKey = (K) uuid;
  }

  public Comparable<?>[] getKeys() {
    if (rowKey instanceof MixedKey) {
      return ((MixedKey) rowKey).getValues();
    }
    return new Comparable[] {(Comparable<?>) rowKey};
  }

  @SuppressWarnings("unchecked")
  public void setKeys(Comparable<?>[] value) {
    rowKey = (K) MixedKey.of(value);
  }

  public Date getOpTime() {
    return new Date(opAt);
  }

  public void setOpTime(Date opTime) {
    opAt = opTime != null ? opTime.getTime() : 0;
  }

  @Override
  @SuppressWarnings("unchecked")
  public int compareTo(OpLog<K> o) {
    return ((Comparable<K>) rowKey).compareTo(o.rowKey);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(72);
    toJson(builder);
    return builder.toString();
  }

  public StringBuilder toJson(StringBuilder builder) {
    builder.append('{');
    AttrUtil.append(builder, "op", true, buf -> buf.append('"').append(op.getValue()).append('"'));
    AttrUtil.append(builder, "opAt", opAt > 0, buf -> buf.append(opAt));
    KeyType keyType = getType();
    switch (keyType) {
      case KEY:
      case KEYS:
        AttrUtil.append(builder, keyType.value, getRowKey());
        break;
      default:
        AttrUtil.append(
            builder, keyType.value, true, buf -> buf.append('"').append(rowKey).append('"'));
    }
    builder.append('}');
    return builder;
  }

  public KeyType getType() {
    if (rowKey instanceof Long || rowKey instanceof Number) {
      return KeyType.KEY;
    }
    if (isGuidKey()) {
      return KeyType.GUID;
    }
    if (rowKey instanceof MixedKey) {
      return KeyType.KEYS;
    }
    if (rowKey instanceof String) {
      return KeyType.STR;
    }
    return KeyType.RAW;
  }

  private boolean isGuidKey() {
    if (rowKey instanceof String) {
      String key = (String) rowKey;
      return key.length() == 36 && UUID_PATTERN.matcher(key).matches();
    }
    return rowKey instanceof UUID;
  }

  public enum KeyType {
    RAW("raw"),
    KEY("key"),
    GUID("guid"),
    STR("str"),
    KEYS("keys");

    private final String value;

    KeyType(String value) {
      this.value = value;
    }
  }
}
