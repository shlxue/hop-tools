package org.apache.hop.it;

import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.HopExtension;
import org.apache.hop.testing.SpecMode;
import org.apache.hop.workflow.actions.abort.ActionAbortDialog;
import org.apache.hop.workflow.actions.addresultfilenames.ActionAddResultFilenamesDialog;
import org.apache.hop.workflow.actions.as400command.ActionAs400CommandDialog;
import org.apache.hop.workflow.actions.checkdbconnection.ActionCheckDbConnectionsDialog;
import org.apache.hop.workflow.actions.checkfilelocked.ActionCheckFilesLockedDialog;
import org.apache.hop.workflow.actions.columnsexist.ActionColumnsExistDialog;
import org.apache.hop.workflow.actions.copyfiles.ActionCopyFilesDialog;
import org.apache.hop.workflow.actions.copymoveresultfilenames.ActionCopyMoveResultFilenamesDialog;
import org.apache.hop.workflow.actions.createfile.ActionCreateFileDialog;
import org.apache.hop.workflow.actions.createfolder.ActionCreateFolderDialog;
import org.apache.hop.workflow.actions.delay.ActionDelayDialog;
import org.apache.hop.workflow.actions.deletefile.ActionDeleteFileDialog;
import org.apache.hop.workflow.actions.deletefiles.ActionDeleteFilesDialog;
import org.apache.hop.workflow.actions.deletefolders.ActionDeleteFoldersDialog;
import org.apache.hop.workflow.actions.deleteresultfilenames.ActionDeleteResultFilenamesDialog;
import org.apache.hop.workflow.actions.dostounix.ActionDosToUnixDialog;
import org.apache.hop.workflow.actions.eval.ActionEvalDialog;
import org.apache.hop.workflow.actions.evalfilesmetrics.ActionEvalFilesMetricsDialog;
import org.apache.hop.workflow.actions.evaluatetablecontent.ActionEvalTableContentDialog;
import org.apache.hop.workflow.actions.filecompare.ActionFileCompareDialog;
import org.apache.hop.workflow.actions.fileexists.ActionFileExistsDialog;
import org.apache.hop.workflow.actions.filesexist.ActionFilesExistDialog;
import org.apache.hop.workflow.actions.folderisempty.ActionFolderIsEmptyDialog;
import org.apache.hop.workflow.actions.folderscompare.ActionFoldersCompareDialog;
import org.apache.hop.workflow.actions.ftp.ActionFtpDialog;
import org.apache.hop.workflow.actions.ftpdelete.ActionFtpDeleteDialog;
import org.apache.hop.workflow.actions.ftpput.ActionFtpPutDialog;
import org.apache.hop.workflow.actions.getpop.ActionGetPOPDialog;
import org.apache.hop.workflow.actions.getpop.SelectFolderDialog;
import org.apache.hop.workflow.actions.http.ActionHttpDialog;
import org.apache.hop.workflow.actions.mail.ActionMailDialog;
import org.apache.hop.workflow.actions.mailvalidator.ActionMailValidatorDialog;
import org.apache.hop.workflow.actions.movefiles.ActionMoveFilesDialog;
import org.apache.hop.workflow.actions.msgboxinfo.ActionMsgBoxInfoDialog;
import org.apache.hop.workflow.actions.mssqlbulkload.ActionMssqlBulkLoadDialog;
import org.apache.hop.workflow.actions.mysqlbulkfile.ActionMysqlBulkFileDialog;
import org.apache.hop.workflow.actions.mysqlbulkload.ActionMysqlBulkLoadDialog;
import org.apache.hop.workflow.actions.pgpdecryptfiles.ActionPGPDecryptFilesDialog;
import org.apache.hop.workflow.actions.pgpencryptfiles.ActionPGPEncryptFilesDialog;
import org.apache.hop.workflow.actions.pgpverify.ActionPGPVerifyDialog;
import org.apache.hop.workflow.actions.ping.ActionPingDialog;
import org.apache.hop.workflow.actions.pipeline.ActionPipelineDialog;
import org.apache.hop.workflow.actions.repeat.EndRepeatDialog;
import org.apache.hop.workflow.actions.repeat.RepeatDialog;
import org.apache.hop.workflow.actions.sendnagiospassivecheck.ActionSendNagiosPassiveCheckDialog;
import org.apache.hop.workflow.actions.setvariables.ActionSetVariablesDialog;
import org.apache.hop.workflow.actions.sftp.ActionSftpDialog;
import org.apache.hop.workflow.actions.sftpput.ActionSftpPutDialog;
import org.apache.hop.workflow.actions.shell.ActionShellDialog;
import org.apache.hop.workflow.actions.simpleeval.ActionSimpleEvalDialog;
import org.apache.hop.workflow.actions.snmptrap.ActionSNMPTrapDialog;
import org.apache.hop.workflow.actions.snowflake.WarehouseManagerDialog;
import org.apache.hop.workflow.actions.sql.ActionSqlDialog;
import org.apache.hop.workflow.actions.success.ActionSuccessDialog;
import org.apache.hop.workflow.actions.tableexists.ActionTableExistsDialog;
import org.apache.hop.workflow.actions.telnet.ActionTelnetDialog;
import org.apache.hop.workflow.actions.truncatetables.ActionTruncateTablesDialog;
import org.apache.hop.workflow.actions.unzip.ActionUnZipDialog;
import org.apache.hop.workflow.actions.waitforfile.ActionWaitForFileDialog;
import org.apache.hop.workflow.actions.waitforsql.ActionWaitForSqlDialog;
import org.apache.hop.workflow.actions.webserviceavailable.ActionWebServiceAvailableDialog;
import org.apache.hop.workflow.actions.workflow.ActionWorkflowDialog;
import org.apache.hop.workflow.actions.writetofile.ActionWriteToFileDialog;
import org.apache.hop.workflow.actions.writetolog.ActionWriteToLogDialog;
import org.apache.hop.workflow.actions.xml.dtdvalidator.DtdValidatorDialog;
import org.apache.hop.workflow.actions.xml.xmlwellformed.XmlWellFormedDialog;
import org.apache.hop.workflow.actions.xml.xsdvalidator.XsdValidatorDialog;
import org.apache.hop.workflow.actions.xml.xslt.XsltDialog;
import org.apache.hop.workflow.actions.zipfile.ActionZipFileDialog;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(HopExtension.class)
@HopEnv(ui = SpecMode.DEBUG)
class ActionIT {
  @TestTemplate
  void testActionAbortUi(ActionAbortDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionAddResultFilenamesUi(ActionAddResultFilenamesDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionAs400CommandUi(ActionAs400CommandDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionCheckDbConnectionsUi(ActionCheckDbConnectionsDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionCheckFilesLockedUi(ActionCheckFilesLockedDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionColumnsExistUi(ActionColumnsExistDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionCopyFilesUi(ActionCopyFilesDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionCopyMoveResultFilenamesUi(ActionCopyMoveResultFilenamesDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionCreateFileUi(ActionCreateFileDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionCreateFolderUi(ActionCreateFolderDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionDelayUi(ActionDelayDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionDeleteFileUi(ActionDeleteFileDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionDeleteFilesUi(ActionDeleteFilesDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionDeleteFoldersUi(ActionDeleteFoldersDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionDeleteResultFilenamesUi(ActionDeleteResultFilenamesDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionDosToUnixUi(ActionDosToUnixDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionEvalUi(ActionEvalDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionEvalFilesMetricsUi(ActionEvalFilesMetricsDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionEvalTableContentUi(ActionEvalTableContentDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionFileCompareUi(ActionFileCompareDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionFileExistsUi(ActionFileExistsDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionFilesExistUi(ActionFilesExistDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionFolderIsEmptyUi(ActionFolderIsEmptyDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionFoldersCompareUi(ActionFoldersCompareDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionFtpUi(ActionFtpDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionFtpDeleteUi(ActionFtpDeleteDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionFtpPutUi(ActionFtpPutDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionGetPOPUi(ActionGetPOPDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionHttpUi(ActionHttpDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionMailUi(ActionMailDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionMailValidatorUi(ActionMailValidatorDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionMoveFilesUi(ActionMoveFilesDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionMsgBoxInfoUi(ActionMsgBoxInfoDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionMssqlBulkLoadUi(ActionMssqlBulkLoadDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionMysqlBulkFileUi(ActionMysqlBulkFileDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionMysqlBulkLoadUi(ActionMysqlBulkLoadDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionPGPDecryptFilesUi(ActionPGPDecryptFilesDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionPGPEncryptFilesUi(ActionPGPEncryptFilesDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionPGPVerifyUi(ActionPGPVerifyDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionPingUi(ActionPingDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionPipelineUi(ActionPipelineDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionSNMPTrapUi(ActionSNMPTrapDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionSendNagiosPassiveCheckUi(ActionSendNagiosPassiveCheckDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionSetVariablesUi(ActionSetVariablesDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionSftpUi(ActionSftpDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionSftpPutUi(ActionSftpPutDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionShellUi(ActionShellDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionSimpleEvalUi(ActionSimpleEvalDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionSqlUi(ActionSqlDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionSuccessUi(ActionSuccessDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionTableExistsUi(ActionTableExistsDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionTelnetUi(ActionTelnetDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionTruncateTablesUi(ActionTruncateTablesDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionUnZipUi(ActionUnZipDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionWaitForFileUi(ActionWaitForFileDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionWaitForSqlUi(ActionWaitForSqlDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionWebServiceAvailableUi(ActionWebServiceAvailableDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionWorkflowUi(ActionWorkflowDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionWriteToFileUi(ActionWriteToFileDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionWriteToLogUi(ActionWriteToLogDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testActionZipFileUi(ActionZipFileDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testDtdValidatorUi(DtdValidatorDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testEndRepeatUi(EndRepeatDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testRepeatUi(RepeatDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSelectFolderUi(SelectFolderDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testWarehouseManagerUi(WarehouseManagerDialog dialog) {
    assertNull(dialog);
  }

  //  @TestTemplate
  //  void testWorkflowEntryPipelineDialogTestUi(WorkflowEntryPipelineDialogTestDialog dialog) {
  //    assertNull(dialog);
  //  }

  @TestTemplate
  void testXmlWellFormedUi(XmlWellFormedDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testXsdValidatorUi(XsdValidatorDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testXsltUi(XsltDialog dialog) {
    assertNull(dialog);
  }
}
