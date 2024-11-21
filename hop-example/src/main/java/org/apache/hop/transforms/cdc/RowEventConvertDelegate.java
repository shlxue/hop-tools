package org.apache.hop.transforms.cdc;

import com.opennews.domain.FinishType;
import com.opennews.domain.Op;
import com.opennews.domain.OpLog;
import com.opennews.domain.TransMessage;
import org.apache.hop.core.IRowSet;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowDataUtil;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.row.value.ValueMetaSerializable;
import org.apache.hop.core.variables.IVariables;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.*;

@SuppressWarnings("squid:S3740")
class RowEventConvertDelegate extends BaseDelegate {

  private List<OpLog<?>> caches;
  private Map<Comparable<?>, Collection<TransMessage>> rowEvents;
  private IRowMeta inputRowMeta;
  private Boolean compatibleKey;
  private Map<Object, Comparable<?>> compatibleKeyMap;
  private boolean mixedKey;

  public RowEventConvertDelegate(IVariables variables, OpEventInput step) {
    super(variables, step);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean beforeProcess(List<IRowSet> inputRowSets) throws HopException {
    if (!super.beforeProcess(inputRowSets)) {
      return false;
    }
    if (inputRowSets.isEmpty()) {
      return false;
    }
    data.inputRowSet = inputRowSets.get(0);
    inputRowMeta = data.inputRowSet.getRowMeta();
    if (inputRowMeta == null) {
      inputRowMeta = new RowMeta();
      inputRowMeta.addValueMeta(new ValueMetaSerializable("_op"));
    }
    data.keyMetas = new IValueMeta[data.outputMeta.size()];
    if (inputRowMeta.size() == 1
        && inputRowMeta.getValueMeta(0).getType() == IValueMeta.TYPE_SERIALIZABLE) {
      Map<String, Object> extensionDataMap = step.getPipeline().getExtensionDataMap();
      rowEvents = (Map<Comparable<?>, Collection<TransMessage>>) extensionDataMap.get("rowEvents");
      compatibleKeyMap = (Map<Object, Comparable<?>>) extensionDataMap.get("compatibleKeyMap");
      caches = (List<OpLog<?>>) extensionDataMap.get("events");
      mixedKey = meta.getKeyFields().size() > 1;
      return initKeyIndex(inputRowMeta, data);
    }
    return false;
  }

  @Override
  protected boolean initKeyIndex(IRowMeta inputRowMeta, OpEventInputData data) {
    data.allMatch = true;
    data.keyNrs = new int[data.outputMeta.size()];
    for (int i = 0; i < data.keyNrs.length; i++) {
      data.keyNrs[i] = i;
    }
    return true;
  }

  @Override
  protected boolean processRow() throws HopException {
    Object[] row = step.getRow();
    if (row == null) {
      return false;
    }
    OpLog<?> opLog = Util.toOpLog(row[0]);
    OpLog ref;
    try {
      step.incrementLinesUpdated();
      ref = Util.toOpLog(opLog, data.keyMeta);
      // FIXME
      // eventBag.pushEvent(ref.getRowKey(), opLog);
    } catch (Throwable ex) {
      step.logError(i18n("MSG.InvalidKeyVal", row), ex);
      // FIXME
      // Util.skipOpLog(opLog, row, step, eventBag);
      return true;
    }
    if (meta.isIgnoreDel() && ref.getOp() == Op.DELETE) {
      fastCloseEvent(ref.getRowKey(), FinishType.SKIP, "IGNORE-DEL");
      return true;
    }

    IRowSet outputRowSet = Op.DELETE == ref.getOp() ? data.deleteRowSet : data.defaultRowSet;
    Object[] cloneRow = copyRowFromOpLog(data.outputMeta, ref);
    if (compatibleKey == null) {
      compatibleKey = Objects.deepEquals(opLog.getRowKey(), ref.getRowKey());
    }
    if (!isCompatibleKey() && !mixedKey) {
      cloneRow = convertData(data.keyMetas, data.outputMeta, cloneRow);
      ref.setRowKey(cloneRow[0]);
    }
    initContext(opLog, ref);
    Object[] keyData = RowDataUtil.allocateRowData(data.outputMeta.size());
    keyData[0] = opLog.getRowKey();
    Comparable<?>[] keys = ref.getKeys();
    System.arraycopy(keys, 0, keyData, 1, keys.length);
    putRow(data.outputMeta, outputRowSet, keyData);
    if (feedback.apply(step.getLinesInput())) {
      step.logRowlevel("{0} row: {1}", step.getLinesRead(), data.outputMeta.getString(cloneRow));
    }
    return true;
  }

  @Override
  protected boolean isCompatibleKey() {
    return compatibleKey;
  }

  private Object[] copyRowFromOpLog(IRowMeta rowMeta, OpLog<?> opLog) {
    if (rowMeta.size() == 1) {
      return new Object[] {opLog.getRowKey()};
    }
    return opLog.getKeys();
  }

  private void fastCloseEvent(Comparable<?> key, FinishType type, String code) {
    // FIXME
    // eventBag.getResultMap().put(key, Util.transMessage(key, step.getTransformName(), true, type,
    // code));
    step.incrementLinesRejected();
  }

  private boolean compatibleKeys(IRowMeta input, IRowMeta output, IValueMeta[] metas, Object[] row)
      throws HopException {
    boolean allMatch = true;
    if (output.size() > 1 && metas.length > 1) {
      return false;
    }
    IRowMeta keyMeta = input;
    if (input.size() == 1 && input.getValueMeta(0).getType() == IValueMeta.TYPE_SERIALIZABLE) {
      keyMeta = new RowMeta();
      for (int i = 0; i < output.size(); i++) {
        IValueMeta guessValueMeta = ValueMetaFactory.guessValueMetaInterface(row[i]);
        if (guessValueMeta == null) {
          if (row[i] instanceof Number) {
            guessValueMeta = ValueMetaFactory.createValueMeta(IValueMeta.TYPE_INTEGER);
          } else {
            guessValueMeta = output.getValueMeta(i);
          }
        }
        keyMeta.addValueMeta(guessValueMeta);
      }
    }
    assert keyMeta.size() == output.size();
    for (int i = 0; i < output.size(); i++) {
      final IValueMeta valueMeta = keyMeta.getValueMeta(i);
      if (!sameValueMeta(valueMeta, output.getValueMeta(i))) {
        metas[i] = valueMeta;
        allMatch = false;
      }
    }
    return allMatch;
  }

  private boolean sameValueMeta(IValueMeta left, IValueMeta right) {
    boolean same = left.getType() == right.getType();
    if (same && right.getType() == IValueMeta.TYPE_STRING) {
      return true;
    }
    return left.getLength() == right.getLength() && left.getPrecision() == right.getPrecision();
  }

  private Object[] convertData(IValueMeta[] keyMetas, IRowMeta outputMeta, Object[] data)
      throws HopValueException {
    for (int i = 0; i < data.length; i++) {
      if (keyMetas[i] != null) {
        data[i] = outputMeta.getValueMeta(i).convertDataCompatible(keyMetas[i], data[i]);
      }
    }
    return data;
  }

  private void initContext(OpLog<?> opLog, OpLog<?> ref) {
    caches.add(opLog);
    if (!isCompatibleKey()) {
      compatibleKeyMap.put(ref.getRowKey(), opLog.getRowKey());
    }
    if (rowEvents != null) {
      rowEvents.put(ref.getRowKey(), new ConcurrentLinkedQueue<>());
    }
  }
}
