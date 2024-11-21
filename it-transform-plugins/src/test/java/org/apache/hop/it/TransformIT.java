package org.apache.hop.it;

import org.apache.hop.pipeline.transforms.abort.AbortDialog;
import org.apache.hop.pipeline.transforms.addsequence.AddSequenceDialog;
import org.apache.hop.pipeline.transforms.analyticquery.AnalyticQueryDialog;
import org.apache.hop.pipeline.transforms.append.AppendDialog;
import org.apache.hop.pipeline.transforms.blockingtransform.BlockingTransformDialog;
import org.apache.hop.pipeline.transforms.blockuntiltransformsfinish.BlockUntilTransformsFinishDialog;
import org.apache.hop.pipeline.transforms.calculator.CalculatorDialog;
import org.apache.hop.pipeline.transforms.changefileencoding.ChangeFileEncodingDialog;
import org.apache.hop.pipeline.transforms.checksum.CheckSumDialog;
import org.apache.hop.pipeline.transforms.clonerow.CloneRowDialog;
import org.apache.hop.pipeline.transforms.closure.ClosureGeneratorDialog;
import org.apache.hop.pipeline.transforms.coalesce.CoalesceDialog;
import org.apache.hop.pipeline.transforms.columnexists.ColumnExistsDialog;
import org.apache.hop.pipeline.transforms.combinationlookup.CombinationLookupDialog;
import org.apache.hop.pipeline.transforms.concatfields.ConcatFieldsDialog;
import org.apache.hop.pipeline.transforms.constant.ConstantDialog;
import org.apache.hop.pipeline.transforms.cratedbbulkloader.CrateDBBulkLoaderDialog;
import org.apache.hop.pipeline.transforms.creditcardvalidator.CreditCardValidatorDialog;
import org.apache.hop.pipeline.transforms.csvinput.CsvInputDialog;
import org.apache.hop.pipeline.transforms.cubeinput.CubeInputDialog;
import org.apache.hop.pipeline.transforms.cubeoutput.CubeOutputDialog;
import org.apache.hop.pipeline.transforms.databasejoin.DatabaseJoinDialog;
import org.apache.hop.pipeline.transforms.databaselookup.DatabaseLookupDialog;
import org.apache.hop.pipeline.transforms.datagrid.DataGridDialog;
import org.apache.hop.pipeline.transforms.dbproc.DBProcDialog;
import org.apache.hop.pipeline.transforms.delay.DelayDialog;
import org.apache.hop.pipeline.transforms.delete.DeleteDialog;
import org.apache.hop.pipeline.transforms.denormaliser.DenormaliserDialog;
import org.apache.hop.pipeline.transforms.detectemptystream.DetectEmptyStreamDialog;
import org.apache.hop.pipeline.transforms.detectlastrow.DetectLastRowDialog;
import org.apache.hop.pipeline.transforms.dimensionlookup.DimensionLookupDialog;
import org.apache.hop.pipeline.transforms.dorisbulkloader.DorisBulkLoaderDialog;
import org.apache.hop.pipeline.transforms.drools.RulesAccumulatorDialog;
import org.apache.hop.pipeline.transforms.drools.RulesExecutorDialog;
import org.apache.hop.pipeline.transforms.dynamicsqlrow.DynamicSqlRowDialog;
import org.apache.hop.pipeline.transforms.edi2xml.Edi2XmlDialog;
import org.apache.hop.pipeline.transforms.excelinput.ExcelInputDialog;
import org.apache.hop.pipeline.transforms.excelwriter.ExcelWriterTransformDialog;
import org.apache.hop.pipeline.transforms.execinfo.ExecInfoDialog;
import org.apache.hop.pipeline.transforms.execprocess.ExecProcessDialog;
import org.apache.hop.pipeline.transforms.execsqlrow.ExecSqlRowDialog;
import org.apache.hop.pipeline.transforms.fake.FakeDialog;
import org.apache.hop.pipeline.transforms.fieldschangesequence.FieldsChangeSequenceDialog;
import org.apache.hop.pipeline.transforms.fieldsplitter.FieldSplitterDialog;
import org.apache.hop.pipeline.transforms.fileexists.FileExistsDialog;
import org.apache.hop.pipeline.transforms.fileinput.text.TextFileCSVImportProgressDialog;
import org.apache.hop.pipeline.transforms.fileinput.text.TextFileInputDialog;
import org.apache.hop.pipeline.transforms.filelocked.FileLockedDialog;
import org.apache.hop.pipeline.transforms.filemetadata.FileMetadataDialog;
import org.apache.hop.pipeline.transforms.filesfromresult.FilesFromResultDialog;
import org.apache.hop.pipeline.transforms.filestoresult.FilesToResultDialog;
import org.apache.hop.pipeline.transforms.filterrows.FilterRowsDialog;
import org.apache.hop.pipeline.transforms.flattener.FlattenerDialog;
import org.apache.hop.pipeline.transforms.formula.FormulaDialog;
import org.apache.hop.pipeline.transforms.fuzzymatch.FuzzyMatchDialog;
import org.apache.hop.pipeline.transforms.getfilenames.GetFileNamesDialog;
import org.apache.hop.pipeline.transforms.getfilesrowcount.GetFilesRowsCountDialog;
import org.apache.hop.pipeline.transforms.getsubfolders.GetSubFoldersDialog;
import org.apache.hop.pipeline.transforms.gettablenames.GetTableNamesDialog;
import org.apache.hop.pipeline.transforms.getvariable.GetVariableDialog;
import org.apache.hop.pipeline.transforms.groupby.GroupByDialog;
import org.apache.hop.pipeline.transforms.http.HttpDialog;
import org.apache.hop.pipeline.transforms.httppost.HttpPostDialog;
import org.apache.hop.pipeline.transforms.ifnull.IfNullDialog;
import org.apache.hop.pipeline.transforms.input.MappingInputDialog;
import org.apache.hop.pipeline.transforms.insertupdate.InsertUpdateDialog;
import org.apache.hop.pipeline.transforms.janino.JaninoDialog;
import org.apache.hop.pipeline.transforms.javafilter.JavaFilterDialog;
import org.apache.hop.pipeline.transforms.javascript.ScriptValuesDialog;
import org.apache.hop.pipeline.transforms.joinrows.JoinRowsDialog;
import org.apache.hop.pipeline.transforms.jsoninput.JsonInputDialog;
import org.apache.hop.pipeline.transforms.jsonoutputenhanced.JsonOutputDialog;
import org.apache.hop.pipeline.transforms.kafka.consumer.KafkaConsumerInputDialog;
import org.apache.hop.pipeline.transforms.kafka.producer.KafkaProducerOutputDialog;
import org.apache.hop.pipeline.transforms.ldapinput.LdapInputDialog;
import org.apache.hop.pipeline.transforms.ldapoutput.LdapOutputDialog;
import org.apache.hop.pipeline.transforms.loadfileinput.LoadFileInputDialog;
import org.apache.hop.pipeline.transforms.mail.MailDialog;
import org.apache.hop.pipeline.transforms.mailinput.MailInputDialog;
import org.apache.hop.pipeline.transforms.mapping.SimpleMappingDialog;
import org.apache.hop.pipeline.transforms.memgroupby.MemoryGroupByDialog;
import org.apache.hop.pipeline.transforms.mergejoin.MergeJoinDialog;
import org.apache.hop.pipeline.transforms.mergerows.MergeRowsDialog;
import org.apache.hop.pipeline.transforms.metainject.MetaInjectDialog;
import org.apache.hop.pipeline.transforms.metainput.MetadataInputDialog;
import org.apache.hop.pipeline.transforms.metastructure.TransformMetaStructureDialog;
import org.apache.hop.pipeline.transforms.monetdbbulkloader.MonetDbBulkLoaderDialog;
import org.apache.hop.pipeline.transforms.mongodbdelete.MongoDbDeleteDialog;
import org.apache.hop.pipeline.transforms.mongodbinput.MongoDbInputDialog;
import org.apache.hop.pipeline.transforms.mongodboutput.MongoDbOutputDialog;
import org.apache.hop.pipeline.transforms.multimerge.MultiMergeJoinDialog;
import org.apache.hop.pipeline.transforms.normaliser.NormaliserDialog;
import org.apache.hop.pipeline.transforms.nullif.NullIfDialog;
import org.apache.hop.pipeline.transforms.numberrange.NumberRangeDialog;
import org.apache.hop.pipeline.transforms.orabulkloader.OraBulkLoaderDialog;
import org.apache.hop.pipeline.transforms.output.MappingOutputDialog;
import org.apache.hop.pipeline.transforms.pgbulkloader.PGBulkLoaderDialog;
import org.apache.hop.pipeline.transforms.pgpdecryptstream.PGPDecryptStreamDialog;
import org.apache.hop.pipeline.transforms.pgpencryptstream.PGPEncryptStreamDialog;
import org.apache.hop.pipeline.transforms.pipelineexecutor.PipelineExecutorDialog;
import org.apache.hop.pipeline.transforms.processfiles.ProcessFilesDialog;
import org.apache.hop.pipeline.transforms.propertyinput.PropertyInputDialog;
import org.apache.hop.pipeline.transforms.propertyoutput.PropertyOutputDialog;
import org.apache.hop.pipeline.transforms.randomvalue.RandomValueDialog;
import org.apache.hop.pipeline.transforms.recordsfromstream.RecordsFromStreamDialog;
import org.apache.hop.pipeline.transforms.regexeval.RegexEvalDialog;
import org.apache.hop.pipeline.transforms.regexeval.RegexEvalHelperDialog;
import org.apache.hop.pipeline.transforms.replacestring.ReplaceStringDialog;
import org.apache.hop.pipeline.transforms.reservoirsampling.ReservoirSamplingDialog;
import org.apache.hop.pipeline.transforms.rest.RestDialog;
import org.apache.hop.pipeline.transforms.rowgenerator.RowGeneratorDialog;
import org.apache.hop.pipeline.transforms.rowsfromresult.RowsFromResultDialog;
import org.apache.hop.pipeline.transforms.rowstoresult.RowsToResultDialog;
import org.apache.hop.pipeline.transforms.salesforce.SalesforceTransformDialog;
import org.apache.hop.pipeline.transforms.salesforcedelete.SalesforceDeleteDialog;
import org.apache.hop.pipeline.transforms.salesforceinput.SalesforceInputDialog;
import org.apache.hop.pipeline.transforms.salesforceinsert.SalesforceInsertDialog;
import org.apache.hop.pipeline.transforms.salesforceupdate.SalesforceUpdateDialog;
import org.apache.hop.pipeline.transforms.salesforceupsert.SalesforceUpsertDialog;
import org.apache.hop.pipeline.transforms.samplerows.SampleRowsDialog;
import org.apache.hop.pipeline.transforms.sasinput.SasInputDialog;
import org.apache.hop.pipeline.transforms.schemamapping.SchemaMappingDialog;
import org.apache.hop.pipeline.transforms.script.ScriptDialog;
import org.apache.hop.pipeline.transforms.selectvalues.SelectValuesDialog;
import org.apache.hop.pipeline.transforms.serverstatus.GetServerStatusDialog;
import org.apache.hop.pipeline.transforms.setvalueconstant.SetValueConstantDialog;
import org.apache.hop.pipeline.transforms.setvaluefield.SetValueFieldDialog;
import org.apache.hop.pipeline.transforms.setvariable.SetVariableDialog;
import org.apache.hop.pipeline.transforms.snowflake.bulkloader.SnowflakeBulkLoaderDialog;
import org.apache.hop.pipeline.transforms.sort.SortRowsDialog;
import org.apache.hop.pipeline.transforms.sortedmerge.SortedMergeDialog;
import org.apache.hop.pipeline.transforms.splitfieldtorows.SplitFieldToRowsDialog;
import org.apache.hop.pipeline.transforms.splunkinput.SplunkInputDialog;
import org.apache.hop.pipeline.transforms.sql.ExecSqlDialog;
import org.apache.hop.pipeline.transforms.sqlfileoutput.SQLFileOutputDialog;
import org.apache.hop.pipeline.transforms.ssh.SshDialog;
import org.apache.hop.pipeline.transforms.standardizephonenumber.StandardizePhoneNumberDialog;
import org.apache.hop.pipeline.transforms.streamlookup.StreamLookupDialog;
import org.apache.hop.pipeline.transforms.streamschemamerge.StreamSchemaDialog;
import org.apache.hop.pipeline.transforms.stringcut.StringCutDialog;
import org.apache.hop.pipeline.transforms.stringoperations.StringOperationsDialog;
import org.apache.hop.pipeline.transforms.switchcase.SwitchCaseDialog;
import org.apache.hop.pipeline.transforms.synchronizeaftermerge.SynchronizeAfterMergeDialog;
import org.apache.hop.pipeline.transforms.systemdata.SystemDataDialog;
import org.apache.hop.pipeline.transforms.tablecompare.TableCompareDialog;
import org.apache.hop.pipeline.transforms.tableexists.TableExistsDialog;
import org.apache.hop.pipeline.transforms.tableinput.TableInputDialog;
import org.apache.hop.pipeline.transforms.tableoutput.TableOutputDialog;
import org.apache.hop.pipeline.transforms.terafast.TeraFastDialog;
import org.apache.hop.pipeline.transforms.textfileoutput.TextFileOutputDialog;
import org.apache.hop.pipeline.transforms.tika.TikaDialog;
import org.apache.hop.pipeline.transforms.tokenreplacement.TokenReplacementDialog;
import org.apache.hop.pipeline.transforms.uniquerows.UniqueRowsDialog;
import org.apache.hop.pipeline.transforms.uniquerowsbyhashset.UniqueRowsByHashSetDialog;
import org.apache.hop.pipeline.transforms.update.UpdateDialog;
import org.apache.hop.pipeline.transforms.userdefinedjavaclass.UserDefinedJavaClassDialog;
import org.apache.hop.pipeline.transforms.validator.ValidatorDialog;
import org.apache.hop.pipeline.transforms.valuemapper.ValueMapperDialog;
import org.apache.hop.pipeline.transforms.vertica.bulkloader.VerticaBulkLoaderDialog;
import org.apache.hop.pipeline.transforms.webserviceavailable.WebServiceAvailableDialog;
import org.apache.hop.pipeline.transforms.webservices.WebServiceDialog;
import org.apache.hop.pipeline.transforms.workflowexecutor.WorkflowExecutorDialog;
import org.apache.hop.pipeline.transforms.writetolog.WriteToLogDialog;
import org.apache.hop.pipeline.transforms.xml.addxml.AddXmlDialog;
import org.apache.hop.pipeline.transforms.xml.getxmldata.GetXmlDataDialog;
import org.apache.hop.pipeline.transforms.xml.getxmldata.LoopNodesImportProgressDialog;
import org.apache.hop.pipeline.transforms.xml.getxmldata.XmlInputFieldsImportProgressDialog;
import org.apache.hop.pipeline.transforms.xml.xmlinputstream.XmlInputStreamDialog;
import org.apache.hop.pipeline.transforms.xml.xmljoin.XmlJoinDialog;
import org.apache.hop.pipeline.transforms.xml.xmloutput.XmlOutputDialog;
import org.apache.hop.pipeline.transforms.xml.xsdvalidator.XsdValidatorDialog;
import org.apache.hop.pipeline.transforms.xml.xslt.XsltDialog;
import org.apache.hop.pipeline.transforms.yamlinput.YamlInputDialog;
import org.apache.hop.pipeline.transforms.zipfile.ZipFileDialog;
import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.HopExtension;
import org.apache.hop.testing.SpecMode;
import org.apache.pipeline.transform.jdbcmetadata.JdbcMetadataDialog;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(HopExtension.class)
@HopEnv(ui = SpecMode.PREVIEW)
class TransformIT {
  @TestTemplate
  void testAbortUi(AbortDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testAddSequenceUi(AddSequenceDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testAddXmlUi(AddXmlDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testAnalyticQueryUi(AnalyticQueryDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testAppendUi(AppendDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testBlockUntilTransformsFinishUi(BlockUntilTransformsFinishDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testBlockingTransformUi(BlockingTransformDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testCalculatorUi(CalculatorDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testChangeFileEncodingUi(ChangeFileEncodingDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testCheckSumUi(CheckSumDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testCloneRowUi(CloneRowDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testClosureGeneratorUi(ClosureGeneratorDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testCoalesceUi(CoalesceDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testColumnExistsUi(ColumnExistsDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testCombinationLookupUi(CombinationLookupDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testConcatFieldsUi(ConcatFieldsDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testConstantUi(ConstantDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testCrateDBBulkLoaderUi(CrateDBBulkLoaderDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testCreditCardValidatorUi(CreditCardValidatorDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testCsvInputUi(CsvInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testCubeInputUi(CubeInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testCubeOutputUi(CubeOutputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testDBProcUi(DBProcDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testDataGridUi(DataGridDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testDatabaseJoinUi(DatabaseJoinDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testDatabaseLookupUi(DatabaseLookupDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testDelayUi(DelayDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testDeleteUi(DeleteDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testDenormaliserUi(DenormaliserDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testDetectEmptyStreamUi(DetectEmptyStreamDialog dialog) {
    assertNull(dialog);
  }

  //  @TestTemplate
  //  void testDetectLanguageUi(DetectLanguageDialog dialog) {
  //    assertNull(dialog);
  //  }

  @TestTemplate
  void testDetectLastRowUi(DetectLastRowDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testDimensionLookupUi(DimensionLookupDialog dialog) {
    assertNull(dialog);
  }

  //  @TestTemplate
  //  void testDirectoryDialogButtonListenerFactoryUi(
  //      DirectoryDialogButtonListenerFactoryDialog dialog) {
  //    assertNull(dialog);
  //  }

  @TestTemplate
  void testDorisBulkLoaderUi(DorisBulkLoaderDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testDynamicSqlRowUi(DynamicSqlRowDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testEdi2XmlUi(Edi2XmlDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testExcelInputUi(ExcelInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testExcelWriterTransformUi(ExcelWriterTransformDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testExecInfoUi(ExecInfoDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testExecProcessUi(ExecProcessDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testExecSqlUi(ExecSqlDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testExecSqlRowUi(ExecSqlRowDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testFakeUi(FakeDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testFieldSplitterUi(FieldSplitterDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testFieldsChangeSequenceUi(FieldsChangeSequenceDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testFileExistsUi(FileExistsDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testFileLockedUi(FileLockedDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testFileMetadataUi(FileMetadataDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testFilesFromResultUi(FilesFromResultDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testFilesToResultUi(FilesToResultDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testFilterRowsUi(FilterRowsDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testFlattenerUi(FlattenerDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testFormulaUi(FormulaDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testFuzzyMatchUi(FuzzyMatchDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testGetFileNamesUi(GetFileNamesDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testGetFilesRowsCountUi(GetFilesRowsCountDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testGetServerStatusUi(GetServerStatusDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testGetSubFoldersUi(GetSubFoldersDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testGetTableNamesUi(GetTableNamesDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testGetVariableUi(GetVariableDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testGetXmlDataUi(GetXmlDataDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testGroupByUi(GroupByDialog dialog) {
    assertNull(dialog);
  }

  //  @TestTemplate
  //  void testHtml2TextUi(Html2TextDialog dialog) {
  //    assertNull(dialog);
  //  }

  @TestTemplate
  void testHttpUi(HttpDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testHttpPostUi(HttpPostDialog dialog) {
    assertNull(dialog);
  }

  //  @TestTemplate
  //  void testIDialogCompositeUi(IDialogCompositeDialog dialog) {
  //    assertNull(dialog);
  //  }

  @TestTemplate
  void testIfNullUi(IfNullDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testInsertUpdateUi(InsertUpdateDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testJaninoUi(JaninoDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testJavaFilterUi(JavaFilterDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testJdbcMetadataUi(JdbcMetadataDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testJoinRowsUi(JoinRowsDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testJsonInputUi(JsonInputDialog dialog) {
    assertNull(dialog);
  }

  //  @TestTemplate
  //  void testJsonOutputUi(JsonOutputDialog dialog) {
  //    assertNull(dialog);
  //  }

  @TestTemplate
  void testJsonOutputUi(JsonOutputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testKafkaConsumerInputUi(KafkaConsumerInputDialog dialog) {
    assertNull(dialog);
  }

  //  @TestTemplate
  //  void testKafkaDialogHelperUi(KafkaDialogHelperDialog dialog) {
  //    assertNull(dialog);
  //  }

  @TestTemplate
  void testKafkaProducerOutputUi(KafkaProducerOutputDialog dialog) {
    assertNull(dialog);
  }

  //  @TestTemplate
  //  void testLanguageModelChatUi(LanguageModelChatDialog dialog) {
  //    assertNull(dialog);
  //  }

  @TestTemplate
  void testLdapInputUi(LdapInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testLdapOutputUi(LdapOutputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testLoadFileInputUi(LoadFileInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testLoopNodesImportProgressUi(LoopNodesImportProgressDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testMailUi(MailDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testMailInputUi(MailInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testMappingInputUi(MappingInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testMappingOutputUi(MappingOutputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testMemoryGroupByUi(MemoryGroupByDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testMergeJoinUi(MergeJoinDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testMergeRowsUi(MergeRowsDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testMetaInjectUi(MetaInjectDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testMetadataInputUi(MetadataInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testMonetDbBulkLoaderUi(MonetDbBulkLoaderDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testMongoDbDeleteUi(MongoDbDeleteDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testMongoDbInputUi(MongoDbInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testMongoDbOutputUi(MongoDbOutputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testMultiMergeJoinUi(MultiMergeJoinDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testNormaliserUi(NormaliserDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testNullIfUi(NullIfDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testNumberRangeUi(NumberRangeDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testOraBulkLoaderUi(OraBulkLoaderDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testPGBulkLoaderUi(PGBulkLoaderDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testPGPDecryptStreamUi(PGPDecryptStreamDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testPGPEncryptStreamUi(PGPEncryptStreamDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testPipelineExecutorUi(PipelineExecutorDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testProcessFilesUi(ProcessFilesDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testPropertyInputUi(PropertyInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testPropertyOutputUi(PropertyOutputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testRandomValueUi(RandomValueDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testRecordsFromStreamUi(RecordsFromStreamDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testRegexEvalUi(RegexEvalDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testRegexEvalHelperUi(RegexEvalHelperDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testReplaceStringUi(ReplaceStringDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testReservoirSamplingUi(ReservoirSamplingDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testRestUi(RestDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testRowGeneratorUi(RowGeneratorDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testRowsFromResultUi(RowsFromResultDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testRowsToResultUi(RowsToResultDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testRulesAccumulatorUi(RulesAccumulatorDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testRulesExecutorUi(RulesExecutorDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSQLFileOutputUi(SQLFileOutputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSalesforceDeleteUi(SalesforceDeleteDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSalesforceInputUi(SalesforceInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSalesforceInsertUi(SalesforceInsertDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSalesforceTransformUi(SalesforceTransformDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSalesforceUpdateUi(SalesforceUpdateDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSalesforceUpsertUi(SalesforceUpsertDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSampleRowsUi(SampleRowsDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSasInputUi(SasInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSchemaMappingUi(SchemaMappingDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testScriptUi(ScriptDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testScriptValuesUi(ScriptValuesDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSelectValuesUi(SelectValuesDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSetValueConstantUi(SetValueConstantDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSetValueFieldUi(SetValueFieldDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSetVariableUi(SetVariableDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSimpleMappingUi(SimpleMappingDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSnowflakeBulkLoaderUi(SnowflakeBulkLoaderDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSortRowsUi(SortRowsDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSortedMergeUi(SortedMergeDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSplitFieldToRowsUi(SplitFieldToRowsDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSplunkInputUi(SplunkInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSshUi(SshDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testStandardizePhoneNumberUi(StandardizePhoneNumberDialog dialog) {
    assertNull(dialog);
  }

  //  @TestTemplate
  //  void testStanfordSimpleNlpUi(StanfordSimpleNlpDialog dialog) {
  //    assertNull(dialog);
  //  }

  @TestTemplate
  void testStreamLookupUi(StreamLookupDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testStreamSchemaUi(StreamSchemaDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testStringCutUi(StringCutDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testStringOperationsUi(StringOperationsDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSwitchCaseUi(SwitchCaseDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSynchronizeAfterMergeUi(SynchronizeAfterMergeDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testSystemDataUi(SystemDataDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testTableCompareUi(TableCompareDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testTableExistsUi(TableExistsDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testTableInputUi(TableInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testTableOutputUi(TableOutputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testTeraFastUi(TeraFastDialog dialog) {
    assertNull(dialog);
  }

  //  @TestTemplate
  //  void testTextFileCSVImportProgressUi(TextFileCSVImportProgressDialog dialog) {
  //    assertNull(dialog);
  //  }

  @TestTemplate
  void testTextFileCSVImportProgressUi(TextFileCSVImportProgressDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testTextFileInputUi(TextFileInputDialog dialog) {
    assertNull(dialog);
  }

  //  @TestTemplate
  //  void testTextFileInputUi(TextFileInputDialog dialog) {
  //    assertNull(dialog);
  //  }

  @TestTemplate
  void testTextFileOutputUi(TextFileOutputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testTikaUi(TikaDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testTokenReplacementUi(TokenReplacementDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testTransformMetaStructureUi(TransformMetaStructureDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testUniqueRowsUi(UniqueRowsDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testUniqueRowsByHashSetUi(UniqueRowsByHashSetDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testUpdateUi(UpdateDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testUserDefinedJavaClassUi(UserDefinedJavaClassDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testValidatorUi(ValidatorDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testValueMapperUi(ValueMapperDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testVerticaBulkLoaderUi(VerticaBulkLoaderDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testWebServiceUi(WebServiceDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testWebServiceAvailableUi(WebServiceAvailableDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testWorkflowExecutorUi(WorkflowExecutorDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testWriteToLogUi(WriteToLogDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testXmlInputFieldsImportProgressUi(XmlInputFieldsImportProgressDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testXmlInputStreamUi(XmlInputStreamDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testXmlJoinUi(XmlJoinDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testXmlOutputUi(XmlOutputDialog dialog) {
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

  @TestTemplate
  void testYamlInputUi(YamlInputDialog dialog) {
    assertNull(dialog);
  }

  @TestTemplate
  void testZipFileUi(ZipFileDialog dialog) {
    assertNull(dialog);
  }
}
