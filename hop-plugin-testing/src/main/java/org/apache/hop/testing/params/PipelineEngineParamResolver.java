package org.apache.hop.testing.params;

import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.pipeline.engines.local.LocalPipelineEngine;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import java.lang.reflect.Constructor;

class PipelineEngineParamResolver
    extends BaseEngineParam<IPipelineEngine<PipelineMeta>, PipelineMeta> {

  public PipelineEngineParamResolver(PipelineMeta pipelineMeta) {
    super(pipelineMeta);
  }

  @Override
  protected boolean matchConstructor(Constructor<?> constructor) {
    return true;
  }

  @Override
  public IPipelineEngine<PipelineMeta> resolveParameter(ParameterContext parameterContext, ExtensionContext context) throws ParameterResolutionException {
    return new LocalPipelineEngine(super.meta);
  }

  @Override
  protected IPipelineEngine<PipelineMeta> creator(
      Constructor<IPipelineEngine<PipelineMeta>> constructor, ExtensionContext context)
      throws ReflectiveOperationException {
    return new LocalPipelineEngine(super.meta);
  }
}
