package org.apache.hop.testing.params;

import org.apache.hop.core.Result;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.pipeline.engine.IEngineComponent;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.pipeline.transforms.dummy.Dummy;
import org.apache.hop.testing.HopExtension;
import org.apache.hop.testing.params.provider.HopFileSource;
import org.apache.hop.ui.pipeline.transforms.dummy.DummyDialog;
import org.apache.hop.ui.workflow.actions.dummy.ActionDummyDialog;
import org.apache.hop.workflow.action.IAction;
import org.apache.hop.workflow.actions.dummy.ActionDummy;
import org.apache.hop.workflow.engine.IWorkflowEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
class HopParamsTest {

  @TestTemplate
  void testDummyActionUi(ActionDummyDialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  void testDummyTransformUi(DummyDialog dialog) {
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  @HopFileSource
  void testActionDummy(IAction dummy) {
    dummy.getXml();
  }

  @TestTemplate
  @HopFileSource
  void testTransformMeta(ITransformMeta dummyMeta) throws HopException {
    dummyMeta.getXml();
  }

  @TestTemplate
  @HopFileSource
  void testEngine(IWorkflowEngine<?> engine) {
    Assertions.assertNotNull(engine);
  }

  @TestTemplate
  @HopFileSource
  void testEngine(IPipelineEngine<?> engine) {
    Assertions.assertNotNull(engine);
  }

  @TestTemplate
  @HopFileSource
  void testTransform(ITransform dummy) {
    Assertions.assertNotNull(dummy);
  }

  @TestTemplate
  @HopFileSource
  void testTransform(IEngineComponent component) {
    Assertions.assertNotNull(component);
  }

  @TestTemplate
  @HopFileSource
  void testResult(Result rs) {}

  @TestTemplate
  @HopFileSource
  void testDummy(ActionDummy dummy) {}

  @TestTemplate
  @HopFileSource
  void testDummy(Dummy dummy) {}
}
