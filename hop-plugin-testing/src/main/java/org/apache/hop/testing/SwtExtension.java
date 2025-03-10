package org.apache.hop.testing;

import org.apache.hop.testing.extension.TestTemplates;
import org.apache.hop.testing.junit.StatusUtil;
import org.apache.hop.testing.junit.StoreKey;
import org.apache.hop.testing.junit.SwtContext;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

public final class SwtExtension
    implements BeforeAllCallback, AfterAllCallback, TestTemplateInvocationContextProvider {
  private static final Logger logger = LoggerFactory.getLogger(SwtExtension.class);

  private final SwtContext swtContext;
  private final TestTemplateInvocationContextProvider swtUiProvider;
  private boolean active;

  public SwtExtension() {
    this.swtContext = SwtContext.getInstance();
    this.swtUiProvider = TestTemplates.swtUiProvider();
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    active = supportUi(testClass);
    if (active) {
      StatusUtil.set(context, StoreKey.HOP_SWT_CONTEXT, swtContext);
      Shell shell = swtContext.getShell();
      StatusUtil.set(context, StoreKey.HOP_SWT_SHELL, shell);
      injectShellField(testClass, shell);
      logger.debug("Inject swt to junit store for " + testClass);
    } else {
      logger.trace("Not inject to swt extension: " + testClass);
    }
  }

  @Override
  public void afterAll(ExtensionContext context) {
    if (active) {
      logger.debug("Clean swt status from junit store");
      StatusUtil.remove(context, StoreKey.HOP_SWT_CONTEXT);
      StatusUtil.remove(context, StoreKey.HOP_SWT_SHELL);
      active = false;
    }
  }

  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    return active && swtUiProvider.supportsTestTemplate(context);
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
      ExtensionContext context) {
    return swtUiProvider.provideTestTemplateInvocationContexts(context);
  }

  private boolean supportUi(Class<?> testClass) {
    SpecMode specMode = StatusUtil.uiSpec(testClass.getAnnotation(HopEnv.class));
    if (swtContext.supportX11()) {
      return specMode != SpecMode.NONE;
    }
    return swtContext.supportX11() && specMode != SpecMode.NONE;
  }

  private boolean isShellField(Field field) {
    int modifiers = field.getModifiers();
    return Modifier.isStatic(modifiers)
        && !Modifier.isFinal(modifiers)
        && field.getType().equals(Shell.class);
  }

  private void injectShellField(Class<?> testClass, Shell shell) {
    for (Field field : testClass.getDeclaredFields()) {
      if (isShellField(field)) {
        try {
          ReflectionUtils.makeAccessible(field);
          field.set(null, shell);
        } catch (Throwable throwable) {
          logger.debug("Ignore field {}", field, throwable);
        }
      }
    }
  }
}
