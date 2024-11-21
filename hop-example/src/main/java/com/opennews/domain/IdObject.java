package com.opennews.domain;

import java.util.Objects;
import java.util.function.UnaryOperator;

public class IdObject<K extends Comparable<K>> implements Comparable<IdObject<K>> {

  private K id;

  public IdObject() {}

  protected IdObject(K id) {
    this.id = id;
  }

  public static void append(
      final StringBuilder builder,
      String fieldName,
      boolean check,
      UnaryOperator<StringBuilder> call) {
    AttrUtil.append(builder, fieldName, check, call);
  }

  public K getId() {
    return id;
  }

  public void setId(K id) {
    this.id = id;
  }

  @Override
  public int compareTo(IdObject<K> o) {
    return id.compareTo(o.id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !getClass().equals(o.getClass())) return false;
    IdObject ref = (IdObject) o;
    return Objects.equals(id, ref.getId()) || KeyUtil.compare(id, ref.getId()) == 0;
  }

  @Override
  public int hashCode() {
    return 59 + (id == null ? 43 : id.hashCode());
  }

  @Override
  public String toString() {
    final StringBuilder builder = initBuffer();
    builder.append('{');
    writeBefore(builder);
    write(builder);
    writeAfter(builder);
    builder.append('}');
    return builder.toString();
  }

  protected StringBuilder initBuffer() {
    return new StringBuilder();
  }

  protected void writeBefore(final StringBuilder builder) {
    append(builder, "id", id);
  }

  protected void write(final StringBuilder builder) {}

  protected void writeAfter(final StringBuilder builder) {}

  protected final void append(final StringBuilder builder, String fieldName, Object value) {
    append(builder, fieldName, value, value != null);
  }

  protected final void append(
      final StringBuilder builder, String fieldName, Object value, boolean check) {
    AttrUtil.append(builder, fieldName, value, check);
  }

  protected final void append(final StringBuilder builder, String fieldName, String value) {
    if (value != null) {
      AttrUtil.append(
          builder, fieldName, value.length() > 16 ? value.substring(0, 13) + "..." : value, true);
    }
  }
}
