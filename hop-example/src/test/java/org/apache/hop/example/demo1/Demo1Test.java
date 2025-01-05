package org.apache.hop.example.demo1;

import org.apache.hop.core.Result;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.HopExtension;
import org.apache.hop.testing.params.provider.HopFileSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
@HopEnv(withH2 = true)
class Demo1Test {

  @TestTemplate
  void testTransformUi(Demo1Dialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  @HopFileSource()
  void testDemo1(IPipelineEngine<?> engine, ITransform transform, Result result) {
    Assertions.assertNotNull(result);
  }
}
