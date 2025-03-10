package org.apache.hop.testing.junit;

import org.apache.hop.core.logging.ILoggingObject;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.pipeline.engines.local.LocalPipelineEngine;
import org.apache.hop.pipeline.engines.remote.RemotePipelineEngine;

class EngineDelegates {
  private final IVariables variables;
  private final ILoggingObject parent;

  EngineDelegates(IVariables variables, ILoggingObject parent) {
    this.variables = variables;
    this.parent = parent;
  }

  IPipelineEngine<?> beamPipelineEngine(PipelineMeta pipeline) {
    return new RemotePipelineEngine(pipeline);
  }

  IPipelineEngine<?> remotePipelineEngine(PipelineMeta pipeline) {
    return new RemotePipelineEngine(pipeline);
  }

  IPipelineEngine<?> localPipelineEngine(PipelineMeta pipeline) {
    return new LocalPipelineEngine(pipeline, variables, parent);
  }
}
