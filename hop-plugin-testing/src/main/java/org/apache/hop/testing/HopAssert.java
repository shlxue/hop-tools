package org.apache.hop.testing;

import org.apache.hop.core.Result;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.engine.EngineComponent;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.ITransformData;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class HopAssert {

  public static void assertTransform(Pipeline trans, boolean run, boolean stop, boolean finish) {
    //    assertThat(trans.isRunning()).isEqualTo(run);
    //    assertThat(trans.isStopped()).isEqualTo(stop);
    //    assertThat(trans.isFinished()).isEqualTo(finish);
  }

  public static void assertTransform(
      Pipeline trans, boolean run, boolean stop, boolean finish, boolean init, boolean prepare) {
    //    assertThat(trans.isInitializing()).isEqualTo(init);
    //    assertThat(trans.isPreparing()).isEqualTo(prepare);
    assertTransform(trans, run, stop, finish);
  }

  public static void assertResult(
      Result actual, int exitStatus, boolean stopped, boolean safeStop) {
    //    assertThat(actual.getExitStatus()).isEqualTo(exitStatus);
    //    assertThat(actual.isStopped()).isEqualTo(stopped);
    //        assertThat(actual.isSafeStop()).isEqualTo(safeStop);
  }

  public static void assertResult(Result actual, int exitStatus, boolean result) {
    //    assertThat(actual.getExitStatus()).isEqualTo(exitStatus);
    //    assertThat(actual.getResult()).isEqualTo(result);
  }

  public static void assertResult(Result actual, long entry, long errors, long retrievedFiles) {
    //    assertThat(actual.getEntryNr()).isEqualTo(entry);
    //    assertThat(actual.getNrErrors()).isEqualTo(errors);
    //    assertThat(actual.getNrFilesRetrieved()).isEqualTo(retrievedFiles);
  }

  public static void assertLines(Result actual, long input, long output) {
    //    assertThat(actual.getNrLinesInput()).isEqualTo(input);
    //    assertThat(actual.getNrLinesOutput()).isEqualTo(output);
  }

  public static void assertLines(
      Result actual, long read, long written, long updated, long deleted, long rejected) {
    //    assertThat(actual.getNrLinesRead()).isEqualTo(read);
    //    assertThat(actual.getNrLinesWritten()).isEqualTo(written);
    //    assertThat(actual.getNrLinesUpdated()).isEqualTo(updated);
    //    assertThat(actual.getNrLinesDeleted()).isEqualTo(deleted);
    //    assertThat(actual.getNrLinesRejected()).isEqualTo(rejected);
  }

  public static void assertRows(ITransform transform, int nrRows, IRowMeta rowMeta) {}

  public static void assertLines(
      Result actual, long input, long output, long read, long u, long w, long d, long r) {
    assertLines(actual, input, output);
    assertLines(actual, read, u, w, d, r);
  }

  public static void assertStep(
      ITransformData stepData, EngineComponent.ComponentExecutionStatus status) {
    assertEquals(status, stepData.getStatus());
  }

  public static void assertStep(ITransform step, int read, int write, int error) {
    assertStep(
        step,
        "[read, write, error] for step " + step.getTransformName(),
        v -> new long[] {v.getLinesRead(), v.getLinesWritten(), v.getErrors()},
        read,
        write,
        error);
  }

  public static void assertStep(ITransform step, int read, int write, int in, int out) {
    assertStep(
        step,
        "[read, write, input, output] for step " + step.getTransformName(),
        v ->
            new long[] {
              v.getLinesRead(), v.getLinesWritten(), v.getLinesInput(), v.getLinesOutput()
            },
        read,
        write,
        in,
        out);
  }

  public static void assertStepInput(
      ITransform step, int read, int write, int error, int input, int skip) {
    assertStep(
        step,
        "[read, write, error, input, skip] for step " + step.getTransformName(),
        v ->
            new long[] {
              v.getLinesRead(),
              v.getLinesWritten(),
              v.getErrors(),
              v.getLinesInput(),
              v.getLinesSkipped()
            },
        read,
        write,
        error,
        input,
        skip);
  }

  public static void assertStepOutput(
      ITransform step, int read, int write, int error, int output, int update, int reject) {
    assertStep(
        step,
        "[read, write, error, output, update, reject] for " + step.getTransformName(),
        v ->
            new long[] {
              v.getLinesRead(),
              v.getLinesWritten(),
              v.getErrors(),
              v.getLinesOutput(),
              v.getLinesUpdated(),
              v.getLinesRejected()
            },
        read,
        write,
        error,
        output,
        update,
        reject);
  }

  private static void assertStep(
      ITransform step, String msg, Function<BaseTransform, long[]> function, long... args) {
    //    Assertions.assertThat(step).isInstanceOf(BaseTransform.class);
    //    assertThat(
    //        msg, function.apply((BaseTransform) step), CoreMatchers.equalTo(args));
    //        assertThat(function.apply((BaseStep) step)).isEqualTo(args);
    //        assertArrayEquals(args, function.apply((BaseStep) step), msg);
  }

  //    public static void assertLines(Result actual, long entry, long retrieveFiles, long errors) {
  ////        assertThat(actual.getEntryNr(), is(entry));
  ////        assertThat(actual.getNrFilesRetrieved(), is(retrieveFiles));
  ////        assertThat(actual.getNrErrors(), is(errors));
  //    }

  //    public static void assertLogBuffer() {
  //    }
}
