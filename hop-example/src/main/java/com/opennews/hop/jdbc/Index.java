package com.opennews.hop.jdbc;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Index<V extends Cloneable> implements Cloneable {
  private final Function<V, String> nameGetter;
  private final Collection<V> fields;
  private String name;
  private Type type = Type.NONE;
  private boolean unique;
  @Getter @Setter private boolean primaryKey;
  private long cardinality;

  private Index(Index<V> index) {
    this(index.name, index.unique, new ArrayList<>(index.fields), index.nameGetter);
    this.type = index.type;
    this.primaryKey = index.primaryKey;
    this.cardinality = index.cardinality;
  }

  private Index(String name, boolean unique, Collection<V> fields, Function<V, String> nameGetter) {
    this.nameGetter = nameGetter;
    this.name = name;
    this.unique = unique;
    this.fields = fields;
  }

  public static <T extends Cloneable> Index<T> build(
      boolean unique, String name, Function<T, String> nameGetter) {
    return new Index<>(name, unique, new ArrayList<>(), nameGetter);
  }

  public static <T extends Cloneable> Index<T> build(
      boolean unique, Collection<T> field, Function<T, String> nameGetter) {
    return new Index<>(null, unique, field, nameGetter);
  }

  public Index<V> create() {
    Index<V> vIndex = new Index<>(name, unique, new TreeSet<>(this::compareFieldName), nameGetter);
    vIndex.type = type;
    vIndex.setFields(fields);
    vIndex.cardinality = cardinality;
    return vIndex;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public boolean isUnique() {
    return unique;
  }

  public int getCoverage(long total) {
    if (total <= 0) {
      return -1;
    }
    return (int) (cardinality * 100 / total);
  }

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  public long getCardinality() {
    return cardinality;
  }

  public void setCardinality(long cardinality) {
    this.cardinality = cardinality;
  }

  public String getFieldNames() {
    return nameStream().collect(Collectors.joining(", "));
  }

  public Collection<V> getFields() {
    return fields;
  }

  public void setFields(Collection<V> fields) {
    this.fields.clear();
    this.fields.addAll(fields);
  }

  public String format(String prefix, String suffix, String separator) {
    return nameStream().map(s -> prefix + s + suffix).collect(Collectors.joining(separator));
  }

  @Override
  public Index<V> clone() {
    return new Index<>(this);
  }

  int compareFieldName(V v1, V v2) {
    return nameGetter.apply(v1).compareToIgnoreCase(nameGetter.apply(v2));
  }

  Stream<String> nameStream() {
    return fields.stream().map(nameGetter);
  }

  public enum Type {
    NONE(-1),
    STATISTIC(0),
    CLUSTERED(1),
    HASH(2),
    OTHER(3);
    private final short value;

    Type(int value) {
      this.value = (short) value;
    }

    public static Type valueOf(int value) {
      for (Type type : values()) {
        if (type.getValue() == value) {
          return type;
        }
      }
      return Type.NONE;
    }

    public short getValue() {
      return value;
    }
  }
}
