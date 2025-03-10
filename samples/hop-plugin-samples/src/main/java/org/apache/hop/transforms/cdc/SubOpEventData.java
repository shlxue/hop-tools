package org.apache.hop.transforms.cdc;

import org.apache.hop.core.IRowSet;
import org.apache.hop.core.database.Database;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.pipeline.transform.BaseTransformData;
import org.apache.hop.transforms.cdc.domain.OpLog;

import java.sql.PreparedStatement;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SubOpEventData extends BaseTransformData {

  Database db;

  boolean active;
  //  boolean hasActiveTable;
  String subTableName;
  boolean ignoreDel;
  boolean needConvertKey;
  BiFunction<OpLog<?>, IRowMeta, OpLog<?>> mixedGetter;
  Function<Object[], Comparable<?>> masterKeyGetter;
  RowMeta keyRowMeta = new RowMeta();
  RowMeta masterKeyMeta = new RowMeta();
  PreparedStatement query;
  int limit;

  int arraySize;
  //  int[] indexKeys;
  //  int[] indexMasterKeys;
  RowMeta outMeta = new RowMeta();
  RowMeta noneMeta = new RowMeta();
  RowMeta delMeta = new RowMeta();

  String delStep;
  //  int rawKeyIndex;
  int opIndex;
  int keyCacheIndex;
  int tempMasterKeysIndex;
  int[] detailIndex;
  int[] masterKeyIndex;

  IRowSet outputRowSet;
  IRowSet notKeyRowSet;
  IRowSet deleteRowSet;
  //  List<IRowSet> delRowSets;
}
