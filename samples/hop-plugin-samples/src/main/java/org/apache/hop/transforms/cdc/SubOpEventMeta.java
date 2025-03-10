package org.apache.hop.transforms.cdc;

import lombok.Getter;
import lombok.Setter;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.Const;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.parameters.INamedParameters;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.xml.XmlFormatter;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.PipelineHopMeta;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.*;
import org.apache.hop.pipeline.transform.stream.IStream;
import org.apache.hop.pipeline.transform.stream.IStream.StreamType;
import org.apache.hop.pipeline.transform.stream.Stream;
import org.apache.hop.pipeline.transform.stream.StreamIcon;
import org.apache.hop.transforms.cdc.jdbc.Tab;
import org.w3c.dom.Node;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Transform(
    id = "SubOpEvent",
    name = "Step.SubOpEvent.Name",
    description = "Step.SubOpEvent.Description",
    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseTransform.Category.Input",
    image = "ui/images/SOE.svg",
    keywords = "i18n:SbuOpEventMeta.keywords",
    documentationUrl = "/pipeline/transform/sub-op-event.html")
public class SubOpEventMeta extends BaseTransformMeta<SubOpEvent, SubOpEventData> {

  static String i18n(String key, Object... params) {
    return Util.i18n(SubOpEventMeta.class, key, params);
  }

  static String delStepMetaType = "org.pentaho.di.trans.steps.delete.DeleteMeta";
  static Method getTableNameMethod;

  @Getter private Tab<IValueMeta> master = Tab.build(null, null, IValueMeta::getName);

  @Getter private List<Relationship> details = new ArrayList<>();
  @Getter @Setter private String connection;
  @Getter @Setter private int rowLimit = 100;
  @Getter @Setter private String noneStep;
  @Getter @Setter private boolean ignoreNonMap;
  @Getter @Setter private boolean ignoreAllDelOp;
  boolean showAllField;

  String getOutStep() {
    TransformMeta stepMeta = getDefaultStep().orElse(null);
    return stepMeta != null ? stepMeta.getName() : null;
  }

  public Optional<TransformMeta> getDefaultStep() {
    TransformMeta stepMeta = getParentTransformMeta();
    if (stepMeta != null) {
      List<String> nextSteps =
          new ArrayList<>(
              Arrays.asList(stepMeta.getParentPipelineMeta().getNextTransformNames(stepMeta)));
      nextSteps.remove(noneStep);
      for (Relationship item : getDetails()) {
        if (item.getDelStep() != null) {
          nextSteps.remove(item.getDelStep());
        }
      }
      if (!nextSteps.isEmpty()) {
        return Optional.of(stepMeta.getParentPipelineMeta().findTransform(nextSteps.get(0)));
      }
    }
    return Optional.empty();
  }

  public String getSchema() {
    return master.getSchema();
  }

  public void setSchema(String schema) {
    master.setSchema(schema);
  }

  public String getTable() {
    return master.getName();
  }

  public void setTable(String table) {
    master.setName(table);
  }

  public Collection<IValueMeta> getKeyFields() {
    return master.getPkFields();
  }

  public void setKeyFields(Collection<IValueMeta> valueMetaList) {
    master.setPkFieldNames(getFieldNames(valueMetaList));
  }

  public String getKeyNames() {
    return master.getPkFieldNames();
  }

  void setDetails(List<Relationship> details) {
    this.details.clear();
    this.details.addAll(details);
  }

  Relationship getRelation(String table) {
    return details.stream()
        .filter(relation -> table.equalsIgnoreCase(relation.getDetail().getName()))
        .findFirst()
        .orElse(null);
  }

  public RowMeta getPkMeta(String subTable) {
    RowMeta rowMeta = new RowMeta();
    Relationship relationship = getRelation(subTable);
    if (relationship != null) {
      relationship.getDetail().getPkFields().forEach(rowMeta::addValueMeta);
    }
    return rowMeta;
  }

  private List<String> getStreamNames() {
    List<String> list = new ArrayList<>();
    list.add(noneStep);
    getDetails().forEach(relationship -> list.add(relationship.getDelStep()));
    return list;
  }

  @Override
  public void setDefault() {
    connection = null;
    master.setSchema(null);
    master.setName(null);
    master.setFields(Collections.emptyList());
    details.clear();
    rowLimit = getRowLimit();
  }

  @Override
  public Object clone() {
    SubOpEventMeta meta = (SubOpEventMeta) super.clone();
    meta.master = master.clone();
    meta.details = details.stream().map(Relationship::clone).collect(Collectors.toList());
    meta.details.forEach(relationship -> relationship.setMaster(meta.master));
    return meta;
  }

  private boolean noKeyStream(TransformMeta next) {
    for (IStream s : getTransformIOMeta().getTargetStreams()) {
      if (next.equals(s.getTransformMeta())) {}
    }
    return false;
  }

  private RowMeta buildKyeFields(Relationship item) {
    RowMeta rowMeta = new RowMeta();
    Collection<IValueMeta> keyFields = item.getDetail().getPkFields();
    keyFields.forEach(rowMeta::addValueMeta);
    //    rowMeta.addValueMeta(getRawField(rowMeta.getValueMetaList()));
    return rowMeta;
  }

  @Override
  public void getFields(
      IRowMeta inputRowMeta,
      String name,
      IRowMeta[] info,
      TransformMeta nextStep,
      IVariables space,
      IHopMetadataProvider metaStore) {
    logRowlevel(i18n("MSG.GetFields", name, nextStep));
    inputRowMeta.addValueMeta(Util.KEY_VAL);
    StreamIcon streamType = StreamIcon.TARGET;
    if (nextStep != null) {
      for (IStream stream : getTransformIOMeta().getTargetStreams()) {
        if (nextStep.equals(stream.getTransformMeta())) {
          streamType = stream.getStreamIcon();
          break;
        }
      }
    }
    Predicate<String> match = s -> nextStep.getName().equalsIgnoreCase(s);
    switch (streamType) {
      case FALSE:
        for (Relationship item : getDetails()) {
          if (match.test(item.getDelStep())) {
            inputRowMeta.addRowMeta(buildKyeFields(item));
            return;
          }
        }
        break;
      case INFO:
        Relationship item = null;
        try {
          INamedParameters params;
          if (space instanceof ITransform) {
            params = ((ITransform) space).getPipeline();
          } else if (space instanceof INamedParameters) {
            params = (INamedParameters) space;
          } else {
            return;
          }
          String table = params.getParameterValue("subTableName");
          if (StringUtil.isEmpty(table)) {
            table = params.getParameterDefault("subTableName");
          }
          if (!StringUtil.isEmpty(table)) {
            item = getRelation(table);
          }
        } catch (HopException ignore) {
        }
        if (item != null) {
          inputRowMeta.addRowMeta(buildKyeFields(item));
        }
        break;
      case TARGET:
        master.getPkFields().forEach(inputRowMeta::addValueMeta);
        break;
      default:
        throw new IllegalStateException("Don't support stream type on SubOpEvent: " + streamType);
    }
    if (nextStep != null) {
      //      if ("Dummy".equals(nextStep.getTypeId()) && ((ITransform)
      // space).getPipeline().isPreview()) {}
    }
  }

  IValueMeta getRawField(List<IValueMeta> fields) {
    IValueMeta rawField = new ValueMetaString();
    if (fields.size() == 1) {
      rawField = fields.get(fields.size() - 1).clone();
    }
    rawField.setName("_key");
    return rawField;
  }

  private boolean checkTab(
      List<ICheckResult> remarks, Tab<IValueMeta> tab, TransformMeta stepMeta) {
    if (StringUtil.isEmpty(tab.getName())) {
      remarks.add(
          new CheckResult(ICheckResult.TYPE_RESULT_ERROR, i18n("MSG_EmptyTableName"), stepMeta));
    } else if (tab.getPkFields().isEmpty()) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR,
              i18n("MSG.Check.MissingPk", tab.getName()),
              stepMeta));
    } else if (tab.getPK().isEmpty() || !tab.getPK().get().isUnique()) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_WARNING, i18n("MSG.Check.Index", tab.getName()), stepMeta));
    } else {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_COMMENT, i18n("MSG.Check.Pk", tab.getName()), stepMeta));
      return true;
    }
    return false;
  }

  @Override
  public void check(
      List<ICheckResult> remarks,
      PipelineMeta pipelineMeta,
      TransformMeta stepMeta,
      IRowMeta prev,
      String[] input,
      String[] output,
      IRowMeta info,
      IVariables space,
      IHopMetadataProvider metaStore) {
    logRowlevel(i18n("MSG.CheckStep", stepMeta.getName()));
    if (StringUtil.isEmpty(connection)) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR, i18n("MSG.Check.EmptyConnection"), stepMeta));
    } else if (StringUtil.isEmpty(master.getName())
        || StringUtil.isEmpty(master.getPkFieldNames())) {
      remarks.add(new CheckResult(ICheckResult.TYPE_RESULT_ERROR, "InvalidMasterTable", stepMeta));
    }

    if (getDetails().isEmpty()) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR,
              i18n("MSG.Check.MissingConfig", master.getName()),
              stepMeta));
    }
    for (Relationship item : getDetails()) {
      checkTab(remarks, item.getDetail(), stepMeta);
      if (StringUtil.isEmpty(item.getJoinSql())) {
        remarks.add(
            new CheckResult(
                ICheckResult.TYPE_RESULT_ERROR,
                i18n("MSG.Check.JoinCondition", item.getDetail().getName()),
                stepMeta));
      }
    }

    List<IStream> streams = getTransformIOMeta().getTargetStreams();
    IStream[] defaultStreams = filter(streams, StreamIcon.TARGET);
    IStream[] nonExistStreams = filter(streams, StreamIcon.INFO);
    IStream[] deleteStreams = filter(streams, StreamIcon.FALSE);
    if (streams.size() > defaultStreams.length + nonExistStreams.length + deleteStreams.length) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_WARNING, "MSG.Check.CleanInvalidFlow", stepMeta));
    }
    if (defaultStreams.length != 1 || defaultStreams[0].getTransformMeta() == null) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR, i18n("MSG.Check.MissingDefaultStream"), stepMeta));
    }
    boolean exist = !StringUtil.isEmpty(noneStep);
    if (isIgnoreNonMap() && exist) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_WARNING,
              i18n("MSG.Check.Removed", nonExistStreams[0].getTransformMeta()),
              stepMeta));
    } else if (!isIgnoreNonMap() && !exist) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR, i18n("MSG.Check.MissingNotFoundStream"), stepMeta));
    }
    for (Relationship item : getDetails()) {
      boolean ignore = isIgnoreAllDelOp() || item.isIgnoreDel();
      exist = !StringUtil.isEmpty(item.getDelStep());
      String name = item.getDetail().getName();
      if (ignore && exist) {
        remarks.add(
            new CheckResult(
                ICheckResult.TYPE_RESULT_WARNING,
                i18n("MSG.Check.RemoveIgnoreStream", name),
                stepMeta));
      } else if (!ignore && !exist) {
        remarks.add(
            new CheckResult(
                ICheckResult.TYPE_RESULT_ERROR,
                i18n("MSG.Check.MissingDeleteStream", name),
                stepMeta));
      }
    }
  }

  private IStream[] filter(List<IStream> streams, StreamIcon type) {
    return streams.stream().filter(si -> type == si.getStreamIcon()).toArray(IStream[]::new);
  }

  private void setStream(IStream stream, TransformMeta stepMeta) {
    stream.setTransformMeta(stepMeta);
    if (stream.getSubject() != null) {
      TransformMeta transformMeta =
          getParentTransformMeta().getParentPipelineMeta().findTransform(stream.getSubject());
      stepMeta
          .getParentPipelineMeta()
          .removePipelineHop(new PipelineHopMeta(getParentTransformMeta(), transformMeta));
    }
    stream.setSubject(stepMeta.getName());
  }

  private Optional<IStream> findDeleteStream(ITransformIOMeta ioMeta, String name) {
    if (!StringUtil.isEmpty(name)) {
      for (IStream stream : ioMeta.getTargetStreams()) {
        if (stream.getStreamIcon() == StreamIcon.FALSE && name.equals(stream.getSubject())) {
          return Optional.of(stream);
        }
      }
    }
    return Optional.empty();
  }

  @Override
  public void setChanged(boolean ch) {
    PipelineMeta pipelineMeta = getParentTransformMeta().getParentPipelineMeta();
    if (ch) {
      if (pipelineMeta != null) {
        //        ITransformIOMeta ioMeta = getTransformIOMeta();
        //        boolean needRebuild = false;

        //        for (Relationship item : getDetails()) {
        //          Optional<IStream> stream = findDeleteStream(ioMeta, item.getDelStep());
        //          if (item.isIgnoreDel() && stream.isPresent()) {
        //            // delete hop
        //            pipelineMeta.removePipelineHop(new PipelineHopMeta(getParentTransformMeta(),
        //            pipelineMeta.findTransform(item.getDelStep())));
        //            stream.get().setStreamIcon(null);
        //            item.setDelStep(null);
        //            needRebuild = true;
        //          } else if (!item.isIgnoreDel() && !stream.isPresent()) {
        //            // add stream
        //            String table = safeTableName(item.getDetail().getName());
        //            ioMeta.addStream(newStream(i18n("SOE.Stream.Delete", table), StreamIcon.FALSE,
        // null));
        //            needRebuild = true;
        //          }
        //        }
        //        logBasic("Change status: %s", needRebuild);
        //        if (needRebuild) {
        //          super.resetStepIoMeta();
        //        }
      }
    } else {
      List<IStream> targetStreams = getTransformIOMeta().getTargetStreams();
      applyDefaultStream(targetStreams.get(0), getDefaultStep().orElse(null));
      if (!StringUtil.isEmpty(noneStep)) {
        applyDefaultStream(targetStreams.get(1), pipelineMeta.findTransform(noneStep));
      }
      for (int i = 0; i < getDetails().size(); i++) {
        TransformMeta stepMeta = pipelineMeta.findTransform(getDetails().get(i).getDelStep());
        if (stepMeta != null) {
          targetStreams.get(i + 2).setTransformMeta(stepMeta);
        }
      }
    }
    super.setChanged(ch);
  }

  private void applyDefaultStream(IStream stream, TransformMeta stepMeta) {
    if (stepMeta != null) {
      if (!stepMeta.getName().equals(stream.getSubject())) {
        stream.setSubject(stepMeta.getName());
      }
      stream.setTransformMeta(stepMeta);
    }
  }

  @Override
  public void searchInfoAndTargetTransforms(List<TransformMeta> steps) {
    if (log.isRowLevel()) {
      logRowlevel(i18n("MSG.TransformMeta.SearchInfoAndTarget", steps.size(), getName()));
    }
    List<IStream> streams = getTransformIOMeta().getTargetStreams();
    getDefaultStep().ifPresent(stepMeta -> streams.get(0).setTransformMeta(stepMeta));
    if (!StringUtil.isEmpty(noneStep)) {
      Optional.ofNullable(TransformMeta.findTransform(steps, noneStep))
          .ifPresent(stepMeta -> streams.get(1).setTransformMeta(stepMeta));
    }
    for (int i = 2; i < streams.size(); i++) {
      TransformMeta stepMeta =
          TransformMeta.findTransform(steps, (String) streams.get(i).getSubject());
      if (stepMeta != null) {
        streams.get(i).setTransformMeta(stepMeta);
      }
    }
    resetTransformIoMeta();
  }

  @Override
  public void resetTransformIoMeta() {
    if (log.isRowLevel()) {
      logRowlevel("Disable reset step io meta");
    }
  }

  @Override
  public String getName() {
    return getParentTransformMeta() != null ? getParentTransformMeta().getName() : super.getName();
  }

  private IStream newStream(String text, StreamIcon icon, String subject) {
    return new Stream(StreamType.TARGET, null, text, icon, subject);
  }

  private String safeTableName(String table) {
    return StringUtil.isEmpty(table) ? "-" : table;
  }

  @Override
  public ITransformIOMeta getTransformIOMeta() {
    ITransformIOMeta ioMeta = super.getTransformIOMeta(false);
    if (ioMeta == null) {
      ioMeta = new TransformIOMeta(false, false, true, false, false, true);
      if (StringUtil.isEmpty(master.getName())) {
        initMasterIfExist(master);
      }
      String table = safeTableName(master.getName());
      ioMeta.addStream(newStream(i18n("SOE.Stream.Default", table), StreamIcon.TARGET, null));

      ioMeta.addStream(newStream(i18n("SOE.Stream.NoKey"), StreamIcon.INFO, noneStep));

      for (Relationship item : getDetails()) {
        ioMeta.addStream(
            newStream(
                i18n("SOE.Stream.Delete", safeTableName(item.getDetail().getName())),
                StreamIcon.FALSE,
                item.getDelStep()));
      }
      setTransformIOMeta(ioMeta);
      logDetailed(i18n("MSG.Step.InitIoMetaStream", ioMeta.getTargetStreams().size(), getName()));
    }
    return ioMeta;
  }

  void initMasterIfExist(Tab<IValueMeta> tab) {
    for (TransformMeta stepMeta :
        getParentTransformMeta().getParentPipelineMeta().getTransforms()) {
      if ("OpEventInput".equals(stepMeta.getTypeId())) {
        OpEventInputMeta refMeta = (OpEventInputMeta) stepMeta.getTransform();
        if (StringUtil.isEmpty(connection) && !StringUtil.isEmpty(refMeta.getConnection())) {
          connection = refMeta.getConnection();
        }
        if (!StringUtil.isEmpty(refMeta.getTable())) {
          tab.refresh(refMeta.getTabModel(), true);
        }
        break;
      }
    }
  }

  @Override
  public void handleStreamSelection(IStream stream) {
    logRowlevel(i18n("MSG.Stream.Handle", stream.getTransformName()));
    TransformMeta selfMeta = getParentTransformMeta();
    PipelineHopMeta exist = null;
    if (stream.getSubject() != null) {
      for (PipelineHopMeta hopMeta : selfMeta.getParentPipelineMeta().getPipelineHops()) {
        if (hopMeta.getFromTransform().equals(getParentTransformMeta())
            && hopMeta.getToTransform().getName().equals(stream.getSubject())) {
          exist = hopMeta;
          break;
        }
      }
      if (exist != null) {
        selfMeta.getParentPipelineMeta().removePipelineHop(exist);
      }
    }
    if (stream.getStreamIcon() == StreamIcon.INFO) {
      setNoneStep(stream.getTransformName());
    } else if (stream.getStreamIcon() == StreamIcon.FALSE) {
      for (Relationship item : getDetails()) {
        if (exist != null && exist.getToTransform().getName().equals(item.getDelStep())
            || stream.getDescription().endsWith(" [" + item.getDetail().getName() + "]")) {
          item.setDelStep(stream.getTransformName());
          break;
        }
      }
    }
    stream.setSubject(stream.getTransformMeta().getName());
    //    if (stream.getStreamIcon() == StreamIcon.ERROR) {
    //    } else if (stream.getStreamIcon() == StreamIcon.TRUE) {
    //      List<String> list = (List<String>) stream.getSubject();
    //      for (Relationship item : getDetails()) {
    //        if (item.isIgnoreDel()) {
    //          item.setDeleteFlow(stream.getTransformMeta());
    //          list.add(item.getDetail().getName());
    //        }
    //      }
    //    } else if (stream.getStreamIcon() == StreamIcon.ERROR) {
    //      for (Relationship item : getDetails()) {
    //        item.setIgnoreDel(false);
    //        item.setDeleteFlow(stream.getTransformMeta());
    //      }
    //    }
    resetTransformIoMeta();
  }

  @Override
  public boolean cleanAfterHopFromRemove(TransformMeta toStep) {
    logRowlevel(i18n("MSG.Stream.Clean", toStep.getName()));
    boolean change = false;
    for (IStream stream : getTransformIOMeta().getTargetStreams()) {
      if (toStep.equals(stream.getTransformMeta())) {
        stream.setTransformMeta(null);
        change = true;
        break;
      }
    }
    if (change) {
      if (toStep.getName().equals(noneStep)) {
        noneStep = null;
      } else {
        for (Relationship item : getDetails()) {
          if (toStep.getName().equals(item.getDelStep())) {
            item.setDelStep(null);
            break;
          }
        }
      }
    }
    return change;
  }

  private String detectDeleteTable(TransformMeta stepMeta) {
    Optional<TransformMeta> optional = detectDeleteStep(stepMeta);
    if (optional.isPresent()) {
      try {
        if (getTableNameMethod == null) {
          getTableNameMethod = optional.get().getTransform().getClass().getMethod("getTableName");
        }
        return (String) getTableNameMethod.invoke(optional.get().getTransform());
      } catch (Throwable ignore) {
      }
    }
    return "";
  }

  private Optional<TransformMeta> detectDeleteStep(TransformMeta stepMeta) {
    if (delStepMetaType.equals(stepMeta.getTransform().getClass().getCanonicalName())) {
      return Optional.of(stepMeta);
    }
    PipelineMeta pipelineMeta = stepMeta.getParentPipelineMeta();
    for (TransformMeta sm : pipelineMeta.findNextTransforms(stepMeta)) {
      Optional<TransformMeta> optional = detectDeleteStep(sm);
      if (optional.isPresent()) {
        return optional;
      }
    }
    return Optional.empty();
  }

  @Override
  public boolean excludeFromCopyDistributeVerification() {
    return true;
  }

  private void writeTable(StringBuilder sb, Tab<IValueMeta> table) {
    // TODO attrs
    XmlHandler.openTag(sb, Util.TAG_TABLE); // Util.tableAttrs(table);
    Util.forEachWithAttr(sb, table.getIndexFields(), Util.TAG_FIELD, Util::getFieldAttrs);
    Util.forEachWithAttr(sb, table.getIndexes(), Util.TAG_INDEX, Util::getIndexAttrs);
    XmlHandler.closeTag(sb, Util.TAG_TABLE);
  }

  private void writeRelation(StringBuilder sb, Relationship ref) {
    Tab<IValueMeta> tab = ref.getDetail();
    Map<String, String> attrs = new LinkedHashMap<>(Map.of("name", tab.getName()));
    if (ref.getLimit() > 1) {
      attrs.put("limit", Integer.toString(ref.getLimit()));
    }
    if (ref.isIgnoreDel()) {
      attrs.put("ignoreDel", "Y");
    }
    if (!StringUtil.isEmpty(ref.getDelStep()) && isNextStep(ref.getDelStep())) {
      attrs.put("delStep", ref.getDelStep());
    }
    // TODO attrs
    // XmlHandler.openTag(sb, Util.TAG_TABLE, attrs);
    String xml = ref.getJoinSql();
    sb.append(XmlHandler.addTagValue("keyFields", tab.getPkFieldNames()));
    sb.append(XmlHandler.addTagValue("join", xml));
    if (!StringUtil.isEmpty(ref.getConditionSql())) {
      xml = ref.getConditionSql();
      sb.append(XmlHandler.addTagValue("condition", xml));
    }
    XmlHandler.closeTag(sb, Util.TAG_TABLE);
  }

  @Override
  @Deprecated
  public String getXml() {
    logRowlevel(i18n("TransformMeta.GetXml", getClass().getSimpleName()));
    StringBuilder sb =
        new StringBuilder(1024)
            .append(XmlHandler.addTagValue(Util.TAG_CONNECTION, getConnection()))
            .append(XmlHandler.addTagValue(Util.TAG_SCHEMA, getSchema()))
            .append(XmlHandler.addTagValue(Util.TAG_TABLE, getTable()))
            .append(XmlHandler.addTagValue(Util.TAG_KEY_FIELDS, getKeyNames()));
    if (ignoreNonMap) {
      sb.append(XmlHandler.addTagValue("ignoreNonMap", true));
    }
    if (ignoreAllDelOp) {
      sb.append(XmlHandler.addTagValue("ignoreAllDelOp", true));
    }
    XmlHandler.openTag(sb, Util.TAG_RELATIONS);
    Util.forEach(sb, details, relationship -> writeRelation(sb, relationship));
    XmlHandler.closeTag(sb, Util.TAG_RELATIONS);

    XmlHandler.openTag(sb, Util.TAG_TABLES);
    List<Tab<IValueMeta>> list = new ArrayList<>();
    list.add(master);
    details.forEach(relationship -> list.add(relationship.getDetail()));
    Util.forEach(sb, list, tab -> writeTable(sb, tab));
    XmlHandler.closeTag(sb, Util.TAG_TABLES);

    if (!isNextStep(noneStep)) {
      noneStep = null;
    }
    sb.append(XmlHandler.addTagValue("notKeyStep", noneStep))
        .append(XmlHandler.addTagValue("rowLimit", rowLimit));

    return XmlFormatter.format(sb.toString());
  }

  private boolean isNextStep(String stepName) {
    if (getParentTransformMeta() != null) {
      for (TransformMeta stepMeta :
          getParentTransformMeta()
              .getParentPipelineMeta()
              .findNextTransforms(getParentTransformMeta())) {
        if (stepMeta.getName().equals(stepName)) {
          return true;
        }
      }
    }
    return false;
  }

  private <T> List<T> forEachNodes(Collection<Node> items, Function<Node, T> getter) {
    List<T> list = new ArrayList<>(items.size());
    for (Node node : items) {
      list.add(getter.apply(node));
    }
    return list;
  }

  private IValueMeta loadField(Node node) {
    String name = XmlHandler.getTagAttribute(node, "name");
    int type = Const.toInt(XmlHandler.getTagAttribute(node, "type"), 0);
    return Util.newField(name, type);
  }

  @Override
  @Deprecated
  public void loadXml(Node node, IHopMetadataProvider metaStore) {
    logRowlevel(i18n("TransformMeta.LoadXml", getClass().getSimpleName()));
    Node subNode;
    this.connection = XmlHandler.getTagValue(node, "connection");
    if (StringUtil.isEmpty(XmlHandler.getTagValueWithAttribute(node, Util.TAG_TABLE, "name"))) {
      setSchema(XmlHandler.getTagValue(node, Util.TAG_SCHEMA));
      setTable(XmlHandler.getTagValue(node, Util.TAG_TABLE));
      List<Tab<IValueMeta>> list = new ArrayList<>();
      subNode = XmlHandler.getSubNode(node, Util.TAG_TABLES);
      XmlHandler.getNodes(subNode, Util.TAG_TABLE).forEach(tabNode -> list.add(loadTable(tabNode)));
      Map<String, Tab<IValueMeta>> map =
          list.stream().collect(Collectors.toMap(Tab::getName, Function.identity()));
      if (map.containsKey(master.getName())) {
        master.refresh(map.get(master.getName()), true);
        master.setPkFieldNames(XmlHandler.getTagValue(node, Util.TAG_KEY_FIELDS));
      }
      ignoreNonMap = "Y".equalsIgnoreCase(XmlHandler.getTagValue(node, "ignoreNonMap"));
      ignoreAllDelOp = "Y".equalsIgnoreCase(XmlHandler.getTagValue(node, "ignoreAllDelOp"));

      subNode = XmlHandler.getSubNode(node, Util.TAG_RELATIONS);
      for (Node n : XmlHandler.getNodes(subNode, Util.TAG_TABLE)) {
        String name = XmlHandler.getTagAttribute(n, "name");
        Relationship ref = new Relationship(master, map.get(name));
        ref.getDetail().setPkFieldNames(XmlHandler.getTagValue(n, Util.TAG_KEY_FIELDS));

        ref.setJoinSql(XmlHandler.getTagValue(n, "join"));
        ref.setConditionSql(XmlHandler.getTagValue(n, "condition"));
        ref.setDelStep(XmlHandler.getTagAttribute(n, "delStep"));
        ref.setLimit(Const.toInt(XmlHandler.getTagAttribute(n, "limit"), 1));
        ref.setLimit(Math.min(Math.max(ref.getLimit(), 1), 255));
        ref.setIgnoreDel("Y".equalsIgnoreCase(XmlHandler.getTagAttribute(n, "ignoreDel")));
        details.add(ref);
      }
      noneStep = XmlHandler.getTagValue(node, "notKeyStep");
    } else {
      loadLegacy(node);
    }
    rowLimit = Const.toInt(XmlHandler.getTagValue(node, "rowLimit"), 100);
  }

  private void loadLegacy(Node node) {
    master.refresh(loadTab(XmlHandler.getSubNode(node, Util.TAG_TABLE)), true);

    List<Relationship> list = new ArrayList<>();
    for (Node tabNode :
        XmlHandler.getNodes(XmlHandler.getSubNode(node, "relations"), Util.TAG_TABLE)) {
      Relationship ref = new Relationship(master, loadTab(tabNode));
      ref.setDelStep(XmlHandler.getTagAttribute(tabNode, "delStep"));
      ref.setJoinSql(XmlHandler.getTagValue(tabNode, "join"));
      ref.setLimit(1);
      list.add(ref);
    }
    noneStep = XmlHandler.getTagValue(node, "notExistStep");
    this.setDetails(list);
  }

  private Tab<IValueMeta> loadTable(Node node) {
    Tab<IValueMeta> tab =
        Tab.build(getSchema(), XmlHandler.getTagAttribute(node, "name"), IValueMeta::getName);
    List<IValueMeta> fields = new ArrayList<>();
    XmlHandler.getNodes(node, Util.TAG_FIELD).forEach(colNode -> fields.add(loadField(colNode)));
    tab.setFields(fields);

    XmlHandler.getNodes(node, Util.TAG_INDEX)
        .forEach(
            indNode -> {
              boolean unique = "Y".equalsIgnoreCase(XmlHandler.getTagAttribute(indNode, "unique"));
              String keyFields = XmlHandler.getTagAttribute(indNode, "fields");
              tab.addIndex(unique, keyFields);
              if ("Y".equalsIgnoreCase(XmlHandler.getTagAttribute(indNode, Util.TAG_PRIMARY_KEY))) {
                tab.setPrimaryKeyFields(keyFields);
              }
            });

    //    tab.setPkFieldNames(getFieldNames(fields));
    return tab;
  }

  private Tab<IValueMeta> loadTab(Node node) {
    Tab<IValueMeta> tab =
        Tab.build(
            XmlHandler.getTagAttribute(node, "schema"),
            XmlHandler.getTagAttribute(node, "name"),
            IValueMeta::getName);
    node = XmlHandler.getSubNode(node, "primaryKey");
    List<IValueMeta> fields = new ArrayList<>();
    XmlHandler.getNodes(node, "column").forEach(colNode -> fields.add(loadField(colNode)));
    tab.setFields(fields);
    String fieldNames = getFieldNames(fields);
    tab.addIndex(true, fieldNames);
    tab.setPkFieldNames(fieldNames);
    return tab;
  }

  private String getFieldNames(Collection<IValueMeta> list) {
    return list.stream().map(IValueMeta::getName).collect(Collectors.joining(","));
  }
}
