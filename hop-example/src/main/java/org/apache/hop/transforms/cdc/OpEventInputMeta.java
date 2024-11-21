package org.apache.hop.transforms.cdc;

import com.opennews.hop.jdbc.Index;
import com.opennews.hop.jdbc.Tab;
import lombok.Getter;
import lombok.Setter;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.Const;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopPluginException;
import org.apache.hop.core.exception.HopXmlException;
import org.apache.hop.core.injection.InjectionSupported;
import org.apache.hop.core.plugins.ParentFirst;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.xml.XmlFormatter;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformIOMeta;
import org.apache.hop.pipeline.transform.TransformIOMeta;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transform.stream.IStream;
import org.apache.hop.pipeline.transform.stream.IStream.StreamType;
import org.apache.hop.pipeline.transform.stream.Stream;
import org.apache.hop.pipeline.transform.stream.StreamIcon;
import org.w3c.dom.Node;

import java.sql.DatabaseMetaData;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.*;

@ParentFirst(
    patterns = {
      "^com.opennews.domain.OpLog$",
      "^com.opennews.domain.Op$",
      "^com.opennews.domain.MixedKey$",
      "^com.opennews.domain.trans.TransMessage$"
    })
@Transform(
    id = "OpEventInput",
    name = "OpEventInput.Title",
    description = "OEI.Description",
    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseTransform.Category.Input",
    image = "ui/images/OEI.svg",
    keywords = "i18n:OpEventInputMeta.keywords",
    documentationUrl = "/pipeline/transform/op-event-input.html")
@InjectionSupported(localizationPrefix = "OpEventInput.Injection.")
@Getter
@Setter
public class OpEventInputMeta extends BaseTransformMeta<OpEventInput, OpEventInputData> {
  private static final Class<?> PKG = OpEventInput.class;

  static final String FIELD_SEPARATOR = ",";

  private String connection;

  private Tab<IValueMeta> table = Tab.build(null, null, IValueMeta::getName);
  private boolean legacyJoinType;
  private boolean ignoreDel;
  private String whereCondition;
  private int rowLimit = 100;
  private String deleteStepName;
  private boolean order;
  private transient boolean legacy;

  @Override
  public String getName() {
    return getParentTransformMeta() != null ? getParentTransformMeta().getName() : super.getName();
  }

  public Tab<IValueMeta> getTabModel() {
    return table;
  }

  public RowMeta getPkMeta() {
    RowMeta rowMeta = new RowMeta();
    table.getPkFields().forEach(rowMeta::addValueMeta);
    return rowMeta;
  }

  public String getSchema() {
    return table.getSchema();
  }

  public void setSchema(String schema) {
    this.table.setSchema(schema);
  }

  public String getTable() {
    return table.getName();
  }

  public void setTable(String table) {
    this.table.setName(table);
  }

  public String getPkFields() {
    return table.getPkFieldNames();
  }

  public void setPkFields(Collection<IValueMeta> keyFields) {
    table.setPkFieldNames(
        keyFields.stream().map(IValueMeta::getName).collect(Collectors.joining(",")));
  }

  public void setKeyNames(String keyNames) {
    table.setPkFieldNames(keyNames);
  }

  public Collection<IValueMeta> getKeyFields() {
    Optional<Index<IValueMeta>> pkIndex = table.getPK();
    if (pkIndex.isPresent()) {
      return pkIndex.get().getFields();
    }
    return Collections.emptySet();
  }

  public boolean isSoftDelete() {
    return StringUtil.isEmpty(deleteStepName);
  }

  public void setDeleteStepName(String stepName) {
    deleteStepName = stepName;
    getTransformIOMeta().getTargetStreams().get(1).setSubject(stepName);
  }

  public Collection<IValueMeta> getFields() {
    return table.getFields();
  }

  public void setFields(Collection<IValueMeta> columns) {
    table.setFields(columns);
  }

  public Collection<Index<IValueMeta>> getIndexes() {
    return table.getIndexes();
  }

  public void setIndexes(Collection<Index<IValueMeta>> indexes) {
    table.setIndexes(indexes);
  }

  @Override
  public void setDefault() {
    connection = null;
    table = Tab.build(null, IValueMeta::getName);
    whereCondition = null;
    deleteStepName = null;
    rowLimit = 100;
    order = false;
  }

  @Override
  public Object clone() {
    OpEventInputMeta clone = (OpEventInputMeta) super.clone();
    clone.table = table.clone();
    return clone;
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
    table.getPK().ifPresent(index -> index.getFields().forEach(inputRowMeta::addValueMeta));
  }

  String getExecuteSql() {
    return Util.getMasterSql(table, whereCondition, order);
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
    if (StringUtil.isEmpty(connection)) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR, i18n("MSG.CheckResult.InvalidConnection"), stepMeta));
    }
    if (Utils.isEmpty(table.getName())) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR, i18n("MSG.CheckResult.MissingTableName"), stepMeta));
    }
    if (!table.getPK().isPresent()) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR, i18n("MSG.CheckResult.MissingKeyFields"), stepMeta));
    }
    if (input.length > 0) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_WARNING,
              i18n("OEIMeta.CheckResult.IgnorePrevStep"),
              stepMeta));
    }
    if (ignoreDel) {
      if (nextSteps(pipelineMeta.findNextTransforms(stepMeta), this::delStep) > 0) {
        remarks.add(
            new CheckResult(
                ICheckResult.TYPE_RESULT_WARNING,
                i18n("OEIMeta.CheckResult.IgnoreDeleteStep"),
                stepMeta));
      }
    } else if (StringUtil.isEmpty(deleteStepName)) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR,
              i18n("OEIMeta.CheckResult.MissingDelStep"),
              stepMeta));
    } else if (pipelineMeta.findTransform(deleteStepName) == null) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR, i18n("OEIMeta.CheckResult.NotDeleteFlow"), stepMeta));
    }
    long outputSteps = nextSteps(pipelineMeta.findNextTransforms(stepMeta), sm -> !delStep(sm));
    if (outputSteps == 0) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR, i18n("OEIMeta.CheckResult.NotMainFlow"), stepMeta));
    } else if (outputSteps > 1) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_WARNING, i18n("OEIMeta.CheckResult.MainFlowOK"), stepMeta));
    }
    if (remarks.isEmpty()) {
      remarks.add(
          new CheckResult(ICheckResult.TYPE_RESULT_OK, i18n("MSG.CheckResult.Pass"), stepMeta));
    }
  }

  private long nextSteps(List<TransformMeta> nextSteps, Predicate<TransformMeta> filter) {
    return nextSteps.stream().filter(filter).count();
  }

  private boolean delStep(TransformMeta stepMeta) {
    return stepMeta.getName().equals(deleteStepName);
  }

  @Override
  @Deprecated
  public String getXml() {
    StringBuilder builder = new StringBuilder(512);
    builder
        .append(XmlHandler.addTagValue(Util.TAG_CONNECTION, connection))
        .append(XmlHandler.addTagValue(Util.TAG_SCHEMA, table.getSchema()))
        .append(XmlHandler.addTagValue(Util.TAG_TABLE, table.getName()));
    //    if (StringUtil.isEmpty(delFlagField)) {
    //      builder.append(XmlHandler.addTagValue(Util.TAG_DEL_FLAG_FIELD, delFlagField));
    //    }

    XmlHandler.openTag(builder, Util.TAG_TABLES);
    if (!StringUtil.isEmpty(table.getName())) {
      // TODO attrs
      // XmlHandler.openTag(builder, Util.TAG_TABLE, Collections.singletonMap("name", getTable()));
      Util.forEachWithAttr(builder, table.getIndexFields(), "field", Util::getFieldAttrs);
      Util.forEachWithAttr(
          builder, nonEmptyIndexes(table.getIndexes()), "index", Util::getIndexAttrs);
      XmlHandler.closeTag(builder, Util.TAG_TABLE);
    }
    XmlHandler.closeTag(builder, Util.TAG_TABLES);
    if (legacyJoinType) {
      builder.append(XmlHandler.addTagValue("legacyJoinType", "Y"));
    }
    if (ignoreDel) {
      builder.append(XmlHandler.addTagValue("ignoreDel", isIgnoreDel()));
    }

    builder.append(XmlHandler.addTagValue("keyFields", getPkFields()));
    //    if (!StringUtil.isEmpty(delFlagField)) {
    //      builder.append(XmlHandler.addTagValue("delFieldName", delFlagField));
    //    }
    if (!StringUtil.isEmpty(deleteStepName)
        && nextSteps(
                parentTransformMeta.getParentPipelineMeta().findNextTransforms(parentTransformMeta),
                this::delStep)
            == 0) {
      deleteStepName = "";
    }
    builder
        .append(XmlHandler.addTagValue("rowLimit", rowLimit))
        .append(XmlHandler.addTagValue("deleteStepName", deleteStepName))
        .append(XmlHandler.addTagValue("condition", whereCondition));
    if (isOrder()) {
      XmlHandler.addTagValue("order", "Y");
    }
    return XmlFormatter.format(builder.toString());
  }

  private boolean filter(Index<IValueMeta> index) {
    return index.getFields() != null && !index.getFields().isEmpty();
  }

  private List<Index<IValueMeta>> nonEmptyIndexes(Collection<Index<IValueMeta>> index) {
    return index.stream().filter(this::filter).collect(Collectors.toList());
  }

  private <T> Collection<T> read(Node listNode, String tag, Function<Node, T> getter) {
    int nr = XmlHandler.countNodes(listNode, tag);
    List<T> list = new ArrayList<>(nr);
    for (int i = 0; i < nr; i++) {
      list.add(getter.apply(XmlHandler.getSubNodeByNr(listNode, tag, i)));
    }
    return list;
  }

  private IValueMeta readField(Node node) {
    String name = XmlHandler.getTagAttribute(node, "name");
    int type = Const.toInt(XmlHandler.getTagAttribute(node, "type"), 0);
    try {
      IValueMeta vm = ValueMetaFactory.createValueMeta(name, type);
      vm.setOriginalNullable(DatabaseMetaData.columnNullable);
      if ("Y".equalsIgnoreCase(XmlHandler.getTagAttribute(node, "non-null"))) {
        vm.setOriginalNullable(DatabaseMetaData.columnNoNulls);
      }
      vm.setLength(Const.toInt(XmlHandler.getTagAttribute(node, "length"), 0));
      vm.setPrecision(Const.toInt(XmlHandler.getTagAttribute(node, "precision"), -1));
      return vm;
    } catch (HopException e) {
      throw new IllegalStateException(e);
    }
  }

  private boolean readIndex(Node node) {
    return table.addIndex(
        Boolean.getBoolean(XmlHandler.getTagAttribute(node, "unique")),
        XmlHandler.getTagAttribute(node, "fields"));
  }

  @Override
  @SuppressWarnings("deprecated")
  @Deprecated
  public void loadXml(Node stepNode, IHopMetadataProvider metaStore) throws HopXmlException {
    boolean legacyMeta = XmlHandler.getSubNode(stepNode, "keyFields", "key") != null;
    String connTag = legacyMeta ? "connect" : "connection";
    connection = XmlHandler.getTagValue(stepNode, connTag);
    table.setSchema(XmlHandler.getTagValue(stepNode, "schema"));
    table.setName(XmlHandler.getTagValue(stepNode, "table"));
    legacyJoinType = "Y".equalsIgnoreCase(XmlHandler.getTagValue(stepNode, "legacyJoinType"));
    ignoreDel = "Y".equalsIgnoreCase(XmlHandler.getTagValue(stepNode, "ignoreDel"));

    if (legacyMeta) {
      legacy = true;
      loadLegacyMeta(stepNode);
    } else {
      Node node = XmlHandler.getSubNode(stepNode, "tables", "table");
      table.setFields(read(node, "field", this::readField));
      if (table.getFields().isEmpty()) {
        Node keyFieldNode = XmlHandler.getSubNode(stepNode, "keyFields");
        int nrRows = XmlHandler.countNodes(keyFieldNode, "key");
        List<IValueMeta> list = new ArrayList<>(nrRows);
        for (int i = 0; i < nrRows; i++) {
          node = XmlHandler.getSubNodeByNr(keyFieldNode, "key", i);
          try {
            list.add(
                ValueMetaFactory.createValueMeta(
                    XmlHandler.getTagValue(node, "column"),
                    Const.toInt(XmlHandler.getTagValue(node, "type"), 0),
                    Const.toInt(XmlHandler.getTagValue(node, "length"), -1),
                    Const.toInt(XmlHandler.getTagValue(node, "precision"), -1)));
          } catch (HopPluginException e) {
            throw new HopXmlException(e);
          }
          setFields(list);
        }
      } else {
        read(node, "index", this::readIndex);
      }
      setKeyNames(XmlHandler.getTagValue(stepNode, "keyFields"));
    }

    //    delFlagField = XmlHandler.getTagValue(stepNode, "delFieldName");
    whereCondition = XmlHandler.getTagValue(stepNode, "condition");
    this.rowLimit = Const.toInt(XmlHandler.getTagValue(stepNode, "rowLimit"), 30);
    setDeleteStepName(XmlHandler.getTagValue(stepNode, "deleteStepName"));
    order = "Y".equalsIgnoreCase(XmlHandler.getTagValue(stepNode, "order"));
  }

  private void loadLegacyMeta(Node stepNode) {
    Node fields = XmlHandler.getSubNode(stepNode, "keyFields");
    int nrRows = XmlHandler.countNodes(fields, "key");
    List<IValueMeta> fieldList = new ArrayList<>(nrRows);
    for (int i = 0; i < nrRows; i++) {
      Node node = XmlHandler.getSubNodeByNr(fields, "key", i);
      String name = XmlHandler.getTagValue(node, "column");
      int type = Const.toInt(XmlHandler.getTagValue(node, "type"), 0);
      try {
        fieldList.add(ValueMetaFactory.createValueMeta(name, type));
        fieldList.get(fieldList.size() - 1).setOriginalNullable(DatabaseMetaData.columnNullable);
      } catch (HopException ignore) {
      }
    }
    table.setFields(fieldList);
    table.setPkFieldNames(
        fieldList.stream().map(IValueMeta::getName).collect(Collectors.joining(",")));
    String sql = XmlHandler.getTagValue(stepNode, "sql");
    if (!StringUtil.isEmpty(sql)) {
      String[] strings = sql.split("\bwhere\b");
      if (strings.length > 1) {
        whereCondition = strings[1];
      }
    }
  }

  private IStream newStream(String text, StreamIcon icon) {
    return new Stream(StreamType.TARGET, null, text, icon, null);
  }

  @Override
  public ITransformIOMeta getTransformIOMeta() {
    ITransformIOMeta ioMeta = super.getTransformIOMeta(false);
    if (ioMeta == null) {
      ioMeta = new TransformIOMeta(false, false, false, false, false, true);
      ioMeta.addStream(newStream(i18n("OEIMeta.UpdateFlow.Description"), StreamIcon.TARGET));
      ioMeta.addStream(newStream(i18n("OEIMeta.DelFlow.Description"), StreamIcon.FALSE));
      setTransformIOMeta(ioMeta);
      logDetailed(i18n("MSG.Step.InitIoMetaStream", ioMeta.getTargetStreams().size(), getName()));
    }

    return ioMeta;
  }

  @Override
  public void resetTransformIoMeta() {}

  @Override
  public void setChanged(boolean ch) {
    super.setChanged(ch);
    if (!ch) {
      for (IStream stream : getTransformIOMeta().getTargetStreams()) {
        if (stream.getTransformMeta() != null) {
          continue;
        }
        TransformMeta self = getParentTransformMeta();
        for (TransformMeta stepMeta : self.getParentPipelineMeta().findNextTransforms(self)) {
          if (!stepMeta.getName().equalsIgnoreCase(deleteStepName)) {
            stream.setTransformMeta(stepMeta);
            return;
          }
        }
      }
    }
  }

  @Override
  public void searchInfoAndTargetTransforms(List<TransformMeta> steps) {
    logDetailed(i18n("MSG.TransformMeta.SearchInfoAndTarget", steps.size(), getName()));
    for (IStream stream : getTransformIOMeta().getTargetStreams()) {
      TransformMeta delStep = TransformMeta.findTransform(steps, deleteStepName);
      if (stream.getTransformMeta() == null && StreamIcon.FALSE.equals(stream.getStreamIcon())) {
        stream.setTransformMeta(delStep);
        break;
      }
    }
  }

  @Override
  public void handleStreamSelection(IStream stream) {
    logDebug(i18n("MSG.Stream.Selection", stream));
    List<IStream> streams = getTransformIOMeta().getTargetStreams();
    int index = streams.indexOf(stream);
    if (index == 0) {
      clearStepMeta(streams.get(1), stream);
    } else if (index == 1) {
      setDeleteStepName(stream.getTransformName());
      clearStepMeta(streams.get(0), stream);
    }
    resetTransformIoMeta();
  }

  private void clearStepMeta(IStream targetStream, IStream stream) {
    if (targetStream.getTransformMeta() != null
        && stream.getTransformMeta().equals(targetStream.getTransformMeta())) {
      targetStream.setTransformMeta(null);
    }
  }

  @Override
  public boolean excludeFromCopyDistributeVerification() {
    return true;
  }

  private String i18n(String key, Object... params) {
    return Util.i18n(SubOpEventMeta.class, key, params);
  }
}
