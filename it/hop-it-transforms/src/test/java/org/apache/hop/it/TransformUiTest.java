package org.apache.hop.it;

import org.apache.hop.pipeline.transforms.excelinput.ExcelInputDialog;
import org.apache.hop.pipeline.transforms.fileinput.text.TextFileCSVImportProgressDialog;
import org.apache.hop.pipeline.transforms.fileinput.text.TextFileInputDialog;
import org.apache.hop.pipeline.transforms.kafka.consumer.KafkaConsumerInputDialog;
import org.apache.hop.pipeline.transforms.mail.MailDialog;
import org.apache.hop.pipeline.transforms.pipelineexecutor.PipelineExecutorDialog;
import org.apache.hop.pipeline.transforms.rest.RestDialog;
import org.apache.hop.pipeline.transforms.textfileoutput.TextFileOutputDialog;
import org.apache.hop.pipeline.transforms.userdefinedjavaclass.UserDefinedJavaClassDialog;
import org.apache.hop.pipeline.transforms.workflowexecutor.WorkflowExecutorDialog;
import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.HopExtension;
import org.apache.hop.testing.SpecMode;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(HopExtension.class)
@HopEnv(ui = SpecMode.PREVIEW)
class TransformUiTest {
  @TestTemplate
  void testExcelInputUi(ExcelInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testKafkaConsumerInputUi(KafkaConsumerInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testMailUi(MailDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testPipelineExecutorUi(PipelineExecutorDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testRestUi(RestDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testTextFileCSVImportProgressUi(TextFileCSVImportProgressDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testTextFileInputUi(TextFileInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testTextFileOutputUi(TextFileOutputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testUserDefinedJavaClassUi(UserDefinedJavaClassDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testWorkflowExecutorUi(WorkflowExecutorDialog dialog) {
    assertNull(dialog);
  }
}
