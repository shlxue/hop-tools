package org.apache.hop.transforms.cdc;

import org.apache.hop.core.IRowSet;
import org.apache.hop.core.database.Database;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.pipeline.transform.BaseTransformData;

import java.sql.ResultSet;
import java.util.Optional;

public class OpEventInputData extends BaseTransformData {

  IRowSet inputRowSet;

  IValueMeta[] keyMetas;
  IRowMeta keyMeta;
  IRowMeta outputMeta;
  IRowSet defaultRowSet;
  IRowSet deleteRowSet;

  ResultSet rs;
  Database db;

  Optional<Boolean> hasSubTable;
  boolean hasDelete;
  boolean allMatch;
  int[] keyNrs;
}
