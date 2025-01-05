package org.apache.hop.example.demo2;

import org.apache.hop.core.Result;
import org.apache.hop.pipeline.engine.IEngineComponent;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.testing.HopAssert;
import org.apache.hop.testing.HopExtension;
import org.apache.hop.testing.params.provider.HopFileSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
class Demo2Test {

  @TestTemplate
  void testDemo2Ui(Demo2Dialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  @HopFileSource
  void testDemo2Default(Result result) {
    Assertions.assertNotNull(result);
  }

  @TestTemplate
  @HopFileSource
  void testDemo2(Result result, ITransform transform, IEngineComponent component) {
    HopAssert.assertResult(result, 1, true);
  }
}
