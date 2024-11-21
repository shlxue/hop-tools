package org.apache.hop.example.demo1;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;

public class Demo1 extends BaseTransform<Demo1Meta, Demo1Data> {

  public Demo1(
      TransformMeta transformMeta,
      Demo1Meta meta,
      Demo1Data data,
      int copyNr,
      PipelineMeta pipelineMeta,
      Pipeline pipeline) {
    super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
  }

  @Override
  public boolean processRow() throws HopException {
    return super.processRow();
  }

  @Override
  public void dispose() {
    super.dispose();
  }

  @Override
  public boolean init() {
    return super.init();
  }
}
