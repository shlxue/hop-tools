package org.apache.hop.testing.engine;

import org.apache.hop.core.Result;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.pipeline.engine.IPipelineEngine;

public class LocalPipelineEngineProvider extends AbstractEngineProvider<IPipelineEngine> {
  public LocalPipelineEngineProvider() {
    super(IPipelineEngine.class);
  }

  @Override
  protected Result execute() throws HopException {
    engine.prepareExecution();
    if (engine.isReadyToStart()) {
      engine.startThreads();
      if (engine.isRunning()) {
        engine.waitUntilFinished();
      }
    }
    return engine.getResult();
  }
}
