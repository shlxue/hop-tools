package org.apache.hop.transforms.cdc;

// import com.opennews.pdi.domain.RowEventBag;
import lombok.Setter;
import org.apache.hop.core.IRowSet;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;

import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

import static java.util.Objects.isNull;

abstract class BaseDelegate {
  protected final IVariables variables;
  protected final OpEventInput step;
  protected final OpEventInputMeta meta;
  protected final OpEventInputData data;
  @Setter protected boolean preview;
  protected LongFunction<Boolean> feedback;
  // RowEventBag eventBag;

  BaseDelegate(IVariables variables, OpEventInput step) {
    this.variables = variables;
    this.step = step;
    this.meta = step.getMeta();
    this.data = step.getData();
    step.logDetailed(i18n("MSG.ApplyRowEvent", getClass().getSimpleName()));
  }

  protected boolean init() {
    if (step.isRowLevel()) {
      step.logRowlevel("Output row log columns {0}", meta.getKeyFields());
    }
    return true;
  }

  @SuppressWarnings("squid:S1172")
  protected boolean beforeProcess(List<IRowSet> inputRowSets) throws HopException {
    if (preview) {
      return true;
    }
    boolean pass = true;
    final String key = "OEI.Error.NotFoundNextStep";
    try {
      data.hasDelete = !StringUtil.isEmpty(meta.getDeleteStepName());
      Optional<IRowSet> optionalRowSet;
      if (Utils.isEmpty(meta.getDeleteStepName())) {
        optionalRowSet = step.getOutputRowSets().stream().findFirst();
      } else {
        data.deleteRowSet = step.findOutputRowSet(meta.getDeleteStepName());
        pass = step.check(isNull(data.deleteRowSet), step::logError, key, meta.getDeleteStepName());
        optionalRowSet = findDefaultTargetStep(step.getOutputRowSets(), meta.getDeleteStepName());
      }
      step.check(!optionalRowSet.isPresent(), step::logMinimal, key, meta.getDeleteStepName());
      optionalRowSet.ifPresent(rowSet -> data.defaultRowSet = rowSet);
    } catch (HopTransformException e) {
      step.logError(i18n(key, e.getMessage()));
      pass = false;
    }
    return pass;
  }

  private Optional<IRowSet> findDefaultTargetStep(List<IRowSet> rowSets, String deleteStepName) {
    return rowSets.stream()
        .filter(rowSet -> !deleteStepName.equals(rowSet.getDestinationTransformName()))
        .findFirst();
  }

  protected abstract boolean processRow() throws HopException;

  protected boolean initKeyIndex(IRowMeta inputRowMeta, OpEventInputData data) {
    data.keyNrs = new int[data.keyMeta.size()];
    data.allMatch = true;
    for (int i = 0; i < data.keyMeta.size(); i++) {
      IValueMeta outputValueMeta = data.keyMeta.getValueMeta(i);
      int nr = inputRowMeta.indexOfValue(outputValueMeta.getName());
      if (nr == -1) {
        step.logError(i18n("OEI.NotFoundKeyField", outputValueMeta));
        return false;
      }
      IValueMeta valueMeta = inputRowMeta.getValueMeta(nr);
      if (outputValueMeta.getType() != valueMeta.getType()) {
        step.logError(i18n("OEI.KeyFieldTypeConflict", outputValueMeta, valueMeta.getTypeDesc()));
        return false;
      }
      data.allMatch &= nr == i;
      data.keyNrs[i] = nr;
    }
    return true;
  }

  protected abstract boolean isCompatibleKey();

  final Object[] copyRow(Object[] row, int[] keyNrs, boolean allMatch) {
    if (allMatch) {
      return row;
    }
    Object[] cloneRow = new Object[keyNrs.length];
    for (int i = 0; i < keyNrs.length; i++) {
      cloneRow[i] = row[keyNrs[i]];
    }
    return cloneRow;
  }

  void putRow(IRowMeta meta, IRowSet outputRowSet, Object[] data) throws HopTransformException {
    if (outputRowSet != null) {
      step.putRowTo(meta, data, outputRowSet);
    } else {
      step.putRow(meta, data);
    }
  }

  protected String i18n(String key, Object... params) {
    return Util.i18n(OpEventInputMeta.class, key, params);
  }
}
