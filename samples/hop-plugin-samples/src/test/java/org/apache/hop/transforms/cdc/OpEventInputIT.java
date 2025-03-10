package org.apache.hop.transforms.cdc;

import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.HopExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
@HopEnv(withH2 = true)
class OpEventInputIT {
  @TestTemplate
  void testOpEvent(IPipelineEngine<?> engine) {
    Assertions.assertNotNull(engine);
  }
}
