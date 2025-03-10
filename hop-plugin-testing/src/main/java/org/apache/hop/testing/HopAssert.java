package org.apache.hop.testing;

import org.apache.hop.core.Result;
import org.apache.hop.pipeline.engine.EngineComponent;
import org.apache.hop.pipeline.engine.IEngineComponent;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.workflow.action.IAction;
import org.apache.hop.workflow.engine.IWorkflowEngine;
import org.hamcrest.Matchers;

import static org.hamcrest.MatcherAssert.assertThat;

public final class HopAssert {

  public static void assertEqual(ITransformMeta expected, ITransformMeta actual) {}

  public static void assertTransformMeta(ITransformMeta transformMeta) {}

  public static void assertSuccess(Result rs) {
    assertResult(rs, true, 0);
  }

  public static void assertSuccess(IEngineComponent component) {
    assertComponent(component, true, 0);
  }

  public static void assertSuccess(ITransform transform) {

    assertTransform(transform, 0, EngineComponent.ComponentExecutionStatus.STATUS_FINISHED);
  }

  public static void assertSuccess(IAction action) {
    //    assertTransform(transform, 0, EngineComponent.ComponentExecutionStatus.STATUS_FINISHED);
  }

  public static void assertSuccess(IPipelineEngine<?> engine) {}

  public static void assertSuccess(IWorkflowEngine<?> engine) {}

  public static void assertFailed(ITransform transform, long errors) {
    assertTransform(transform, errors, EngineComponent.ComponentExecutionStatus.STATUS_STOPPED);
  }

  public static void assertFailed(Result rs) {
    assertFailed(rs, -1);
  }

  public static void assertFailed(Result rs, int errors) {
    assertResult(rs, false, errors);
  }

  public static void assertTransform(
      ITransform transform, EngineComponent.ComponentExecutionStatus status) {
    assertTransform(transform, -1, status);
  }

  public static void assertTransform(
      ITransform transform, long errors, EngineComponent.ComponentExecutionStatus status) {
    assertThat("Status mismatch", transform.getStatus(), Matchers.is(status));
    if (errors >= 0) {
      assertThat("Errors mismatch", transform.getErrors(), Matchers.is(errors));
    } else {
      assertThat("Errors mismatch", transform.getErrors(), Matchers.greaterThan(0L));
    }
  }

  public static void assertRows(ITransform transform, long read, long written) {
    assertRows(transform, read, written, 0);
  }

  public static void assertRows(
      ITransform transform, long read, long written, long input, long rejected) {
    assertRows(transform, read, written, input, 0, 0, 0, rejected);
  }

  public static void assertRows(ITransform transform, long read, long written, long deleted) {
    assertRows(transform, read, written, 0, 0, 0, deleted, 0);
  }

  public static void assertRows(
      ITransform transform, long read, long written, long output, long updated, long rejected) {
    assertRows(transform, read, written, 0, output, updated, 0, rejected);
  }

  public static void assertRows(
      ITransform transform,
      long read,
      long written,
      long output,
      long updated,
      long deleted,
      long rejected) {
    assertRows(transform, read, written, 0, output, updated, deleted, rejected);
  }

  public static void assertRows(
      ITransform transform,
      long read,
      long written,
      long input,
      long output,
      long updated,
      long deleted,
      long rejected) {
    assertRows(
        new long[] {read, written, input, output, updated, deleted, rejected},
        new long[] {
          transform.getLinesRead(),
          transform.getLinesWritten(),
          transform.getLinesInput(),
          transform.getLinesOutput(),
          transform.getLinesUpdated(),
          transform.getLinesRejected()
        });
  }

  public static void assertResult(Result rs, boolean passed, long errors) {
    assertThat("Result mismatch", rs.getResult(), Matchers.is(passed));
    if (errors >= 0) {
      assertThat("Errors mismatch", rs.getNrErrors(), Matchers.is(errors));
    } else {
      assertThat("Errors mismatch", rs.getNrErrors(), Matchers.greaterThan(0L));
    }
  }

  public static void assertComponent(IEngineComponent component, boolean passed, long errors) {}

  public static void assertRows(Result result, long read, long written) {
    assertRows(result, read, written, 0);
  }

  public static void assertRows(Result result, long read, long written, long input, long rejected) {
    assertRows(result, read, written, input, 0, 0, 0, rejected);
  }

  public static void assertRows(Result result, long read, long written, long deleted) {
    assertRows(result, read, written, 0, 0, 0, deleted, 0);
  }

  public static void assertRows(
      Result result, long read, long written, long output, long updated, long rejected) {
    assertRows(result, read, written, 0, output, updated, 0, rejected);
  }

  public static void assertRows(
      Result transform,
      long read,
      long written,
      long output,
      long updated,
      long deleted,
      long rejected) {
    assertRows(transform, read, written, 0, output, updated, deleted, rejected);
  }

  public static void assertRows(
      Result rs,
      long input,
      long output,
      long read,
      long written,
      long updated,
      long deleted,
      long rejected) {
    assertRows(
        new long[] {input, output, read, written, updated, deleted, rejected},
        new long[] {
          rs.getNrLinesInput(),
          rs.getNrLinesOutput(),
          rs.getNrLinesRead(),
          rs.getNrLinesWritten(),
          rs.getNrLinesUpdated(),
          rs.getNrLinesDeleted(),
          rs.getNrLinesRejected()
        });
  }

  private static void assertRows(long[] expected, long[] actual) {
    assertThat(
        "[read, written, input, output, updated, deleted, rejected] mismatch",
        actual,
        Matchers.equalTo(expected));
  }
}
