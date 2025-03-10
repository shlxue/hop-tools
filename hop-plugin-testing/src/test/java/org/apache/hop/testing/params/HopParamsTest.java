package org.apache.hop.testing.params;

import org.apache.hop.core.Result;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.pipeline.transforms.dummy.Dummy;
import org.apache.hop.testing.HopAssert;
import org.apache.hop.testing.HopExtension;
import org.apache.hop.testing.params.provider.HopFilter;
import org.apache.hop.ui.pipeline.transforms.dummy.DummyDialog;
import org.apache.hop.ui.workflow.actions.dummy.ActionDummyDialog;
import org.apache.hop.workflow.action.IAction;
import org.apache.hop.workflow.actions.dummy.ActionDummy;
import org.apache.hop.workflow.engine.IWorkflowEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@ExtendWith(HopExtension.class)
class HopParamsTest {

  @TestTemplate
  @HopFilter()
  void testActionDummy(IAction dummy) {
    dummy.getXml();
  }

  @TestTemplate
  @HopFilter
  void testTransformMeta(ITransformMeta dummyMeta) {
    //    dummyMeta.getXml();
  }

  @TestTemplate
  @HopFilter()
  void testActionDummy(IAction before, IAction after) {}

  @TestTemplate
  @HopFilter
  void testTransformMeta(ITransformMeta before, ITransformMeta after) {}

  @TestTemplate
  @HopFilter
  void testEngine(IWorkflowEngine<?> engine) {
    HopAssert.assertSuccess(engine);
  }

  @TestTemplate
  @HopFilter
  void testEngine(IPipelineEngine<?> engine) {
    HopAssert.assertSuccess(engine);
  }

  @TestTemplate
  @HopFilter
  void testTransform(ITransform transform) {
    HopAssert.assertSuccess(transform);
    HopAssert.assertRows(transform, 1, 1);
  }

  @TestTemplate
  @HopFilter
  void testResult(Result rs) {
    HopAssert.assertFailed(rs);
    HopAssert.assertRows(rs, 1, 1);
  }

  @TestTemplate
  @HopFilter
  void testDummy(ActionDummy dummy) {}

  @TestTemplate
  @HopFilter
  void testDummy(Dummy dummy) {}

  //  @TestTemplate
  //  @HopFilter
  //  void testDummies(List<ActionDummy> dummies) {
  //  }

  @TestTemplate
  @HopFilter
  void testDummies(List<Dummy> dummies) {}

  @TestTemplate
  void testDummyActionUi(ActionDummyDialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  void testDummyTransformUi(DummyDialog dialog) {
    Assertions.assertNotNull(dialog);
  }
}
