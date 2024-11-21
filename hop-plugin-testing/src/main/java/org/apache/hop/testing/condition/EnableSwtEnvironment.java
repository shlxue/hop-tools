package org.apache.hop.testing.condition;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

public class EnableSwtEnvironment implements ExecutionCondition {
  private static final String DISPLAY_CLASS = "org.eclipse.swt.widgets.Display";

  static final ConditionEvaluationResult DISABLED_ON_APPKIT =
      disabled(
          "Missing -XstartOnFirstThread on jvm options",
          "See: the java command, Extra Options for macOS");
  static final ConditionEvaluationResult ENABLED_WHEN_SWT = enabled("Enabled SWT UI tests");
  static final ConditionEvaluationResult DISABLED_WHEN_NOT_SWT =
      disabled("Disabled UI tests when not found swt library");

  private final Logger logger = LoggerFactory.getLogger(EnableSwtEnvironment.class);

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    logger.trace("Check swt environment condition");
    ConditionEvaluationResult result = ENABLED_WHEN_SWT;
    //    if (context.getStore(HopJunit.GUI_NS).get("HEADLESS", Boolean.class) == null) {
    //      result = disabled("Disabled swt on non-X11 os");
    //    } else {
    //      try {
    //        Class<?> displayType = getClass().getClassLoader().loadClass(DISPLAY_CLASS);
    //        if (displayType.getDeclaredMethod("getDefault").invoke(null) == null) {
    //          logger.info(DISABLED_WHEN_NOT_SWT.toString());
    //          result = DISABLED_WHEN_NOT_SWT;
    //          //        } else if (OS.MAC.isCurrentOs() && !jvmStartOnFirstThread()) {
    //          //          result = DISABLED_ON_APPKIT;
    //        }
    //      } catch (Throwable throwable) {
    //        result = disabled(DISABLED_WHEN_NOT_SWT.getReason().orElseThrow(),
    // throwable.getMessage());
    //      }
    //    }
    if (result.isDisabled()) {
      logger.warn(result.getReason().orElseThrow());
    }
    return result;
  }

  private boolean jvmStartOnFirstThread() {
    RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
    String opt = "-XstartOnFirstThread".toLowerCase();
    return mxBean.getInputArguments().stream().anyMatch(s -> s.toLowerCase().contains(opt));
  }
}
