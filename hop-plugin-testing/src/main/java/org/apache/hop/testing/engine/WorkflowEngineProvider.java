package org.apache.hop.testing.engine;

import org.apache.hop.core.Result;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.workflow.engine.IWorkflowEngine;

public class WorkflowEngineProvider extends AbstractEngineProvider<IWorkflowEngine> {
  public WorkflowEngineProvider() {
    super(IWorkflowEngine.class);
  }

  @Override
  protected Result execute()  {
    return engine.startExecution();
  }
}
