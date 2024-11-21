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
