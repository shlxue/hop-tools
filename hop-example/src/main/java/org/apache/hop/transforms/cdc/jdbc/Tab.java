package org.apache.hop.transforms.cdc.jdbc;

import org.apache.hop.core.util.StringUtil;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Tab<V extends Cloneable> implements Cloneable {
  private final Function<V, String> nameGetter;
  private final Collection<V> fields = new ArrayList<>();
  private final List<Index<V>> indexes = new ArrayList<>();
  private Type type;
  private String schema;
  private String name;
  private String primaryKeyFields;
  private String pkFieldNames;
  private int pkIndex = -1;
  private Index<V> primaryKey;
  private Index<V> pk;

  private Tab(Type type, String schema, String name, Function<V, String> nameGetter) {
    this.type = type;
    this.schema = schema;
    this.name = name;
    this.nameGetter = nameGetter;
  }

  public static <V extends Cloneable> Tab<V> build(String name, Function<V, String> nameGetter) {
    return build(name, name, nameGetter);
  }

  public static <V extends Cloneable> Tab<V> build(
      String schema, String name, Function<V, String> nameGetter) {
    return build(schema, name, Type.NONE, nameGetter);
  }

  public static <V extends Cloneable> Tab<V> build(
      String schema, String name, Type type, Function<V, String> nameGetter) {
    return new Tab<>(type, schema, name, nameGetter);
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFullName() {
    if (StringUtil.isEmpty(schema)) {
      return name;
    }
    return String.format("%s.%s", schema, name);
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Collection<V> getFields() {
    return fields;
  }

  public void setFields(Collection<V> fields) {
    this.fields.clear();
    this.fields.addAll(fields);
    setIndexes(Collections.emptyList());
  }

  public String getPrimaryKeyFields() {
    return primaryKeyFields;
  }

  public void setPrimaryKeyFields(String primaryKeyFields) {
    String fieldNames = safeFieldNames(primaryKeyFields);
    this.primaryKey =
        indexStream()
            .filter(index -> matchIndexFields(index, fieldNames))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException("Unknown primary key by fields " + primaryKeyFields));
    this.primaryKeyFields = primaryKeyFields;
  }

  public String getPkFieldNames() {
    return pkFieldNames;
  }

  public String formatSql(
      Collection<V> fields, UnaryOperator<String> fieldOperator, boolean expression) {
    String joiningStr = expression ? " AND " : ", ";
    return fields.stream()
        .map(nameGetter)
        .map(fieldOperator)
        .collect(Collectors.joining(joiningStr));
  }

  public void setPkFieldNames(String pkFields) {
    this.pk = null;
    this.pkIndex = -1;
    if (StringUtil.isEmpty(pkFields)) {
      this.pkFieldNames = null;
      return;
    }
    String separator = ",";
    pkFields = safeFieldNames(pkFields);
    SortedSet<String> set = new TreeSet<>(String::compareToIgnoreCase);
    set.addAll(Arrays.asList(pkFields.split(separator)));
    String fieldNames = String.join(separator, set);
    for (int i = 0; i < indexes.size(); i++) {
      Index<V> index = indexes.get(i);
      if (index.getFields().size() == set.size()) {
        set.clear();
        set.addAll(index.getFields().stream().map(nameGetter).collect(Collectors.toList()));
        if (fieldNames.equalsIgnoreCase(String.join(separator, set))) {
          pk = index.create();
          pkIndex = i;
          break;
        }
      }
    }
    if (pk == null) {
      set.clear();
      set.addAll(Arrays.asList(pkFields.split(separator)));
      List<V> list = new ArrayList<>(set.size());
      for (V v : fields) {
        if (set.contains(nameGetter.apply(v))) {
          list.add(v);
        }
      }
      if (list.size() == set.size()) {
        pk = Index.build(false, list, nameGetter).create();
      }
    }
    if (pk != null) {
      if (pkIndex != -1) {
        pk.setName(indexes.get(pkIndex).getName());
        pk.setUnique(indexes.get(pkIndex).isUnique());
      }
      this.pkFieldNames = pk.getFieldNames();
    }
  }

  public List<Index<V>> getIndexes() {
    return indexes;
  }

  public void setIndexes(Collection<Index<V>> indexes) {
    this.indexes.clear();
    this.indexes.addAll(indexes);
    this.primaryKeyFields = null;
    this.primaryKey = null;
    this.pk = null;
  }

  public Collection<V> getFields(String fieldNames) {
    String[] names = safeFieldNames(fieldNames).split(",");
    List<V> list = new ArrayList<>(names.length);
    for (String s : names) {
      for (V v : fields) {
        if (nameGetter.apply(v).equalsIgnoreCase(s)) {
          list.add(v);
        }
      }
    }
    return list;
  }

  public boolean addIndex(boolean unique, String fieldNames) {
    return indexes.add(Index.build(unique, getFields(safeFieldNames(fieldNames)), nameGetter));
  }

  public Collection<Index<V>> getUnique() {
    return indexStream().filter(Index::isUnique).collect(Collectors.toList());
  }

  public long getCardinality() {
    return getPrimaryKey()
        .map(Index::getCardinality)
        .orElseGet(() -> getUnique().stream().mapToLong(Index::getCardinality).max().orElse(-1));
  }

  public void setPrimaryKeyName(String primaryKeyName) {
    this.primaryKey =
        indexStream()
            .filter(vIndex -> matchPrimaryKeyName(vIndex, primaryKeyName))
            .findFirst()
            .orElse(null);
    if (primaryKey == null) {
      return;
    }
    String fieldNames = primaryKey.getFieldNames();
    if (!fieldNames.equals(primaryKeyFields)) {
      this.primaryKeyFields = fieldNames;
    }
  }

  public int getPkIndex() {
    return pkIndex;
  }

  public Optional<Index<V>> getPrimaryKey() {
    return Optional.ofNullable(primaryKey);
  }

  public Optional<Index<V>> getPK() {
    return Optional.ofNullable(pk);
  }

  public Optional<Index<V>> getPkInIndexes() {
    if (pkIndex != -1) {
      return Optional.of(indexes.get(pkIndex));
    }
    return Optional.empty();
  }

  public Collection<V> getPkFields() {
    Optional<Index<V>> index = getPK();
    if (index.isPresent()) {
      return index.get().getFields();
    }
    return Collections.emptyList();
  }

  public Collection<Index<V>> getUniqueIndexes() {
    return indexStream().filter(Index::isUnique).collect(Collectors.toList());
  }

  public Collection<V> getIndexFields() {
    return fields.stream().filter(this::inIndex).collect(Collectors.toList());
  }

  public void refresh(Tab<V> tab) {
    refresh(tab, false);
  }

  public void refresh(Tab<V> tab, boolean all) {
    if (all) {
      setSchema(tab.getSchema());
      setName(tab.getName());
    }
    setType(tab.getType());
    setFields(tab.getFields());
    setIndexes(tab.getIndexes());
    if (!StringUtil.isEmpty(tab.primaryKeyFields)) {
      setPrimaryKeyFields(tab.primaryKeyFields);
    }
    if (!StringUtil.isEmpty(tab.pkFieldNames)) {
      setPkFieldNames(tab.pkFieldNames);
    }
  }

  @Override
  public Tab<V> clone() {
    Tab<V> clone = build(schema, name, type, nameGetter);
    clone.setFields(new ArrayList<>(fields));
    clone.setIndexes(indexes.stream().map(Index::clone).collect(Collectors.toList()));
    String fieldNames = getPkFieldNames();
    if (!StringUtil.isEmpty(fieldNames)) {
      clone.setPkFieldNames(fieldNames);
    }
    fieldNames = getPrimaryKeyFields();
    if (!StringUtil.isEmpty(fieldNames)) {
      clone.setPrimaryKeyFields(fieldNames);
    }
    return clone;
  }

  @Override
  public int hashCode() {
    return Arrays.deepHashCode(getValues());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Tab) {
      return Arrays.deepEquals(getValues(), ((Tab<V>) obj).getValues());
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format("%s: %s", name, pkFieldNames);
  }

  private Object[] getValues() {
    return new Object[] {schema, name, fields.size(), pkFieldNames, primaryKeyFields};
  }

  private boolean inIndex(V field) {
    boolean inPK = false;
    if (pk != null) {
      inPK = pk.getFields().stream().anyMatch(v -> v.equals(field));
    }
    return inPK
        || indexStream()
            .anyMatch(index -> index.getFields().stream().anyMatch(v -> v.equals(field)));
  }

  private String safeFieldNames(String fieldNames) {
    return fieldNames.replace(" ", "");
  }

  private Stream<Index<V>> indexStream() {
    return indexes.stream();
  }

  private boolean matchIndexFields(Index<V> index, String indexFields) {
    return indexFields.equalsIgnoreCase(index.getFieldNames().replace(" ", ""));
  }

  private boolean matchPrimaryKeyName(Index<V> index, String indexName) {
    return index.getName().equals(indexName);
  }

  public enum Type {
    NONE(""),
    TABLE("TABLE"),
    VIEW("VIEW"),
    SYSTEM_TABLE("SYSTEM TABLE"),
    GLOBAL_TEMPORARY("GLOBAL TEMPORARY"),
    LOCAL_TEMPORARY("LOCAL_TEMPORARY"),
    ALIAS("ALIAS"),
    SYNONYM("SYNONYM");

    private final String value;

    Type(String value) {
      this.value = value;
    }

    public static Type valueOfValue(String typeValue) {
      for (Type type : values()) {
        if (type.value.equals(typeValue)) {
          return type;
        }
      }
      return NONE;
    }

    public String getValue() {
      return value;
    }
  }
}
