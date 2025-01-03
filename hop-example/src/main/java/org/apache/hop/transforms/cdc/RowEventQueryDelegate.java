package org.apache.hop.transforms.cdc;

import org.apache.hop.core.IRowSet;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.transforms.cdc.domain.FinishType;
import org.apache.hop.transforms.cdc.domain.MixedKey;
import org.apache.hop.transforms.cdc.domain.Op;
import org.apache.hop.transforms.cdc.domain.OpLog;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

class RowEventQueryDelegate extends BaseDelegate {

  private AtomicInteger count;
  private final Set<Comparable<?>> batch = new HashSet<>();
  private int rowIndex;
  // private Results.Stream stream;

  RowEventQueryDelegate(IVariables variables, OpEventInput step) {
    super(variables, step);
  }

  @Override
  public boolean init() {
    boolean pass = super.init();
    pass &=
        step.check(
            StringUtil.isEmpty(meta.getConnection()),
            step::logError,
            "OEI.Init.ConnectionMissing",
            step.getTransformName());

    if (!pass) {
      return false;
    }

    // TODO ...
    // data.db = DBUtil.connect(step, meta.getDatabaseMeta());
    IRowMeta rowMeta = new RowMeta();
    meta.getTabModel().getPkFields().forEach(rowMeta::addValueMeta);
    return initKeyIndex(rowMeta, data);
  }

  @Override
  protected boolean beforeProcess(List<IRowSet> inputRowSets) throws HopException {
    if (!super.beforeProcess(inputRowSets)) {
      return false;
    }
    if (meta.getRowLimit() > 0) {
      data.db.setQueryLimit(meta.getRowLimit());
    }
    try {
      String sql = meta.getExecuteSql();
      data.rs = data.db.openQuery(sql);
      // FIXME
      //      stream = TransUtil.getData(Results.RsData.class, step.getPipeline(),
      // Features.BAG_RS_DATA, 7000).stream();
      //      stream.initMetrics();
      rowIndex = 0;
      //          } catch (HopDatabaseException e) {
      //            step.logError(step.message(pkg, "OEI.Error.OpeningSqlQuery", e.getMessage()));
    } catch (Exception e) {
      step.logError(i18n("OEI.Error.UnknownError"), e);
    }
    if (data.rs != null) {
      count = new AtomicInteger();
    }
    return data.rs != null;
  }

  @Override
  public boolean processRow() throws HopException {
    Object[] row = data.db.getRow(data.rs);
    if (row == null) {
      // FIXME
      // stream.initMetrics(rowIndex);
      // stream.getCount().setKey(rowIndex);
      // stream.metricsOp();
      data.db.closeQuery(data.rs);
      batch.clear();
      return false;
    }
    Object[] keys = new Object[data.keyNrs.length];
    System.arraycopy(row, 0, keys, 0, keys.length);
    Comparable<?> key = data.keyNrs.length > 1 ? MixedKey.of(keys) : (Comparable<?>) keys[0];
    if (batch.contains(key)) {
      return true;
    }
    step.incrementLinesRead();

    IRowSet outputRowSet = null;
    boolean delete = false;
    if (!preview) {
      delete = data.hasDelete && count.incrementAndGet() % 3 == 0;
      outputRowSet = delete ? data.deleteRowSet : data.defaultRowSet;
    }

    //    Object[] keys = copyRow(row, data.keyNrs, data.allMatch);
    batch.add(key);
    OpLog<Comparable<?>> build = OpLog.build(delete ? Op.DELETE : Op.UPDATE, key);
    // FIXME
    // eventBag.pushEvent(key, build);
    // stream.increment(rowIndex++, build.getOp(), -1, false);
    if (meta.isIgnoreDel() && delete) {
      fastCloseEvent(key, FinishType.SKIP, "IGNORE-DEL");
      return true;
    }
    row[0] = Util.joinOpLogKey(data.keyMeta, keys, meta.isLegacyJoinType());
    System.arraycopy(keys, 0, row, 1, keys.length);
    putRow(data.outputMeta, outputRowSet, row);
    if (feedback.apply(step.getLinesRead())) {
      step.logRowlevel(
          "{0} flow data {1}", delete ? "delete" : "default", data.outputMeta.getString(row));
    }

    return true;
  }

  private void fastCloseEvent(Comparable<?> key, FinishType type, String code) {
    // FIXME
    // eventBag.getResultMap().put(key, Util.transMessage(key, step.getTransformName(), true, type,
    // code));
    step.incrementLinesRejected();
  }

  @Override
  protected boolean isCompatibleKey() {
    return true;
  }
}
