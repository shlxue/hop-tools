package org.apache.hop.transforms.cdc;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.transforms.cdc.domain.*;
import org.apache.hop.transforms.cdc.jdbc.Index;
import org.apache.hop.transforms.cdc.jdbc.MetadataUtil;
import org.apache.hop.transforms.cdc.jdbc.Tab;

import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

final class Util {
  private static final boolean STRICT_KEY_VAL = Boolean.getBoolean("STRICT_KEY_VAL");
  private static final IValueMeta MIXED_VALUE_META = new ValueMetaString();
  private static Method getRowKeyMethod;
  private static Method getOpMethod;
  static IValueMeta KEY_VAL = new ValueMetaString("_key_val");

  static final String TAG_CONNECTION = "connection";
  static final String TAG_SCHEMA = "schema";
  static final String TAG_TABLE = "table";
  static final String TAG_FIELD = "field";
  static final String TAG_INDEX = "index";
  static final String TAG_KEY_FIELDS = "keyFields";
  static final String TAG_PRIMARY_KEY = "primaryKey";
  static final String TAG_TABLES = "tables";
  static final String TAG_RELATIONS = "relations";
  static final String TAG_DEL_FLAG_FIELD = "delFlagField";
  static final String TAG_REF_TABLE_FIELD = "opTableField";

  static {
    ClassLoader cl = RowEventConvertDelegate.class.getClassLoader();
    String className = "c2m.carbon.domain.OpLog";
  }

  private Util() {}

  static String i18n(Class<?> pkg, String key, Object... params) {
    return BaseMessages.getString(pkg, key, params);
  }

  static IValueMeta newField(String name, int type) {
    return newField(name, type, -1, -1);
  }

  static IValueMeta newField(String name, int type, int len, int precision) {
    try {
      return ValueMetaFactory.createValueMeta(name, type, len, precision);
    } catch (HopException e) {
      throw new IllegalStateException(e);
    }
  }

  static Map<String, String> tableAttrs(Tab<?> tab) {
    Map<String, String> map = new LinkedHashMap<>(Map.of("name", tab.getName()));
    if (!StringUtil.isEmpty(tab.getSchema())) {
      map.put(TAG_SCHEMA, tab.getSchema());
    }
    return map;
  }

  static void fixKeyFormat(IValueMeta valueMeta) {
    switch (valueMeta.getType()) {
      case IValueMeta.TYPE_INTEGER:
      case IValueMeta.TYPE_BIGNUMBER:
        valueMeta.setConversionMask("#");
        break;
      case IValueMeta.TYPE_DATE:
      case IValueMeta.TYPE_TIMESTAMP:
        valueMeta.setConversionMask("yyyyMMddHHmmss");
        break;
      case IValueMeta.TYPE_NUMBER:
        valueMeta.setConversionMask("#.#");
        break;
    }
  }

  static Map<String, String> fieldAttrs(IValueMeta vm) {
    Map<String, String> attrs =
        new HashMap<>(Map.of("type", Integer.toString(vm.getType()), "name", vm.getName()));
    if (vm.isOriginalNullable() == DatabaseMetaData.columnNoNulls) {
      attrs.put("non-null", "Y");
    }
    if (vm.getLength() > 0) {
      attrs.put("length", Integer.toString(vm.getLength()));
      if (vm.getPrecision() > 0) {
        attrs.put("precision", Integer.toString(vm.getPrecision()));
      }
    }
    return attrs;
  }

  public static <T> void forEach(StringBuilder sb, Collection<T> list, Consumer<T> runnable) {
    forEach(sb, list, null, false, runnable);
  }

  public static <T> void forEach(
      StringBuilder sb, Collection<T> list, String tag, boolean open, Consumer<T> consumer) {
    for (T v : list) {
      if (open) {
        XmlHandler.openTag(sb, tag);
      }
      consumer.accept(v);
      if (open) {
        XmlHandler.closeTag(sb, tag);
      }
    }
  }

  static String[] getFieldAttrs(IValueMeta vm) {
    List<String> attrs = new ArrayList<>();
    new ArrayList<>();
    attrs.add("type");
    attrs.add(Integer.toString(vm.getType()));
    attrs.add("name");
    attrs.add(vm.getName());
    if (vm.isOriginalNullable() == DatabaseMetaData.columnNoNulls) {
      attrs.add("non-null");
      attrs.add("Y");
    }
    if (vm.getLength() > 0) {
      attrs.add("length");
      attrs.add(Integer.toString(vm.getLength()));
      if (vm.getPrecision() > 0) {
        attrs.add("precision");
        attrs.add(Integer.toString(vm.getPrecision()));
      }
    }
    return attrs.toArray(new String[0]);
  }

  static <T> void forEachWithAttr(
      StringBuilder builder, Collection<T> list, String tag, Function<T, String[]> getter) {
    for (T v : list) {
      builder.append(XmlHandler.addTagValue(tag, null, true, getter.apply(v)));
    }
  }

  static Map<String, String> indexAttrs(Index<IValueMeta> index) {
    Map<String, String> attrs = new LinkedHashMap<>();
    if (index.isUnique()) {
      attrs.put("unique", "Y");
    }
    String fieldNames = index.getFieldNames();
    if (!StringUtil.isEmpty(fieldNames)) {
      attrs.put("fields", fieldNames);
    }
    if (index.isPrimaryKey()) {
      attrs.put(TAG_PRIMARY_KEY, "Y");
    }
    return attrs;
  }

  static String[] getIndexAttrs(Index<IValueMeta> index) {
    List<String> attrs = new ArrayList<>(6);
    if (index.isUnique()) {
      attrs.add("unique");
      attrs.add("Y");
    }
    String fieldNames = index.getFieldNames();
    if (!StringUtil.isEmpty(fieldNames)) {
      attrs.add("fields");
      attrs.add(fieldNames);
    }
    if (index.isPrimaryKey()) {
      attrs.add(TAG_PRIMARY_KEY);
      attrs.add("Y");
    }
    return attrs.toArray(new String[0]);
  }

  static void refreshSchema(Tab<IValueMeta> table, MetadataUtil util) throws SQLException {
    String schema = table.getSchema();
    String tableName = table.getName();
    Tab<IValueMeta> tab = Tab.build(schema, tableName, IValueMeta::getName);
    tab.setType(Tab.Type.valueOfValue(util.getTableType(schema, tableName)));
    tab.setFields(util.getRowMeta(schema, tableName).getValueMetaList());
    tab.setIndexes(
        util.getIndexes(schema, tableName, (rowMeta, row) -> SwtUtil.toIndex(tab, rowMeta, row)));
    String[] pkNames = util.getPrimaryKeys(schema, tableName);
    if (pkNames.length > 0) {
      tab.setPrimaryKeyName(pkNames[0]);
      if (!tab.getPrimaryKey().isPresent()) {
        tab.setPrimaryKeyFields(pkNames[1]);
      }
    }
    tab.setPkFieldNames(table.getPkFieldNames());
    table.refresh(tab, true);
  }

  static String getMasterSql(Tab<IValueMeta> tab, String condition, boolean withOrderBy) {
    StringBuilder sb = new StringBuilder(512);
    String fullName = tab.getName();
    if (!StringUtil.isEmpty(tab.getSchema())) {
      fullName = String.format("%s.%s", tab.getSchema(), tab.getName());
    }
    sb.append("SELECT ").append(tab.getPkFieldNames()).append("\nFROM ").append(fullName);
    if (!StringUtil.isEmpty(condition)) {
      sb.append("\nWHERE ").append(condition.trim());
    }
    if (withOrderBy) {
      sb.append("\nORDER BY ").append(tab.getPkFieldNames());
    }
    return sb.toString();
  }

  static String getPreviewSql(String sql, String condition, String orderFields) {
    StringBuilder builder = new StringBuilder(512);
    if (StringUtil.isEmpty(condition)) {
      builder.append(sql);
    } else {
      builder.append(sql, 0, sql.indexOf("-- WHERE")).append("WHERE ").append(condition);
    }
    builder.append("\n");
    if (!StringUtil.isEmpty(orderFields)) {
      builder.append("ORDER BY ").append(orderFields);
    }
    return builder.toString();
  }

  static OpLog<?> wrapEventRow(Object[] row, RowMeta keyMeta, OpLog<?> rawEvent) {
    //    OpLog<?> rawEvent = toOpLog(row[0]);
    OpLog<?> event = toOpLog(rawEvent, keyMeta);
    Comparable<?>[] keys = event.getKeys();
    int index = 0;
    row[index++] = rawEvent.getRowKey();
    for (Comparable<?> value : keys) {
      row[index++] = value;
    }
    row[index++] = rawEvent.getOp();
    row[index] = keys;
    return event;
  }

  static OpLog<?> toOpLog(Object value) {
    if (value instanceof OpLog) {
      return (OpLog<?>) value;
    }
    try {
      if (getRowKeyMethod == null) {
        try {
          Class<?> oldOpLogType = value.getClass();
          getOpMethod = oldOpLogType.getDeclaredMethod("getOp");
          getRowKeyMethod = oldOpLogType.getDeclaredMethod("getRowKey");
        } catch (ReflectiveOperationException e) {
          throw new IllegalStateException("Event: " + value, e);
        }
      }
      Comparable val = (Comparable<?>) getRowKeyMethod.invoke(value);
      Op op = Op.valueOf(getOpMethod.invoke(value).toString());
      return OpLog.of(op, val);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Compatible OpLog", e);
    }
  }

  static String joinOpLogKey(IRowMeta rowMeta, Object[] keys) {
    return joinOpLogKey(rowMeta, keys, false);
  }

  static String joinOpLogKey(IRowMeta rowMeta, Object[] keys, boolean legacy) {
    try {
      if (rowMeta.size() == 1) {
        return rowMeta.getValueMeta(0).getString(keys[0]);
      }
      StringBuilder buf = new StringBuilder(64 * rowMeta.size());
      // TODO remove legacy join type
      if (legacy) {
        buf.append(rowMeta.getValueMeta(0).getString(keys[0]));
        for (int i = 1; i < rowMeta.size(); i++) {
          buf.append(',').append(rowMeta.getValueMeta(i).getString(keys[i]));
        }
        return buf.toString();
      }
      char close = '>';
      buf.append('<').append(rowMeta.getValueMeta(0).getString(keys[0])).append(close);
      for (int i = 1; i < rowMeta.size(); i++) {
        buf.append("::<").append(rowMeta.getValueMeta(i).getString(keys[i])).append(close);
      }
      return buf.toString();
    } catch (HopValueException ex) {
      throw new IllegalStateException(ex);
    }
  }

  static OpLog<?> toOpLog(OpLog<?> opLog, IRowMeta rowMeta) {
    Object[] keys = new Comparable[rowMeta.size()];
    Object[] values;
    boolean mixed = rowMeta.size() > 1;
    if (mixed) {
      String value = opLog.getStr();
      if (value.charAt(0) == '<' && value.charAt(value.length() - 1) == '>') {
        values = value.substring(1, value.length() - 1).split(">::<");
      } else {
        values = opLog.getStr().split(",");
      }
    } else {
      // TODO deprecated field wrap on single field key: <field>
      Object value = opLog.getRowKey();
      if (value instanceof String) {
        String str = (String) value;
        if (str.charAt(0) == '<' && str.charAt(str.length() - 1) == '>') {
          value = str.substring(1, str.length() - 1);
        }
      }
      values = new Object[] {value};
    }
    assert values.length > 0 && keys.length == values.length;
    if (rowMeta.getValueMeta(0).getConversionMetadata() == null) {
      for (int i = 0; i < rowMeta.size(); i++) {
        // cache origin value meta to conversionMetadata
        IValueMeta conversionMetadata = ValueMetaFactory.guessValueMetaInterface(values[i]);
        if (conversionMetadata == null) {
          //          conversionMetadata.setName(rowMeta.getValueMeta(i).getName());
          //          if (values[i] instanceof Number) {
          //            if (values[i] instanceof Integer) {
          //              conversionMetadata = new ValueMetaInteger();
          //            }
          //          }
          throw new IllegalArgumentException("Unknown type: " + values[i]);
        }
        rowMeta.getValueMeta(i).setConversionMetadata(conversionMetadata);
        if (conversionMetadata.getType() != rowMeta.getValueMeta(i).getType()) {
          conversionMetadata.setName(rowMeta.getValueMeta(i).getName());
          fixValueMetaMask(conversionMetadata, rowMeta.getValueMeta(i));
        }
      }
    }
    for (int i = 0; i < rowMeta.size(); i++) {
      IValueMeta valueMeta = rowMeta.getValueMeta(i);
      try {
        // convert origin data by
        if (valueMeta.getConversionMetadata().getName() != null) {
          keys[i] = valueMeta.convertData(valueMeta.getConversionMetadata(), values[i]);
        } else {
          keys[i] = values[i];
        }
      } catch (HopValueException e) {
        throw new AssertionError("Convert key value: " + values[i], e);
      }
    }
    if (mixed) {
      return OpLog.of(opLog.getOp(), MixedKey.of(keys));
    }
    return OpLog.of(opLog.getOp(), (Comparable) keys[0]);
  }

  //  static void skipOpLog(OpLog<?> opLog, Object[] row, ITransform step, RowEventBag eventBag) {
  //    String code = "NON-KEY-001";
  //    if (opLog == null) {
  //      opLog = OpLog.build(Op.NONE, row[0] != null ? row[0].toString() : "[err-row-event]");
  //      code = "NON-KEY-002";
  //    }
  //    Comparable<?> rowKey = opLog.getRowKey();
  //    eventBag.pushEvent(rowKey, opLog);
  //    TransMessage transMessage =
  //        transMessage(rowKey, step.getTransformName(), !STRICT_KEY_VAL, FinishType.INVALID,
  // code);
  //    eventBag.getResultMap().put(rowKey, transMessage);
  //    if (STRICT_KEY_VAL) {
  //      step.setErrors(step.getErrors() + 1);
  //    } else if (step instanceof BaseTransform) {
  //      ((BaseTransform) step).incrementLinesRejected();
  //    }
  //  }

  static TransMessage transMessage(
      Comparable<?> key, String source, boolean success, FinishType finishType, String code) {
    TransMessage message = new TransMessage(source);
    message.setKey(key);
    message.setSuccess(success);
    message.setFinishType(finishType);
    message.setCode(code);
    message.setTime(new Timestamp(System.currentTimeMillis()));
    return message;
  }

  private static void fixValueMetaMask(IValueMeta from, IValueMeta to) {
    switch (to.getType()) {
        // TODO only support format: yyyyMMddHHmmss
      case IValueMeta.TYPE_TIMESTAMP:
      case IValueMeta.TYPE_DATE:
        from.setConversionMask("yyyyMMddHHmmss");
        break;
      case IValueMeta.TYPE_NUMBER:
        // TODO when precision is 0
      default:
        break;
    }
  }
}
