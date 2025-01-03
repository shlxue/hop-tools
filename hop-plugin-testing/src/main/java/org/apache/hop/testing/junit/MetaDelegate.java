package org.apache.hop.testing.junit;

import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.PipelineHopMeta;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transforms.rowgenerator.GeneratorField;
import org.apache.hop.pipeline.transforms.rowgenerator.RowGeneratorMeta;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.action.ActionMeta;
import org.apache.hop.workflow.action.IAction;
import org.apache.hop.workflow.actions.missing.MissingAction;
import org.apache.hop.workflow.engine.IWorkflowEngine;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

final class MetaDelegate {
  private final IHopMetadataProvider metadataProvider;
  private final Runnable waitUntilLoaded;

  MetaDelegate(IHopMetadataProvider metadataProvider, Runnable waitUntilLoaded) {
    this.metadataProvider = metadataProvider;
    this.waitUntilLoaded = waitUntilLoaded;
  }

  public <M extends PipelineMeta, T extends IPipelineEngine<M>> T newEngine(
      Class<T> clazz, M meta) {
    return null;
  }

  public <M extends WorkflowMeta, T extends IWorkflowEngine<M>> T newEngine(
      Class<T> clazz, M meta) {
    return null;
  }

  //  public PipelineMeta newPipelineMeta(ITransformMeta transformMeta, String transformName) {
  //    return newPipelineMeta(new MemoryMetadataProvider(), transformMeta, transformName);
  //  }

  public PipelineMeta newPipelineMeta(ITransformMeta transformMeta, String transformName) {
    waitUntilLoaded.run();
    PipelineMeta pipelineMeta = new PipelineMeta();
    pipelineMeta.setMetadataProvider(metadataProvider);
    pipelineMeta.addTransform(mockRowGenerator());
    pipelineMeta.addTransform(new TransformMeta(transformName, transformMeta));
    pipelineMeta.addPipelineHop(
        new PipelineHopMeta(pipelineMeta.getTransform(0), pipelineMeta.getTransform(1)));
    pipelineMeta.setName(transformMeta.getClass().getSimpleName() + "-pipeline");
    return pipelineMeta;
  }

  public <T extends ITransform> T newTransform(Class<T> type) {
    try {
      return type.getConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Instance transform " + type.getName(), e);
    }
  }

  public <T extends ITransformMeta> T newTransformMeta(Class<T> type) {
    try {
      T transformMeta = type.getConstructor().newInstance();
      transformMeta.setDefault();
      newPipelineMeta(transformMeta, type.getSimpleName() + "-test");
      return transformMeta;
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InstantiationException
        | InvocationTargetException e) {
      throw new IllegalStateException("Instance transform meta", e);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends IAction> T newAction(Class<T> type) {
    try {
      IAction action = newPluginInstance(type);
      action.setName("test-" + type.getSimpleName());
      WorkflowMeta workflowMeta = new WorkflowMeta();
      workflowMeta.setName(type.getSimpleName() + "-workflow");
      ActionMeta actionMeta = new ActionMeta(action);
      workflowMeta.addAction(actionMeta);
      return (T) action;
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Instance action " + type.getName(), e);
    }
  }

  private <T extends IAction> T newPluginInstance(Class<T> type)
      throws ReflectiveOperationException {
    if (MissingAction.class.equals(type)) {
      try {
        return type.getConstructor(String.class, String.class)
            .newInstance("test-" + type.getSimpleName(), "mock-UNKNOWN");
      } catch (Exception e) {
        throw new IllegalAccessException(e.getMessage());
      }
    }
    return type.getConstructor().newInstance();
  }

  private TransformMeta mockRowGenerator() {
    RowGeneratorMeta rowGeneratorMeta = new RowGeneratorMeta();
    rowGeneratorMeta.setDefault();
    List<GeneratorField> fields = new ArrayList<>();
    for (int i = 0; i < 9; i++) {
      GeneratorField field = new GeneratorField();
      int type = i + 1;
      field.setName("f" + type);
      field.setType(IValueMeta.getTypeDescription(type));
      fields.add(field);
    }
    rowGeneratorMeta.setFields(fields);
    return new TransformMeta("mockRowGenerator", rowGeneratorMeta);
  }
}
