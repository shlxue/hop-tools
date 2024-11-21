package org.apache.hop.testing.condition;

import org.apache.hop.testing.junit.StatusUtil;
import org.apache.hop.testing.junit.StoreKey;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

public class EnableOnX11Condition implements ExecutionCondition {
  //  static final ConditionEvaluationResult DEFAULT = enabled("@DisableOnX11 is not present");

  static final ConditionEvaluationResult ENABLED_ON_X =
      enabled("Enabled UI tests on support X11's os");
  static final ConditionEvaluationResult DISABLED_ON_NON_X =
      disabled("Disabled UI tests on no X11's os");

  private final Logger logger = LoggerFactory.getLogger(EnableOnX11Condition.class);

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    logger.trace("Check x11 condition");
    ConditionEvaluationResult result = DISABLED_ON_NON_X;
    try {
      if (GraphicsEnvironment.getLocalGraphicsEnvironment() != null) {
        StatusUtil.set(context, StoreKey.HOP_GUI_HEADLESS, GraphicsEnvironment.isHeadless());
        result = ENABLED_ON_X;
      }
    } catch (Throwable throwable) {
      result = disabled(DISABLED_ON_NON_X.getReason().orElseThrow(), throwable.getMessage());
    }
    //    if (hasAnnotation(context)) {
    //    }
    if (result.isDisabled()) {
      logger.warn(result.getReason().orElseThrow());
    }
    return result;
  }

  //  private boolean hasAnnotation(ExtensionContext context) {
  //    if (AnnotationUtils.findAnnotation(context.getElement(), EnableOnX11.class).isPresent()) {
  //      return true;
  //    }
  //    Optional<Class<?>> testClassOptional = context.getTestClass();
  //    if (testClassOptional.isPresent()) {
  //      Class<?> testClass = testClassOptional.get();
  //      return testClass.getDeclaredAnnotation(EnableOnX11.class) != null;
  //    }
  //    return false;
  //  }
}
