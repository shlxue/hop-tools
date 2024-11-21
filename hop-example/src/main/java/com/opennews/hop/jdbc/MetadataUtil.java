package com.opennews.hop.jdbc;

import lombok.Getter;
import org.apache.hop.core.IRowSet;
import org.apache.hop.core.QueueRowSet;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopPluginException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.*;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;

import java.sql.*;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.*;

@Getter
public final class MetadataUtil {
  static final String TABLE_NAME = "TABLE_NAME";
  static final String TABLE_TYPE = "TABLE_TYPE";
  static final String INDEX_NAME = "INDEX_NAME";
  static final String COLUMN_NAME = "COLUMN_NAME";
  static final String CARDINALITY = "CARDINALITY";
  private final IVariables variables;
  private final DatabaseMeta meta;
  private final Connection conn;
  private final DatabaseMetaData metaData;
  private final String category;
  private final String schema;
  private final boolean upperCaseIdentifiers;
  private final UnaryOperator<String> nameClear;

  private MetadataUtil(IVariables variables, DatabaseMeta meta, Connection conn)
      throws SQLException {
    this.variables = variables;
    this.meta = meta;
    this.conn = conn;
    this.metaData = conn.getMetaData();
    this.category = safeValue(conn::getCatalog);
    if (metaData.supportsSchemasInTableDefinitions()) {
      this.schema = safeValue(conn::getSchema);
    } else if (metaData.supportsCatalogsInTableDefinitions()) {
      this.schema = category;
    } else {
      schema = null;
    }
    this.upperCaseIdentifiers = metaData.storesUpperCaseIdentifiers();
    if (StringUtil.isEmpty(metaData.getIdentifierQuoteString())) {
      this.nameClear = s -> s;
    } else {
      String quote = metaData.getIdentifierQuoteString().trim();
      this.nameClear = s -> s != null ? s.replace(quote, "") : s;
    }
  }

  public static MetadataUtil getInstance(
      IVariables variables, DatabaseMeta meta, Connection connection) throws SQLException {
    return new MetadataUtil(variables, meta, connection);
  }

  public <T> List<T> fetch(
      SqlCallable<ResultSet> query, RowReader<T> reader, String... filterColumns)
      throws SQLException {
    List<T> rows = new ArrayList<>();
    try (ResultSet rs = query.call()) {
      //      IRowMeta rowMeta = createRowMeta(rs.getMetaData(), filterColumns);
      while (rs.next()) {
        rows.add(reader.read(rs));
      }
    }
    return rows;
  }

  public IRowMeta getRowMeta(String schema, String table) throws SQLException {
    IRowMeta rowMeta = new RowMeta();
    List<IValueMeta> valueMetas = fetch(() -> getColumns(schema, table), this::toColumn);
    valueMetas.forEach(rowMeta::addValueMeta);
    return rowMeta;
  }

  private String getTableName(ResultSet rs) throws SQLException {
    return nameClear.apply(rs.getString(TABLE_NAME));
  }

  private String getTableType(ResultSet rs) throws SQLException {
    return rs.getString(TABLE_TYPE);
  }

  public String getTableType(String schema, String table) throws SQLException {
    return fetch(
            () -> metaData.getTables(getCategory(), named(schema), named(table), null),
            this::getTableType,
            TABLE_TYPE)
        .stream()
        .findFirst()
        .orElse(null);
  }

  public List<String> getTable(String schemaPatter, String tablePattern) throws SQLException {
    String schema = safeValue(schemaPatter, getSchema());
    String table = safeValue(tablePattern, null);
    return fetch(
        () -> metaData.getTables(getCategory(), named(schema), named(table), null),
        this::getTableName,
        TABLE_NAME);
  }

  public String[] getTableTypes() throws SQLException {
    return fetch(metaData::getTableTypes, rs -> rs.getString(TABLE_TYPE)).toArray(new String[0]);
  }

  private String safeValue(SqlCallable<String> getter) {
    try {
      return getter.call();
    } catch (SQLException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private String safeValue(String value, String def) {
    return StringUtil.isEmpty(value) ? def : value;
  }

  public String[] getPrimaryKeys(String schema, String table) throws SQLException {
    List<String[]> list =
        fetch(
            () ->
                metaData.getPrimaryKeys(
                    getCategory(), safeValue(named(schema), getSchema()), named(table)),
            rs ->
                new String[] {rs.getString("PK_NAME"), nameClear.apply(rs.getString(COLUMN_NAME))});
    if (list.isEmpty()) {
      return new String[0];
    }
    return new String[] {
      list.get(0)[0], list.stream().map(strings -> strings[1]).collect(Collectors.joining(","))
    };
  }

  public <T> List<T> getIndexes(
      String schema, String table, BiFunction<IRowMeta, Object[], T> reader) throws SQLException {
    IRowSet rowSet = new QueueRowSet();
    try (ResultSet rs = getIndexInfo(schema, table)) {
      IRowMeta rowMeta = new RowMeta();
      rowMeta.addValueMeta(new ValueMetaBoolean("NON_UNIQUE"));
      rowMeta.addValueMeta(new ValueMetaString(INDEX_NAME));
      rowMeta.addValueMeta(new ValueMetaInteger("TYPE"));
      rowMeta.addValueMeta(new ValueMetaInteger(CARDINALITY));
      rowMeta.addValueMeta(new ValueMetaString("COLUMNS"));
      Map<String, Object[]> map = new LinkedHashMap<>();
      int colValueIndex = 4;
      while (rs.next()) {
        long type = rs.getShort("TYPE");
        String indexName = rs.getString(INDEX_NAME);
        String columnName = nameClear.apply(rs.getString(COLUMN_NAME));
        if (StringUtil.isEmpty(indexName) || StringUtil.isEmpty(columnName)) {
          continue;
        }
        Object[] row = map.get(indexName);
        SortedSet<Object[]> colList;
        if (row == null) {
          row = new Object[5];
          map.put(indexName, row);
          row[0] = rs.getBoolean("NON_UNIQUE");
          row[1] = rs.getString(INDEX_NAME);
          row[2] = type;
          row[3] = rs.getLong(CARDINALITY);
          colList = new TreeSet<>(Comparator.comparingInt(o -> (int) o[0]));
          row[colValueIndex] = colList;
        } else {
          colList = SortedSet.class.cast(row[colValueIndex]);
        }
        Object[] colInfo = new Object[4];
        colInfo[0] = rs.getInt("ORDINAL_POSITION");
        colInfo[1] = columnName;
        colInfo[2] = rs.getString("ASC_OR_DESC");
        colInfo[3] = rs.getLong(CARDINALITY);
        colList.add(colInfo);
      }
      map.values()
          .forEach(
              row -> {
                SortedSet<Object[]> colList = SortedSet.class.cast(row[colValueIndex]);
                row[colValueIndex] =
                    colList.stream()
                        .map(objects -> (String) objects[1])
                        .collect(Collectors.joining(","));
                rowSet.putRow(rowMeta, row);
              });
    } finally {
      rowSet.setDone();
    }
    List<T> rows = new ArrayList<>(rowSet.size());
    Object[] row;
    while ((row = rowSet.getRow()) != null) {
      rows.add(reader.apply(rowSet.getRowMeta(), row));
    }
    return rows;
  }

  private Object[] getIndex(ResultSet rs, String columnNames) throws SQLException {
    return null;
  }

  public ResultSet getColumns(String schemaPattern, String tablePattern) throws SQLException {
    return getColumns(schemaPattern, tablePattern, null);
  }

  public ResultSet getColumns(String schemaPattern, String tablePattern, String columnPattern)
      throws SQLException {
    String schema = Utils.isEmpty(schemaPattern) ? this.schema : schemaPattern;
    return metaData.getColumns(category, named(schema), named(tablePattern), named(columnPattern));
  }

  public ResultSet getIndexInfo(String schema, String table) throws SQLException {
    return getIndexInfo(schema, table, false);
  }

  public ResultSet getIndexInfo(String schema, String table, boolean unique) throws SQLException {
    return metaData.getIndexInfo(category, named(schema), named(table), unique, true);
  }

  private IRowMeta createRowMeta(ResultSetMetaData data, String... filterColNames)
      throws SQLException {
    int columnCount = data.getColumnCount();
    int[] ints = new int[filterColNames.length > 0 ? filterColNames.length : columnCount];
    if (filterColNames.length > 0) {
      Map<String, Integer> map = new HashMap<>(columnCount);
      for (int i = 1; i <= columnCount; i++) {
        map.put(data.getColumnName(i).toLowerCase(), i);
      }
      for (int i = 0; i < ints.length; i++) {
        String key = filterColNames[i].toLowerCase();
        assert map.containsKey(key);
        ints[i] = map.get(key);
      }
    } else {
      for (int i = 1; i <= columnCount; i++) {
        ints[i - 1] = i;
      }
    }
    IRowMeta rowMeta = new RowMeta();
    for (int i : ints) {
      IValueMeta valueMeta = createValueMeta(data, i);
      if (valueMeta == null) {
        valueMeta = createValueMeta(data.getColumnTypeName(i), data.getColumnName(i));
      }
      rowMeta.addValueMeta(valueMeta);
    }
    return rowMeta;
  }

  private IValueMeta toColumn(ResultSet rs) throws SQLException {
    IValueMeta valueMeta;
    String name = nameClear.apply(rs.getString(COLUMN_NAME));
    int len = rs.getInt("COLUMN_SIZE");
    int precision = -1;
    String typeName = rs.getString("TYPE_NAME");
    try {
      precision = rs.getObject("DECIMAL_DIGITS") == null ? 0 : rs.getInt("DECIMAL_DIGITS");
    } catch (Exception ex) {
    }
    int size = rs.getInt("COLUMN_SIZE");
    try {
      boolean signed = true;
      if (rs.getInt("DATA_TYPE") == Types.BIGINT && typeName != null) {
        signed = !typeName.toUpperCase().endsWith("UNSIGNED");
      }
      valueMeta =
          createValueMeta(rs.getInt("DATA_TYPE"), name, len, precision, size, typeName, signed);
    } catch (Exception e) {
      try {
        valueMeta = ValueMetaFactory.createValueMeta(name, 0);
      } catch (HopPluginException ignore) {
        valueMeta = new ValueMetaBase(name);
      }
    }
    valueMeta.setComments(rs.getString("REMARKS"));
    valueMeta.setOriginalColumnTypeName(typeName);
    valueMeta.setOriginalNullable(rs.getInt("NULLABLE"));
    if ("YES".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT"))) {
      valueMeta.setOriginalAutoIncrement(true);
    }
    return valueMeta;
  }

  private IValueMeta createValueMeta(ResultSetMetaData rm, int index) throws SQLException {
    return createValueMeta(rm, index, false, false);
  }

  private IValueMeta createValueMeta(
      ResultSetMetaData rm, int index, boolean ignoreLength, boolean lazyConversion)
      throws SQLException {
    int length = -1;
    int precision = -1;
    int valtype = IValueMeta.TYPE_NONE;
    boolean isClob = false;

    String name = rm.getColumnName(index);
    int type = rm.getColumnType(index);
    boolean signed = false;
    try {
      signed = rm.isSigned(index);
    } catch (Exception ignored) {
    }
    switch (type) {
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.NVARCHAR:
      case Types.LONGVARCHAR:
        valtype = IValueMeta.TYPE_STRING;
        if (!ignoreLength) {
          length = rm.getColumnDisplaySize(index);
        }
        break;

      case Types.CLOB:
      case Types.NCLOB:
        valtype = IValueMeta.TYPE_STRING;
        length = DatabaseMeta.CLOB_LENGTH;
        isClob = true;
        break;

      case Types.BIGINT:
        if (signed) {
          valtype = IValueMeta.TYPE_INTEGER;
          precision = 0;
          length = 15;
        } else {
          valtype = IValueMeta.TYPE_BIGNUMBER;
          precision = 0;
          length = 16;
        }
        break;

      case Types.INTEGER:
        valtype = IValueMeta.TYPE_INTEGER;
        precision = 0;
        length = 9;
        break;

      case Types.SMALLINT:
        valtype = IValueMeta.TYPE_INTEGER;
        precision = 0;
        length = 4;
        break;

      case Types.TINYINT:
        valtype = IValueMeta.TYPE_INTEGER;
        precision = 0;
        length = 2;
        break;

      case Types.DECIMAL:
      case Types.DOUBLE:
      case Types.FLOAT:
      case Types.REAL:
      case Types.NUMERIC:
        valtype = IValueMeta.TYPE_NUMBER;
        length = rm.getPrecision(index);
        precision = rm.getScale(index);
        if (length >= 126) {
          length = -1;
        }
        if (precision >= 126) {
          precision = -1;
        }

        if (type == Types.DOUBLE || type == Types.FLOAT || type == Types.REAL) {
          if (precision == 0) {
            precision = -1;
          }

          // MySQL: max resolution is double precision floating point (double)
          // The (12,31) that is given back is not correct
          if (meta.isMySqlVariant() && precision >= length) {
            precision = -1;
            length = -1;
          }

          // if the length or precision needs a BIGNUMBER
          if (length > 15 || precision > 15) {
            valtype = IValueMeta.TYPE_BIGNUMBER;
          }
        } else {
          if (precision == 0) {
            if (length <= 18 && length > 0) {
              // here.
              valtype = IValueMeta.TYPE_INTEGER;
              // significant digits
            } else if (length > 18) {
              valtype = IValueMeta.TYPE_BIGNUMBER;
            }
          } else { // we have a precision: keep NUMBER or change to BIGNUMBER?
            if (length > 15 || precision > 15) {
              valtype = IValueMeta.TYPE_BIGNUMBER;
            }
          }
        }

        //        if (meta.getIDatabase() instanceof PostgreSQLDatabaseMeta
        //            || meta.getIDatabase() instanceof GreenplumDatabaseMeta) {
        //          if (type == Types.NUMERIC && length == 0 && precision == 0) {
        //            valtype = IValueMeta.TYPE_BIGNUMBER;
        //            length = -1;
        //            precision = -1;
        //          }
        //        }
        //        if (meta.getIDatabase() instanceof OracleDatabaseMeta) {
        //          if (precision == 0 && length == 38) {
        //            valtype =
        //                ((OracleDatabaseMeta) meta.getIDatabase()).strictBigNumberInterpretation()
        //                    ? IValueMeta.TYPE_BIGNUMBER
        //                    : IValueMeta.TYPE_INTEGER;
        //          }
        //          if (precision <= 0 && length <= 0) {
        //            valtype = IValueMeta.TYPE_BIGNUMBER;
        //            length = -1;
        //            precision = -1;
        //          }
        //        }
        break;

      case Types.TIMESTAMP:
        if (meta.supportsTimestampDataType()) {
          valtype = IValueMeta.TYPE_TIMESTAMP;
          length = rm.getScale(index);
        }
        break;

      case Types.DATE:
      case Types.TIME:
        valtype = IValueMeta.TYPE_DATE;
        break;

      case Types.BOOLEAN:
      case Types.BIT:
        valtype = IValueMeta.TYPE_BOOLEAN;
        break;

      case Types.BINARY:
      case Types.BLOB:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        valtype = IValueMeta.TYPE_BINARY;
        break;

      default:
        valtype = IValueMeta.TYPE_STRING;
        precision = rm.getScale(index);
        break;
    }

    IValueMeta v;
    try {
      v = ValueMetaFactory.createValueMeta(name, valtype);
    } catch (HopException ex) {
      throw new IllegalStateException(ex);
    }
    v.setLength(length);
    v.setPrecision(precision);
    v.setLargeTextField(isClob);

    getOriginalColumnMetadata(v, rm, index, ignoreLength);

    if (lazyConversion && valtype == IValueMeta.TYPE_STRING) {
      v.setStorageType(IValueMeta.STORAGE_TYPE_BINARY_STRING);
      try {
        IValueMeta storageMetaData = ValueMetaFactory.cloneValueMeta(v, IValueMeta.TYPE_STRING);
        storageMetaData.setStorageType(IValueMeta.STORAGE_TYPE_NORMAL);
        v.setStorageMetadata(storageMetaData);
      } catch (Exception e) {
        throw new SQLException(e);
      }
    }
    return v;
  }

  private void getOriginalColumnMetadata(
      IValueMeta v, ResultSetMetaData rm, int index, boolean ignoreLength) throws SQLException {
    String comments = rm.getColumnLabel(index);
    v.setComments(comments);

    int originalColumnType = rm.getColumnType(index);
    v.setOriginalColumnType(originalColumnType);

    String originalColumnTypeName = rm.getColumnTypeName(index);
    v.setOriginalColumnTypeName(originalColumnTypeName);

    int originalPrecision = -1;
    if (!ignoreLength) {
      originalPrecision = rm.getPrecision(index);
    }
    v.setOriginalPrecision(originalPrecision);

    int originalScale = rm.getScale(index);
    v.setOriginalScale(originalScale);

    boolean originalSigned = false;
    try {
      originalSigned = rm.isSigned(index);
    } catch (Exception ignored) {
    }
    v.setOriginalSigned(originalSigned);
  }

  private IValueMeta createValueMeta(
      int sqlType, String name, int len, int precision, int size, String typeName, boolean signed) {
    int type = IValueMeta.TYPE_NONE;
    boolean clob = false;
    switch (sqlType) {
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
      case Types.NCHAR:
      case Types.NVARCHAR:
        type = IValueMeta.TYPE_STRING;
        len = size;
        break;
      case Types.CLOB:
      case Types.NCLOB:
        type = IValueMeta.TYPE_STRING;
        len = DatabaseMeta.CLOB_LENGTH;
        clob = true;
        break;
      case Types.BIGINT:
        type = IValueMeta.TYPE_BIGNUMBER;
        len = 16;
        if (signed) {
          type = IValueMeta.TYPE_INTEGER;
          len = 15;
        }
        precision = 0;
        break;
      case Types.INTEGER:
      case Types.SMALLINT:
      case Types.TINYINT:
        type = IValueMeta.TYPE_INTEGER;
        precision = 0;
        len = sqlType == Types.INTEGER ? 9 : (sqlType == Types.TINYINT) ? 2 : 4;
        break;
      case Types.NUMERIC:
      case Types.DECIMAL:
      case Types.DOUBLE:
      case Types.FLOAT:
      case Types.REAL:
        type = IValueMeta.TYPE_NUMBER;
        if (len >= 126) {
          len = -1;
        }
        if (precision >= 126) {
          precision = -1;
        }

        if (sqlType == Types.DOUBLE || sqlType == Types.FLOAT || sqlType == Types.REAL) {
          if (precision == 0) {
            precision = -1;
          }
          if (isDbVariant(meta, Type.PostgresVariant)) {
            if (sqlType == Types.DOUBLE && precision >= 16 && len > 16) {
              precision = -1;
              len = -1;
            }
          } else if (isDbVariant(meta, Type.MySqlVariant)) {
            if (precision >= len) {
              precision = -1;
              len = -1;
            } else if (sqlType == Types.DOUBLE && len > 15) {
              len = -1;
            }
          }
          if (len > 15 || precision > 15) {
            type = IValueMeta.TYPE_BIGNUMBER;
          }
        } else {
          if (precision == 0) {
            if (len <= 18 && len > 0) {
              type = IValueMeta.TYPE_INTEGER;
            } else if (len > 18) {
              type = IValueMeta.TYPE_BIGNUMBER;
            }
          } else if (len > 15 || precision > 15) {
            type = IValueMeta.TYPE_BIGNUMBER;
          }
        }

        if (isDbVariant(meta, Type.PostgresVariant)) {
          if (sqlType == Types.NUMERIC && len == 0 && precision == 0) {
            type = IValueMeta.TYPE_BIGNUMBER;
            len = -1;
            precision = -1;
          }
        } else if (isDbVariant(meta, Type.OracleVariant)) {
          if (precision == 0 && len == 38) {
            //            type =
            //                ((OracleDatabaseMeta)
            // meta.getIDatabase()).strictBigNumberInterpretation()
            //                    ? IValueMeta.TYPE_BIGNUMBER
            //                    : IValueMeta.TYPE_INTEGER;
          }
          if (precision <= 0 && len <= 0) {
            type = IValueMeta.TYPE_BIGNUMBER;
            len = -1;
            precision = -1;
          }
        }
        break;
      case Types.TIMESTAMP:
        if (meta.supportsTimestampDataType()) {
          type = IValueMeta.TYPE_TIMESTAMP;
          len = precision;
        }
        break;
      case Types.DATE:
        //        if (meta.getIDatabase() instanceof TeradataDatabaseMeta) {
        //          precision = 1;
        //        }
      case Types.TIME:
        type = IValueMeta.TYPE_DATE;
        if (isDbVariant(meta, Type.MySqlVariant)) {
          //          String yearIsDate =
          // meta.getConnectionProperties().getProperty("yearIsDateType");
          //          if ("false".equalsIgnoreCase(yearIsDate) && "YEAR".equalsIgnoreCase(typeName))
          // {
          //            type = IValueMeta.TYPE_INTEGER;
          //            len = 4;
          //            precision = 0;
          //          }
        }
        break;
      case Types.BOOLEAN:
      case Types.BIT:
        type = IValueMeta.TYPE_BOOLEAN;
        break;
      case Types.BINARY:
      case Types.BLOB:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        type = IValueMeta.TYPE_BINARY;
        if (meta.isDisplaySizeTwiceThePrecision() && 2 * precision == size) {
          len = precision;
        } else if (isDbVariant(meta, Type.OracleVariant)
            && (sqlType == Types.VARBINARY || sqlType == Types.LONGVARBINARY)) {
          type = IValueMeta.TYPE_STRING;
          len = size;
        } else if (isDbVariant(meta, Type.SqliteVariant)) {
          type = IValueMeta.TYPE_STRING;
        } else {
          len = -1;
        }
        precision = -1;
        break;
      default:
        type = IValueMeta.TYPE_STRING;
        precision = size;
        break;
    }
    IValueMeta vmi;
    try {
      vmi = ValueMetaFactory.createValueMeta(name, type);
    } catch (HopException ex) {
      throw new IllegalStateException(ex);
    }
    vmi.setLength(len);
    vmi.setPrecision(precision);
    vmi.setLargeTextField(clob);
    return vmi;
  }

  private boolean isBigNumber(int len, int precision) {
    return len > 15 || precision > 15;
  }

  private IValueMeta createValueMeta(String dataType, String name) {
    if (dataType.equalsIgnoreCase("Integer") || dataType.equalsIgnoreCase("Long")) {
      return new ValueMetaInteger(name);
    } else if (dataType.equalsIgnoreCase("BigDecimal") || dataType.equalsIgnoreCase("BigNumber")) {
      return new ValueMetaBigNumber(name);
    } else if (dataType.equalsIgnoreCase("Double") || dataType.equalsIgnoreCase("Number")) {
      return new ValueMetaNumber(name);
    } else if (dataType.equalsIgnoreCase("String")) {
      return new ValueMetaString(name);
    } else if (dataType.equalsIgnoreCase("Date")) {
      return new ValueMetaDate(name);
    } else if (dataType.equalsIgnoreCase("Boolean")) {
      return new ValueMetaBoolean(name);
    } else if (dataType.equalsIgnoreCase("Binary")) {
      return new ValueMetaBinary(name);
    } else if (dataType.equalsIgnoreCase("Timestamp")) {
      return new ValueMetaTimestamp(name);
    } else if (dataType.equalsIgnoreCase("Internet Address")) {
      return new ValueMetaInternetAddress(name);
    }
    return null;
  }

  private String named(String val) {
    val = safeValue(val, null);
    if (val != null && upperCaseIdentifiers) {
      return val.toUpperCase();
    }
    return val;
  }

  private Type dbType(DatabaseMeta databaseMeta) {
    if (meta.getIDatabase() != null && !StringUtil.isEmpty(meta.getIDatabase().getDriverClass())) {
      String driverClass = meta.getIDatabase().getDriverClass();
      for (Type type : Type.values()) {
        if (driverClass.equals(type.name())) {}
      }
    }
    return Type.UNKNOWN;
  }

  private boolean isDbVariant(DatabaseMeta meta, Type type) {
    if (meta.getIDatabase() != null) {
      String driverClass = meta.getIDatabase().getDriverClass();
      if (driverClass != null) {}
      String name = meta.getIDatabase().getClass().getSimpleName();
      if (name.endsWith("DatabaseMeta")) {
        String prefix = type.name().replace("Variant", "").toLowerCase();
        if (name.toLowerCase().startsWith(prefix)) {
          return true;
        }
      }
    }
    return false;
  }

  private enum Type {
    UNKNOWN(""),
    DuckDbVariant(""),
    GreenplumVariant(""),
    NetezzaVariant(""),
    MySqlVariant(""),
    OracleVariant(""),
    SqliteVariant(""),
    TeradataVariant(""),
    PostgresVariant("");

    private final String driver;

    Type(String driver) {
      this.driver = driver;
    }
  }
}
