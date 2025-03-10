package org.apache.hop.transforms.cdc;

import org.apache.hop.core.Result;
import org.apache.hop.pipeline.engine.IEngineComponent;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.HopExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
@HopEnv(withH2 = true)
class OpEventInputTest {

  @TestTemplate
  void testOpEventInputUi(OpEventInputDialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  void testOpEvent(IPipelineEngine<?> engine) {
    Assertions.assertNotNull(engine);
  }

  @TestTemplate
  void testOpEvent(Result result) {
    Assertions.assertNotNull(result);
  }

  @TestTemplate
  void testOpEvent(IEngineComponent component) {
    Assertions.assertNotNull(component);
  }

  @TestTemplate
  void testOpEvent(ITransform transform) {
    Assertions.assertNotNull(transform);
  }

  @TestTemplate
  void testOpEvent(OpEventInput transform) {
    Assertions.assertNotNull(transform);
  }
}
