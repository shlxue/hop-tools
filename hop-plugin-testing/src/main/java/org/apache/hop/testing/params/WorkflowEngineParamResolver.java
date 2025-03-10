package org.apache.hop.testing.params;

import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.engine.IWorkflowEngine;
import org.apache.hop.workflow.engines.local.LocalWorkflowEngine;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import java.lang.reflect.Constructor;

class WorkflowEngineParamResolver
    extends BaseEngineParam<IWorkflowEngine<WorkflowMeta>, WorkflowMeta> {


  protected WorkflowEngineParamResolver(WorkflowMeta meta) {
    super(meta);
  }

  @Override
  protected boolean matchConstructor(Constructor<?> constructor) {
    return true;
  }

  @Override
  public IWorkflowEngine<WorkflowMeta> resolveParameter(ParameterContext parameterContext, ExtensionContext context) throws ParameterResolutionException {
    return new LocalWorkflowEngine(meta);
  }

  @Override
  protected IWorkflowEngine<WorkflowMeta> creator(
      Constructor<IWorkflowEngine<WorkflowMeta>> constructor, ExtensionContext context)
      throws ReflectiveOperationException {
    return null;
  }
}
