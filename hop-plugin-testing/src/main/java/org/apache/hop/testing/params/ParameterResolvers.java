package org.apache.hop.testing.params;

import org.junit.jupiter.api.extension.ParameterResolver;

public final class ParameterResolvers {
  private ParameterResolvers() {}

  public static ParameterResolver swtWidget() {
    return new WidgetParamResolver();
  }

  public static ParameterResolver swtDialog() {
    return new DialogParamResolver();
  }

  public static ParameterResolver action() {
    return new ActionParamResolver();
  }

  public static ParameterResolver transformMeta() {
    return new TransformParamResolver();
  }

  public static ParameterResolver transform() {
    return new TransformParamResolver();
  }

  public static ParameterResolver actionUi() {
    return new ActionUiParamResolver();
  }

  public static ParameterResolver transformUi() {
    return new TransformUiParamResolver();
  }

  public static ParameterResolver pipelineEngine() {
    return new PipelineEngineParamResolver();
  }

  public static ParameterResolver workflowEngine() {
    return new WorkflowEngineParamResolver();
  }
}
