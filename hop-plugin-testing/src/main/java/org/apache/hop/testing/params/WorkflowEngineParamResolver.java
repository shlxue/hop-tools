package org.apache.hop.testing.params;

import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.engine.IWorkflowEngine;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Constructor;

class WorkflowEngineParamResolver
    extends BaseEngineParam<IWorkflowEngine<WorkflowMeta>, WorkflowMeta> {

  @Override
  protected boolean matchConstructor(Constructor<?> constructor) {
    return false;
  }

  @Override
  protected IWorkflowEngine<WorkflowMeta> creator(
      Constructor<IWorkflowEngine<WorkflowMeta>> constructor, ExtensionContext context)
      throws ReflectiveOperationException {
    return null;
  }
}
