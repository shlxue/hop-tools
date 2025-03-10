package org.apache.hop.testing;

import org.apache.hop.core.Result;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transforms.switchcase.SwitchCase;
import org.apache.hop.pipeline.transforms.writetolog.WriteToLog;
import org.apache.hop.testing.params.provider.HopFilter;
import org.apache.hop.workflow.actions.start.ActionStart;
import org.apache.hop.workflow.engine.IWorkflowEngine;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(HopExtension.class)
class HopExtensionTest {

  @TestTemplate
//  @HopFilter(value = SwitchCase.class)
  void testTransform(IPipelineEngine<?> engine) {
    assertNotNull(engine);
  }

  @TestTemplate
  @HopFilter(value = SwitchCase.class)
  void testTransform(IPipelineEngine<?> engine, Result result, SwitchCase transform) {
    assertNotNull(engine);
    assertNotNull(transform);
    assertNotNull(result);
    HopAssert.assertSuccess(result);
  }

  @TestTemplate
  @HopFilter(value = ActionStart.class)
  void testAction(IWorkflowEngine<?> engine) {
    assertNotNull(engine);
    HopAssert.assertSuccess(engine.getResult());
  }
}
