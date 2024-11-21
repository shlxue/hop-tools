package org.apache.hop.transforms.cdc;

// import com.opennews.pdi.domain.RowEventBag;
// import com.opennews.pdi.util.TransUtil;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.parameters.UnknownParamException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.*;

import java.util.Optional;
import java.util.function.Consumer;

public class OpEventInput extends BaseTransform<OpEventInputMeta, OpEventInputData> {

  private OpEventInputMeta meta;
  private OpEventInputData data;
  private BaseDelegate delegate;
  private boolean preview;

  public OpEventInput(
      TransformMeta transformMeta,
      OpEventInputMeta meta,
      OpEventInputData data,
      int copyNr,
      PipelineMeta pipelineMeta,
      Pipeline pipeline) {
    super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
  }

  static String i18n(String key, Object... params) {
    return Util.i18n(OpEventInputMeta.class, key, params);
  }

  private BaseDelegate convertDelegate() {
    try {
      Class<?> aClass =
          getClass()
              .getClassLoader()
              .loadClass("org.apache.hop.transforms.cdc.RowEventConvertDelegate");
      return (BaseDelegate) aClass.getConstructor(OpEventInput.class).newInstance(this);
    } catch (Exception e) {
      throw new IllegalStateException("Not support convert delegate", e);
    }
  }

  public boolean processRow() throws HopException {
    if (first) {
      if (data.hasSubTable.isPresent() && data.hasSubTable.get()) {
        logDetailed(i18n("MSG.DisableOpEvent", getTransformName()));
        setOutputDone();
        return false;
      }
      if (!getInputRowSets().isEmpty()) {
        delegate = convertDelegate();
      } else {
        delegate = new RowEventQueryDelegate(variables, this);
      }
      // FIXME
      // delegate.eventBag = TransUtil.getData(RowEventBag.class, getPipeline(), 30000);
      if (isDetailed()) {
        logDetailed(i18n("OEI.Init.ApplyDelegateClass", delegate.getClass().getName()));
      }

      if (data.outputMeta != null) {
        for (int i = 1; i < data.outputMeta.size(); i++) {
          data.keyMeta.addValueMeta(data.outputMeta.getValueMeta(i));
          Util.fixKeyFormat(data.keyMeta.getValueMeta(i - 1));
        }
      }
      boolean pass = delegate.init();
      if (!pass) {
        setOutputDone();
        return false;
      }

      // Only active one of SubOpEvent and OpEvent in the transformation
      if (preview || !(data.hasSubTable.isPresent() && data.hasSubTable.get())) {
        pass = delegate.beforeProcess(getInputRowSets());
      }
      if (!pass) {
        setOutputDone();
        return false;
      }
      delegate.feedback = this::checkFeedback;
      if (data.outputMeta != null) {
        for (int i = 1; i < data.outputMeta.size(); i++) {
          data.outputMeta.getValueMeta(i).setConversionMetadata(null);
        }
      }
      first = false;
      logDetailed("Column info {0}", data.outputMeta);
    }
    if (delegate.processRow()) {
      return true;
    }
    setOutputDone();
    return false;
  }

  public boolean init() {
    boolean pass = super.init();
    preview = getPipeline().isPreview();
    if (pass) {
      if (StringUtil.isEmpty(meta.getTable())) {
        logError(i18n("OEI.Init.TableNameMissing", getTransformName()));
        return false;
      }
      if (meta.getKeyFields().isEmpty()) {
        logError(i18n("OEI.Init.KeyFieldsMissing", meta.getTable()));
        return false;
      }

      if (meta.isIgnoreDel()) {
        if (!StringUtil.isEmpty(meta.getDeleteStepName())) {
          logMinimal("Ignore del step", meta.getDeleteStepName());
        }
      } else {
        if (!StringUtil.isEmpty(meta.getDeleteStepName())) {
          try {
            data.deleteRowSet = findOutputRowSet(meta.getDeleteStepName());
          } catch (HopException ex) {
            throw new IllegalStateException("Found del step", ex);
          }
        }
        if (data.deleteRowSet == null && !getPipeline().isPreparing()) {
          logMinimal(i18n("MSG.MissingFlow4DeleteEvent"));
          return false;
        }
      }

      data.hasSubTable = checkSubTableStep();
      data.keyMeta = new RowMeta();
      data.outputMeta = new RowMeta();
      meta.getFields(data.outputMeta, getTransformName(), new IRowMeta[0], null, null, null);
      //      meta.getKeyFields().forEach(data.outputMeta::addValueMeta);
      getPipeline().getExtensionDataMap().put("rowEventsMeta", data.outputMeta);
      return true;
    }
    return false;
  }

  boolean check(boolean condition, Consumer<String> log, String key, Object... params) {
    if (condition) {
      log.accept(i18n(key, params));
      return false;
    }
    return true;
  }

  public void dispose() {
    if (data.db != null) {
      data.db.disconnect();
    }
    super.dispose();
  }

  private boolean hasInputProducer() {
    return getPipeline().getExtensionDataMap().containsKey("events");
  }

  private Optional<Boolean> checkSubTableStep() {
    String subTableStepId = "SubOpEvent";
    Optional<TransformMeta> refStep =
        getPipelineMeta().getTransforms().stream()
            .filter(stepMeta -> subTableStepId.equals(stepMeta.getTypeId()))
            .findFirst();
    if (refStep.isPresent()) {
      try {
        return Optional.of(!StringUtil.isEmpty(getPipeline().getParameterValue("subTableName")));
      } catch (UnknownParamException ignore) {
      }
    }
    return Optional.empty();
  }
}
