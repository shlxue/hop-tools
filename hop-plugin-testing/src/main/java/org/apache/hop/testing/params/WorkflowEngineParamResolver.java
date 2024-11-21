package org.apache.hop.testing.params;

import org.apache.hop.core.parameters.INamedParameters;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.engine.IWorkflowEngine;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Constructor;

class WorkflowEngineParamResolver
    extends GenericTypeParamResolver<IWorkflowEngine<WorkflowMeta>, INamedParameters> {

  WorkflowEngineParamResolver() {
    super(true);
  }

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
