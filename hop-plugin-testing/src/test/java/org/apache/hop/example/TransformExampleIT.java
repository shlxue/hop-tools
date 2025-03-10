package org.apache.hop.example;

import org.apache.hop.core.Result;
import org.apache.hop.pipeline.engine.IEngineComponent;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.testing.HopAssert;
import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.HopExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
@HopEnv(withH2 = true)
class TransformExampleIT {

  @TestTemplate
  void testTransformExampleByEngine(IPipelineEngine<?> engine) {
    Assertions.assertNotNull(engine);
  }

  @TestTemplate
  void testTransformExampleByResult(Result result) {
    HopAssert.assertSuccess(result);
  }

  @TestTemplate
  void testTransformExample(IEngineComponent component) {
    HopAssert.assertSuccess(component);
  }

  @TestTemplate
  void testTransformExample(TransformExample demo1) {
    HopAssert.assertSuccess(demo1);
  }
}
