package org.apache.hop.example;

import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;

public class TransformExample extends BaseTransform<TransformExampleMeta, TransformExampleData> {

  public TransformExample(
      TransformMeta transformMeta,
      TransformExampleMeta meta,
      TransformExampleData data,
      int copyNr,
      PipelineMeta pipelineMeta,
      Pipeline pipeline) {
    super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
  }
}
