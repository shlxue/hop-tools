package org.apache.hop.example;

import org.apache.hop.core.Result;
import org.apache.hop.testing.HopAssert;
import org.apache.hop.testing.HopExtension;
import org.apache.hop.workflow.action.IAction;
import org.apache.hop.workflow.engine.IWorkflowEngine;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HopExtension.class)
class ActionExampleIT {

  @TestTemplate
  void testActionExample(Result result) {
    HopAssert.assertSuccess(result);
  }

  @TestTemplate
  void testActionExample(IWorkflowEngine<?> engine) {
    Result result = engine.getResult();
    HopAssert.assertSuccess(result);
  }

  @TestTemplate
  void testActionExample(IAction action) {
    HopAssert.assertSuccess(action);
  }

  @TestTemplate
  void testActionExample(ActionExample example) {
    HopAssert.assertSuccess(example);
  }
}
