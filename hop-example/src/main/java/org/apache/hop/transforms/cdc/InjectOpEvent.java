package org.apache.hop.transforms.cdc;

// import com.opennews.pdi.domain.Features;
// import com.opennews.pdi.domain.Results;
// import com.opennews.pdi.util.TransUtil;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.ValueMetaSerializable;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.RowProducer;
import org.apache.hop.pipeline.transform.ITransform;

final class InjectOpEvent {
  static final IValueMeta STR_VALUE_META = new ValueMetaString();

  private InjectOpEvent() {}

  static int inject(
      Pipeline trans,
      DatabaseMeta databaseMeta,
      ITransform step,
      String sql,
      RowMeta keyMeta,
      int limit) {
    RowMeta rowEvent = new RowMeta();
    rowEvent.addValueMeta(new ValueMetaSerializable("row"));
    RowProducer rowProducer = null;

    //    Results.Stream stream = TransUtil.getData(Results.RsData.class, trans,
    // Features.BAG_RS_DATA, 7000).stream();
    //    try (Database db = new Database(trans, databaseMeta)) {
    //      db.setQueryLimit(limit);
    //      db.connect();
    //      step.getLogChannel().logDetailed(sql);
    //      rowProducer = trans.addRowProducer(step.getTransformName(), step.getCopy());
    //      List<Object[]> rows = db.getRows(sql, Math.min(limit,
    // trans.getPipelineMeta().getSizeRowset()));
    //      stream.initMetrics(rows.size());
    //      BiFunction<RowMeta, Object[], Comparable<?>> keyGetter = InjectOpEvent::toKey;
    //      if (keyMeta.size() > 1) {
    //        keyGetter = InjectOpEvent::toMixedKey;
    //      }
    //      int i = 1;
    //      Set<Comparable<?>> keys = new HashSet<>(rows.size());
    //      for (Object[] row : rows) {
    //        Op op = i % 3 == 0 ? Op.DELETE : Op.UPDATE;
    //        Comparable<?> key = keyGetter.apply(keyMeta, row);
    //        keys.add(key);
    //        stream.increment(i, op, -1, false);
    //        rowProducer.putRow(rowEvent, new Object[] {OpLog.build(op, key)});
    //        i++;
    //      }
    //      stream.getCount().setKey(keys.size());
    //      return i - 1;
    //    } catch (HopException e) {
    //      throw new IllegalStateException(e);
    //    } finally {
    //      if (rowProducer != null) {
    //        stream.metricsOp();
    //        rowProducer.finished();
    //      }
    //    }
    return 0;
  }

  private static Comparable<?> toKey(RowMeta rowMeta, Object[] row) {
    return (Comparable<?>) row[0];
  }

  private static Comparable<?> toMixedKey(RowMeta rowMeta, Object[] row) {
    String[] data = new String[rowMeta.size()];
    for (int i = 0; i < rowMeta.size(); i++) {
      IValueMeta valueMeta = rowMeta.getValueMeta(i);
      int type = valueMeta.getType();
      Object value = row[i];
      if (value == null) {
        data[i] = "";
        continue;
      }
      try {
        switch (type) {
          case IValueMeta.TYPE_DATE:
          case IValueMeta.TYPE_TIMESTAMP:
            value = Long.toString(valueMeta.getDate(value).getTime());
            break;
          case IValueMeta.TYPE_STRING:
            break;
          default:
            value = valueMeta.convertData(STR_VALUE_META, value);
        }
      } catch (HopException ignore) {
        value = value.toString();
      }
      data[i] = (String) value;
    }
    return String.join(",", data);
  }
}
