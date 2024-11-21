package org.apache.hop.transforms.cdc;

import com.opennews.domain.MixedKey;
import com.opennews.domain.Op;
import com.opennews.domain.OpLog;
import com.opennews.domain.RefKey;
import org.apache.hop.core.IRowSet;
import org.apache.hop.core.database.Database;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.parameters.INamedParameters;
import org.apache.hop.core.parameters.UnknownParamException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowDataUtil;
import org.apache.hop.core.row.value.ValueMetaBoolean;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.util.Assert;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transform.stream.IStream;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.*;

import static org.apache.hop.transforms.cdc.SubOpEventMeta.i18n;

public class SubOpEvent extends BaseTransform<SubOpEventMeta, SubOpEventData> {
  private static final Class<?> PKG = SubOpEventMeta.class;

  static final String TRANS_PARAM_KEY = "subTableName";
  private SubOpEventMeta meta;
  private SubOpEventData data;
  // private RowEventBag eventBag;
  private Set<RefKey> batchKeySet;
  private Map<Comparable<?>, List<RefKey>> batchMap;
  private boolean preview;
  int mKeyCount = 0;
  int keyCount = 0;
  int nfdCount = 0;
  int deleteCount = 0;
  int ignoreCount = 0;

  private final BlockingQueue<KeyMap> batchRows = new ArrayBlockingQueue<>(1000);

  public SubOpEvent(
      TransformMeta transformMeta,
      SubOpEventMeta meta,
      SubOpEventData data,
      int copyNr,
      PipelineMeta pipelineMeta,
      Pipeline pipeline) {
    super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
    this.meta = meta;
    this.data = data;
  }

  @Override
  public boolean processRow() throws HopException {
    if (first) {
      // init default stream
      meta.setChanged(false);
      initDynamicOutputStreams(meta.getTransformIOMeta().getTargetStreams());
      if (data.active && getInputRowSets().isEmpty()) {
        injectRowEventByQuery(meta.getRelation(getActiveSubTable()));
      }
      for (int i = 0; i < data.keyRowMeta.size(); i++) {
        data.keyRowMeta.getValueMeta(i).setConversionMetadata(null);
      }
      batchMap = new HashMap<>();
      batchKeySet = new HashSet<>();
      // FIXME remove?
      // eventBag = TransUtil.getData(RowEventBag.class, getPipeline(), 30000);
      first = false;
    }
    if (data.active) {
      Object[] row = super.getRow();
      if (row != null || !batchRows.isEmpty()) {
        KeyMap rowKeyMap = null;
        if (row != null) {
          OpLog<?> opLog = Util.toOpLog(row[0]);
          try {
            rowKeyMap = toKeyMap(row, opLog);
            if (batchRows.offer(rowKeyMap)) {
              return true;
            }
          } catch (Throwable ex) {
            logMinimal(i18n("MSG.InvalidKeyVal", row));
            // FIXME remove?
            // Util.skipOpLog(opLog, row, this, eventBag);
            return true;
          }
        }
        for (KeyMap keyMap : batchRows) {
          if (keyMap.nonDeleteOp()) {
            queryMasterTableKeys(keyMap);
          }
        }
        BatchMarker marker = new BatchMarker(batchRows);
        Assert.assertTrue(marker.marked(), "Marked batch rows were not processed: " + marker);
        logBasic(SubOpEventMeta.i18n("MSG.ConvertKeys", marker));
        batchKeySet.addAll(marker.dispatch(batchRows, batchMap));

        if (preview) {
          //          for (Object[] objects : batchRows) {
          //            if (objects[data.tempMasterKeysIndex] instanceof List) {
          //              for (Object[] masterKey : (List<Object[]>)
          // objects[data.tempMasterKeysIndex]) {
          //                Object[] clone = new Object[data.outMeta.size()];
          //                System.arraycopy(objects, 0, clone, 0, data.tempMasterKeysIndex);
          //                System.arraycopy(masterKey, 0, clone, data.tempMasterKeysIndex,
          // masterKey.length);
          //                putRowTo(data.outMeta, clone, data.outputRowSet);
          //                incrementLinesInput();
          //              }
          //            } else {
          //              objects[data.tempMasterKeysIndex] =
          //                  (boolean) objects[data.opIndex] ? data.delStep : meta.getNoneStep();
          //              putRowTo(data.outMeta, objects, data.outputRowSet);
          //            }
          //          }
        } else {
          // FIXME remove?
          // eventBag.pushKeys(batchKeySet);
          for (KeyMap keyMap : batchRows) {
            switch (keyMap.type()) {
              case DELETE:
                sendToDelFlow(keyMap);
                break;
              case NOT_FOUND:
                sendToNotFoundFlow(keyMap);
                break;
              case MATCH:
                keyCount++;
                keyMap.forEach((mKey, tap) -> sendToDefaultFlow(keyMap.raw(), mKey, tap));
                break;
              case DUPLICATE:
                fastCloseEvent(keyMap.key(), true, "Ref: " + keyMap.getRefKey());
                break;
              default:
                break;
            }
          }
          logBasic(
              i18n(
                  "MSG.DispatchRowEvent",
                  batchRows.size(),
                  keyCount,
                  mKeyCount,
                  nfdCount,
                  deleteCount,
                  ignoreCount));
        }
        batchKeySet.clear();
        batchMap.clear();
        batchRows.clear();
        if (row != null) {
          return batchRows.offer(rowKeyMap);
        }
      }
    }
    setOutputDone();
    closeQueryIfNeed();
    return false;
  }

  private void sendToDelFlow(KeyMap keyMap) throws HopException {
    if (data.ignoreDel) {
      fastCloseEvent(keyMap.key(), false, "DEL");
    } else if (data.deleteRowSet != null) {
      putRowTo(
          data.delMeta,
          newKeyData(keyMap.row(), keyMap.raw(), keyToArray(keyMap.key())),
          data.deleteRowSet);
      incrementLinesWritten();
    }
    deleteCount++;
  }

  private void sendToNotFoundFlow(KeyMap keyMap) throws HopException {
    if (meta.isIgnoreNonMap()) {
      fastCloseEvent(keyMap.key(), false, "NDF-IGNORE");
    } else if (data.notKeyRowSet != null) {
      putRowTo(
          data.noneMeta,
          newKeyData(keyMap.row(), keyMap.raw(), keyToArray(keyMap.key())),
          data.notKeyRowSet);
      incrementLinesWritten();
    }
    nfdCount++;
  }

  private void sendToDefaultFlow(Comparable<?> rawKey, Comparable<?> mKey, AtomicBoolean tap) {
    keyCount++;
    // FIXME remove?
    boolean appended = false; // eventBag.pushKey2Key(mKey, batchMap.get(mKey));
    if (tap.get() && appended) {
      mKeyCount++;
      Object[] toKeys = keyToArray(mKey);
      Object[] rowData =
          newKeyData(
              RowDataUtil.allocateRowData(data.masterKeyMeta.size()),
              Util.joinOpLogKey(data.masterKeyMeta, toKeys),
              toKeys);
      try {
        putRowTo(data.outMeta, rowData, data.outputRowSet);
      } catch (HopException ex) {
        throw new IllegalStateException("Send row to next step", ex);
      }
      if (checkFeedback(incrementLinesUpdated())) {
        logBasic(i18n("MSG.ConvertKeyMap", rawKey, mKey, appended));
      }
    }
  }

  private Object[] keyToArray(Comparable<?> key) {
    return key instanceof MixedKey ? ((MixedKey) key).getValues() : new Object[] {key};
  }

  private Object[] newKeyData(Object[] rowData, Object originKey, Object[] toKeys) {
    rowData[0] = originKey;
    System.arraycopy(toKeys, 0, rowData, 1, toKeys.length);
    return rowData;
  }

  private void fastCloseEvent(Comparable<?> key, boolean duplicate, String code) {
    // FIXME removed
    // eventBag.getResultMap().put(key, Util.transMessage(key, getTransformName(), true, type,
    // code));
    incrementLinesRejected();
    ignoreCount++;
  }

  private KeyMap toKeyMap(Object[] row, OpLog<?> opLog) {
    row = RowDataUtil.resizeArray(row, data.arraySize);
    OpLog<?> ref = Util.wrapEventRow(row, data.keyRowMeta, opLog);
    // FIXME removed
    // eventBag.pushEvent(ref.getRowKey(), opLog);
    return new KeyMap(row, opLog.getRowKey(), ref.getRowKey(), opLog.getOp() == Op.DELETE);
  }

  private void initDynamicOutputStreams(List<IStream> streams) throws HopException {
    if (streams.get(0).getTransformMeta() != null) {
      data.outputRowSet = findOutputRowSet(streams.get(0).getTransformName());
    }
    if (!meta.isIgnoreNonMap() && streams.get(1).getTransformMeta() != null) {
      data.notKeyRowSet = findOutputRowSet(streams.get(1).getTransformName());
    }
    if (!data.ignoreDel) {
      for (Relationship item : meta.getDetails()) {
        if (!item.isIgnoreDel() && !StringUtil.isEmpty(item.getDelStep())) {
          IRowSet outputRowSet = findOutputRowSet(item.getDelStep());
          if (outputRowSet != null) {
            if (data.subTableName.equalsIgnoreCase(item.getDetail().getName())) {
              data.deleteRowSet = outputRowSet;
            } else {
              outputRowSet.setDone();
            }
          }
        }
      }
    }
    if (!meta.isIgnoreNonMap() && data.notKeyRowSet == null) {
      logMinimal(i18n("MSG.MissingFLowWhenNonMap"));
    }
    if (data.deleteRowSet == null && !data.ignoreDel && data.active) {
      logMinimal(i18n("MSG.MissingFlow4DeleteEvent"));
    }
  }

  private void closeQueryIfNeed() {
    try {
      if (data.query != null) {
        data.query.close();
      }
      if (data.db != null) {
        data.db.close();
        data.db = null;
      }
    } catch (SQLException ex) {
      // ignore
    }
  }

  private void convertKeys(IRowMeta rowMeta, int[] index, Object[] row) throws HopException {
    //        int length = Math.min(rowMeta.size(), row.length);
    for (int i : index) {
      IValueMeta valueMeta = rowMeta.getValueMeta(i);
      row[i] = valueMeta.convertData(valueMeta, row[i]);
    }
  }

  private ResultSet openQuery(PreparedStatement ps, IRowMeta meta, Object[] params)
      throws HopException, SQLException {
    for (int i = 0; i < meta.size(); i++) {
      meta.getValueMeta(i)
          .setPreparedStatementValue(data.db.getDatabaseMeta(), ps, i + 1, params[i]);
    }
    return ps.executeQuery();
  }

  private boolean queryMasterTableKeys(KeyMap keyMap) throws HopException {
    try (ResultSet rs = openQuery(data.query, data.keyRowMeta, keyMap.getKeys())) {
      List<Object[]> rows = data.db.getRows(rs, data.limit, null);
      Set<Comparable<?>> toKey = Collections.emptySet();
      if (!rows.isEmpty()) {
        setLinesInput(getLinesInput() + rows.size());
        if (rows.size() == 1) {
          toKey = Collections.singleton(data.masterKeyGetter.apply(rows.get(0)));
        } else {
          toKey =
              rows.stream()
                  .map(objects -> data.masterKeyGetter.apply(objects))
                  .collect(Collectors.toSet());
        }
      }
      if (data.limit >= 2 && rows.size() == data.limit) {
        logMinimal(
            i18n("MSG.LinkLimit"),
            data.limit - 1,
            rows.size(),
            data.keyRowMeta.getString(keyMap.getKeys()));
      }
      keyMap.mKeys(toKey);
      return true;
    } catch (SQLException e) {
      throw new HopException(e);
    }
  }

  private String getActiveSubTable() {
    String subTable = getSubTableName(getPipeline());
    if (StringUtil.isEmpty(subTable)) {
      try {
        subTable = getPipeline().getParameterValue(TRANS_PARAM_KEY);
      } catch (UnknownParamException ignore) {
      }
    }
    if (StringUtil.isEmpty(subTable) && preview) {
      subTable = getPipeline().getVariable(TRANS_PARAM_KEY);
    }
    return subTable;
  }

  protected String getSubTableName(INamedParameters namedParams) {
    try {
      return namedParams.getParameterValue(TRANS_PARAM_KEY);
    } catch (HopException ignore) {
    }
    return null;
  }

  @Override
  public boolean init() {
    this.preview = getPipeline().isPreview();
    if (!super.init()) {
      return false;
    }
    data.subTableName = getActiveSubTable();
    data.active = !StringUtil.isEmpty(data.subTableName);
    if (!data.active) {
      logDetailed(i18n("MSG.DisableSubOpEvent"));
      return true;
    }
    if (StringUtil.isEmpty(meta.getConnection())) {
      logError(i18n("MSG.ConnectionMissing", getTransformName()));
      return false;
    }

    meta.initMasterIfExist(meta.getMaster());
    meta.getMaster().getPkFields().forEach(vm -> data.masterKeyMeta.addValueMeta(vm));
    logDetailed(i18n("MSG.ActiveSubTable", data.subTableName));
    Relationship item = meta.getRelation(data.subTableName);
    if (item == null || item.getDetail() == null || item.getDetail().getPkFields().isEmpty()) {
      logError(i18n("MSG.InvalidRelationTable", data.subTableName));
      return false;
    }

    item.getDetail().getPkFields().forEach(vm -> data.keyRowMeta.addValueMeta(vm));
    String name = getTransformName();
    if (!meta.isIgnoreAllDelOp() && !item.isIgnoreDel()) {
      meta.getFields(data.delMeta, name, null, getStep(item.getDelStep()), this, metadataProvider);
    }
    meta.getFields(data.noneMeta, name, null, getStep(meta.getNoneStep()), this, metadataProvider);
    meta.getFields(data.outMeta, name, null, getStep(meta.getOutStep()), this, metadataProvider);

    data.opIndex = data.keyRowMeta.size() + 1;
    data.keyCacheIndex = data.opIndex + 1;
    data.tempMasterKeysIndex = data.keyCacheIndex + 1;
    data.ignoreDel = meta.isIgnoreAllDelOp() || item.isIgnoreDel();

    if (preview && meta.showAllField) {
      AtomicInteger index = new AtomicInteger();
      for (IValueMeta vm : data.noneMeta.getValueMetaList()) {
        IValueMeta field = vm.clone();
        if (!field.getName().startsWith("_")) {
          field.setName("_" + field.getName());
        }
        data.outMeta.addValueMeta(index.getAndIncrement(), field);
      }
      data.outMeta.addValueMeta(index.getAndIncrement(), new ValueMetaBoolean("_delOp"));
      data.outMeta.addValueMeta(index.getAndIncrement(), new ValueMetaString("_nextStep"));
    }

    data.arraySize = Math.max(data.outMeta.size(), data.noneMeta.size() + 2);
    data.detailIndex = initIndex(0, data.noneMeta.size());
    data.masterKeyIndex =
        initIndex(data.outMeta.size() - meta.getMaster().getPkFields().size(), data.outMeta.size());
    Assert.assertTrue(
        item.getLimit() > 0 && item.getLimit() < 256, "limit must be between 1 and 255");
    data.limit = item.getLimit() + 1;

    data.mixedGetter = data.keyRowMeta.size() > 1 ? Util::toOpLog : (op, meta) -> op;
    data.masterKeyGetter = objects -> (Comparable<?>) objects[0];
    int masterKeyLen = meta.getMaster().getPkFields().size();
    if (masterKeyLen > 1) {
      data.masterKeyGetter = objects -> MixedKey.of(Arrays.copyOf(objects, masterKeyLen));
    }

    DatabaseMeta databaseMeta = getPipelineMeta().findDatabase(meta.getConnection(), variables);
    data.db = new Database(this, variables, databaseMeta);
    try {
      // only debug
      data.db.shareWith(this);
      data.db.connect();
      data.query = data.db.prepareSql(item.getDesignSqlText());
      data.query.setMaxRows(256);
      data.query.setFetchSize(data.limit);
      return true;
    } catch (SQLException | HopException e) {
      logError(i18n("MSG_ErrorOccurred", e.getMessage()));
      setErrors(1L);
      stopAll();
    }

    return false;
  }

  private void injectRowEventByQuery(Relationship item) {
    DatabaseMeta dbMeta = data.db.getDatabaseMeta();
    int count = 0;
    // FIXME
    //        InjectOpEvent.inject(
    //            getPipeline(), dbMeta, this, item.getTestSqlText(), data.keyRowMeta,
    // meta.getRowLimit());
    logDebug(i18n("MSG.InjectRowCount", data.subTableName, count));
  }

  private void initTargetFlows(List<IStream> streams) {
    List<String> delFlows = new ArrayList<>(streams.size() - 2);
    for (IStream stream : streams) {
      delFlows.add(stream.getTransformName());
    }
    for (Relationship item : meta.getDetails()) {
      assert delFlows.contains(meta.getRelation(item.getDetail().getName()).getDelStep());
    }
  }

  private TransformMeta getStep(String name) {
    return getPipelineMeta().findTransform(name);
  }

  private int[] initIndex(int start, int count) {
    int[] index = new int[count];
    for (int i = 0; i < count; i++) {
      index[i] = start + i;
    }
    return index;
  }

  @Override
  public void dispose() {
    if (data.db != null) {
      data.db.disconnect();
    }
    super.dispose();
  }
}
